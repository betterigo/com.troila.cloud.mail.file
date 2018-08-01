package com.troila.cloud.mail.file.model.fenum;

public enum FileType {
	VEDIO("vedio"),
	AUDIO("audio"),
	PICTURE("picture"),
	DOCUMENT("document"),
	APPLICATION("application"),
	OTHER("other");
	private String value;
	FileType(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
