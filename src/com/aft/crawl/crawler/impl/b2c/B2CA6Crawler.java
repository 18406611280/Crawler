package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 红土航空 
 */
public class B2CA6Crawler extends Crawler {
	
	private final static String queryUrl = "http://www.redair.cn/booking/ajaxFlightSearch?airwayType=DC&orgCity=%depCode%&dstCity=%desCode%&flightDate=%depDate%";
	
	public B2CA6Crawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			List<Map<String, Object>> flights = (List<Map<String, Object>>)resultMap.get("flights");
			if(null == flights || flights.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
			for(Map<String, Object> flihtMap : flights) {
				List<Map<String, Object>> segments = (List<Map<String, Object>>)flihtMap.get("segments");
				if(null == segments || segments.isEmpty()) continue ;
				
				Map<String, Object> avFlightShoppingMap = (Map<String, Object>)segments.get(0).get("avFlightShopping");
				List<Map<String, Object>> comfortableCabinPrices = (List<Map<String, Object>>)segments.get(0).get("comfortableCabinPrices");
				List<Map<String, Object>> economyCabinPrices = (List<Map<String, Object>>)segments.get(0).get("economyCabinPrices");
				economyCabinPrices.addAll(comfortableCabinPrices);
				
				// 出发机场
				String depCode = flihtMap.get("orgCity").toString().toUpperCase().trim();
				
				// 到达机场
				String desCode = flihtMap.get("dstCity").toString().toUpperCase().trim();
				
				// 出发日期
				String depDate = flihtMap.get("flightDate").toString().trim();
				for(Map<String, Object> economyCabinPriceMap : economyCabinPrices) {
					// 航班号,航司
					String fltNo = economyCabinPriceMap.get("airline").toString().toUpperCase().trim();
					String airlineCode = fltNo.substring(0, 2);
					
					// 判断共享
					String shareFlight = this.getShareFlight(airlineCode);
					
					// 出发时间
					String depTime = avFlightShoppingMap.get("fmtOrgTime").toString().trim();
					
					// 到达时间
					String desTime = avFlightShoppingMap.get("fmtDstTime").toString().trim();
					
					// 舱位
					String cabin = economyCabinPriceMap.get("cabin").toString().trim().toUpperCase();
					
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					
					// 剩余座位数
					List<Map<String, Object>> cabinNumbers = (List<Map<String, Object>>)avFlightShoppingMap.get("cabinNumbers");
					for(Map<String, Object> cabinNumberMap : cabinNumbers) {
						if(!cabin.equals(cabinNumberMap.get("cabin"))) continue ;
						crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(cabinNumberMap.get("avNum").toString()));
						break ;
					}
					
					// 价格
					BigDecimal ticketPrice = new BigDecimal(economyCabinPriceMap.get("price").toString().trim());
					crawlResult.setTicketPrice(ticketPrice);
					crawlResult.setSalePrice(crawlResult.getTicketPrice());
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
	public String httpResult() throws Exception {
		String url = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode())
								.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return super.httpProxyGet(url, "json");
	}
}