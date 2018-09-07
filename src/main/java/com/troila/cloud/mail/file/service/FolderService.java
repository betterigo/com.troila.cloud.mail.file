package com.troila.cloud.mail.file.service;

import java.util.List;

import com.troila.cloud.mail.file.exception.FolderException;
import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.model.fenum.AccessList;
import com.troila.cloud.mail.file.model.fenum.FolderType;

public interface FolderService {
	public Folder create(UserInfo user, String folderName,int pid,AccessList acl,FolderType folderType) throws FolderException;
	
	public Folder getFolder(UserInfo user,int fid);
	
	public List<Folder> getUserFolders(UserInfo user,int pid);
	
	public boolean deleteFolder(UserInfo user,int fid);
	
	public boolean deleteFolderLogic(UserInfo user,int fid);
	
	public Folder update(Folder folder);
	
}
