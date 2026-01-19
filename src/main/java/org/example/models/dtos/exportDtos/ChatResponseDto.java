package org.example.models.dtos.exportDtos;

public class ChatResponseDto {
    private String answer;
    private Long messageId;

    public ChatResponseDto() {}

    public ChatResponseDto(String answer, Long messageId) {
        this.answer = answer;
        this.messageId = messageId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
