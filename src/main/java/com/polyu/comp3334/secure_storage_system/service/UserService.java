package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

//public class UserService implements UserDetailsService {
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    //BCrypt uses the Blowfish cipher internally, adapted into a one-way hashing function.
    //2^12 = 4,096 rounds, controlling how computationally expensive (and thus secure) the hashing is.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public void registerUser(Long id, String username, String password, String email) {
        User user = new User(id, username, encoder.encode(password), email);
        System.out.println(user);
        userRepository.save(user);
    }
    public boolean loginUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return encoder.matches(password, user.getPassword());
        }
        // Username doesn't exist in the database
        return false;
    }
    public void forgotPassword(String username, String email){
        var user = userRepository.findByUsername(username);

    }

}