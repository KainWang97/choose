package com.choose.service;

import com.choose.model.ReplyTemplate;
import com.choose.repository.ReplyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplyTemplateService {
    private final ReplyTemplateRepository templateRepository;

    public List<ReplyTemplate> getAllTemplates() {
        return templateRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<ReplyTemplate> getTemplateById(Long templateId) {
        return templateRepository.findById(templateId);
    }

    @Transactional
    public ReplyTemplate createTemplate(String name, String content, Long createdBy) {
        log.info("Creating template: name={}", name);
        ReplyTemplate template = new ReplyTemplate();
        template.setName(name);
        template.setContent(content);
        template.setCreatedBy(createdBy);
        return templateRepository.save(template);
    }

    @Transactional
    public ReplyTemplate updateTemplate(Long templateId, String name, String content) {
        log.info("Updating template: id={}", templateId);
        ReplyTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        template.setName(name);
        template.setContent(content);
        return templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        log.info("Deleting template: id={}", templateId);
        if (!templateRepository.existsById(templateId)) {
            throw new IllegalArgumentException("Template not found");
        }
        templateRepository.deleteById(templateId);
    }
}
