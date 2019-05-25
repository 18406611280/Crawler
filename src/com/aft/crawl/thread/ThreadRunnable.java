package com.aft.crawl.thread;

import com.aft.crawl.bean.JobDetail;


public abstract class ThreadRunnable implements Runnable {

	// 采集类型
	protected final JobDetail jobDetail;
	
	// 线程类型
	protected final String threadType;
	
	/**
	 * 
	 * @param pageType
	 * @param threadType
	 */
	public ThreadRunnable(JobDetail jobDetail, String threadType) {
		this.jobDetail = jobDetail;
		this.threadType = threadType;
	}
	
	public JobDetail getJobDetail() {
		return jobDetail;
	}

	public String getThreadType() {
		return threadType;
	}

	public String toStr() {
		return this.threadType + (null != this.jobDetail ? "|" + this.jobDetail.toStr() : "");
	}
}