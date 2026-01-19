package org.example.models.dtos.importDtos;

public class ChatRequestDto {
    private String message;

    public ChatRequestDto() {}

    public ChatRequestDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
