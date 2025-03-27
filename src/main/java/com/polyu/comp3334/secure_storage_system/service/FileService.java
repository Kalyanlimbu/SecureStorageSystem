package com.polyu.comp3334.secure_storage_system.service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import com.polyu.comp3334.secure_storage_system.model.*;
import com.polyu.comp3334.secure_storage_system.repository.FileRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

@Service
public class FileService {
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;

    @Autowired
    private FileRepository fileRepository;

    // Upload file with encryption
    public void uploadFile(Scanner scanner, User owner) throws Exception {
        String filePath;
        Path path = null;
        boolean validPath = false;
        while (!validPath) {
            System.out.print("Please enter the file path: ");
            filePath = scanner.nextLine();
            path = Paths.get(filePath);
            if (Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path)) {
                validPath = true; // Exit the loop if the path is valid
            } else {
                if (!Files.exists(path)) {
                    System.out.println("File not found at path: " + filePath);
                } else if (!Files.isRegularFile(path)) {
                    System.out.println("Path does not point to a regular file: " + filePath);
                } else if (!Files.isReadable(path)) {
                    System.out.println("File is not readable: " + filePath);
                }
                System.out.println("Please enter a valid file path: ");
            }
        }
        // Get the file name
        System.out.print("Please enter the file name: ");
        String fileName = scanner.nextLine();
        // Read file bytes and proceed with encryption and storage
        byte[] fileData = Files.readAllBytes(path);
        byte[] salt = generateRandomBytes(16);
        byte[] iv = generateRandomBytes(16);
        SecretKey secretKey = generateKeyFromPassword(owner.getPassword(), salt);
        byte[] encryptedData = encrypt(fileData, secretKey, iv);
        File file = new File(fileName, encryptedData, owner, salt, iv);
        fileRepository.save(file);
        System.out.println("The file " + fileName + " has been successfully uploaded.");
    }

    // Download and decrypt file
    public void downloadFile(Scanner scanner, User owner) throws Exception {
        if (!displayAccessibleFiles(owner, "download")) {
            return;
        }
        // File selection loop
        File file = null;
        while (true) {
            System.out.print("Enter filename to download: ");
            String fileName = scanner.nextLine();
            file = fileRepository.findByFileName(fileName);

            if (file != null) break;
            System.out.println("File '" + fileName + "' not found. Please enter a valid file name.");
        }
        // Destination path validation loop
        Path destinationDir;
        while (true) {
            System.out.print("Enter destination directory path: ");
            String destPath = scanner.nextLine();
            destinationDir = Paths.get(destPath);

            if (Files.isDirectory(destinationDir) && Files.isWritable(destinationDir)) break;
            System.out.println("Invalid path. Must be an existing writable directory.");
        }
        // Construct full path and check for existing file
        Path destinationPath = destinationDir.resolve(file.getFileName());
        if (Files.exists(destinationPath)) {
            System.out.print("File already exists. Do you want to overwrite the existing file? (yes/no): ");
            String response = scanner.nextLine();
            if (!response.equalsIgnoreCase("yes") && !response.equalsIgnoreCase("y")) {
                System.out.println("Download cancelled.");
                return;
            }
        }
        // 3. Recreate encryption key
        SecretKey secretKey = generateKeyFromPassword(owner.getPassword(), file.getSalt());
        // 4. Decrypt the file
        byte[] decryptedData = decrypt(file.getFileData(), secretKey, file.getIv());
        // 5. Save to destination
        Files.write(destinationPath, decryptedData);
        System.out.println("File successfully downloaded to: " + destinationPath);
    }

    //Returns all the files under owner
    public List<File> getAllFilesByOwner(User owner) {
        return fileRepository.findByOwner(owner);
    }

    //Displays all the files of owner
    public Boolean displayAccessibleFiles(User owner, String situation){
        List<File> files = getAllFilesByOwner(owner);
        if(!(files.isEmpty())){
            if(situation.equals("download")){
                System.out.println("Here are the list of files you can download.");
            }else {
                System.out.println("Here are the list of files you have access to: ");
            }
            int i = 1;
            for(File f:files){
                System.out.println(i++ + ". " + f.getFileName());
            }
            return true;
        }else{
            System.out.println("You do not have access to any files.");
            if(situation.equals("download")){
                System.out.println("Hence, you cannot download any file.");
            }
            return false;
        }
    }

    @Transactional
    public void changeFileName(Scanner scanner, User user){
        List<File> files = getAllFilesByOwner(user);
        if(!(files.isEmpty())){
            System.out.println("Here are the list of files you have access to: ");
            int i = 1;
            for(File f:files){
                System.out.println(i++ + ". " + f.getFileName());
            }
            System.out.print("Enter the filename you want to rename: ");
            String filename = scanner.nextLine();
            File file = fileRepository.findByFileName(filename);
            while(file == null){
                System.out.print("The file does not exist. Please enter a valid filename: ");
                filename = scanner.nextLine();
                file = fileRepository.findByFileName(filename);
            }
            System.out.print("Enter the new name for the file: ");
            String newFileName = scanner.nextLine();
            file.setFileName(newFileName);
            fileRepository.save(file);
        }else{
            System.out.println("You do not have access to any files. Hence you cannot rename anything.");
        }
    }

    @Transactional
    public void deleteFile(Scanner scanner, User owner){
        List<File> files = getAllFilesByOwner(owner);
        if(!(files.isEmpty())){
            System.out.println("Here are the list of files you have access to: ");
            int i = 1;
            for(File f:files){
                System.out.println(i++ + ". " + f.getFileName());
            }
            System.out.print("Enter the filename you want to delete: ");
            String filename = scanner.nextLine();
            File file = fileRepository.findByFileName(filename);
            while(file == null){
                System.out.print("The file does not exist. Please enter a valid filename: ");
                filename = scanner.nextLine();
                file = fileRepository.findByFileName(filename);
            }
            fileRepository.delete(file);
            System.out.println("The file has been successfully deleted.");
        }else{
            System.out.println("You do not have access to any files. Hence you cannot delete anything.");
        }
    }

    // Helper method to generate encryption key
    private SecretKey generateKeyFromPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    // Helper method for encryption
    private byte[] encrypt(byte[] data, SecretKey key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    // Helper method for decryption
    private byte[] decrypt(byte[] encryptedData, SecretKey key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(encryptedData);
    }

    // Helper to generate random bytes
    private byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
