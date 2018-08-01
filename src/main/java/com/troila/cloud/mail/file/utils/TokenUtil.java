package com.troila.cloud.mail.file.utils;

import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

public class TokenUtil {
	public static String getToken() {
		UUID uuid = UUID.randomUUID();
		return DigestUtils.md5Hex(uuid.toString());
	}
}
