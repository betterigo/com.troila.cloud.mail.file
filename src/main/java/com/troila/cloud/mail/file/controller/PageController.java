package com.troila.cloud.mail.file.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.troila.cloud.mail.file.component.DownloadUrlSecureConverter;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.ValidateInfo;
import com.troila.cloud.mail.file.service.UserFileService;

@Controller
@RequestMapping("/page")
public class PageController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DownloadUrlSecureConverter downloadUrlSecureConverter;
	
	@Autowired
	private UserFileService userFileService;
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@GetMapping("/validate")
	public String test(Model model,HttpServletRequest request) {
		byte[] byteUrl;
		String secretUrl = (String) request.getAttribute("secreturl");
		UserFile userFile = null;
		try {
			byte[] fidByte = Base64.getDecoder().decode(secretUrl.getBytes("UTF-8"));
			byteUrl = downloadUrlSecureConverter.decode(fidByte);
			String secretStr = new String(byteUrl,"UTF-8");
			int pos = secretStr.indexOf("&");
			if(pos==-1) {
				return "error";
			}
			String fidStr = secretStr.substring(0, pos);
			int fid = Integer.valueOf(fidStr);
			userFile = userFileService.findOnePublic(fid);
			model.addAttribute("filename", userFile.getOriginalFileName());
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		ValidateInfo validateInfo = new ValidateInfo();
		Object preview = request.getAttribute("preview");
		if(preview!=null) {
			validateInfo.setPreview(true);
		}
		validateInfo.setSecretUrl(secretUrl);
		validateInfo.setFileName(userFile.getOriginalFileName());
		model.addAttribute("validateInfo", validateInfo);
		model.addAttribute("secretUrl", secretUrl);
		return "validate";
	}
	
	@GetMapping("/validate/key")
	public String validateKey(@ModelAttribute ValidateInfo validateInfo,HttpServletRequest request,HttpServletResponse response,RedirectAttributes attr) {
		String secretUrl = validateInfo.getSecretUrl();
		byte[] byteUrl;
		try {
			byte[] fidByte = Base64.getDecoder().decode(secretUrl.getBytes("UTF-8"));
			byteUrl = downloadUrlSecureConverter.decode(fidByte);
			String secretStr = new String(byteUrl,"UTF-8");
			int pos = secretStr.indexOf("&");
			if(pos==-1) {
				throw new BadRequestException("提取码验证异常！");
			}
			String fidStr = secretStr.substring(0, pos);
			int fid = Integer.valueOf(fidStr);
			UserFile userFile = userFileService.findOnePublic(fid);
			if(userFile.getSecretKey()!=null && userFile.getSecretKey().equals(validateInfo.getKey())) {
				validateInfo.setFid(fid);
				UUID uuid = UUID.randomUUID();
				String key = uuid.toString();
				String secretCode = Base64.getEncoder().encodeToString(key.getBytes("UTF-8"));
				try {
					redisTemplate.opsForValue().set(key, mapper.writeValueAsString(validateInfo),5,TimeUnit.MINUTES);//5分钟有效时间
					attr.addAttribute("secretcode", secretCode);
					return "redirect:/file/download";
				} catch (IOException e) {
					logger.error("验证文件{}发生错误！",validateInfo.getFileName(),e);
				}
				throw new BadRequestException("提取码验证异常！");
			}else {
				//未通过
				throw new BadRequestException("提取码验证异常！");
			}
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			logger.error("验证文件{}发生错误！",validateInfo.getFileName(),e);
		}
		throw new BadRequestException("提取码验证异常！");
	}
}
