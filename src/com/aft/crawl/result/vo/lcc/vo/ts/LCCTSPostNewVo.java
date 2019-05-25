package com.aft.crawl.result.vo.lcc.vo.ts;

import java.util.List;

import com.aft.crawl.result.vo.lcc.LCCPostVoBase;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCTSPostNewVo {
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
	 * 是否国内
	 */
	@JsonProperty
	private String isGn;
	/**
	 * MUGN
	 */
	@JsonProperty
	private String priceFrom;
	
	/**
	 * 所有仓位数据
	 */
	@JsonProperty
	private List<LCCPostVoBase> fltList ;

	public String getDepAirport() {
		return depAirport;
	}

	public void setDepAirport(String depAirport) {
		this.depAirport = depAirport;
	}

	public String getArrAirport() {
		return arrAirport;
	}

	public void setArrAirport(String arrAirport) {
		this.arrAirport = arrAirport;
	}

	public String getGoDate() {
		return goDate;
	}

	public void setGoDate(String goDate) {
		this.goDate = goDate;
	}

	public String getIsGn() {
		return isGn;
	}

	public void setIsGn(String isGn) {
		this.isGn = isGn;
	}

	public String getPriceFrom() {
		return priceFrom;
	}

	public void setPriceFrom(String priceFrom) {
		this.priceFrom = priceFrom;
	}

	public List<LCCPostVoBase> getFltList() {
		return fltList;
	}

	public void setFltList(List<LCCPostVoBase> fltList) {
		this.fltList = fltList;
	}
}
