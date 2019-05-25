package com.aft.crawl.result.vo.inter;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlResultInterTrip implements Serializable {

	private static final long serialVersionUID = 4153877449985615904L;
	
	/** 航空公司二字码 trip 第一个航司 */
	private String airlineCode;
	
	/** 航班号 trip 航班号 / 拼起来 */
	private String fltNr;
	
	/** 出发Cd */
	private String depCode;
	
	/** 到达Cd */
	private String desCode;
	
	/** 舱位 */
	private String cabin;
	
	/** 出发日期(yyyy-MM-dd) */
	private String depDate;

	/** 到达日期(yyyy-MM-dd) */
	private String desDate;

	/** 出发时间(HH:mm) */
	private String depTime;

	/** 到达时间(HH:mm) */
	private String desTime;
	
	/** 票面价 */
	private BigDecimal ticketPrice = new BigDecimal(0);
	
	/** 税费 */
	private BigDecimal taxFee = new BigDecimal(0);
	
	/** 剩余座位数 */
	private int remainSite=0;
	
	/** 航段 1开始 */
	private int tripNo;
	
	/** 1去程2回城 */
	private int way;
	
	/**目前东航需要的类型*/
	private String type;
	
	/**是否共享*/
	private String shareFlight;
	
	public CrawlResultInterTrip(String airlineCode,String fltNo,String depCode,String desCode,String depDate,String cabin,int remainSite,int tripNo,int way) {
		this.airlineCode = airlineCode;
		this.fltNr = fltNo;
		this.depCode = depCode;
		this.desCode = desCode;
		this.depDate = depDate;
		this.tripNo = tripNo;
		this.way = way;
		this.cabin = cabin;
		this.remainSite = remainSite;
	}

	public CrawlResultInterTrip(String airlineCode,String fltNo,String depCode,String desCode,String depDate,String cabin,int tripNo,int way) {
		this.airlineCode = airlineCode;
		this.fltNr = fltNo;
		this.depCode = depCode;
		this.desCode = desCode;
		this.depDate = depDate;
		this.tripNo = tripNo;
		this.way = way;
		this.cabin = cabin;
	}

	public String getAirlineCode() {
		return airlineCode;
	}

	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}

	public String getFltNr() {
		return fltNr;
	}

	public void setFltNr(String fltNr) {
		this.fltNr = fltNr;
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

	public String getDesDate() {
		return desDate;
	}

	public void setDesDate(String desDate) {
		this.desDate = desDate;
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
	
	public int getRemainSite() {
		return remainSite;
	}

	public void setRemainSite(int remainSite) {
		this.remainSite = remainSite;
	}

	public int getTripNo() {
		return tripNo;
	}

	public void setTripNo(int tripNo) {
		this.tripNo = tripNo;
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

	public String getCabin() {
		return cabin;
	}

	public void setCabin(String cabin) {
		this.cabin = cabin;
	}

	public int getWay() {
		return way;
	}

	public void setWay(int way) {
		this.way = way;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public String getShareFlight() {
		return shareFlight;
	}
	public void setShareFlight(String shareFlight) {
		this.shareFlight = shareFlight;
	}
}