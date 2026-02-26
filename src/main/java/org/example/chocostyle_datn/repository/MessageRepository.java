package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.Conversation;
import org.example.chocostyle_datn.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    Page<Message> findByConversationOrderBySentAtAsc(Conversation conversation, Pageable pageable);
}
