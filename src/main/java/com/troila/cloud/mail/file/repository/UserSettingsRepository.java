package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.UserSettings;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Integer>{
	public UserSettings findByUid(int uid);
}
