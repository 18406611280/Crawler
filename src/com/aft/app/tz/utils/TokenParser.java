package com.aft.app.tz.utils;

public class TokenParser {

	public static String parse(String token){ //����token
		int begin = token.indexOf("token");
		begin += 8;
		int end = token.length() - 2;
		String res = token.substring(begin, end);
		return res;
	}
}
