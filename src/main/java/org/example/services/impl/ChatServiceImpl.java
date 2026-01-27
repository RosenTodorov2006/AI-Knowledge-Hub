package org.example.services.impl;

import jakarta.transaction.Transactional;
import org.example.models.dtos.exportDtos.ChatResponseDto;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.dtos.exportDtos.MessageResponseDto;
import org.example.models.entities.*;
import org.example.models.entities.enums.DocumentStatus;
import org.example.models.entities.enums.MessageRole;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.repositories.*;
import org.example.services.ChatService;
import org.example.services.DocumentProcessingService;
import org.example.services.MessageService;
import org.modelmapper.ModelMapper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.services.impl.UserServiceImpl.ERR_USER_NOT_FOUND;

@Service
public class ChatServiceImpl implements ChatService {
    public static final String ERR_CHAT_NOT_FOUND = "Chat not found";
    public static final String ERR_USER_NOT_FOUND = "User not found!";
    private static final String CONTEXT_SEPARATOR = "\n---\n";
    private static final String PROMPT_TEMPLATE = "Context:\n%s\n\nQuestion: %s";
    private static final int DEFAULT_TOP_K = 5;
    private static final String OPENAI_THREADS_URL = "https://api.openai.com/v1/threads";
    private static final String URL_SEPARATOR = "/";
    private static final String ASSISTANT_ID = "asst_aAiBqIjw5EolrhSfnQSZAdwL";
    private static final String PATH_MESSAGES = "/messages";
    private static final String PATH_RUNS = "/runs";
    private static final String OPENAI_BETA_HEADER = "OpenAI-Beta";
    private static final String OPENAI_BETA_VERSION = "assistants=v2";
    private static final String AUTH_BEARER_PREFIX = "Bearer ";
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_STATUS = "status";
    private static final String JSON_KEY_ROLE = "role";
    private static final String JSON_KEY_CONTENT = "content";
    private static final String JSON_KEY_DATA = "data";
    private static final String JSON_KEY_TEXT = "text";
    private static final String JSON_KEY_VALUE = "value";
    private static final String JSON_KEY_ASSISTANT_ID = "assistant_id";
    private static final String ROLE_USER = "user";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_FAILED = "failed";
    private static final String EMPTY_JSON = "{}";
    private static final long POLLING_INTERVAL_MS = 1000;
    private static final int FIRST_INDEX = 0;
    private static final String ERR_ASSISTANT_RUN_FAILED = "Assistant run failed";
    public static final String ERR_ACCESS_DENIED = "You do not have access to this chat!";
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ChatRepository chatRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final DocumentProcessingService documentProcessingService;
    private final MessageService messageService;
    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;
    private final ModelMapper modelMapper;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public ChatServiceImpl(UserRepository userRepository, DocumentRepository documentRepository, ChatRepository chatRepository, ProcessingJobRepository processingJobRepository, DocumentProcessingService documentProcessingService, MessageService messageService, EmbeddingModel embeddingModel, DocumentChunkRepository documentChunkRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.processingJobRepository = processingJobRepository;
        this.documentProcessingService = documentProcessingService;
        this.messageService = messageService;
        this.embeddingModel = embeddingModel;
        this.documentChunkRepository = documentChunkRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public ChatViewDto startNewChat(MultipartFile file, String userEmail) throws IOException {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(ERR_USER_NOT_FOUND));

        Document document = saveNewDocument(file);
        createProcessingJob(document);
        Chat chat = saveNewChat(user, document);

        triggerAsyncProcessing(document.getId());

        return mapToChatViewDto(chat);
    }

    private Document saveNewDocument(MultipartFile file) throws IOException {
        Document document = new Document();
        document.setFilename(file.getOriginalFilename());
        document.setMimeType(file.getContentType());
        document.setDocumentStatus(DocumentStatus.UPLOADED);
        document.setUploadedAt(LocalDateTime.now());
        document.setContent(file.getBytes());
        return documentRepository.save(document);
    }

    private void createProcessingJob(Document document) {
        ProcessingJob job = new ProcessingJob();
        job.setDocument(document);
        job.setStage(ProcessingJobStage.UPLOADED);
        processingJobRepository.save(job);
    }

    private Chat saveNewChat(UserEntity user, Document document) {
        Chat chat = new Chat();
        chat.setTitle(document.getFilename());
        chat.setUser(user);
        chat.setDocument(document);
        chat.setLastMessageAt(LocalDateTime.now());
        return chatRepository.save(chat);
    }

