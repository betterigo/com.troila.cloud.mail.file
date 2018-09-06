package com.troila.cloud.mail.file.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.repository.UserFolderRepository;
import com.troila.cloud.mail.file.service.UserFolderService;

@Service
public class UserFolderImpl implements UserFolderService{

	@Autowired
	private UserFolderRepository userFolderRepository;
	
	@Override
	public UserFolder getUserFolder(int uid, int folderId) {
		return userFolderRepository.findByUidAndFolderId(uid, folderId);
	}

}
