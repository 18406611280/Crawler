package com.aft.crawl.crawler;

import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerType;

public class CrawlerController {
	
	private final static Logger logger = Logger.getLogger(CrawlerController.class);
	
	// 重用Crawler对象 vo
	private final static CopyOnWriteArrayList<CrawlerVo> crawlerVos = new CopyOnWriteArrayList<CrawlerVo>();

	/**
	 * 获取采集者
	 * @param jobId
	 * @param pageType
	 * @param threadMark 主线程&代理标识
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Crawler getCrawler(int jobId, String pageType, String threadMark) throws Exception {
		logger.debug(pageType + ", jobId[" + jobId + "], 准备获取实例...");
		synchronized(threadMark) {
			for(CrawlerVo crawlerVo : crawlerVos) {
				if(null == crawlerVo) {
					crawlerVos.remove(crawlerVo);
					continue ;
				}
				if(!crawlerVo.getEnable()) continue ;
				if(jobId != crawlerVo.getJobId()) continue ;
				if(!crawlerVo.getCrawler().getThreadMark().equals(threadMark)) continue ;
				
				crawlerVo.enable = false;
				logger.debug(pageType + ", jobId[" + jobId + "], 获取实例:" + crawlerVo.getCrawler());
				return crawlerVo.getCrawler();
			}
			
			// vo根据 ext 修改数量的减少暂时不变,数量改变不多...搞起来工程大...
			Class<?> clazz = CrawlerType.getCrawlerClass(pageType);
			if(null == clazz) throw new RuntimeException(pageType + ", jobId[" + jobId + "], 获取不到Crawler实例...");
			
			Crawler crawler = (Crawler)clazz.getConstructor(String.class).newInstance(threadMark);
			CrawlerVo crawlerVo = new CrawlerController().new CrawlerVo(jobId, crawler);
			crawlerVo.enable = false;
			crawlerVos.add(crawlerVo);
			logger.debug(pageType + ", jobId[" + jobId + "], 获取实例:" + crawler);
			return crawler;
		}
	}
	
	/**
	 * 释放采集者(变为可用)
	 * @param jobId
	 * @param pageType
	 * 
	 * @return
	 */
	public static void releaseCrawler(int jobId) {
		for(CrawlerVo crawlerVo : crawlerVos) {
			if(jobId != crawlerVo.getJobId()) continue ;
			crawlerVo.enable = true;
			logger.debug("jobId[" + jobId + "], 释放实例:" + crawlerVo.getCrawler());
			break ;
		}
	}
	
	/**
	 * 删除当前类型所有采集者
	 * @param jobId
	 * @param pageType
	 * 
	 * @return
	 */
	public static void delAllCrawler(int jobId) {
		for(CrawlerVo crawlerVo : crawlerVos) {
			if(jobId != crawlerVo.getJobId()) continue ;
			crawlerVos.remove(crawlerVo);
			logger.debug("jobId[" + jobId + "], 移除实例:" + crawlerVo.getCrawler());
		}
	}
	
	private class CrawlerVo {

		private final int jobId;
		
		private final Crawler crawler;
		
		private boolean enable;
		
		public CrawlerVo(int jobId, Crawler crawler) {
			this.jobId = jobId;
			this.crawler = crawler;
		}
		
		public int getJobId() {
			return jobId;
		}
		
		public Crawler getCrawler() {
			return crawler;
		}
		
		public boolean getEnable() {
			return enable;
		}
	}
}