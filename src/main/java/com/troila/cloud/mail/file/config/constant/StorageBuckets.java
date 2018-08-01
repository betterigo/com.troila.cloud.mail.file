package com.troila.cloud.mail.file.config.constant;

import com.troila.cloud.mail.file.model.fenum.FileType;

public abstract class StorageBuckets {
	
	public static final String VEDIO = FileType.VEDIO.getValue();
	
	public static final String AUDIO = FileType.AUDIO.getValue();
	
	public static final String PICTURE = FileType.PICTURE.getValue();
	
	public static final String DOCUMENT = FileType.DOCUMENT.getValue();
	
	public static final String APPLICATION = FileType.APPLICATION.getValue();
	
	public static final String OTHER = FileType.OTHER.getValue();
	
	public static final String[] buckets = new String[] {
			VEDIO,
			AUDIO,
			PICTURE,
			DOCUMENT,
			APPLICATION,
			OTHER
	};
	
}
