package org.example.services.impl;

import groovy.util.logging.Log;
import jakarta.transaction.Transactional;
import org.example.clients.OpenAiClient;
import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.dtos.exportDtos.MessageResponseDto;
import org.example.models.entities.*;
import org.example.models.entities.enums.MessageRole;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.repositories.*;
import org.example.services.*;
import org.example.utils.TextUtils;
import org.example.utils.VectorUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;


@Service
public class ChatServiceImpl implements ChatService {
    private static final String MSG_KEY_NOT_FOUND = "error.chat.notfound";
    private static final String MSG_KEY_DENIED = "error.chat.denied";
    private static final String PROMPT_TEMPLATE = "Context:\n%s\n\nQuestion: %s";
    private static final int DEFAULT_TOP_K = 5;
    private final ChatRepository chatRepository;
    private final DocumentProcessingService documentProcessingService;
    private final MessageService messageService;
    private final EmbeddingModel embeddingModel;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final ProcessingJobService processingJobService;
    private final DocumentService documentService;
    private final OpenAiClient openAiClient;
    private final MessageSource messageSource;
    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    public ChatServiceImpl(ChatRepository chatRepository,
                           DocumentProcessingService documentProcessingService,
                           MessageService messageService,
                           EmbeddingModel embeddingModel,
                           ModelMapper modelMapper,
                           UserService userService,
                           ProcessingJobService processingJobService,
                           DocumentService documentService,
                           OpenAiClient openAiClient, MessageSource messageSource) {
        this.chatRepository = chatRepository;
        this.documentProcessingService = documentProcessingService;
        this.messageService = messageService;
        this.embeddingModel = embeddingModel;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.processingJobService = processingJobService;
        this.documentService = documentService;
        this.openAiClient = openAiClient;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional
    public ChatViewDto startNewChat(MultipartFile file, String userEmail) throws IOException {
        UserEntity user = userService.findUserByEmail(userEmail);
        Document document = documentService.saveDocument(file);
        processingJobService.createProcessingJob(document);
        Chat chat = saveNewChat(user, document);

        triggerAsyncProcessing(document.getId());

        return mapToChatViewDto(chat);
    }

    @Override
    @Transactional
    public ChatResponseDto generateResponse(Long chatId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException(
                        messageSource.getMessage(MSG_KEY_NOT_FOUND, null, LocaleContextHolder.getLocale())
                ));

        messageService.saveMessage(chat, content, MessageRole.USER);

        List<ChunkSearchResult> topResults = searchContext(chat.getDocument().getId(), content);

        List<ChunkSearchResult> relevantResults = topResults.stream()
                .filter(result -> result.getSimilarity() >= 0.35)
                .toList();

        String contextText;
        if (relevantResults.isEmpty()) {
            contextText = "No relevant information was found in the provided document regarding the user's query.";
        } else {
            contextText = TextUtils.joinChunkContents(relevantResults, "\n---\n");
        }

        String threadId = getOrInitThread(chat);
        String combinedPrompt = String.format(PROMPT_TEMPLATE, contextText, content);
        String aiResponse = openAiClient.askAssistant(threadId, combinedPrompt);

        Message aiMessage = messageService.saveMessage(chat, aiResponse, MessageRole.ASSISTANT);
        messageService.saveMessageSources(aiMessage, relevantResults);

        return new ChatResponseDto(aiResponse, aiMessage.getId());
    }

    @Override
    public String findUserEmailByDocument(Document document) {
        return chatRepository.findByDocument(document)
                .map(chat -> chat.getUser().getEmail())
                .orElse("System/Unknown");
    }

    @Override
    public ChatViewDto getChatDetails(Long id, String gmail) {
        Chat chat = chatRepository.findByIdAndUserEntityEmail(id, gmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        messageSource.getMessage(MSG_KEY_DENIED, null, LocaleContextHolder.getLocale())
                ));

        ChatViewDto dto = modelMapper.map(chat, ChatViewDto.class);
        dto.setDocumentFilename(chat.getDocument().getFilename());
        dto.setDocumentId(chat.getDocument().getId());
        dto.setStage(resolveProcessingStage(chat));

