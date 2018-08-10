package com.troila.cloud.mail.file.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PreviewConverter previewConverter;

	@Autowired
	private FileService fileService;

	@GetMapping("/tohtml/{fid}")
	public void toHtml(@PathVariable("fid") int fid, HttpServletResponse response) {
		FileDetailInfo fileDetailInfo = null;
		try {
			fileDetailInfo = fileService.find(fid);
			if (fileDetailInfo == null) {
				throw new BadRequestException("文件资源未找到！");
			}
			logger.info("准备预览文件{}...HTML格式", fileDetailInfo.getOriginalFileName());
			InputStream source = fileService.download(fileDetailInfo);
			String path = previewConverter.toHtml(source, fileDetailInfo.getSuffix());
			response.sendRedirect("/preview/" + path);
		} catch (IOException e) {
			logger.error("准备预览文件{}...HTML格式,发生错误：{}", fileDetailInfo.getOriginalFileName(), e.getMessage(), e);
			throw new BadRequestException("预览发生错误！");
		}
	}

	@GetMapping("/topdf/{fid}")
	public void toPdf(@PathVariable("fid") int fid, HttpServletResponse response) {
		FileDetailInfo fileDetailInfo = null;
		try {
			fileDetailInfo = fileService.find(fid);
			if (fileDetailInfo == null) {
				response.sendError(HttpStatus.NOT_FOUND.value(), "文件资源未找到！");
			}
			logger.info("准备预览文件{}...PDF格式", fileDetailInfo.getOriginalFileName());
			InputStream source = fileService.download(fileDetailInfo);
			response.setContentType("application/pdf");
			OutputStream out = response.getOutputStream();
			previewConverter.toPdf(source, out, fileDetailInfo.getSuffix());
			out.flush();
			out.close();
		} catch (IOException e) {
			logger.error("准备预览文件{}...HTML格式,发生错误：{}", fileDetailInfo.getOriginalFileName(), e.getMessage(), e);
			throw new BadRequestException("预览发生错误！");
		}
	}

}
