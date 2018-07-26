package com.troila.cloud.mail.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.User;

public interface UserRepository extends JpaRepository<User, Integer>{

}
