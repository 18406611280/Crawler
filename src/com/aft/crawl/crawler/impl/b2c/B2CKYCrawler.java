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
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;

/**
 * 昆明航空 
 */
public class B2CKYCrawler extends Crawler {

	private final static String queryUrl = "http://www.airkunming.com/booking/flightSearch";
	
	public B2CKYCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			
			String depCode = document.getElementById("orgCity").val();
			String desCode = document.getElementById("dstCity").val();
			String depDate = document.getElementById("leaveFlightDate").val();
			Elements eleSegments = document.select("#slide > div.tupian > div.slide_a > div[flighttype=leaveFlight] > div[name=segment" + depCode + desCode + "]");
			if(null == eleSegments || eleSegments.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + document.getElementById("slide").text());
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
			for(Element eleSegment : eleSegments) {
				Elements eleTds = eleSegment.select("> table > tbody > tr:eq(0) > td");
				
				// 航班号,航司
				String fltNo = eleTds.get(0).select("> div").first().ownText().trim().toUpperCase();
				String airlineCode = fltNo.substring(0, 2);
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				// 出发日期
				String depTime = eleTds.get(1).select("> div > font").first().ownText().trim();
				String desTime = eleTds.get(3).select("> div > font").first().ownText().trim();
				
				Elements eleTrs = eleTds.get(5).select("li[name=classPriceLi" + fltNo + "] > div.zi > table > tbody > tr");
				if(null == eleTrs || eleTrs.isEmpty()) {
					logger.info(this.getJobDetail().toStr() + ", 航班价格信息为空...");
					continue ;
				}
				eleTrs.remove(0);	// 忽略第一个, 后面有...
				for(Element eleTr : eleTrs) {
//					B;
//					1983.0*1170.0*230.0:0,0,5;
//					KY8283;
//					A;
//					85;
//					B;
//					;
//					;
//					2110.0*1170.0*230.0;
//					2016-01-20 08:00:00;
//					90.0;
//					昆明;上海浦东;
//					2016年1月20日
					String cabinInfo = eleTr.select("> td:eq(1) > input[name=classPrice" + depCode + desCode + "]").first().val();
					String[] cabinInfos = cabinInfo.split(";");
					
					String cabin = cabinInfos[0].trim().toUpperCase();
					
					// 价格
					BigDecimal ticketPrice = new BigDecimal(cabinInfos[1].trim().split("\\*")[0].trim()).setScale(0);
					
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					
					// 剩余座位数
					crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(cabinInfos[3].trim().trim()));
					crawlResult.setTicketPrice(ticketPrice);
					crawlResult.setSalePrice(ticketPrice);
					crawlResults.add(crawlResult);
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
		String title = document.title();
		return title.contains("403") || title.contains("拒绝");
	}
	
	@Override
	public String httpResult() throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("flightType", "DC");
		paramMap.put("source", "airkunming");
		paramMap.put("orgCity", this.getJobDetail().getDepCode());
		paramMap.put("dstCity", this.getJobDetail().getDesCode());
		paramMap.put("flightDates", this.getJobDetail().getDepDate());
		return this.httpProxyPost(queryUrl, paramMap, "gb2312", "html");
	}
}