package com.aft.utils.http;

import java.util.Map;

public class MyHttpClientSessionVo {

	private MyHttpClientSession httpClientSession;
	
	private String cookieRemark;
	
	private String parentCookieRemark;
	
	private Map<String, Object> headerMap;
	
	public MyHttpClientSessionVo(MyHttpClientSession httpClientSession) {
		this(httpClientSession, null);
	}
	
	public MyHttpClientSessionVo(MyHttpClientSession httpClientSession, Map<String, Object> headerMap) {
		this.httpClientSession = httpClientSession;
		this.headerMap = headerMap;
	}

	public MyHttpClientSessionVo(MyHttpClientSession httpClientSession, String cookieRemark, String parentCookieRemark) {
		this(httpClientSession, cookieRemark, parentCookieRemark, null);
	}

	public MyHttpClientSessionVo(MyHttpClientSession httpClientSession, String cookieRemark, String parentCookieRemark,
			Map<String, Object> headerMap) {
		this.httpClientSession = httpClientSession;
		this.cookieRemark = cookieRemark;
		this.parentCookieRemark = parentCookieRemark;
		this.headerMap = headerMap;
	}

	public MyHttpClientSession getHttpClientSession() {
		return httpClientSession;
	}

	public void setHttpClientSession(MyHttpClientSession httpClientSession) {
		this.httpClientSession = httpClientSession;
	}

	public String getCookieRemark() {
		return cookieRemark;
	}

	public void setCookieRemark(String cookieRemark) {
		this.cookieRemark = cookieRemark;
	}

	public String getParentCookieRemark() {
		return parentCookieRemark;
	}

	public void setParentCookieRemark(String parentCookieRemark) {
		this.parentCookieRemark = parentCookieRemark;
	}

	public Map<String, Object> getHeaderMap() {
		return headerMap;
	}

	public void setHeaderMap(Map<String, Object> headerMap) {
		this.headerMap = headerMap;
	}
}