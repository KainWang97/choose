package com.choose.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages", indexes = {
    // 搜尋索引
        @Index(name = "idx_case_number", columnList = "case_number"),
        @Index(name = "idx_status_created", columnList = "status, created_at"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_ip_created", columnList = "ip_address, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "case_number", nullable = false, unique = true, length = 20)
    private String caseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "subject", length = 100)
    private String subject;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.PENDING;

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "admin_reply_by")
    private Long adminReplyBy;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MessageStatus {
        PENDING, REPLIED_TRACKING, CLOSED
    }
}

