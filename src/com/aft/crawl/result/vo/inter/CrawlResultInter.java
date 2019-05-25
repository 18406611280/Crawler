package com.aft.crawl.result.vo.inter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.fasterxml.jackson.annotation.JsonInclude;
/**
 * 多航段
 * @author thinkpad
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlResultInter extends CrawlResultBase implements Serializable {

	private static final long serialVersionUID = 2246709810740764603L;
	
	/** 航程类型 OW:单程, RT:往返 */
	private String routeType;
	
	/** 出发Cd */
	private String depCode;
	
	/** 到达Cd */
	private String desCode;
	
	/** 出发日期(yyyy-MM-dd) */
	private String depDate;

	/** 返航日期(yyyy-MM-dd) */
	private String backDate;
	
	/** 币种 */
	private String currency="CNY";
	
	/** 票面价 */
	private BigDecimal ticketPrice=new BigDecimal(0);
	
	/** 税费 */
	private BigDecimal taxFee = new BigDecimal(0);
	
	/** 退票信息 */
	private String refundInfo;

	/** 改期信息 */
	private String changeInfo;

	/** 签转信息 */
	private String qianZhuanInfo;

	/** 误机信息 */
	private String wuJiInfo;
	
	/** 航段集合 */
	private List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
	
	/** 任务id */
	private Integer jobId;
	
	private String type;
	
	public CrawlResultInter(JobDetail jobDetail, String depCode, String desCode, String depDate) {
		this(jobDetail, depCode, desCode, depDate, null);
	}
	
	public CrawlResultInter(JobDetail jobDetail, String depCode, String desCode, String depDate, String backDate) {
		super(jobDetail.getPageType(), jobDetail.getCrawlMark());
		this.depCode = depCode;
		this.desCode = desCode;
		this.depDate = depDate;
		this.backDate = backDate;
		this.routeType = "OW";
		if(StringUtils.isNotEmpty(backDate)) this.routeType = "RT";
		this.jobId = jobDetail.getTimerJob().getJobId();
	}

	public String getRouteType() {
		return routeType;
	}

	public void setRouteType(String routeType) {
		this.routeType = routeType;
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

	public String getDepDate() {
		return depDate;
	}

	public void setDepDate(String depDate) {
		this.depDate = depDate;
	}

	public String getBackDate() {
		return backDate;
	}

	public void setBackDate(String backDate) {
		this.backDate = backDate;
	}

	public BigDecimal getTicketPrice() {
		return ticketPrice;
	}

	public void setTicketPrice(BigDecimal ticketPrice) {
		this.ticketPrice = ticketPrice;
	}

	public BigDecimal getTaxFee() {
		return taxFee;
	}

	public void setTaxFee(BigDecimal taxFee) {
		this.taxFee = taxFee;
	}
	
	public String getRefundInfo() {
		return refundInfo;
	}

	public void setRefundInfo(String refundInfo) {
		this.refundInfo = refundInfo;
	}

	public String getChangeInfo() {
		return changeInfo;
	}

	public void setChangeInfo(String changeInfo) {
		this.changeInfo = changeInfo;
	}

	public String getQianZhuanInfo() {
		return qianZhuanInfo;
	}

	public void setQianZhuanInfo(String qianZhuanInfo) {
		this.qianZhuanInfo = qianZhuanInfo;
	}

	public String getWuJiInfo() {
		return wuJiInfo;
	}

	public void setWuJiInfo(String wuJiInfo) {
		this.wuJiInfo = wuJiInfo;
	}

	public List<CrawlResultInterTrip> getFlightTrips() {
		return flightTrips;
	}

	public void setFlightTrips(List<CrawlResultInterTrip> flightTrips) {
		this.flightTrips = flightTrips;
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	
}