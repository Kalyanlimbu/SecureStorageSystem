package com.polyu.comp3334.secure_storage_system.repository;

import com.polyu.comp3334.secure_storage_system.model.PwdResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PwdResetTokenRepository extends JpaRepository<PwdResetToken, Long> {
    Optional<PwdResetToken> findByToken(String token);
}
