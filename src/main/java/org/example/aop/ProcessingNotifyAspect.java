package org.example.aop;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProcessingNotifyAspect {

    private final SimpMessagingTemplate messagingTemplate;

    public ProcessingNotifyAspect(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @AfterReturning(pointcut = "@annotation(org.example.validation.annotation.TrackProcessing) && args(documentId)", argNames = "documentId")
    public void afterDocumentProcessed(Long documentId) {
        System.out.println("AOP: Обработката приключи за ID: " + documentId + ". Изпращане на сигнал COMPLETED...");

        messagingTemplate.convertAndSend("/topic/processing/" + documentId, "COMPLETED");
    }
}