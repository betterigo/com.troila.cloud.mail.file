package com.troila.cloud.mail.file.model.fenum;

public enum FolderAuth {
	READ("r"),
	WRITE("w"),
	MODIFY("m"),
	DELETE("d"),
	RW("rw"),
	RWM("rwm"),
	RWMD("rwmd");
	private String value;
	FolderAuth(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
