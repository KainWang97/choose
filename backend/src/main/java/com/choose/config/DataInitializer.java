package com.choose.config;

import com.choose.model.User;
import com.choose.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 程式啟動時自動建立預設管理員帳號
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_EMAIL = "admin@choose.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456";
    private static final String DEFAULT_ADMIN_NAME = "管理員";

    @Override
    public void run(String... args) {
        createDefaultAdminIfNotExists();
    }

    private void createDefaultAdminIfNotExists() {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            log.info("預設管理員帳號已存在: {}", DEFAULT_ADMIN_EMAIL);
            return;
        }

        User admin = new User();
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setName(DEFAULT_ADMIN_NAME);
        admin.setPhone("0912345678");
        admin.setRole(User.Role.ADMIN);
        admin.setIsDeleted(false);

        userRepository.save(admin);
        log.info("已建立預設管理員帳號: email={}, password={}", DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);
    }
}
