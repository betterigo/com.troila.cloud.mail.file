package com.troila.cloud.mail.file.service;

import java.io.File;
import java.io.InputStream;

import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FileInfo;
import com.troila.cloud.mail.file.model.FileInfoExt;
import com.troila.cloud.mail.file.model.ProgressInfo;

public interface FileService {
	
	public FileInfo upload(File file);
	
	public FileInfo upload(InputStream in,FileInfo fileInfo);
	
	public FileInfo updateFileInfo(FileInfo fileInfo);
	
	public FileInfo find(String md5);
	
	public FileDetailInfo find(int fid);
	
	public FileInfoExt saveInfoExt(FileInfoExt fileInfoExt);
	
	public boolean deleteFile(int fid);
	
	public ProgressInfo uploadPart(InputStream in,int index,FileDetailInfo fileInfo,long size);
	
	public InputStream download(FileDetailInfo fileDetailInfo);
}
