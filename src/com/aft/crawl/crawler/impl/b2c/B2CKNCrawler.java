package com.aft.crawl.crawler.impl.b2c;

import java.util.Map;

import org.jsoup.nodes.Document;

/**
 * 联合航空(已放 东航 @see B2CMUCrawler) 
 */
public class B2CKNCrawler extends B2CMUCrawler {
	
	private final static String queryUrl = "http://www.flycua.com/otabooking/flight-search!doFlightSearch.shtml";

	public B2CKNCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public String httpResult() throws Exception {
		return httpResult(queryUrl);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean requestAgain(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		if(null == jsonObject) return false;
		
		Map<String, Object> mapResult = (Map<String, Object>)jsonObject;
		return "OTR20000".equals(mapResult.get("resultCode"));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
		Map<String, Object> mapResult = (Map<String, Object>)jsonObject;
		return null == mapResult.get("sessionId");
	}
}