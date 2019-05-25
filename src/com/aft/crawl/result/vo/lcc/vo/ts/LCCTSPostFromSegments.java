package com.aft.crawl.result.vo.lcc.vo.ts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCTSPostFromSegments {
	/**
	 * 航司
	 */
	@JsonProperty
	private String carrier;
	/**
	 * 航班号
	 */
	@JsonProperty
	private String flightNumber;
	/**
	 * 出发地
	 */
	@JsonProperty
	private String depAirport;
	/**
	 * 出发时间
	 * 2017-04-01 07:40:00
	 */
	@JsonProperty
	private String depTime;
	/**
	 * 目的地
	 */
	@JsonProperty
	private String arrAirport;
	/**
	 * 到达时间
	 * 2017-04-01 07:40:00
	 */
	@JsonProperty
	private String arrTime;
	/**
	 * 经停城市
	 */
	@JsonProperty
	private String stopCities;
	/**
	 * 是否共享
	 */
	@JsonProperty
	private String codeShare;
	/**
	 * 共享航班号，没有为空
	 */
	@JsonProperty
	private String codeShareFltNum;
	/**
	 * 舱位
	 */
	@JsonProperty
	private String cabin;
	/**
	 * 舱位数量
	 */
	@JsonProperty
	private String cabinCount;
	/**
	 * 机型
	 */
	@JsonProperty
	private String aircraftCode;
	
	/**
	 * 仓位类型
	 */
	@JsonProperty
	private String cabinName;

	
	@JsonIgnore
	public String getCarrier() {
		return carrier;
	}@JsonIgnore
	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}
	@JsonIgnore
	public String getFlightNumber() {
		return flightNumber;
	}
	@JsonIgnore
	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}
	@JsonIgnore
	public String getDepAirport() {
		return depAirport;
	}
	@JsonIgnore
	public void setDepAirport(String depAirport) {
		this.depAirport = depAirport;
	}
	@JsonIgnore
	public String getDepTime() {
		return depTime;
	}
	@JsonIgnore
	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}
	@JsonIgnore
	public String getArrAirport() {
		return arrAirport;
	}
	@JsonIgnore
	public void setArrAirport(String arrAirport) {
		this.arrAirport = arrAirport;
	}
	@JsonIgnore
	public String getArrTime() {
		return arrTime;
	}
	@JsonIgnore
	public void setArrTime(String arrTime) {
		this.arrTime = arrTime;
	}
	@JsonIgnore
	public String getStopCities() {
		return stopCities;
	}
	@JsonIgnore
	public void setStopCities(String stopCities) {
		this.stopCities = stopCities;
	}
	@JsonIgnore
	public String getCodeShare() {
		return codeShare;
	}
	@JsonIgnore
	public void setCodeShare(String codeShare) {
		this.codeShare = codeShare;
	}
	@JsonIgnore
	public String getCabin() {
		return cabin;
	}
	@JsonIgnore
	public void setCabin(String cabin) {
		this.cabin = cabin;
	}
	@JsonIgnore
	public String getCabinCount() {
		return cabinCount;
	}
	@JsonIgnore
	public void setCabinCount(String cabinCount) {
		this.cabinCount = cabinCount;
	}
	@JsonIgnore
	public String getAircraftCode() {
		return aircraftCode;
	}
	@JsonIgnore
	public void setAircraftCode(String aircraftCode) {
		this.aircraftCode = aircraftCode;
	}
	@JsonIgnore
	public String getCabinName() {
		return cabinName;
	}
	@JsonIgnore
	public void setCabinName(String cabinName) {
		this.cabinName = cabinName;
	}
	public String getCodeShareFltNum() {
		return codeShareFltNum;
	}
	public void setCodeShareFltNum(String codeShareFltNum) {
		this.codeShareFltNum = codeShareFltNum;
	}
}
