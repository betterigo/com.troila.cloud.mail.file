package com.troila.cloud.mail.file.component;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.troila.cloud.mail.file.config.constant.SecureKey;

@Component
public class SecureConverter {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Cipher enCipher;
	
	private Cipher deCipher;
	
	public SecureConverter() {
		super();
		try {
			logger.info("初始化对象字段加密组件...");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(SecureKey.KEY.getBytes());
			KeyGenerator gen = KeyGenerator.getInstance("AES");
			gen.init(128, random);
			SecretKey key = gen.generateKey();
			byte[] enCode = key.getEncoded();
			SecretKeySpec encodeKey = new SecretKeySpec(enCode, "AES");
			enCipher = Cipher.getInstance("AES");
			deCipher = Cipher.getInstance("AES");
			enCipher.init(Cipher.ENCRYPT_MODE, encodeKey);
			deCipher.init(Cipher.DECRYPT_MODE, encodeKey);
			logger.info("初始化对象字段加密组件...ok!");
		} catch (NoSuchAlgorithmException e) {
			logger.error("初始化对象字段加密组件...fail!",e);
		} catch (NoSuchPaddingException e) {
			logger.error("初始化对象字段加密组件...fail!",e);
		} catch (InvalidKeyException e) {
			logger.error("初始化对象字段加密组件...fail!",e);
		}
	}

	public byte[] encode(byte[] content) throws IllegalBlockSizeException, BadPaddingException{
		return enCipher.doFinal(content);
	}
	
	public byte[] decode(byte[] content) throws IllegalBlockSizeException, BadPaddingException {
		return deCipher.doFinal(content);
	}
}
