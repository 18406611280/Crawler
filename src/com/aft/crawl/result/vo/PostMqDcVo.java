package com.aft.crawl.result.vo;


public class PostMqDcVo {

	/**
	  	航班动态：	flightChange
		机场大屏：	rtFlight
		FD运价：		fdfare
		NFD运价：		nfdfare
		百拓特殊政策：	btNfdPolicy
		天气数据：	weatherData
	 */
	private String type;
	
	private String datas;
	
	public PostMqDcVo() { }

	public PostMqDcVo(String type, String datas) {
		this.type = type;
		this.datas = datas;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDatas() {
		return datas;
	}

	public void setDatas(String datas) {
		this.datas = datas;
	}
}