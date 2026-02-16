package com.chatbotrag.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ConversationListResponse {
    private Long id;
    private String title;
    private LocalDateTime lastUpdatedAt;
    private int messageCount;
}