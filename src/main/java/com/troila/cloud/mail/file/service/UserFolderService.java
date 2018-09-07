package com.troila.cloud.mail.file.service;

import java.util.List;

import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.model.UserFolderDetail;

public interface UserFolderService {
	public UserFolder getUserFolder(int uid,int folderId);
	
	public List<UserFolderDetail> listMyFolders(int uid,int pid);
}
