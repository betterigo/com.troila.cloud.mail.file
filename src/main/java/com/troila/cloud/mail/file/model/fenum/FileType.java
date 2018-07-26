package com.troila.cloud.mail.file.model.fenum;

public enum FileType {
	VEDIO(1),
	AUDIO(2),
	PICTURE(3),
	DOCUMENT(4),
	APPLICATION(5),
	OTHER(6);
	private int value;
	FileType(int value) {
		this.value = value;
	}
	public int getValue() {
		return value;
	}
}
