package com.polyu.comp3334.secure_storage_system;

import com.polyu.comp3334.secure_storage_system.view.ConsoleView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class SecureStorageSystemApplication {
	@Autowired
	private ConsoleView consoleView;

	public static void main(String[] args) {
		SpringApplication.run(SecureStorageSystemApplication.class, args);
	}
	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> consoleView.start();
	}
};