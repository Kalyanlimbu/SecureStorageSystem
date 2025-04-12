package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.PwdResetToken;
import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.PwdResetTokenRepository;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;

//public class UserService implements UserDetailsService {
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileService fileService;

    private static final String HMAC_ALGORITHM="HmacSHA256";
    private static final int SALT_LENGTH = 32;
    private static final int KEY_LENGTH = 32;
    private static final int ITERATION_COUNT = 10000;

    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
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

    private static String hashPassword(String password, byte[] salt) {
        byte[] hmacKey = generateRandomBytes(KEY_LENGTH);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        byte[] hash = HmacSHA256((password + saltBase64).getBytes(), hmacKey);
        for (int i = 0; i < ITERATION_COUNT; i++) {
            hash = HmacSHA256(hash, hmacKey);
        }

        return Base64.getEncoder().encodeToString(hmacKey) + ":" + saltBase64 + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 3) return false;
        String hmacKeyEncoded = parts[0].trim();
        String saltEncoded = parts[1].trim();
        String hashEncoded = parts[2].trim();

        byte[] hmacKey = Base64.getDecoder().decode(hmacKeyEncoded);
        byte[] salt = Base64.getDecoder().decode(saltEncoded);
        byte[] originalHash = Base64.getDecoder().decode(hashEncoded);
        // Recompute the hash
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        byte[] computedHash = HmacSHA256((password + saltBase64).getBytes(), hmacKey);

        for (int i = 0; i < ITERATION_COUNT; i++) {
            computedHash = HmacSHA256(computedHash, hmacKey);
        }
        return slowEquals(originalHash, computedHash);
    }

// Time comparison to prevent timing attacks
    private boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    @Transactional
    public boolean usernameExists(String username){
        return userRepository.existsByUsername(username);
    }
    @Value("${ADMIN_USERNAME}")
    private String AdminUsername;
    @Value("${ADMIN_PASSWORD}")
    private String AdminPassword;
    @Value("${ADMIN_EMAIL}")
    private String AdminEmail;

    public void adminCreation() {

        String adminName = AdminUsername, adminPassword = AdminPassword, adminEmail = AdminEmail;
        byte[] salt = generateRandomBytes(SALT_LENGTH);
        String hashedPassword = hashPassword(adminPassword, salt);
        // Secure way to check for existing admin
        User adminCheck = userRepository.findByUsername("admin");
        if (adminCheck == null || !adminCheck.isAdmin()) {
            User admin = new User(adminName, hashedPassword, adminEmail, LocalDateTime.now(), true);
            userRepository.save(admin);
        }
    }

    @Transactional
    public void registerUser(String username, String password, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (usernameExists(username)) {
            throw new IllegalStateException("Username already exists");
        }

        byte[] salt = generateRandomBytes(SALT_LENGTH);
        String hashedPassword = hashPassword(password, salt);

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setRegisterAt(LocalDateTime.now());
        userRepository.save(user);
    }

//    @Transactional
//    public Optional<User> userAuthentication(String username, String password) {
//        User user = userRepository.findByUsername(username);
//        if (user != null && verifyPassword(password, user.getPassword())) {
//            return Optional.of(user);
//        }
//        return Optional.empty();
//    }
//    @Transactional
//    public Boolean userAuthentication(String username, String password){
//        User user = userRepository.findByUsername(username);
//        if (user != null && password.equals(user.getPassword())){
//            recordLogin(user);
//            return true;
//        }
//        return false;
//    }

    @Transactional

    public void forgotPassword(String username, String email){
        // let's call the required functions here



    }

//    @Transactional
//    public void changePassword(Scanner scanner, User user) {
//        if (user == null) {
//            System.out.println("User not found.");
//            return;
//        }
//
//        Console console = System.console();
//        String currentPassword;
//
//        if (console != null) {
//            char[] currentPasswordArray = console.readPassword("Enter your current password: ");
//            currentPassword = new String(currentPasswordArray);
//        } else {
//            System.out.print("Enter your current password: ");
//            currentPassword = scanner.nextLine();
//        }
//
//        if (!verifyPassword(currentPassword, user.getPassword())) {
//            System.out.println("Incorrect password.");
//            return;
//        }
//
//        String newPassword;
//        while (true) {
//                // Use Console for secure input
//                if (console != null) {
//                    char[] newPasswordArray = console.readPassword("Enter new password: ");
//                    char[] confirmArray = console.readPassword("Confirm new password: ");
//
//                    if (Arrays.equals(newPasswordArray, confirmArray)) {
//                        newPassword = new String(newPasswordArray);
//                        break;
//                    }
//            } else {
//                System.out.print("Enter new password: ");
//                String password1 = scanner.nextLine();
//
//                System.out.print("Confirm new password: ");
//                String password2 = scanner.nextLine();
//
//                if (password1.equals(password2)) {
//                    newPassword = password1;
//                    break;
//                }
//            }
//            System.out.println("Passwords do not match. Please try again.");
//        }
//
//        byte[] newSalt = generateRandomBytes(SALT_LENGTH);
//        user.setPassword(hashPassword(newPassword, newSalt));
//        userRepository.save(user);
//        System.out.println("Password changed successfully.");
//    }

    @Transactional
    public void changePassword(String username, String newPassword){
        User user = userRepository.findByUsername(username);
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    //Tracks login of the user
    @Transactional
    public void recordLogin(String username, String password){
        User user = userRepository.findByUsername(username);
        if(password.equals(user.getPassword())){
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    //Tracks logout of the user
    @Transactional
    public void recordLogout(String username){
        User user = userRepository.findByUsername(username);
        user.setLastLogout(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public boolean isValidAdmin(String adminName, String adminPassword) {
        User admin = userRepository.findByUsername(adminName);
        if(admin == null) return false;
        if(adminPassword.equals(admin.getPassword())){
            return true;
        }
        return false;
    }
}

