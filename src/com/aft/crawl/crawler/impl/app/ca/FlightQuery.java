package com.aft.crawl.crawler.impl.app.ca;

import java.io.Serializable;

/**
 * 航班查询参数
 * 
 * @author huzhenhua
 * 
 */
public class FlightQuery implements Serializable {
	
	private static final long serialVersionUID = 6455148686673805052L;

	/** 国航APP接口访问IP */
	private String ip;

	/** 国航APP接口访问端口 */
	private int port;

	/** 出发地 */
	private String dep;

	/** 目的地 */
	private String arr;

	/** 出发日期 */
	private String depDate;

	/** 航班号 */
	private String flightNo;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDep() {
		return dep;
	}

	public void setDep(String dep) {
		this.dep = dep;
	}

	public String getArr() {
		return arr;
	}

	public void setArr(String arr) {
		this.arr = arr;
	}

	public String getDepDate() {
		return depDate;
	}

	public void setDepDate(String depDate) {
		this.depDate = depDate;
	}

	public String getFlightNo() {
		return flightNo;
	}

	public void setFlightNo(String flightNo) {
		this.flightNo = flightNo;
	}
}