package com.chanapp.chanjet.customer.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * md5
 * 
 * @author zhailzh
 * 
 */
public class ISVMD5 {

	final static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * Encodes a string
	 * 
	 * @param str
	 *            String to encode
	 * @return Encoded String
	 */
	public static String crypt(String str) {
		return crypt(str, "UTF-8");
	}

	/**
	 * encode a string with given encoding
	 * 
	 * @param str
	 *            String to encode
	 * @param charSet
	 *            encoding
	 * @return the encode String
	 */
	public static String crypt(String str, String charSet) {
		if (str == null || str.length() == 0 || charSet == null) {
			throw new IllegalArgumentException("String or charset to encript cannot be null or zero length");
		}
		return getMD5(getBytes(str, charSet));
	}

	public static byte[] getBytes(String s, String encoding) {
		try {
			return s.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			return s.getBytes();
		}
	}

	public static String getMD5(byte[] source) {
		return crypt(source);
	}

	/**
	 * encode bytes
	 * 
	 * @param source
	 *            source bytes
	 * @return the encode String
	 */
	public static String crypt(byte[] source) {
		String s = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];

				str[k++] = hexDigits[byte0 & 0xf];
			}
			s = new String(str);

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		return s;
	}
}
