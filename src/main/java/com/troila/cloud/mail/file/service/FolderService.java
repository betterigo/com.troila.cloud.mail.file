package com.troila.cloud.mail.file.service;

import java.util.List;

import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.User;

public interface FolderService {
	public Folder create(User user, String folderName,int pid);
	
	public Folder getFolder(User user,int fid);
	
	public List<Folder> getUserFolders(User user);
	
	public boolean deleteFolder(User user,int fid);
	
	public boolean deleteFolderLogic(User user,int fid);
	
	public Folder update(Folder folder);
	
}
