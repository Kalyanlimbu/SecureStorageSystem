package com.polyu.comp3334.secure_storage_system.repository;

import com.polyu.comp3334.secure_storage_system.model.PwdResetToken;
import com.polyu.comp3334.secure_storage_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PwdResetTokenRepository extends JpaRepository<PwdResetToken, Long> {
    Optional<PwdResetToken> findByToken(String token);
    List<PwdResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, LocalDateTime expiryDateAfter);
}
