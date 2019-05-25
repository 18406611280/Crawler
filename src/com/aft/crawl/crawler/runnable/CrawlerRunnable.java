package com.aft.crawl.crawler.runnable;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.CrawlerController;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.ResultPost;
import com.aft.crawl.result.jobDetail.runnable.UpdateJobDetailRunnable;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.jobDetail.JobDetailResultVo;
import com.aft.crawl.thread.ThreadController;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.crawl.thread.ThreadType;
import com.aft.logger.MyCrawlerLogger;
import com.aft.swing.CrawlerWin;

/**
 * 采集线程
 */
public class CrawlerRunnable extends ThreadRunnable {

	// 采集主线程&代理标识
	private final String threadMark;
	
	/**
	 * 
	 * @param jobDetail
	 * @param threadMark
	 * @param threadMark 采集主线程&代理标识
	 */
	public CrawlerRunnable(JobDetail jobDetail, String threadMark) {
		super(jobDetail, ThreadType.crawlerType);
		this.threadMark = threadMark;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();	// 开始时间
		Logger logger = MyCrawlerLogger.getCrawlerLogger(this.jobDetail.getTimerJob());
		
		int crawlStatus = 4;	// 2: 采集成功; 3: 采集超时; 4: 异常,失败, 5:废弃
		JobDetailResultVo jobDetailResultVo = new JobDetailResultVo(this.jobDetail.getTimerJob().getJobId(), this.jobDetail.getPageType(), this.jobDetail.getJobDetailId());
		try {
			logger.info(this.toStr() + "-[" + this.threadMark + "] crawl 开始...");
			Crawler crawler = CrawlerController.getCrawler(this.jobDetail.getTimerJob().getJobId(), this.jobDetail.getPageType(), this.threadMark);
			if(crawler.getDoProxy()) ProxyUtil.startProxy(this.threadMark, crawler.getUseType(), this.jobDetail.getTimerJob().getJobId(), this.jobDetail.getPageType());
			
			// null --> 失败
			List<CrawlResultBase> crawlResults = crawler.crawlGo(this.jobDetail);
			if(null == crawlResults) crawlResults = new ArrayList<CrawlResultBase>();
			else {
				if(crawler.isTimeout()) crawlStatus = 3;
				else crawlStatus = 2;
				
//				ResultPost.muMemberPrice(this.threadMark, this.jobDetail, crawlResults);	// 处理东航会员价
				ResultPost.saveFileLogger(this.jobDetail.getTimerJob(), crawlResults);		// 特殊处理, 别乱动...
				ResultPost.filterResult(this.jobDetail.getTimerJob(), crawlResults);		// 排序,过滤
			}
			jobDetailResultVo.setResultAmount(crawlResults.size());
			jobDetailResultVo.setCrawlTime(System.currentTimeMillis() - startTime);
			logger.info(this.toStr() + "-[" + this.threadMark + "] finish[" + crawlResults.size() + "-" + jobDetailResultVo.getCrawlTime() + "]");
			CrawlerWin.logger(this.toStr() + " finish[" + crawlResults.size() + "-" + jobDetailResultVo.getCrawlTime() + "]");
			
			ResultPost.postResult(this.threadMark, this.jobDetail, crawlResults);
		} catch(Exception e) {
			logger.error(this.toStr() + "-[" + this.threadMark + "], exception:\r", e);
			CrawlerWin.logger(this.toStr() + ", exception:" + e);
		} finally {
			CrawlExt crawlExt = CrawlExt.getCrawlExt(this.jobDetail.getPageType());
			if(crawlExt.getUpdateJobPost()) {
				jobDetailResultVo.setStatus(crawlStatus);
				jobDetailResultVo.setCrawlTime(System.currentTimeMillis() - startTime);
				ThreadController.addThread(this.threadMark, new UpdateJobDetailRunnable(this.jobDetail, ThreadType.postJobDetailStatusType, jobDetailResultVo));
			}
			CrawlerController.releaseCrawler(this.jobDetail.getTimerJob().getJobId());
		}
	}
}