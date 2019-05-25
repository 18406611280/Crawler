package com.aft.crawl.result.vo.gds;

/**
 * 航段信息VO
 * @author 
 *
 */
public class FlightSegmentVO {
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
	/**
	 * 中转指示
	 */
    protected String connectionInd;
    /**
     * 目的地时区
     */
    protected String destinationTimeZone;
    protected Boolean divideInd;
    protected String elapsedTime;
    /**
     * 
     */
    protected Boolean eTicket;
    /**
     * 航班号
     */
    protected String flightNumber;
    /**
     * 营销舱位
     */
    protected String marketingCabin;
    /**
     * 准点率
     */
    protected String onTimeRate;
    protected String onTimePercent;
    protected String originTimeZone;
    /**
     * 预订舱位类型代码
     */
    protected String resBookDesigCode;
    /**
     * 航段序号
     */
    protected String rph;
    protected Boolean smokingAllowed;
    /**
     * 航班计划停留次数
     */
    protected String stopQuantity;
    /**
     * 飞行器类型
     */
    protected String airEquipType;
    /**
     * 营销航司代码
     */
    protected String marketingAirlineCode;
    /**
     * 运营航司代码
     */
    protected String operatingAirlineCode;
    /**
     *运营航司航班号
     */
    protected String operatingflightNumber;
    /**
     * 餐食代码
     */
    protected String mealCode;
    /**
     * 经销商
     */
    protected String availabilitySource;
    /**
     * 经停地点
     */
    protected String stopLocationCode;
    /**舱位等级，经济舱，商务舱，头等舱***/
    protected String  cabinClass;
    /***出发的航站楼*******/
    protected String originTerminal;
    /***到达的航站楼*******/
    protected String destinationTerminal;
    /********************/
    /****座位数******/
    protected Integer seats;
//===============================================
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
	public String getConnectionInd() {
		return connectionInd;
	}
	public void setConnectionInd(String connectionInd) {
		this.connectionInd = connectionInd;
	}
	public String getDepartureDateTime() {
		return departureDateTime;
	}
	public void setDepartureDateTime(String departureDateTime) {
		this.departureDateTime = departureDateTime;
	}
	public String getDestinationTimeZone() {
		return destinationTimeZone;
	}
	public void setDestinationTimeZone(String destinationTimeZone) {
		this.destinationTimeZone = destinationTimeZone;
	}
	public Boolean getDivideInd() {
		return divideInd;
	}
	public void setDivideInd(Boolean divideInd) {
		this.divideInd = divideInd;
	}
	public String getElapsedTime() {
		return elapsedTime;
	}
	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	public Boolean geteTicket() {
		return eTicket;
	}
	public void seteTicket(Boolean eTicket) {
		this.eTicket = eTicket;
	}
	public String getFlightNumber() {
		return flightNumber;
	}
	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}
	public String getMarketingCabin() {
		return marketingCabin;
	}
	public void setMarketingCabin(String marketingCabin) {
		this.marketingCabin = marketingCabin;
	}
	public String getOnTimeRate() {
		return onTimeRate;
	}
	public void setOnTimeRate(String onTimeRate) {
		this.onTimeRate = onTimeRate;
	}
	public String getOnTimePercent() {
		return onTimePercent;
	}
	public void setOnTimePercent(String onTimePercent) {
		this.onTimePercent = onTimePercent;
	}
	public String getOriginTimeZone() {
		return originTimeZone;
	}
	public void setOriginTimeZone(String originTimeZone) {
		this.originTimeZone = originTimeZone;
	}
	public String getResBookDesigCode() {
		return resBookDesigCode;
	}
	public void setResBookDesigCode(String resBookDesigCode) {
		this.resBookDesigCode = resBookDesigCode;
	}
	public String getRph() {
		return rph;
	}
	public void setRph(String rph) {
		this.rph = rph;
	}
	public Boolean getSmokingAllowed() {
		return smokingAllowed;
	}
	public void setSmokingAllowed(Boolean smokingAllowed) {
		this.smokingAllowed = smokingAllowed;
	}
	public String getStopQuantity() {
		return stopQuantity;
	}
	public void setStopQuantity(String stopQuantity) {
		this.stopQuantity = stopQuantity;
	}
	public String getAirEquipType() {
		return airEquipType;
	}
	public void setAirEquipType(String airEquipType) {
		this.airEquipType = airEquipType;
	}
	public String getMarketingAirlineCode() {
		return marketingAirlineCode;
	}
	public void setMarketingAirlineCode(String marketingAirlineCode) {
		this.marketingAirlineCode = marketingAirlineCode;
	}
	public String getOperatingAirlineCode() {
		return operatingAirlineCode;
	}
	public void setOperatingAirlineCode(String operatingAirlineCode) {
		this.operatingAirlineCode = operatingAirlineCode;
	}
	public String getMealCode() {
		return mealCode;
	}
	public void setMealCode(String mealCode) {
		this.mealCode = mealCode;
	}
	
	public String getAvailabilitySource() {
		return availabilitySource;
	}
	public void setAvailabilitySource(String availabilitySource) {
		this.availabilitySource = availabilitySource;
	}
	public String getStopLocationCode() {
		return stopLocationCode;
	}
	public void setStopLocationCode(String stopLocationCode) {
		this.stopLocationCode = stopLocationCode;
	}
	public String getCabinClass() {
		return cabinClass;
	}
	public void setCabinClass(String cabinClass) {
		this.cabinClass = cabinClass;
	}
	public String getOperatingflightNumber() {
		return operatingflightNumber;
	}
	public void setOperatingflightNumber(String operatingflightNumber) {
		this.operatingflightNumber = operatingflightNumber;
	}
	public String getOriginTerminal() {
		return originTerminal;
	}
	public void setOriginTerminal(String originTerminal) {
		this.originTerminal = originTerminal;
	}
	public String getDestinationTerminal() {
		return destinationTerminal;
	}
	public void setDestinationTerminal(String destinationTerminal) {
		this.destinationTerminal = destinationTerminal;
	}
	public Integer getSeats() {
		return seats;
	}
	public void setSeats(Integer seats) {
		this.seats = seats;
	}
	
    
}
