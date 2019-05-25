package com.aft.crawl.result.vo.gds;
/**
 * 运价信息VO
 * @author 
 *
 */
public class AirItineraryPricingInfoVO {
	/**
	 * 基础运价
	 */
	protected String baseFare;
	/**
	 * 基础运价货币
	 */
	protected String baseFareCC;
	/**
	 * 销售运价
	 */
	protected String equivFare;
	/**
	 * 销售运价货币
	 */
	protected String equivFareCC;
	/**
	 * 税费总额
	 */
	protected String totalTax;
	/**
	 * 税费货币
	 */
	protected String totalTaxCC;
	/**
	 * 总价
	 */
	protected String TotalFare;
	/**
	 * 总价货币
	 */
	protected String TotalFareCC;
	/**
	 * 旅客类型
	 */
	protected String passengerType;
	/**
	 * 销售航司二字码
	 */
	protected String airlineCode;
//===============================
	public String getBaseFare() {
		return baseFare;
	}
	public void setBaseFare(String baseFare) {
		this.baseFare = baseFare;
	}
	public String getBaseFareCC() {
		return baseFareCC;
	}
	public void setBaseFareCC(String baseFareCC) {
		this.baseFareCC = baseFareCC;
	}
	public String getEquivFare() {
		return equivFare;
	}
	public void setEquivFare(String equivFare) {
		this.equivFare = equivFare;
	}
	public String getEquivFareCC() {
		return equivFareCC;
	}
	public void setEquivFareCC(String equivFareCC) {
		this.equivFareCC = equivFareCC;
	}
	public String getTotalTax() {
		return totalTax;
	}
	public void setTotalTax(String totalTax) {
		this.totalTax = totalTax;
	}
	public String getTotalTaxCC() {
		return totalTaxCC;
	}
	public void setTotalTaxCC(String totalTaxCC) {
		this.totalTaxCC = totalTaxCC;
	}
	public String getTotalFare() {
		return TotalFare;
	}
	public void setTotalFare(String totalFare) {
		TotalFare = totalFare;
	}
	public String getTotalFareCC() {
		return TotalFareCC;
	}
	public void setTotalFareCC(String totalFareCC) {
		TotalFareCC = totalFareCC;
	}
	public String getPassengerType() {
		return passengerType;
	}
	public void setPassengerType(String passengerType) {
		this.passengerType = passengerType;
	}
	public String getAirlineCode() {
		return airlineCode;
	}
	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}
	
}
