package com.aft.crawl.result.vo.lcc.vo.mt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LCCMtPostFlts {

	// 出发
	@JsonProperty
	private String FromCode;
	// 到达
	@JsonProperty
	private String ToCode;


	// 起飞时间  DD/MM/YYYY hh:mm:ss
	@JsonProperty
	private String FltTime;

	// 到达时间 DD/MM/YYYY hh:mm:ss,如果是单程，默认01/01/0001 00:00:00
	@JsonProperty
	private String ArrTime;

	// 航班号
	@JsonProperty
	private String FltNo;
	// 是否共享航班，没有就为null
	@JsonProperty
	private String MainNo;
	// 舱位
	@JsonProperty
	private String Seat;
	// 舱位数量
	@JsonProperty
	private String Qty;
	// 航班顺序  针对于联程多航段，第一段为1，第二段为2，以此类推
	@JsonProperty
	private String Seq;
	// 舱位类型  经济舱/公务舱……，没有就填null
	@JsonProperty
	private String SeatType;
	// 机型
	@JsonProperty
	private String Plane;
	// 出发航站楼
	@JsonProperty
	private String FromHall;
	// 到达航站楼
	@JsonProperty
	private String ToHall;
	// 飞行时间
	@JsonProperty
	private String LastMin;

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
	public String getFltNo() {
		return FltNo;
	}
	@JsonIgnore
	public void setFltNo(String fltNo) {
		FltNo = fltNo;
	}
	@JsonIgnore
	public String getMainNo() {
		return MainNo;
	}
	@JsonIgnore
	public void setMainNo(String mainNo) {
		MainNo = mainNo;
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
	public String getQty() {
		return Qty;
	}
	@JsonIgnore
	public void setQty(String qty) {
		Qty = qty;
	}
	@JsonIgnore
	public String getSeq() {
		return Seq;
	}
	@JsonIgnore
	public void setSeq(String seq) {
		Seq = seq;
	}
	@JsonIgnore
	public String getSeatType() {
		return SeatType;
	}
	@JsonIgnore
	public void setSeatType(String seatType) {
		SeatType = seatType;
	}
	@JsonIgnore
	public String getPlane() {
		return Plane;
	}
	@JsonIgnore
	public void setPlane(String plane) {
		Plane = plane;
	}
	@JsonIgnore
	public String getFromHall() {
		return FromHall;
	}
	@JsonIgnore
	public void setFromHall(String fromHall) {
		FromHall = fromHall;
	}
	@JsonIgnore
	public String getToHall() {
		return ToHall;
	}
	@JsonIgnore
	public void setToHall(String toHall) {
		ToHall = toHall;
	}
	@JsonIgnore
	public String getLastMin() {
		return LastMin;
	}
	@JsonIgnore
	public void setLastMin(String lastMin) {
		LastMin = lastMin;
	}
	
}
