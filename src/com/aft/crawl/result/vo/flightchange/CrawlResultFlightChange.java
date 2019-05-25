package com.aft.crawl.result.vo.flightchange;

import java.math.BigDecimal;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlResultFlightChange extends CrawlResultBase {

	private static final long serialVersionUID = 2346768878478652513L;
	
	/** 航司二字吗*/
	private String airlineCode;

	/** 航班号*/
	private String fltNo;

	/** 出发三字码*/
	private String depCode;

	/** 到达三字码*/
	private String desCode;

	/** 出发日期*/
	private String depDate;

	/** 计划起飞时间 */
	private String planDepTime;

	/** 实际起飞时间 */
	private String depTime;

	/** 计划到达时间 */
	private String planDesTime;

	/** 实际到达时间 */
	private String desTime;

	/** 状态(到达/取消) */
	private String status;

	/** 总里程 */
	private String mileage;

	/** 飞行时长 */
	private String fltTime;

	/** 机型 */
	private String modelType;

	/** 机龄 */
	private String fltAge;

	/** 历史航班准点率 */
	private BigDecimal zdl;

	/** 当日出发机场进港准点率 */
	private BigDecimal depJGzdl;

	/** 当日出发机场出港准点率 */
	private BigDecimal depCGzdl;

	/** 当日到达机场进港准点率 */
	private BigDecimal desJGzdl;

	/** 当日到达机场出港准点率 */
	private BigDecimal desCGzdl;
	
	public CrawlResultFlightChange(JobDetail jobDetail, String fltNo, String depDate) {
		super(jobDetail.getPageType(), jobDetail.getCrawlMark());
		this.fltNo = fltNo;
		this.depDate = depDate;
		this.airlineCode = fltNo.substring(0, 2);
	}
	
	public CrawlResultFlightChange(JobDetail jobDetail, String depCode, String desCode, String depDate) {
		super(jobDetail.getPageType(), jobDetail.getCrawlMark());
		this.depCode = depCode;
		this.desCode = desCode;
		this.depDate = depDate;
	}

	public String getAirlineCode() {
		return airlineCode;
	}

	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}

	public String getFltNo() {
		return fltNo;
	}

	public void setFltNo(String fltNo) {
		this.fltNo = fltNo;
	}

	public String getDepCode() {
		return depCode;
	}

	public void setDepCode(String depCode) {
		this.depCode = depCode;
	}

	public String getDesCode() {
		return desCode;
	}

	public void setDesCode(String desCode) {
		this.desCode = desCode;
	}

	public String getDepDate() {
		return depDate;
	}

	public void setDepDate(String depDate) {
		this.depDate = depDate;
	}

	public String getPlanDepTime() {
		return planDepTime;
	}

	public void setPlanDepTime(String planDepTime) {
		this.planDepTime = planDepTime;
	}

	public String getDepTime() {
		return depTime;
	}

	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}

	public String getPlanDesTime() {
		return planDesTime;
	}

	public void setPlanDesTime(String planDesTime) {
		this.planDesTime = planDesTime;
	}

	public String getDesTime() {
		return desTime;
	}

	public void setDesTime(String desTime) {
		this.desTime = desTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMileage() {
		return mileage;
	}

	public void setMileage(String mileage) {
		this.mileage = mileage;
	}

	public String getFltTime() {
		return fltTime;
	}

	public void setFltTime(String fltTime) {
		this.fltTime = fltTime;
	}

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	public String getFltAge() {
		return fltAge;
	}

	public void setFltAge(String fltAge) {
		this.fltAge = fltAge;
	}

	public BigDecimal getZdl() {
		return zdl;
	}

	public void setZdl(BigDecimal zdl) {
		this.zdl = zdl;
	}

	public BigDecimal getDepJGzdl() {
		return depJGzdl;
	}

	public void setDepJGzdl(BigDecimal depJGzdl) {
		this.depJGzdl = depJGzdl;
	}

	public BigDecimal getDepCGzdl() {
		return depCGzdl;
	}

	public void setDepCGzdl(BigDecimal depCGzdl) {
		this.depCGzdl = depCGzdl;
	}

	public BigDecimal getDesJGzdl() {
		return desJGzdl;
	}

	public void setDesJGzdl(BigDecimal desJGzdl) {
		this.desJGzdl = desJGzdl;
	}

	public BigDecimal getDesCGzdl() {
		return desCGzdl;
	}

	public void setDesCGzdl(BigDecimal desCGzdl) {
		this.desCGzdl = desCGzdl;
	}
}