package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.FileOtherInfo;

public interface FileOtherInfoRepository extends JpaRepository<FileOtherInfo, Integer>{
	public FileOtherInfo findByFid(int fid);
}
