package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
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

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public void registerUser(String username, String password, String email) {
        User user = new User(username, encoder.encode(password), email, LocalDateTime.now(), false);
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

    // Need to implement OTP here.
    public void forgotPassword(String username, String email){
        var user = userRepository.findByUsername(username);

    }

    public void changePassword(String username) {
        Scanner scanner = new Scanner(System.in);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.print("Enter your current password: ");
        String currentPassword = scanner.nextLine();
        if (!encoder.matches(currentPassword, user.getPassword())) {
            System.out.println("Incorrect current password.");
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

    // This method will track login and logout
    public void loggedIn(String username){
        User user = userRepository.findByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Upload File 2. Download File 3. Display Accessible Files 4. Rename File 5. Delete file 6. Logout");
            String choice = scanner.nextLine();
            String name, path;
            switch (choice) {
                case "1":
                    System.out.println("Please enter the correct file path and file name to upload the file: ");
                    System.out.println("Please enter the file path:");
                    path = scanner.nextLine();
                    System.out.println("Please enter the file name:");
                    name = scanner.nextLine();
                    //fileService.uploadFile(path, name);
                    break;
                case "2":
                    System.out.println("You are here to download the file by giving the filename you want to download and the destination path where you want it to be downloaded.");
                    System.out.println("Please enter the file path:");
                    path = scanner.nextLine();
                    System.out.println("Please enter the file name:");
                    name = scanner.nextLine();
                    //fileService.downloadFile(name, path);
                    break;
                case "3":
                    // displaying files


                    break;
                case "4":
                    //display files and rename them
                    break;
                case "5":
                    //display files and delete it
                    break;
                case "6":
                    user.setLastLogout(LocalDateTime.now());

                default:
                    System.out.println("Invalid choice, try again");
            }
        }
    };
    //userRepository.save(user);
}