package com.chatbotrag.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String name;
}