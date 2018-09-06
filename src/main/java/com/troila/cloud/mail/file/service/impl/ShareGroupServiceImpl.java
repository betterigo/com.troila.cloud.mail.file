package com.troila.cloud.mail.file.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.troila.cloud.mail.file.exception.FolderException;
import com.troila.cloud.mail.file.exception.ShareGroupException;
import com.troila.cloud.mail.file.model.Folder;
import com.troila.cloud.mail.file.model.ShareGroup;
import com.troila.cloud.mail.file.model.ShareGroupUser;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.model.UserSettings;
import com.troila.cloud.mail.file.model.fenum.AccessList;
import com.troila.cloud.mail.file.model.fenum.FolderAuth;
import com.troila.cloud.mail.file.model.fenum.GroupUserRole;
import com.troila.cloud.mail.file.repository.ShareGroupRepository;
import com.troila.cloud.mail.file.repository.ShareGroupUserRepostory;
import com.troila.cloud.mail.file.repository.UserFolderRepository;
import com.troila.cloud.mail.file.repository.UserRepository;
import com.troila.cloud.mail.file.repository.UserSettingsRepository;
import com.troila.cloud.mail.file.service.FolderService;
import com.troila.cloud.mail.file.service.ShareGroupService;

@Service
public class ShareGroupServiceImpl implements ShareGroupService{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ShareGroupRepository shareGroupRepository;
	
	@Autowired
	private UserSettingsRepository userSettingsRepository;
	
	@Autowired
	private ShareGroupUserRepostory shareGroupUserRepostory;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private FolderService folderService;
	
	@Autowired
	private UserFolderRepository userFolderRepository;
	
	private static final long DEFAULT_MAX_STORAGE = 1 * 1024 * 1024 *1024L;

	private static final int DEFAULT_MAX_MEMBERS = 30;
	
	@Override
	@Transactional
	public ShareGroup create(UserInfo user, ShareGroup shareGroup) throws ShareGroupException {
		shareGroup.setOwner(user.getId());
		if(shareGroup.getMaxMembers() == 0) {
			shareGroup.setMaxStorage(DEFAULT_MAX_STORAGE);
		}
		if(shareGroup.getMaxMembers() == 0) {
			shareGroup.setMaxMembers(DEFAULT_MAX_MEMBERS);
		}
		//处理用户剩余空间
		UserSettings userSettings = userSettingsRepository.findByUid(user.getId());
		if(userSettings.getUsed() + shareGroup.getMaxStorage() > userSettings.getVolume()) {
			throw new ShareGroupException("用户空间不足！");
		}
		//创建分享文件夹
		try {
			Folder folder = folderService.create(user, shareGroup.getName(), 0, AccessList.SHARE);
			logger.info("已经为用户{}创建分享文件夹{}",user.getName(),folder.getName());
			shareGroup.setFolderId(folder.getId());
		} catch (FolderException e) {
			logger.error("创建分享文件夹失败！",e);
			throw new ShareGroupException("创建分享文件夹失败！");
		}
		ShareGroup result = shareGroupRepository.save(shareGroup);
		userSettings.setUsed(userSettings.getUsed() + result.getMaxStorage());
		userSettingsRepository.save(userSettings);
		//创建组admin用户
		ShareGroupUser adminUser = new ShareGroupUser();
		adminUser.setGmtCreate(new Date());
		adminUser.setGid(result.getId());
		adminUser.setUid(user.getId());
		adminUser.setUserRole(GroupUserRole.ADMIN);
		shareGroupUserRepostory.save(adminUser);
		return result;
	}

