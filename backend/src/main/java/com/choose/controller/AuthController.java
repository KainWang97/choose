package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.model.User;
import com.choose.model.VerificationToken;
import com.choose.security.JwtUtil;
import com.choose.service.EmailService;
import com.choose.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String AUTH_COOKIE_NAME = "auth_token";
    
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration; // 24 hours in milliseconds

    /**
     * 建立 HttpOnly Cookie
     */
    private ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true) // HTTPS 必須為 true
                .path("/")
                .maxAge(jwtExpiration / 1000) // 轉換為秒
                .sameSite("None") // 跨域請求需要 None
                .build();
    }

    /**
     * 建立清除 Cookie
     */
    private ResponseCookie createClearCookie() {
        return ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt: email={}", request.getEmail());
        
        // 使用簡化註冊（自動產生密碼）
        User savedUser = userService.registerSimple(request.getEmail(), request.getName());
        
        // 發送驗證信
        try {
            emailService.sendVerificationEmail(savedUser);
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            // 不中斷註冊流程，但記錄錯誤
        }
        
        log.info("Registration successful: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return ResponseEntity.ok(ApiResponse.success("註冊成功！請查收驗證信件以完成註冊", (String) null));
    }

    /**
     * Magic Link 登入 - 發送登入驗證信
     */
    @PostMapping("/login-magic")
    public ResponseEntity<ApiResponse<String>> loginMagic(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Magic link login request: email={}", request.getEmail());
        
        userService.findByEmail(request.getEmail()).ifPresent(user -> {
            try {
                emailService.sendLoginLinkEmail(user);
            } catch (Exception e) {
                log.error("Failed to send login link email: {}", e.getMessage());
            }
        });
        
        // 統一回應，避免洩漏帳號是否存在
        return ResponseEntity.ok(ApiResponse.success("如果該信箱已註冊，您將會收到登入連結", (String) null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt: email={}", request.getEmail());
        
        User user = userService.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            log.warn("Login failed: User not found, email={}", request.getEmail());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid email or password"));
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password, email={}", request.getEmail());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid email or password"));
        }
        
        // Generate token and set as HttpOnly cookie
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole().name());
        ResponseCookie cookie = createAuthCookie(token);
        
        log.info("Login successful: userId={}, email={}, role={}", 
                user.getUserId(), user.getEmail(), user.getRole());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Login successful", new UserResponse(user, token)));
    }

    /**
     * 驗證信箱 - 驗證成功後自動登入
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        log.info("Email verification attempt: token={}", request.getToken());
        
        return emailService.validateToken(request.getToken())
                .filter(t -> t.getType() == VerificationToken.TokenType.EMAIL_VERIFY)
                .map(token -> {
                    User user = token.getUser();
                    userService.verifyEmail(user.getUserId());
                    emailService.deleteToken(token);
                    
                    // 自動登入 - 產生 JWT Token
                    String jwtToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole().name());
                    ResponseCookie cookie = createAuthCookie(jwtToken);
                    
                    // 更新 user 狀態
                    user.setEmailVerified(true);
                    
                    log.info("Email verified and auto-login: userId={}", user.getUserId());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(ApiResponse.success("信箱驗證成功！已自動登入", new UserResponse(user, jwtToken)));
                })
                .orElseGet(() -> {
                    log.warn("Email verification failed: invalid or expired token");
                    return ResponseEntity.badRequest().body(ApiResponse.error("驗證連結無效或已過期"));
                });
    }

    /**
     * Magic Link 登入驗證
     */
    @PostMapping("/login-verify")
    public ResponseEntity<ApiResponse<UserResponse>> loginVerify(@RequestBody VerifyEmailRequest request) {
        log.info("Magic link login verification: token={}", request.getToken());
        
        return emailService.validateToken(request.getToken())
                .filter(t -> t.getType() == VerificationToken.TokenType.LOGIN_LINK)
                .map(token -> {
                    User user = token.getUser();
                    emailService.deleteToken(token);
                    
                    // 產生 JWT Token
                    String jwtToken = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole().name());
                    ResponseCookie cookie = createAuthCookie(jwtToken);
                    
                    log.info("Magic link login successful: userId={}", user.getUserId());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, cookie.toString())
                            .body(ApiResponse.success("登入成功", new UserResponse(user, jwtToken)));
                })
                .orElseGet(() -> {
                    log.warn("Magic link login failed: invalid or expired token");
                    return ResponseEntity.badRequest().body(ApiResponse.error("登入連結無效或已過期"));
                });
    }

    /**
     * 重新發送驗證信
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("請先登入"));
        }
        
        if (user.getEmailVerified()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("您的信箱已經驗證過了"));
        }
        
        try {
            emailService.sendVerificationEmail(user);
            return ResponseEntity.ok(ApiResponse.success("驗證信已重新發送，請查收您的信箱", (String) null));
        } catch (Exception e) {
            log.error("Failed to resend verification email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("發送失敗，請稍後再試"));
        }
    }

    /**
     * 忘記密碼 - 發送重設連結
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset request: email={}", request.getEmail());
        
        // 統一回應，避免洩漏帳號是否存在
        userService.findByEmail(request.getEmail()).ifPresent(user -> {
            try {
                emailService.sendPasswordResetEmail(user);
            } catch (Exception e) {
                log.error("Failed to send password reset email: {}", e.getMessage());
            }
        });
        
        return ResponseEntity.ok(ApiResponse.success("如果該信箱已註冊，您將會收到重設密碼的連結", (String) null));
    }

    /**
     * 重設密碼
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt: token={}", request.getToken());
        
        return emailService.validateToken(request.getToken())
                .filter(t -> t.getType() == VerificationToken.TokenType.PASSWORD_RESET)
                .map(token -> {
                    User user = token.getUser();
                    userService.resetPassword(user.getUserId(), request.getNewPassword());
                    emailService.deleteToken(token);
                    log.info("Password reset successfully: userId={}", user.getUserId());
                    return ResponseEntity.ok(ApiResponse.success("密碼重設成功，請使用新密碼登入", (String) null));
                })
                .orElseGet(() -> {
                    log.warn("Password reset failed: invalid or expired token");
                    return ResponseEntity.badRequest().body(ApiResponse.error("重設連結無效或已過期"));
                });
    }

    /**
     * 初次設定密碼（已登入用戶，不需 token）
     */
    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse<String>> setPassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SetPasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("請先登入"));
        }
        
        if (user.getPasswordSet()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("您已設定過密碼，請使用更換密碼功能"));
        }
        
        // 如果提供了 token，驗證 token（來自驗證信箱流程）
        // 如果沒有 token，直接允許已登入用戶設定密碼
        if (request.getToken() != null && !request.getToken().isEmpty()) {
            return emailService.validateToken(request.getToken())
                    .filter(t -> t.getType() == VerificationToken.TokenType.EMAIL_VERIFY)
                    .filter(t -> t.getUser().getUserId().equals(user.getUserId()))
                    .map(token -> {
                        userService.setPassword(user.getUserId(), request.getNewPassword());
                        emailService.deleteToken(token);
                        log.info("Password set successfully with token: userId={}", user.getUserId());
                        return ResponseEntity.ok(ApiResponse.success("密碼設定成功", (String) null));
                    })
                    .orElseGet(() -> {
                        log.warn("Set password failed: invalid or expired token");
                        return ResponseEntity.badRequest().body(ApiResponse.error("驗證連結無效或已過期"));
                    });
        } else {
            // 已登入用戶直接設定密碼
            userService.setPassword(user.getUserId(), request.getNewPassword());
            log.info("Password set successfully: userId={}", user.getUserId());
            return ResponseEntity.ok(ApiResponse.success("密碼設定成功", (String) null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(new UserResponse(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal User user) {
        if (user != null) {
            log.info("User logged out: userId={}, email={}", user.getUserId(), user.getEmail());
        }
        // Clear the auth cookie
        ResponseCookie clearCookie = createClearCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.success("Logout successful", null));
    }

    // Request DTOs
    @Data
    static class RegisterRequest {
        @NotBlank(message = "請輸入 Email")
        @Email(message = "請輸入有效的 Email 格式")
        private String email;
        
        @NotBlank(message = "請輸入姓名")
        private String name;
    }

    @Data
    static class LoginRequest {
        @NotBlank(message = "請輸入 Email")
        @Email(message = "請輸入有效的 Email 格式")
        private String email;
        
        @NotBlank(message = "請輸入密碼")
        @Size(min = 6, message = "密碼至少需要 6 個字元")
        private String password;
    }

    @Data
    static class VerifyEmailRequest {
        @NotBlank(message = "Token is required")
        private String token;
    }

    @Data
    static class ForgotPasswordRequest {
        @NotBlank(message = "請輸入 Email")
        @Email(message = "請輸入有效的 Email 格式")
        private String email;
    }

    @Data
    static class ResetPasswordRequest {
        @NotBlank(message = "Token is required")
        private String token;
        
        @NotBlank(message = "請輸入新密碼")
        @Size(min = 6, message = "密碼至少需要 6 個字元")
        private String newPassword;
    }

    @Data
    static class SetPasswordRequest {
        // Token 為可選：有 token 時驗證 token，無 token 時允許已登入用戶直接設定密碼
        private String token;
        
        @NotBlank(message = "請輸入密碼")
        @Size(min = 6, message = "密碼至少需要 6 個字元")
        private String newPassword;
    }

    // Response DTO - 包含 token 和 emailVerified 供 Frontend 使用
    @Data
    static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String phone;
        private String role;
        private String token;
        private Boolean emailVerified;
        private Boolean passwordSet;

        public UserResponse(User user) {
            this.id = user.getUserId();
            this.email = user.getEmail();
            this.name = user.getName();
            this.phone = user.getPhone();
            this.role = user.getRole().name();
            this.emailVerified = user.getEmailVerified();
            this.passwordSet = user.getPasswordSet();
        }
        
        public UserResponse(User user, String token) {
            this(user);
            this.token = token;
        }
    }
}
