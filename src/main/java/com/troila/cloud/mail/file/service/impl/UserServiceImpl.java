package com.troila.cloud.mail.file.service.impl;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.troila.cloud.mail.file.exception.UserSettingsException;
import com.troila.cloud.mail.file.model.UserSettings;
import com.troila.cloud.mail.file.repository.UserSettingsRepository;
import com.troila.cloud.mail.file.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserSettingsRepository userSettingsRepository;
	
	@Secured("ROLE_USER_MANAGE")
	@Transactional
	@Override
	public UserSettings updateSettings(UserSettings userSettings) throws UserSettingsException {
		Optional<UserSettings> oldUserSettingsOpt = userSettingsRepository.findById(userSettings.getId());
		UserSettings old = oldUserSettingsOpt.orElseThrow(()->new UserSettingsException("未能找到与"+userSettings.getId()+"匹配的用户信息！"));
		if(userSettings.getDownloadSpeedLimit()>0) {
			old.setDownloadSpeedLimit(userSettings.getDownloadSpeedLimit());
		}
		if(userSettings.getMaxFileSize()>0) {
			old.setMaxFileSize(userSettings.getMaxFileSize());
		}
		if(userSettings.getShareVolume()>old.getShareVolume()) {//只能扩容
			old.setShareVolume(userSettings.getShareVolume());
		}
		if(userSettings.getVolume()>old.getVolume()) {//只能扩容
			old.setVolume(userSettings.getVolume());
		}
		if(userSettings.getUploadSpeedLimit()>0) {
			old.setUploadSpeedLimit(userSettings.getUploadSpeedLimit());
		}
		old.setGmtModify(new Date());
		UserSettings result = userSettingsRepository.save(old);
		return result;
	}

}
