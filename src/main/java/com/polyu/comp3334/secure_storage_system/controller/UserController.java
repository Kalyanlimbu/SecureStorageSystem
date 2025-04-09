package com.polyu.comp3334.secure_storage_system.controller;

import com.polyu.comp3334.secure_storage_system.model.AuditLog;
import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import com.polyu.comp3334.secure_storage_system.service.AuditLogService;
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
            // Add admin validation logic here (e.g., check credentials)
//            if (!userService.isValidAdmin(adminName, adminPassword)) {
//                return ResponseEntity.status(403).body(Collections.singletonList("Unauthorized access"));
//            }
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
            @RequestParam("password") String password) {
        try{
            userService.recordLogin(username, password);
            if(!username.equals("admin")) {
                auditLogService.logInLog(username);
            }
            return ResponseEntity.ok(username + ", you have logged in successfully.");
        }catch(IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(@RequestParam("username") String username){
        try{
            userService.recordLogout(username);
            if(!username.equals("admin")) {
                auditLogService.logOutLog(username);
            }
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

