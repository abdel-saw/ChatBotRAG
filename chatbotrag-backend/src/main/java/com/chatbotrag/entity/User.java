package com.chatbotrag.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;  // Optionnel : prénom/nom complet

    @Column(nullable = false)
    @Builder.Default
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}