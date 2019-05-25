package com.aft.crawl.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.TimerJob;

public final class ThreadController {
	
	private final static Logger logger = Logger.getLogger(ThreadController.class);
	
	// 单线程map
	private final static Map<String, ExecutorService> singleThreadPoolMap = new HashMap<String, ExecutorService>();
	
	// 局部线程
	private final static CopyOnWriteArrayList<ThreadVo> threadVos = new CopyOnWriteArrayList<ThreadVo>();
	
	/**
	 * 添加线程
	 * @param threadMark
	 * @param threadRunnable
	 */
	public static void addThread(String threadMark, ThreadRunnable threadRunnable) {
		logger.debug(threadRunnable.toStr() + ", 添加线程开始...");
		ExecutorService threadPool = ThreadController.getThreadPool(threadRunnable.getJobDetail().getTimerJob(), threadMark, threadRunnable.getThreadType());
		if(null == threadPool || threadPool.isShutdown()) {
			logger.warn(threadRunnable.toStr() + ", 线程" + (null == threadPool ? "不存在" : "已shutdown") + "...");
			return ;
		}
		threadPool.execute(threadRunnable);
		logger.debug(threadRunnable.toStr() + ", 添加线程结束...");
	}
	
	/**
	 * 添加 singleThreadPool 线程
	 * 单线程
	 * @param threadRunnable
	 */
	public static void addSingleThreadPool(ThreadRunnable threadRunnable) {
		logger.debug(threadRunnable.toStr() + ", 添加线程开始...");
		if(!ThreadType.singleBackupPostRemoteFailFileType.equals(threadRunnable.getThreadType())
				&& !ThreadType.singleBackupPostDcFailFileType.equals(threadRunnable.getThreadType())
				&& !ThreadType.singleBackupPostMqFailFileType.equals(threadRunnable.getThreadType())
				&& !ThreadType.singleSaveCrawlInfoType.equals(threadRunnable.getThreadType())
				&& !ThreadType.singleBackupPostJobDetailFailFileType.equals(threadRunnable.getThreadType())) {
			logger.warn("singleThreadPoolMap 不支持[" + threadRunnable.getThreadType() + "]");
			return ;
		}
		ExecutorService singleThreadPool = singleThreadPoolMap.get(threadRunnable.getThreadType());
		if(null == singleThreadPool) {
			synchronized(singleThreadPoolMap) {
				singleThreadPool = singleThreadPoolMap.get(threadRunnable.getThreadType());
				if(null == singleThreadPool) {
					final String threadName = threadRunnable.getJobDetail().getPageType() + "-" + threadRunnable.getThreadType();
					singleThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, threadName);
						}
					});
					singleThreadPoolMap.put(threadRunnable.getThreadType(), singleThreadPool);
				}
			}
		}
		if(singleThreadPool.isShutdown()) {
			logger.warn("singleThreadPoolMap[" + threadRunnable.getThreadType() + "], 线程已shutdown...");
			return ;
		}
		singleThreadPool.execute(threadRunnable);
		logger.debug(threadRunnable.toStr() + ", 添加线程结束...");
	}
		
	
	/**
	 * 获取可用采集主线程 threadMark
	 * @param timerJob
	 * @return
	 */
	public static String getMainThreadMark(TimerJob timerJob) {
		ThreadVo threadVo = ThreadController.getThreadVo(timerJob.getJobId());
		if(null == threadVo) {
			synchronized(threadVos) {
				threadVo = ThreadController.getThreadVo(timerJob.getJobId());
				if(null == threadVo) threadVo = ThreadController.initThreadVo(timerJob);
			}
		}
		return threadVo.getThreadMain(ThreadType.crawlerType);
	}
	
	/**
	 * 线程是否完成
	 * @param jobId
	 * @param pageType
	 * @return
	 */
	public static boolean crawlFinish(int jobId, String pageType) {
		ThreadVo threadVo = ThreadController.getThreadVo(jobId);
		return null == threadVo ? true : threadVo.threadAllCompleted();
	}
	
	/**
	 * 重置线程
	 */
	public synchronized static void reloadThread() {
		for(TimerJob timerJob : TimerJob.getTimerJobs()) {
			ThreadVo threadVo = ThreadController.getThreadVo(timerJob.getJobId());
			if(null == threadVo) continue ;
			
			logger.info(timerJob.getPageType() + ", 重置线程数开始...");
			CrawlExt crawlExt = CrawlExt.getCrawlExt(timerJob.getPageType());
			threadVo.reloadChildrenThreadVos(timerJob.getIpAmount(), timerJob.getOneIpThread(),
					crawlExt.getSaveRemote(), crawlExt.getMqPost(), crawlExt.getSaveDc(),crawlExt.getSaveTs(), crawlExt.getUpdateJobPost());
			logger.info(timerJob.getPageType() + ", 重置线程数结束...");
		}
	}
	
	/**
	 * 返回当前类型 [主线程, 采集线程]数量
	 * @param pageType
	 * @return
	 */
	public static int[] getCrawlThreadAmount(TimerJob timerJob) {
		ThreadVo threadVo = ThreadController.getThreadVo(timerJob.getJobId());
		return null == threadVo ? new int[2] : threadVo.getCrawlThreadAmount();
	}
	
	/**
	 * 释放当前类型线程
	 * @param pageType
	 */
	public synchronized static void closeThreadPool(TimerJob timerJob) {
		logger.info(timerJob.getPageType() + "_" + timerJob.getJobId() + ", 关闭当前类型线程开始...");
		ThreadVo threadVo = ThreadController.getThreadVo(timerJob.getJobId());
		if(null == threadVo) {
			logger.info(timerJob.getPageType() + "_" + timerJob.getJobId() + ", 当前类型线程不存在...");
			return ;
		}
		threadVo.shutdownThreadPool();
		threadVos.remove(threadVo);
		logger.info(timerJob.getPageType() + "_" + timerJob.getJobId() + ", 关闭当前类型线程结束...");
	}

	/**
	 * 初始化当前类型
	 * @param pageType
	 */
	private static ThreadVo initThreadVo(TimerJob timerJob) {
		CrawlExt crawlExt = CrawlExt.getCrawlExt(timerJob.getPageType());
		ThreadVo threadVo = new ThreadVo(timerJob.getJobId(), timerJob.getIpAmount(), timerJob.getOneIpThread(),
				crawlExt.getSaveRemote(), crawlExt.getMqPost(), crawlExt.getSaveDc(),crawlExt.getSaveTs(),crawlExt.getUpdateJobPost());
		threadVos.add(threadVo);
		return threadVo;
	}
	
	/**
	 * 获取当前类型线程vo
	 * @param timerJob
	 * @param threadMark
	 * @param threadType
	 * @return
	 */
	private static ExecutorService getThreadPool(TimerJob timerJob, String threadMark, String threadType) {
		ThreadVo threadVo = ThreadController.getThreadVo(timerJob.getJobId());
		return null == threadVo ? null : threadVo.getThreadPool(threadMark, threadType);
	}
	
	/**
	 * 获取当前类型线程vo
	 * @param jobId
	 * @param pageType
	 * @return
	 */
	private static ThreadVo getThreadVo(int jobId) {
		for(ThreadVo threadVo : threadVos) {
			if(threadVo.getJobId() != jobId) continue ;
			return threadVo;
		}
		return null;
	}
}