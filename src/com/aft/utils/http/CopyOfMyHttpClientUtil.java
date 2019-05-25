package com.aft.utils.http;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import com.aft.utils.file.MyFileUtils;
import com.aft.utils.thread.MyThreadUtils;


public class CopyOfMyHttpClientUtil {
	
	public final static Logger logger = Logger.getLogger(CopyOfMyHttpClientUtil.class);
	
	private static PoolingHttpClientConnectionManager poolConnManager = new PoolingHttpClientConnectionManager();
	
	public final static int defaultConnTimeout = 2000;
	
	public final static int defaultSoTimeout = 30 * 1000;
	
	public final static String defaultCharset = "UTF-8";
	
	public final static String httpGet = "GET";
	
	public final static String httpPost = "POST";
	
	static {
//		poolConnManager.setMaxTotal(1);
//		poolConnManager.setDefaultMaxPerRoute(11);
	}
	
	public static void main(String[] args) throws Exception {
		for(int i=0; i<50; i++) 
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						get("http://www.baidu.com");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}).start();
	}
	
	/**
	 * 提交 post http 请求
	 * 
	 * @param apiURL
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public static String post(String url, Map<String, String> paramMap) throws Exception {
		return CopyOfMyHttpClientUtil.httpClient(url, CopyOfMyHttpClientUtil.httpPost, paramMap, null, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
	}
	
	/**
	 * 提交 get 请求
	 * @param apiURL
	 * @return
	 * @throws Exception 
	 */
	public static String get(String url) throws Exception {
		return CopyOfMyHttpClientUtil.httpClient(url, CopyOfMyHttpClientUtil.httpGet, null, null, null, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
	}
	
	/**
	 * 提交 get http 代理 请求
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static MyHttpClientResultVo httpClientGet(String url) throws Exception {
		return CopyOfMyHttpClientUtil.httpClient(null, url, CopyOfMyHttpClientUtil.httpGet, null, null, null, null, null,
				CopyOfMyHttpClientUtil.defaultConnTimeout, CopyOfMyHttpClientUtil.defaultSoTimeout, CopyOfMyHttpClientUtil.defaultCharset);
	}
	
	/**
	 * 提交 get http 代理 请求
	 * @param url
	 * @param headerMap
	 * @return
	 * @throws Exception
	 */
	public static MyHttpClientResultVo httpClientGet(String url, Map<String, String> headerMap) throws Exception {
		return CopyOfMyHttpClientUtil.httpClient(null, url, CopyOfMyHttpClientUtil.httpGet, null, null, headerMap,
				null, null, CopyOfMyHttpClientUtil.defaultConnTimeout, CopyOfMyHttpClientUtil.defaultSoTimeout, CopyOfMyHttpClientUtil.defaultCharset);
	}
	
	/**
	 * 提交 get/post http 代理 请求
	 * @param url
	 * @param sendType GET/POST
	 * @param paramMap
	 * @param headerMap
	 * @return
	 * @throws Exception
	 */
	public static String httpClient(String url, String sendType, Map<String, String> paramMap, Map<String, String> headerMap) throws Exception {
		return CopyOfMyHttpClientUtil.httpClient(url, sendType, paramMap, null, headerMap, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
	}
	
	/**
	 * 提交 get/post http 代理 请求
	 * @param url
	 * @param sendType GET/POST
	 * @param paramMap
	 * @param headerMap
	 * @param proxyIp
	 * @param proxyPort
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String httpClient(String url, String sendType, Map<String, String> paramMap, Map<String, String> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		return CopyOfMyHttpClientUtil.httpClient(url, sendType, paramMap, null, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
	}
	
	/**
	 * 提交 get/post http 代理 请求
	 * @param url
	 * @param sendType GET/POST
	 * @param httpContent
	 * @param headerMap
	 * @param proxyIp
	 * @param proxyPort
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String httpClient(String url, String sendType, String httpContent, Map<String, String> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		MyHttpClientResultVo vo = CopyOfMyHttpClientUtil.httpClient(null, url, sendType, null, httpContent, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
		return null == vo ? null : vo.getHttpResult();
	}
	
	/**
	 * 提交 get/post http 代理 请求
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param proxyIp
	 * @param proxyPort
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String httpClient(String url, String sendType, Map<String, String> paramMap, String httpContent, Map<String, String> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		MyHttpClientResultVo vo = CopyOfMyHttpClientUtil.httpClient(null, url, sendType, paramMap, httpContent, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
		return null == vo ? null : vo.getHttpResult();
	}
	
	/**
	 * 提交 get/post http 代理 请求
	 * @param httpClientSession 如果传入对象, 则关闭交给调用者
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param proxyIp
	 * @param proxyPort
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public static MyHttpClientResultVo httpClient(CloseableHttpClient httpClient, String url, String sendType,
			Map<String, String> paramMap, String httpContent, Map<String, String> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		String httpResult = null;
		boolean closeFlag = true;
		if(null != httpClient) closeFlag = false;
		else httpClient = HttpClients.custom().setConnectionManager(poolConnManager).build();
		
		System.out.println(httpClient);
		MyThreadUtils.sleep(2000);
		
//		else httpClient = HttpClients.createDefault();
		Header[] headers = null;
		if(null != headerMap && !headerMap.isEmpty()) headers = CopyOfMyHttpClientUtil.toHeaders(headerMap);
		try {
			HttpRequestBase httpRequest = null;
			if(httpPost.equalsIgnoreCase(sendType)) {
				httpRequest = new HttpPost(url);
				CopyOfMyHttpClientUtil.setPostParam((HttpPost)httpRequest, paramMap, httpContent, charset);
			} else if(httpGet.equalsIgnoreCase(sendType)) httpRequest = new HttpGet(url);
			else return new MyHttpClientResultVo("只支持POST/GET", null);
			
			if(null != headers) httpRequest.setHeaders(headers);
			CopyOfMyHttpClientUtil.setRequestConfig(httpRequest, proxyIp, proxyPort, connTimeout, soTimeout);
			HttpResponse httpResponse = httpClient.execute(httpRequest);	// 提交get请求
			headers = httpResponse.getAllHeaders();
			
			// 处理编码,优先使用返回的...
			String charsetTemp = CopyOfMyHttpClientUtil.getHeaderValue(headers, "Content-Type", "charset=");
			if(StringUtils.isNotEmpty(charsetTemp)) charset = charsetTemp.split("=")[1].trim().toUpperCase();
			
			// 获得服务器相应html信息
			HttpEntity httpEntity = httpResponse.getEntity();
			if(null == httpEntity) return null;
			
//			String encoding = MyHttpClientUtil.getHeaderValue(headers, "Content-Encoding", "gzip");
//			if("gzip".equalsIgnoreCase(encoding)) httpEntity = new GzipDecompressingEntity(httpEntity);
			
			httpResult = MyFileUtils.getStringByInputStream(httpEntity.getContent(), charset);
//			httpResult = EntityUtils.toString(httpEntity, charset);
			httpRequest.releaseConnection();
//			if(closeFlag) httpClient.close();
		} catch(Exception e) {
			throw e;
		} finally {
//			if(closeFlag && null != httpClient) httpClient.close();
		}
		return new MyHttpClientResultVo(httpResult, headers);
	}
	
	/**
	 * 下载文件
	 * @param url
	 * @param file
	 * @param timeout
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public static void download(String url, Map<String, String> headerMap, String fullFileName, int timeout) throws Exception {
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();	// 实例化 httpclient
			Header[] headers = null;
			if(null != headerMap && !headerMap.isEmpty()) headers = CopyOfMyHttpClientUtil.toHeaders(headerMap);
			HttpGet httpGet = new HttpGet(url);
			if(null != headers) httpGet.setHeaders(headers);
			CopyOfMyHttpClientUtil.setRequestConfig(httpGet, CopyOfMyHttpClientUtil.defaultConnTimeout, timeout);
			HttpResponse response = httpClient.execute(httpGet);		// 提交get请求
			MyFileUtils.createFile(new File(fullFileName), response.getEntity().getContent());
			httpGet.releaseConnection();
			httpClient.close();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			if(null != httpClient) httpClient.close();
		}
	}
	
	/**
	 * 设置 post 参数
	 * @param httpPost
	 * @param paramMap
	 * @param httpContent
	 * @param charset
	 * @throws Exception
	 */
	public static void setPostParam(HttpPost httpPost, Map<String, String> paramMap, String httpContent, String charset) throws Exception {
		if(StringUtils.isNotEmpty(httpContent)) httpPost.setEntity(new StringEntity(httpContent, charset));
		if(null != paramMap && !paramMap.isEmpty()) {
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			Iterator<Entry<String, String>> it = paramMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, String> entry = it.next();
				paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(paramList, charset));
		}
	}
	
	/**
	 * 设置 代理, 超时
	 * @param requestBase
	 * @param connTimeout
	 * @param soTimeout
	 */
	public static void setRequestConfig(HttpRequestBase requestBase, int connTimeout, int soTimeout) {
		CopyOfMyHttpClientUtil.setRequestConfig(requestBase, null, null, connTimeout, soTimeout);
	}
	
	/**
	 * 设置 代理, 超时
	 * @param requestBase
	 * @param proxyIp
	 * @param proxyPort
	 * @param connTimeout
	 * @param soTimeout
	 */
	public static void setRequestConfig(HttpRequestBase requestBase, String proxyIp, Integer proxyPort, int connTimeout, int soTimeout) {
		if(null != proxyIp && null != proxyPort) requestBase.setConfig(RequestConfig.custom().setSocketTimeout(soTimeout).setConnectTimeout(connTimeout).setProxy(new HttpHost(proxyIp, proxyPort)).build());
		else requestBase.setConfig(RequestConfig.custom().setSocketTimeout(soTimeout).setConnectTimeout(connTimeout).build());
	}
	
	/**
	 * 转成 header
	 * @param headerMap
	 */
	public static Header[] toHeaders(Map<String, String> headerMap) {
		List<Header> tempHeaders = new ArrayList<Header>();
		if(null == headerMap || headerMap.isEmpty()) return null;
		Iterator<Map.Entry<String, String>> it = headerMap.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			tempHeaders.add(new BasicHeader(entry.getKey(), entry.getValue()));
		}
		return tempHeaders.toArray(new Header[tempHeaders.size()]);
	}
	
	/**
	 * 返回 header 中 headerName 中 headerValueKey
	 * @param headers
	 * @param headerName
	 * @param headerValueKey
	 * @return
	 */
	public static String getHeaderValue(Header[] headers, String headerName, String headerValueKey) {
		for(Header header : headers) {
			if(!header.getName().equals(headerName)) continue ;
			String[] values = header.getValue().trim().split(";");
			for(String value : values) {
				if(value.toUpperCase().contains(headerValueKey.toUpperCase())) return value.trim();
			}
		}
		return "";
	}
}