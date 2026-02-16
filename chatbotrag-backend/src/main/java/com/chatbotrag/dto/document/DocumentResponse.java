package com.chatbotrag.dto.document;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String title;
    private LocalDateTime uploadedAt;
}