package com.chatbotrag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatbotrag.dto.chat.*;
import com.chatbotrag.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationListResponse>> getConversations() {
        return ResponseEntity.ok(chatService.getConversations());
    }

    @GetMapping("/conversation/{id}")
    public ResponseEntity<ChatResponse> getConversation(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getConversation(id));
    }
    
    @DeleteMapping("/conversation/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}