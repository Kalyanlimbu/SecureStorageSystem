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

    @Transactional
    public HashMap<String, String> getSharedFileNameAndOwnerName(String designatedUserName) {
        User designatedUser = userRepository.findByUsername(designatedUserName);
        List<File> files = fileRepository.findBySharedWithContaining(designatedUser);
        HashMap<String, String> sharedFileInfo = new HashMap<>();
        if (files != null && !files.isEmpty()) {  // Changed from just null check
            for (File file : files) {
                sharedFileInfo.put(file.getFileName(), file.getOwner().getUsername());
            }
        }
        return sharedFileInfo;
    }

    //    @Transactional
//    public List<File> getSharedFilesWithUser(User designatedUser) {
//        return fileRepository.findBySharedWithContaining(designatedUser);
//    }

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

    @Transactional
    public void shareFile(String ownerName, String fileToBeShared, String designatedUserName){
        User owner = userRepository.findByUsername(ownerName);
        File file = fileRepository.findByFileNameAndOwner(fileToBeShared, owner);
        User designatedUser = userRepository.findByUsername(designatedUserName);
        file.addSharedWith(designatedUser);
        fileRepository.save(file);
    }


}
