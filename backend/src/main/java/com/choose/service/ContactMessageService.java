package com.choose.service;

import com.choose.model.ContactMessage;
import com.choose.model.User;
import com.choose.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactMessageService {
    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ContactMessage> getMessagesByStatus(ContactMessage.MessageStatus status) {
        return contactMessageRepository.findByStatus(status);
    }

    public Optional<ContactMessage> getMessageById(Long messageId) {
        return contactMessageRepository.findById(messageId);
    }

    /**
     * 產生唯一案件編號 (CS-XXXX)
     */
    private String generateCaseNumber() {
        long count = contactMessageRepository.count();
        String caseNumber;
        do {
            count++;
            caseNumber = String.format("CS-%04d", count);
        } while (contactMessageRepository.existsByCaseNumber(caseNumber));
        return caseNumber;
    }

    @Transactional
    public ContactMessage createMessage(ContactMessage message, User user, String ipAddress) {
        if (user != null) {
            message.setUser(user);
        }
        message.setIpAddress(ipAddress);
        message.setStatus(ContactMessage.MessageStatus.PENDING);
        message.setCaseNumber(generateCaseNumber());
        return contactMessageRepository.save(message);
    }

    /**
     * 回覆客服訊息並發送 Email 給用戶
     */
    @Transactional
    public ContactMessage replyMessage(Long messageId, String replyContent, Long adminUserId) {
        log.info("Replying to inquiry: messageId={}, adminUserId={}", messageId, adminUserId);
        
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        message.setAdminReply(replyContent);
        message.setAdminReplyBy(adminUserId);
        message.setStatus(ContactMessage.MessageStatus.REPLIED_TRACKING);
        message.setRepliedAt(LocalDateTime.now());
        
        ContactMessage saved = contactMessageRepository.save(message);
        
        // 發送回覆 Email 給用戶
        try {
            emailService.sendInquiryReplyEmail(saved);
            log.info("Reply email sent to: {}", saved.getEmail());
        } catch (Exception e) {
            log.error("Failed to send reply email to: {}", saved.getEmail(), e);
            // Email 發送失敗不影響回覆功能
        }
        
        return saved;
    }

    /**
     * 結案（只能從 REPLIED_TRACKING 轉為 CLOSED）
     */
    @Transactional
    public ContactMessage closeInquiry(Long messageId) {
        log.info("Closing inquiry: messageId={}", messageId);
        
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        if (message.getStatus() != ContactMessage.MessageStatus.REPLIED_TRACKING) {
            throw new IllegalStateException("只有已回覆狀態可以結案");
        }
        
        message.setStatus(ContactMessage.MessageStatus.CLOSED);
        return contactMessageRepository.save(message);
    }

    /**
     * 重開案件（從 CLOSED 退回 REPLIED_TRACKING）
     */
    @Transactional
    public ContactMessage reopenInquiry(Long messageId) {
        log.info("Reopening inquiry: messageId={}", messageId);
        
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        if (message.getStatus() != ContactMessage.MessageStatus.CLOSED) {
            throw new IllegalStateException("只有已結案狀態可以重開");
        }
        
        message.setStatus(ContactMessage.MessageStatus.REPLIED_TRACKING);
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
