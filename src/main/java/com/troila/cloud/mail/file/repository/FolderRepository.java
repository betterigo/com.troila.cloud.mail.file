package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.fenum.FolderType;

public interface FolderRepository extends JpaRepository<Folder, Integer>{
	public int deleteByIdAndUid(int id,int uid);
	
	public List<Folder> findByUid(int uid);
	
	public List<Folder> findByType(FolderType type);
	
	public List<Folder> findByTypeAndUid(FolderType type,int uid);
}
