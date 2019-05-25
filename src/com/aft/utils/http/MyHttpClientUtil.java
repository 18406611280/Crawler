package com.aft.utils.http;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.aft.utils.file.MyFileUtils;


public class MyHttpClientUtil {
	
	public final static Logger logger = Logger.getLogger(MyHttpClientUtil.class);
	
	public final static int defaultConnTimeout = 50000;
	
	public final static int defaultSoTimeout = 30 * 1000;
	
	public final static String defaultCharset = "UTF-8";
	
	public final static String httpGet = "GET";
	
	public final static String httpPost = "POST";
	
	public final static int[] httpRedirectCode = new int[]{HttpStatus.SC_MOVED_TEMPORARILY,
															HttpStatus.SC_MOVED_PERMANENTLY,
															HttpStatus.SC_SEE_OTHER,
															HttpStatus.SC_TEMPORARY_REDIRECT};
	
	public static void main(String[] args) throws Exception {
//		File file = new File("D:\\myeclipse6.5\\workspace\\CrawlerRemote\\resource\\file\\result");
//		for(File f : file.listFiles()) {
//			String[] temps = f.getName().split("\\.")[0].split("-");
//			Map<String, String> parameMap = new HashMap<String, String>();
//			parameMap.put("type", temps[0]);
//			parameMap.put("version", temps[2]);
//			
//			File gzipFile = MyGzipUtil.gzipFile(f.getParent(), temps[0] + "-" + temps[2] + ".gz", f);
//			System.out.println(gzipFile.getPath());
//			String result = postForm("http://192.168.8.95:8080/aftInterface/collect/upload.do?username=fltchg&needKey=3201",
//						"jarFile", gzipFile.getPath(), gzipFile.getName(), parameMap);
//			System.out.println(result);
//		}
		//捷星发送MT测试
//		String postData = "[{\"FromCode\":\"AKL\",\"ToCode\":\"WLG\",\"FltNo\":\"JQ251\",\"Seat\":\"Y\",\"SeatCount\":\"10\",\"FltDate\":\"2016-12-12\",\"PriceFrom\":\"JQ\",\"FltTime\":\"2016-12-12 05:25:00\",\"ArrTime\":\"2016-12-12 06:30:00\",\"Tax\":\"0.0\",\"AddFee\":\"0\",\"YPrice\":\"335.3\",\"MoneyType\":\"CNY\",\"AdtPrice\":\"335.3\",\"YuanShiPrice\":\"335.3\",\"CNYPrice\":\"335.3\",\"PataPrice\":\"0\",\"PataRate\":\"0\",\"IsShare\":\"0\",\"Model\":\"\",\"PlaneSize\":\"\",\"MainFltNo\":\"\",\"AccuteRate\":\"0.0\",\"QneRank\":\"0\",\"IsTH\":\"0\",\"AdtAgentRate\":\"0\",\"CabinNote\":\"\",\"SignRule\":\"\",\"RefundRule\":\"\",\"EndorsRule\":\"\"},{\"FromCode\":\"AKL\",\"ToCode\":\"WLG\",\"FltNo\":\"JQ253\",\"Seat\":\"Y\",\"SeatCount\":\"10\",\"FltDate\":\"2016-12-12\",\"PriceFrom\":\"JQ\",\"FltTime\":\"2016-12-12 07:00:00\",\"ArrTime\":\"2016-12-12 08:05:00\",\"Tax\":\"0.0\",\"AddFee\":\"0\",\"YPrice\":\"675.46\",\"MoneyType\":\"CNY\",\"AdtPrice\":\"675.46\",\"YuanShiPrice\":\"675.46\",\"CNYPrice\":\"675.46\",\"PataPrice\":\"0\",\"PataRate\":\"0\",\"IsShare\":\"0\",\"Model\":\"\",\"PlaneSize\":\"\",\"MainFltNo\":\"\",\"AccuteRate\":\"0.0\",\"QneRank\":\"0\",\"IsTH\":\"0\",\"AdtAgentRate\":\"0\",\"CabinNote\":\"\",\"SignRule\":\"\",\"RefundRule\":\"\",\"EndorsRule\":\"\"},{\"FromCode\":\"AKL\",\"ToCode\":\"WLG\",\"FltNo\":\"JQ257\",\"Seat\":\"Y\",\"SeatCount\":\"10\",\"FltDate\":\"2016-12-12\",\"PriceFrom\":\"JQ\",\"FltTime\":\"2016-12-12 10:10:00\",\"ArrTime\":\"2016-12-12 11:15:00\",\"Tax\":\"0.0\",\"AddFee\":\"0\",\"YPrice\":\"335.3\",\"MoneyType\":\"CNY\",\"AdtPrice\":\"335.3\",\"YuanShiPrice\":\"335.3\",\"CNYPrice\":\"335.3\",\"PataPrice\":\"0\",\"PataRate\":\"0\",\"IsShare\":\"0\",\"Model\":\"\",\"PlaneSize\":\"\",\"MainFltNo\":\"\",\"AccuteRate\":\"0.0\",\"QneRank\":\"0\",\"IsTH\":\"0\",\"AdtAgentRate\":\"0\",\"CabinNote\":\"\",\"SignRule\":\"\",\"RefundRule\":\"\",\"EndorsRule\":\"\"},{\"FromCode\":\"AKL\",\"ToCode\":\"WLG\",\"FltNo\":\"JQ263\",\"Seat\":\"Y\",\"SeatCount\":\"10\",\"FltDate\":\"2016-12-12\",\"PriceFrom\":\"JQ\",\"FltTime\":\"2016-12-12 16:45:00\",\"ArrTime\":\"2016-12-12 17:50:00\",\"Tax\":\"0.0\",\"AddFee\":\"0\",\"YPrice\":\"335.3\",\"MoneyType\":\"CNY\",\"AdtPrice\":\"335.3\",\"YuanShiPrice\":\"335.3\",\"CNYPrice\":\"335.3\",\"PataPrice\":\"0\",\"PataRate\":\"0\",\"IsShare\":\"0\",\"Model\":\"\",\"PlaneSize\":\"\",\"MainFltNo\":\"\",\"AccuteRate\":\"0.0\",\"QneRank\":\"0\",\"IsTH\":\"0\",\"AdtAgentRate\":\"0\",\"CabinNote\":\"\",\"SignRule\":\"\",\"RefundRule\":\"\",\"EndorsRule\":\"\"},{\"FromCode\":\"AKL\",\"ToCode\":\"WLG\",\"FltNo\":\"JQ267\",\"Seat\":\"Y\",\"SeatCount\":\"10\",\"FltDate\":\"2016-12-12\",\"PriceFrom\":\"JQ\",\"FltTime\":\"2016-12-12 18:45:00\",\"ArrTime\":\"2016-12-12 19:50:00\",\"Tax\":\"0.0\",\"AddFee\":\"0\",\"YPrice\":\"335.3\",\"MoneyType\":\"CNY\",\"AdtPrice\":\"335.3\",\"YuanShiPrice\":\"335.3\",\"CNYPrice\":\"335.3\",\"PataPrice\":\"0\",\"PataRate\":\"0\",\"IsShare\":\"0\",\"Model\":\"\",\"PlaneSize\":\"\",\"MainFltNo\":\"\",\"AccuteRate\":\"0.0\",\"QneRank\":\"0\",\"IsTH\":\"0\",\"AdtAgentRate\":\"0\",\"CabinNote\":\"\",\"SignRule\":\"\",\"RefundRule\":\"\",\"EndorsRule\":\"\"}]";
//		logger.info("保存信息: " + postData);
//		Map<String, Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("ACTION", "UpResult");
//		paramMap.put("data", postData);
//		String httpResult = MyHttpClientUtil.post("http://192.168.2.200/AFTService.aspx", paramMap, "GBK");
//		logger.info("保存返回: " + httpResult);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("adults","1");
		paramMap.put("arrivalStation","SIN");
		paramMap.put("children","0");
		paramMap.put("currency","CNY");
		paramMap.put("departureDate","2016-12-20");
		paramMap.put("departureStation","CAN");
		paramMap.put("infants","0");
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
		String httpResult = MyHttpClientUtil.post("https://m.tigerair.com/booking/search", paramMap, headerMap);
		logger.info("保存返回: " + httpResult);
	}
	
