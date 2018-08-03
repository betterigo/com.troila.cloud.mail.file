package com.troila.cloud.mail.file.service;

import java.io.InputStream;

import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;

public interface FileService {
	
	public FileInfo updateFileInfo(FileInfo fileInfo);
	
	public FileInfo find(String md5);
	
	public FileDetailInfo find(int fid);
	
	public FileInfoExt saveInfoExt(FileDetailInfo fileInfoExt);
	
	public boolean deleteFile(int fid);
	
	public FileDetailInfo uploadPart(InputStream in,int index,FileDetailInfo fileInfo,long size);
	
	public InputStream download(FileDetailInfo fileDetailInfo);
	
	public FileInfoExt findOneFileInfoExt(int fileId);
	
	public FileInfoExt updateFileInfoExt(FileInfoExt fileInfoExt);
}
