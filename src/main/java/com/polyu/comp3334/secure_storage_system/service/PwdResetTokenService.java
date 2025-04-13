package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.PwdResetToken;
import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.PwdResetTokenRepository;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class PwdResetTokenService {
    private static final long PIN_EXPIRATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PwdResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;

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

        // build token entity
        PwdResetToken token = new PwdResetToken(
                user,
                pin,
                LocalDateTime.now().plusMinutes(PIN_EXPIRATION_MINUTES)
        );
        tokenRepository.save(token); // maybe angad you can help me hash this token so i don't store its plain text in the server

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

    /**
     * Step 2: User submits email + PIN + new password.
     * We validate the PIN, mark PIN used.
     */
    public void tokenAuthenticate(String username, String pin) {
        User user = userRepository.findByUsername(username);
        PwdResetToken token = tokenRepository.findByToken(pin) // the pin used here is user input, might have to hash to check if the input matches token (if we hash the token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code"));

        if (token.isUsed()
                || token.getExpiryDate().isBefore(LocalDateTime.now().toInstant(ZoneOffset.UTC))
                || !token.getUser().getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }
        // mark PIN as used
        token.setUsed(true);
        tokenRepository.save(token);
    }
}
