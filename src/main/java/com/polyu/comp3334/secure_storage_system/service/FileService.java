package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.File;
import com.polyu.comp3334.secure_storage_system.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    public void uploadFile(String filePath, String filename) throws Exception {
        // Convert the filePath string to a Path object
        Path path = Paths.get(filePath);

        // Check if the file exists
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        // Read the file as bytes
        byte[] fileData = Files.readAllBytes(path);

        // Create a File entity and set its properties
        File fileEntity = new File();
        fileEntity.setFileName(filename);
        fileEntity.setFileData(fileData);

        // Save to the database
        fileRepository.save(fileEntity);

        System.out.println("File uploaded successfully: " + filename);
    }
    public void downloadFile(String filename, String destinationPath) throws Exception {
        // Find the file in the database by filename
        File fileEntity = fileRepository.findById(filename)
                .orElseThrow(() -> new IllegalArgumentException("File not found in database: " + filename));

        // Get the file data
        byte[] fileData = fileEntity.getFileData();
        if (fileData == null) {
            throw new Exception("No data found for file: " + filename);
        }

        // Define the destination path
        Path destination = Paths.get(destinationPath, filename);

        // Write the file data to the destination
        Files.write(destination, fileData);
        System.out.println("File downloaded successfully to: " + destination);
    }
}



