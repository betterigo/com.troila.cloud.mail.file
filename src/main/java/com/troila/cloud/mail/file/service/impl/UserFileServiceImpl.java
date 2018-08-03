package com.troila.cloud.mail.file.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.repository.UserFileRespository;
import com.troila.cloud.mail.file.service.UserFileService;

@Service
@Transactional
public class UserFileServiceImpl implements UserFileService{

//	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private UserFileRespository userFileRespository;
	
	@Override
	public Page<UserFile> findAll(int uid,int page,int size) {
		Pageable pageable = PageRequest.of(page, size);
		return userFileRespository.findByUidOrderByGmtCreateDesc(uid, pageable);
	}

	@Override
	public Page<UserFile> search(UserFile example, int page, int size) {
		return null;
	}

	@Override
	public Page<UserFile> findByFolderId(int userid, int fid, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return userFileRespository.findByUidAndFolderIdOrderByGmtCreateDesc(userid, fid, pageable);
	}

	@Override
	public UserFile findOne(int uid, int id) {
		UserFile result = userFileRespository.getOne(id);
		if(result.getUid()!=uid) {			
			return null;
		}else {
			return result;
		}
	}
	
}	
