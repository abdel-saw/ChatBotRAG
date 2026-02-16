package com.chatbotrag.dto.document;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentUploadRequest {

    private MultipartFile file;  // CV ou JOB_DESCRIPTION
    private String title;       // Optionnel, sinon on prend le nom du fichier
}