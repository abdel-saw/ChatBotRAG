package com.chatbotrag.service;

import com.chatbotrag.entity.Document;
import com.chatbotrag.entity.User;
import com.chatbotrag.repository.DocumentChunkRepository;
import com.chatbotrag.repository.DocumentRepository;
import com.chatbotrag.util.DocumentTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InOrder;
import org.springframework.ai.vectorstore.VectorStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {
    @TempDir Path directory;
    private DocumentRepository documents;
    private DocumentChunkRepository chunks;
    private VectorStore vectors;
    private UserService users;
    private DocumentService service;
    private User owner;

    @BeforeEach
    void setUp() {
        documents = mock(DocumentRepository.class);
        chunks = mock(DocumentChunkRepository.class);
        vectors = mock(VectorStore.class);
        users = mock(UserService.class);
        owner = User.builder().id(7L).build();
        when(users.getCurrentUser()).thenReturn(owner);
        service = new DocumentService(documents, chunks, mock(DocumentTextExtractor.class), vectors, users);
    }

    private Document storedDocument(User user) throws Exception {
        Path file = directory.resolve("document.txt");
        Files.writeString(file, "fixture document content");
        Document document = Document.builder().id(42L).user(user).title("Example")
                .filePath(file.toString()).build();
        when(documents.findById(42L)).thenReturn(Optional.of(document));
        return document;
    }

    @Test
    void deletesOnlyTheOwnersDocumentFromTheVectorStoreBeforeMetadata() throws Exception {
        Document document = storedDocument(owner);

        service.deleteDocument(42L);

        InOrder order = inOrder(vectors, chunks, documents);
        order.verify(vectors).delete("documentId == 42 && userId == 7");
        order.verify(chunks).deleteByDocument(document);
        order.verify(documents).delete(document);
        assertThat(Path.of(document.getFilePath())).doesNotExist();
    }

    @Test
    void vectorStoreFailureKeepsTheFileAndMetadata() throws Exception {
        Document document = storedDocument(owner);
        doThrow(new IllegalStateException("vector store unavailable"))
                .when(vectors).delete(anyString());

        assertThatThrownBy(() -> service.deleteDocument(42L))
                .isInstanceOf(IllegalStateException.class);

        assertThat(Path.of(document.getFilePath())).exists();
        verify(chunks, never()).deleteByDocument(any());
        verify(documents, never()).delete(any(Document.class));
    }

    @Test
    void anotherUserCannotDeleteTheFileOrEitherStore() throws Exception {
        Document document = storedDocument(User.builder().id(8L).build());

        assertThatThrownBy(() -> service.deleteDocument(42L)).hasMessage("Unauthorized");

        assertThat(Path.of(document.getFilePath())).exists();
        verifyNoInteractions(vectors, chunks);
        verify(documents, never()).delete(any(Document.class));
    }

    @Test
    void missingDocumentDoesNotModifyEitherStore() {
        when(documents.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteDocument(42L)).hasMessage("Document not found");

        verifyNoInteractions(vectors, chunks);
        verify(documents, never()).delete(any(Document.class));
    }

    @Test
    void missingOriginalFileStillRemovesTheIndexedContent() throws Exception {
        Document document = storedDocument(owner);
        Files.delete(Path.of(document.getFilePath()));

        service.deleteDocument(42L);

        verify(vectors).delete("documentId == 42 && userId == 7");
        verify(documents).delete(document);
    }
}
