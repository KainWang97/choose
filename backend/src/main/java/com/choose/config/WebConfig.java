package com.choose.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置
 * 
 * 注意：CORS 配置已移至 SecurityConfig，
 * 因為 Spring Security Filter 會在 MVC 之前執行，
 * 若在此處配置 CORS 會導致重複標頭或衝突。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS 配置請見 SecurityConfig.corsConfigurationSource()
    // 其他 MVC 配置可在此新增（如 Formatter、Interceptor 等）
}
