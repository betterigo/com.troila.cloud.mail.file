package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.Folder;

public interface FolderRepository extends JpaRepository<Folder, Integer>{

}
