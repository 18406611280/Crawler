package com.aft.crawl.result.vo.inter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlInterFlightTrip implements Serializable {

	private static final long serialVersionUID = 4153877449985615904L;
	
	/** 航空公司二字码 trip 第一个航司 */
	private String airlineCode;
	
	/** 航班号 trip 航班号 / 拼起来 */
	private String fltNr;
	
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
	
	/** 航段 1开始 */
	private int tripNo;
	
	private List<CrawlInterFlightSegment> flightSegments = new ArrayList<CrawlInterFlightSegment>();
	
	public CrawlInterFlightTrip(String airlineCode, int remainSite, int tripNo, List<CrawlInterFlightSegment> flightSegments) {
		this.airlineCode = airlineCode;
		this.remainSite = remainSite;
		this.tripNo = tripNo;
		this.flightSegments = flightSegments;
		if(null != flightSegments && !flightSegments.isEmpty()) {
			Collections.sort(this.flightSegments, new Comparator<CrawlInterFlightSegment>() {
				@Override
				public int compare(CrawlInterFlightSegment o1, CrawlInterFlightSegment o2) {
					return o1.getSegmentNo() - o2.getSegmentNo();
				}
			});
			this.fltNr = "";
			for(int i=0,size=flightSegments.size(); i<size; i++) {
				CrawlInterFlightSegment fs = flightSegments.get(i);
				this.fltNr += fs.getFltNr() + "/";
				if(0 == i) {
					this.depCode = fs.getDepCode();
					this.depDate = fs.getDepDate();
					this.depTime = fs.getDepTime();
				}
				if(i == size - 1) {
					this.desCode = fs.getDesCode();
					this.desDate = fs.getDesDate();
					this.desTime = fs.getDesTime();
				}
			}
			this.fltNr = this.fltNr.substring(0, this.fltNr.length() - 1);
		}
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

	public List<CrawlInterFlightSegment> getFlightSegments() {
		return flightSegments;
	}

	public void setFlightSegments(List<CrawlInterFlightSegment> flightSegments) {
		this.flightSegments = flightSegments;
	}
}