	@Transactional
	@Override
	public void importGroupMembers(List<String> users, int gid) throws ShareGroupException {
		
		Optional<ShareGroup> group = shareGroupRepository.findById(gid);
		int folderId;
		if(group.isPresent()) {
			folderId = group.get().getFolderId();
		}else {
			throw new ShareGroupException("无法获取用户共享组{"+gid+"}文件夹信息！");
		}
		for(String userName:users){
			//验证用户是否存在
			User user = userRepository.findByName(userName);
			if(user == null) {
				throw new ShareGroupException("无法获取用户{"+userName+"}的信息！");
			}
			//像用户添加一个共享文件夹
			UserFolder userFolder = new UserFolder();
			userFolder.setFolderId(folderId);
			userFolder.setAuth(FolderAuth.RW);//读写权限
			userFolder.setUid(user.getId());
			userFolder.setGmtCreate(new Date());
			userFolderRepository.save(userFolder);
			//添加组用户
			ShareGroupUser member = new ShareGroupUser();
			member.setGmtCreate(new Date());
			member.setGid(gid);
			member.setUid(user.getId());
			member.setUserRole(GroupUserRole.MEMBER);
			shareGroupUserRepostory.save(member);
		};
	}

	@Override
	public List<ShareGroup> findMyGroups(int uid) {
		return shareGroupRepository.findByOwner(uid);
	}

	@Override
	public List<ShareGroup> findMyJoinGroups(int uid) {
		List<ShareGroupUser> myJoin = shareGroupUserRepostory.findByUid(uid);
		List<ShareGroup> result = new ArrayList<>();
		result = myJoin.stream().map(joinGroup->{
			Optional<ShareGroup> group = shareGroupRepository.findById(joinGroup.getGid());
			return group.orElse(null);
		}).filter(shareGroup->shareGroup!=null)
				.distinct().collect(Collectors.toList());
		return result;
	}

	@Override
	public List<User> listGroupUsers(int uid, int gid) {
		List<ShareGroupUser> myJoin = shareGroupUserRepostory.findByUid(uid);
		List<User> result = new ArrayList<>();
		result = myJoin.stream().filter(su->su.getGid()==gid).map(su->{
			Optional<User> user = userRepository.findById(su.getUid());
			return user.orElse(null);
		}).filter(user->user!=null)
		 .distinct()
		 .map(user->{
			 user.setGmtCreate(null);
			 user.setPassword(null);
			 user.setUserCode(null);
			 user.setGmtModify(null);
			 return user;
		 })
		 .collect(Collectors.toList());
		return result;
	}

	@Override
	public boolean assignManager(int owner,int gid, int uid,boolean isAssign) throws ShareGroupException {
		List<ShareGroupUser> su = shareGroupUserRepostory.findByUidAndGid(uid, gid);
		if(su==null || su.isEmpty()) {
			throw new ShareGroupException("无法获取用户{"+uid+"}的分享组信息！");
		}
		ShareGroupUser user = su.get(0);
		if(user.getId()!=owner) {
			throw new ShareGroupException("您不是此分享组的创建者，无法进行此操作！");
		}
		//设定对应的文件夹权限
		Optional<ShareGroup> shareGroup = shareGroupRepository.findById(user.getGid());
		ShareGroup sg = shareGroup.orElseThrow(()->new ShareGroupException("无法获取用户分享组信息！"));
		//通过shareGroup中的folderId获取文件夹权限信息
		Optional<UserFolder> userFolderOpt = userFolderRepository.findById(sg.getFolderId());
		UserFolder userFolder = userFolderOpt.orElseThrow(()->new ShareGroupException("无法获取文件夹权限信息！"));
		if(isAssign) {			
			user.setUserRole(GroupUserRole.MANAGER);
			userFolder.setAuth(FolderAuth.RWMD);
		}else {
			user.setUserRole(GroupUserRole.MEMBER);
			userFolder.setAuth(FolderAuth.RW);
		}
		try {
			shareGroupUserRepostory.save(user);
			userFolderRepository.save(userFolder);
		} catch (Exception e) {
			logger.error("",e);
			throw new ShareGroupException("保存用户信息失败！");
		}
		return true;
	}
}
