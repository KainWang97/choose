package com.choose.config;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloudinary 配置類別
 * 提供 Cloudinary 實例供圖片上傳服務使用
 */
@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        log.info("=== Cloudinary Configuration ===");
        log.info("cloud_name: '{}'", cloudName);
        log.info("api_key: '{}'", apiKey);
        log.info("api_secret: '{}***'", apiSecret != null && apiSecret.length() > 4 ? apiSecret.substring(0, 4) : "EMPTY");
        
        if (cloudName == null || cloudName.isEmpty() ||
            apiKey == null || apiKey.isEmpty() ||
            apiSecret == null || apiSecret.isEmpty()) {
            log.error("Cloudinary configuration is incomplete. Image upload will not work.");
            log.error("Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.");
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true");

        log.info("=== Cloudinary Configuration Complete ===");
        return new Cloudinary(config);
    }
}
