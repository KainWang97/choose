package com.choose.dto.response;

import com.choose.model.ContactMessage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Inquiry DTO for frontend compatibility
 * Maps ContactMessage to frontend Inquiry format
 */
@Data
public class InquiryDTO {
    private Long id;
    private String name;
    private String email;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
    private Long userId;

    public static InquiryDTO fromEntity(ContactMessage message) {
        InquiryDTO dto = new InquiryDTO();
        dto.setId(message.getMessageId());
        dto.setName(message.getName());
        dto.setEmail(message.getEmail());
        dto.setMessage(message.getMessage());
        dto.setStatus(message.getStatus().name());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setRepliedAt(message.getRepliedAt());
        
        if (message.getUser() != null) {
            dto.setUserId(message.getUser().getUserId());
        }
        
        return dto;
    }

    public static List<InquiryDTO> fromEntities(List<ContactMessage> messages) {
        return messages.stream()
                .map(InquiryDTO::fromEntity)
                .toList();
    }
}

