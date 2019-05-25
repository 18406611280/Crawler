package com.aft.crawl.result.vo.gds;

import java.util.List;

/**
 * Sabre低价查询结果父类
 * @author 
 *
 */
public class LowPriceSearchRsVO {

	private String status;
	private String errorType;
	private String errorMsg;
	private List<PricedItineraryVO> pricedItinerary;

	/** 查询结果状态 */
	public enum StatusCodes {
		/** 成功 */
		SUCCESS("2"),
		/** 超时 */
		TIMEOUT("3"),
		/** 异常 */
		EXCEPTION("4"),
		/** 参数出错 */
		PARAM_ERROR("6");
		
		private final String value;
		
		StatusCodes(String value) {
			this.value = value;
		}
		
		public String value() {
	        return value;
	    }
	}
	
//======================================
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorType() {
		return errorType;
	}
	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public List<PricedItineraryVO> getPricedItinerary() {
		return pricedItinerary;
	}
	public void setPricedItinerary(List<PricedItineraryVO> pricedItinerary) {
		this.pricedItinerary = pricedItinerary;
	}
	
}
