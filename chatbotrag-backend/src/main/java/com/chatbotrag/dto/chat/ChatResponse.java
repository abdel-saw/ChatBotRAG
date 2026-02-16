package com.chatbotrag.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatResponse {
    private Long conversationId;
    private String title;  // Généré si nouvelle conversation
    private List<ChatMessageDTO> messages;
}