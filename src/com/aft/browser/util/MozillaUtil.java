package com.aft.browser.util;

import org.apache.log4j.Logger;
import org.mozilla.xpcom.Mozilla;

public class MozillaUtil {
	
	private final static Logger logger = Logger.getLogger(MozillaUtil.class);
	
	
	/**
	 * 设置ip代理
	 * @param proxyIp
	 * @param proxyPort
	 * 
	 * @return
	 */
	public static boolean setMozillaProxy(String proxyIp, int proxyPort) {
		try {
			Mozilla mozilla = Mozilla.getInstance();
			org.mozilla.interfaces.nsIServiceManager serviceManager = mozilla.getServiceManager();
			
			String contractID = "@mozilla.org/preferences-service;1";
			org.mozilla.interfaces.nsIPrefService prefService = (org.mozilla.interfaces.nsIPrefService)serviceManager.getServiceByContractID(contractID, org.mozilla.interfaces.nsIPrefService.NS_IPREFSERVICE_IID); // 3
			org.mozilla.interfaces.nsIPrefBranch branch = prefService.getBranch("");
			
			branch.setIntPref("network.proxy.type", 1);
			branch.setCharPref("network.proxy.http", proxyIp);
			branch.setIntPref("network.proxy.http_port", proxyPort);
			return true;
		} catch(Exception e) {
			logger.error("Mozilla set proxy exception:\r", e);
		}
		return false;
	}
	
	/**
	 * 设置 userAgent
	 * 
	 * Chrome Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36
	 * 
	 * @param userAgent
	 * 
	 * @return
	 */
	public static boolean setMozillaUserAgent(String userAgent) {
		try {
			Mozilla mozilla = Mozilla.getInstance();
			org.mozilla.interfaces.nsIServiceManager serviceManager = mozilla.getServiceManager();
			
			String contractID = "@mozilla.org/preferences-service;1";
			org.mozilla.interfaces.nsIPrefService prefService = (org.mozilla.interfaces.nsIPrefService)serviceManager.getServiceByContractID(contractID, org.mozilla.interfaces.nsIPrefService.NS_IPREFSERVICE_IID); // 3
			org.mozilla.interfaces.nsIPrefBranch branch = prefService.getBranch("");
			branch.setCharPref("general.useragent.override", userAgent);
		} catch(Exception e) {
			logger.error("Mozilla set userAgent exception:\r", e);
		}
		return false;
	}

	public static void setMozillaUserRedirect() {
		try {
			Mozilla mozilla = Mozilla.getInstance();
			org.mozilla.interfaces.nsIServiceManager serviceManager = mozilla.getServiceManager();
			
			String contractID = "@mozilla.org/preferences-service;1";
			org.mozilla.interfaces.nsIPrefService prefService = (org.mozilla.interfaces.nsIPrefService)serviceManager.getServiceByContractID(contractID, org.mozilla.interfaces.nsIPrefService.NS_IPREFSERVICE_IID); // 3
			org.mozilla.interfaces.nsIPrefBranch branch = prefService.getBranch("");
			
			branch.setBoolPref("network.websocket.auto-follow-http-redirects",0);
			branch.setBoolPref("network.http.prompt-temp-redirect",0);
			branch.setBoolPref("browser.search.redirectWindowsSearch",0);
			
		} catch(Exception e) {
			logger.error("Mozilla set redirect exception:\r", e);
		}
	}
}
