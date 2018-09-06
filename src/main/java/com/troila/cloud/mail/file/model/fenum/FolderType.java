package com.troila.cloud.mail.file.model.fenum;

public enum FolderType {
	ROOT("root"),
	DEFAULT("default"),
	CUSTOM("custom"),
	VEDIO("vedio"),
	AUDIO("audio"),
	PICTURE("picture"),
	DOCUMENT("document"),
	APPLICATION("application"),
	OTHER("other");
	private String value;
	FolderType(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
