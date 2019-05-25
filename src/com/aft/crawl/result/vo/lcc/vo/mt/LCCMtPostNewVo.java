package com.aft.crawl.result.vo.lcc.vo.mt;

import java.util.List;

import com.aft.crawl.result.vo.lcc.LCCPostVoBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCMtPostNewVo extends LCCPostVoBase{

	// 出发
	@JsonProperty
	private String FromCode;

	// 到达
	@JsonProperty
	private String ToCode;

	// 出发日期  DD/MM/YYYY hh:mm:ss
	@JsonProperty
	private String GoDate;

	//返程日期 DD/MM/YYYY hh:mm:ss,如果是单程，默认01/01/0001 00:00:00
	@JsonProperty
	private String BackDate="01/01/0001 00:00:00";

	// 旅行类型  1 单程；2 往返（往返要填BackDate）
	@JsonProperty
	private String TripType;

	// 数据源
	@JsonProperty
	private String PriceFrom;

	@JsonProperty
	private List<LCCMtPostFltWays> FltWays;

	@JsonProperty
	private List<LCCMtPostBags> Bags;

	@JsonProperty
	private List<LCCMtPostPrice> Price;

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
	public String getGoDate() {
		return GoDate;
	}
	@JsonIgnore
	public void setGoDate(String goDate) {
		GoDate = goDate;
	}
	@JsonIgnore
	public String getBackDate() {
		return BackDate;
	}
	@JsonIgnore
	public void setBackDate(String backDate) {
		BackDate = backDate;
	}
	@JsonIgnore
	public String getTripType() {
		return TripType;
	}
	@JsonIgnore
	public void setTripType(String tripType) {
		TripType = tripType;
	}
	@JsonIgnore
	public String getPriceFrom() {
		return PriceFrom;
	}
	@JsonIgnore
	public void setPriceFrom(String priceFrom) {
		PriceFrom = priceFrom;
	}
	@JsonIgnore
	public List<LCCMtPostFltWays> getFltWays() {
		return FltWays;
	}
	@JsonIgnore
	public void setFltWays(List<LCCMtPostFltWays> fltWays) {
		FltWays = fltWays;
	}
	@JsonIgnore
	public List<LCCMtPostBags> getBags() {
		return Bags;
	}
	@JsonIgnore
	public void setBags(List<LCCMtPostBags> bags) {
		Bags = bags;
	}
	@JsonIgnore
	public List<LCCMtPostPrice> getPrice() {
		return Price;
	}
	@JsonIgnore
	public void setPrice(List<LCCMtPostPrice> price) {
		Price = price;
	}
}
