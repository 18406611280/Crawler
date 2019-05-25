package com.aft.crawl.result.vo.inter;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlInterFlightSegment implements Serializable {

	private static final long serialVersionUID = 1404606294979766272L;

	/** 航空公司二字码 */
	private String airlineCode;
	
	/** 航班号 */
	private String fltNr;
	
	/** 舱位 */
	private String cabin;
	
	/** 出发Cd */
	private String depCode;
	
	/** 到达Cd */
	private String desCode;

	/** 出发日期(yyyy-MM-dd) */
	private String depDate;

	/** 到达日期(yyyy-MM-dd) */
	private String desDate;

	/** 出发时间(HH:mm) */
	private String depTime;

	/** 到达时间(HH:mm) */
	private String desTime;
	
	/** 剩余座位数 */
	private int remainSite;
	
	/** 航段 1 开始 */
	private int segmentNo;
	
	public CrawlInterFlightSegment(String airlineCode, String fltNr,
			String cabin, String depCode, String desCode, int remainSite, int segmentNo) {
		this.airlineCode = airlineCode;
		this.fltNr = fltNr;
		this.cabin = cabin;
		this.depCode = depCode;
		this.desCode = desCode;
		this.remainSite = remainSite;
		this.segmentNo = segmentNo;
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

	public String getCabin() {
		return cabin;
	}

	public void setCabin(String cabin) {
		this.cabin = cabin;
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

	public int getSegmentNo() {
		return segmentNo;
	}

	public void setSegmentNo(int segmentNo) {
		this.segmentNo = segmentNo;
	}
}