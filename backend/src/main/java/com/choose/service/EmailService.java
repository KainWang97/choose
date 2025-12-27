package com.choose.service;

import com.choose.model.User;
import com.choose.model.VerificationToken;
import com.choose.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final VerificationTokenRepository tokenRepository;
    
    @Value("${app.frontend-url:https://choose.zeabur.app}")
    private String frontendUrl;
    
    @Value("${spring.mail.username:noreply@choose.com}")
    private String fromEmail;
    
    private static final int EMAIL_VERIFY_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_EXPIRY_HOURS = 1;
    
    /**
     * 產生驗證 Token
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 發送信箱驗證郵件
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        log.info("Sending verification email to: {}", user.getEmail());
        
        // 刪除舊的驗證 token
        tokenRepository.deleteByUserUserId(user.getUserId());
        
        // 建立新 token
        String token = generateToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setType(VerificationToken.TokenType.EMAIL_VERIFY);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(EMAIL_VERIFY_EXPIRY_HOURS));
        tokenRepository.save(verificationToken);
        
        // 發送郵件
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[Choose] 請驗證您的信箱");
        message.setText(
            "親愛的 " + user.getName() + "，\n\n" +
            "感謝您註冊 Choose！\n\n" +
            "請點擊以下連結驗證您的信箱：\n" +
            verifyUrl + "\n\n" +
            "此連結將在 24 小時後失效。\n\n" +
            "如果您沒有註冊 Choose，請忽略此信件。\n\n" +
            "Choose 團隊"
        );
        
        mailSender.send(message);
        log.info("Verification email sent successfully to: {}", user.getEmail());
    }
    
    /**
     * 發送密碼重設郵件
     */
    @Transactional
    public void sendPasswordResetEmail(User user) {
        log.info("Sending password reset email to: {}", user.getEmail());
        
        // 刪除舊的重設 token
        Optional<VerificationToken> existingToken = tokenRepository
                .findByUserUserIdAndType(user.getUserId(), VerificationToken.TokenType.PASSWORD_RESET);
        existingToken.ifPresent(tokenRepository::delete);
        
        // 建立新 token
        String token = generateToken();
        VerificationToken resetToken = new VerificationToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setType(VerificationToken.TokenType.PASSWORD_RESET);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(PASSWORD_RESET_EXPIRY_HOURS));
        tokenRepository.save(resetToken);
        
        // 發送郵件
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[Choose] 重設密碼");
        message.setText(
            "親愛的 " + user.getName() + "，\n\n" +
            "我們收到了您重設密碼的請求。\n\n" +
            "請點擊以下連結重設您的密碼：\n" +
            resetUrl + "\n\n" +
            "此連結將在 1 小時後失效。\n\n" +
            "如果您沒有請求重設密碼，請忽略此信件，您的帳號密碼不會被更改。\n\n" +
            "Choose 團隊"
        );
        
        mailSender.send(message);
        log.info("Password reset email sent successfully to: {}", user.getEmail());
    }

    /**
     * 發送 Magic Link 登入郵件
     */
    @Transactional
    public void sendLoginLinkEmail(User user) {
        log.info("Sending login link email to: {}", user.getEmail());
        
        // 刪除舊的登入 token
        Optional<VerificationToken> existingToken = tokenRepository
                .findByUserUserIdAndType(user.getUserId(), VerificationToken.TokenType.LOGIN_LINK);
        existingToken.ifPresent(tokenRepository::delete);
        
        // 建立新 token（15 分鐘有效）
        String token = generateToken();
        VerificationToken loginToken = new VerificationToken();
        loginToken.setToken(token);
        loginToken.setUser(user);
        loginToken.setType(VerificationToken.TokenType.LOGIN_LINK);
        loginToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(loginToken);
        
        // 發送郵件
        String loginUrl = frontendUrl + "/login-verify?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("[Choose] 登入驗證連結");
        message.setText(
            "親愛的 " + user.getName() + "，\n\n" +
            "您已請求使用信箱驗證登入。\n\n" +
            "請點擊以下連結登入：\n" +
            loginUrl + "\n\n" +
            "此連結將在 15 分鐘後失效。\n\n" +
            "如果您沒有請求登入，請忽略此信件。\n\n" +
            "Choose 團隊"
        );
        
        mailSender.send(message);
        log.info("Login link email sent successfully to: {}", user.getEmail());
    }
    
    /**
     * 驗證 Token
     */
    public Optional<VerificationToken> validateToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isExpired());
    }
    
    /**
     * 刪除 Token
     */
    @Transactional
    public void deleteToken(VerificationToken token) {
        tokenRepository.delete(token);
    }
    
    /**
     * 清除過期 Token（可定期執行）
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Expired tokens cleaned up");
    }

    /**
     * 發送客服回覆郵件給用戶
     */
    public void sendInquiryReplyEmail(com.choose.model.ContactMessage inquiry) {
        log.info("Sending inquiry reply email to: {}", inquiry.getEmail());
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(inquiry.getEmail());
        message.setSubject("[Choose] 您的詢問已回覆");
        
        String subjectLine = inquiry.getSubject() != null 
            ? "【主題】" + inquiry.getSubject() + "\n\n"
            : "";
        
        message.setText(
            "親愛的 " + inquiry.getName() + "，\n\n" +
            "感謝您的來信，以下是您的詢問與我們的回覆：\n\n" +
            subjectLine +
            "【您的問題】\n" + inquiry.getMessage() + "\n\n" +
            "【客服回覆】\n" + inquiry.getAdminReply() + "\n\n" +
            "如有任何疑問，歡迎再次與我們聯繫。\n\n" +
            "Choose 團隊"
        );
        
        mailSender.send(message);
        log.info("Inquiry reply email sent successfully to: {}", inquiry.getEmail());
    }
}
