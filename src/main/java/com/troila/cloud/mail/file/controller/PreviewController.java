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
		File file = new File("D://test.doc");
		String folder = priviewConverter.toHtml(new FileInputStream(file), "doc");
		return "preview/"+folder+"/index.html";
	}
	
	@GetMapping("/test")
	public void del1(HttpServletResponse response) throws FileNotFoundException {
		try {
			response.setContentType("text/html");
			response.getWriter().print("aaaa");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
