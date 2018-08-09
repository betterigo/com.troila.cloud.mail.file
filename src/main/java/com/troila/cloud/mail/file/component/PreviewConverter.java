package com.troila.cloud.mail.file.component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.util.ResourceUtils;

public interface PreviewConverter {
	/**
	 * 把文件转换为html格式预览
	 * @return
	 */
	public void toPdf(InputStream in,OutputStream out, String suffix);
	
	public String toHtml(InputStream in,String suffix);
	
	default File getClassPath() {
		//获取跟目录
		File path = null;
		try {
			path = new File(ResourceUtils.getURL("classpath:").getPath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}
}
