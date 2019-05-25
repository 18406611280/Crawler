package com.aft.crawl.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerType;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

public class CrawlExt implements Serializable {

	private static final long serialVersionUID = 8455122777499554167L;

	private final static Logger logger = Logger.getLogger(CrawlExt.class);
	
	// 按各pageType, 如果是 common 则是基础
	private String pageType;
	
	// 获取任务类型, 1:mq, 2:远程缓存, 3:远程查库(2,3现在一起)
	private Integer jobType;
	
	// 获取任务类型, 1:mq, 2:远程action
	private Integer updateJobType;

	// 单次请求最大等待时间(毫秒)
	private Integer oneWaitTime;
	
	// 单次采集航程最大等待时间(毫秒)
	private Integer maxWaitTime;
	
	// y/n
	private boolean saveRemote;
	
	// y/n
	private boolean saveDc;
	
	// y/n
	private boolean saveTs;
	
	// 保存路径
	private String saveRemoteUrl;
	
	// 数据中心保存路径
	private String saveDcUrl;
	
	// LCC保存路径
	private String saveTsUrl;
	
	// --记录保存 y/n
	private boolean backupHttpResponse;
	
	// ---mq y/n
	private boolean mqPost;
	
	// ---是否更新任务结果 y/n
	private boolean updateJobPost;
	
	private static CopyOnWriteArrayList<CrawlExt> crawlExts = new CopyOnWriteArrayList<CrawlExt>();
	
	private CrawlExt(String pageType) {
		this.pageType = pageType;
	}
	
	/**
	 * 获取基础配置信息(缓存)
	 * 
	 * @param pageType
	 * @return
	 */
	public static CrawlExt getCrawlExt(String pageType) {
		for(CrawlExt crawlExt : crawlExts) {
			if(!crawlExt.getPageType().equals(pageType)) continue ;
			return crawlExt;
		}
		if(MyDefaultProp.getLocalTest()) {
			CrawlExt crawlExt = new CrawlExt(pageType);
			crawlExt.jobType = 2;
			crawlExt.updateJobType = 2;
			
			crawlExt.saveDc = false;
			crawlExt.saveDcUrl = null;
			
			crawlExt.saveTs = false;
			crawlExt.saveTsUrl = null;
			
//			crawlExt.saveTs = true;
//			crawlExt.saveTsUrl = "http://lccsystem.mytkt.cn/SaveFltData.ashx";
			
			crawlExt.backupHttpResponse = true;
			crawlExt.mqPost = false;
			crawlExt.updateJobPost = false;
			
			crawlExt.oneWaitTime = 30;
			crawlExt.maxWaitTime = 33;
			crawlExt.saveRemote = true;
			crawlExt.saveRemoteUrl = "";
			crawlExts.add(crawlExt);
			return crawlExt;
		}
		return null;
	}
	
	/**
	 * 获取基础配置信息(缓存)
	 * @return
	 */
	public static List<CrawlExt> getCrawlExts() {
		return crawlExts;
	}
	
