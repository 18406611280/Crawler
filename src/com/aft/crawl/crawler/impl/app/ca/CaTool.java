package com.aft.crawl.crawler.impl.app.ca;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 下单相关工具类
 * 
 * @author huzhenhua
 * 
 */
public class CaTool {

	private static Logger logger = Logger.getLogger(CaTool.class);
	
	public static final String ADULTNUM = "1";

	/** 航司 */
	public static String AIRLINECODE ="CA";
	
	/** 超时设置 */
	public static int TIMEOUT = 60000;
	
	/** caAPP访问url */
	public static String CAAPPURL = "https://m.airchina.com.cn:9061/worklight/apps/services/api/AirChina/android/query";
	
	/** 国航接口成功代码标示 */
	public static String CODE = "00000000";

	/** http请求头信息 */
	public static String COOKIE = "dba3bf9b-a0f9-43b4-b029-5c36f4fab0cf";
	
	public static String TOKEN = "904f840b254329885040a09563bcb838dc5dd9a035759d55c31ec039b8b45c52";
	
	public static String SCHEME = "http";
	
	public static String APPVER = "4.1.2";	// 国航app版本
	
	public static String SYSVER = "18";		//
	
	public static String MOBILETYPE = "Android"; // 手机类型
	
	public static String LOGINTYPE = "4"; 	// 登录类型 身份证：1 手机号码：3 账号：4
	
	public static String VERSION = "1"; 	// 不清楚什么版本，app传的是1

