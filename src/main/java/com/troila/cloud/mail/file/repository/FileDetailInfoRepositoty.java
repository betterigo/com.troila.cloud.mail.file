package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.FileDetailInfo;

public interface FileDetailInfoRepositoty extends JpaRepository<FileDetailInfo, Integer>{
	
	public FileDetailInfo findById(int id);

}
