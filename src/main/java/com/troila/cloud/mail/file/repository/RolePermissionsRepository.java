package com.troila.cloud.mail.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.troila.cloud.mail.file.model.RolePermissions;

public interface RolePermissionsRepository extends JpaRepository<RolePermissions, Integer>{
	
	List<RolePermissions> findByRoleId(int roleId);

}
