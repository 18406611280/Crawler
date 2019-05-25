package com.aft.crawl.result.vo.lcc.vo.ts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCTSPostRule {
	/***/
	@JsonProperty
	private String refund;
	/***/
	@JsonProperty
	private String endorse;
	/***/
	@JsonProperty
	private String baggage;
	/***/
	@JsonProperty
	private String other;
	
	@JsonIgnore
	public String getRefund() {
		return refund;
	}
	@JsonIgnore
	public void setRefund(String refund) {
		this.refund = refund;
	}
	@JsonIgnore
	public String getEndorse() {
		return endorse;
	}
	@JsonIgnore
	public void setEndorse(String endorse) {
		this.endorse = endorse;
	}
	@JsonIgnore
	public String getBaggage() {
		return baggage;
	}
	@JsonIgnore
	public void setBaggage(String baggage) {
		this.baggage = baggage;
	}
	@JsonIgnore
	public String getOther() {
		return other;
	}
	@JsonIgnore
	public void setOther(String other) {
		this.other = other;
	}
}
