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
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ChatRepository chatRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final DocumentProcessingService documentProcessingService;
    private final MessageService messageService;
    private final EmbeddingModel embeddingModel;
    private final DocumentChunkRepository documentChunkRepository;
    private final ChatModel chatModel;

    private final String assistantId = "asst_aAiBqIjw5EolrhSfnQSZAdwL";

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public ChatServiceImpl(UserRepository userRepository, DocumentRepository documentRepository, ChatRepository chatRepository, ProcessingJobRepository processingJobRepository, DocumentProcessingService documentProcessingService, MessageService messageService, EmbeddingModel embeddingModel, DocumentChunkRepository documentChunkRepository, ChatModel chatModel) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.processingJobRepository = processingJobRepository;
        this.documentProcessingService = documentProcessingService;
        this.messageService = messageService;
        this.embeddingModel = embeddingModel;
        this.documentChunkRepository = documentChunkRepository;
        this.chatModel = chatModel;
    }

    @Override
    @Transactional
    public ChatViewDto startNewChat(MultipartFile file, String userEmail) throws IOException {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Document document = new Document();
        document.setFilename(file.getOriginalFilename());
        document.setMimeType(file.getContentType());
        document.setStatus(DocumentStatus.UPLOADED);
        document.setUploadedAt(LocalDateTime.now());
        document.setContent(file.getBytes());
        document = documentRepository.save(document);

        ProcessingJob job = new ProcessingJob();
        job.setDocument(document);
        job.setStage(ProcessingJobStage.UPLOADED);
        processingJobRepository.save(job);

        Chat chat = new Chat();
        chat.setTitle("Chat regarding: " + file.getOriginalFilename());
        chat.setUser(user);
        chat.setDocument(document);
        chat.setLastMessageAt(LocalDateTime.now());
        chat = chatRepository.save(chat);

        ChatViewDto chatViewDto = new ChatViewDto();
        chatViewDto.setId(chat.getId());
        chatViewDto.setTitle(chat.getTitle());
        chatViewDto.setDocumentFilename(document.getFilename());
        chatViewDto.setLastMessageAt(chat.getLastMessageAt());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            Document finalDocument = document;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    documentProcessingService.processDocument(finalDocument.getId());
                }
            });
        } else {
            documentProcessingService.processDocument(document.getId());
        }

        return chatViewDto;
    }

    @Override
    @Transactional
    public ChatResponseDto generateResponse(Long chatId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        messageService.saveMessage(chat, content, MessageRole.USER);

        float[] queryVector = convertToFloatArray(embeddingModel.embed(content));
        List<ChunkSearchResult> topResults = documentChunkRepository.findTopSimilar(
                chat.getDocument().getId(), queryVector, 5);

        String contextText = topResults.stream()
                .map(ChunkSearchResult::getContent)
                .collect(Collectors.joining("\n---\n"));

        if (chat.getOpenAiThreadId() == null) {
            String threadId = createThread();
            chat.setOpenAiThreadId(threadId);
            chatRepository.save(chat);
        }

        String threadId = chat.getOpenAiThreadId();

        String combinedMessage = "Context from PDF:\n" + contextText + "\n\nUser Question: " + content;
        addMessageToThread(threadId, combinedMessage);

        String runId = createRun(threadId);

        waitForRunCompletion(threadId, runId);

        String aiAnswer = getLastAssistantMessage(threadId);

        Message aiMessage = messageService.saveMessage(chat, aiAnswer, MessageRole.ASSISTANT);
        messageService.saveMessageSources(aiMessage, topResults);

        return new ChatResponseDto(aiAnswer, aiMessage.getId());
    }


    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");
        headers.set("OpenAI-Beta", "assistants=v2");
        return headers;
    }

    private String createThread() {
        HttpEntity<String> entity = new HttpEntity<>("{}", getHeaders());
        Map<String, Object> response = restTemplate.postForObject("https://api.openai.com/v1/threads", entity, Map.class);
        return (String) response.get("id");
    }

    private void addMessageToThread(String threadId, String content) {
        Map<String, String> body = Map.of("role", "user", "content", content);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, getHeaders());
        restTemplate.postForObject("https://api.openai.com/v1/threads/" + threadId + "/messages", entity, Map.class);
    }

    private String createRun(String threadId) {
        Map<String, String> body = Map.of("assistant_id", assistantId);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, getHeaders());
        Map<String, Object> response = restTemplate.postForObject("https://api.openai.com/v1/threads/" + threadId + "/runs", entity, Map.class);
        return (String) response.get("id");
    }

    private void waitForRunCompletion(String threadId, String runId) {
        String status = "";
        while (!status.equals("completed")) {
            try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            Map<String, Object> response = restTemplate.exchange(
                    "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId,
                    HttpMethod.GET, entity, Map.class).getBody();
            status = (String) response.get("status");
            if (status.equals("failed")) throw new RuntimeException("Assistant run failed");
        }
    }

    private String getLastAssistantMessage(String threadId) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        Map<String, Object> response = restTemplate.exchange(
                "https://api.openai.com/v1/threads/" + threadId + "/messages",
                HttpMethod.GET, entity, Map.class).getBody();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        Map<String, Object> lastMsg = data.get(0);
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) lastMsg.get("content");
        Map<String, Object> textObj = (Map<String, Object>) contentList.get(0).get("text");
        return (String) textObj.get("value");
    }

    private float[] convertToFloatArray(List<Double> doubles) {
        float[] floats = new float[doubles.size()];
        for (int i = 0; i < doubles.size(); i++) floats[i] = doubles.get(i).floatValue();
        return floats;
    }

    @Override
    public ChatViewDto getChatDetails(Long id) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        ChatViewDto dto = new ChatViewDto();
        dto.setId(chat.getId());
        dto.setTitle(chat.getTitle());
        dto.setDocumentFilename(chat.getDocument().getFilename());

        if (chat.getMessages() != null && !chat.getMessages().isEmpty()) {
            LocalDateTime lastDate = chat.getMessages().stream()
                    .map(Message::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            dto.setLastMessageAt(lastDate);
        }

        List<MessageResponseDto> messageDtos = chat.getMessages().stream()
                .map(m -> new MessageResponseDto(m.getContent(), m.getRole().name(), m.getCreatedAt()))
                .sorted(Comparator.comparing(MessageResponseDto::getCreatedAt))
                .collect(Collectors.toList());

        dto.setMessages(messageDtos);

        return dto;
    }
}
