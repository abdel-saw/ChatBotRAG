package com.chatbotrag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatbotrag.entity.Conversation;
import com.chatbotrag.entity.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
}