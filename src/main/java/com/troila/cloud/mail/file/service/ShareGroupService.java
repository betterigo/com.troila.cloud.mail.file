package com.troila.cloud.mail.file.service;

import java.util.List;

import com.troila.cloud.mail.file.exception.ShareGroupException;
import com.troila.cloud.mail.file.model.ShareGroup;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.model.UserInfo;

public interface ShareGroupService {
	public ShareGroup create(UserInfo user,ShareGroup shareGroup) throws ShareGroupException;
	
	public void importGroupMembers(List<String> users, int gid) throws ShareGroupException;
	
	public List<ShareGroup> findMyGroups(int uid);
	
	public List<ShareGroup> findMyJoinGroups(int uid);
	
	public List<User> listGroupUsers(int uid,int gid);
	
	public boolean assignManager(int owner,int gid,int uid,boolean isAssign) throws ShareGroupException;
}
