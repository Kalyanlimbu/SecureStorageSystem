package com.polyu.comp3334.secure_storage_system;

import com.polyu.comp3334.secure_storage_system.controller.UserController;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import com.polyu.comp3334.secure_storage_system.service.FileService;
import com.polyu.comp3334.secure_storage_system.service.UserService;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class SecureStorageSystemApplication {
	@Autowired
	private UserService service;

	@Autowired
	private FileService fileService;

	public static void main(String[] args) {
		SpringApplication.run(SecureStorageSystemApplication.class, args);
	}
	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			Scanner scanner = new Scanner(System.in);
			while (true) {
				System.out.println("1. Register 2. Login 3. Upload  4. Download ...");
				String choice = scanner.nextLine();
				// Handle choices (you’ll add logic here later)
				switch (choice) {
					case "1":
						System.out.println("Please enter the credentials");
						System.out.print("Please enter the ID: ");
						Long id = scanner.nextLong();
						scanner.nextLine(); // Consume the newline character
						System.out.print("Please enter the username: ");
						String name = scanner.nextLine();
						while(service.usernameExists(name)){
							System.out.println("The entered username is already in existence.");
							System.out.println("Please enter another username: ");
							name = scanner.nextLine();
						}
						System.out.print("Please enter the password: ");
						String password = scanner.nextLine();
						System.out.print("Please enter the gmail: ");
						String gmail = scanner.nextLine();
						service.registerUser(id, name, password, gmail);
						System.out.print("Since you have registered, please log in: ");
						break;
					case "2":
						System.out.println("Please enter the correct credentials for logging in:");
						System.out.println("Please enter your username: ");
						name = scanner.nextLine();
						System.out.println("Please enter your password: ");
						password = scanner.nextLine();
						boolean check = service.loginUser(name, password);
						if(service.loginUser(name, password)){
							System.out.println("You have successfully logged into the system.");
							System.out.println("Here is you account details: " + " ");
						}else{
							System.out.println("The credentials are incorrect. Hence, you cannot acces sthe system.");
						}
						break;
					case "3":
						System.out.println("Please enter the correct file path and file name to upload the file: ");
						System.out.println("Please enter the file path:");
						String path = scanner.nextLine();
						System.out.println("Please enter the file name:");
						name = scanner.nextLine();
						fileService.uploadFile(path, name);
						break;
					case "4":
						System.out.println("You are here to download the file by giving the filename you want to download and the destination path where you want it to be downloaded.");
						System.out.println("Please enter the file path:");
						path = scanner.nextLine();
						System.out.println("Please enter the file name:");
						name = scanner.nextLine();
						fileService.downloadFile(name, path);
						break;
					default:
						System.out.println("Invalid choice, try again");
				}
			}
		};
	}
}