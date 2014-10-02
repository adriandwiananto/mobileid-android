package com.example.mobileid;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.util.Base64;

public class Converter {
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	@SuppressLint("DefaultLocale")
	public static String sha256Hmac(String key, String data) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")), "HmacSHA256");
		sha256_HMAC.init(secret_key);

		return Converter.bytesToHex(sha256_HMAC.doFinal(data.toLowerCase().getBytes())).toLowerCase();
	}

	public static String sha256Hash(String text) {
		MessageDigest digest=null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		digest.reset();
		return Converter.bytesToHex(digest.digest(text.getBytes())).toLowerCase();
	}
}
