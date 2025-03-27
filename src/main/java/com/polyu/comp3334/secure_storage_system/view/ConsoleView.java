package com.polyu.comp3334.secure_storage_system.view;

import com.polyu.comp3334.secure_storage_system.model.User;
import com.polyu.comp3334.secure_storage_system.service.FileService;
import com.polyu.comp3334.secure_storage_system.service.UserService;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Scanner;

@Component
public class ConsoleView {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    private Terminal terminal;
    private LineReader lineReader;

    public void start() {
        Scanner scanner = new Scanner(System.in);

        // Initialize JLine Terminal and LineReader
        try {
            terminal = TerminalBuilder.builder().system(true).build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
        } catch (IOException e) {
            System.out.println("Warning: Could not initialize JLine. Passwords will be visible.");
            lineReader = null; // Fallback to Scanner if JLine fails
        }

        headerMenu();
        while (true) {
            displayMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": // Register
                    handleRegister(scanner);
                    break;
                case "2": // Login
                    handleLogin(scanner);
                    break;
                case "3": // Forget Password
                    handleForgetPassword(scanner);
                    break;
                case "4": // Exit
                    handleExit(scanner);
                    return;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }

    private void headerMenu() {
        System.out.println("*******************************************************");
        System.out.println("*          SECURE STORAGE SYSTEM                      *");
        System.out.println("*******************************************************");
    }

    private void displayMenu() {
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Forget Password");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
    }

    private void loginMenu(User user) {
        System.out.println("*******************************************************");
        System.out.println("Logged In Menu: Hi! " + user.getUsername());
        System.out.println("1. Upload File ");
        System.out.println("2. Download File");
        System.out.println("3. Display Accessible Files");
        System.out.println("4. Rename File");
        System.out.println("5. Delete File");
        System.out.println("6. Share File");
        System.out.println("7. Change Password");
        System.out.println("8. Logout");
        System.out.print("Enter your choice: ");
    }

    private String readPasswordWithAsterisks(String prompt) {
        if (lineReader != null) {
            // Use JLine to read password with asterisks
            return lineReader.readLine(prompt, '*');
        } else {
            // Fallback to Scanner if JLine isn’t available
            System.out.print(prompt);
            Scanner scanner = new Scanner(System.in);
            String password = scanner.nextLine();
            System.out.println("********"); // Simulate masking after input
            return password;
        }
    }

    private void handleRegister(Scanner scanner) {
        System.out.println("*******************************************************");
        System.out.println("Please enter the credentials for registering: ");
        String username = "";
        boolean validUsername = false;
        while (!validUsername) {
            System.out.print("Please enter the username: ");
            username = scanner.nextLine();
            if (!userService.usernameExists(username)) {
                validUsername = true; // Exit the loop if the username is valid
            } else {
                System.out.println("The entered username already exists.");
            }
        }
        String password = readPasswordWithAsterisks("Please enter the password: ");
        System.out.print("Please enter the email: ");
        String email = scanner.nextLine();
        userService.registerUser(username, password, email);
        System.out.println("Registration successful! Please log in.");
    }

    private void handleLogin(Scanner scanner) {
        System.out.println("*******************************************************");
        System.out.println("Please enter the credentials for logging in:");
        System.out.print("Please enter your username: ");
        String name = scanner.nextLine();
        String password = readPasswordWithAsterisks("Please enter your password: ");
        var user = userService.userAuthentication(name, password);
        if (user.isPresent()) {
            System.out.println("You have successfully logged into the system.");
            handleAuthorization(user.get());
        } else {
            System.out.println("The credentials are incorrect. Access denied.");
            System.out.println("*******************************************************");
        }
    }

    //Implementation for OTP, not done
    private void handleForgetPassword(Scanner scanner) {
        System.out.println("*******************************************************");
        System.out.println("To change your password, enter your username & E-mail: ");
        System.out.print("Please enter your username: ");
        String name = scanner.nextLine();
        // Add email and new password logic if needed
        System.out.print("Please enter your email: ");
        String email = scanner.nextLine();
        String newPassword = readPasswordWithAsterisks("Please enter your new password: ");
        // Assuming a method to reset password exists
        // userService.resetPassword(name, email, newPassword);
        System.out.println("Password reset requested (feature not fully implemented).");
    }

    private void handleExit(Scanner scanner) {
        System.out.println("Exiting system. Goodbye!");
        scanner.close();
        if (terminal != null) {
            try {
                terminal.close();
            } catch (IOException e) {
                // Ignore closing errors
            }
        }
    }

    private void handleAuthorization(User user){
        userService.recordLogin(user);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            loginMenu(user);
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    handleUpload(scanner, user);
                    break;
                case "2":
                    handleDownload(scanner, user);
                    break;
                case "3":
                    handleDisplayFiles(user);
                    handleDisplaySharedFiles(user);
                    break;
                case "4":
                    handleRenameFiles(scanner, user);
                    break;
                case "5":
                    handleDeleteFiles(scanner, user);
                    break;
                case "6":
                    handleShareFile(scanner, user);
                    break;
                case "7":
                    handleChangePassword(scanner, user);
                    break;
                case "8":
                    handleUserLogout(user);
                    return;
                default:
                    System.out.println("Invalid choice, try again");
            }
        }
    }

    private void handleUpload(Scanner scanner, User owner) {
        try {
            fileService.uploadFile(scanner, owner);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleDownload(Scanner scanner, User owner) {
        try {
            fileService.downloadFile(scanner, owner);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleDisplayFiles(User user){
        Boolean display = fileService.displayAccessibleFiles(user, "display");
    }

    private void handleRenameFiles(Scanner scanner, User owner){
        fileService.renameFile(scanner, owner);
    }

    private void handleDeleteFiles(Scanner scanner, User owner){
        fileService.deleteFile(scanner, owner);
    }

    private void handleShareFile(Scanner scanner, User owner){
        fileService.shareFile(scanner, owner);
    }

    private void handleChangePassword(Scanner scanner, User user){
        userService.changePassword(scanner, user);
    }

    private void handleUserLogout(User user){
        userService.recordLogout(user);
    }

    private void handleDisplaySharedFiles(User owner){
        fileService.displaySharedFiles(owner);
    }
}