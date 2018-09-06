package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.UserFolder;

public interface UserFolderRepository extends JpaRepository<UserFolder, Integer>{
	public UserFolder findByUidAndFolderId(int uid,int folderId);
}
