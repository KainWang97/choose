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
        message.setMessage(request.getMessage());
        
        String ipAddress = getClientIpAddress(httpRequest);
        ContactMessage created = contactMessageService.createMessage(message, user, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success("Inquiry submitted successfully", InquiryDTO.fromEntity(created)));
    }

    @PatchMapping("/{messageId}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDTO>> markAsRead(@PathVariable Long messageId) {
        ContactMessage message = contactMessageService.markAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", InquiryDTO.fromEntity(message)));
    }

    @PatchMapping("/{messageId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDTO>> markAsReplied(@PathVariable Long messageId) {
        ContactMessage message = contactMessageService.markAsReplied(messageId);
        return ResponseEntity.ok(ApiResponse.success("Marked as replied", InquiryDTO.fromEntity(message)));
    }

    @PatchMapping("/{messageId}/unreply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InquiryDTO>> markAsUnreplied(@PathVariable Long messageId) {
        ContactMessage message = contactMessageService.markAsUnreplied(messageId);
        return ResponseEntity.ok(ApiResponse.success("Marked as unreplied", InquiryDTO.fromEntity(message)));
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@PathVariable Long messageId) {
        contactMessageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("Inquiry deleted successfully", null));
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
        
        @NotBlank
        private String message;
    }
}

