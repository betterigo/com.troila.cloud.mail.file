package com.troila.cloud.mail.file.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.troila.cloud.mail.file.model.FileDetailInfo;
import com.troila.cloud.mail.file.model.RangeSettings;

public class FileUtil {
	/**
	 * 断点续传支持
	 * 
	 * @param file
	 * @param request
	 * @param response
	 * @return 跳过多少字节
	 */
	public static RangeSettings headerSetting(FileDetailInfo file, HttpServletRequest request, HttpServletResponse response, boolean preview) {
		long len = file.getSize();// 文件长度
		RangeSettings settings = null;
		response.reset();
		if (null == request.getHeader("Range")) {
			settings = new RangeSettings(len);
			response.setHeader("Accept-Ranges", "bytes");
			setResponse(settings, file.getOriginalFileName(), response, preview);
			return settings;
		}
		String range = request.getHeader("Range").replaceAll("bytes=", "");
		settings = getSettings(len, range);
		setResponse(settings, file.getOriginalFileName(), response, preview);
		return settings;
	}

	private static void setResponse(RangeSettings settings, String fileName, HttpServletResponse response, boolean preview) {
		if(preview) {			
			response.addHeader("Content-Disposition", "filename=\"" + IoUtil.toUtf8String(fileName) + "\"");
		}else {
			response.addHeader("Content-Disposition", "attachment; filename=\"" + IoUtil.toUtf8String(fileName) + "\"");
		}
		response.setContentType(IoUtil.setContentType(fileName));// set the MIME type.
		if (!settings.isRange()) {
			response.addHeader("Content-Length", String.valueOf(settings.getTotalLength()));
		} else {
			long start = settings.getStart();
			long end = settings.getEnd();
			long contentLength = settings.getContentLength();
			response.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
			response.addHeader("Content-Length", String.valueOf(contentLength));
			String contentRange = new StringBuffer("bytes ").append(start).append("-").append(end).append("/")
					.append(settings.getTotalLength()).toString();
			response.setHeader("Content-Range", contentRange);
		}
	}

	private static RangeSettings getSettings(long len, String range) {
		long contentLength = 0;
		long start = 0;
		long end = 0;
		if (range.startsWith("-"))// -500，最后500个
		{
			contentLength = Long.parseLong(range.substring(1));// 要下载的量
			end = len - 1;
			start = len - contentLength;
		} else if (range.endsWith("-"))// 从哪个开始
		{
			start = Long.parseLong(range.replace("-", ""));
			end = len - 1;
			contentLength = len - start;
		} else// 从a到b
		{
			String[] se = range.split("-");
			start = Long.parseLong(se[0]);
			end = Long.parseLong(se[1]);
			contentLength = end - start + 1;
		}
		return new RangeSettings(start, end, contentLength, len);
	}
}
