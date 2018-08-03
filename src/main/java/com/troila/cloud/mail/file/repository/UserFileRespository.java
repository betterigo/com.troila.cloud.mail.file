package com.troila.cloud.mail.file.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.UserFile;

public interface UserFileRespository extends JpaRepository<UserFile, Integer>{
	Page<UserFile> findByUid(int uid,Pageable pageable);
}
