package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.FolderFile;

public interface FolderFileRepository extends JpaRepository<FolderFile, Integer>{

}
