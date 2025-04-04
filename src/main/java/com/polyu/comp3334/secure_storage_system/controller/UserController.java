package com.polyu.comp3334.secure_storage_system.controller;

import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import com.polyu.comp3334.secure_storage_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @PostMapping("/{username}/check")
    public ResponseEntity<String> existUsername(@PathVariable String username) {
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already exists. Please enter a different username.");
        }
        return ResponseEntity.ok("Username is available.");
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String email = userData.get("email");
        try {
            User user = new User(username, password, email, LocalDateTime.now(), false);
            userRepository.save(user);
            return ResponseEntity.ok("User registered successfully: " + username);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        Boolean verify = userService.userAuthentication(username, password);
        if (!verify) {
            return ResponseEntity.badRequest().body("Invalid username or password.");
        }
        return ResponseEntity.ok(username + ", you have logged in successfully.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestParam("username") String username){
        try{
            userService.recordLogout(username);
            return ResponseEntity.ok(username + "! you've been logged out.");
        } catch(IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/changePassword")
    public ResponseEntity<String> changePassword(
            @RequestParam("username") String username,
            @RequestParam("newPassword") String newPassword){
        try {
            userService.changePassword(username, newPassword);
            return ResponseEntity.ok(username + ", your password was change successfully.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
