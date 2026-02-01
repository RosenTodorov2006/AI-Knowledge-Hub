package unit.aop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.mockito.Mockito;
import org.example.aop.ProcessingNotifyAspect;
public class ProcessingNotifyAspectTest {
    private static final Long TEST_DOC_ID = 1L;
    private static final String EXPECTED_TOPIC = ProcessingNotifyAspect.PROCESSING_TOPIC_PREFIX + TEST_DOC_ID;
    private static final String EXPECTED_STATUS = ProcessingNotifyAspect.STATUS_COMPLETED;

    private SimpMessagingTemplate messagingTemplate;
    private ProcessingNotifyAspect processingNotifyAspect;
    @BeforeEach
    public void setUp() {
        messagingTemplate = Mockito.mock(SimpMessagingTemplate.class);
        processingNotifyAspect = new ProcessingNotifyAspect(messagingTemplate);
    }
    @Test
    public void testAfterDocumentProcessedShouldSendMessageToCorrectTopic() {
        processingNotifyAspect.afterDocumentProcessed(TEST_DOC_ID);

        Mockito.verify(messagingTemplate).convertAndSend(EXPECTED_TOPIC, EXPECTED_STATUS);
    }
}
