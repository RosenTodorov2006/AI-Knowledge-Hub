package org.example.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProcessingNotifyAspect {

    private final SimpMessagingTemplate messagingTemplate; // За WebSockets

    public ProcessingNotifyAspect(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @AfterReturning(pointcut = "@annotation(org.example.validation.annotation.TrackProcessing) && args(documentId)", argNames = "documentId")
    public void afterDocumentProcessed(Long documentId) {
        // Когато методът приключи, пращаме сигнал по WebSocket към фронт-енда
        messagingTemplate.convertAndSend("/topic/processing/" + documentId, "COMPLETED");
    }
}
