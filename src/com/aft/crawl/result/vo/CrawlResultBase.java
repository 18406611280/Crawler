package com.aft.crawl.result.vo;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.aft.crawl.CrawlerType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CrawlResultBase implements Serializable {
	
	private static final long serialVersionUID = UUID.randomUUID().getLeastSignificantBits();

	/** 抓取页面类型 */
	private String pageType;
	
	/** 抓取页面类型(文字说明) */
	private String pageTypeMemo;
	
	/** 采集标识(yyyyMMddhhMM) */
	private String crawlMark;
	
	@JsonIgnore
	private int crawlStatus = 2;	// 2: 采集成功; 3: 采集超时; 4: 异常,失败
	
	public CrawlResultBase(String pageType, String crawlMark) {
		this.setPageType(pageType);
		this.crawlMark = crawlMark;
	}
	
	/**
	 * 需要的重写
	 * @return
	 */
	public String toSaveFileStr() {
		return null;
	}
	
	public String getPageType() {
		return pageType;
	}

	public void setPageType(String pageType) {
		if(null != pageType) {
			this.pageType = pageType;
			this.pageTypeMemo = CrawlerType.getCrawlerType(pageType).getPageTypeMemo();
			if(StringUtils.isEmpty(pageTypeMemo)) throw new RuntimeException("抓取页面类型(文字说明)为空...");
		}
	}

	public String getPageTypeMemo() {
		return pageTypeMemo;
	}

	public void setPageTypeMemo(String pageTypeMemo) {
		this.pageTypeMemo = pageTypeMemo;
	}

	public String getCrawlMark() {
		return crawlMark;
	}

	public void setCrawlMark(String crawlMark) {
		this.crawlMark = crawlMark;
	}

	public int getCrawlStatus() {
		return crawlStatus;
	}

	public void setCrawlStatus(int crawlStatus) {
		this.crawlStatus = crawlStatus;
	}
}