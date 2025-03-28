package com.polyu.comp3334.secure_storage_system.service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import com.polyu.comp3334.secure_storage_system.model.*;
import com.polyu.comp3334.secure_storage_system.repository.FileRepository;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    // Upload file with encryption
    @Transactional
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
        String fileName;
        while(true){
            System.out.print("Please enter the file name: ");
            fileName = scanner.nextLine();
            if(fileRepository.findByFileName(fileName) == null) break;
            System.out.println("The file name already exists. Please enter another file name.");
        }
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
    @Transactional
    public void downloadFile(Scanner scanner, User owner) throws Exception {
        if (!displayAccessibleFiles(owner, "download")) {
            return;
        }
        displaySharedFiles(owner);
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
        SecretKey secretKey = generateKeyFromPassword(file.getOwner().getPassword(), file.getSalt());
        // 4. Decrypt the file
        byte[] decryptedData = decrypt(file.getFileData(), secretKey, file.getIv());
        // 5. Save to destination
        Files.write(destinationPath, decryptedData);
        System.out.println("File successfully downloaded to: " + destinationPath);
    }

    //Returns all the files under owner
    @Transactional
    public List<File> getAllFilesByOwner(User owner) {
        return fileRepository.findByOwner(owner);
    }

    //Displays all the files of owner
    @Transactional
    public Boolean displayAccessibleFiles(User owner, String situation){
        System.out.println("*******************************************************");
        List<File> files = getAllFilesByOwner(owner);
        if(files.isEmpty()){
            System.out.println("You have not uploaded any files.");
            if(situation.equals("download")){
                System.out.println("Hence, you cannot download any file.");
            }
            return false;
        }
        else {
            if(situation.equals("download")){
                System.out.println("Here is the list of files you can download that you have uploaded in the past:");
            }else if(situation.equals("sharingFiles")){
                System.out.println("Here is the list of files you can share.");
            }
            else {
                System.out.println("Here is the list of files you have access to: ");
            }
            int i = 1;
            for(File f:files){
                System.out.println(i++ + ". " + f.getFileName());
            }
        }
        return true;
    }
    @Transactional
    public void displaySharedFiles(User designatedUser){
        List<File> sharedFiles = getSharedFilesWithUser(designatedUser);
        if(sharedFiles.isEmpty()){
            return;
        }
        System.out.println("However, here is the list of other owners' files that you have reading access to.");
        int j = 1;
        for(File file:sharedFiles){
            System.out.println(j++ + ". " + file.getFileName());
        }
    }

    @Transactional
    public void renameFile(Scanner scanner, User user) {
        List<File> files = getAllFilesByOwner(user);
        if (files.isEmpty()) {
            System.out.println("You do not have access to any files. Hence you cannot rename anything.");
            return;
        }

        System.out.println("Here are the list of files you have access to: ");
        int i = 1;
        for (File f : files) {
            System.out.println(i++ + ". " + f.getFileName());
        }

        File oldFile;
        String filename;
        while (true) {
            System.out.print("Enter the file name you want to rename: ");
            filename = scanner.nextLine();
            oldFile = fileRepository.findByFileName(filename);
            if (oldFile != null) {
                break;
            }
            System.out.println("The file does not exist. Please enter a valid filename.");
        }

        String newFileName;
        while (true) {
            System.out.print("Enter the new name for the file: ");
            newFileName = scanner.nextLine();
            if (fileRepository.findByFileName(newFileName) == null) {
                break;
            }
            System.out.println("The file name already exists. Please enter another file name.");
        }
        changeFilename(newFileName, oldFile);
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

    @Transactional
    private void changeFilename(String newFileName, File oldFile){
        var newFile = new File();
        newFile.setFileData(oldFile.getFileData());
        newFile.setFileName(newFileName);
        newFile.setSalt(oldFile.getSalt());
        newFile.setIv(oldFile.getIv());
        newFile.setOwner(oldFile.getOwner());
        fileRepository.delete(oldFile);
        fileRepository.save(newFile);
        System.out.println("The file " + oldFile.getFileName() + " has been successfully renamed into " + newFile.getFileName() + ".");
    }

    @Transactional
    public List<File> getSharedFilesWithUser(User designatedUser) {
        return fileRepository.findBySharedWithContaining(designatedUser);
    }

    @Transactional
    public void shareFile(Scanner scanner, User owner){
        Boolean shareAuthorization = displayAccessibleFiles(owner, "sharingFiles");
        if(!shareAuthorization){
            System.out.println("Hence you are unable to share files.");
            return;
        }
        String fileNameToShare;
        File fileToShare;
        while(true){
            System.out.print("Please enter the file name you want to share and the file name should be in the list: ");
            fileNameToShare = scanner.nextLine();
            fileToShare = fileRepository.findByFileName(fileNameToShare);
            if(fileToShare != null) break;
            System.out.println("The file does not exist. Please enter a valid filename.");
        }
        String designatedUsername;
        User designatedUser;
        while(true){
            System.out.print("Please enter the username of the designated user with whom you would like to share your file: ");
            designatedUsername = scanner.nextLine();
            designatedUser = userRepository.findByUsername(designatedUsername);
            if(designatedUser != null) break;
            System.out.println("The username does not exists. Please enter a valid username.");
        }
        fileToShare.addSharedWith(designatedUser);
        fileRepository.save(fileToShare);
        System.out.println("The file " + fileToShare.getFileName() + " has been successfully shared to " + designatedUsername);
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
