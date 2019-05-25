package com.aft.utils.adsl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.utils.cmd.MyCmdUtil;
import com.aft.utils.regex.MyRegexUtil;

/**
 * ADSL工具
 *
 */
public class MyADSLUtil {

	private final static Logger logger = Logger.getLogger(MyADSLUtil.class);
	
	/** 是否拨号中 */
	private static boolean connecting = false;
	
	/** maxWaitTime 内 只能拨号一次 */
	private static long maxWaitTime = 30000;
	
	/** 拨号开始时间 */
	private static long startTime = 0L;
	
	/**
	 * 连接ADSL
	 * @param adslTitle 宽带连接名称
	 * @param adslName 用户名
	 * @param adslPass 密码
	 * @return
	 * @throws Exception
	 */
	private synchronized static boolean connAdsl(String adslTitle, String adslName, String adslPass) throws Exception {
		logger.info("ADSL 正在建立连接 >>>>> 宽带连接名称: " + adslTitle);
		String adslCmd = "rasdial " + adslTitle + " " + adslName + " " + adslPass;
		String result = MyCmdUtil.execCmd(adslCmd);
		logger.info("ADSL 建立连接 >>>>> 宽带连接名称: " + adslTitle + ", msg:" + result);
		return result.contains("已连接");
	}

	/**
	 * 断开ADSL
	 * @param adslTitle 宽带连接名称
	 * @return
	 * @throws Exception
	 */
	private synchronized static boolean cutAdsl(String adslTitle) throws Exception {
		logger.info("ADSL 正在断开连接 >>>>> 宽带连接名称: " + adslTitle);
		String cutAdsl = "rasdial " + adslTitle + " /disconnect";
		String result = MyCmdUtil.execCmd(cutAdsl);
		logger.info("ADSL 断开连接 >>>>> 宽带连接名称: " + adslTitle + ", msg: " + result);
		return !result.contains("没有连接");
	}
	
	/**
	 * 切换ip null: 已在拨号中..., true/false 成功/失败
	 * @param adslTitle
	 * @param adslUsername
	 * @param adslPassword
	 * @param networkName
	 * @return
	 */
	public static final Boolean changeIp(String adslTitle, String adslUsername, String adslPassword, String networkName) {
		synchronized(MyADSLUtil.class) {
			if(connecting || System.currentTimeMillis() - startTime <= maxWaitTime) return null;	// 正在拨号中...
			connecting = true;
		}
		try {
			startTime = System.currentTimeMillis();
			String currentIp = MyADSLUtil.getPPPOEIP(networkName);
			logger.info("ADSL 当前ip [" + currentIp + "],开始切换ip...");
			MyADSLUtil.cutAdsl(adslTitle);	// 先断开连接
			boolean ok = MyADSLUtil.connAdsl(adslTitle, adslUsername, adslPassword);
			if(ok) logger.info("ADSL 切换ip成功, 切换后ip [" + MyADSLUtil.getPPPOEIP(networkName) + "], 耗时:" + (System.currentTimeMillis() - startTime) + "毫秒");
			else logger.info("ADSL 切换ip失败, 耗时:" + (System.currentTimeMillis() - startTime) + "毫秒");
			return ok;
		} catch (Exception e) {
			logger.error("ADSL ip切换异常---->>>>>>>>>>>>>>\r", e);
		} finally {
			connecting = false;
		}
		return false;
	}
	
	/**
	 * 获取pppoe ip
	 * @param networkName
	 * @return
	 */
	public static String getPPPOEIP(String networkName) {
		String pppoeIp = MyADSLUtil.getPPPOEAddress();
		if(StringUtils.isEmpty(pppoeIp)) pppoeIp = MyADSLUtil.getPPPOEAddress(networkName);
		return pppoeIp;
	}
	
	/**
	 * ipconfig 匹配方式获取
	 * @return
	 */
	public static String getPPPOEAddress() {
		try {
			String ipconfig = MyCmdUtil.execCmd("ipconfig");
			Matcher matcher = MyRegexUtil.ipconfiIpPattern.matcher(ipconfig);
			if(matcher.find()) return matcher.group(1);
		} catch(Exception e) {
			logger.error("ipconfig方式获取 pppoe 公网地址异常: ", e);
		}
		return null;
	}
	
	/**
	 * 返回 pppoe 公网地址
	 * 
	 * @param networkName
	 */
	public static String getPPPOEAddress(String networkName) {
		if(StringUtils.isEmpty(networkName)) return null;
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while(nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				if(!networkName.equals(ni.getDisplayName())) continue;
				Enumeration<InetAddress> ids = ni.getInetAddresses();
				while(ids.hasMoreElements()) {
					InetAddress id = ids.nextElement();
					if(id instanceof Inet4Address) return id.getHostAddress();
				}
			}
		} catch(Exception e) {
			logger.error("网卡方式获取 pppoe 公网地址异常: ", e);
		}
		return null;
	}
}