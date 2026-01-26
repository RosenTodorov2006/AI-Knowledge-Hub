package org.example.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProcessingNotifyAspect {
    public static final String PROCESSING_TOPIC_PREFIX = "/topic/processing/";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private final SimpMessagingTemplate messagingTemplate;

    public ProcessingNotifyAspect(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @AfterReturning(pointcut = "@annotation(org.example.validation.annotation.TrackProcessing) && args(documentId)", argNames = "documentId")
    public void afterDocumentProcessed(Long documentId) {
        messagingTemplate.convertAndSend(PROCESSING_TOPIC_PREFIX + documentId, STATUS_COMPLETED);
    }
}