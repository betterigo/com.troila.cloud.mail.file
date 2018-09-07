package com.troila.cloud.mail.file.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
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
import com.troila.cloud.mail.file.model.ShareGroupUserDetail;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.model.UserFolder;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.model.UserSettings;
import com.troila.cloud.mail.file.model.fenum.AccessList;
import com.troila.cloud.mail.file.model.fenum.FolderAuth;
import com.troila.cloud.mail.file.model.fenum.FolderType;
import com.troila.cloud.mail.file.model.fenum.GroupUserRole;
import com.troila.cloud.mail.file.repository.FolderRepository;
import com.troila.cloud.mail.file.repository.ShareGroupRepository;
import com.troila.cloud.mail.file.repository.ShareGroupUserDetailRepository;
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
	
	@Autowired
	private FolderRepository folderRepository;
	
	@Autowired
	private ShareGroupUserDetailRepository shareGroupUserDetailRepository;
	
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
		if(userSettings.getShareVolumeUsed() + shareGroup.getMaxStorage() > userSettings.getShareVolume()) {
			throw new ShareGroupException("用户空间不足！");
		}
		//创建分享文件夹
		try {
			//获取根目录
			List<Folder> rootL = folderRepository.findByTypeAndUid(FolderType.ROOT, user.getId());
			if(rootL == null || rootL.isEmpty()) {
				throw new ShareGroupException("没有找到用户的root目录！");
			}
			Folder folder = folderService.create(user, shareGroup.getName(), rootL.get(0).getId(), AccessList.SHARE,null);
			logger.info("已经为用户{}创建分享文件夹{}",user.getName(),folder.getName());
			shareGroup.setFolderId(folder.getId());
		shareGroup.setGmtCreate(new Date());
		ShareGroup result = shareGroupRepository.save(shareGroup);
		//更新folder中的gid，用来以后创建子文件夹的时候判断归于哪个分享组
		folder.setGid(result.getId());
		folderRepository.save(folder);
		//修改settings信息
		userSettings.setUsed(userSettings.getShareVolumeUsed() + result.getMaxStorage());
		userSettingsRepository.save(userSettings);
		//创建组admin用户
		ShareGroupUser adminUser = new ShareGroupUser();
		adminUser.setGmtCreate(new Date());
		adminUser.setGid(result.getId());
		adminUser.setUid(user.getId());
		adminUser.setUserRole(GroupUserRole.ADMIN);
		shareGroupUserRepostory.save(adminUser);
		return result;
		} catch (FolderException e) {
			logger.error("创建分享文件夹失败！",e);
			throw new ShareGroupException("创建分享文件夹失败！"+e.getMessage());
		}
	}

	@Transactional
	@Override
	public void importGroupMembers(List<String> users, int gid, int operator) throws ShareGroupException {
		
		Optional<ShareGroup> group = shareGroupRepository.findById(gid);
		int folderId;
		if(group.isPresent()) {
			folderId = group.get().getFolderId();
		}else {
			throw new ShareGroupException("无法获取用户共享组{"+gid+"}文件夹信息！");
		}
		List<ShareGroupUser> opList = shareGroupUserRepostory.findByUidAndGid(operator, gid);
		if(opList == null || opList.isEmpty()) {
			throw new ShareGroupException("你不是共享组{"+gid+"}的用户！");
		}
		ShareGroupUser op = opList.get(0);
		if(op.getUserRole() == GroupUserRole.MEMBER) {//权限认证
			throw new ShareGroupException("你没有权限为共享组{"+gid+"}添加用户！只有管理员以及群主可以进行此操作");
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
	public List<ShareGroupUserDetail> listGroupUsers(int uid, int gid) {
		List<ShareGroupUserDetail> users = shareGroupUserDetailRepository.findByGid(gid);
		if(users.stream().anyMatch(user->user.getUid()==uid)) {
			return users;
		}else {
			return null;
		}
	}

	@Transactional
	@Override
	public boolean assignManager(int owner,int gid, int uid,boolean isAssign) throws ShareGroupException {
		List<ShareGroupUser> su = shareGroupUserRepostory.findByUidAndGid(uid, gid);
		if(su==null || su.isEmpty()) {
			throw new ShareGroupException("无法获取用户{"+uid+"}的分享组信息！");
		}
		ShareGroupUser user = su.get(0);
		Optional<ShareGroup> shareGroup = shareGroupRepository.findById(user.getGid());
		ShareGroup sg = shareGroup.orElseThrow(()->new ShareGroupException("无法获取用户分享组信息！"));
		if(sg.getOwner()!=owner) {
			throw new ShareGroupException("您不是此分享组的创建者，无法进行此操作！");
		}
		//设定对应的文件夹权限
		//通过shareGroup中的folderId获取文件夹权限信息
		Optional<UserFolder> userFolderOpt = userFolderRepository.findByUidAndFolderId(uid, sg.getFolderId());
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

	@Transactional
	@Override
	public ShareGroup dissolutionGroup(int uid, int gid) throws ShareGroupException {
		List<ShareGroupUser> su = shareGroupUserRepostory.findByUidAndGid(uid, gid);
		if(su==null || su.isEmpty()) {
			throw new ShareGroupException("无法获取用户{"+uid+"}的分享组信息！");
		}
		ShareGroupUser user = su.get(0);
		Optional<ShareGroup> shareGroup = shareGroupRepository.findById(user.getGid());
		ShareGroup sg = shareGroup.orElseThrow(()->new ShareGroupException("无法获取用户分享组信息！"));
		if(sg.getOwner()!=uid) {
			throw new ShareGroupException("您不是此分享组的创建者，无法进行此操作！");
		}
		//获取分享组信息
		//转让admin用户
		//1 获取所有组用户
		List<ShareGroupUser> sUsers = shareGroupUserRepostory.findByGid(gid);
		List<ShareGroupUser> targetUsers = sUsers.stream().filter(u->u.getUserRole() == GroupUserRole.MANAGER)
				.collect(Collectors.toList());
		if(targetUsers == null || targetUsers.isEmpty()) {
			targetUsers = sUsers.stream().filter(u->u.getUserRole() == GroupUserRole.MEMBER)
					.collect(Collectors.toList());
		}
		//1删除组用户
		shareGroupUserRepostory.delete(user);
		if(targetUsers == null || targetUsers.isEmpty()) {
			//2删除这个分享组
			shareGroupRepository.deleteById(gid);
			//3删除用户分享文件夹
			folderRepository.deleteById(sg.getFolderId());
			//删除此用户该文件夹的权限信息
			Optional<UserFolder> userFolderOpt = userFolderRepository.findByUidAndFolderId(uid, sg.getFolderId());
			if(userFolderOpt.isPresent()) {
				userFolderRepository.delete(userFolderOpt.get());
			}
			logger.info("已经清除用户{}分享组{}的相关权限信息，由于该分享组只有当前admin一人，已经删除该分享组及其文件！",uid,gid);
		}
		int totalMember = targetUsers.size();
		int target = RandomUtils.nextInt(0, totalMember);
		ShareGroupUser newAdmin = targetUsers.get(target);
		newAdmin.setUserRole(GroupUserRole.ADMIN);
		shareGroupUserRepostory.save(newAdmin);
		sg.setOwner(newAdmin.getUid());
		shareGroupRepository.save(sg);
		//修改用户settings信息
		UserSettings userSettings = userSettingsRepository.findByUid(newAdmin.getUid());
		if(userSettings == null) {
			throw new ShareGroupException("无法获取用户{"+newAdmin.getUid()+"}的用户设置信息！");
		}
		Optional<UserFolder> targetUserFolderOpt = userFolderRepository.findByUidAndFolderId(newAdmin.getUid(), sg.getFolderId());
		UserFolder targetFolder = targetUserFolderOpt.orElseThrow(()->new ShareGroupException("无法获取用户文件夹权限信息！"));
		targetFolder.setAuth(FolderAuth.RWMD);
		userFolderRepository.save(targetFolder);
		userSettings.setShareVolume(userSettings.getShareVolume()+sg.getMaxStorage());
		userSettings.setShareVolumeUsed(userSettings.getShareVolumeUsed()+sg.getMaxStorage());
		userSettingsRepository.save(userSettings);
		return sg;
	}

	@Transactional
	@Override
	public void removeGroupMembers(List<String> users, int gid, int operator) throws ShareGroupException {
		Optional<ShareGroup> group = shareGroupRepository.findById(gid);
		int folderId;
		if(group.isPresent()) {
			folderId = group.get().getFolderId();
		}else {
			throw new ShareGroupException("无法获取用户共享组{"+gid+"}文件夹信息！");
		}
		List<ShareGroupUser> opList = shareGroupUserRepostory.findByUidAndGid(operator, gid);
		if(opList == null || opList.isEmpty()) {
			throw new ShareGroupException("你不是共享组{"+gid+"}的用户！");
		}
		ShareGroupUser op = opList.get(0);
		if(op.getUserRole() == GroupUserRole.MEMBER) {//权限认证
			throw new ShareGroupException("你没有权限为共享组{"+gid+"}添加用户！只有管理员以及群主可以进行此操作");
		}
		for(String userName:users){
			//验证用户是否存在
			User user = userRepository.findByName(userName);
			if(user == null) {
				throw new ShareGroupException("无法获取用户{"+userName+"}的信息！");
			}
			//删除这个用户
			List<ShareGroupUser> su = shareGroupUserRepostory.findByUidAndGid(user.getId(), gid);
			if(su == null || su.isEmpty()) {
				continue;
			}
			ShareGroupUser targetUser = su.get(0);
			//判断角色
			if(targetUser.getUserRole() == GroupUserRole.ADMIN) {
				throw new ShareGroupException("你没有此权限删除此用户："+user.getName());
			}
			//同是管理员不能删除对方
			if(targetUser.getUserRole() == GroupUserRole.MANAGER && op.getUserRole() == GroupUserRole.MANAGER) {
				throw new ShareGroupException("你没有此权限删除此用户："+user.getName());
			}
			shareGroupUserRepostory.delete(targetUser);//清除组用户
			Optional<UserFolder> userFolderOpt = userFolderRepository.findByUidAndFolderId(targetUser.getId(), folderId);
			UserFolder userFolder = userFolderOpt.orElseThrow(()->new ShareGroupException("无法获取用户文件夹信息"));
			userFolderRepository.delete(userFolder);//清除用户文件夹
		};
	}
}
