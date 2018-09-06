package com.troila.cloud.mail.file.service;

import com.troila.cloud.mail.file.model.UserFolder;

public interface UserFolderService {
	public UserFolder getUserFolder(int uid,int folderId);
}
