package com.troila.cloud.mail.file.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class OfficeFileUtils {
	private static final String ENCODING = "GB2312";// UTF-8

	private static String[] officeSuffix = new String[] {
			".doc",
			".docx",
			".xls",
			".xlsx",
			".ppt",
			".pptx",
			"doc",
			"docx",
			"xls",
			"xlsx",
			"ppt",
			"pptx"
	};
	
	public static String GetFileExt(String path) {
		String ext = null;
		int i = path.lastIndexOf('.');
		if (i > 0 && i < path.length() - 1) {
			ext = path.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	public static void createHtmlDir(String path) {
		int i = path.lastIndexOf('/');
		String dirPath = "";
		if (i > 0 && i < path.length() - 1) {
			dirPath = path.substring(0, i).toLowerCase();
		}
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

	}
	
	public static void createDir(String dirPath) {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

	}

	public static void writeFile(String content, String path) {
		createHtmlDir(path);
		OutputStream os = null;
		BufferedWriter bw = null;
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			os = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(os, ENCODING));
			bw.write(content);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (os != null)
					os.close();
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	public static boolean isOfficeFile(String suffix) {
		for(String suf:officeSuffix) {
			if(suf.equals(suffix)) {
				return true;
			}
		}
		return false;
	}
}
