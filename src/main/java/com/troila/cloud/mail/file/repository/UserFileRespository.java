package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.troila.cloud.mail.file.model.UserFile;

public interface UserFileRespository extends JpaRepository<UserFile, Integer>{
	@Query("SELECT uf FROM UserFile uf WHERE uid=?1 AND isDeleted=false ORDER BY gmtCreate DESC")
	Page<UserFile> findByUidOrderByGmtCreateDesc(int uid,Pageable pageable);
	
	Page<UserFile> findByUidAndFolderIdOrderByGmtCreateDesc(int uid,int folderId,Pageable pageable);
	
	@Query(value="select * from v_user_file where now() >= DATE_SUB(gmt_expired, INTERVAL ?1 DAY) and now() < gmt_expired and is_deleted = 0 and status = 'SUCCESS' and uid = ?2 order by original_file_name", nativeQuery = true)
	List<UserFile> findExpireBefores(int expireBeforeDays, int uid);
	
	@Query(value="select * from v_user_file where now() < DATE_SUB(gmt_expired, INTERVAL ?1 DAY) and is_deleted = 0 and status = 'SUCCESS' and uid = ?2 order by original_file_name", nativeQuery = true)
	List<UserFile> findExpireBeforesBefore(int expireBeforeDays, int uid);
}
