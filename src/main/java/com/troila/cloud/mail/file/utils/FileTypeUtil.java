package com.troila.cloud.mail.file.utils;

import java.util.ArrayList;
import java.util.List;

import com.troila.cloud.mail.file.model.fenum.FileType;

public class FileTypeUtil {
	
	private static List<String> vedioSuffixes = new ArrayList<>();
	
	private static List<String> audioSuffixes = new ArrayList<>();
	
	private static List<String> documentSuffixes = new ArrayList<>();
	
	private static List<String> applicationSuffixes = new ArrayList<>();
	
	private static List<String> pictureSuffixes = new ArrayList<>();
	
	static {
		String[] vedio = new String[] {
				".avi",".mpeg",".mp4",".f4v",
				".wmv",".mov",".mkv",".flv",".m4v",
				".rmvb",".rm",".3gp",".dat",".ts",
				".mts",".vob"
		};
		for(int i = 0; i< vedio.length;i++) {
			vedioSuffixes.add(vedio[i]);
		}
		
		String[] audio = new String[] {
				".aac",".ac3",".amr",".ape",
				".cda",".dts",".flac",".mla",".m2a",
				".m4a",".mka",".mp2",".mp3",".mpa",
				".ra",".tta",".wav",".wma",".wv",
				".mid",".midi",".ogg",".oga"
		};
		
		for(int i = 0; i< audio.length;i++) {
			audioSuffixes.add(audio[i]);
		}
		
		String[] document = new String[] {
				".doc",".xls",".ppt",".docx",
				".xlsx",".pptx",".txt",".properties",
				".xml",".html",".zip",".rar",".tar"
		};
		
		for(int i = 0; i< document.length;i++) {
			documentSuffixes.add(document[i]);
		}
		
		String[] application = new String[] {
				".exe",".msi",".com",".bat",
				".sh",".java",".py",".jar",".war"
		};
		
		for(int i = 0; i< application.length;i++) {
			applicationSuffixes.add(application[i]);
		}
		

		String[] picture = new String[] {
				".jpeg2000",".tiff",".psd",".png",
				".swf",".svg",".pcx",".dxf",".wmf",
				".emf",".lic",".eps",".tga",".wmf",
				".bmp",".jpg",".jpeg",".gif",".wmf",
				".ico"
		};
		

		for(int i = 0; i< picture.length;i++) {
			pictureSuffixes.add(picture[i]);
		}
		
	}
	
	public static FileType distinguishFileType(String suffix) {
		
		if(vedioSuffixes.contains(suffix)) {
			return FileType.VEDIO;
		}
		if(audioSuffixes.contains(suffix)) {
			return FileType.AUDIO;
		}
		if(documentSuffixes.contains(suffix)) {
			return FileType.DOCUMENT;
		}
		if(applicationSuffixes.contains(suffix)) {
			return FileType.APPLICATION;
		}
		if(pictureSuffixes.contains(suffix)) {
			return FileType.PICTURE;
		}
		return FileType.OTHER;
	}
}
