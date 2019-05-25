package com.aft.crawl.crawler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.bean.TimerJob;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.crawler.impl.b2c.B2CGSCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CHXCrawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.logger.MyCrawlerLogger;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 根据每个类型处理
 * @author Administrator
 *
 */
public abstract class Crawler {

	protected Logger logger;
	
	// http 请求头部
	protected final Map<String, Object> headerMap = new HashMap<String, Object>();
	protected static Map<String,BigDecimal> rateMap = new HashMap<String, BigDecimal>();
	// 基础共享参数
	private final static Map<String, Map<String, Object>> baseJobParamMap = new HashMap<String, Map<String, Object>>();
	
	
	// 代理请求异常时, 睡眠等待时长
	protected final static int sleepTime = 1000;
	
	// http 连续返回错误最大次数
	private final static int maxHttpErrorTryAmount = 5;
	
	// http 连续返回错误次数
	private int httpErrorTryAmount = 0;
	
	
	// 开始采集时间
	private long startTime;
	
	
	// 主线程&代理标识
	protected final String threadMark;
	
	// 获取代理的类型
	protected final String useType;
	
	// 是否切换代理
	private final boolean doProxy;
	
	
	// 采集任务
	private JobDetail jobDetail;
	
	public Crawler(String threadMark) {
		this(threadMark, "0", true);
	}
	
	public Crawler(String threadMark, String useType) {
		this(threadMark, useType, true);
	}
	
	public Crawler(String threadMark, boolean doProxy) {
		this(threadMark, "0", doProxy);
	}
	
	public Crawler(String threadMark, String useType, boolean doProxy) {
		this.threadMark = threadMark;
		this.useType = useType;
		this.doProxy = doProxy;
		headerMap.put("User-Agent", MyDefaultProp.getUserAgent());
	}
	
	/**
	 * 开始采集
	 * @param jobDetail
	 * @return
	 */
	public List<CrawlResultBase> crawlGo(JobDetail jobDetail) {
		try {
			this.jobDetail = jobDetail;
			this.startTime = System.currentTimeMillis();
			this.logger = MyCrawlerLogger.getCrawlerLogger(this.jobDetail.getTimerJob());
			List<CrawlResultBase> crbs = this.crawl();
			return crbs;
		} catch(Exception e) {
			logger.error(this.jobDetail.toStr() + ", run crawl exception:\r", e);
			CrawlerWin.logger(this.jobDetail.toStr() + ", run crawl exception:" + e);
		}
		return null;
	}
	
	/**
	 * 采集内容(异常的忽略当次)
	 * @return null --> 失败
	 * @throws Exception
	 */
	protected abstract List<CrawlResultBase> crawl() throws Exception;
	
	/**
	 * 获取请求内容
	 * @return
	 * @throws Exception
	 */
	public abstract String httpResult() throws Exception;

	/**
	 * 请求返回 空 是否立马切换ip
	 * @param httpResult
	 * 
	 * @return
	 * @throws Exception
	 */
	protected boolean returnEmptyChangeIp(String httpResult) throws Exception {
		return false;
	}
	
	/**
	 * 返回 true 继续请求...,可能是当前次页面异常
	 * @param httpResult
	 * @param document
	 * @param jsonObject
	 * @param returnType
	 * @return
	 * @throws Exception
	 */
	protected boolean requestAgain(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		return false;
	}
	
