package com.polyu.comp3334.secure_storage_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class File {

    @Id
    private String fileName;

    @Lob
    private byte[] fileData;

    public File(){}

    public File(byte[] fileData, String fileName) {
        this.fileData = fileData;
        this.fileName = fileName;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

}