        List<MessageResponseDto> messages = mapMessages(chat.getMessages());
        dto.setMessages(messages);
        dto.setLastMessageAt(determineLastMessageDate(messages));

        return dto;
    }

    @Override
    public List<Chat> findAllChatsByUserEntityId(long userId) {
        return chatRepository.findAllByUserEntityId(userId);
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId, String userEmail) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (!chat.getUserEntity().getEmail().equals(userEmail)) {
            throw new IllegalStateException("Unauthorized to delete this chat");
        }

        messageService.deleteMessagesByChatId(chatId);

        chatRepository.delete(chat);
    }

    private String getOrInitThread(Chat chat) {
        if (chat.getOpenAiThreadId() == null) {
            String threadId = openAiClient.createThread();
            chat.setOpenAiThreadId(threadId);
            chatRepository.save(chat);
        }
        return chat.getOpenAiThreadId();
    }

    private List<ChunkSearchResult> searchContext(Long documentId, String query) {
        float[] queryVector = VectorUtils.toFloatArray(embeddingModel.embed(query));
        return this.documentProcessingService.findTopSimilar(documentId, queryVector, DEFAULT_TOP_K);
    }

    private void triggerAsyncProcessing(Long documentId) {
        log.info("Иницииране на асинхронна обработка за документ ID: {}", documentId);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("Транзакцията е комитната. Стартиране на фонова нишка за ID: {}", documentId);

                    CompletableFuture.runAsync(() -> {
                        try {
                            documentProcessingService.processDocument(documentId);
                            log.info("Методът processDocument е извикан успешно за ID: {}", documentId);
                        } catch (Exception e) {
                            log.error("КРИТИЧНА ГРЕШКА във фоновата нишка за документ " + documentId, e);
                        }
                    }, CompletableFuture.delayedExecutor(1500, TimeUnit.MILLISECONDS));
                }
            });
        } else {
            log.warn("Няма активна транзакция. Стартиране на директна обработка.");
            documentProcessingService.processDocument(documentId);
        }
    }

    private Chat saveNewChat(UserEntity user, Document document) {
        Chat chat = new Chat(
                document.getFilename(),
                user,
                document,
                LocalDateTime.now()
        );
        return chatRepository.save(chat);
    }

    private ChatViewDto mapToChatViewDto(Chat chat) {
        ChatViewDto dto = this.modelMapper.map(chat, ChatViewDto.class);
        dto.setLastMessageAt(chat.getLastMessageAt());
        if (chat.getDocument() != null) {
            dto.setDocumentFilename(chat.getDocument().getFilename());
            dto.setDocumentId(chat.getDocument().getId());
            if (chat.getDocument().getProcessingJob() != null) {
                dto.setStage(chat.getDocument().getProcessingJob().getStage());
            } else {
                dto.setStage(ProcessingJobStage.UPLOADED);
            }
        }
        return dto;
    }

    private ProcessingJobStage resolveProcessingStage(Chat chat) {
        ProcessingJob job = chat.getDocument().getProcessingJob();
        return (job != null) ? job.getStage() : ProcessingJobStage.UPLOADED;
    }

    private List<MessageResponseDto> mapMessages(List<Message> messages) {
        if (messages == null) return Collections.emptyList();
        return messages.stream()
                .map(this::toMessageResponseDto)
                .sorted(Comparator.comparing(MessageResponseDto::getCreatedAt))
                .collect(Collectors.toList());
    }

    private MessageResponseDto toMessageResponseDto(Message message) {
        return new MessageResponseDto(
                message.getContent(),
                message.getRole().name(),
                message.getCreatedAt(),
                extractSourceTexts(message)
        );
    }

    private List<String> extractSourceTexts(Message message) {
        if (message.getRole() != MessageRole.ASSISTANT || message.getContextSources() == null) {
            return Collections.emptyList();
        }
        return message.getContextSources().stream()
                .map(source -> source.getChunk().getContent())
                .collect(Collectors.toList());
    }

    private LocalDateTime determineLastMessageDate(List<MessageResponseDto> messages) {
        return messages.isEmpty() ? null : messages.get(messages.size() - 1).getCreatedAt();
    }
}