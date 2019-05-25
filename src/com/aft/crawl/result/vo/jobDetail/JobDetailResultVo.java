package com.aft.crawl.result.vo.jobDetail;

import com.aft.utils.MyDefaultProp;


public class JobDetailResultVo {

	private int timerJobId;
	
	private String pageType;
	
	private int jobDetailId;
	
	// 0: 初始状态, 刚发布, 1: 已获取, 2: 采集成功; 3: 采集超时; 4: 异常,失败
	private int status;
	
	// 耗时
	private long crawlTime;
	
	private int resultAmount;
	
	private String operator;
	
	public JobDetailResultVo(int timerJobId, String pageType, int jobDetailId) {
		this.timerJobId = timerJobId;
		this.pageType = pageType;
		this.jobDetailId = jobDetailId;
		operator = MyDefaultProp.getOperatorName();
	}

	public int getTimerJobId() {
		return timerJobId;
	}

	public void setTimerJobId(int timerJobId) {
		this.timerJobId = timerJobId;
	}
	
	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		this.pageType = pageType;
	}

	public int getJobDetailId() {
		return jobDetailId;
	}

	public void setJobDetailId(int jobDetailId) {
		this.jobDetailId = jobDetailId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getCrawlTime() {
		return crawlTime;
	}

	public void setCrawlTime(long crawlTime) {
		this.crawlTime = crawlTime;
	}

	public int getResultAmount() {
		return resultAmount;
	}

	public void setResultAmount(int resultAmount) {
		this.resultAmount = resultAmount;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
}