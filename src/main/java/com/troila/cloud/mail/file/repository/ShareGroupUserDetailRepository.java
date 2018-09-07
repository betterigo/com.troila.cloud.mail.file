package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.ShareGroupUserDetail;

public interface ShareGroupUserDetailRepository extends JpaRepository<ShareGroupUserDetail, Integer>{
	public List<ShareGroupUserDetail> findByGid(int gid);
}
