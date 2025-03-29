package com.polyu.comp3334.secure_storage_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "File_Id")
    private Long id;

    @Column(nullable = false)
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

    @ElementCollection
    @Column
    private List<User> sharedWith;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public void setOwner(User owner) {
        this.owner = owner;
    }
    public User getOwner() { return owner; }

    public void setSharedWith(List<User> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public void addSharedWith(User designatedUser) {
        if (sharedWith == null) {
            sharedWith = new ArrayList<>();
        }
        if (!sharedWith.contains(designatedUser)) {
            sharedWith.add(designatedUser);
        }
    }
    public List<User> getSharedWith() {
        return sharedWith;
    }

    public byte[] getSalt() { return salt; }
    public void setSalt(byte[] salt) { this.salt = salt; }

    public byte[] getIv() { return iv; }
    public void setIv(byte[] iv) { this.iv = iv; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    @Override
    public String toString() {
        return "File{" +
                "fileData=" + Arrays.toString(fileData) +
                ", fileName='" + fileName + '\'' +
                ", owner=" + owner +
                ", salt=" + Arrays.toString(salt) +
                ", iv=" + Arrays.toString(iv) +
                ", uploadTime=" + uploadTime +
                ", sharedWith=" + sharedWith +
                '}';
    }
}