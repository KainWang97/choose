package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.dto.response.InquiryDTO;
import com.choose.model.ContactMessage;
import com.choose.model.User;
import com.choose.service.ContactMessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {
    private final ContactMessageService contactMessageService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InquiryDTO>>> getAllInquiries() {
        List<ContactMessage> messages = contactMessageService.getAllMessages();
        List<InquiryDTO> dtos = InquiryDTO.fromEntities(messages);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{messageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDTO>> getInquiryById(@PathVariable Long messageId) {
        return contactMessageService.getMessageById(messageId)
                .map(message -> ResponseEntity.ok(ApiResponse.success(InquiryDTO.fromEntity(message))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InquiryDTO>> createInquiry(
            @Valid @RequestBody InquiryRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpRequest) {
        
        ContactMessage message = new ContactMessage();
        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setSubject(request.getSubject());
        message.setMessage(request.getMessage());
        
        String ipAddress = getClientIpAddress(httpRequest);
        ContactMessage created = contactMessageService.createMessage(message, user, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success("Inquiry submitted successfully", InquiryDTO.fromEntity(created)));
    }

    /**
     * 回覆客服訊息（含發送 Email 給用戶）
     */
    @PostMapping("/{messageId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDTO>> replyInquiry(
            @PathVariable Long messageId,
            @Valid @RequestBody ReplyRequest request,
            @AuthenticationPrincipal User admin) {
        ContactMessage message = contactMessageService.replyMessage(
                messageId, request.getReplyContent(), admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success("已回覆並發送 Email", InquiryDTO.fromEntity(message)));
    }

    /**
     * 結案客服訊息
     */
    @PatchMapping("/{messageId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDTO>> closeInquiry(@PathVariable Long messageId) {
        ContactMessage message = contactMessageService.closeInquiry(messageId);
        return ResponseEntity.ok(ApiResponse.success("已結案", InquiryDTO.fromEntity(message)));
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@PathVariable Long messageId) {
        contactMessageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("Inquiry deleted successfully", null));
    }

    @PatchMapping("/{messageId}/reopen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDTO>> reopenInquiry(@PathVariable Long messageId) {
        ContactMessage updated = contactMessageService.reopenInquiry(messageId);
        return ResponseEntity.ok(ApiResponse.success("已重新開啟案件", InquiryDTO.fromEntity(updated)));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Data
    static class InquiryRequest {
        @NotBlank
        private String name;
        
        @NotBlank
        @Email
        private String email;
        
        private String subject;
        
        @NotBlank
        private String message;
    }

    @Data
    static class ReplyRequest {
        @NotBlank
        private String replyContent;
    }
}
