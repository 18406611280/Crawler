package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;

/**
 * 华夏航空 
 */
public class B2CG5Crawler extends Crawler {

	private final static String queryUrl = "http://www.chinaexpressair.com/flight.ac";
	
	private static long changeIpTime = System.currentTimeMillis();
	
	public B2CG5Crawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			Elements eleOneForms = document.select("div.lines > ul.lines-bd > li.line-item > div.f-cb > div.col > div.f-ib > form");
			if(null == eleOneForms || eleOneForms.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
			for(Element eleOneForm : eleOneForms) {				
				// 出发,到达
				String depCode = eleOneForm.getElementById("boardPoint").val().trim();
				String desCode = eleOneForm.getElementById("offPoint").val().trim();
				
				// 航班号,航司
				String fltNo = eleOneForm.getElementById("flightno").val().toUpperCase();
				String airlineCode = eleOneForm.getElementById("carrier").val().toUpperCase();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				// 出发日期
				String depTime = eleOneForm.getElementById("departuretime").val().trim();
				String desTime = eleOneForm.getElementById("arrivaltime").val().trim();
				String depDate = eleOneForm.getElementById("departuredate").val().trim();
				
				String cabin = eleOneForm.getElementById("code").val().trim();

				BigDecimal ticketPrice = new BigDecimal(eleOneForm.getElementById("price").val().trim()).setScale(0);
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
				crawlResult.setDepTime(depTime);	// 出发时间
				crawlResult.setDesTime(desTime);	// 到达时间
				
				crawlResult.setRemainSite(10);
				crawlResult.setTicketPrice(ticketPrice);
				crawlResult.setSalePrice(ticketPrice);
				crawlResults.add(crawlResult);
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	public String httpResult() throws Exception {
		if(System.currentTimeMillis() - changeIpTime >= 5 * 60 * 1000) {
			synchronized(queryUrl) {
				if(System.currentTimeMillis() - changeIpTime >= 5 * 60 * 1000) {
					super.changeProxy();
					changeIpTime = System.currentTimeMillis();
				}
			}
		}
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("reqCode", "queryDCFlights");
		paramMap.put("adult", "1");
		paramMap.put("child", "0");
		paramMap.put("baby", "0");
		paramMap.put("homecity_name", this.getJobDetail().getDepName());
		paramMap.put("getcity_name", this.getJobDetail().getDesName());
		paramMap.put("sdate", this.getJobDetail().getDepDate());
		return this.httpProxyPost(queryUrl, paramMap, "html");
	}
}