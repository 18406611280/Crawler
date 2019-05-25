package com.aft.crawl.result.vo.common;

/**
 * 航班规则
 * @author liangdongming
 *
 */
public class FilghtRule {
	
	/** 1去程2回城 */
	private Integer way;
	/**退票规则*/
	private String refund;
	/**签转规则*/
	private String endorse;
	/**行李规则*/
	private String baggage;
	/**其他规则*/
	private String other;
	
	public String getRefund() {
		return refund;
	}
	public void setRefund(String refund) {
		this.refund = refund;
	}
	public String getEndorse() {
		return endorse;
	}
	public void setEndorse(String endorse) {
		this.endorse = endorse;
	}
	public String getBaggage() {
		return baggage;
	}
	public void setBaggage(String baggage) {
		this.baggage = baggage;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	public Integer getWay() {
		return way;
	}
	public void setWay(Integer way) {
		this.way = way;
	}
}
