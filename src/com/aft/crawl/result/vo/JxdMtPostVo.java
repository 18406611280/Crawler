package com.aft.crawl.result.vo;

import com.aft.crawl.result.vo.lcc.LCCPostVoBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JxdMtPostVo  extends LCCPostVoBase{

	// 出发
	@JsonProperty
	private String FromCode;
	
	// 到达
	@JsonProperty
	private String ToCode;

	// 航班号
	@JsonProperty
	private String FltNo;

	// 仓位
	@JsonProperty
	private String Seat;

	// 仓位 数量
	@JsonProperty
	private String SeatCount;

	// 起飞日期
	@JsonProperty
	private String FltDate;

	// 数据源
	@JsonProperty
	private String PriceFrom;

	// 起飞时间 "0001-01-01 0:00:00",
	@JsonProperty
	private String FltTime;

	// 到达时间
	@JsonProperty
	private String ArrTime;

	// 税
	@JsonProperty
	private String Tax = "0";

	// 燃油
	@JsonProperty
	private String AddFee = "0";

	// 全价
	@JsonProperty
	private String YPrice = "0";

	// 货币
	@JsonProperty
	private String MoneyType = "CNY";
	
	// 票面价
	@JsonProperty
	private String AdtPrice = "3930";

	// 票面价
	@JsonProperty
	private String YuanShiPrice = "0";

	// 票面价
	@JsonProperty
	private String CNYPrice = "0";
	@JsonProperty
	private String PataPrice="0";
	@JsonProperty
	private String PataRate="0";
	@JsonProperty
	private String IsShare = "0";
	@JsonProperty
	private String Model = "";
	@JsonProperty
	private String PlaneSize = "";
	@JsonProperty
	private String MainFltNo = "";
	@JsonProperty
	private String AccuteRate = "0.0";
	@JsonProperty
	private String QneRank = "0";
	@JsonProperty
	private String IsTH = "0";
	@JsonProperty
	private String AdtAgentRate = "0";
	@JsonProperty
	private String CabinNote = "";
	@JsonProperty
	private String SignRule = "";
	@JsonProperty
	private String RefundRule = "";
	@JsonProperty
	private String EndorsRule = "";
	
//	public static List<JxdMtPostVo> toThis(List<CrawlResultBase> resultBases) {
//		List<JxdMtPostVo> vos = new ArrayList<JxdMtPostVo>();	
//		JxdMtPostVo vo = null;
//		for(CrawlResultBase resultBase : resultBases) {
//			if(resultBase instanceof CrawlResultB2C) {
//				CrawlResultB2C b2c = (CrawlResultB2C)resultBase;
//				//不是人民币的币种不上传到MT上
//				if(b2c.getType()!=null && !"".equals(b2c.getType()) && b2c.getType().contains("此价格的币种类型"))continue;
//				//有位置的位置数小于3个的不上传到MT
////				if("PUT955,PUT956,PUT957".contains(b2c.getPageType()) && b2c.getRemainSite().intValue()<3) continue;
//				vo = new JxdMtPostVo();
//				vo.setFromCode(b2c.getDepCode());
//				vo.setToCode(b2c.getDesCode());
//				vo.setFltNo(b2c.getFltNo());
//				vo.setFltDate(b2c.getDepDate());
//				vo.setSeat(b2c.getCabin());
//				vo.setSeatCount(b2c.getRemainSite().toString());
//				if("PUT954".equals(b2c.getPageType())){
//					vo.setPriceFrom("JQ");
//				}
//				if("PUT955".equals(b2c.getPageType())){
//					vo.setPriceFrom("TZ");
//				}
//				if("PUT956".equals(b2c.getPageType())){
//					vo.setPriceFrom("TR");
//				}
//				if("PUT957".equals(b2c.getPageType())){
//					vo.setPriceFrom("VY");
//				}
//				if("PUT958".equals(b2c.getPageType())){
//					vo.setPriceFrom("VJ");
//				}
//				if("PUT959".equals(b2c.getPageType())){
//					vo.setPriceFrom("air5J");
//				}
//				if("PUT960".equals(b2c.getPageType())){
//					vo.setPriceFrom("MM");
//				}
//				if("PUT961".equals(b2c.getPageType())){
//					vo.setPriceFrom("JT");
//				}
//				if("PUT962".equals(b2c.getPageType())){
//					vo.setPriceFrom("JW");
//				}
//				vo.setFltTime(b2c.getDepDate()+" "+b2c.getDepTime()+":00");
//				vo.setArrTime(b2c.getEndDate()+" "+b2c.getDesTime()+":00");
//				vo.setTax(b2c.getSalePrice().toString());
//				vo.setYPrice(b2c.getTicketPrice().toString());
//				vo.setAdtPrice(b2c.getTicketPrice().toString());
//				vo.setYuanShiPrice(b2c.getTicketPrice().toString());
//				vo.setCNYPrice(b2c.getTicketPrice().toString());
//				vos.add(vo);
//			}
//		}
//		return vos;
//	}
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
	public String getFltNo() {
		return FltNo;
	}
	@JsonIgnore
	public void setFltNo(String fltNo) {
		FltNo = fltNo;
	}
	@JsonIgnore
	public String getSeat() {
		return Seat;
	}
	@JsonIgnore
	public void setSeat(String seat) {
		Seat = seat;
	}
	@JsonIgnore
	public String getSeatCount() {
		return SeatCount;
	}
	@JsonIgnore
	public void setSeatCount(String seatCount) {
		SeatCount = seatCount;
	}
	@JsonIgnore
	public String getFltDate() {
		return FltDate;
	}
	@JsonIgnore
	public void setFltDate(String fltDate) {
		FltDate = fltDate;
	}
	@JsonIgnore
	public String getPataPrice() {
		return PataPrice;
	}
	@JsonIgnore
	public void setPataPrice(String pataPrice) {
		PataPrice = pataPrice;
	}
	@JsonIgnore
	public String getPataRate() {
		return PataRate;
	}
	@JsonIgnore
	public void setPataRate(String pataRate) {
		PataRate = pataRate;
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
	public String getTax() {
		return Tax;
	}
	@JsonIgnore
	public void setTax(String tax) {
		Tax = tax;
	}
	@JsonIgnore
	public String getAddFee() {
		return AddFee;
	}
	@JsonIgnore
	public void setAddFee(String addFee) {
		AddFee = addFee;
	}
	@JsonIgnore
	public String getIsShare() {
		return IsShare;
	}
	@JsonIgnore
	public void setIsShare(String isShare) {
		IsShare = isShare;
	}
	@JsonIgnore
	public String getModel() {
		return Model;
	}
	@JsonIgnore
	public void setModel(String model) {
		Model = model;
	}
	@JsonIgnore
	public String getPlaneSize() {
		return PlaneSize;
	}
	@JsonIgnore
	public void setPlaneSize(String planeSize) {
		PlaneSize = planeSize;
	}
	@JsonIgnore
	public String getMainFltNo() {
		return MainFltNo;
	}
	@JsonIgnore
	public void setMainFltNo(String mainFltNo) {
		MainFltNo = mainFltNo;
	}
	@JsonIgnore
	public String getAccuteRate() {
		return AccuteRate;
	}
	@JsonIgnore
	public void setAccuteRate(String accuteRate) {
		AccuteRate = accuteRate;
	}
	@JsonIgnore
	public String getQneRank() {
		return QneRank;
	}
	@JsonIgnore
	public void setQneRank(String qneRank) {
		QneRank = qneRank;
	}
	@JsonIgnore
	public String getIsTH() {
		return IsTH;
	}
	@JsonIgnore
	public void setIsTH(String isTH) {
		IsTH = isTH;
	}
	@JsonIgnore
	public String getYPrice() {
		return YPrice;
	}
	@JsonIgnore
	public void setYPrice(String yPrice) {
		YPrice = yPrice;
	}
	@JsonIgnore
	public String getMoneyType() {
		return MoneyType;
	}
	@JsonIgnore
	public void setMoneyType(String moneyType) {
		MoneyType = moneyType;
	}
	@JsonIgnore
	public String getAdtPrice() {
		return AdtPrice;
	}
	@JsonIgnore
	public void setAdtPrice(String adtPrice) {
		AdtPrice = adtPrice;
	}
	@JsonIgnore
	public String getYuanShiPrice() {
		return YuanShiPrice;
	}
	@JsonIgnore
	public void setYuanShiPrice(String yuanShiPrice) {
		YuanShiPrice = yuanShiPrice;
	}
	@JsonIgnore
	public String getCNYPrice() {
		return CNYPrice;
	}
	@JsonIgnore
	public void setCNYPrice(String cNYPrice) {
		CNYPrice = cNYPrice;
	}
	@JsonIgnore
	public String getAdtAgentRate() {
		return AdtAgentRate;
	}
	@JsonIgnore
	public void setAdtAgentRate(String adtAgentRate) {
		AdtAgentRate = adtAgentRate;
	}
	@JsonIgnore
	public String getCabinNote() {
		return CabinNote;
	}
	@JsonIgnore
	public void setCabinNote(String cabinNote) {
		CabinNote = cabinNote;
	}
	@JsonIgnore
	public String getSignRule() {
		return SignRule;
	}
	@JsonIgnore
	public void setSignRule(String signRule) {
		SignRule = signRule;
	}
	@JsonIgnore
	public String getRefundRule() {
		return RefundRule;
	}
	@JsonIgnore
	public void setRefundRule(String refundRule) {
		RefundRule = refundRule;
	}
	@JsonIgnore
	public String getEndorsRule() {
		return EndorsRule;
	}
	@JsonIgnore
	public void setEndorsRule(String endorsRule) {
		EndorsRule = endorsRule;
	}
}