	/**
	 * urlEncoder 编码
	 * 
	 * @param data
	 * @return
	 */
	public static String urlChange(String data) {
		try {
			data = URLEncoder.encode(data, "UTF-8");
			data = data.replace("+", "%20");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * 初次鉴权post
	 * @param ip
	 * @param port
	 * @param url
	 * @param parm
	 * @param deviceId
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static String firstJianQuanPost(String ip, int port, String url, String parm, String deviceId, int amount) throws Exception {
		CloseableHttpClient httpClient = null;
		try {
			// 获取httpsClient
			httpClient = HttpsClientTool.getHttpsClient();
			// 依次是代理地址，代理端口号，协议类型
			HttpHost proxy = new HttpHost(ip, port, SCHEME);
			RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
			HttpPost post = new HttpPost(url);
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			String[] pA = parm.split("&");
			for(String p : pA) {
				formparams.add(new BasicNameValuePair(p.split("=")[0], p.split("=")[1]));
			}
			post.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			post.setConfig(config);
			post.setHeader("X-Requested-With", "XMLHttpRequest");
			post.setHeader("x-wl-app-version", "1.0");
			post.setHeader("Accept-Language", "zh_CN");
			post.setHeader("x-wl-platform-version", "6.3.0.0");
			post.setHeader("x-wl-clientlog-deviceId", deviceId);
			post.setHeader("x-wl-clientlog-appname", "AirChina");
			post.setHeader("x-wl-clientlog-appversion", "1.0");
			post.setHeader("x-wl-clientlog-osversion", "4.3");
			post.setHeader("x-wl-clientlog-env", "Android");
			post.setHeader("x-wl-clientlog-model", "Lenovo A788t");
			post.setHeader("x-wl-analytics-tracking-id", UUID.randomUUID() .toString().toUpperCase());
			post.setHeader("Content-Type", "application/json;charset=utf-8");
			post.setHeader("Host", "m.airchina.com.cn:9061");
			post.setHeader("Connection", "Keep-Alive");
			post.setHeader("Proxy-Connection", "keep-alive");
			post.setHeader("User-Agent", "WLNativeAPI(A788t; LenovoA788t_S18251_140829; Lenovo A788t; SDK 18; Android 4.3)");
			post.setHeader("Cookie", "WL_PERSISTENT_COOKIE=" + COOKIE + "; citrix_ns_id_.airchina.com.cn_%2F_wlf=V0xfUEVSU0lTVEVOVF9DT09LSUVf?A89b4r5bKiaHQJw2ADXEAiKJ8lsA#tywPFe3XwUF6BdMhiRJiEm2f43UA&");
			post.setHeader("Cookie2", "$Version=1");
			HttpResponse response = httpClient.execute(post);
			if(403 == response.getStatusLine().getStatusCode()) return "forbidden";	// 封ip...
			if(502 == response.getStatusLine().getStatusCode() && amount <= 3) {
				MyThreadUtils.sleep(1000);
				return CaTool.firstJianQuanPost(ip, port, url, parm, deviceId, amount+1);	// 502
			}
			
			
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("JSESSIONID", MyHttpClientUtil.getHeaderValue(response.getAllHeaders(), "Set-Cookie", "JSESSIONID="));
			resultMap.put("NSC_xpslmjhiu", MyHttpClientUtil.getHeaderValue(response.getAllHeaders(), "Set-Cookie", "NSC_xpslmjhiu="));
			resultMap.put("citrix_ns_id", MyHttpClientUtil.getHeaderValue(response.getAllHeaders(), "Set-Cookie", "citrix_ns_id="));
			resultMap.put("citrix_ns_id_airchina_wat", MyHttpClientUtil.getHeaderValue(response.getAllHeaders(), "Set-Cookie", "citrix_ns_id_airchina_wat="));
			resultMap.put("citrix_ns_id_airchina_wlf", MyHttpClientUtil.getHeaderValue(response.getAllHeaders(), "Set-Cookie", "citrix_ns_id_airchina_wlf="));
			
			String result = EntityUtils.toString(response.getEntity(), "UTF-8");
			result = result.replace("/*-secure-", "");
			result = result.replace("*/", "");
			
			Map<String, Object> jsonMap = MyJsonTransformUtil.readValue(result, Map.class);
			Map<String, Object> challengesJosn = (Map<String, Object>)jsonMap.get("challenges");
			Map<String, Object> authenticityRealmJson = (Map<String, Object>)challengesJosn.get("wl_authenticityRealm");
			Map<String, Object> antiXSRFRealmJson = (Map<String, Object>)challengesJosn.get("wl_antiXSRFRealm");
			Map<String, Object> deviceNoProvisioningRealm = (Map<String, Object>)challengesJosn.get("wl_deviceNoProvisioningRealm");
			
			resultMap.put("WL_Challenge_Data", authenticityRealmJson.get("WL-Challenge-Data"));
			resultMap.put("WL_Instance_Id", antiXSRFRealmJson.get("WL-Instance-Id"));
			resultMap.put("secondToken", deviceNoProvisioningRealm.get("token"));
			return MyJsonTransformUtil.writeValue(resultMap);
		} catch(Exception e) {
			logger.error("下单首次鉴权异常，", e);
			throw e;
		} finally {
			if(null != httpClient) httpClient.close();
		}
	}

	public static String httpsResult(String ip, int port, String url, String parm, String httpsParam, int amount) {
		CloseableHttpClient httpClient = null;
		try {
			JSONObject json = JSONObject.fromObject(httpsParam);
			String WL_Instance_Id = json.getString("WL_Instance_Id");
			String secondToken = json.getString("secondToken");
			String wl_authenticityRealm = json.getString("wl_authenticityRealm");
			String JSESSIONID = json.getString("JSESSIONID");
			String NSC_xpslmjhiu = json.getString("NSC_xpslmjhiu");
			String citrix_ns_id = json.getString("citrix_ns_id");
			String citrix_ns_id_airchina_wat = json.getString("citrix_ns_id_airchina_wat");
			String citrix_ns_id_airchina_wlf = json.getString("citrix_ns_id_airchina_wlf");
			String deviceId = json.getString("deviceId");

			httpClient = HttpsClientTool.getHttpsClient();
			// 依次是代理地址，代理端口号，协议类型
			HttpHost proxy = new HttpHost(ip, port, SCHEME);
			RequestConfig config = RequestConfig.custom().setProxy(proxy)
												.setConnectTimeout(60000)
												.setConnectionRequestTimeout(60000).setSocketTimeout(60000)
												.build();

			// url = url + "?" + parm;
			HttpPost post = new HttpPost(url);
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			String[] pA = parm.split("&");
			for (String p : pA) {
				formparams.add(new BasicNameValuePair(p.split("=")[0], p.split("=")[1]));
			}
			post.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			HttpResponse response;
			post.setConfig(config);
			post.setHeader("X-Requested-With", "XMLHttpRequest");
			post.setHeader("x-wl-app-version", "1.0");
			post.setHeader("Accept-Language", "zh_CN");
			post.setHeader("x-wl-platform-version", "6.3.0.0");
			post.setHeader("x-wl-clientlog-deviceId", deviceId); // deviceId
			post.setHeader("x-wl-clientlog-appname", "AirChina");
			post.setHeader("x-wl-clientlog-appversion", "1.0");
			post.setHeader("x-wl-clientlog-osversion", "4.3");
			post.setHeader("x-wl-clientlog-env", "Android");
			post.setHeader("x-wl-clientlog-model", "Lenovo A788t");
			post.setHeader("WL-Instance-Id", WL_Instance_Id); // instanceId
			post.setHeader("x-wl-analytics-tracking-id", UUID.randomUUID().toString().toUpperCase()); // trackingId
			post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			post.setHeader("Host", "m.airchina.com.cn:9061");
			post.setHeader("Connection", "Keep-Alive");
			post.setHeader("Accept-Encoding", "gzip, deflate");
			post.setHeader("Accept", "*/*");
			// post.setHeader("Content-Length", "2100");
			post.setHeader("Proxy-Connection", "keep-alive");
			post.setHeader("User-Agent", " WLNativeAPI(4G; ALPS.KK1.MP7.V1; 4G; SDK 19; Android 4.4.4)");
			post.setHeader("Authorization",
							"{\"wl_deviceNoProvisioningRealm\":{\"ID\":{\"app\":{\"id\":\"AirChina\",\"version\":\"1.0\"},\"device\":{\"id\":\""
									+ deviceId + "\",\"os\":\"4.3\",\"environment\":\"Android\",\"model\":\"Lenovo A788t\"},\"token\":\""
									+ secondToken + "\",\"app\":{\"id\":\"AirChina\",\"version\":\"1.0\"}}},\"wl_authenticityRealm\":\"" + wl_authenticityRealm + "\"}");
			post.setHeader("Cookie", "WL_PERSISTENT_COOKIE=" + COOKIE + "; "
									+ JSESSIONID + "; " + NSC_xpslmjhiu + "; " + citrix_ns_id + "; " + citrix_ns_id_airchina_wat + "; " + citrix_ns_id_airchina_wlf + "");
			// post.setHeader("Cookie2", "$Version=1");
			response = httpClient.execute(post);
//			if(403 == response.getStatusLine().getStatusCode()) return "forbidden";	// 封ip...
			if(502 == response.getStatusLine().getStatusCode() && amount <= 3) {
				MyThreadUtils.sleep(1000);
				return CaTool.httpsResult(ip, port, url, parm, deviceId, amount+1);	// 502
			}
			
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				if(entity.getContentEncoding() != null) {
					if("gzip".equalsIgnoreCase(entity.getContentEncoding().getValue())) {
						entity = new GzipDecompressingEntity(entity);
					} else if("deflate".equalsIgnoreCase(entity.getContentEncoding().getValue())) {
						entity = new DeflateDecompressingEntity(entity);
					}
				}
				byte[] htmlByte = EntityUtils.toByteArray(entity);
				String result = new String(htmlByte, "UTF-8");
				return result;
			}
		} catch (Exception e) {
			logger.error("caTool httpsResult 异常:\r", e);
		}
		return null;
	}
}