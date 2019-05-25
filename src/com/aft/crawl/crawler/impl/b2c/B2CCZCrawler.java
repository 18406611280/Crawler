package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 南方航空 
 */
public class B2CCZCrawler extends Crawler {

	private final static String queryUrl = "http://b2c.csair.com/B2C40/query/jaxb/direct/query.ao";
	
	private final static String params = "{\"depcity\": \"%depCode%\",\"arrcity\": \"%desCode%\",\"flightdate\": \"%depDate%\",\"adultnum\": \"1\",\"childnum\": \"0\",\"infantnum\": \"0\",\"cabinorder\": \"0\",\"airline\": \"1\",\"flytype\": \"0\",\"international\": \"0\",\"action\": \"0\",\"segtype\": \"1\",\"cache\": \"0\",\"preUrl\": \"\"}";
	
	public B2CCZCrawler(String threadMark) {
		super(threadMark);
	}
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object objMsg = mapResult.get("message");
			if(null != objMsg) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			FlightData flightData = null;
			Map<String, Object> segmentMap = (Map<String, Object>)mapResult.get("segment");
			Map<String, Object> dateFlightMap = (Map<String, Object>)segmentMap.get("dateflight");
			List<Map<String, Object>> flights = (List<Map<String, Object>>)dateFlightMap.get("flight");
			for(Map<String, Object> flightMap : flights) {
				// 航班号,航司
				String fltNo = flightMap.get("flightno").toString().trim().toUpperCase();
				String airlineCode = flightMap.get("airline").toString().trim().toUpperCase();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				Object codeShare = flightMap.get("codeshare");
				if(null != codeShare && Boolean.valueOf(codeShare.toString())) shareFlight = "Y";
//				if ("Y".endsWith(shareFlight)) continue;//移除共享
				// 出发,到达
				String depCode = flightMap.get("depport").toString().trim().toUpperCase();
				String desCode = flightMap.get("arrport").toString().trim().toUpperCase();
				
				// 出发日期
				String depDate = flightMap.get("depdate").toString().trim();
				depDate = depDate.substring(0, 4) + "-" + depDate.substring(4, 6) + "-" + depDate.substring(6);
				
				// 出发时间
				String depTime = flightMap.get("deptime").toString().trim();
				depTime = depTime.substring(0, 2) + ":" + depTime.substring(2);
				
				// 到达时间
				String desTime = flightMap.get("arrtime").toString().trim();
				desTime = desTime.substring(0, 2) + ":" + desTime.substring(2);
				
				List<Map<String, Object>> cabins = (List<Map<String, Object>>)flightMap.get("cabin");
				for(Map<String, Object> cabinMap : cabins) {
					String cabin = cabinMap.get("name").toString().trim().toUpperCase();
//					if(!this.allowCabin(cabin))continue;
					// 剩余座位数
					String remainSite = cabinMap.get("info").toString().trim();
					
					flightData = new FlightData(this.getJobDetail(), "OW", depCode, desCode, depDate);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(1);
					flightSegment.setAircraftCode("");
					flightSegment.setAirlineCode(airlineCode);
					flightSegment.setFlightNumber(fltNo);
					flightSegment.setDepAirport(depCode);
					flightSegment.setDepTime(depTime);
					flightSegment.setArrAirport(desCode);
					flightSegment.setArrTime(desTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setCabinCount(String.valueOf(CrawlerUtil.getCabinAmount(1 == remainSite.length() ? remainSite : remainSite.substring(1))));
					flightSegmentList.add(flightSegment);
					flightData.setFromSegments(flightSegmentList);
					
					// 价格
					Object adultprice = cabinMap.get("adultprice");
					if(null == adultprice) continue ;
					List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
					FlightPrice flightPrice = new FlightPrice();
					flightPrice.setPassengerType("ADT");
					flightPrice.setFare(adultprice.toString().trim());//票面价
					flightPrice.setCurrency("CNY");//币种
					flightPrice.setEquivFare(flightPrice.getFare());
					flightPriceList.add(flightPrice);
					flightData.setAirlineCode(airlineCode);
					flightData.setPrices(flightPriceList);
					crawlResults.add(flightData);
					
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	protected boolean requestAgain(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		return httpResult.contains("获取里程出现异常");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
		Map<String, Object> mapResult = (Map<String, Object>)jsonObject;
		Object needverify = mapResult.get("needverify");
		return "true".equals(needverify);
	}

	@Override
	public String httpResult() throws Exception {
		String json = params.replaceAll("%depCode%", this.getJobDetail().getDepCode())
							.replaceAll("%desCode%", this.getJobDetail().getDesCode())
							.replaceAll("%depDate%", this.getJobDetail().getDepDate().replaceAll("-", ""));
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("json", json);
		return this.httpProxyPost(queryUrl, paramMap, "json");
	}
}