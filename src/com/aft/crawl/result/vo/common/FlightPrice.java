package com.aft.crawl.result.vo.common;
/**
 * 价格信息
 * @author liangdongming
 *
 */
public class FlightPrice {
	/**
	 * 乘客类型
	 * ADT 成人
	 * CHD 儿童
	 */
	private String passengerType;
	/**
	 * 票面价
	 */
	private String fare;
	/**
	 * 税费
	 */
	private String tax;
	/**
	 * 币种
	 */
	private String currency;
	/**
	 * 当地票面价
	 */
	private String equivFare;
	/**
	 * 当地税费
	 */
	private String equivTax;
	/**
	 * 当地币种
	 */
	private String equivCurrency;
	public String getPassengerType() {
		return passengerType;
	}
	public void setPassengerType(String passengerType) {
		this.passengerType = passengerType;
	}
	public String getFare() {
		return fare;
	}
	public void setFare(String fare) {
		this.fare = fare;
	}
	public String getTax() {
		return tax;
	}
	public void setTax(String tax) {
		this.tax = tax;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getEquivFare() {
		return equivFare;
	}
	public void setEquivFare(String equivFare) {
		this.equivFare = equivFare;
	}
	public String getEquivTax() {
		return equivTax;
	}
	public void setEquivTax(String equivTax) {
		this.equivTax = equivTax;
	}
	public String getEquivCurrency() {
		return equivCurrency;
	}
	public void setEquivCurrency(String equivCurrency) {
		this.equivCurrency = equivCurrency;
	}
	
}
