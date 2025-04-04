package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.User;
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


    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private byte[] HmacSHA256(byte[] data, byte[] key) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(key, HMAC_ALGORITHM);
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {;
            throw new RuntimeException("HMAC256 Computation failed", e);
        }
    }

    private String hashPassword(String password, byte[] salt){
        byte[] hmacKey = generateRandomBytes(KEY_LENGTH);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        //Initial hash
        byte[] hash = HmacSHA256(hmacKey, (password + saltBase64).getBytes(StandardCharsets.UTF_8));
        //Iterate the hash
        for (int i = 0; i < ITERATION_COUNT; i++) {
            hash = HmacSHA256(hmacKey, hash);
        }
        
        return Base64.getEncoder().encodeToString(hmacKey) + ":" 
             + saltBase64 + ":" 
             + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 3) return false;

        byte[] hmacKey = Base64.getDecoder().decode(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] originalHash = Base64.getDecoder().decode(parts[2]);

        // Recompute the hash
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        byte[] computedHash = HmacSHA256(hmacKey, (password + saltBase64).getBytes(StandardCharsets.UTF_8));

        for (int i = 0; i < ITERATION_COUNT; i++) {
            computedHash = HmacSHA256(hmacKey, computedHash);
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
    @Transactional
    public Boolean userAuthentication(String username, String password){
        User user = userRepository.findByUsername(username);
        if (user != null && password.equals(user.getPassword())){
            recordLogin(user);
            return true;
        }
        return false;
    }

    @Transactional
    // Need to implement OTP here.
    public void forgotPassword(String username, String email){
        var user = userRepository.findByUsername(username);

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
    public void recordLogin(User user){
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    //Tracks logout of the user
    @Transactional
    public void recordLogout(String username){
        User user = userRepository.findByUsername(username);
        user.setLastLogout(LocalDateTime.now());
        userRepository.save(user);
    }
}
