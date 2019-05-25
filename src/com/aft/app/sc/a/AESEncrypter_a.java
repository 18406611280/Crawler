package com.aft.app.sc.a;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.aft.app.sc.huicent.utils.Base64;

public class AESEncrypter_a {
	public static String a(String arg4, String arg5) throws Exception {
		String string0;
		try {
			Cipher cipher0 = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher0.init(1, new SecretKeySpec(arg5.getBytes(), "AES"));
			string0 = Base64
					.encodeToString(cipher0.doFinal(arg4.getBytes()), 0);

		} catch (Exception exception0) {
			exception0.printStackTrace();
			string0 = null;
		}

		return string0;
	}
}
