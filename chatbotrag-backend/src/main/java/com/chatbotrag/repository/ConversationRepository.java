package com.chatbotrag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chatbotrag.entity.Conversation;
import com.chatbotrag.entity.User;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByUserOrderByLastUpdatedAtDesc(User user);
}