    private void triggerAsyncProcessing(Long documentId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    documentProcessingService.processDocument(documentId);
                }
            });
        } else {
            documentProcessingService.processDocument(documentId);
        }
    }

    private ChatViewDto mapToChatViewDto(Chat chat) {
        ChatViewDto dto = new ChatViewDto();
        dto.setId(chat.getId());
        dto.setTitle(chat.getTitle());
        dto.setDocumentFilename(chat.getDocument().getFilename());
        dto.setLastMessageAt(chat.getLastMessageAt());
        return dto;
    }

    @Override
    @Transactional
    public ChatResponseDto generateResponse(Long chatId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException(ERR_CHAT_NOT_FOUND));
        messageService.saveMessage(chat, content, MessageRole.USER);

        List<ChunkSearchResult> topResults = searchContext(chat.getDocument().getId(), content);
        String contextText = buildContextString(topResults);

        String threadId = getOrInitThread(chat);
        String aiAnswer = getAssistantReply(threadId, content, contextText);

        Message aiMessage = messageService.saveMessage(chat, aiAnswer, MessageRole.ASSISTANT);
        messageService.saveMessageSources(aiMessage, topResults);

        return new ChatResponseDto(aiAnswer, aiMessage.getId());
    }

    private List<ChunkSearchResult> searchContext(Long documentId, String query) {
        float[] queryVector = convertToFloatArray(embeddingModel.embed(query));
        return documentChunkRepository.findTopSimilar(documentId, queryVector, DEFAULT_TOP_K);
    }

    private String buildContextString(List<ChunkSearchResult> results) {
        return results.stream()
                .map(ChunkSearchResult::getContent)
                .collect(Collectors.joining(CONTEXT_SEPARATOR));
    }

    private String getOrInitThread(Chat chat) {
        if (chat.getOpenAiThreadId() == null) {
            String threadId = createThread();
            chat.setOpenAiThreadId(threadId);
            chatRepository.save(chat);
        }
        return chat.getOpenAiThreadId();
    }

    private String getAssistantReply(String threadId, String question, String context) {
        String combinedPrompt = String.format(PROMPT_TEMPLATE, context, question);

        addMessageToThread(threadId, combinedPrompt);
        String runId = createRun(threadId);
        waitForRunCompletion(threadId, runId);

        return getLastAssistantMessage(threadId);
    }

    private String createThread() {
        HttpEntity<String> entity = new HttpEntity<>(EMPTY_JSON, getHeaders());
        Map<String, Object> response = restTemplate.postForObject(OPENAI_THREADS_URL, entity, Map.class);
        return (String) Objects.requireNonNull(response).get(JSON_KEY_ID);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AUTH_BEARER_PREFIX + apiKey);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(OPENAI_BETA_HEADER, OPENAI_BETA_VERSION);
        return headers;
    }

    private void addMessageToThread(String threadId, String content) {
        Map<String, String> body = Map.of(JSON_KEY_ROLE, ROLE_USER, JSON_KEY_CONTENT, content);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, getHeaders());
        String url = OPENAI_THREADS_URL + URL_SEPARATOR + threadId + PATH_MESSAGES;
        restTemplate.postForObject(url, entity, Map.class);
    }

    private String createRun(String threadId) {
        Map<String, String> body = Map.of(JSON_KEY_ASSISTANT_ID, ASSISTANT_ID);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, getHeaders());
        String url = OPENAI_THREADS_URL + URL_SEPARATOR + threadId + PATH_RUNS;
        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        return (String) Objects.requireNonNull(response).get(JSON_KEY_ID);
    }

    private void waitForRunCompletion(String threadId, String runId) {
        String status = "";
        String runUrl = OPENAI_THREADS_URL + URL_SEPARATOR + threadId + PATH_RUNS + URL_SEPARATOR + runId;

        while (!STATUS_COMPLETED.equals(status)) {
            try {
                Thread.sleep(POLLING_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            Map<String, Object> response = restTemplate.exchange(runUrl, HttpMethod.GET, entity, Map.class).getBody();

            status = (String) Objects.requireNonNull(response).get(JSON_KEY_STATUS);
            if (STATUS_FAILED.equals(status)) {
                throw new RuntimeException(ERR_ASSISTANT_RUN_FAILED);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String getLastAssistantMessage(String threadId) {
        String messagesUrl = OPENAI_THREADS_URL + URL_SEPARATOR + threadId + PATH_MESSAGES;
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        Map<String, Object> response = restTemplate.exchange(messagesUrl, HttpMethod.GET, entity, Map.class).getBody();

        List<Map<String, Object>> data = (List<Map<String, Object>>) Objects.requireNonNull(response).get(JSON_KEY_DATA);
        Map<String, Object> lastMsg = data.get(FIRST_INDEX);
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) lastMsg.get(JSON_KEY_CONTENT);
        Map<String, Object> textObj = (Map<String, Object>) contentList.get(FIRST_INDEX).get(JSON_KEY_TEXT);

        return (String) textObj.get(JSON_KEY_VALUE);
    }

    private float[] convertToFloatArray(List<Double> doubles) {
        float[] floats = new float[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) {
            floats[i] = doubles.get(i).floatValue();
        }
        return floats;
    }

    @Override
    public ChatViewDto getChatDetails(Long id, String gmail) {
        Chat chat = chatRepository.findByIdAndUserEntityEmail(id, gmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, ERR_ACCESS_DENIED));

        ChatViewDto dto = modelMapper.map(chat, ChatViewDto.class);

        dto.setDocumentFilename(chat.getDocument().getFilename());
        dto.setDocumentId(chat.getDocument().getId());
        dto.setStage(resolveProcessingStage(chat));

        List<MessageResponseDto> messages = mapMessages(chat.getMessages());
        dto.setMessages(messages);
        dto.setLastMessageAt(determineLastMessageDate(messages));

        return dto;
    }

    private ProcessingJobStage resolveProcessingStage(Chat chat) {
        ProcessingJob job = chat.getDocument().getProcessingJob();
        return (job != null) ? job.getStage() : ProcessingJobStage.UPLOADED;
    }

    private List<MessageResponseDto> mapMessages(List<Message> messages) {
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages.stream()
                .map(this::toMessageResponseDto)
                .sorted(Comparator.comparing(MessageResponseDto::getCreatedAt))
                .collect(Collectors.toList());
    }

    private MessageResponseDto toMessageResponseDto(Message message) {
        List<String> sourceTexts = extractSourceTexts(message);

        return new MessageResponseDto(
                message.getContent(),
                message.getRole().name(),
                message.getCreatedAt(),
                sourceTexts
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
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1).getCreatedAt();
    }
}
