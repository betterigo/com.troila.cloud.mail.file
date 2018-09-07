package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.UserFolderDetail;

public interface UserFolderDetailRepository extends JpaRepository<UserFolderDetail, Integer>{
	
	public List<UserFolderDetail> findByUidAndPid(int uid,int pid);
	
}
