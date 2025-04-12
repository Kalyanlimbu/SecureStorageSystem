package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.PwdResetToken;
import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.PwdResetTokenRepository;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class PwdResetTokenService {
    private static final long PIN_EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PwdResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PwdResetTokenService(UserRepository userRepository,
                                PwdResetTokenRepository tokenRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
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

        // build token entity
        PwdResetToken token = new PwdResetToken(
                user,
                pin,
                LocalDateTime.now().plusMinutes(PIN_EXPIRATION_MINUTES)
        );
        tokenRepository.save(token);

        // send email
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("Your Password Reset Verification Code");
        msg.setText(
                "Your one‑time verification code is: " + pin +
                        "\nIt will expire in " + PIN_EXPIRATION_MINUTES + " minutes."
        );
        mailSender.send(msg);
        return pin;
    }

    /**
     * Step 2: User submits email + PIN + new password.
     * We validate the PIN, reset password, mark PIN used.
     */
    public void resetPassword(String email, String pin, String newPassword) {
        User user;
        try{user = userRepository.findByEmail(email);}
        catch (Exception e){throw new IllegalArgumentException("Email does not match any user records\n");}

        PwdResetToken token = tokenRepository.findByToken(pin)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code"));

        if (token.isUsed()
                || token.getExpiryDate().isBefore(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                || !token.getUser().getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        // update and hash new password [ADD LOGIC HERE]
//        user.setPassword(passwordEncoder.encode(newPassword));
//        userRepository.save(user);

        // mark PIN as used
        token.setUsed(true);
        tokenRepository.save(token);
    }
}
