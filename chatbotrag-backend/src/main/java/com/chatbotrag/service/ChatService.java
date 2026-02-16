package com.chatbotrag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbotrag.dto.chat.ChatMessageDTO;
import com.chatbotrag.dto.chat.ChatRequest;
import com.chatbotrag.dto.chat.ChatResponse;
import com.chatbotrag.dto.chat.ConversationListResponse;
import com.chatbotrag.entity.Conversation;
import com.chatbotrag.entity.Message;
import com.chatbotrag.entity.User;
import com.chatbotrag.repository.ConversationRepository;
import com.chatbotrag.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

	private final ChatClient chatClient;
	private final VectorStore vectorStore;
	private final UserService userService;
	private final ConversationRepository conversationRepository;
	private final MessageRepository messageRepository;

	private static final String SYSTEM_PROMPT = """
		    Tu es un assistant IA honnête et strict.
		    
		    Règle absolue : Tu dois répondre **UNIQUEMENT** en utilisant le contexte fourni ci-dessous.
		    Si le contexte est vide ou ne contient pas d'information pertinente pour répondre à la question, 
		    tu dois répondre exactement : "Je n'ai pas assez d'informations dans les documents fournis pour répondre à cette question."
		    
		    Ne fais jamais d'hypothèses, ne complète pas avec tes connaissances générales.
		    
		    Contexte des documents de l'utilisateur :
		    """;
	public ChatService(ChatClient chatClient, VectorStore vectorStore, UserService userService,
			ConversationRepository conversationRepository, MessageRepository messageRepository) {
		this.chatClient = chatClient;
		this.vectorStore = vectorStore;
		this.userService = userService;
		this.conversationRepository = conversationRepository;
		this.messageRepository = messageRepository;
	}

	@Transactional
	public ChatResponse sendMessage(ChatRequest request) {
		User user = userService.getCurrentUser();

		Conversation conversation;
		if (request.getConversationId() == null) {
			conversation = createNewConversation(user, request.getMessage());
		} else {
			conversation = conversationRepository.findById(request.getConversationId())
					.orElseThrow(() -> new RuntimeException("Conversation not found"));

			if (!conversation.getUser().getId().equals(user.getId())) {
				throw new RuntimeException("Unauthorized");
			}
		}

		// Sauvegarde message utilisateur
		Message userMessage = Message.builder().conversation(conversation).role(Message.Role.USER)
				.content(request.getMessage()).sentAt(LocalDateTime.now()).build();
		messageRepository.save(userMessage);

		// Retrieval
		List<org.springframework.ai.document.Document> relevantDocs = vectorStore
				.similaritySearch(SearchRequest.builder().query(request.getMessage()).topK(8).similarityThreshold(0.6)
						.filterExpression("userId == " + user.getId()).build());

		// Extraction du contenu
		String context = relevantDocs.stream()
				.map((org.springframework.ai.document.Document doc) -> doc.getFormattedContent())
				.collect(Collectors.joining("\n\n---\n\n"));
		
		
		// DEBUG
		System.out.println("=== CONTEXT LENGTH: " + context.length() + " ===");
		System.out.println(context);
		

		// GARDE-FOU : Si pas de contexte pertinent → réponse directe SANS appeler le LLM
		if (context.trim().isEmpty() || context.length() < 30) {
		    String noContextReply = "Je n'ai pas de contexte pertinent pour répondre à cette question.";

		    Message assistantMessage = Message.builder()
		            .conversation(conversation)
		            .role(Message.Role.ASSISTANT)
		            .content(noContextReply)
		            .sentAt(LocalDateTime.now())
		            .build();
		    messageRepository.save(assistantMessage);

		    conversation.setLastUpdatedAt(LocalDateTime.now());
		    conversationRepository.save(conversation);

		    List<ChatMessageDTO> dtoMessages = new ArrayList<>();
		    dtoMessages.add(new ChatMessageDTO("USER", request.getMessage()));
		    dtoMessages.add(new ChatMessageDTO("ASSISTANT", noContextReply));

		    return new ChatResponse(conversation.getId(), conversation.getTitle(), dtoMessages);
		}
		

		// Prompt final très structuré
		String fullPrompt = SYSTEM_PROMPT + "\n" + context + "\n\nQuestion de l'utilisateur : " + request.getMessage();
		

		// Appel IA
		org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt().system(fullPrompt)
				.user(request.getMessage()).call().chatResponse();

		String assistantReply = aiResponse.getResult().getOutput().getText();

		// Sauvegarde réponse
		Message assistantMessage = Message.builder().conversation(conversation).role(Message.Role.ASSISTANT)
				.content(assistantReply).sentAt(LocalDateTime.now()).build();
		messageRepository.save(assistantMessage);

		conversation.setLastUpdatedAt(LocalDateTime.now());
		conversationRepository.save(conversation);

		// Retour
		List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conversation);

		List<ChatMessageDTO> dtoMessages = messages.stream()
				.map(m -> new ChatMessageDTO(m.getRole().name(), m.getContent())).toList();

		return new ChatResponse(conversation.getId(), conversation.getTitle(), dtoMessages);
	}

	private Conversation createNewConversation(User user, String firstMessage) {
		String title = firstMessage.length() > 50 ? firstMessage.substring(0, 47) + "..." : firstMessage;

		Conversation conversation = Conversation.builder().user(user).title(title).createdAt(LocalDateTime.now())
				.lastUpdatedAt(LocalDateTime.now()).build();

		return conversationRepository.save(conversation);
	}

	public List<ConversationListResponse> getConversations() {
		User user = userService.getCurrentUser();
		return conversationRepository.findByUserOrderByLastUpdatedAtDesc(user).stream()
				.map(c -> new ConversationListResponse(c.getId(), c.getTitle(), c.getLastUpdatedAt(),
						c.getMessages().size()))
				.toList();
	}

	public ChatResponse getConversation(Long conversationId) {
		User user = userService.getCurrentUser();
		Conversation conversation = conversationRepository.findById(conversationId)
				.orElseThrow(() -> new RuntimeException("Conversation not found"));

		if (!conversation.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("Unauthorized");
		}

		List<com.chatbotrag.dto.chat.ChatMessageDTO> messages = conversation.getMessages().stream()
				.sorted((a, b) -> a.getSentAt().compareTo(b.getSentAt()))
				.map(m -> new com.chatbotrag.dto.chat.ChatMessageDTO(m.getRole().name(), m.getContent())).toList();

		return new ChatResponse(conversation.getId(), conversation.getTitle(), messages);
	}
	
	@Transactional
	public void deleteConversation(Long conversationId) {
	    User user = userService.getCurrentUser();

	    Conversation conversation = conversationRepository.findById(conversationId)
	            .orElseThrow(() -> new RuntimeException("Conversation not found"));

	    if (!conversation.getUser().getId().equals(user.getId())) {
	        throw new RuntimeException("Unauthorized");
	    }

	    // Cascade delete automatique grâce à orphanRemoval = true + CascadeType.ALL
	    conversationRepository.delete(conversation);
	}
}