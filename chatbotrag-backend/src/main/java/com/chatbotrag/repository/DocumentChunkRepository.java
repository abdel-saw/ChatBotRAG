package com.chatbotrag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatbotrag.entity.Document;
import com.chatbotrag.entity.DocumentChunk;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    void deleteByDocument(Document document);
}