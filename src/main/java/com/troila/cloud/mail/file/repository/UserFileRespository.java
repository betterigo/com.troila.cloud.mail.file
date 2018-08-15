package com.troila.cloud.mail.file.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.troila.cloud.mail.file.model.UserFile;

public interface UserFileRespository extends JpaRepository<UserFile, Integer>{
	@Query("SELECT uf FROM UserFile uf WHERE uid=?1 AND isDeleted=false ORDER BY gmtCreate DESC")
	Page<UserFile> findByUidOrderByGmtCreateDesc(int uid,Pageable pageable);
	
	Page<UserFile> findByUidAndFolderIdOrderByGmtCreateDesc(int uid,int folderId,Pageable pageable);
}
