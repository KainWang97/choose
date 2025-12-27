package com.choose.repository;

import com.choose.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByUserUserIdAndType(Long userId, VerificationToken.TokenType type);
    
    void deleteByUserUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
