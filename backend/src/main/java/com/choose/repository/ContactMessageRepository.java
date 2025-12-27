package com.choose.repository;

import com.choose.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    List<ContactMessage> findByStatus(ContactMessage.MessageStatus status);
    List<ContactMessage> findByUserUserId(Long userId);
    boolean existsByCaseNumber(String caseNumber);
}

