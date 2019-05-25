package com.aft.crawl.crawler.runnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.bean.TimerJob;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.CrawlerController;
import com.aft.crawl.crawler.CrawlerJob;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.thread.ThreadController;
import com.aft.logger.MyCrawlerLogger;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.MyStringUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 主线程
 */
public class CrawlerMainRunnable implements Runnable {

	// 各任务运行状态
	private final static Map<String, Boolean> timerJobRunMap = new HashMap<String, Boolean>();
	
	// 任务
	private final TimerJob timerJob;
	
	public CrawlerMainRunnable(TimerJob timerJob) {
		this.timerJob = timerJob;
	}

	@Override
	public void run() {
		Logger logger = MyCrawlerLogger.getCrawlerLogger(this.timerJob);
		CrawlerType crawlerType = CrawlerType.getCrawlerType(this.timerJob.getPageType());
		logger.warn("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 定时开始...");
		
		synchronized(CrawlerMainRunnable.class) {
			if(CrawlerMainRunnable.getTimerJobRun(this.timerJob)) {
				logger.warn("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 正在运行...");
				return ;
			}
			CrawlerMainRunnable.timerJobRunMap.put(this.timerJob.getTimerJobKey(), true);	// 锁定
		}
		boolean noJobs = true;
		long startTime = System.currentTimeMillis();
		logger.info("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "]开始...");

		try {
			JobDetail jobDetail = null;
			Iterator<JobDetail> itJd = null;
			while(true) {
				if(CrawlerWin.isClosing()) break ;
				List<JobDetail> jobDetails = CrawlerJob.getJobDetails(this.timerJob);
				if(null == jobDetails || jobDetails.isEmpty()) {
					logger.info("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 不存在可执行任务...");
					break ;
				}
				
				itJd = jobDetails.iterator();
				while(itJd.hasNext()) {
					jobDetail = itJd.next();
					itJd.remove();
					String threadMark = null;	// 采集主线程&代理标识
					while(null == threadMark) {
						MyThreadUtils.sleep(5);
						threadMark = ThreadController.getMainThreadMark(this.timerJob);
					}
					logger.debug("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 获取threadMark:" + threadMark);
					ThreadController.addThread(threadMark, new CrawlerRunnable(jobDetail, threadMark));
				}
				noJobs = false;
				if(MyDefaultProp.getLocalTest()) break ;
			}
		} catch(Exception e) {
			logger.error("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 开始异常:\r", e);
			CrawlerWin.logger("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 开始异常:" + e);
		} finally {
			CrawlerMainRunnable.timerJobRunMap.put(this.timerJob.getTimerJobKey(), false);	// 释放
			if(noJobs) return ;
			
			long waitStartTime = System.currentTimeMillis();
			logger.info("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 所有线程已运行, 等待线程处理完成...");
			while(!ThreadController.crawlFinish(this.timerJob.getJobId(), this.timerJob.getPageType())) {	// 采集线程是否完成
				if(System.currentTimeMillis() - waitStartTime >= 120 * 1000) {
					logger.info("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 等待线程处理完成中, 强制退出等待...");
					break ;
				}
				MyThreadUtils.sleep(1000);
			}
			Crawler.clearAllTemp(this.timerJob);	// 清空所有临时属性	
			CrawlerController.delAllCrawler(this.timerJob.getJobId());	// 删除当前类型所有采集者 
			ProxyUtil.updateProxyUsed(this.timerJob.getJobId());		// 释放代理
			ThreadController.closeThreadPool(this.timerJob);			// 关闭线程
			
			String haoShi = MyStringUtil.toHHmmss(startTime);
			logger.info("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 采集完成, 总耗时[" + haoShi + "]");
			CrawlerWin.logger("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 采集完成, 总耗时[" + haoShi + "]");
		}
		System.gc();
		logger.warn("CrawlerMainRunnable 类型[" + crawlerType.toStr() + "], 定时结束...");
		MyCrawlerLogger.closeSaveFile(this.timerJob);
	}
		
	/**
	 * 是否正在运行
	 * @param timerJob
	 * @return
	 */
	public static boolean getTimerJobRun(TimerJob timerJob) {
		Boolean run = CrawlerMainRunnable.timerJobRunMap.get(timerJob.getTimerJobKey());
		return null == run ? false : run;
	}
}