	/**
	 * 获取基础配置信息(远程获取)
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized static boolean loadCrawlExts() {
		if(MyDefaultProp.getLocalTest()) {
			CrawlExt crawlExt = new CrawlExt(MyDefaultProp.getTestPageType());
			crawlExt.jobType = 2;
			crawlExt.updateJobType = 2;
			
			crawlExt.oneWaitTime = 20;
			crawlExt.maxWaitTime = 22;
			
			crawlExt.saveRemote = false;
			crawlExt.saveDc = false;
			crawlExt.saveRemoteUrl = null;
			crawlExt.saveDcUrl = null;
			crawlExt.saveTs = true;
			crawlExt.saveTsUrl = "http://lccsystem.mytkt.cn/SaveFltData.ashx";
//			crawlExt.saveTs = false;
//			crawlExt.saveTsUrl = null;
			
			
			crawlExt.backupHttpResponse = true;
			crawlExt.mqPost = false;
			crawlExt.updateJobPost = false;
			
			crawlExts.add(crawlExt);
			return true;
		}
			
		try {
			logger.info(MyDefaultProp.getOperatorName() + ", 获取基础配置信息[" + CrawlCommon.getExtUrl() + "]开始...");
			String result = MyHttpClientUtil.get(CrawlCommon.getExtUrl() + "?operator=" + MyDefaultProp.getOperatorName());
			logger.info(MyDefaultProp.getOperatorName() + ", 获取基础配置信息[" + CrawlCommon.getExtUrl() + "]返回:" + result);
			if("null".equalsIgnoreCase(result) || StringUtils.isEmpty(result)) {
				logger.info(MyDefaultProp.getOperatorName() + ", 返回基础配置信息为空...");
				return false;
			}
			List<Map> results = MyJsonTransformUtil.readValueToList(result, Map.class);
			if(results.isEmpty()) {
				logger.info(MyDefaultProp.getOperatorName() + ", 不存在基础配置信息, 清空所有基础配置信息...");
				crawlExts.clear();
				return true;
			}
			
			// 移除不存在的...
			loop: for(CrawlExt crawlExt : crawlExts) {
				for(Map<String, Object> resultMap : results) {
					if(resultMap.get("pageType").toString().trim().equals(crawlExt.getPageType())) continue loop;
				}
				crawlExts.remove(crawlExt);
			}
			
			// 修改或新增...
			CrawlExt crawlExt = null;
			for(Map<String, Object> resultMap : results) {
				String pageType = resultMap.get("pageType").toString().trim();
				CrawlerType crawlerType = CrawlerType.getCrawlerType(pageType);
				if(null == crawlerType) {	// 不支持
					logger.warn(MyDefaultProp.getOperatorName() + ", 采集类型[" + pageType + "]不支持...");
					CrawlerWin.logger(MyDefaultProp.getOperatorName() + ", 采集类型[" + pageType + "]不支持!");
					continue ;
				}
				crawlExt = CrawlExt.getCrawlExt(pageType);
				if(null == crawlExt) {	// 新增的
					crawlExt = new CrawlExt(pageType);
					crawlExts.add(crawlExt);
				}
				crawlExt.jobType = Integer.parseInt(resultMap.get("jobType").toString().trim());
				crawlExt.updateJobType = Integer.parseInt(resultMap.get("updateJobType").toString().trim());
				
				crawlExt.oneWaitTime = Integer.parseInt(resultMap.get("oneWaitTime").toString().trim());
				crawlExt.maxWaitTime = Integer.parseInt(resultMap.get("maxWaitTime").toString().trim());
				
				crawlExt.saveRemote = "Y".equalsIgnoreCase(resultMap.get("saveRemote").toString().trim());
				crawlExt.saveDc = "Y".equalsIgnoreCase(resultMap.get("saveDc").toString().trim());
				crawlExt.saveTs = "Y".equalsIgnoreCase(resultMap.get("saveTs").toString().trim());
				crawlExt.saveRemoteUrl = (String)resultMap.get("saveRemoteUrl");
				crawlExt.saveDcUrl = (String)resultMap.get("saveDcUrl");
				crawlExt.saveTsUrl = (String)resultMap.get("saveTsUrl");
				
				crawlExt.backupHttpResponse = "Y".equalsIgnoreCase(resultMap.get("backupHttpResponse").toString().trim());
				crawlExt.mqPost = "Y".equalsIgnoreCase(resultMap.get("mqPost").toString().trim());
				crawlExt.updateJobPost = "Y".equalsIgnoreCase(resultMap.get("updateJobPost").toString().trim());
			}
			return true;
		} catch(Exception e) {
			logger.error(MyDefaultProp.getOperatorName() + ", 获取基础配置信息[" + CrawlCommon.getExtUrl() + "]异常:\r", e);
		}
		return false;
	}
	
	public String getPageType() {
		return pageType;
	}
	
	public Integer getJobType() {
		return jobType;
	}
	
	public Integer getUpdateJobType() {
		return updateJobType;
	}

	public Integer getOneWaitTime() {
		return oneWaitTime * 1000;
	}

	public Integer getMaxWaitTime() {
		return maxWaitTime * 1000;
	}

	public boolean getSaveRemote() {
		return saveRemote;
	}

	public boolean getSaveDc() {
		return saveDc;
	}

	public String getSaveRemoteUrl() {
		return saveRemoteUrl;
	}

	public String getSaveDcUrl() {
		return saveDcUrl;
	}
	
	public boolean getBackupHttpResponse() {
		return backupHttpResponse;
	}

	public boolean getMqPost() {
		return mqPost;
	}

	public boolean getUpdateJobPost() {
		return updateJobPost;
	}

	public boolean getSaveTs() {
		return saveTs;
	}

	public String getSaveTsUrl() {
		return saveTsUrl;
	}

	public void setSaveRemoteUrl(String saveRemoteUrl) {
		this.saveRemoteUrl = saveRemoteUrl;
	}
	
}