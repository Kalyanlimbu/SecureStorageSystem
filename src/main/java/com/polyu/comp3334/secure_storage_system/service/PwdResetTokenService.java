package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.PwdResetToken;
import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.PwdResetTokenRepository;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import com.polyu.comp3334.secure_storage_system.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class PwdResetTokenService {
    private static final long PIN_EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PwdResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

    private static final String HMAC_ALGORITHM="HmacSHA256";
    private static final int SALT_LENGTH = 32;
    private static final int KEY_LENGTH = 32;
    private static final int ITERATION_COUNT = 10000;

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
    @Autowired
    public PwdResetTokenService(UserRepository userRepository,
                                PwdResetTokenRepository tokenRepository,
                                JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    /**
     * Step 1: User has forgotten password and supplies email.
     * We generate a 6‑digit PIN, persist it, and email it.
     */
    public String emailAuthenticate(String email, String username) {
        User user;
        try {
            user = userRepository.findByUsernameAndEmail(email, username);
        } catch (Exception e) {
            throw new IllegalArgumentException("A user with the provided combination of information is not registered. Please try again\n");
        }

        // generate secure 6‑digit PIN
        String pin = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        byte[] salt = generateRandomBytes(SALT_LENGTH);
        String hashedToken = hashToken(pin, salt);

        // build token entity
        PwdResetToken token = new PwdResetToken(
                user,
                hashedToken,
                LocalDateTime.now().plusMinutes(PIN_EXPIRATION_MINUTES)
        );
        tokenRepository.save(token);
        // send email
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            String to = user.getEmail();
            msg.setFrom ("sidharth.kts05@gmail.com");
            msg.setTo(to);
            msg.setSubject("Your Password Reset Verification Code");
            msg.setText(
                    "Your one‑time verification code is: " + pin +
                            "\nIt will expire in " + PIN_EXPIRATION_MINUTES + " minutes."
            );
            mailSender.send(msg);
            return pin;
        }catch (Exception e){
            throw new IllegalArgumentException("Issue with sending email: " + e.getMessage());
        }
    }



    private String hashToken(String token, byte[] salt) {
        byte[] hmacKey = generateRandomBytes(KEY_LENGTH);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        byte[] hash = HmacSHA256((token + saltBase64).getBytes(), hmacKey);
        for (int i = 0; i < ITERATION_COUNT; i++) {
            hash = HmacSHA256(hash, hmacKey);
        }
        return Base64.getEncoder().encodeToString(hmacKey) + ":" + saltBase64 + ":" + Base64.getEncoder().encodeToString(hash);
    }
    private static byte[] HmacSHA256(byte[] data, byte[] key) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance(HMAC_ALGORITHM);
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(key, HMAC_ALGORITHM);
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (java.security.NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new RuntimeException("HMAC256 Computation failed", e);
        }
    }

    /**
     * Step 2: User submits email + PIN + new password.
     * We validate the PIN, mark PIN used.
     */
    public void tokenAuthenticate(String username, String pin) {
        User user = userRepository.findByUsername(username);
        List<PwdResetToken> tokens = tokenRepository.findByUserAndUsedFalseAndExpiryDateAfter(
                user,
                LocalDateTime.now(ZoneOffset.UTC)  // Compare directly with LocalDateTime
        );

        for (PwdResetToken token : tokens) {
            if (verifyToken(pin, token.getToken()) &&
                    !token.isUsed()){
                token.setUsed(true);
                tokenRepository.save(token);
                return;
            }
        }
        throw new IllegalArgumentException("Invalid or expired code");
    }
    private boolean verifyToken(String inputPin, String storedToken) {
        String[] parts = storedToken.split(":");
        if (parts.length != 3) return false;

        byte[] hmacKey = Base64.getDecoder().decode(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] storedHash = Base64.getDecoder().decode(parts[2]);

        // Recompute hash
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        byte[] computedHash = HmacSHA256((inputPin + saltBase64).getBytes(), hmacKey);
        for (int i = 0; i < ITERATION_COUNT; i++) {
            computedHash = HmacSHA256(computedHash, hmacKey);
        }

        return Arrays.equals(computedHash, storedHash);
    }}
