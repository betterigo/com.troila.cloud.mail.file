package com.troila.cloud.mail.file.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.troila.cloud.mail.file.model.ExpireBeforeUserFile;
import com.troila.cloud.mail.file.model.UserFile;

/**
 * 关于用户文件操作的接口
 * @author haodonglei
 *
 */
public interface UserFileService {
	/**
	 * 获取用户的所有文件
	 * @param uid
	 * @return
	 */
	Page<UserFile> findAll(int uid,int page,int size);
	
	/**
	 * 根据条件分页查询用户文件
	 * @param example
	 * @param page
	 * @param size
	 * @return
	 */
	Page<UserFile> search(UserFile example,int page,int size);
	
	/**
	 * 获取用户某个文件夹中的文件
	 * @param userid
	 * @param fid
	 * @param page
	 * @param size
	 * @return
	 */
	Page<UserFile> findByFolderId(int userid,int fid,int page,int size);
	
	/**
	 * 获取一个文件信息
	 * @param uid
	 * @param folderId
	 * @return
	 */
	UserFile findOne(int uid,int id);
	
	/**
	 * 获取一个可以公共访问的文件
	 * @param id
	 * @return
	 */
	UserFile findOnePublic(int id);

	
	/**
	 * 获取即将到期大附件列表
	 * @param expireBeforeDays
	 * @return
	 */
	ExpireBeforeUserFile findExpireBefores(int expireBeforeDays, int uid);
	
	List<UserFile> findExpiredFiles(int days);
}	
