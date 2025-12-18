package com.choose.controller;

import com.choose.common.ApiResponse;
import com.choose.model.User;
import com.choose.service.CartService;
import com.choose.service.OrderService;
import com.choose.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final OrderService orderService;
    private final CartService cartService;
    /**
     * Get current user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(new UserProfileResponse(user)));
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        
        User updatedData = new User();
        updatedData.setName(request.getName());
        updatedData.setPhone(request.getPhone());
        updatedData.setPassword(request.getNewPassword());
        
        User updated = userService.updateUser(user.getUserId(), updatedData);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", new UserProfileResponse(updated)));
    }

    /**
     * Admin: Get all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserProfileResponse> responses = users.stream()
                .map(UserProfileResponse::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Admin: Get user by ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(u -> ResponseEntity.ok(ApiResponse.success(new UserProfileResponse(u))))
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    static class UpdateProfileRequest {
        private String name;
        private String phone;
        private String newPassword;
    }

    @Data
    static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }

    @Data
    static class DeleteAccountRequest {
        @jakarta.validation.constraints.NotBlank(message = "請輸入密碼")
        private String password;
    }

    @Data
    static class UserProfileResponse {
        private Long id;
        private String email;
        private String name;
        private String phone;
        private String role;

        public UserProfileResponse(User user) {
            this.id = user.getUserId();
            this.email = user.getEmail();
            this.name = user.getName();
            this.phone = user.getPhone();
            this.role = user.getRole().name();
        }
    }

    /**
     * Change password (requires current password verification)
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        try {
            userService.changePassword(user.getUserId(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete current user's account (de-identification + soft delete)
     * 使用 POST 替代 DELETE 以支援 request body
     */
    @PostMapping("/me/delete")
    public ResponseEntity<ApiResponse<String>> deleteMyAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DeleteAccountRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }

        try {
            userService.deleteUserAccount(user.getUserId(), request.getPassword(), orderService, cartService);
            return ResponseEntity.ok(ApiResponse.success("帳號已成功刪除", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

