package com.troila.cloud.mail.file.controller;

import javax.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.troila.cloud.mail.file.model.UserInfo;

@RestController
@RequestMapping("/user")
public class UserController {	
	/**
	 * 获取用户信息
	 * @return
	 */
	@GetMapping
	public ResponseEntity<UserInfo> getUserInfo(HttpSession session){
		UserInfo user = (UserInfo) session.getAttribute("user");
		return ResponseEntity.ok(user);
	}
}
