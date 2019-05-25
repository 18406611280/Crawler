package com.aft.crawl.result.vo.b2c;

import java.math.BigDecimal;
import java.util.Date;

import com.aft.crawl.SwitchTypeEnum;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.utils.date.MyDateFormatUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlResultB2CRt extends CrawlResultBase {

	private static final long serialVersionUID = -1846079317347500869L;

	/** 航司二字码 */
	private String airlineCode;

	/** 出发城市Cd */
	private String depCode;

	/** 到达城市Cd */
	private String desCode;

	/** 航班号 */
	private String fltNo;

	/** 舱位 */
	private String cabin;

	/** 出发日期(yyyy-MM-dd) */
	private String depDate;

	/** 出发时间 */
	private String depTime;

	/** 到达时间 */
	private String desTime;

	/** 开始日期(yyyy-MM-dd) */
	private String startDate;

	/** 结束日期(yyyy-MM-dd) */
	private String endDate;

	/** 票面价 */
	private BigDecimal ticketPrice;
	
	/** 币种 */
	private String currency;

	/** 剩余座位数 */
	private Integer remainSite;

	/** 返程航班号 */
	private String backFltNo;

	/** 返程舱位 */
	private String backCabin;

	/** 返程出发日期(yyyy-MM-dd) */
	private String backDate;

	/** 返程出发时间 */
	private String backDepTime;

	/** 返程到达时间 */
	private String backDesTime;

	/** 返程开始日期(yyyy-MM-dd) */
	private String backStartDate;

	/** 返程结束日期(yyyy-MM-dd) */
	private String backEndDate;

	/** 剩余座位数 */
	private Integer backRemainSite;
	
	/** 返程价格 */
	private BigDecimal backTicketPrice;
	
	private String shareFlight;
	
	private String backShareFlight;
	
	private String type;
	
	private String backType;
	
	
	/** 任务id */
	private Integer jobId;

	public CrawlResultB2CRt(JobDetail jobDetail, String depCode, String desCode, String depDate) {
		this(jobDetail, depCode, desCode, depDate, null);
	}
	
	public CrawlResultB2CRt(JobDetail jobDetail, String depCode, String desCode, String depDate, String backDate) {
		super(jobDetail.getPageType(), jobDetail.getCrawlMark());
		this.depCode = depCode;
		this.desCode = desCode;
		this.depDate = depDate;
		this.backDate = backDate;
		this.jobId = jobDetail.getTimerJob().getJobId();
		
		this.startDate = depDate;
		this.endDate = depDate;
		this.backStartDate = backDate;
		this.backEndDate = backDate;
	}
	
	@Override
	public String toSaveFileStr() {
		SwitchTypeEnum switchTypeEnum = SwitchTypeEnum.getSwitchTypeEnum(this.getPageType());
		return switchTypeEnum.name()
					+ "," + this.airlineCode
					+ "," + this.depCode
					+ "," + this.desCode
					+ "," + this.depDate
					+ "," + this.fltNo
					+ "," + this.cabin
					+ "," + this.ticketPrice
					+ "," + this.remainSite
					+ "," + this.type
					+ "," + this.backDate
					+ "," + this.backFltNo
					+ "," + this.backCabin
					+ "," + this.backTicketPrice
					+ "," + this.backRemainSite
					+ "," + this.backType
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

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public BigDecimal getTicketPrice() {
		return ticketPrice;
	}

	public void setTicketPrice(BigDecimal ticketPrice) {
		this.ticketPrice = ticketPrice;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getRemainSite() {
		return remainSite;
	}

	public void setRemainSite(Integer remainSite) {
		this.remainSite = remainSite;
	}

	public String getBackFltNo() {
		return backFltNo;
	}

	public void setBackFltNo(String backFltNo) {
		this.backFltNo = backFltNo;
	}

	public String getBackCabin() {
		return backCabin;
	}

	public void setBackCabin(String backCabin) {
		this.backCabin = backCabin;
	}

	public String getBackDate() {
		return backDate;
	}

	public void setBackDate(String backDate) {
		this.backDate = backDate;
	}

	public String getBackDepTime() {
		return backDepTime;
	}

	public void setBackDepTime(String backDepTime) {
		this.backDepTime = backDepTime;
	}

	public String getBackDesTime() {
		return backDesTime;
	}

	public void setBackDesTime(String backDesTime) {
		this.backDesTime = backDesTime;
	}

	public String getBackStartDate() {
		return backStartDate;
	}

	public void setBackStartDate(String backStartDate) {
		this.backStartDate = backStartDate;
	}

	public String getBackEndDate() {
		return backEndDate;
	}

	public void setBackEndDate(String backEndDate) {
		this.backEndDate = backEndDate;
	}

	public Integer getBackRemainSite() {
		return backRemainSite;
	}

	public void setBackRemainSite(Integer backRemainSite) {
		this.backRemainSite = backRemainSite;
	}

	public BigDecimal getBackTicketPrice() {
		return backTicketPrice;
	}

	public void setBackTicketPrice(BigDecimal backTicketPrice) {
		this.backTicketPrice = backTicketPrice;
	}

	public String getShareFlight() {
		return shareFlight;
	}

	public void setShareFlight(String shareFlight) {
		this.shareFlight = shareFlight;
	}

	public String getBackShareFlight() {
		return backShareFlight;
	}

	public void setBackShareFlight(String backShareFlight) {
		this.backShareFlight = backShareFlight;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBackType() {
		return backType;
	}

	public void setBackType(String backType) {
		this.backType = backType;
	}

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}
}