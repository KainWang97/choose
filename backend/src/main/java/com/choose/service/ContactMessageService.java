package com.choose.service;

import com.choose.model.ContactMessage;
import com.choose.model.User;
import com.choose.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactMessageService {
    private final ContactMessageRepository contactMessageRepository;

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ContactMessage> getMessagesByStatus(ContactMessage.MessageStatus status) {
        return contactMessageRepository.findByStatus(status);
    }

    public Optional<ContactMessage> getMessageById(Long messageId) {
        return contactMessageRepository.findById(messageId);
    }

    @Transactional
    public ContactMessage createMessage(ContactMessage message, User user, String ipAddress) {
        if (user != null) {
            message.setUser(user);
        }
        message.setIpAddress(ipAddress);
        message.setStatus(ContactMessage.MessageStatus.UNREAD);
        return contactMessageRepository.save(message);
    }

    @Transactional
    public ContactMessage markAsRead(Long messageId) {
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        message.setStatus(ContactMessage.MessageStatus.READ);
        return contactMessageRepository.save(message);
    }

    @Transactional
    public ContactMessage markAsReplied(Long messageId) {
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        message.setStatus(ContactMessage.MessageStatus.REPLIED);
        message.setRepliedAt(LocalDateTime.now());
        return contactMessageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        if (!contactMessageRepository.existsById(messageId)) {
            throw new IllegalArgumentException("Message not found");
        }
        contactMessageRepository.deleteById(messageId);
    }
}

