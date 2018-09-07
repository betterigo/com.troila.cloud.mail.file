package com.troila.cloud.mail.file.service;

import java.util.List;

import com.troila.cloud.mail.file.exception.ShareGroupException;
import com.troila.cloud.mail.file.model.ShareGroup;
import com.troila.cloud.mail.file.model.ShareGroupUserDetail;
import com.troila.cloud.mail.file.model.UserInfo;

public interface ShareGroupService {
	public ShareGroup create(UserInfo user,ShareGroup shareGroup) throws ShareGroupException;
	
	public void importGroupMembers(List<String> users, int gid, int operator) throws ShareGroupException;
	
	public void removeGroupMembers(List<String> users, int gid, int operator) throws ShareGroupException;
	
	public List<ShareGroup> findMyGroups(int uid);
	
	public List<ShareGroup> findMyJoinGroups(int uid);
	
	public List<ShareGroupUserDetail> listGroupUsers(int uid,int gid);
	
	public boolean assignManager(int owner,int gid,int uid,boolean isAssign) throws ShareGroupException;
	
	public ShareGroup dissolutionGroup(int uid,int gid) throws ShareGroupException;
}
