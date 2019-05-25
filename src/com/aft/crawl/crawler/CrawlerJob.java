package com.aft.crawl.crawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.bean.TimerJob;


public class CrawlerJob {
	
	private final static Logger logger = Logger.getLogger(CrawlerJob.class);
	
	// mq任务
	public final static int mqJobType = 1;
	
	// 远程任务
	public final static int remoteJobType = 2;
	
	/**
	 * 获取任务
	 * @param timerJob
	 * @return
	 */
	public static List<JobDetail> getJobDetails(TimerJob timerJob) {
		CrawlExt crawlExt = CrawlExt.getCrawlExt(timerJob.getPageType());
		if(null == crawlExt) return null;

		logger.debug("数据采集程序, [" + timerJob.getPageType() + "-" + crawlExt.getJobType() + "]获取任务开始");
		List<JobDetail> jobDetails = new ArrayList<JobDetail>();
		try {
			if(mqJobType == crawlExt.getJobType()) jobDetails = JobDetail.mqJobs(timerJob);					// mq任务方式
			else if(remoteJobType == crawlExt.getJobType()) jobDetails = JobDetail.remoteJobs(timerJob);	// remote获取任务...
		} catch(Exception e) {
			logger.error("数据采集程序, [" + timerJob.getPageType() + "-" + crawlExt.getJobType() + "]获取任务异常:\r", e);
		}
		
		logger.debug("数据采集程序, [" + timerJob.getPageType() + "-" + crawlExt.getJobType() + "]获取任务结束");
		return jobDetails;
	}
}