package com.chatbotrag.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageDTO {
    private String role;  // "USER" ou "ASSISTANT"
    private String content;
}