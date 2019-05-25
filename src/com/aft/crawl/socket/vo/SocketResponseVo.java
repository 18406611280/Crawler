package com.aft.crawl.socket.vo;

import java.io.Serializable;
import java.util.List;

import com.aft.crawl.bean.JobDetail;

public class SocketResponseVo implements Serializable {
	
	private static final long serialVersionUID = 6378033859747810547L;

	private int jobId;
	
	private String crawlMark;

	private List<JobDetail> jobDetails;
	
	public SocketResponseVo() { }

	public SocketResponseVo(int jobId, String crawlMark) {
		this.jobId = jobId;
		this.crawlMark = crawlMark;
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

	public List<JobDetail> getJobDetails() {
		return jobDetails;
	}

	public void setJobDetails(List<JobDetail> jobDetails) {
		this.jobDetails = jobDetails;
	}
}