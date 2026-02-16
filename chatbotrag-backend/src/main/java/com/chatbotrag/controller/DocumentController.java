package com.chatbotrag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatbotrag.dto.document.DocumentResponse;
import com.chatbotrag.dto.document.DocumentUploadRequest;
import com.chatbotrag.entity.Document;
import com.chatbotrag.service.DocumentService;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(@ModelAttribute DocumentUploadRequest request) throws Exception {
        Document document = documentService.uploadDocument(
                request.getFile(),
                request.getTitle()
        );

        DocumentResponse response = new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getUploadedAt()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments() {
        List<Document> documents = documentService.getUserDocuments();

        List<DocumentResponse> response = documents.stream()
                .map(d -> new DocumentResponse(d.getId(), d.getTitle(), d.getUploadedAt()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}