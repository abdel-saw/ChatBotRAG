package com.chatbotrag.service;

import com.chatbotrag.entity.Document;
import com.chatbotrag.entity.User;
import com.chatbotrag.repository.DocumentChunkRepository;
import com.chatbotrag.repository.DocumentRepository;
import com.chatbotrag.util.DocumentTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Exercises the real pgvector metadata filter without model/API calls. */
@EnabledIfEnvironmentVariable(named = "PGVECTOR_TEST_URL", matches = ".+")
class PgVectorDocumentDeletionTest {
    @TempDir Path directory;

    @Test
    void realVectorStoreDeletesOnlyMatchingDocumentAndOwner() throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                System.getenv("PGVECTOR_TEST_URL"),
                System.getenv().getOrDefault("PGVECTOR_TEST_USER", "postgres"),
                System.getenv().getOrDefault("PGVECTOR_TEST_PASSWORD", "postgres"));
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        String table = "deletion_vectors_" + UUID.randomUUID().toString().replace("-", "");
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbc.execute("CREATE TABLE " + table + " (id uuid PRIMARY KEY, content text, metadata jsonb, embedding vector(3))");
        try {
            for (String metadata : new String[]{
                    "{\"documentId\":42,\"userId\":7}",
                    "{\"documentId\":43,\"userId\":7}",
                    "{\"documentId\":42,\"userId\":8}"}) {
                jdbc.update("INSERT INTO " + table + " VALUES (?, 'fixture', ?::jsonb, '[1,0,0]')",
                        UUID.randomUUID(), metadata);
            }
            EmbeddingModel embedding = mock(EmbeddingModel.class);
            PgVectorStore vectors = PgVectorStore.builder(jdbc, embedding)
                    .vectorTableName(table).dimensions(3).initializeSchema(false).build();
            vectors.afterPropertiesSet();
            User user = User.builder().id(7L).build();
            Path file = directory.resolve("document.txt");
            Files.writeString(file, "fixture content");
            Document document = Document.builder().id(42L).user(user).filePath(file.toString()).build();
            DocumentRepository documents = mock(DocumentRepository.class);
            when(documents.findById(42L)).thenReturn(Optional.of(document));
            UserService users = mock(UserService.class);
            when(users.getCurrentUser()).thenReturn(user);
            DocumentService service = new DocumentService(documents, mock(DocumentChunkRepository.class),
                    mock(DocumentTextExtractor.class), vectors, users);

            service.deleteDocument(42L);

            assertThat(jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class)).isEqualTo(2);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM " + table
                    + " WHERE metadata->>'documentId'='42' AND metadata->>'userId'='7'", Integer.class)).isZero();
            assertThat(file).doesNotExist();
            verify(documents).delete(document);
            verifyNoInteractions(embedding);
        } finally {
            jdbc.execute("DROP TABLE " + table);
        }
    }
}
