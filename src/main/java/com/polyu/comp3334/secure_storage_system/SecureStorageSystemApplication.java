package com.polyu.comp3334.secure_storage_system;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
//import com.polyu.comp3334.secure_storage_system.view.ConsoleView;
import com.polyu.comp3334.secure_storage_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SecureStorageSystemApplication {
	@Autowired
	private UserService userService;
	public static void main(String[] args) {
		SpringApplication.run(SecureStorageSystemApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(UserService userService) {
		return args -> {
			userService.adminCreation();
		};
	}
}