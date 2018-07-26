package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.FileInfoExt;

public interface FileInfoExtRepository extends JpaRepository<FileInfoExt, Integer> {

}
