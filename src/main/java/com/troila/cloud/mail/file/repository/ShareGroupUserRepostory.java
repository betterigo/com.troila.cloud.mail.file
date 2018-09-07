package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.ShareGroupUser;

public interface ShareGroupUserRepostory extends JpaRepository<ShareGroupUser, Integer> {
	public List<ShareGroupUser> findByUid(int uid);
	
	public List<ShareGroupUser> findByGid(int gid);
	
	public List<ShareGroupUser> findByUidAndGid(int uid,int gid);
}
