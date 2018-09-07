package com.troila.cloud.mail.file.component;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.troila.cloud.mail.file.config.settings.UserDefaultSettings;
import com.troila.cloud.mail.file.model.UserSettings;

@Component
@EnableConfigurationProperties(UserDefaultSettings.class)
public class UserDefaultSettingsGenerator {
	
	@Autowired
	private UserDefaultSettings userDefaultSettings;
	
	public UserSettings create() {
		UserSettings userSettings = new UserSettings();
		userSettings.setDownloadSpeedLimit(userDefaultSettings.getDownloadSpeedLong());
		userSettings.setUploadSpeedLimit(userDefaultSettings.getUploadSpeedLong());
		userSettings.setVolume(userDefaultSettings.getVolumeLong());
		userSettings.setMaxFileSize(userDefaultSettings.getMaxFileSizeLong());
		userSettings.setShareVolume(userDefaultSettings.getShareGroupVolumeLong());
		userSettings.setGmtCreate(new Date());
		return userSettings;
	}
}
