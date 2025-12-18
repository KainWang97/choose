package com.choose.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * 圖片上傳服務
 * 處理圖片上傳至 Cloudinary
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final Cloudinary cloudinary;

    /**
     * 上傳圖片至 Cloudinary
     *
     * @param file 上傳的圖片檔案
     * @return 圖片的 URL
     * @throws IOException 如果上傳失敗
     */
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading image: filename={}, size={} bytes, contentType={}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        // 驗證檔案類型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: jpg, jpeg, png, gif, webp");
        }

        // 驗證檔案大小 (最大 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File too large. Maximum size is 5MB");
        }

        try {
            // 建立 Eager Transformation 縮圖版本
            com.cloudinary.Transformation thumbnailTransform = new com.cloudinary.Transformation()
                    .width(600).crop("limit").quality("auto:best").fetchFormat("auto");
            com.cloudinary.Transformation largeTransform = new com.cloudinary.Transformation()
                    .width(1200).crop("limit").quality("auto:good").fetchFormat("auto");

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "chooseMVP/products",
                            "resource_type", "image",
                            "eager", Arrays.asList(thumbnailTransform, largeTransform),
                            "eager_async", false
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully: url={}", secureUrl);
            return secureUrl;
        } catch (Exception e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new IOException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    /**
     * 檢查是否為允許的圖片類型
     */
    private boolean isAllowedImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }
}
