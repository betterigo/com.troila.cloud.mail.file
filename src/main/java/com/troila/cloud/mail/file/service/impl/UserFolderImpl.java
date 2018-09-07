package com.troila.cloud.mail.file.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.model.UserFolderDetail;
import com.troila.cloud.mail.file.repository.UserFolderDetailRepository;
import com.troila.cloud.mail.file.repository.UserFolderRepository;
import com.troila.cloud.mail.file.service.UserFolderService;

@Service
public class UserFolderImpl implements UserFolderService{

	@Autowired
	private UserFolderRepository userFolderRepository;
	
	@Autowired
	private UserFolderDetailRepository userFolderDetailRepository;
	
	@Override
	public UserFolder getUserFolder(int uid, int folderId) {
		return userFolderRepository.findByUidAndFolderId(uid, folderId).get();
	}

	@Override
	public List<UserFolderDetail> listMyFolders(int uid,int pid) {
		List<UserFolderDetail> folders = userFolderDetailRepository.findByUidAndPid(uid, pid);
		return folders;
	}

}
