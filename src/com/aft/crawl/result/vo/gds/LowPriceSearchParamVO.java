package com.aft.crawl.result.vo.gds;
/**
 * Sabre 低价航班查询 查询参数VO
 * @author 
 *
 */
public class LowPriceSearchParamVO {
	private String sessionID;//日志使用
	/**
	 * 出发地三字码,非空
	 */
	protected String depCode;
	/**
	 * 目的地三字码,非空
	 */
	protected String desCode;
	/**
	 * 出发日期 yyyy-MM-dd,非空
	 */
	protected String depDate;
	/**
	 * 返程日期 yyyy-MM-dd
	 */
	protected String returnDate;
	
	/***行程类型****/
	protected String tripType;
	
	public enum DataTripType {
		OneWay("1"),
		ReturnTrip("2"),
		MultiTrip("3");
		
		public String value;
		DataTripType(String value) {
			this.value = value;
		}
	}
	
	protected String gdsSource;
	
	/***travelport 的价格集返回数******/
	protected Integer maxSolutions; 
	/**
	 * 航司二字码，多个之间用,隔开
	 */
	protected String airlines;
	/**
	 * 旅客类型
	 */
	protected String passengerType;
	/**
	 * 旅客数量
	 */
	protected String passengerQuantity;
	/**
	 * 成人人数
	 */
	protected Integer adtCount;
	/**
	 * 儿童人数
	 */
	protected Integer chdCount;
	/**
	 * 婴儿人数
	 */
	protected Integer infCount;
//=========================================	
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
	public String getReturnDate() {
		return returnDate;
	}
	public void setReturnDate(String returnDate) {
		this.returnDate = returnDate;
	}
	public String getAirlines() {
		return airlines;
	}
	public void setAirlines(String airlines) {
		this.airlines = airlines;
	}
	public String getPassengerType() {
		return passengerType;
	}
	public void setPassengerType(String passengerType) {
		this.passengerType = passengerType;
	}
	public String getPassengerQuantity() {
		return passengerQuantity;
	}
	public void setPassengerQuantity(String passengerQuantity) {
		this.passengerQuantity = passengerQuantity;
	}
	public Integer getAdtCount() {
		return adtCount;
	}
	public void setAdtCount(Integer adtCount) {
		this.adtCount = adtCount;
	}
	public Integer getChdCount() {
		return chdCount;
	}
	public void setChdCount(Integer chdCount) {
		this.chdCount = chdCount;
	}
	public Integer getInfCount() {
		return infCount;
	}
	public void setInfCount(Integer infCount) {
		this.infCount = infCount;
	}
	public String getTripType() {
		return tripType;
	}
	public void setTripType(String tripType) {
		this.tripType = tripType;
	}
	public Integer getMaxSolutions() {
		return maxSolutions;
	}
	public void setMaxSolutions(Integer maxSolutions) {
		this.maxSolutions = maxSolutions;
	}
	public String getGdsSource() {
		return gdsSource;
	}
	public void setGdsSource(String gdsSource) {
		this.gdsSource = gdsSource;
	}
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

}

