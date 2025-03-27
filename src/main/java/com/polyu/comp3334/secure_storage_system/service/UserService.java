package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

//public class UserService implements UserDetailsService {
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileService fileService;

    //BCrypt uses the Blowfish cipher internally, adapted into a one-way hashing function.
    //2^12 = 4,096 rounds, controlling how computationally expensive (and thus secure) the hashing is.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public void registerUser(String username, String password, String email) {
        User user = new User(username, encoder.encode(password), email, LocalDateTime.now(), false);
        userRepository.save(user);
    }

    public Optional<User> userAuthentication(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && encoder.matches(password, user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // Need to implement OTP here.
    public void forgotPassword(String username, String email){
        var user = userRepository.findByUsername(username);

    }

    public void changePassword(Scanner scanner,User user) {
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.print("Enter your current password: ");
        String currentPassword = scanner.nextLine();
        if (!encoder.matches(currentPassword, user.getPassword())) {
            System.out.println("Incorrect password.");
            return;
        }
        String newPassword;
        while (true) {
            System.out.print("Enter new password: ");
            String password1 = scanner.nextLine();

            System.out.print("Confirm new password: ");
            String password2 = scanner.nextLine();

            if (password1.equals(password2)) {
                newPassword = password1;
                break;
            }
            System.out.println("Passwords do not match. Please try again.");
        }
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        System.out.println("Password changed successfully.");
    }

    //Tracks login of the user
    public void recordLogin(User user){
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    //Tracks logout of the user
    public void recordLogout(User user){
        user.setLastLogout(LocalDateTime.now());
        userRepository.save(user);
        System.out.println("You've been logged out.");
        System.out.println("*******************************************************");
    }
}