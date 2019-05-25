package com.aft.crawl.result.vo.lcc.vo.mt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCMtPostPrice {

	// 票面价
	@JsonProperty
	private String TicketFee="0";

	// 税
	@JsonProperty
	private String Tax = "0";

	// 价格类型（成人/儿童）
	@JsonProperty
	private String PasType="ADT";

	// 货币
	@JsonProperty
	private String Currency = "CNY";
		
	@JsonIgnore	
	public String getTicketFee() {
		return TicketFee;
	}
	@JsonIgnore
	public void setTicketFee(String ticketFee) {
		TicketFee = ticketFee;
	}
	@JsonIgnore
	public String getTax() {
		return Tax;
	}
	@JsonIgnore
	public void setTax(String tax) {
		Tax = tax;
	}
	@JsonIgnore
	public String getPasType() {
		return PasType;
	}
	@JsonIgnore
	public void setPasType(String pasType) {
		PasType = pasType;
	}
	@JsonIgnore
	public String getCurrency() {
		return Currency;
	}
	@JsonIgnore
	public void setCurrency(String currency) {
		Currency = currency;
	}
}
