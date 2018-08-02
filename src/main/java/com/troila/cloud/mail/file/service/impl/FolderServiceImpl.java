package com.troila.cloud.mail.file.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.model.fenum.FolderType;
import com.troila.cloud.mail.file.repository.FolderRepository;
import com.troila.cloud.mail.file.service.FolderService;

@Service
public class FolderServiceImpl implements FolderService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FolderRepository folderRepository;
	
	@Override
	public Folder create(User user, String folderName, int pid) {
		Folder folder = new Folder();
		folder.setGmtCreate(new Date());
		folder.setName(folderName);
		folder.setUid(user.getId());
		folder.setPid(pid);
		if(folder.getType() == null) {			
			folder.setType(FolderType.CUSTOM);
		}
		Folder result = folderRepository.save(folder);
		return result;
	}

	@Override
	public Folder getFolder(User user, int fid) {
		Folder result = folderRepository.getOne(fid);
		if(user.getId() != result.getUid()) {
			return null;
		}else {
			return result;
		}
	}

	@Override
	public List<Folder> getUserFolders(User user) {
		List<Folder> folders = folderRepository.findByUid(user.getId());
		Map<Integer, Folder> folderMap = new HashMap<>();
		for(Folder f : folders) {
			folderMap.put(f.getPid(), f);
		}
		for(Folder ff : folders) {
			int parentFid = ff.getPid();
			if(parentFid != 0) {//不是顶级目录
				Folder parentFolder = folderMap.get(parentFid);
				parentFolder.addSubFolder(ff);
			}
		}
		List<Folder> result = folders.stream().filter(folder->{
			return folder.getPid() == 0;
		}).collect(Collectors.toList());//只返回顶级目录
		return result;
	}

	@Override
	public boolean deleteFolder(User user, int fid) {
		int result = folderRepository.deleteByIdAndUid(fid, user.getId());
		if(result > 0) {
			logger.info("用户【{}】已经删除文件夹,ID={}",user.getId(),fid);
			return true;
		}
		return false;
		
	}

	@Override
	public Folder update(Folder folder) {
		folder.setGmtModify(new Date());
		Folder result = folderRepository.save(folder);
		logger.info("用户【{}】已经更新文件夹【{}】信息,ID={}",folder.getUid(),folder.getName(),folder.getId());
		return result;
	}

}
