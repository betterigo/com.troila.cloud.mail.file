package com.troila.cloud.mail.file.service;

import org.springframework.data.domain.Page;

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
}	
