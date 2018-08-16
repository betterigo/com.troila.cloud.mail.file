package com.troila.cloud.mail.file.utils;

import javax.servlet.http.HttpSession;

public class RedisValueManager {
	public static void updateUserInfo(HttpSession session) {
		session.setAttribute("sync-user", true);
	}
}
