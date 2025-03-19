package com.polyu.comp3334.secure_storage_system.controller;

import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserRepository repo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @GetMapping("/")
    public List<User> retrieveUser(){
        return repo.findAll();
    }
    @PostMapping("/insert")
    public void addUser(@RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);
    }
}
