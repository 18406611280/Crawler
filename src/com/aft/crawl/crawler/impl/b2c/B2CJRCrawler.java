package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.HttpClients;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 幸福航空 
 */
public class B2CJRCrawler extends Crawler {
	
	private final static String queryUrl = "http://www.joy-air.com/pssweb/ota/flights?adultNum=1&childNum=0&depDate=%depDate%&dstcity=%desCode%&flightWayType=OW&orgcity=%depCode%";
	
	private static MyHttpClientSessionVo httpClientSessionVo = null;
	
	public B2CJRCrawler(String threadMark) {
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
			List<Map<String, Object>> flihtProductList = (List<Map<String, Object>>)resultMap.get("flihtProductList");
			if(null == flihtProductList || flihtProductList.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
			for(Map<String, Object> flihtProductMap : flihtProductList) {
				Map<String, Object> goFlightMap = (Map<String, Object>)flihtProductMap.get("goFlight");
				
				// 航班号,航司
				String airlineCode = goFlightMap.get("airlineCode").toString().toUpperCase().trim();
				String fltNo = goFlightMap.get("flightNumber").toString().toUpperCase().trim();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				// 出发机场
				String depCode = goFlightMap.get("daparturePortCode").toString().toUpperCase().trim();
				
				// 到达机场
				String desCode = goFlightMap.get("arrivalPortCode").toString().toUpperCase().trim();
				
				// 出发日期
				String depDate = goFlightMap.get("takeoffDateTime").toString().trim().substring(0, 10);
				
				// 出发时间
				String depTime = goFlightMap.get("takeoffTime").toString().trim();
				
				// 到达时间
				String desTime = goFlightMap.get("arrivalTime").toString().trim();

				List<Map<String, Object>> fareList = (List<Map<String, Object>>)flihtProductMap.get("fareList");
				for(Map<String, Object> fareMap : fareList) {
					// 舱位
					String cabin = fareMap.get("cabinClass").toString().trim().toUpperCase();
					
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					
					// 剩余座位数
					crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(fareMap.get("count").toString()));
					
					// 价格
					BigDecimal ticketPrice = new BigDecimal(fareMap.get("publishPrice").toString().trim());
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
	protected boolean returnEmptyChangeIp(String httpResult) {
		return StringUtils.isEmpty(httpResult);
	}

	@Override
	public String httpResult() throws Exception {
		if(null == httpClientSessionVo) {
			synchronized(queryUrl) {
				headerMap.put("Referer", "http://www.joy-air.com/pssui/joyairportal/views/");
				httpClientSessionVo = new MyHttpClientSessionVo(new MyHttpClientSession(HttpClients.createDefault()), headerMap);
			}
		}
		String url = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode())
								.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return super.httpProxyGet(httpClientSessionVo, url, "json");
	}
}