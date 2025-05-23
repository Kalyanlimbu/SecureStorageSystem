package com.polyu.comp3334.secure_storage_system.controller;

import com.polyu.comp3334.secure_storage_system.model.AuditLog;
import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import com.polyu.comp3334.secure_storage_system.service.AuditLogService;
import com.polyu.comp3334.secure_storage_system.service.PwdResetTokenService;
import com.polyu.comp3334.secure_storage_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class  UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/getLogsForAdmin")
    public ResponseEntity<List<String>> getLogsForAdmin(
            @RequestParam("adminName") String adminName,
            @RequestParam("adminPassword") String adminPassword) {
        try {
            List<String> logs = auditLogService.getAllLogs();
            return ResponseEntity.ok(logs.isEmpty() ? Collections.emptyList() : logs);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Collections.singletonList("Error retrieving logs"));
        }
    }

    @PostMapping("/{username}/check")
    public ResponseEntity<String> existUsername(@PathVariable String username) {
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body("Username already exists. Please enter a different username.");
        }
        return ResponseEntity.ok("Username is available.");
    }

    @GetMapping("/getHashedPassword")
    public ResponseEntity<String> getHashedPassword(@RequestParam("username") String username){
        try{
            User user = userRepository.findByUsername(username);
            return ResponseEntity.ok(user.getPassword());
        }catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
            @RequestParam("password") String password,
            @RequestParam("signature") String signature) {
        try{
            userService.recordLogin(username, password);
            auditLogService.logInLog(username, signature);

            return ResponseEntity.ok(username + ", you have logged in successfully.");
        }catch(IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(
            @RequestParam("username") String username,
            @RequestParam("signature") String signature
            ){
        try{
            userService.recordLogout(username);
            auditLogService.logOutLog(username, signature);

            return ResponseEntity.ok(username + "! you've been logged out.");
        } catch(IllegalArgumentException | IllegalStateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @Autowired
    private PwdResetTokenService authservice;
    @PostMapping("/initiate-forget-password")
    public ResponseEntity<String> getToken (@RequestParam("username") String username){
        try{
            User user = userRepository.findByUsername(username);
            String email = user.getEmail();
            authservice.emailAuthenticate(username, user.getEmail());
            return ResponseEntity.ok("Email verification code has been sent to: " + email);
        }catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("something wrong when sending. check server.");
        }
    }
    @PostMapping("/submit-token-password")
    public ResponseEntity<String> submitToken(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String pin = userData.get("pin");
        try {
            authservice.tokenAuthenticate(username, pin);
            return ResponseEntity.ok("Multifactor authentication successful");
        } catch (IllegalArgumentException | IllegalStateException e) {
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

