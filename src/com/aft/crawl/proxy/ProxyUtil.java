package com.aft.crawl.proxy;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.ConnectionClosedException;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.bean.CrawlCommon;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@SuppressWarnings("unchecked")
public final class ProxyUtil {
	
	private final static Logger logger = Logger.getLogger(ProxyUtil.class);
	
	// 代理请求异常时, 睡眠等待时长
	private final static int proxySleepTime = 500;
	
	private static CopyOnWriteArrayList<String> proxyIpList = new CopyOnWriteArrayList<String>();
	
	// 线程代理vo
	private final static CopyOnWriteArrayList<ProxyVo> proxyVos = new CopyOnWriteArrayList<ProxyVo>();
	
	static {
		logger.info("ProxyUtil 初始化[overdueProxy]开始...");
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				for(ProxyVo threadProxy : proxyVos) {
					if(System.currentTimeMillis() - threadProxy.getStartTime() < CrawlCommon.getProxySleepMaxTime()) continue ;
					logger.warn(threadProxy.getThreadMark() + ", 无效代理过期, 强制无效!");
					ProxyUtil.updateProxyUsed(threadProxy.getThreadMark());
				}
			}
		}, CrawlCommon.getProxySleepMaxTime(), 30 * 1000);
		logger.info("ProxyUtil 初始化[overdueProxy]结束...");
	}
	
	
	/**
	 * 
	 * @param url
	 * @param headerMap
	 * @param soTimeout
	 * @param charset
	 * @return
	 */
	public static String httpProxyGet(String url, Map<String, Object> headerMap, int soTimeout, String charset) {
		return ProxyUtil.httpProxy(null, url, MyHttpClientUtil.httpGet, null, null, headerMap, soTimeout, charset);
	}
		
	/**
	 * 
	 * @param threadMark
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param httpContent
	 * @param soTimeout
	 * @param charset
	 * @return
	 */
	public static String httpProxy(String threadMark, String url, String sendType, Map<String, Object> paramMap, String httpContent, Map<String, Object> headerMap, int soTimeout, String charset) {
		return ProxyUtil.httpProxy(null, threadMark, url, sendType, paramMap, httpContent, headerMap, soTimeout, charset);
	}
	
	/**
	 * 
	 * @param httpClientSessionVo
	 * @param threadMark
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param soTimeout
	 * @param charset
	 * @return
	 */
	public static String httpProxy(MyHttpClientSessionVo httpClientSessionVo, String threadMark, String url, String sendType,
			Map<String, Object> paramMap, String httpContent, Map<String, Object> headerMap, int soTimeout, String charset) {
		return ProxyUtil.httpProxy(httpClientSessionVo, threadMark, url, sendType, paramMap, httpContent, headerMap, null, soTimeout, charset);
	}
	
	/**
	 * 
	 * @param httpClientSessionVo
	 * @param threadMark
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param soTimeout
	 * @param charset
	 * @return
	 */
	public static MyHttpClientResultVo httpProxyResultVo(MyHttpClientSessionVo httpClientSessionVo, String threadMark, String url, String sendType,
			Map<String, Object> paramMap, String httpContent, Map<String, Object> headerMap, int soTimeout, String charset) {
		return ProxyUtil.httpProxyResultVo(httpClientSessionVo, threadMark, url, sendType, paramMap, httpContent, headerMap, null, soTimeout, charset);
	}
	
	
	/**
	 * 
	 * @param httpClientSessionVo
	 * @param threadMark
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 */
	private static String httpProxy(MyHttpClientSessionVo httpClientSessionVo, String threadMark, String url, String sendType,
			Map<String, Object> paramMap, String httpContent, Map<String, Object> headerMap, Integer connTimeout, Integer soTimeout, String charset) {
		MyHttpClientResultVo resultVo = ProxyUtil.httpProxyResultVo(httpClientSessionVo, threadMark, url, sendType, paramMap, httpContent, headerMap, connTimeout, soTimeout, charset);
		return null == resultVo ? null : resultVo.getHttpResult();
	}
	
	/**
	 * 
	 * @param httpClientSessionVo
	 * @param threadMark
	 * @param url
	 * @param sendType
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param connTimeout
	 * @param soTimeout
	 * @param charset
	 * @return
	 */
	private static MyHttpClientResultVo httpProxyResultVo(MyHttpClientSessionVo httpClientSessionVo, String threadMark, String url, String sendType,
			Map<String, Object> paramMap, String httpContent, Map<String, Object> headerMap, Integer connTimeout, Integer soTimeout, String charset) {
		if(null == connTimeout) connTimeout = null != httpClientSessionVo ? httpClientSessionVo.getHttpClientSession().getConnTimeout() : MyHttpClientUtil.defaultConnTimeout;
		if(null == soTimeout) soTimeout = null != httpClientSessionVo ? httpClientSessionVo.getHttpClientSession().getSoTimeout() : MyHttpClientUtil.defaultSoTimeout;
		if(null == charset) charset = null != httpClientSessionVo ? httpClientSessionVo.getHttpClientSession().getCharset() : MyHttpClientUtil.defaultCharset;
		
		ProxyVo proxyVo = ProxyUtil.getProxy(threadMark);
		String proxyIp = null == proxyVo ? null : proxyVo.getProxyIp();
		Integer proxyPort = null == proxyVo ? null : proxyVo.getProxyPort();
		try {
			logger.debug(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "]");
			if(null == httpClientSessionVo) return MyHttpClientUtil.httpClient(null, url, sendType, paramMap, httpContent, headerMap, proxyIp, proxyPort, connTimeout, soTimeout, charset);
			return httpClientSessionVo.getHttpClientSession().httpResultVo(url, sendType, paramMap, httpContent, httpClientSessionVo.getCookieRemark(), httpClientSessionVo.getParentCookieRemark(), httpClientSessionVo.getHeaderMap(), proxyIp, proxyPort, connTimeout, soTimeout, charset);
		} catch(MalformedChunkCodingException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] MalformedChunkCodingException:" + e);
		} catch(ConnectionClosedException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] ConnectionClosedException:" + e);
		} catch(ConnectTimeoutException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] ConnectTimeoutException:" + e);
		} catch(NoHttpResponseException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] NoHttpResponseException:" + e);
			return new MyHttpClientResultVo("NoHttpResponseException");
		} catch(SocketTimeoutException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] SocketTimeoutException:" + e);
			return new MyHttpClientResultVo("Readtimedout");
		} catch(SSLHandshakeException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] SSLHandshakeException:" + e);
			if(e.toString().contains("closed connection")){
				return new MyHttpClientResultVo("ConnectionException");
			}
		} catch(UnknownHostException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] UnknownHostException:" + e);
		} catch(SocketException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] SocketException:" + e);
			if(e.toString().contains("Connection reset")
					|| e.toString().contains("Connection timed out")
					|| e.toString().contains("Connection refused")) {
				return new MyHttpClientResultVo("ConnectionException");
			}
		} catch(SSLException e) {
			logger.warn(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] SSLException:" + e);
		} catch(Exception e) {
			logger.error(threadMark + ", HttpCient proxy[" + proxyIp + ":" + proxyPort + "] 请求处理异常:\r", e);
		}
		return null;
	}
	
	/**
	 * 开始ip代理
	 * @param threadMark 线程标识
	 * @param jobId
	 * @param pageType
	 * @return
	 */
	public static void startProxy(String threadMark, String useType, int jobId, String pageType) {
		if(null != ProxyUtil.getProxy(threadMark)) return ;
		synchronized(threadMark) {
			if(null != ProxyUtil.getProxy(threadMark)) return ;
		}
		ProxyUtil.changeProxy(threadMark, useType, jobId, pageType);
	}
	
	/**
	 * 手工设置代理
	 * @param threadMark
	 * @param jobId
	 * @param pageType
	 * @param proxyIp
	 * @param proxyPort
	 */
	public static void setProxy(String threadMark, int jobId, String pageType, String proxyIp, int proxyPort) {
		for(ProxyVo proxyVo : proxyVos) {
			if(proxyVo.getThreadMark().equals(threadMark)) {
				proxyVo.setProxyIp(proxyIp);
				proxyVo.setProxyPort(proxyPort);
				proxyVo.setStartTime(System.currentTimeMillis());
				return ;
			}
		}
		ProxyVo proxyVo = new ProxyVo(threadMark, jobId);
		proxyVo.setProxyIp(proxyIp);
		proxyVo.setProxyPort(proxyPort);
		proxyVo.setChanging(false);
		proxyVos.add(proxyVo);
	}

	/**
	 * 切换ip代理
	 * @param threadMark 线程标识
	 * @param useType 获取代理的类型
	 * @param jobId
	 * @param pageType
	 * @return
	 */
	public static Object[] changeProxy(String threadMark, String useType, int jobId, String pageType) {
		String proxyType = MyDefaultProp.getProxyType();//代理方式
		Object[] proxyObj = null;
		if("1".equals(proxyType)){
			//1:快代理切换
			 proxyObj = kuaiProxy(threadMark, useType, jobId, pageType);
		}else if("2".equals(proxyType)){
			//2:VPS代理切换
			proxyObj = vpsProxy(threadMark, useType, jobId, pageType);
		}else if("3".equals(proxyType)){
			//3:全网代理切换
			 proxyObj = wholeProxy(threadMark, useType, jobId, pageType);
		}else{
			proxyObj = vpsProxy(threadMark, useType, jobId, pageType);
		}
		return proxyObj;
	}
	
	/**
	 * VPS代理切换
	 * @return
	 */
	public static Object[] vpsProxy(String threadMark, String useType, int jobId, String pageType){
		ProxyVo proxyVo = ProxyUtil.getProxy(threadMark);
		if(null == proxyVo) {
			synchronized(threadMark) {
				proxyVo = ProxyUtil.getProxy(threadMark);
				if(null == proxyVo) {
					proxyVo = new ProxyVo(threadMark, jobId);
					proxyVos.add(proxyVo);
				}
			}
		}
		
		synchronized(proxyVo) {
			if(!proxyVo.canChange()) {
				MyThreadUtils.sleep(proxySleepTime);
				return new Object[4];
			}
			proxyVo.setChanging(true);
		}
		
		CrawlerType crawlerType = CrawlerType.getCrawlerType(pageType);
		logger.info(threadMark + ", 原代理[" + proxyVo.getQueryFlag() + "]");
		
		if(StringUtils.isEmpty(useType)) useType = "0";
		int maxTime = 5;		// 最大五次
		while(--maxTime >= 0) {	// 获取...
			try {
				if(CrawlerWin.isClosing()) return new Object[4];	// 系统正在关闭中...
				String result = MyHttpClientUtil.get(CrawlCommon.getProxyIpUrl()
						+ "?queryFlag=" + proxyVo.getQueryFlag() + "&type=Crawler-" + crawlerType.getCrawlerFlag()
						+ "&userFlag=" + MyDefaultProp.getOperatorName() + "_" + jobId+ "&useType=" + useType);
				logger.info(threadMark + ", 切换ip代理返回: " + result);
				Map<String, Object> resultMap = MyJsonTransformUtil.readValue(result, Map.class);
				if(!(Boolean)resultMap.get("success")) {
					Thread.sleep(proxySleepTime);
					continue ;
				}
				List<String> proxys = (List<String>)resultMap.get("msg");
				proxyVo.setProxyIp(proxys.get(0));
				proxyVo.setProxyPort(Integer.parseInt(proxys.get(1)));
				proxyVo.setStartTime(System.currentTimeMillis());
				proxyVo.setRealIp(proxys.get(2));
				proxyVo.setQueryFlag(proxys.get(3));
				proxyVo.setIpRegion(proxys.get(4));
				proxyVo.setChanging(false);
				return new Object[]{proxyVo.getProxyIp(), proxyVo.getProxyPort(), proxyVo.getRealIp(), proxyVo.getIpRegion()};
			} catch (Exception e) {
				logger.error(threadMark + ", 获取ip代理异常:\r", e);
				MyThreadUtils.sleep(proxySleepTime);
			}
		}
		logger.warn(threadMark + ", 没有获取到可用代理, 已尝试5次...");
		return new Object[4];
	}
	
	/**
	 * 快代理切换
	 * @return
	 */
	public static Object[] kuaiProxy(String threadMark, String useType, int jobId, String pageType) {
		ProxyVo proxyVo = ProxyUtil.getProxy(threadMark);
		if(null == proxyVo) {
			synchronized(threadMark) {
				proxyVo = ProxyUtil.getProxy(threadMark);
				if(null == proxyVo) {
					proxyVo = new ProxyVo(threadMark, jobId);
					proxyVos.add(proxyVo);
				}
			}
		}
		
		synchronized(proxyVo) {
			if(!proxyVo.canChange()) {//正在切换IP
				MyThreadUtils.sleep(proxySleepTime);
				return new Object[4];
			}
			proxyVo.setChanging(true);
		}
		logger.info(threadMark + ", 原代理[" + proxyVo.getQueryFlag() + "]");
		
		if(StringUtils.isEmpty(useType)) useType = "0";
		synchronized(proxyVo){
			int ipSize = proxyIpList.size();
			if(ipSize==0){
				int maxTime = 5;		// 最大五次
				while(--maxTime >= 0) {	// 获取...
					try {
						if(CrawlerWin.isClosing()) return new Object[4];	// 系统正在关闭中...
						String result = MyHttpClientUtil.get("http://ent.kdlapi.com/api/getproxy/?orderid=965086445941662&num=9999&port_ex=8080%2C80&ipstart_ex=192.168&b_pcchrome=1&b_pcie=1&b_pcff=1&b_android=1&protocol=1&method=1&an_an=1&an_ha=1&sp1=1&sp2=1&quality=2&format=json&sep=1");
						logger.info(threadMark + ", 获取ip代理返回: " + result);
						if(result.contains("ERROR")){
							Thread.sleep(proxySleepTime);
							continue ;
						}
						JSONObject resultObj = JSONObject.parseObject(result);
						String code = resultObj.getString("code");
						if(!code.equals("0")) {
							Thread.sleep(proxySleepTime);
							continue ;
						}
						JSONObject data = resultObj.getJSONObject("data");
						JSONArray proxyList = data.getJSONArray("proxy_list");
						int proxySize = proxyList.size();
						for(int p = 0;p<proxySize;p++){
							String IP = proxyList.getString(p);
							proxyIpList.add(IP);
						}
						break;
					} catch (Exception e) {
						logger.error(threadMark + ", 获取ip代理异常:\r", e);
						MyThreadUtils.sleep(proxySleepTime);
					}
			}
		}
		String ip = proxyIpList.get(0);
		logger.info(threadMark + ", 切换ip代理返回: " + ip);
		String[] array = ip.split(":");
		proxyVo.setProxyIp(array[0]);
		proxyVo.setProxyPort(Integer.parseInt(array[1]));
		proxyVo.setStartTime(System.currentTimeMillis());
		proxyVo.setRealIp(array[0]);
		proxyVo.setQueryFlag(ip);
		proxyVo.setIpRegion("");
		proxyVo.setChanging(false);
		proxyIpList.remove(0);
		return new Object[]{proxyVo.getProxyIp(), proxyVo.getProxyPort(), proxyVo.getRealIp(), proxyVo.getIpRegion()};	
		}
	}
	
	/**
	 * 全网代理切换
	 * @return
	 */
	public static Object[] wholeProxy(String threadMark, String useType, int jobId, String pageType) {
		ProxyVo proxyVo = ProxyUtil.getProxy(threadMark);
		if(null == proxyVo) {
			synchronized(threadMark) {
				proxyVo = ProxyUtil.getProxy(threadMark);
				if(null == proxyVo) {
					proxyVo = new ProxyVo(threadMark, jobId);
					proxyVos.add(proxyVo);
				}
			}
		}
		
		synchronized(proxyVo) {
			if(!proxyVo.canChange()) {//正在切换IP
				MyThreadUtils.sleep(proxySleepTime);
				return new Object[4];
			}
			proxyVo.setChanging(true);
		}
		logger.info(threadMark + ", 原代理[" + proxyVo.getQueryFlag() + "]");
		
		if(StringUtils.isEmpty(useType)) useType = "0";
		synchronized(proxyVo){
			int ipSize = proxyIpList.size();
			if(ipSize==0){
				int maxTime = 5;		// 最大五次
				while(--maxTime >= 0) {	// 获取...
					try {
						if(CrawlerWin.isClosing()) return new Object[4];	// 系统正在关闭中...
						String result = MyHttpClientUtil.get("http://dynamic.goubanjia.com/dynamic/get/4344738b5e70f941c634a304d4e7c3f8.html?sep=5");
						logger.info(threadMark + ", 获取ip代理返回: " + result);
						if(result.contains("msg")){
							Thread.sleep(proxySleepTime);
							continue ;
						}
						if(result.contains("many")){
							Thread.sleep(proxySleepTime);
							continue ;
						}
						String[] ips = result.split(",");
						int proxySize = ips.length;
						for(int p = 0;p<proxySize;p++){
							String IP = ips[p];
							proxyIpList.add(IP);
						}
						break;
					} catch (Exception e) {
						logger.error(threadMark + ", 获取ip代理异常:\r", e);
						MyThreadUtils.sleep(proxySleepTime);
					}
			}
		}
		String ip = proxyIpList.get(0);
		logger.info(threadMark + ", 切换ip代理返回: " + ip);
		String[] array = ip.split(":");
		proxyVo.setProxyIp(array[0]);
		proxyVo.setProxyPort(Integer.parseInt(array[1]));
		proxyVo.setStartTime(System.currentTimeMillis());
		proxyVo.setRealIp(array[0]);
		proxyVo.setQueryFlag(ip);
		proxyVo.setIpRegion("");
		proxyVo.setChanging(false);
		proxyIpList.remove(0);
		return new Object[]{proxyVo.getProxyIp(), proxyVo.getProxyPort(), proxyVo.getRealIp(), proxyVo.getIpRegion()};	
		}
	}
	
	/**
	 * 更新已使用
	 */
	public static void updateAllProxyUsed() {
		for(ProxyVo proxyVo : proxyVos) {
			ProxyUtil.updateProxyUsed(proxyVo.getThreadMark());
		}
	}
	
	/**
	 * 更新已使用
	 * @param jobId
	 */
	public static void updateProxyUsed(int jobId) {
		for(ProxyVo proxyVo : proxyVos) {
			if(jobId != proxyVo.getJobId()) continue ;
			ProxyUtil.updateProxyUsed(proxyVo.getThreadMark());
		}
	}
	
	/**
	 * 更新已使用
	 * @param threadMark
	 */
	public static void updateProxyUsed(String threadMark) {
		ProxyVo proxyVo = ProxyUtil.getProxy(threadMark);
		if(null == proxyVo || StringUtils.isEmpty(proxyVo.getQueryFlag())) return ;
		try {
			String result = MyHttpClientUtil.get(CrawlCommon.getProxyInvalidUrl() + "?queryFlag=" + proxyVo.getQueryFlag());
			logger.info(threadMark + ", 更新代理失效请求[" + proxyVo.getProxyIp() + ":" + proxyVo.getProxyPort() + "], 返回:" + result);
			proxyVos.remove(proxyVo);
		} catch(Exception e) {
			logger.error(threadMark + ", 更新代理失效异常:\r", e);
		}
	}
	
	/**
	 * 获取本线程代理信息
	 * @param threadMark
	 * @return 不存在代理,返回 new Object[4]
	 */
	public static Object[] getProxyInfo(String threadMark) {
		if(StringUtils.isEmpty(threadMark)) return new Object[2];
		for(ProxyVo proxyVo : proxyVos) {
			if(!proxyVo.getThreadMark().equals(threadMark)) continue ;
			return new Object[]{proxyVo.getProxyIp(), proxyVo.getProxyPort(), proxyVo.getRealIp(), proxyVo.getIpRegion()};
		}
		return new Object[4];
	}
	
	/**
	 * 是否存在此代理
	 * @param proxyIp
	 * @param proxyPort
	 * @return
	 */
	public static boolean existProxy(String proxyIp, int proxyPort) {
		for(ProxyVo proxyVo : proxyVos) {
			if(!proxyVo.getProxyIp().equals(proxyIp)) continue ;
			if(proxyVo.getProxyPort() != proxyPort) continue ;
			return true;
		}
		return false;
	}
	
	/**
	 * 获取本线程代理信息
	 * @param threadMark
	 * @return
	 */
	public static ProxyVo getProxy(String threadMark) {
		for(ProxyVo proxyVo : proxyVos) {
			if(proxyVo.getThreadMark().equals(threadMark)) return proxyVo;
		}
		return null;
	}
}