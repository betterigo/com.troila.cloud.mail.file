package com.troila.cloud.mail.file.model.fenum;

public enum GroupUserRole {
	ADMIN("admin"),
	MANAGER("manager"),
	MEMBER("member");
	private String value;
	GroupUserRole(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
