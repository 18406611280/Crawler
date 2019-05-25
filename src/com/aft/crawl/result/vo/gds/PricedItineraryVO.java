package com.aft.crawl.result.vo.gds;

import java.util.List;

import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.gds.FareRules.Baggage;
/**
 * 运价与航程
 * @author 
 *
 */
public class PricedItineraryVO extends CrawlResultBase {
	
	private static final long serialVersionUID = -1087630456026401862L;
	
	public PricedItineraryVO() {
		super(null, null);
	}
	
	/** GDS来源枚举 */
	public enum DataSource {
		Sabre("1"),
		Amadues("2"),
		Travelport("3");
		
		public String value;
		DataSource(String value) {
			this.value = value;
		}
	}
	
	//GDS来源
	protected String gdsSource;
	//GDS账号
	protected String gdsAccount;
	//运价
	protected List<AirItineraryPricingInfoVO> airItineraryPricingInfoVO;
	//航程信息
	protected List<OriginDestinationOptionVO> originDestinationOptionVO;
	//规则
	protected List<FareRules> fareRulesVO;
	/***行李**/
	private List<Baggage> baggageList;
	//序号--不用
	protected String rph;
	//总金额--不用
	protected String totalAmount;
	//总金额币种
	protected String currencyCode;
	//
	protected String customizeRoutingOption;
//==================================
	public List<AirItineraryPricingInfoVO> getAirItineraryPricingInfoVO() {
		return airItineraryPricingInfoVO;
	}
	public void setAirItineraryPricingInfoVO(
			List<AirItineraryPricingInfoVO> airItineraryPricingInfoVO) {
		this.airItineraryPricingInfoVO = airItineraryPricingInfoVO;
	}
	public List<OriginDestinationOptionVO> getOriginDestinationOptionVO() {
		return originDestinationOptionVO;
	}
	public void setOriginDestinationOptionVO(
			List<OriginDestinationOptionVO> originDestinationOptionVO) {
		this.originDestinationOptionVO = originDestinationOptionVO;
	}
	public String getRph() {
		return rph;
	}
	public void setRph(String rph) {
		this.rph = rph;
	}
	public String getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getCustomizeRoutingOption() {
		return customizeRoutingOption;
	}
	public void setCustomizeRoutingOption(String customizeRoutingOption) {
		this.customizeRoutingOption = customizeRoutingOption;
	}
	public String getGdsSource() {
		return gdsSource;
	}
	public void setGdsSource(String gdsSource) {
		this.gdsSource = gdsSource;
	}
	public String getGdsAccount() {
		return gdsAccount;
	}
	public void setGdsAccount(String gdsAccount) {
		this.gdsAccount = gdsAccount;
	}
	public List<FareRules> getFareRulesVO() {
		return fareRulesVO;
	}
	public void setFareRulesVO(List<FareRules> fareRulesVO) {
		this.fareRulesVO = fareRulesVO;
	}
	public List<Baggage> getBaggageList() {
		return baggageList;
	}
	public void setBaggageList(List<Baggage> baggageList) {
		this.baggageList = baggageList;
	}
	
}
