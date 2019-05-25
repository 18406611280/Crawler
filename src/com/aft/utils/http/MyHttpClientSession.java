package com.aft.utils.http;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import com.aft.utils.MyStringUtil;

public class MyHttpClientSession {
	
	private final static Logger logger = Logger.getLogger(MyHttpClientSession.class);
	
	private int connTimeout = MyHttpClientUtil.defaultConnTimeout;
	
	private int soTimeout = MyHttpClientUtil.defaultSoTimeout;
	
	private String charset = MyHttpClientUtil.defaultCharset;
	
	private Map<String, String> commonHeaders = new HashMap<String, String>();
	
	private Map<String, Set<String>> cookieMap = new HashMap<String, Set<String>>();
	
	private CloseableHttpClient httpClient;
	
	public MyHttpClientSession() {
		this(null);
	}
	
	public MyHttpClientSession(CloseableHttpClient httpClient) {
		if(null == httpClient) httpClient = HttpClients.createDefault();
		this.httpClient = httpClient;
	}
	
	/**
	 * 
	 * @param url
	 * @param paramMap
	 * @param proxyIp
	 * @param proxyPort
	 * @return
	 * @throws Exception
	 */
	public String httpPost(String url, Map<String, Object> paramMap, String proxyIp, Integer proxyPort) throws Exception {
		return this.http(url, MyHttpClientUtil.httpPost, paramMap, null, null, null, null, proxyIp, proxyPort, connTimeout, soTimeout, charset);
	}
	
	
	/**
	 * 
	 * @param url
	 * @param httpContent
	 * @param proxyIp
	 * @param proxyPort
	 * @return
	 * @throws Exception
	 */
	public String httpPost(String url, String httpContent, String proxyIp, Integer proxyPort) throws Exception {
		return this.http(url, MyHttpClientUtil.httpPost, null, httpContent, null, null, null, proxyIp, proxyPort, connTimeout, soTimeout, charset);
	}
	
	/**
	 * 
	 * @param url
	 * @param proxyIp
	 * @param proxyPort
	 * @return
	 * @throws Exception
	 */
	public String httpGet(String url, String proxyIp, Integer proxyPort) throws Exception {
		return this.http(url, MyHttpClientUtil.httpGet, null, null, null, null, null, proxyIp, proxyPort, connTimeout, soTimeout, charset);
	}
	
	/**
	 * 
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param cookieRemark
	 * @param parentCookieRemark
	 * @param headerMap
	 * @param proxyIp
	 * @param proxyPort
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public String http(String url, String sendType, Map<String, Object> paramMap, String httpContent, String cookieRemark, String parentCookieRemark,
			Map<String, Object> headerMap, String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		return this.httpResultVo(url, sendType, paramMap, httpContent,
				cookieRemark, parentCookieRemark, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset).getHttpResult();
	}
	
	/**
	 * 
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param cookieRemark
	 * @param parentCookieRemark
	 * @param headerMap
	 * @param proxyIp
	 * @param proxyPort
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public MyHttpClientResultVo httpResultVo(String url, String sendType, Map<String, Object> paramMap, String httpContent,
			String cookieRemark, String parentCookieRemark, Map<String, Object> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		Map<String, Object> sendHeaderMap = new HashMap<String, Object>(commonHeaders);
		if(null != headerMap && !headerMap.isEmpty()) sendHeaderMap.putAll(headerMap);
		
		// 添加cookie到header
		Set<String> cookies = this.cookieMap.get(parentCookieRemark);
		if(null != cookies && !cookies.isEmpty()) sendHeaderMap.put("Cookie", sendHeaderMap.get("Cookie") + ";" + MyStringUtil.spliceValueRepeat(cookies, ";"));
		
		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(this.httpClient, url, sendType, paramMap, httpContent, sendHeaderMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
		
		if(StringUtils.isNotEmpty(cookieRemark)) {
			Set<String> addCookies = this.getCookieSet(vo.getHeaders());
			if(null != addCookies && !addCookies.isEmpty()) {
				if(null != cookies && !cookies.isEmpty()) addCookies.addAll(cookies);
			}
			this.cookieMap.put(cookieRemark, addCookies);
		}
		return vo;
	}
	
	/**
	 * 还原初始属性
	 */
	public void clearDefaultProp() {
		if(null == httpClient) return ;
//		this.connTimeout = MyHttpClientUtil.defaultConnTimeout;
//		this.soTimeout = MyHttpClientUtil.defaultSoTimeout;
//		this.charset = MyHttpClientUtil.defaultCharset;
//		this.commonHeaders = new HashMap<String, String>();
//		this.cookieMap = new HashMap<String, Set<String>>();
		this.httpClient = HttpClients.createDefault();
		this.commonHeaders.clear();
		this.cookieMap.clear();
	}
	
	/**
	 * 关闭
	 */
	public void close() {
		if(null == httpClient) return ;
		try {
			httpClient.close();
		} catch(Exception e) {
			logger.error("MyHttpClientSession 关闭异常:\r", e);
		}
	}
	
	/**
	 * 设置新增 cookie
	 * @param headers
	 */
	public Set<String> getCookieSet(Header[] headers) {
		Set<String> cookies = new HashSet<String>();
		if(null == headers || 0 == headers.length) return cookies;
		for(Header header : headers) {
			if(!"Set-Cookie".equals(header.getName())) continue ;
			String[] values = header.getValue().trim().split(";");
			for(String value : values) {
				if(StringUtils.isEmpty(value) || value.contains("Path") || value.contains("expires")) continue ;
				cookies.add(value.trim());
				break ;
			}
		}
		return cookies;
	}

	public int getConnTimeout() {
		return connTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public String getCharset() {
		return charset;
	}
}