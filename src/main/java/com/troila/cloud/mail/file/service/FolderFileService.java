package com.troila.cloud.mail.file.service;

import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FolderFile;

public interface FolderFileService {
	/**
	 * 完成上传需要调用次方法，用来在数据库中保存文件信息
	 * @param fileDetailInfo
	 * @return
	 */
	public FolderFile complateUpload(FileDetailInfo fileDetailInfo);
	
	/**
	 * 从数据库中删除folderFile
	 * @param fileId
	 * @return
	 */
	public int deleteFolderFile(int fileId);
	
	/**
	 * 更新folderFile
	 * @param folderFile
	 * @return
	 */
	public FolderFile updateFolderFile(int uid,FolderFile folderFile);
	
	/**
	 * 逻辑上删除folderFile
	 * @param fileId
	 * @return
	 */
	public boolean deleteFolderFileLogic(int uid,int fileId);
	
	public FolderFile findOne(int id);
}
