package com.aft.crawl.result.crawlInfo.runnable;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.CrawlCommon;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.crawl.thread.ThreadType;
import com.aft.utils.http.MyHttpClientUtil;

public class CrawlInfoSaveRunnable extends ThreadRunnable {
	
	private final static Logger logger = Logger.getLogger(CrawlInfoSaveRunnable.class);
	
	private final String crawlInfo;
	
	public CrawlInfoSaveRunnable(JobDetail jobDetail, String crawlInfo) {
		super(jobDetail, ThreadType.singleSaveCrawlInfoType);
		this.crawlInfo = crawlInfo;
	}
	
	@Override
	public void run() {
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("criteria.crawlInfoType", "1");
			paramMap.put("criteria.crawlInfoOrg", this.jobDetail.getTimerJob().getParamMapValueByKey("crawlInfoOrg"));
			paramMap.put("criteria.crawlInfo", crawlInfo);
			String httpResult = MyHttpClientUtil.post(CrawlCommon.getSaveCrawlerInfoUrl(), paramMap);
			logger.info(this.toStr() + ", 新增航线返回:" + httpResult);
		} catch(Exception e) {
			logger.error(this.toStr() + ", 新增航线异常:" + this.crawlInfo + "\r", e);
		}
	}
}