package com.aft.crawl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.TimerJob;
import com.aft.crawl.crawler.runnable.CrawlerMainRunnable;
import com.aft.crawl.result.post.runnable.PostFailResultAgainRunnable;
import com.aft.crawl.thread.ScheduledThreadPoolController;
import com.aft.crawl.thread.ThreadType;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyDefaultProp;

public final class CrawlerInit {
	
	private final static Logger logger = Logger.getLogger(CrawlerInit.class);
	
	// 运行线程池
	private final static ExecutorService mainThreadPool = Executors.newCachedThreadPool();
	
	// 单例
	private static CrawlerInit crawlerInit;
	
	private CrawlerInit() {
		logger.info("CrawlerInit 初始化实例开始...");
		this.scheduleTimer();				// 自动定时
		if(!MyDefaultProp.getLocalTest()) this.postFailResultAgainTimer();	// 自动提交失败的内容
		logger.info("CrawlerInit 初始化实例结束...");
	}
	
	/**
	 * 初始化
	 */
	public static CrawlerInit initController() {
		if(null != crawlerInit) return crawlerInit;
		synchronized(CrawlerInit.class) {
			if(null == crawlerInit) crawlerInit = new CrawlerInit();
			return crawlerInit;
		}
	}
	
	/**
	 * 自动采集定时本地任务
	 */
	private void scheduleTimer() {
		logger.info("CrawlerInit 初始化[scheduleTimer]本地任务开始...");
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if(CrawlerWin.isClosing()) return ;
					
					boolean loadOk = TimerJob.loadTimerJobs();	// 加载任务列表数据
					if(!loadOk) return ;
					
					for(TimerJob timerJob : TimerJob.getTimerJobs()) {
						Boolean run = CrawlerMainRunnable.getTimerJobRun(timerJob);
						if(run) {
							logger.info("CrawlerInit 执行定时, 类型[" + timerJob.getTimerJobKey() + "], 正在运行...");
							continue ;
						}
						logger.info("CrawlerInit 执行定时, 类型[" + timerJob.getTimerJobKey() + "], 开启线程...");
						CrawlerInit.mainThreadPool.execute(new CrawlerMainRunnable(timerJob));
					}
				} catch(Exception e) {
					logger.error("CrawlerInit 执行定时任务异常\r", e);
				}
			}
		}, 0, ("all_realtime".equalsIgnoreCase(MyDefaultProp.getOperatorName())? 1000 : 10 * 1000));
		logger.info("CrawlerInit 初始化[scheduleTimer]本地任务结束...");
	}
	
	/**
	 * 保存失败任务在次提交
	 */
	private void postFailResultAgainTimer() {
		logger.info("CrawlerInit 初始化[postFailResultAgainTimer]开始...");
		if(MyDefaultProp.getBackupOpen()) ScheduledThreadPoolController.schedule(new PostFailResultAgainRunnable(ThreadType.singleBackupPostMqFailFileType), 1, TimeUnit.MINUTES);
//		if(MyDefaultProp.getBackupOpen()) ScheduledThreadPoolController.schedule(new PostFailResultAgainRunnable(ThreadType.singleBackupPostDcFailFileType), 1, TimeUnit.MINUTES);
		if(MyDefaultProp.getBackupOpen()) ScheduledThreadPoolController.schedule(new PostFailResultAgainRunnable(ThreadType.singleBackupPostRemoteFailFileType), 1, TimeUnit.MINUTES);
//		if(MyDefaultProp.getBackupOpen()) ScheduledThreadPoolController.schedule(new PostFailResultAgainRunnable(ThreadType.singleBackupPostJobDetailFailFileType), 1, TimeUnit.MINUTES);
		logger.info("CrawlerInit 初始化[postFailResultAgainTimer]结束...");
	}
}