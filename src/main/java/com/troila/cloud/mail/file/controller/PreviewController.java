package com.troila.cloud.mail.file.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.troila.cloud.mail.file.component.PreviewConverter;


@Controller
@RequestMapping("/preview")
public class PreviewController {
	
	@Autowired
	private PreviewConverter priviewConverter;
	
	@GetMapping
	public String preview() throws FileNotFoundException {
		File file = new File("test.doc");
		String folder = priviewConverter.toHtml(new FileInputStream(file), "doc");
		return "preview/"+folder+"/index.html";
	}
	
	@GetMapping("/test")
	public void del1(HttpServletResponse response) throws FileNotFoundException {
		try {
			File file = new File("test.docx");
			String content = priviewConverter.toHtml(new FileInputStream(file), "docx");
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");
			response.getWriter().print(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
