package com.aft.crawl.result.vo.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.vo.CrawlResultBase;

/**
 * 采集航班信息
 * @author liangdongming
 *
 */
public class FlightData extends CrawlResultBase{
	
	private static final long serialVersionUID = UUID.randomUUID().getLeastSignificantBits();

	/**
	 * 创建时间
	 * 2017-03-30 23:51:27
	 */
	private String createTime;
	
	/** 航程类型 OW:单程, RT:往返 */
	private String routeType;
	
	/**
	 * 航司
	 */
	private String airlineCode;
	
	/**
	 * 起飞地
	 */
	private String depAirport;
	/**
	 * 目的地
	 */
	private String arrAirport;
	/**
	 * 出发日期
	 * 2017-04-01
	 */
	private String goDate;
	/**
	 * 返程日期
	 * 2017-04-04
	 */
	private String backDate;
	/**
	 * 规则
	 */
	private List<FilghtRule> rule;
	/**
	 * 价格信息
	 */
	private List<FlightPrice> prices;
	/**
	 * 去程航班信息
	 */
	private List<FlightSegment> fromSegments;
	/**
	 * 返程航班信息
	 */
	private List<FlightSegment> retSegments;
	/**
	 * 备注信息
	 */
	private String memo;
	
	/** 任务id */
	private Integer jobId;
	
	public FlightData(JobDetail jobDetail, String routeType,String depCode, String desCode, String depDate) {
		this(jobDetail, routeType,depCode, desCode, depDate, null);
	}
	
	public FlightData(JobDetail jobDetail, String routeType,String depCode, String desCode, String depDate, String backDate) {
		super(jobDetail.getPageType(), jobDetail.getCrawlMark());
		this.depAirport = depCode;
		this.arrAirport = desCode;
		this.goDate = depDate;
		this.backDate = backDate;
		this.routeType = routeType;
		this.jobId = jobDetail.getTimerJob().getJobId();
	}
	
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
	public String getBackDate() {
		return backDate;
	}
	public void setBackDate(String backDate) {
		this.backDate = backDate;
	}
	public List<FlightSegment> getFromSegments() {
		return fromSegments;
	}
	public void setFromSegments(List<FlightSegment> fromSegments) {
		this.fromSegments = fromSegments;
	}
	public String getCreateTime() {
		if(createTime ==null){
			SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
			createTime = SDF_YMD.format(new Date());
		}
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public List<FlightSegment> getRetSegments() {
		return retSegments;
	}
	public void setRetSegments(List<FlightSegment> retSegments) {
		this.retSegments = retSegments;
	}
	public String getAirlineCode() {
		return airlineCode;
	}
	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}
	public List<FlightPrice> getPrices() {
		return prices;
	}
	public void setPrices(List<FlightPrice> prices) {
		this.prices = prices;
	}
	public String getRouteType() {
		return routeType;
	}
	public void setRouteType(String routeType) {
		this.routeType = routeType;
	}
	public List<FilghtRule> getRule() {
		return rule;
	}
	public void setRule(List<FilghtRule> rule) {
		this.rule = rule;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}
}
