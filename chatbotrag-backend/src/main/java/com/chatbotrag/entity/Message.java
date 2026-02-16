package com.chatbotrag.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // USER ou ASSISTANT

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    public enum Role {
        USER,
        ASSISTANT
    }
}