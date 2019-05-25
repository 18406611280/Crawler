package com.aft.crawl.result.vo.gds;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * 航程VO
 * @author 
 *
 */
public class OriginDestinationOptionVO {
	//航程序号：往返程分别为1/2，单程则为1
	protected String rph;
	@JsonProperty(value = "flightSegment")
	protected List<FlightSegmentVO> flightSegment;
	/**
	 * 航段数
	 */
	protected Integer flightSegmentCount;
	/**
	 * 目的地三字码
	 */
	protected String destinationLocationCode;
	/**
	 * 出发地三字码
	 */
	protected String OriginLocationCode;
	/**
	 * 到达时间
	 */
	protected String arrivalDateTime;
	/**
	 * 出发时间
	 */
	protected String departureDateTime;
	
	public String getRph() {
		return rph;
	}
	public void setRph(String rph) {
		this.rph = rph;
	}
	public List<FlightSegmentVO> getFlightSegment() {
		return flightSegment;
	}
	public void setFlightSegment(List<FlightSegmentVO> flightSegment) {
		this.flightSegment = flightSegment;
	}
	public String getDestinationLocationCode() {
		return destinationLocationCode;
	}
	public void setDestinationLocationCode(String destinationLocationCode) {
		this.destinationLocationCode = destinationLocationCode;
	}
	public String getOriginLocationCode() {
		return OriginLocationCode;
	}
	public void setOriginLocationCode(String originLocationCode) {
		OriginLocationCode = originLocationCode;
	}
	public String getArrivalDateTime() {
		return arrivalDateTime;
	}
	public void setArrivalDateTime(String arrivalDateTime) {
		this.arrivalDateTime = arrivalDateTime;
	}
	public String getDepartureDateTime() {
		return departureDateTime;
	}
	public void setDepartureDateTime(String departureDateTime) {
		this.departureDateTime = departureDateTime;
	}
	public Integer getFlightSegmentCount() {
		return flightSegmentCount;
	}
	public void setFlightSegmentCount(Integer flightSegmentCount) {
		this.flightSegmentCount = flightSegmentCount;
	}
	
}
