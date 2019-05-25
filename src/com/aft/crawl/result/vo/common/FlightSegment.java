package com.aft.crawl.result.vo.common;

/**
 * 航段信息
 * @author liangdongming
 *
 */
public class FlightSegment implements Cloneable{
	
	/** 航段 1开始 */
	private Integer tripNo;
	/**
	 * 航司
	 */
	private String airlineCode;
	/**
	 * 航班号
	 */
	private String flightNumber;
	/**
	 * 出发地
	 */
	private String depAirport;
	/**
	 * 出发航站楼
	 */
	private String departureTerminal;
	/**
	 * 出发时间
	 * 2017-04-01 07:40:00
	 */
	private String depTime;
	/**
	 * 目的地
	 */
	private String arrAirport;
	/**
	 * 到达航站楼
	 */
	private String arrivalTerminal;
	/**
	 * 到达时间
	 * 2017-04-01 07:40:00
	 */
	private String arrTime;
	/**
	 * 经停城市
	 */
	private String stopCities;
	/**
	 * 是否共享
	 */
	private String codeShare;
	/**
	 * 舱位
	 */
	private String cabin;
	/**
	 * 舱位数量
	 */
	private String cabinCount;
	/**
	 * 机型
	 */
	private String aircraftCode;
	
	public String getAirlineCode() {
		return airlineCode;
	}
	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}
	public String getFlightNumber() {
		return flightNumber;
	}
	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}
	public String getDepAirport() {
		return depAirport;
	}
	public void setDepAirport(String depAirport) {
		this.depAirport = depAirport;
	}
	public String getDepTime() {
		return depTime;
	}
	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}
	public String getArrAirport() {
		return arrAirport;
	}
	public void setArrAirport(String arrAirport) {
		this.arrAirport = arrAirport;
	}
	public String getArrTime() {
		return arrTime;
	}
	public void setArrTime(String arrTime) {
		this.arrTime = arrTime;
	}
	public String getStopCities() {
		return stopCities;
	}
	public void setStopCities(String stopCities) {
		this.stopCities = stopCities;
	}
	public String getCodeShare() {
		return codeShare;
	}
	public void setCodeShare(String codeShare) {
		this.codeShare = codeShare;
	}
	public String getCabin() {
		return cabin;
	}
	public void setCabin(String cabin) {
		this.cabin = cabin;
	}
	public String getCabinCount() {
		return cabinCount;
	}
	public void setCabinCount(String cabinCount) {
		this.cabinCount = cabinCount;
	}
	public String getAircraftCode() {
		return aircraftCode;
	}
	public void setAircraftCode(String aircraftCode) {
		this.aircraftCode = aircraftCode;
	}
	public Integer getTripNo() {
		return tripNo;
	}
	public void setTripNo(Integer tripNo) {
		this.tripNo = tripNo;
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
	
	public Object clone() {
		
        Object o = null;
        try {
             o = super.clone();
        } catch (CloneNotSupportedException e) {
             System.out.println(e.toString());
        }
        return o;
    }
	
}
