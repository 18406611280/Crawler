package com.aft.crawl.bean;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aft.utils.MyDefaultProp;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 采集基础信息
 * 
 */
public class CrawlCommon implements Serializable {

	private static final long serialVersionUID = -4182247944857883866L;

	private final static Logger logger = Logger.getLogger(CrawlCommon.class);

	// 获取操作者地址
	private String operatorUrl;
	
	// 基础配置地址
	private String extUrl;
	
	// 任务地址
	private String timeJobUrl;
	
	// 任务详细地址
	private String jobDetailUrl;
	
	// 操作者在线地址
	private String processorOnlineUrl;
	
	// 任务更新地址
	private String jobDetailResultUrl;
	
	// 任务是否还有
	private String jobDetailExistsUrl;
	
	// y/n
	private boolean saveFile;

	private String saveFileUrl;
	
	// y/n
	private boolean saveOther;
	
	private String saveOtherUrl;
	
	private String mqBrokerList;
	
	private String mqSaveDataTopic;
	
	private String mqQueryJobDetailTopic;
	
	private String mqUpdateJobDetailTopic;
	
	private String zookeeper;
	
	private String saveCrawlerInfoUrl;
	
	
	// 获取代理地址
	private String proxyIpUrl;
	
	private String allProxyIpUrl;

	// 释放代理地址
	private String proxyInvalidUrl;
	
	// 线程睡眠最长时间(多久没动静, 后续解锁ip!) 毫秒
	private Integer proxySleepMaxTime;
	
	// 切换代理最短间隔(毫秒)
	private Integer changeProxySplit;
	
	private static CrawlCommon crawlCommon;
	
	private CrawlCommon() {}
	
	/**
	 * 请求获取基础信息
	 */
	@SuppressWarnings("unchecked")
	public synchronized static CrawlCommon loadCommon() {
		if(MyDefaultProp.getLocalTest()) {
			crawlCommon = new CrawlCommon();
			crawlCommon.operatorUrl = null;
			crawlCommon.extUrl = null;
			crawlCommon.jobDetailUrl = null;
			crawlCommon.processorOnlineUrl = null;
			crawlCommon.jobDetailResultUrl = null;
			
			crawlCommon.mqBrokerList = null;
			crawlCommon.mqSaveDataTopic = null;
			
			crawlCommon.saveFile = false;
			crawlCommon.saveFileUrl = null;
			
			crawlCommon.mqQueryJobDetailTopic = null;
			crawlCommon.mqUpdateJobDetailTopic = null;
			crawlCommon.zookeeper = null;
			
			crawlCommon.proxyIpUrl = "http://120.76.199.56:8081/ipProxy/proxy.action";
			crawlCommon.allProxyIpUrl = "http://120.76.199.56:8081/ipProxy/all.action";
			crawlCommon.proxyInvalidUrl = "http://120.76.199.56:8081/ipProxy/updateUsed.action";
			crawlCommon.proxySleepMaxTime = 1800000;
			crawlCommon.changeProxySplit = 2000;
			return crawlCommon;
		}
		try {
			logger.info(MyDefaultProp.getOperatorName() + ", 请求获取基础信息开始...");
			String result = MyHttpClientUtil.get(MyDefaultProp.getCommonInfoUrl());
			logger.info(MyDefaultProp.getOperatorName() + ", 请求获取基础信息返回:" + result);
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(result, Map.class);
			if(null ==crawlCommon) crawlCommon = new CrawlCommon();
			crawlCommon.operatorUrl = resultMap.get("operatorUrl").toString().trim();
			crawlCommon.extUrl = resultMap.get("extUrl").toString().trim();
			crawlCommon.timeJobUrl = resultMap.get("timeJobUrl").toString().trim();
			crawlCommon.jobDetailUrl = resultMap.get("jobDetailUrl").toString().trim();
			crawlCommon.processorOnlineUrl = resultMap.get("processorOnlineUrl").toString().trim();
			crawlCommon.jobDetailResultUrl = resultMap.get("jobDetailResultUrl").toString().trim();
			crawlCommon.jobDetailExistsUrl = resultMap.get("jobDetailExistsUrl").toString().trim();
			
			crawlCommon.saveFile = "Y".equalsIgnoreCase(resultMap.get("saveFile").toString().trim());
			crawlCommon.saveFileUrl = resultMap.get("saveFileUrl").toString().trim();
			crawlCommon.saveOther = "Y".equalsIgnoreCase(resultMap.get("saveOther").toString().trim());
			crawlCommon.saveOtherUrl = resultMap.get("saveOtherUrl").toString().trim();
			
			crawlCommon.mqBrokerList = resultMap.get("mqBrokerList").toString().trim();
			crawlCommon.mqSaveDataTopic = resultMap.get("mqSaveDataTopic").toString().trim();
			crawlCommon.mqQueryJobDetailTopic = resultMap.get("mqQueryJobDetailTopic").toString().trim();
			crawlCommon.mqUpdateJobDetailTopic = resultMap.get("mqUpdateJobDetailTopic").toString().trim();
			crawlCommon.zookeeper = resultMap.get("zookeeper").toString().trim();
			crawlCommon.saveCrawlerInfoUrl = resultMap.get("saveCrawlerInfoUrl").toString().trim();
			
			crawlCommon.proxyIpUrl = resultMap.get("proxyIpUrl").toString().trim();
			crawlCommon.allProxyIpUrl = resultMap.get("allProxyIpUrl").toString().trim();
			crawlCommon.proxyInvalidUrl = resultMap.get("proxyInvalidUrl").toString().trim();
			crawlCommon.proxySleepMaxTime = Integer.parseInt(resultMap.get("proxySleepMaxTime").toString().trim());
			crawlCommon.changeProxySplit = Integer.parseInt(resultMap.get("changeProxySplit").toString().trim());
		} catch(Exception e) {
			logger.error(MyDefaultProp.getOperatorName() + ", 请求获取基础信息异常:\r", e);
		}
		return crawlCommon;
	}

