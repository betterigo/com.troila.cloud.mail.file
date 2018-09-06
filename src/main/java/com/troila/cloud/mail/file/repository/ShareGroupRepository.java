package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.ShareGroup;

public interface ShareGroupRepository extends JpaRepository<ShareGroup, Integer>{
	public List<ShareGroup> findByOwner(int owner);
}
