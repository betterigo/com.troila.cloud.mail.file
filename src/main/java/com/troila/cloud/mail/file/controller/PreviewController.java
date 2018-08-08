package com.troila.cloud.mail.file.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.troila.cloud.mail.file.component.PreviewConverter;
import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.service.FileService;


@Controller
@RequestMapping("/preview")
public class PreviewController {
	
	@Autowired
	private PreviewConverter priviewConverter;
	
	@Autowired
	private FileService fileService;
	
	@GetMapping
	public String preview() throws FileNotFoundException {
		File file = new File("test.doc");
		String folder = priviewConverter.toHtml(new FileInputStream(file), "doc");
		return "preview/"+folder+"/index.html";
	}
	
	@GetMapping("/office/{fid}")
	public void del1(@PathVariable("fid")int fid,HttpServletResponse response) throws FileNotFoundException {
		try {
			FileDetailInfo fileDetailInfo = fileService.find(fid);
			InputStream in = fileService.download(fileDetailInfo);
			String content = priviewConverter.toHtml(in, fileDetailInfo.getSuffix());
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");
			response.getWriter().print(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
