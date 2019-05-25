package com.aft.browser.util;

import java.io.File;

import org.apache.log4j.Logger;

public class IeUtil {
	
	private final static Logger logger = Logger.getLogger(IeUtil.class);

	/**
	 * 设置ie代理
	 * @param proxyIp
	 * @param proxyPort
	 */
	public static void setProxy(String proxyIp, int proxyPort) {
		try {
			File proxy = new File("resource/Proxy");
			Runtime.getRuntime().exec("cmd /c " + proxy.getAbsolutePath() + " " + proxyIp + ":" + proxyPort);
		} catch (Exception e) {
			logger.error("IeUtil --> setProxy --> exception\r", e);
		}
	}
}