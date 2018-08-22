package com.troila.cloud.mail.file.utils;

import java.util.Random;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

public class TokenUtil {
	public static String getToken() {
		UUID uuid = UUID.randomUUID();
		return DigestUtils.md5Hex(uuid.toString());
	}
	
	public static String getShortKey() {
		  String str="zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
		        Random random=new Random();  
		        StringBuffer sb=new StringBuffer();
		        for(int i=0; i<5; ++i){
		          int number=random.nextInt(62);
		          sb.append(str.charAt(number));
		        }
		        return sb.toString();
	}
}
