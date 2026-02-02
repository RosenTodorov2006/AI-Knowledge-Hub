package org.example.unit.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.example.clients.OpenAiClient;
import org.springframework.http.HttpMethod;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import org.springframework.http.MediaType;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.junit.jupiter.api.Assertions;
import static org.hamcrest.Matchers.containsString;

public class OpenAiClientTest {
    private static final String API_KEY_VALUE = "test-key";
    private static final String FIELD_API_KEY = "apiKey";
    private static final String FIELD_REST_TEMPLATE = "restTemplate";
    private static final String HEADER_AUTH = "Authorization";
    private static final String AUTH_VALUE = "Bearer test-key";
    private static final String HEADER_BETA = "OpenAI-Beta";
    private static final String BETA_VALUE = "assistants=v2";
    private static final String THREADS_URL = "https://api.openai.com/v1/threads";
    private static final String THREAD_ID = "thread_123";
    private static final String RUN_ID = "run_999";
    private static final String MSG_CONTENT = "Hello";
    private static final String AI_ANSWER = "AI response";
    private static final String JSON_THREAD = "{\"id\": \"thread_123\"}";
    private static final String JSON_RUN = "{\"id\": \"run_999\"}";
    private static final String JSON_STATUS_COMPLETED = "{\"status\": \"completed\"}";
    private static final String JSON_STATUS_FAILED = "{\"status\": \"failed\"}";
    private static final String JSON_MESSAGES = "{\"data\": [{\"content\": [{\"text\": {\"value\": \"AI response\"}}]}]}";
    private static final String EMPTY_JSON = "{}";
    private OpenAiClient openAiClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setUp() {
        openAiClient = new OpenAiClient();
        ReflectionTestUtils.setField(openAiClient, FIELD_API_KEY, API_KEY_VALUE);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(openAiClient, FIELD_REST_TEMPLATE);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }
    @Test
    public void testCreateThreadShouldReturnId() {
        mockServer.expect(requestTo(THREADS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HEADER_AUTH, AUTH_VALUE))
                .andExpect(header(HEADER_BETA, BETA_VALUE))
                .andRespond(withSuccess(JSON_THREAD, MediaType.APPLICATION_JSON));

        String result = openAiClient.createThread();

        Assertions.assertEquals(THREAD_ID, result);
        mockServer.verify();
    }
    @Test
    public void testAskAssistantShouldExecuteFullFlow() {
        mockServer.expect(requestTo(containsString("/messages")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(EMPTY_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(containsString("/runs")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(JSON_RUN, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(containsString("/runs/" + RUN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JSON_STATUS_COMPLETED, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(containsString("/messages")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JSON_MESSAGES, MediaType.APPLICATION_JSON));

        String result = openAiClient.askAssistant(THREAD_ID, MSG_CONTENT);

        Assertions.assertEquals(AI_ANSWER, result);
        mockServer.verify();
    }
    @Test
    public void testAskAssistantShouldThrowExceptionOnRunFailure() {
        mockServer.expect(requestTo(containsString("/messages")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(EMPTY_JSON, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(containsString("/runs")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(JSON_RUN, MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(containsString("/runs/" + RUN_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(JSON_STATUS_FAILED, MediaType.APPLICATION_JSON));

        Assertions.assertThrows(RuntimeException.class, () ->
                openAiClient.askAssistant(THREAD_ID, MSG_CONTENT));
        mockServer.verify();
    }
}
