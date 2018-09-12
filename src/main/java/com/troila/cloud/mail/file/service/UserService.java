package com.troila.cloud.mail.file.service;

import com.troila.cloud.mail.file.exception.UserSettingsException;
import com.troila.cloud.mail.file.model.UserSettings;

public interface UserService {
	public UserSettings updateSettings(UserSettings userSettings) throws UserSettingsException;
}
