package com.aft.browser;

public interface OrderBrowserInterface {
	
	void proxy(String proxyIp, int proxyPort) throws Exception;
	
	String getBrowserText(String itemKey);
	
	String getBrowserUrl(String itemKey);
	
	Object evaluateJs(String itemKey, String jsCode);
	
	boolean executeJs(String itemKey, String jsCode);
	
	void setBrowserUrl(String itemKey, String url);
	
	void refresh(String itemKey);
}