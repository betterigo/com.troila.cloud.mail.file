package com.troila.cloud.mail.file.model.fenum;

public enum FileType {
	FOLDER("folder"),//文件夹类型
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
