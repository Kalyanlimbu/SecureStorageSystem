package com.polyu.comp3334.secure_storage_system.service;

import com.polyu.comp3334.secure_storage_system.model.*;
import com.polyu.comp3334.secure_storage_system.repository.FileRepository;
import com.polyu.comp3334.secure_storage_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public void uploadFile(String ownerName, String filename, byte[] encryptedData, byte[] salt, byte[] iv){
        User owner = userRepository.findByUsername(ownerName);
        File file = new File(filename, encryptedData, owner, salt, iv);
        fileRepository.save(file);
    }

    // Check filename existence according to owner
    @Transactional
    public boolean fileNameExistByOwner(String ownerName, String filename){
        User owner = userRepository.findByUsername(ownerName);
        return fileRepository.existsByFileNameAndOwner(filename, owner);
    }

    @Transactional
    public File downloadFile(String ownerName, String filename){
        User owner = userRepository.findByUsername(ownerName);
        return fileRepository.findByFileNameAndOwner(filename, owner);
    }

    //Returns all the files under owner
    @Transactional
    public List<File> getAllFilesByOwner(User owner) {
        return fileRepository.findByOwner(owner);
    }
    @Transactional
    public List<String> displayAccessibleFiles(String ownerName){
        User owner = userRepository.findByUsername(ownerName);
        List<File> files = getAllFilesByOwner(owner);
        List<String> fileNames = new ArrayList<>();
        for(File file:files){
            fileNames.add(file.getFileName());
        }
        return fileNames;
    }

//    @Transactional
//    public List<String> displaySharedFiles(String ownerName){
//        User designatedUser = userRepository.findByUsername(ownerName);
//        List<File> sharedFiles = getSharedFilesWithUser(designatedUser);
//        List<String> sharedFileNames = new ArrayList<>();
//        if(sharedFiles.isEmpty()){
//            return sharedFileNames;
//        }
//        for(File file:sharedFiles){
//            sharedFileNames.add(file.getFileName());
//        }
//        return sharedFileNames;
//    }

    @Transactional
    public void deleteFile(String username, String filename){
        User owner = userRepository.findByUsername(username);
        File file = fileRepository.findByFileNameAndOwner(filename, owner);
        fileRepository.delete(file);
    }

    @Transactional
    public void changeFilename(String oldFileName, String newFileName, String username){
        var owner = userRepository.findByUsername(username);
        var file = fileRepository.findByFileNameAndOwner(oldFileName, owner);
        file.setFileName(newFileName);
        fileRepository.save(file);
    }

//    @Transactional
//    public List<File> getSharedFilesWithUser(User designatedUser) {
//        return fileRepository.findBySharedWithContaining(designatedUser);
//    }

//    @Transactional
//    public void shareFile(Scanner scanner, User owner){
//        Boolean shareAuthorization = displayAccessibleFiles(owner.getUsername(), "sharingFiles");
//        if(!shareAuthorization){
//            System.out.println("Hence you are unable to share files.");
//            return;
//        }
//        String fileNameToShare;
//        File fileToShare;
//        while(true){
//            System.out.print("Please enter the file name you want to share and the file name should be in the list: ");
//            fileNameToShare = scanner.nextLine();
//            fileToShare = fileRepository.findByFileName(fileNameToShare);
//            if(fileToShare != null) break;
//            System.out.println("The file does not exist. Please enter a valid filename.");
//        }
//        String designatedUsername;
//        User designatedUser;
//        while(true){
//            System.out.print("Please enter the username of the designated user with whom you would like to share your file: ");
//            designatedUsername = scanner.nextLine();
//            designatedUser = userRepository.findByUsername(designatedUsername);
//            if(designatedUser != null) break;
//            System.out.println("The username does not exists. Please enter a valid username.");
//        }
//        fileToShare.addSharedWith(designatedUser);
//        fileRepository.save(fileToShare);
//        System.out.println("The file " + fileToShare.getFileName() + " has been successfully shared to " + designatedUsername);
//    }


}
