package com.troila.cloud.mail.file.controller;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.exception.UserSettingsException;
import com.troila.cloud.mail.file.model.UserInfo;
import com.troila.cloud.mail.file.model.UserSettings;
import com.troila.cloud.mail.file.model.ValidateInfo;
import com.troila.cloud.mail.file.service.UserService;
import com.troila.cloud.mail.file.utils.TokenUtil;

@RestController
@RequestMapping("/user")
public class UserController {	
	
	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private UserService userService;
	
	private ObjectMapper mapper = new ObjectMapper();
	/**
	 * 获取用户信息
	 * @return
	 */
	@GetMapping
	public ResponseEntity<UserInfo> getUserInfo(HttpSession session){
		UserInfo user = (UserInfo) session.getAttribute("user");
		return ResponseEntity.ok(user);
	}
	
	/**
	 * 用户下载自己的private或者protected文件需要这个token
	 */
	@GetMapping("/filetoken")
	public ResponseEntity<String> getDownloadToken(HttpSession session,@RequestParam(name="fid",required=false,defaultValue="0") int fid){
		String base64Token = "";
		try {
			UserInfo user = (UserInfo) session.getAttribute("user");
			String token = TokenUtil.getToken();
			base64Token =Base64.getEncoder().encodeToString(token.getBytes("UTF-8"));
			ValidateInfo validateInfo = new ValidateInfo();
			validateInfo.setKey(String.valueOf(user.getId()));
			validateInfo.setFid(fid);
			redisTemplate.opsForValue().set(token, mapper.writeValueAsString(validateInfo), 5, TimeUnit.MINUTES);
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(base64Token);
	}
	
	/**
	 * 只有系统管理员才可以访问该接口
	 * @return
	 */
	@PutMapping("/updatesettings")
	public ResponseEntity<UserSettings> updateUserSettings(@RequestBody UserSettings userSettings){
		try {
			UserSettings result = userService.updateSettings(userSettings);
			return ResponseEntity.ok(result);
		} catch (UserSettingsException e) {
			e.printStackTrace();
		}
		return ResponseEntity.badRequest().build();
	}
}
