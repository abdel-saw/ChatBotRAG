package com.chatbotrag.service;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.chatbotrag.entity.Document;
import com.chatbotrag.entity.User;
import com.chatbotrag.repository.DocumentChunkRepository;
import com.chatbotrag.repository.DocumentRepository;
import com.chatbotrag.util.DocumentTextExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final String UPLOAD_DIR = "uploads/";

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentTextExtractor documentTextExtractor;
    private final VectorStore vectorStore;
    private final UserService userService;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentChunkRepository documentChunkRepository,
                           DocumentTextExtractor documentTextExtractor,
                           VectorStore vectorStore,
                           UserService userService) {  // ← Ajout
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.documentTextExtractor = documentTextExtractor;
        this.vectorStore = vectorStore;
        this.userService = userService;  // ← Ajout
    }

    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Transactional
    public Document uploadDocument(MultipartFile file, String title) throws Exception {
        User user = getCurrentUser();

        // Créer dossier uploads si besoin
        Path uploadPath = Paths.get(UPLOAD_DIR + user.getId());
        Files.createDirectories(uploadPath);

        // Sauvegarde fichier
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // Créer entité Document
        if (title == null || title.isBlank()) {
            title = file.getOriginalFilename();
        }

        Document document = Document.builder()
                .user(user)
                .title(title)
                .filePath(filePath.toString())
                .uploadedAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);

        // Extraction texte + chunking + embedding
        String text = documentTextExtractor.extractText(file.getInputStream());

        // Chunking simple (500-800 tokens approx)
        List<org.springframework.ai.document.Document> chunks = chunkText(text, document.getId());

        // Ajout dans VectorStore (Spring AI gère les embeddings + stockage)
        vectorStore.add(chunks);

        return document;
    }

    private List<org.springframework.ai.document.Document> chunkText(String text, Long documentId) {
        // Chunking simple : 1024 caractères avec overlap 200
        int chunkSize = 1024;
        int overlap = 200;
        List<org.springframework.ai.document.Document> chunks = new java.util.ArrayList<>();

        for (int i = 0; i < text.length(); i += chunkSize - overlap) {
            int end = Math.min(i + chunkSize, text.length());
            String chunk = text.substring(i, end);

            org.springframework.ai.document.Document aiDoc = new org.springframework.ai.document.Document(chunk);
            aiDoc.getMetadata().put("documentId", documentId);
            aiDoc.getMetadata().put("userId", getCurrentUser().getId());

            chunks.add(aiDoc);
        }

        return chunks;
    }

    public List<Document> getUserDocuments() {
        User user = getCurrentUser();
        return documentRepository.findByUser(user);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        User user = getCurrentUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Suppression physique du fichier
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            // Log error
        }

        // Suppression des chunks dans vector store (Spring AI n'a pas de deleteByMetadata, on le fait manuellement)
        documentChunkRepository.deleteByDocument(document);

        documentRepository.delete(document);
    }
}