	public static String getOperatorUrl() {
		return crawlCommon.operatorUrl;
	}

	public static String getExtUrl() {
		return crawlCommon.extUrl;
	}
	
	public static String getTimeJobUrl() {
		return crawlCommon.timeJobUrl;
	}

	public static String getJobDetailUrl() {
		return crawlCommon.jobDetailUrl;
	}

	public static String getProcessorOnlineUrl() {
		return crawlCommon.processorOnlineUrl;
	}

	public static String getJobDetailResultUrl() {
		return crawlCommon.jobDetailResultUrl;
	}
	
	public static String getJobDetailExistsUrl() {
		return crawlCommon.jobDetailExistsUrl;
	}
	
	public static boolean getSaveFile() {
		return crawlCommon.saveFile;
	}

	public static String getSaveFileUrl() {
		return crawlCommon.saveFileUrl;
	}
	
	public static boolean getSaveOther() {
		return crawlCommon.saveOther;
	}

	public static String getSaveOtherUrl() {
		return crawlCommon.saveOtherUrl;
	}

	public static String getMqBrokerList() {
		return crawlCommon.mqBrokerList;
	}

	public static String getMqSaveDataTopic() {
		return crawlCommon.mqSaveDataTopic;
	}

	public static String getMqQueryJobDetailTopic() {
		return crawlCommon.mqQueryJobDetailTopic;
	}

	public static String getMqUpdateJobDetailTopic() {
		return crawlCommon.mqUpdateJobDetailTopic;
	}

	public static String getZookeeper() {
		return crawlCommon.zookeeper;
	}
	
	public static String getSaveCrawlerInfoUrl() {
		return crawlCommon.saveCrawlerInfoUrl;
	}

	public static String getProxyIpUrl() {
		return crawlCommon.proxyIpUrl;
	}
	
	public static String getAllProxyIpUrl() {
		return crawlCommon.allProxyIpUrl;
	}

	public static String getProxyInvalidUrl() {
		return crawlCommon.proxyInvalidUrl;
	}

	public static Integer getProxySleepMaxTime() {
		return crawlCommon.proxySleepMaxTime;
	}

	public static Integer getChangeProxySplit() {
		return crawlCommon.changeProxySplit;
	}
}