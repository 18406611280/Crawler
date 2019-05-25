package com.aft.crawl.socket.vo;

import java.io.Serializable;
import java.util.List;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.vo.CrawlResultBase;

public class SocketRequestVo implements Serializable {

	private static final long serialVersionUID = -3826381423647194907L;

	/**
	 * 1:请求航线
	 * 2:保存采集结果
	 */
	private int type;
	
	private int jobId;
	
	private String crawlMark;
	
	private JobDetail jobDetail;
	
	private List<CrawlResultBase> results;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public String getCrawlMark() {
		return crawlMark;
	}

	public void setCrawlMark(String crawlMark) {
		this.crawlMark = crawlMark;
	}

	public JobDetail getJobDetail() {
		return jobDetail;
	}

	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}

	public List<CrawlResultBase> getResults() {
		return results;
	}

	public void setResults(List<CrawlResultBase> results) {
		this.results = results;
	}
}