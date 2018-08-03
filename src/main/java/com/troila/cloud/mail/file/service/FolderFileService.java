package com.troila.cloud.mail.file.service;

import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.FolderFile;

public interface FolderFileService {
	public FolderFile complateUpload(FileDetailInfo fileDetailInfo);
	
	public int deleteFolderFile(int fileId);
}
