package org.example.integration.clients;
import org.example.integration.base.BaseIntegrationTest;
import org.example.clients.OpenAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
public class OpenAiClientIntegrationTest extends BaseIntegrationTest {
    private static final String API_BASE_URL = "https://api.openai.com/v1/threads";
    private static final String PATH_MESSAGES = "/messages";
    private static final String PATH_RUNS = "/runs";
    private static final String HEADER_BETA_KEY = "OpenAI-Beta";
    private static final String HEADER_BETA_VAL = "assistants=v2";
    private static final String THREAD_ID_ABC = "thread_abc123";
    private static final String THREAD_ID_123 = "thread_123";
    private static final String THREAD_ID_INT = "thread_int";
    private static final String RUN_ID_456 = "run_456";
    private static final String RUN_ID_FAIL = "run_fail";
    private static final String RUN_ID_INT = "run_int";
    private static final String JSON_THREAD_ID = "{\"id\": \"thread_abc123\"}";
    private static final String JSON_EMPTY = "{}";
    private static final String JSON_RUN_ID = "{\"id\": \"run_456\"}";
    private static final String JSON_STATUS_COMPLETED = "{\"status\": \"completed\"}";
    private static final String JSON_STATUS_FAILED = "{\"status\": \"failed\"}";
    private static final String JSON_AI_MESSAGES = "{\"data\": [{\"content\": [{\"text\": {\"value\": \"Това е отговорът на AI.\"}}]}]}";
    private static final String EXPECTED_AI_ANSWER = "Това е отговорът на AI.";
    private static final String TEST_PROMPT = "Тестов въпрос";
    private static final String ERROR_MSG_PART = "Assistant run failed";
    private static final String REFLECT_FIELD_REST_TEMPLATE = "restTemplate";
    private static final String REFLECT_METHOD_WAIT_FOR_RUN = "waitForRunCompletion";
    private static final long SLEEP_DURATION = 200;
    private static final long JOIN_TIMEOUT = 2000;
    @Autowired
    private OpenAiClient openAiClient;
    private MockRestServiceServer mockServer;
    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(openAiClient, REFLECT_FIELD_REST_TEMPLATE);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }
    @Test
    void testCreateThread_Success() {
        mockServer.expect(requestTo(API_BASE_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HEADER_BETA_KEY, HEADER_BETA_VAL))
                .andRespond(withSuccess(JSON_THREAD_ID, MediaType.APPLICATION_JSON));
        String threadId = openAiClient.createThread();
        assertThat(threadId).isEqualTo(THREAD_ID_ABC);
        mockServer.verify();
    }
    @Test
    void testAskAssistant_FullWorkflow_Success() {
        mockServer.expect(requestTo(API_BASE_URL + "/" + THREAD_ID_123 + PATH_MESSAGES))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(JSON_EMPTY, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(API_BASE_URL + "/" + THREAD_ID_123 + PATH_RUNS))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(JSON_RUN_ID, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(API_BASE_URL + "/" + THREAD_ID_123 + PATH_RUNS + "/" + RUN_ID_456))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JSON_STATUS_COMPLETED, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo(API_BASE_URL + "/" + THREAD_ID_123 + PATH_MESSAGES))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JSON_AI_MESSAGES, MediaType.APPLICATION_JSON));
        String answer = openAiClient.askAssistant(THREAD_ID_123, TEST_PROMPT);
        assertThat(answer).isEqualTo(EXPECTED_AI_ANSWER);
        mockServer.verify();
    }
    @Test
    void testWaitForRunCompletion_WhenFailed_ShouldThrowException() {
        mockServer.expect(requestTo(API_BASE_URL + "/" + THREAD_ID_123 + PATH_RUNS + "/" + RUN_ID_FAIL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JSON_STATUS_FAILED, MediaType.APPLICATION_JSON));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ReflectionTestUtils.invokeMethod(openAiClient, REFLECT_METHOD_WAIT_FOR_RUN, THREAD_ID_123, RUN_ID_FAIL);
        });
        assertThat(exception.getMessage()).contains(ERROR_MSG_PART);
    }
    @Test
    void testWaitForRunCompletion_WhenInterrupted_ShouldThrowRuntimeException() throws InterruptedException {
        java.util.concurrent.atomic.AtomicReference<Exception> caughtException = new java.util.concurrent.atomic.AtomicReference<>();
        Thread executionThread = new Thread(() -> {
            try {
                org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                        openAiClient, REFLECT_METHOD_WAIT_FOR_RUN, THREAD_ID_INT, RUN_ID_INT);
            } catch (Exception e) {
                caughtException.set(e);
            }
        });
        executionThread.start();
        Thread.sleep(SLEEP_DURATION);
        executionThread.interrupt();
        executionThread.join(JOIN_TIMEOUT);
        Exception finalException = caughtException.get();
        assertThat(finalException).isNotNull();
        assertThat(finalException).isInstanceOf(RuntimeException.class);
        assertThat(finalException.getCause()).isInstanceOf(InterruptedException.class);
    }
}