package com.chatbotrag.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "document_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)           // ← Ligne importante ajoutée
    private Map<String, Object> metadata;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}