package com.polyu.comp3334.secure_storage_system.repository;

import com.polyu.comp3334.secure_storage_system.model.File;
import com.polyu.comp3334.secure_storage_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, String> {
    File findByFileName(String fileName);
    List<File> findByOwner(User owner);
}
