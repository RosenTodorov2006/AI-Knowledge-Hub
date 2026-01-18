package org.example.services.impl;

import jakarta.transaction.Transactional;
import org.example.models.dtos.exportDtos.ChatViewDto;
import org.example.models.entities.Chat;
import org.example.models.entities.Document;
import org.example.models.entities.ProcessingJob;
import org.example.models.entities.UserEntity;
import org.example.models.entities.enums.DocumentStatus;
import org.example.models.entities.enums.ProcessingJobStage;
import org.example.repositories.ChatRepository;
import org.example.repositories.DocumentRepository;
import org.example.repositories.ProcessingJobRepository;
import org.example.repositories.UserRepository;
import org.example.services.ChatService;
import org.example.services.DocumentProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class ChatServiceImpl implements ChatService {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ChatRepository chatRepository;
    private final ProcessingJobRepository processingJobRepository;
    private final DocumentProcessingService documentProcessingService;

    public ChatServiceImpl(UserRepository userRepository, DocumentRepository documentRepository, ChatRepository chatRepository, ProcessingJobRepository processingJobRepository, DocumentProcessingService documentProcessingService) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.processingJobRepository = processingJobRepository;
        this.documentProcessingService = documentProcessingService;
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
        document.setContent(file.getBytes()); // Вече добавихме това поле
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
}
