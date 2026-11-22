package com.chatbotrag.service;

import com.chatbotrag.dto.chat.ChatRequest;
import com.chatbotrag.entity.Conversation;
import com.chatbotrag.entity.User;
import com.chatbotrag.repository.ConversationRepository;
import com.chatbotrag.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class ChatServiceTest {
    private ChatClient chat;
    private VectorStore vectors;
    private UserService users;
    private ConversationRepository conversations;
    private MessageRepository messages;
    private ChatService service;
    private User user;

    @BeforeEach
    void setUp() {
        chat = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        vectors = mock(VectorStore.class);
        users = mock(UserService.class);
        conversations = mock(ConversationRepository.class);
        messages = mock(MessageRepository.class);
        user = User.builder().id(7L).build();
        when(users.getCurrentUser()).thenReturn(user);
        when(conversations.save(any())).thenAnswer(call -> call.getArgument(0));
        service = new ChatService(chat, vectors, users, conversations, messages);
    }

    @Test
    void retrievedDocumentTextAndQuestionAreNotWrittenToConsole(CapturedOutput output) {
        String context = "PRIVATE_DOCUMENT_SENTINEL: confidential internal document with enough content for retrieval.";
        when(vectors.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new org.springframework.ai.document.Document(context)));
        when(chat.prompt().system(anyString()).user(anyString()).call().chatResponse())
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("Answer")))));
        ChatRequest request = new ChatRequest();
        request.setMessage("PRIVATE_QUESTION_SENTINEL");

        service.sendMessage(request);

        assertThat(output.getAll()).doesNotContain("PRIVATE_DOCUMENT_SENTINEL", "PRIVATE_QUESTION_SENTINEL");
    }

    @Test
    void emptyRetrievalDoesNotCallTheChatModel() {
        when(vectors.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
        ChatRequest request = new ChatRequest();
        request.setMessage("A question");

        service.sendMessage(request);

        verifyNoInteractions(chat);
    }

    @Test
    void anotherUsersConversationIsRejectedBeforeRetrieval() {
        when(conversations.findById(42L)).thenReturn(Optional.of(Conversation.builder()
                .id(42L).user(User.builder().id(8L).build()).build()));
        ChatRequest request = new ChatRequest();
        request.setConversationId(42L);
        request.setMessage("A question");

        assertThatThrownBy(() -> service.sendMessage(request)).hasMessage("Unauthorized");

        verifyNoInteractions(vectors, chat, messages);
    }
}