	/**
	 * 判断页面是否需要切换ip, 默认过滤 无法显示的 html 标题
	 * returnType = json时, 只过滤了开头不是{
	 * returnType = xml时, 只过滤了开头不是<
	 * returnType = other时, 不做过滤处理
	 * 不符合要求的, 自己重写
	 * 
	 * @param httpResult
	 * @param document
	 * @param jsonObject
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	private final boolean needToChangeIpDefault(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean change = false;
		if(document!=null){
			change = document.title().contains("无法显示");
		}
		if(change) return true;
		if("json".equalsIgnoreCase(returnType)) change = null == jsonObject;
		else if("xml".equalsIgnoreCase(returnType)) change = CrawlerUtil.notXml(httpResult);
		return change;
	}
	
	/**
	 * 基础识别封ip, 有需要自己处理
	 * 
	 * @param httpResult
	 * @param document
	 * @param jsonObject
	 * @param returnType 返回类型:json; xml; other; returnType != json/xml时, 不做过滤处理
	 * @return
	 */
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		return this.needToChangeIpDefault(httpResult, document, jsonObject, returnType);
	}
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	protected boolean exceptionNeedToChangeIp(Exception e) {
		return false;
	}
	
	/**
	 * 获取 请求内容(自带 headerMap)
	 * @param url
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyGet(String url, String returnType) {
		return this.httpProxyGet(url, null, returnType);
	}
	
	/**
	 * 获取 请求内容
	 * @param httpClientSessionVo
	 * @param url
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyGet(MyHttpClientSessionVo httpClientSessionVo, String url, String returnType) {
		return this.httpProxy(httpClientSessionVo, MyHttpClientUtil.httpGet, url, null, null, null, returnType);
	}
	
	/**
	 * 获取 请求内容
	 * @param url
	 * @param headerMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final MyHttpClientResultVo httpProxyResultVoGet(String url, String returnType) {
		return this.httpProxyResultVo(null, MyHttpClientUtil.httpGet, url, null, null, null, returnType);
	}
	/**
	 * 获取 请求内容
	 * @param url
	 * @param headerMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final MyHttpClientResultVo httpProxyResultVoGet(MyHttpClientSessionVo httpClientSessionVo,String url, String returnType) {
		return this.httpProxyResultVo(httpClientSessionVo, MyHttpClientUtil.httpGet, url, null, null, null, returnType);
	}
	/**
	 * 获取 请求内容
	 * @param url
	 * @param headerMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final MyHttpClientResultVo httpProxyResultVoPost(MyHttpClientSessionVo httpClientSessionVo,String url,Map<String, Object> paramMap,String returnType) {
		return this.httpProxyResultVo(httpClientSessionVo, MyHttpClientUtil.httpPost, url, paramMap, null, null, returnType);
	}
	/**
	 * 获取 请求内容
	 * @param url
	 * @param headerMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final MyHttpClientResultVo httpProxyResultVoPost(MyHttpClientSessionVo httpClientSessionVo,String url,String httpContent,String returnType) {
		return this.httpProxyResultVo(httpClientSessionVo, MyHttpClientUtil.httpPost, url, null, httpContent, null, returnType);
	}
	
	/**
	 * 获取 请求内容
	 * @param url
	 * @param headerMap
	 * @param charset
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyGet(String url, String charset, String returnType) {
		return this.httpProxy(null, MyHttpClientUtil.httpGet, url, null, null, charset, returnType);
	}

	/**
	 * 获取 请求内容
	 * @param url
	 * @param paramMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyPost(String url, Map<String, Object> paramMap, String returnType) {
		return this.httpProxy(null, MyHttpClientUtil.httpPost, url, paramMap, null, null, returnType);
	}
	
	/**
	 * 获取 请求内容
	 * @param httpClientSessionVo
	 * @param url
	 * @param paramMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyPost(MyHttpClientSessionVo httpClientSessionVo, String url, Map<String, Object> paramMap, String returnType) {
		return this.httpProxy(httpClientSessionVo, MyHttpClientUtil.httpPost, url, paramMap, null, null, returnType);
	}
	
	/**
	 * 获取 请求内容
	 * @param httpClientSessionVo
	 * @param url
	 * @param paramMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyPost(MyHttpClientSessionVo httpClientSessionVo, String url,String returnType) {
		return this.httpProxy(httpClientSessionVo, MyHttpClientUtil.httpPost, url, null, null, null, returnType);
	}
	
	/**
	 * 获取 请求内容
	 * @param httpClientSessionVo
	 * @param url
	 * @param paramMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyPost(MyHttpClientSessionVo httpClientSessionVo, String url,String httpContent, String returnType) {
		return this.httpProxy(httpClientSessionVo, MyHttpClientUtil.httpPost, url, null, httpContent, null, returnType);
	}
	
	/**
	 * 
	 * @param url
	 * @param paramMap
	 * @param headerMap
	 * @param charset
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyPost(String url, Map<String, Object> paramMap, String charset, String returnType) {
		return this.httpProxy(null, MyHttpClientUtil.httpPost, url, paramMap, null, charset, returnType);
	}
	
	/**
	 * 
	 * @param url
	 * @param httpContent
	 * @param headerMap
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyPost(String url, String httpContent, String returnType) {
		return this.httpProxy(null, MyHttpClientUtil.httpPost, url, null, httpContent, null, returnType);
	}
	
	/**
	 * 
	 * @param url
	 * @param httpContent
	 * @param headerMap
	 * @param charset
	 * @param returnType 返回类型:json; xml; other;
	 * @return
	 */
	protected final String httpProxyPost(String url, String httpContent, String charset, String returnType) {
		return this.httpProxy(null, MyHttpClientUtil.httpPost, url, null, httpContent, charset, returnType);
	}
	
	/**
	 * 获取 请求内容
	 * @param httpClientSessionVo
	 * @param sendType
	 * @param url
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param charset
	 * @param returnType 返回类型:json; xml; other;
	 * 
	 * @return
	 */
	private final String httpProxy(MyHttpClientSessionVo httpClientSessionVo, String sendType, String url,
			Map<String, Object> paramMap, String httpContent, String charset, String returnType) {
		MyHttpClientResultVo resultVo = this.httpProxyResultVo(httpClientSessionVo, sendType, url, paramMap, httpContent, charset, returnType);
		return null == resultVo ? null : resultVo.getHttpResult();
	}
	
	/**
	 * 获取 请求内容
	 * @param httpClientSessionVo
	 * @param sendType
	 * @param url
	 * @param paramMap
	 * @param httpContent
	 * @param headerMap
	 * @param charset
	 * @param returnType 返回类型:json; xml; other;
	 * 
	 * @return
	 */
	public final MyHttpClientResultVo httpProxyResultVo(MyHttpClientSessionVo httpClientSessionVo, String sendType, String url,
			Map<String, Object> paramMap, String httpContent, String charset, String returnType) {
		try {
			String params = "";
			if(null != paramMap && !paramMap.isEmpty() && sendType.equalsIgnoreCase(MyHttpClientUtil.httpPost)) params += "\r\nparamMap:" + MyJsonTransformUtil.writeValue(paramMap);
			if(StringUtils.isNotEmpty(httpContent)) params += "\r\nhttpContent:" + httpContent;
			logger.info(this.jobDetail.toStr() + ", httpClient proxy请求地址:" + url + params);
			MyHttpClientResultVo resultVo = null;
			String httpResult = null;
			while(true) {
				try {
					if(this.isTimeout()) return null;
					resultVo = ProxyUtil.httpProxyResultVo(httpClientSessionVo, this.threadMark,
							url, sendType, paramMap, httpContent, this.headerMap, this.getCrawlExt().getOneWaitTime(), charset);
					httpResult = null == resultVo ? null : resultVo.getHttpResult();
					if("NoHttpResponseException".equals(httpResult)
							||"ConnectionException".equals(httpResult)
							||"Readtimedout".equals(httpResult)
							|| this.returnEmptyChangeIp(httpResult)) {
						if(this.doProxy) this.changeProxy();
						continue ;
					}
					Document document = null;
					if( httpResult ==null ||httpResult.contains("502 Bad Gateway") || httpResult.contains("502 Proxy Error")) {
						if(++this.httpErrorTryAmount > maxHttpErrorTryAmount) {
							this.httpErrorTryAmount = 0;	// 重置
							if(this.getCrawlExt().getBackupHttpResponse()) logger.warn(httpResult);
							if(this.doProxy) this.changeProxy();
						} else Thread.sleep(sleepTime);
						continue ;
					}
					this.httpErrorTryAmount = 0;	// 重置
					if(httpResult!=null)httpResult = httpResult.trim();
					
					Object jsonObject = null;
					if("json".equalsIgnoreCase(returnType)) jsonObject = MyJsonTransformUtil.isJson(httpResult);

					// 不切换ip继续请求
					if(this.requestAgain(httpResult, document, jsonObject, returnType)) {
						Thread.sleep(sleepTime);
						continue ;
					}
					
					// 是否切换ip
					if(this.doProxy && this.needToChangeIp(httpResult, document, jsonObject, returnType)) {
						if(this.getCrawlExt().getBackupHttpResponse()) logger.warn(this.jobDetail.toStr() + ", httpClient 返回:" + httpResult);
						this.changeProxy();
						continue ;
					}
					
					if(this.getCrawlExt().getBackupHttpResponse()) logger.info(this.jobDetail.toStr() + ", httpClient 返回:" + httpResult);
					return resultVo;
				} catch(Exception e) {
					logger.error(this.jobDetail.toStr() + ", httpClient proxy 异常:" + httpResult + "\r", e);
					if(this.exceptionNeedToChangeIp(e) && this.doProxy) this.changeProxy(); 
					else MyThreadUtils.sleep(sleepTime);
				}
			}
		} catch(Exception e) {
			logger.error(this.jobDetail.toStr() + ", logger 异常:\r", e);
		}
		return null;
	}
	
	/**
	 * 切换ip
	 */
	protected final Object[] changeProxy() {
		return ProxyUtil.changeProxy(this.threadMark, this.useType, this.jobDetail.getTimerJob().getJobId(), this.jobDetail.getPageType());
	}
	
	/**
	 * 获取是否是共享航班 Y/N
	 * @param airlineCode
	 * @return
	 */
	public final String getShareFlight(String airlineCode) {
		CrawlerType crawlerType = CrawlerType.getCrawlerType(this.jobDetail.getPageType());
		return crawlerType.getCrawlerFlag().equalsIgnoreCase(airlineCode) ? "N" : "Y";
	}
	
	/**
	 * 是否是运行舱位
	 * @param cabin
	 * @return
	 */
	public final boolean allowCabin(String cabin) {
		logger.debug(this.jobDetail.toStr() + ", 舱位不符合要求:" + cabin);
		return allowCabin(this.jobDetail.getTimerJob(), cabin);
	}
	
	/**
	 * 是否是运行舱位
	 * @param timerJob
	 * @param cabin
	 * @return
	 */
	public final static boolean allowCabin(TimerJob timerJob, String cabin) {
		String[] allowCabins = timerJob.getAllowCabins();
		if(null == allowCabins || 0 == allowCabins.length
				|| (1 == allowCabins.length && StringUtils.isEmpty(allowCabins[0]))) return true;
		return ArrayUtils.contains(allowCabins, cabin);
	}
	
	/**
	 * 清空所有临时属性
	 * @param timerJob
	 */
	public final static void clearAllTemp(TimerJob timerJob) {
		MyCrawlerLogger.getCrawlerLogger(timerJob).info("清空当前类型[" + timerJob.getTimerJobKey() + "]临时属性开始...");
		baseJobParamMap.remove(timerJob.getTimerJobKey());
		MyCrawlerLogger.getCrawlerLogger(timerJob).info("清空当前类型[" + timerJob.getTimerJobKey() + "]临时属性结束...");
	}
	
	/**
	 * 获取基础参数
	 * @return
	 */
	protected final Map<String, Object> getBaseJobParamMap() {
		return baseJobParamMap.get(this.getTimerJob().getTimerJobKey());
	}
	
	/**
	 * 设置基础参数
	 * @param paramMap
	 */
	protected final void putBaseJobParamMap(Map<String, Object> paramMap) {
		baseJobParamMap.put(this.getTimerJob().getTimerJobKey(), paramMap);
	}
	
	/**
	 * 获取基础属性
	 * @return
	 */
	public CrawlExt getCrawlExt() {
		return CrawlExt.getCrawlExt(this.getPageType());
	}
	
	/**
	 * 
	 * @return
	 */
	public JobDetail getJobDetail() {
		return jobDetail;
	}
	
	/**
	 * 
	 * @return
	 */
	public TimerJob getTimerJob() {
		return this.jobDetail.getTimerJob();
	}

	/**
	 * 获取类型
	 * @return
	 */
	public String getPageType() {
		return this.jobDetail.getPageType();
	}
	
	/**
	 * 线程标识
	 * @return
	 */
	public String getThreadMark() {
		return threadMark;
	}
	
	public String getUseType() {
		return useType;
	}

	/**
	 * 是否要代理
	 * @return
	 */
	public boolean getDoProxy() {
		return doProxy;
	}

	/**
	 * 是否超时
	 * @return
	 */
	public final boolean isTimeout() {
		return System.currentTimeMillis() - this.startTime >= this.getCrawlExt().getMaxWaitTime();
	}
	
	
}