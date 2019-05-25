package com.aft.crawl.proxy;

import java.io.Serializable;

import com.aft.crawl.bean.CrawlCommon;

public class ProxyVo implements Serializable {
	
	private static final long serialVersionUID = 7198509548371221751L;

	private String threadMark;
	
	private int jobId;
	
	private String proxyIp;
	
	private Integer proxyPort;
	
	private String realIp;
	
	private String ipRegion;
	
	private String queryFlag = "";
	
	private long startTime = 0L;
	
	// 是否切换中
	private boolean changing = false;
	
	public ProxyVo(String threadMark, int jobId) {
		this.threadMark = threadMark;
		this.jobId = jobId;
	}
	
	public String getThreadMark() {
		return threadMark;
	}
	
	public int getJobId() {
		return jobId;
	}

	public String getProxyIp() {
		return proxyIp;
	}

	public void setProxyIp(String proxyIp) {
		this.proxyIp = proxyIp;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}
	
	public String getRealIp() {
		return realIp;
	}

	public void setRealIp(String realIp) {
		this.realIp = realIp;
	}
	
	public String getIpRegion() {
		return ipRegion;
	}

	public void setIpRegion(String ipRegion) {
		this.ipRegion = ipRegion;
	}

	public String getQueryFlag() {
		return queryFlag;
	}

	public void setQueryFlag(String queryFlag) {
		this.queryFlag = queryFlag;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public boolean getChanging() {
		return changing;
	}

	public void setChanging(boolean changing) {
		this.changing = changing;
	}

	/**
	 * 是否可切换
	 * @return
	 */
	public boolean canChange() {
		boolean chang = !this.changing && System.currentTimeMillis() - this.startTime >= CrawlCommon.getChangeProxySplit();
		return chang;
	}
}