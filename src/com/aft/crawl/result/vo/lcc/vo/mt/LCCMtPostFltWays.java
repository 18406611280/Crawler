package com.aft.crawl.result.vo.lcc.vo.mt;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCMtPostFltWays {

	// 出发
	@JsonProperty
	private String FromCode;
	// 到达
	@JsonProperty
	private String ToCode;

	// 1/2;去程/回程
	@JsonProperty
	private String Way;

	// 起飞时间  DD/MM/YYYY hh:mm:ss
	@JsonProperty
	private String FltTime;

	// 到达时间 DD/MM/YYYY hh:mm:ss,如果是单程，默认01/01/0001 00:00:00
	@JsonProperty
	private String ArrTime;
	
	@JsonProperty
	private List<LCCMtPostFlts> Flts;
	
	@JsonIgnore
	public String getFromCode() {
		return FromCode;
	}
	@JsonIgnore
	public void setFromCode(String fromCode) {
		FromCode = fromCode;
	}
	@JsonIgnore
	public String getToCode() {
		return ToCode;
	}
	@JsonIgnore
	public void setToCode(String toCode) {
		ToCode = toCode;
	}
	@JsonIgnore
	public String getWay() {
		return Way;
	}
	@JsonIgnore
	public void setWay(String way) {
		Way = way;
	}
	@JsonIgnore
	public String getFltTime() {
		return FltTime;
	}
	@JsonIgnore
	public void setFltTime(String fltTime) {
		FltTime = fltTime;
	}
	@JsonIgnore
	public String getArrTime() {
		return ArrTime;
	}
	@JsonIgnore
	public void setArrTime(String arrTime) {
		ArrTime = arrTime;
	}
	@JsonIgnore
	public List<LCCMtPostFlts> getFlts() {
		return Flts;
	}
	@JsonIgnore
	public void setFlts(List<LCCMtPostFlts> flts) {
		Flts = flts;
	}
}
