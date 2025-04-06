package com.polyu.comp3334.secure_storage_system.controller;

import com.polyu.comp3334.secure_storage_system.model.File;
import com.polyu.comp3334.secure_storage_system.repository.FileRepository;
import com.polyu.comp3334.secure_storage_system.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    private ResponseEntity<String> uploadFile(@RequestBody byte[] payload) {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(payload);
            // Read username
            int usernameLen = bytesToInt(readBytes(input, 4));
            String username = new String(readBytes(input, usernameLen), StandardCharsets.UTF_8);
            // Read filename
            int filenameLen = bytesToInt(readBytes(input, 4));
            String filename = new String(readBytes(input, filenameLen), StandardCharsets.UTF_8);
            // Read salt (fixed 16 bytes)
            byte[] salt = readBytes(input, 16);
            // Read iv (fixed 16 bytes)
            byte[] iv = readBytes(input, 16);
            // Read encryptedData
            int encryptedDataLen = bytesToInt(readBytes(input, 4));
            byte[] encryptedData = readBytes(input, encryptedDataLen);

            fileService.uploadFile(username, filename, encryptedData, salt, iv);
            return ResponseEntity.ok(filename + " has been successfully uploaded.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading file: " + e.getMessage());
        }
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam("filename") String filename,
            @RequestParam("username") String username) {
        try {
            File file = fileService.downloadFile(username, filename);
            if (file == null) {
                return ResponseEntity.badRequest().body("File not found".getBytes(StandardCharsets.UTF_8));
            }
            // Construct payload: [salt][iv][encryptedDataLen][encryptedData]
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            payload.write(file.getSalt()); // 16 bytes
            payload.write(file.getIv());   // 16 bytes
            byte[] encryptedData = file.getFileData();
            payload.write(intToBytes(encryptedData.length));
            payload.write(encryptedData);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(payload.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(("Error downloading file: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/displayFiles")
    private ResponseEntity<List<String>> displayFiles(@RequestParam("username") String username) {
        try {
            List<String> fileNames= fileService.displayAccessibleFiles(username);
            if (fileNames.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList()); // Return 200 OK with empty list
            }
            return ResponseEntity.ok(fileNames); // Return 200 OK with list of files
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList()); // Return 400 with empty list for invalid input
        }
    }

    @GetMapping("/displaySharedFiles")
    private ResponseEntity<HashMap<String, String>> displaySharedFiles(@RequestParam("username") String username) {
        try {
            HashMap<String, String> sharedFiles = fileService.getSharedFileNameAndOwnerName(username);
            if (sharedFiles.isEmpty()) {
                HashMap<String, String> errorResponse = new HashMap<>();
                errorResponse.put("No shared files, ", "You do not have any shared files by other users.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            return ResponseEntity.ok(sharedFiles);
        } catch (IllegalArgumentException e) {
            HashMap<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid username provided.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/checkFilenameForUser")
    private ResponseEntity<String> checkFileName(
            @RequestParam String filename,
            @RequestParam String username) {
        boolean exist = fileService.fileNameExistByOwner(username, filename);
        if(exist){
            return ResponseEntity.badRequest().body("Filename already exist.");
        }else{
            return ResponseEntity.ok("Filename does not exist.");
        }
    }

    @DeleteMapping("/delete")
    private ResponseEntity<String> deleteFile(
        @RequestParam String filename,
        @RequestParam String username){
        try{
            fileService.deleteFile(username, filename);
            return ResponseEntity.ok(filename + " has been successfully deleted.");
        }catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/rename")
    private ResponseEntity<String> renameFile(
        @RequestParam String oldFilename,
        @RequestParam String newFilename,
        @RequestParam String username){
        try{
            fileService.changeFilename(oldFilename, newFilename, username);
            return ResponseEntity.ok("The file has been successfully renamed as " + newFilename);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sharingFile")
    private ResponseEntity<String>shareFile(
            @RequestParam String username,
            @RequestParam String filename,
            @RequestParam String designatedUserName){
        try{
            fileService.shareFile(username, filename, designatedUserName);
            return ResponseEntity.ok(filename + " has been successfully shared to " + designatedUserName);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Helper method to read exact number of bytes
    private byte[] readBytes(ByteArrayInputStream input, int length) throws IOException {
        byte[] bytes = new byte[length];
        int read = input.read(bytes);
        if (read != length) {
            throw new IOException("Incomplete data read: expected " + length + ", got " + read);
        }
        return bytes;
    }

    // Helper method to convert 4 bytes to int
    private int bytesToInt(byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[2] & 0xFF) << 8  |
                (bytes[3] & 0xFF);
    }

    private byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

}

