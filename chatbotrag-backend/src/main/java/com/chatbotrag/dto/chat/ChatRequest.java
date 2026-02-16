package com.chatbotrag.dto.chat;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private Long conversationId;  // null = nouvelle conversation
}