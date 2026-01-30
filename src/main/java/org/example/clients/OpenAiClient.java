package org.example.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class OpenAiClient {
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public String createThread() {
        HttpEntity<String> entity = new HttpEntity<>(EMPTY_JSON, getHeaders());
        Map<String, Object> response = restTemplate.postForObject(OPENAI_THREADS_URL, entity, Map.class);
        return (String) Objects.requireNonNull(response).get(JSON_KEY_ID);
    }

    public String askAssistant(String threadId, String combinedPrompt) {
        addMessageToThread(threadId, combinedPrompt);
        String runId = createRun(threadId);
        waitForRunCompletion(threadId, runId);
        return getLastAssistantMessage(threadId);
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
                throw new RuntimeException("Assistant run failed");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String getLastAssistantMessage(String threadId) {
        String messagesUrl = OPENAI_THREADS_URL + URL_SEPARATOR + threadId + PATH_MESSAGES;
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        Map<String, Object> response = restTemplate.exchange(messagesUrl, HttpMethod.GET, entity, Map.class).getBody();

        List<Map<String, Object>> data = (List<Map<String, Object>>) Objects.requireNonNull(response).get(JSON_KEY_DATA);
        Map<String, Object> lastMsg = data.get(0);
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) lastMsg.get(JSON_KEY_CONTENT);
        Map<String, Object> textObj = (Map<String, Object>) contentList.get(0).get(JSON_KEY_TEXT);

        return (String) textObj.get(JSON_KEY_VALUE);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AUTH_BEARER_PREFIX + apiKey);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(OPENAI_BETA_HEADER, OPENAI_BETA_VERSION);
        return headers;
    }
}
