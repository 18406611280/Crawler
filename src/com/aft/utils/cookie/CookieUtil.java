package com.aft.utils.cookie;

public class CookieUtil {
	
	public static String addCookies(String ...strs) {
		StringBuilder sb = new StringBuilder();
		for(String s :strs) {
			sb.append(s).append(";");
		}
		return sb.toString();
	}

}
