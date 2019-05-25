package com.aft.utils.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class MyRegexUtil {
	
	/** 数字正则(全局) */
	public final static String allNumberRegex = "^\\d+$";
	
	/** 数字正则(全局) */
	public final static Pattern allNumberPattern = Pattern.compile("^\\d+$");
	
	
	
	/** 数字正则(匹配局部) */
	public final static String numberRegex = "\\d";
	
	/** 数字正则(匹配局部) */
	public final static Pattern numberPattern = Pattern.compile("\\d");
	
	
	
	/** 字母正则(匹配局部) */
	public final static String letterRegex = "[A-Z]";
	
	/** 字母正则(匹配局部) */
	public final static Pattern letterPattern = Pattern.compile("[A-Z]");
	
	
	
	/**  舱位 YY1ZZ1 */
	public final static String cabinRegex = "[A-Z]\\d?";
	
	/**  舱位 YY1ZZ1 */
	public final static Pattern cabinPattern = Pattern.compile("[A-Z]\\d?");
	
	
	
	/** ipconfig 结果匹配 公网ip */
	public final static String ipconfiIpRegex = "PPP(?:\\s|\\S)+?(?:IPv4|IP Address).+?(\\d+\\.\\d+\\.\\d+\\.\\d+)(?:\\s|\\S)+?(?:子网掩码|Subnet Mask)";
	
	/** ipconfig 结果匹配 公网ip */
	public final static Pattern ipconfiIpPattern = Pattern.compile("PPP(?:\\s|\\S)+?(?:IPv4|IP Address).+?(\\d+\\.\\d+\\.\\d+\\.\\d+)(?:\\s|\\S)+?(?:子网掩码|Subnet Mask)");
	
	/**
	 * 查找全部
	 * @param value
	 * @param pattern
	 * @return
	 */
	public static String findAll(String value, Pattern pattern) {
		if(StringUtils.isEmpty(value)) return null;
		Matcher matcher = pattern.matcher(value);
		StringBuffer sb = new StringBuffer("");
		while(matcher.find()) {
			sb.append(matcher.group());
		}
		return sb.toString();
	}
}