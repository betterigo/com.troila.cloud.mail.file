package com.troila.cloud.mail.file.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.troila.cloud.mail.file.exception.ShareGroupException;
import com.troila.cloud.mail.file.model.ShareGroup;
import com.troila.cloud.mail.file.model.User;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.service.ShareGroupService;

@RestController
@RequestMapping("/group")
public class GroupController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ShareGroupService shareGroupService;

	@PostMapping
	public ResponseEntity<ShareGroup> createGroup(HttpSession session, @RequestBody ShareGroup shareGroup) {
		UserInfo user = (UserInfo) session.getAttribute("user");
		ShareGroup result = null;
		try {
			result = shareGroupService.create(user, shareGroup);
		} catch (ShareGroupException e) {
			logger.error("创建共享组失败！", e);
			throw new BadRequestException("创建共享组失败！");
		}
		return ResponseEntity.ok(result);
	}

	/**
	 * 查询自己创建的分享组
	 * 
	 * @param session
	 * @return
	 */
	@GetMapping("/my")
	public ResponseEntity<List<ShareGroup>> listMyGroups(HttpSession session) {
		UserInfo user = (UserInfo) session.getAttribute("user");
		List<ShareGroup> result = shareGroupService.findMyGroups(user.getId());
		return ResponseEntity.ok(result);
	}

	/**
	 * 查询用户加入的分享组，包括自己创建的
	 * 
	 * @param session
	 * @return
	 */
	@GetMapping("/myjoin")
	public ResponseEntity<List<ShareGroup>> listMyJoinGroups(HttpSession session) {
		UserInfo user = (UserInfo) session.getAttribute("user");
		List<ShareGroup> result = shareGroupService.findMyJoinGroups(user.getId());
		return ResponseEntity.ok(result);
	}

	/**
	 * 查询已加入的分享组中的用户
	 * 
	 * @param session
	 * @param gid
	 * @return
	 */
	@GetMapping("/groupusers")
	public ResponseEntity<List<User>> listGroupUsers(HttpSession session, @RequestParam("gid") int gid) {
		UserInfo user = (UserInfo) session.getAttribute("user");
		List<User> result = shareGroupService.listGroupUsers(user.getId(), gid);
		return ResponseEntity.ok(result);
	}

	/**
	 * 指定群管理
	 * 
	 * @param session
	 * @param gid
	 * @param uid
	 * @return
	 */
	@PostMapping("/assignmanager")
	public ResponseEntity<Boolean> assignManager(HttpSession session, int gid, @RequestParam("uid") List<Integer> uids,
			@RequestParam(defaultValue = "true", required = false) boolean isAssign) {
		UserInfo user = (UserInfo) session.getAttribute("user");
		try {
			for (int uid : uids) {
				shareGroupService.assignManager(user.getId(),gid, uid, isAssign);
			}
		} catch (ShareGroupException e) {
			throw new BadRequestException("设置管理员失败！"+e.getMessage());
		}
		return ResponseEntity.ok(true);
	}
	
	@DeleteMapping("/dissolution")
	public ResponseEntity<ShareGroup> dissolutionGroup(HttpSession session,int gid){
		UserInfo user = (UserInfo) session.getAttribute("user");
		return null;
	}
}
