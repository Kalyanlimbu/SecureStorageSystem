package com.polyu.comp3334.secure_storage_system.repository;

import com.polyu.comp3334.secure_storage_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String > {
    boolean existsByUsername(String username);
    User findByUsername(String username);
    User findByUsernameAndPasswordAndIsAdmin(String username, String password, boolean isAdmin);
    User findByUsernameAndEmail(String username, String email);
    User findByEmail(String email);
}