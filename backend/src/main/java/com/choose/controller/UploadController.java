package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.service.ImageUploadService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 檔案上傳 Controller
 * 處理圖片上傳相關 API
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final ImageUploadService imageUploadService;

    /**
     * 上傳商品圖片
     * POST /api/upload/image
     *
     * @param file 圖片檔案 (multipart/form-data)
     * @return 上傳後的圖片 URL
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received image upload request: filename={}", file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Upload failed: empty file");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No file provided"));
        }

        try {
            String url = imageUploadService.uploadImage(file);
            ImageUploadResponse response = new ImageUploadResponse();
            response.setUrl(url);
            
            log.info("Image upload successful: url={}", url);
            return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("Upload failed with IO error", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to upload image: " + e.getMessage()));
        }
    }

    @Data
    static class ImageUploadResponse {
        private String url;
    }
}
