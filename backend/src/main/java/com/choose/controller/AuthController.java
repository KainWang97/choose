package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.model.User;
import com.choose.security.JwtUtil;
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
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt: email={}", request.getEmail());
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        
        User savedUser = userService.register(user);
        
        // Generate token and set as HttpOnly cookie
        String token = jwtUtil.generateToken(savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole().name());
        ResponseCookie cookie = createAuthCookie(token);
        
        log.info("Registration successful: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("User registered successfully", new UserResponse(savedUser)));
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
                .body(ApiResponse.success("Login successful", new UserResponse(user)));
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
        
        @NotBlank(message = "請輸入密碼")
        @Size(min = 6, message = "密碼至少需要 6 個字元")
        private String password;
        
        @NotBlank(message = "請輸入姓名")
        private String name;
        
        @Pattern(regexp = "^$|^09\\d{8}$", message = "請輸入有效的手機號碼（例：0912345678）")
        private String phone;
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

    // Response DTO - token removed, now sent via HttpOnly cookie
    @Data
    static class UserResponse {
        private Long id;
        private String email;
        private String name;
        private String phone;
        private String role;

        public UserResponse(User user) {
            this.id = user.getUserId();
            this.email = user.getEmail();
            this.name = user.getName();
            this.phone = user.getPhone();
            this.role = user.getRole().name();
        }
    }
}
