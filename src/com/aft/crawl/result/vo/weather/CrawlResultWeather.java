package com.aft.crawl.result.vo.weather;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.vo.CrawlResultBase;

public class CrawlResultWeather extends CrawlResultBase {

	private static final long serialVersionUID = 4726293732052280457L;

	// 机场三字码
	private String cityCode;
	
	// 最高温度
	private String hightTemperature;
	
	// 最低温度
	private String lowTemperature;
	
	// 天气
	private String condition;
	
	// 日期
	private String weatherDate;
	
	// 风力
	private String wind;
	
	public CrawlResultWeather(JobDetail jobDetail, String cityCode) {
		super(jobDetail.getPageType(), jobDetail.getCrawlMark());
		this.cityCode = cityCode;
	}

	public String getCityCode() {
		return cityCode;
	}

	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}

	public String getHightTemperature() {
		return hightTemperature;
	}

	public void setHightTemperature(String hightTemperature) {
		this.hightTemperature = hightTemperature;
	}

	public String getLowTemperature() {
		return lowTemperature;
	}

	public void setLowTemperature(String lowTemperature) {
		this.lowTemperature = lowTemperature;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getWeatherDate() {
		return weatherDate;
	}

	public void setWeatherDate(String weatherDate) {
		this.weatherDate = weatherDate;
	}

	public String getWind() {
		return wind;
	}

	public void setWind(String wind) {
		this.wind = wind;
	}
}