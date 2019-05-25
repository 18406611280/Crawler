package com.aft.crawl.result.vo.lcc.vo.ts;

import java.util.List;

import com.aft.crawl.result.vo.lcc.LCCPostVoBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCTSPostVo extends LCCPostVoBase{
	/**
	 * 更新时间
	 * 2017-03-30 23:51:27
	 */
	@JsonProperty
	private String updateTime;
	/**
	 * 起飞地
	 */
	@JsonProperty
	private String depAirport;
	/**
	 * 目的地
	 */
	@JsonProperty
	private String arrAirport;
	/**
	 * 出发日期
	 * 2017-04-01
	 */
	@JsonProperty
	private String goDate;
	/**
	 * 返程日期
	 * 2017-04-04
	 */
	@JsonProperty
	private String backDate;
	/**
	 * TS 标识
	 * JQ.....
	 */
	@JsonProperty
	private String priceFrom;
	/**
	 * 成人票价
	 */
	@JsonProperty
	private String adultPrice;
	/**
	 * 成人税费
	 */
	@JsonProperty
	private String adultTax;
	/**
	 * 儿童票价
	 */
	@JsonProperty
	private String childPrice="0";
	/**
	 * 儿童税费
	 */
	@JsonProperty
	private String childTax="0";
	/**
	 * 规则
	 */
	@JsonProperty
	private LCCTSPostRule rule;
	/**
	 * 是否国内
	 */
	@JsonProperty
	private String isGn;
	/**
	 * 币种类型
	 */
	@JsonProperty
	private String moneyType;
	/**
	 * 去程航班信息
	 */
	@JsonProperty
	private List<LCCTSPostFromSegments> fromSegments;
	/**
	 * 返程航班信息
	 */
	@JsonProperty
	private List<LCCTSPostRetSegments> retSegments;
	@JsonIgnore
	public String getUpdateTime() {
		return updateTime;
	}
	@JsonIgnore
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	@JsonIgnore
	public String getDepAirport() {
		return depAirport;
	}
	@JsonIgnore
	public void setDepAirport(String depAirport) {
		this.depAirport = depAirport;
	}
	@JsonIgnore
	public String getArrAirport() {
		return arrAirport;
	}
	@JsonIgnore
	public void setArrAirport(String arrAirport) {
		this.arrAirport = arrAirport;
	}
	@JsonIgnore
	public String getGoDate() {
		return goDate;
	}
	@JsonIgnore
	public void setGoDate(String goDate) {
		this.goDate = goDate;
	}
	@JsonIgnore
	public String getBackDate() {
		return backDate;
	}
	@JsonIgnore
	public void setBackDate(String backDate) {
		this.backDate = backDate;
	}
	@JsonIgnore
	public String getPriceFrom() {
		return priceFrom;
	}
	@JsonIgnore
	public void setPriceFrom(String priceFrom) {
		this.priceFrom = priceFrom;
	}
	@JsonIgnore
	public String getAdultPrice() {
		return adultPrice;
	}
	@JsonIgnore
	public void setAdultPrice(String adultPrice) {
		this.adultPrice = adultPrice;
	}
	@JsonIgnore
	public String getAdultTax() {
		return adultTax;
	}
	@JsonIgnore
	public void setAdultTax(String adultTax) {
		this.adultTax = adultTax;
	}
	@JsonIgnore
	public String getChildPrice() {
		return childPrice;
	}
	@JsonIgnore
	public void setChildPrice(String childPrice) {
		this.childPrice = childPrice;
	}
	@JsonIgnore
	public String getChildTax() {
		return childTax;
	}
	@JsonIgnore
	public void setChildTax(String childTax) {
		this.childTax = childTax;
	}
	@JsonIgnore
	public LCCTSPostRule getRule() {
		return rule;
	}
	@JsonIgnore
	public void setRule(LCCTSPostRule rule) {
		this.rule = rule;
	}
	@JsonIgnore
	public String getIsGn() {
		return isGn;
	}
	@JsonIgnore
	public void setIsGn(String isGn) {
		this.isGn = isGn;
	}
	@JsonIgnore
	public List<LCCTSPostFromSegments> getFromSegments() {
		return fromSegments;
	}
	@JsonIgnore
	public void setFromSegments(List<LCCTSPostFromSegments> fromSegments) {
		this.fromSegments = fromSegments;
	}
	@JsonIgnore
	public List<LCCTSPostRetSegments> getRetSegments() {
		return retSegments;
	}
	@JsonIgnore
	public void setRetSegments(List<LCCTSPostRetSegments> retSegments) {
		this.retSegments = retSegments;
	}
	@JsonIgnore
	public String getMoneyType() {
		return moneyType;
	}
	@JsonIgnore
	public void setMoneyType(String moneyType) {
		this.moneyType = moneyType;
	}
	
}
