package com.troila.cloud.mail.file.service;

import java.util.List;

import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.UserInfo;

public interface FolderService {
	public Folder create(UserInfo user, String folderName,int pid);
	
	public Folder getFolder(UserInfo user,int fid);
	
	public List<Folder> getUserFolders(UserInfo user);
	
	public boolean deleteFolder(UserInfo user,int fid);
	
	public boolean deleteFolderLogic(UserInfo user,int fid);
	
	public Folder update(Folder folder);
	
}
