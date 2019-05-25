package com.aft.crawl.result.vo.b2c;

import java.math.BigDecimal;
import java.util.Date;

import com.aft.crawl.SwitchTypeEnum;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.utils.date.MyDateFormatUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CrawlResultB2C extends CrawlResultBase {
	
	private static final long serialVersionUID = 4747797378906619986L;

	/** 航司二字码 */
	private String airlineCode;
	
	/** 出发城市Cd */
	private String depCode;
	
	/** 到达城市Cd */
	private String desCode;

	/** 航班号 */
	private String fltNo;
	
	/** 共享航班Y/N */
	private String shareFlight;
	
	/** 舱位 */
	private String cabin;
	
	/** 出发日期(yyyy-MM-dd) */
	private String depDate;
	
	/** 出发时间 */
	private String depTime;
	
	/** 到达时间 */
	private String desTime;
	
	/** 开始日期(yyyy-MM-dd, 默认跟 depDate一样) */
	private String startDate;
	
	/** 结束日期(yyyy-MM-dd, 默认跟开始日期一样) */
	private String endDate;
	
	/** 票面价 */
	private BigDecimal ticketPrice;
	
	/** 销售价(如果就一个价格,跟票面价一样) */
	private BigDecimal salePrice;
	
	/** 剩余座位数(如果采集结果没提供, 默认10) */
	private Integer remainSite;
	
	/** 任务id */
	private Integer jobId;
	
	private String type;
	
	@JsonIgnore
	private String temp;
	
	public CrawlResultB2C(JobDetail jobDetail, String airlineCode, String fltNo, String shareFlight, String depCode, String desCode, String depDate, String cabin) {
		super(jobDetail.getPageType(), jobDetail.getCrawlMark());
		this.airlineCode = airlineCode;
		this.fltNo = fltNo;
		this.shareFlight = shareFlight;
		this.depCode = depCode;
		this.desCode = desCode;
		this.depDate = depDate;
		this.cabin = cabin;
		this.jobId = jobDetail.getTimerJob().getJobId();
		
		this.startDate = depDate;
		this.endDate = depDate;
	}
	
	@Override
	public String toSaveFileStr() {
		SwitchTypeEnum switchTypeEnum = SwitchTypeEnum.getSwitchTypeEnum(this.getPageType());
		return switchTypeEnum.name()
					+ "," + this.airlineCode
					+ "," + this.depCode
					+ "," + this.desCode
					+ "," + this.depDate
//					+ "," + this.depTime
//					+ "," + this.desTime
					+ "," + this.fltNo
					+ "," + this.cabin
					+ "," + this.ticketPrice
					+ "," + this.remainSite
					+ "," + this.type
					+ "," + MyDateFormatUtils.SDF_YYYYMMDDHHMMSSSSS().format(new Date());
	}

	public String getAirlineCode() {
		return airlineCode;
	}

	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}

	public String getDepCode() {
		return depCode;
	}

	public void setDepCode(String depCode) {
		this.depCode = depCode;
	}

	public String getDesCode() {
		return desCode;
	}

	public void setDesCode(String desCode) {
		this.desCode = desCode;
	}
	
	public String getFltNo() {
		return fltNo;
	}

	public void setFltNo(String fltNo) {
		this.fltNo = fltNo;
	}
	
	public String getShareFlight() {
		return shareFlight;
	}

	public void setShareFlight(String shareFlight) {
		this.shareFlight = shareFlight;
	}

	public String getCabin() {
		return cabin;
	}

	public void setCabin(String cabin) {
		this.cabin = cabin;
	}

	public String getDepDate() {
		return depDate;
	}

	public void setDepDate(String depDate) {
		this.depDate = depDate;
	}
	
	public String getDepTime() {
		return depTime;
	}

	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}

	public String getDesTime() {
		return desTime;
	}

	public void setDesTime(String desTime) {
		this.desTime = desTime;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public BigDecimal getTicketPrice() {
		return ticketPrice;
	}

	public void setTicketPrice(BigDecimal ticketPrice) {
		this.ticketPrice = ticketPrice;
	}
	
	public BigDecimal getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}

	public Integer getRemainSite() {
		return remainSite;
	}

	public void setRemainSite(Integer remainSite) {
		this.remainSite = remainSite;
	}

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
}