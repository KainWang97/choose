package com.choose.service;

import com.choose.model.User;
import com.choose.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(User user) {
        log.info("Registering new user: email={}", user.getEmail());
        
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registration failed: Email already exists, email={}", user.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        
        log.info("User registered successfully: userId={}, email={}", saved.getUserId(), saved.getEmail());
        return saved;
    }

    /**
     * 產生 20 位亂數密碼（英數混合）
     */
    public String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 簡化註冊（不需密碼，系統自動產生）
     */
    @Transactional
    public User registerSimple(String email, String name) {
        log.info("Simple registration: email={}", email);
        
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: Email already exists, email={}", email);
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(generateRandomPassword()));
        user.setEmailVerified(false);
        
        User saved = userRepository.save(user);
        log.info("User registered (simple) successfully: userId={}, email={}", saved.getUserId(), saved.getEmail());
        return saved;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public List<User> findAll() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        log.info("Updating user profile: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User update failed: User not found, userId={}", userId);
                    return new IllegalArgumentException("User not found");
                });
        
        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getPhone() != null) {
            user.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            log.info("Password updated for user: userId={}", userId);
        }
        
        User saved = userRepository.save(user);
        log.info("User profile updated: userId={}", userId);
        return saved;
    }

    /**
     * Change password with current password verification
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Attempting password change for userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Password change failed: User not found, userId={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password change failed: Incorrect current password, userId={}", userId);
            throw new IllegalArgumentException("目前密碼不正確");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            log.warn("Password change failed: New password too short, userId={}", userId);
            throw new IllegalArgumentException("新密碼長度至少需要 6 個字元");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for userId={}", userId);
    }

    /**
     * 驗證信箱
     */
    @Transactional
    public void verifyEmail(Long userId) {
        log.info("Verifying email for userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for userId={}", userId);
    }

    /**
     * 重設密碼（無需驗證舊密碼，用於忘記密碼流程）
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for userId={}", userId);
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("密碼長度至少需要 6 個字元");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordSet(true);
        userRepository.save(user);
        log.info("Password reset successfully for userId={}", userId);
    }

    /**
     * 初次設定密碼（新用戶）
     */
    @Transactional
    public void setPassword(Long userId, String newPassword) {
        log.info("Setting password for first time: userId={}", userId);
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("密碼長度至少需要 6 個字元");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getPasswordSet()) {
            throw new IllegalArgumentException("您已設定過密碼");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordSet(true);
        userRepository.save(user);
        log.info("Password set successfully for userId={}", userId);
    }

    /**
     * 刪除使用者帳號（去識別化 + 軟刪除）
     * @param userId 使用者 ID
     * @param currentPassword 當前密碼（用於驗證身份）
     * @param orderService OrderService 用於檢查進行中訂單
     * @param cartService CartService 用於清空購物車
     */
    @Transactional
    public void deleteUserAccount(Long userId, String currentPassword, 
                                   OrderService orderService, CartService cartService) {
        log.info("Attempting to delete user account: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Account deletion failed: User not found, userId={}", userId);
                    return new IllegalArgumentException("User not found");
                });

        // 1. 驗證密碼
        if (user.getPassword() == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Account deletion failed: Incorrect password, userId={}", userId);
            throw new IllegalArgumentException("密碼不正確");
        }

        // 2. 檢查是否有進行中訂單
        if (orderService.hasActiveOrders(userId)) {
            log.warn("Account deletion failed: Active orders exist, userId={}", userId);
            throw new IllegalStateException("尚有未完成訂單，無法刪除帳號");
        }

        // 3. 去識別化 (De-identification)
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        String originalEmail = user.getEmail();
        
        user.setName("Deleted-User-" + uniqueSuffix);
        user.setEmail("deleted-" + uniqueSuffix + "@inactive.local");
        user.setPhone(null);
        user.setPassword(null);

        // 4. 標記刪除
        user.setIsDeleted(true);
        user.setDeletedAt(java.time.LocalDateTime.now());

        // 5. 清空購物車
        cartService.clearCart(userId);

        userRepository.save(user);
        log.info("User account deleted successfully: userId={}, originalEmail={}", userId, originalEmail);
    }
}
