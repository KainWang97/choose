package com.choose.dto.response;

import com.choose.model.Category;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category DTO for frontend compatibility
 * Maps categoryId -> id
 */
@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    public static CategoryDTO fromEntity(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }

    public static List<CategoryDTO> fromEntities(List<Category> categories) {
        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .toList();
    }
}

