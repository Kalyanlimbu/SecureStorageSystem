package com.polyu.comp3334.secure_storage_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "files")
public class File {
    @Id
    @Column(nullable = false, unique = true)
    private String fileName;

    //Foreign Key
    @ManyToOne
    @JoinColumn(name = "owner", nullable = false)
    private User owner;

    @Lob
    private byte[] fileData;

    @Column(nullable = false)
    private byte[] salt; // For key derivation

    @Column(nullable = false)
    private byte[] iv;   // Initialization vector for encryption

    private LocalDateTime uploadTime;

    //@ElementCollection(fetch = FetchType.EAGER)
    //@CollectionTable(name = "file_shared_with", joinColumns = @JoinColumn(name = "file_id"))
    @Column
    private List<String> sharedWith;

    // Constructors
    public File() {}

    public File(String fileName, byte[] fileData, User owner, byte[] salt, byte[] iv) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.owner = owner;
        this.salt = salt;
        this.iv = iv;
        this.uploadTime = LocalDateTime.now();
        this.sharedWith = new ArrayList<>();
    }

    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public void setOwner(User owner) {
        this.owner = owner;
    }
    public User getOwner() { return owner; }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public void addSharedWith(String designatedUsername) {
        if (sharedWith == null) {
            sharedWith = new ArrayList<>();
        }
        if (!sharedWith.contains(designatedUsername)) {
            sharedWith.add(designatedUsername);
        }
    }
    public List<String> getSharedWith() {
        return sharedWith;
    }

    public byte[] getSalt() { return salt; }
    public void setSalt(byte[] salt) { this.salt = salt; }

    public byte[] getIv() { return iv; }
    public void setIv(byte[] iv) { this.iv = iv; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
}