	/**
	 * 提交流./参数
	 * @param url
	 * @param fileParam
	 * @param fullFileName
	 * @param fileName
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public static String postForm(String url, String fileParam, String fullFileName, String fileName, Map<String, String> paramMap) throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(url);
			MultipartEntityBuilder entity = MultipartEntityBuilder.create();
			entity.addPart(fileParam, new FileBody(new File(fullFileName)));
			if(null != paramMap) {
				Iterator<Entry<String, String>> it = paramMap.entrySet().iterator();
				while(it.hasNext()) {
					Entry<String, String> entry = it.next();
					entity.addPart(entry.getKey(), new StringBody(entry.getValue(), ContentType.DEFAULT_TEXT));
				}
			}
			httpPost.setEntity(entity.build());
			HttpResponse httpResponse = httpClient.execute(httpPost);
			String httpResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
			httpPost.releaseConnection();
			return httpResult;
		} catch(Exception e) {
			throw e;
		} finally {
			httpClient.close();
		}
	}
	
	
	/**
	 * 提交 post http 请求
	 * @param apiURL
	 * @param paramMap
	 * @return
	 * @throws Exception 
	 */
	public static String post(String url, Map<String, Object> paramMap) throws Exception {
		return MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpPost, paramMap, null, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
	}
	
