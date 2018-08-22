package com.troila.cloud.mail.file.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.troila.cloud.mail.file.component.DownloadUrlSecureConverter;
import com.troila.cloud.mail.file.model.UserFile;
import com.troila.cloud.mail.file.model.ValidateInfo;
import com.troila.cloud.mail.file.service.UserFileService;

@Controller
@RequestMapping("/page")
public class PageController {
	
	@Autowired
	private DownloadUrlSecureConverter downloadUrlSecureConverter;
	
	@Autowired
	private UserFileService userFileService;
	
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
		validateInfo.setSecretUrl(secretUrl);
		validateInfo.setFileName(userFile.getOriginalFileName());
		model.addAttribute("validateInfo", validateInfo);
		model.addAttribute("secretUrl", secretUrl);
		return "validate";
	}
	
	@ResponseBody
	@GetMapping("/validate/key")
	public String validateKey(@ModelAttribute ValidateInfo validateInfo,HttpServletRequest request,HttpServletResponse response) {
		String secretUrl = validateInfo.getSecretUrl();
		byte[] byteUrl;
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
			UserFile userFile = userFileService.findOnePublic(fid);
			if(userFile.getSecretKey()!=null && userFile.getSecretKey().equals(validateInfo.getKey())) {
				//验证通过
				request.setAttribute("key", validateInfo.getKey());
				request.setAttribute("fid", userFile.getId());
				try {
					request.getRequestDispatcher("/file/download").forward(request, response);
				} catch (ServletException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}else {
				//未通过
				return "error";
			}
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return "error";
	}
}
