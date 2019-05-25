package com.aft.crawl.crawler.impl.app.ca;

import java.util.UUID;

/**
 * 国航需要用到的字段
 * 
 */
public class CaInterfaceEntity {

	/** 设备id */
	private String deviceId = UUID.randomUUID().toString().toUpperCase();
	
	/** 航班查询的id */
	private String flightID;
	
	/** 出发机场航站楼 */
	private String departureTerminal;
	
	/** 到达机场航站楼 */
	private String arrivalTerminal;
	
	/** 航班搜索searchId */
	private String searchId;
	
	/** 是否共享 */
	private String shareFlag;
	
	/** 航司 */
	private String airline;
	
	/** 出发时间 格式 hh:mm */
	private String depTime;
	
	/** 到达时间 格式 hh:mm */
	private String arrTime;
	
	/** 到达日期 */
	private String arrDate;
	
	/** 航班号 */
	private String flightNo;

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getFlightID() {
		return flightID;
	}

	public void setFlightID(String flightID) {
		this.flightID = flightID;
	}

	public String getDepartureTerminal() {
		return departureTerminal;
	}

	public void setDepartureTerminal(String departureTerminal) {
		this.departureTerminal = departureTerminal;
	}

	public String getArrivalTerminal() {
		return arrivalTerminal;
	}

	public void setArrivalTerminal(String arrivalTerminal) {
		this.arrivalTerminal = arrivalTerminal;
	}

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public String getShareFlag() {
		return shareFlag;
	}

	public void setShareFlag(String shareFlag) {
		this.shareFlag = shareFlag;
	}

	public String getAirline() {
		return airline;
	}

	public void setAirline(String airline) {
		this.airline = airline;
	}

	public String getDepTime() {
		return depTime;
	}

	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}

	public String getArrTime() {
		return arrTime;
	}

	public void setArrTime(String arrTime) {
		this.arrTime = arrTime;
	}

	public String getArrDate() {
		return arrDate;
	}

	public void setArrDate(String arrDate) {
		this.arrDate = arrDate;
	}

	public String getFlightNo() {
		return flightNo;
	}

	public void setFlightNo(String flightNo) {
		this.flightNo = flightNo;
	}
	
}