	/**
	 * 提交 post http 请求
	 * @param apiURL
	 * @param paramMap
	 * @param charset
	 * @return
	 * @throws Exception 
	 */
	public static String post(String url, Map<String, Object> paramMap, String charset) throws Exception {
		return MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpPost, paramMap, null, null, null, defaultConnTimeout, defaultSoTimeout, charset);
	}
	
	/**
	 * 提交 post http 请求
	 * @param apiURL
	 * @param paramMap
	 * @param headerMap
	 * @return
	 * @throws Exception 
	 */
	public static String post(String url, Map<String, Object> paramMap, Map<String, Object> headerMap) throws Exception {
		return MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpPost, paramMap, null, headerMap, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
	}
	
	/**
	 * 提交 post http 请求
	 * @param apiURL
	 * @param postContent
	 * @param headerMap
	 * @return
	 * @throws Exception 
	 */
	public static String post(String url, String postContent, Map<String, Object> headerMap) throws Exception {
		return MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpPost, null, postContent, headerMap, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
	}
	
	/**
	 * 提交 get 请求
	 * @param apiURL
	 * @return
	 * @throws Exception 
	 */
	public static String get(String url) throws Exception {
		return MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpGet, null, null, null, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
	}
	
	/**
	 * 提交 get http 代理 请求
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static MyHttpClientResultVo httpClientGet(String url) throws Exception {
		return MyHttpClientUtil.httpClient(null, url, MyHttpClientUtil.httpGet, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
	}
	
	/**
	 * 提交 get http 代理 请求
	 * @param url
	 * @param headerMap
	 * @return
	 * @throws Exception
	 */
	public static MyHttpClientResultVo httpClientGet(String url, Map<String, Object> headerMap) throws Exception {
		return MyHttpClientUtil.httpClient(null, url, MyHttpClientUtil.httpGet, null, null, headerMap,
				null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
	}
	/**
	 * 提交 get http 代理 请求
	 * @param url
	 * @param headerMap
	 * @return
	 * @throws Exception
	 */
	public static MyHttpClientResultVo httpClientGet(CloseableHttpClient httpClient,String url, Map<String, Object> headerMap) throws Exception {
		return MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpGet, null, null, headerMap,
				null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
	}
	/**
	 * 提交 get http 代理 请求
	 * @param url
	 * @param headerMap
	 * @return
	 * @throws Exception
	 */
	public static MyHttpClientResultVo httpClientPost(CloseableHttpClient httpClient,String url,Map<String, Object> paramMap, Map<String, Object> headerMap) throws Exception {
		return MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, paramMap, null, headerMap,
				null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
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
	public static String httpClient(String url, String sendType, Map<String, Object> paramMap, Map<String, Object> headerMap) throws Exception {
		return MyHttpClientUtil.httpClient(url, sendType, paramMap, null, headerMap, null, null, defaultConnTimeout, defaultSoTimeout, defaultCharset);
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
	public static String httpClient(String url, String sendType, Map<String, Object> paramMap, Map<String, Object> headerMap, String charset) throws Exception {
		return MyHttpClientUtil.httpClient(url, sendType, paramMap, null, headerMap, null, null, defaultConnTimeout, defaultSoTimeout, charset);
	}
	
	/**
	 * 提交 get/post http 代理 请求
	 * @param url
	 * @param sendType GET/POST
	 * @param paramMap
	 * @param headerMap
	 * @param proxyIp
	 * @param proxyPort
	 * @return
	 * @throws Exception
	 */
	public static String httpClient(String url, String sendType, Map<String, Object> paramMap, Map<String, Object> headerMap, String proxyIp, Integer proxyPort) throws Exception {
		return MyHttpClientUtil.httpClient(url, sendType, paramMap, null, headerMap, proxyIp, proxyPort, defaultConnTimeout, defaultSoTimeout, defaultCharset);
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
	public static String httpClient(String url, String sendType, Map<String, Object> paramMap, Map<String, Object> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		return MyHttpClientUtil.httpClient(url, sendType, paramMap, null, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
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
	public static String httpClient(String url, String sendType, String httpContent, Map<String, Object> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(null, url, sendType, null, httpContent, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
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
	public static String httpClient(String url, String sendType, Map<String, Object> paramMap, String httpContent, Map<String, Object> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(null, url, sendType, paramMap, httpContent, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
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
	public static MyHttpClientResultVo httpClient(CloseableHttpClient httpClient, String url, String sendType,
			Map<String, Object> paramMap, String httpContent, Map<String, Object> headerMap,
			String proxyIp, Integer proxyPort, int connTimeout, int soTimeout, String charset) throws Exception {
		String httpResult = null;
		boolean closeFlag = true;
		if(null != httpClient) closeFlag = false;
		else httpClient = HttpClients.createDefault();
		
		Header[] headers = null;
		if(null != headerMap && !headerMap.isEmpty()) headers = MyHttpClientUtil.toHeaders(headerMap);
		try {
			HttpRequestBase httpRequest = null;
			if(httpPost.equalsIgnoreCase(sendType)) {
				httpRequest = new HttpPost(url);
				MyHttpClientUtil.setPostParam((HttpPost)httpRequest, paramMap, httpContent, charset);
			} else if(httpGet.equalsIgnoreCase(sendType)) httpRequest = new HttpGet(url);
			else return new MyHttpClientResultVo("只支持POST/GET", null);

			if(null != headers) httpRequest.setHeaders(headers);
			MyHttpClientUtil.setRequestConfig(httpRequest, proxyIp, proxyPort, connTimeout, soTimeout);
			HttpResponse httpResponse = httpClient.execute(httpRequest);	// 提交请求
			headers = httpResponse.getAllHeaders();
			
//			DefaultHttpParams.getDefaultParams().setParameter("http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY);
			
			// 处理编码,优先使用返回的...
			String charsetTemp = MyHttpClientUtil.getHeaderValue(headers, "Content-Type", "charset=");
			if(StringUtils.isNotEmpty(charsetTemp)) charset = charsetTemp.split("=")[1].trim().toUpperCase();
			
			// 获得服务器相应html信息
			HttpEntity httpEntity = httpResponse.getEntity();
			if(null == httpEntity) return null;
			
			// 根据相应编码解码
//			String encoding = MyHttpClientUtil.getHeaderValue(headers, "Content-Encoding", "gzip");
//			if("gzip".equalsIgnoreCase(encoding)) httpEntity = new GzipDecompressingEntity(httpEntity);
			
			httpResult = MyFileUtils.getStringByInputStream(httpEntity.getContent(), charset);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if((httpResult==null ||"".equals(httpResult)) && statusCode==302){
				httpResult = "302 Moved Temporarily";
			}
//			httpResult = EntityUtils.toString(httpEntity, charset);	// 内存占用大
			
//			if(httpPost.equalsIgnoreCase(sendType)) {
//				while(ArrayUtils.contains(httpRedirectCode, httpResponse.getStatusLine().getStatusCode())) {
//					Header header = httpResponse.getFirstHeader("location");
//					System.out.println(httpRequest.getURI().toURL());
//					String newUri = header.getValue();
//					System.out.println(newUri);
//					if(StringUtils.isEmpty(newUri)) newUri = "/";
//					httpRequest = new HttpPost(newUri);
////					httpRequest.setURI(new URI(newUri));
//					MyHttpClientUtil.setPostParam((HttpPost)httpRequest, paramMap, httpContent, charset);
//					
//					httpResponse = httpClient.execute(httpRequest);	// 提交get请求
//					httpEntity = httpResponse.getEntity();
//					httpResult = MyFileUtils.getStringByInputStream(httpEntity.getContent(), charset);
//				}
//			}
			
			httpRequest.releaseConnection();
			if(closeFlag) httpClient.close();
		} catch(Exception e) {
			throw e;
		} finally {
			if(closeFlag && null != httpClient) httpClient.close();
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
	public static void download(String url, Map<String, Object> headerMap, String fullFileName, int timeout) throws Exception {
		MyHttpClientUtil.download(null, url, headerMap, fullFileName, timeout);
	}
	
	/**
	 * 获取输出流
	 * @param url
	 * @param file
	 * @param timeout
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public static InputStream getInputStream(String url, Map<String, Object> headerMap, int timeout) throws Exception {
		return MyHttpClientUtil.getInputStream(null, url, headerMap, timeout);
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
	public static void download(CloseableHttpClient httpClient, String url, Map<String, Object> headerMap, String fullFileName, int timeout) throws Exception {
		boolean closeFlag = true;
		try {
			if(null == httpClient) httpClient = HttpClients.createDefault();	// 实例化 httpclient
			else closeFlag = false;
			Header[] headers = null;
			if(null != headerMap && !headerMap.isEmpty()) headers = MyHttpClientUtil.toHeaders(headerMap);
			HttpGet httpGet = new HttpGet(url);
			if(null != headers) httpGet.setHeaders(headers);
			MyHttpClientUtil.setRequestConfig(httpGet, MyHttpClientUtil.defaultConnTimeout, timeout);
			HttpResponse response = httpClient.execute(httpGet);		// 提交get请求
			MyFileUtils.createFile(new File(fullFileName), response.getEntity().getContent());
			httpGet.releaseConnection();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			if(closeFlag && null != httpClient) httpClient.close();
		}
	}
	
	/**
	 * 获取输出流
	 * @param url
	 * @param file
	 * @param timeout
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public static InputStream getInputStream(CloseableHttpClient httpClient, String url, Map<String, Object> headerMap, int timeout) throws Exception {
		boolean closeFlag = true;
		InputStream inputStream = null;
		try {
			if(null == httpClient) httpClient = HttpClients.createDefault();	// 实例化 httpclient
			else closeFlag = false;
			Header[] headers = null;
			if(null != headerMap && !headerMap.isEmpty()) headers = MyHttpClientUtil.toHeaders(headerMap);
			HttpGet httpGet = new HttpGet(url);
			if(null != headers) httpGet.setHeaders(headers);
			MyHttpClientUtil.setRequestConfig(httpGet, MyHttpClientUtil.defaultConnTimeout, timeout);
			HttpResponse response = httpClient.execute(httpGet);		// 提交get请求
		    HttpEntity httpEntity=response.getEntity(); //4、获取实体
	        if(httpEntity!=null){
	            inputStream = httpEntity.getContent();
	        }
			httpGet.releaseConnection();
		} catch (RuntimeException e) {
			throw e;
		} finally {
			if(closeFlag && null != httpClient) httpClient.close();
		}
		return inputStream;
	}
	
	/**
	 * 设置 post 参数
	 * @param httpPost
	 * @param paramMap
	 * @param httpContent
	 * @param charset
	 * @throws Exception
	 */
	public static void setPostParam(HttpPost httpPost, Map<String, Object> paramMap, String httpContent, String charset) throws Exception {
		if(StringUtils.isNotEmpty(httpContent)) httpPost.setEntity(new StringEntity(httpContent, charset));
		if(null != paramMap && !paramMap.isEmpty()) {
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			Iterator<Entry<String, Object>> it = paramMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, Object> entry = it.next();
				paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
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
		MyHttpClientUtil.setRequestConfig(requestBase, null, null, connTimeout, soTimeout);
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
	public static Header[] toHeaders(Map<String, Object> headerMap) {
		List<Header> tempHeaders = new ArrayList<Header>();
		if(null == headerMap || headerMap.isEmpty()) return null;
		Iterator<Map.Entry<String, Object>> it = headerMap.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			tempHeaders.add(new BasicHeader(entry.getKey(), entry.getValue().toString()));
		}
		return tempHeaders.toArray(new Header[tempHeaders.size()]);
	}
	
	
	/**
	 * 转成 headerMap
	 * @param headers
	 */
	public static Map<String, Object> toHeaderMap(Header[] headers) {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		if(null == headers || headers.length == 0) return headerMap;
		for(Header header : headers) {
			if("Set-Cookie".equalsIgnoreCase(header.getName())) {
				String[] temps = header.getValue().split(";");
				for(String temp : temps) {
					String[] ts = temp.trim().split("=");
					
					String ts0 = ts[0].trim();
					if("path".equalsIgnoreCase(ts0) || "HttpOnly".equalsIgnoreCase(ts0) || "Expires".equalsIgnoreCase(ts0)) continue ;
					
					String cookieValue = (String)headerMap.get("Cookie");
					if(null == cookieValue) cookieValue = "";
					String cookie = ts0 + "=" + ts[1].trim();
					headerMap.put("Cookie", (StringUtils.isEmpty(cookieValue) ? "" : cookieValue + ";") + cookie);
				}
			} else headerMap.put(header.getName(), header.getValue());
		}
		return headerMap;
	}
	
	/**
	 * 
	 * @param oldHeaderMap
	 * @param newHeaderMap
	 * @return
	 */
	public static Map<String, Object> mergeHeaderMap(Map<String, Object> oldHeaderMap, Map<String, Object> newHeaderMap) {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		
		
		String oldCookie = (String)oldHeaderMap.get("Cookie");
		String newCookie = (String)newHeaderMap.get("Cookie");
		if(null == oldCookie) oldCookie = "";
		if(null == newCookie) newCookie = "";
		
		Map<String, String> oldMap = new HashMap<String, String>();
		if(StringUtils.isNotEmpty(oldCookie)) {
			for(String oc : oldCookie.split(";")) {
				String[] ocs = oc.split("=");
				oldMap.put(ocs[0].trim(), ocs[1].trim());
			}
		}
		Map<String, String> newMap = new HashMap<String, String>();
		if(StringUtils.isNotEmpty(newCookie)) {
			for(String nc : newCookie.split(";")) {
				String[] ncs = nc.split("=");
				newMap.put(ncs[0].trim(), ncs[1].trim());
			}
		}
		if(StringUtils.isNotEmpty(newCookie)) {
			for(String nc : newCookie.split(";")) {
				String[] ncs = nc.split("=");
				if(null == oldMap.get(ncs[0].trim())) newCookie += ";" + nc;
			}
		}
		
		if(StringUtils.isNotEmpty(oldCookie)) {
			for(String oc : oldCookie.split(";")) {
				String[] ocs = oc.split("=");
				if(null == newMap.get(ocs[0].trim())) newCookie += ";" + oc;
			}
		}
		
		headerMap.putAll(oldHeaderMap);
		headerMap.putAll(newHeaderMap);
		headerMap.put("Cookie", newCookie);
		return headerMap;
	}
	
	/**
	 * 返回 header 中 headerName 中 headerValueKey
	 * @param headers
	 * @param headerName
	 * @param headerValueKey
	 * @return
	 */
	public static String getHeaderValue(Header[] headers, String headerName, String headerValueKey) {
		if(null == headers) return "";
		for(Header header : headers) {
			if(!header.getName().equals(headerName)) continue ;
			String[] values = header.getValue().trim().split(";");
			for(String value : values) {
				if(value.toUpperCase().contains(headerValueKey.toUpperCase())) return value.trim();
			}
		}
		return "";
	}
	
	/**
	 * 返回 header 中 headerName
	 * @param headers
	 * @param headerName
	 * @return
	 */
	public static String getHeaderValue(Header[] headers, String headerName) {
		if(null == headers) return "";
		for(Header header : headers) {
			if(!header.getName().equals(headerName)) continue ;
			return header.getValue();
		}
		return "";
	}
	public static String getHeaderValues(Header[] headers, String headerName) {
		if(null == headers) return "";
		String valueStr = "";
		for(Header header : headers) {
			if(!header.getName().equals(headerName)) continue ;
			String[] values = header.getValue().trim().split(";");
			valueStr += values[0].trim()+";";
		}
		return "".equals(valueStr)?valueStr:valueStr.substring(0, valueStr.length()-1);
	}
}