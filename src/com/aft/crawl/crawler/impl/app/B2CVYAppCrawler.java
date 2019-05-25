package com.aft.crawl.crawler.impl.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.aft.app.vy.http.SslRequest;
import com.aft.app.vy.temp.GetQueryOutBoundEntity;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.utils.jackson.MyJsonTransformUtil;


/**
 * 伏林航空 app  
 */
public class B2CVYAppCrawler extends Crawler {
	
	
	private final static String queryUrl = "https://apimobile.vueling.com/Vueling.Mobile.BookingServices.WebAPI/api/v3/Price/DoAirPriceSB";
	
	public B2CVYAppCrawler(String threadMark) {
		super(threadMark, "0");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		System.out.println(httpResult);
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 返回httpResult为空:" + httpResult);
			return null;
		}
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Map<String, Object> trip = (Map<String, Object>)resultMap.get("Trip");
			List<Map<String, Object>> journeyMarkets = (List<Map<String, Object>>)trip.get("JourneyMarkets");
			
			for(Map<String, Object> journeyMarket:journeyMarkets){
				
				List<Map<String, Object>> journeys = (List<Map<String, Object>>)journeyMarket.get("Journeys");
				
				
				
				for(Map<String, Object> journey : journeys){
					String depCode = journey.get("DepartureStation").toString();
					String desCode = journey.get("ArrivalStation").toString();
					String depDate = journey.get("STD").toString().substring(0, 10);
					List<Map<String, Object>> segments = (List<Map<String, Object>>)journey.get("Segments");
					List<Map<String, Object>> journeyFares = (List<Map<String, Object>>)journey.get("JourneyFare");
					for(Map<String, Object> journeyFare : journeyFares){
						CrawlResultInter b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
						b2c.setRouteType("OW");
						
						String isFareAvailable  = journeyFare.get("IsFareAvailable").toString();
						if(null == isFareAvailable || "false".equals(isFareAvailable))continue;	

						String amount = journeyFare.get("Amount").toString();
						String cabin = journeyFare.get("ProductClass").toString();
						cabin = cabin.substring(0, 1);
						String availableCount = journeyFare.get("AvailableCount").toString();
						String remainSite = "";
						if("0".equals(availableCount))remainSite="5";
						else remainSite=availableCount;

						String currencyCode = journeyFare.get("CurrencyCode").toString();
						BigDecimal ticketPrice = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP);
						b2c = CrawlerUtil.calPrice(b2c, ticketPrice, new BigDecimal(0), currencyCode,rateMap);
						if(!"CNY".equals(b2c.getCurrency()))continue;//转换币种失败直接废弃这条数据
						
						List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
						for(int i=0;i<segments.size();i++){
							Map<String, Object>  segment = segments.get(i);
							String segmentSellKey = segment.get("SegmentSellKey").toString();
							String airlineCode = segmentSellKey.substring(0, 2);
							String fltNo = airlineCode+segment.get("FlightNumber").toString();
							String std = segment.get("STD").toString();
							String sta = segment.get("STA").toString();
							String tripDepDate = std.substring(0,10);
							String tripDepTime = std.substring(11, 16);
							String tripDesDate = sta.substring(0, 10);
							String tripDesTime = sta.substring(11, 16);
							String tripDepCode = segment.get("DepartureStation").toString();
							String tripDesCode = segment.get("ArrivalStation").toString();

							CrawlResultInterTrip t = new CrawlResultInterTrip(airlineCode,fltNo,tripDepCode,tripDesCode,tripDepDate,"Y",i+1,1);
							t.setDesDate(tripDesDate);
							t.setDepTime(tripDepTime);
							t.setDesTime(tripDesTime);
							if(remainSite!=null && !"".equals(remainSite)){
								t.setRemainSite(Integer.valueOf(remainSite));
							}
							flightTrips.add(t);
						}
						if(flightTrips.size()>0){
							b2c.setFlightTrips(flightTrips);
							crawlResults.add(b2c);
						}
					}
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
		GetQueryOutBoundEntity entity = new GetQueryOutBoundEntity(this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(),this.getJobDetail().getDepDate(), "EUR");// 出发城市，到达城市，出发日期，币种类
		SslRequest req = new SslRequest(queryUrl);
		String httpRequset = req.Post(entity.getEntity());
//		System.out.println(httpRequset);
		return httpRequset;
	}
}