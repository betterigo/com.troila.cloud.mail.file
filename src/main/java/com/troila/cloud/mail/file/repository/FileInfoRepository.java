package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.FileInfo;

public interface FileInfoRepository extends JpaRepository<FileInfo, Integer>{

	List<FileInfo> findByMd5(String md5);
}
