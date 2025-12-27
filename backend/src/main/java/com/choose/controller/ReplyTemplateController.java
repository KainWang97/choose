package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.model.ReplyTemplate;
import com.choose.model.User;
import com.choose.service.ReplyTemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reply-templates")
@RequiredArgsConstructor
public class ReplyTemplateController {
    private final ReplyTemplateService templateService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TemplateDTO>>> getAllTemplates() {
        List<ReplyTemplate> templates = templateService.getAllTemplates();
        List<TemplateDTO> dtos = templates.stream()
                .map(TemplateDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TemplateDTO>> createTemplate(
            @Valid @RequestBody TemplateRequest request,
            @AuthenticationPrincipal User admin) {
        ReplyTemplate template = templateService.createTemplate(
                request.getName(), request.getContent(), admin.getUserId());
        return ResponseEntity.ok(ApiResponse.success("模板已建立", TemplateDTO.fromEntity(template)));
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TemplateDTO>> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody TemplateRequest request) {
        ReplyTemplate template = templateService.updateTemplate(
                templateId, request.getName(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success("模板已更新", TemplateDTO.fromEntity(template)));
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("模板已刪除", null));
    }

    @Data
    static class TemplateRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String content;
    }

    @Data
    static class TemplateDTO {
        private Long id;
        private String name;
        private String content;
        private String createdAt;

        static TemplateDTO fromEntity(ReplyTemplate template) {
            TemplateDTO dto = new TemplateDTO();
            dto.setId(template.getTemplateId());
            dto.setName(template.getName());
            dto.setContent(template.getContent());
            dto.setCreatedAt(template.getCreatedAt() != null 
                    ? template.getCreatedAt().toString() : null);
            return dto;
        }
    }
}
