package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 台湾虎航Web官网
 * @author chenminghong
 */
public class B2CITCrawler extends Crawler {
															
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	private final static String filghtUrl = "https://tiger-wkgk.matchbyte.net/wkapi/v1.0/flightsearch?adults=1&children=0&infants=0&originStation=%depCode%&destinationStation=%desCode%&departureDate=%depDate%&includeoverbooking=false&daysBeforeAndAfter=4&locale=zh-TW";
	

	
	public B2CITCrawler(String threadMark) {
		super(threadMark);
	}
	
	@SuppressWarnings({"unchecked" })
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		
		String httpResult =this.httpResult();
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			if(resultMap==null || resultMap.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
//			crawlResults = this.owFlight(crawlResults,httpResult,resultMap);
			
			crawlResults = this.moreFlight(crawlResults,httpResult,resultMap);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}
	
	/**
	 * 带有中转航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private List<CrawlResultBase> moreFlight(List<CrawlResultBase> crawlResults,String httpResult,Map<String, Object> resultMap) {
		try {
			List<Map<String, Object>> results = (List<Map<String, Object>>)resultMap.get("journeyDateMarkets");
			if(results==null || results.isEmpty() || results.size()==0){
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			String currencyCode = resultMap.get("currencyCode").toString();
			Map<String, Object> journeyDateMarket = (Map<String, Object>)results.get(0);
			List<Map<String, Object>> journeys = (List<Map<String, Object>>)journeyDateMarket.get("journeys");
			
			for(Map<String, Object> journey : journeys){
				Map<String, Object> originStation =  (Map<String, Object>)journey.get("originStation");
				Map<String, Object> destinationStation =  (Map<String, Object>)journey.get("destinationStation");
				
				String depCode = originStation.get("airportCode").toString();
				String desCode = destinationStation.get("airportCode").toString();
				String depDate = journey.get("departDateTime").toString().substring(0, 10);
				CrawlResultInter b2c =  null;
				
				List<Map<String, Object>> fares = (List<Map<String, Object>>)journey.get("fares");
				
				Map<String,List<Map<String, Object>>> cabinFares = new HashMap<String,List<Map<String, Object>>>();
				
				for(Map<String, Object> fare : fares){
					String cabin = fare.get("productClass").toString();
					List<Map<String, Object>> list = cabinFares.get(cabin);
					if(list==null){
						list = new ArrayList<Map<String, Object>>();
						list.add(fare);
					}else{
						list.add(fare);
					}
					cabinFares.put(cabin, list);
				}
				
				List<Map<String, Object>> segments = (List<Map<String, Object>>)journey.get("segments");
				for(String key : cabinFares.keySet()){
					List<Map<String, Object>> cflist = cabinFares.get(key);
					
					String cabin = key;
					String remainSite = "";
					BigDecimal taxPrice = new BigDecimal(0);
					BigDecimal ticketPrice = new BigDecimal(0);
					
					for(Map<String, Object> cf : cflist){
						remainSite = cf.get("seatsAvailability").toString();
						String tax = cf.get("tax").toString();
						String price = cf.get("price").toString();
						taxPrice = taxPrice.add(new BigDecimal(tax));
						ticketPrice = ticketPrice.add(new BigDecimal(price).subtract(new BigDecimal(tax)));
					}
					b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
					b2c.setRouteType("OW");
					List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
					for(int sg =0;sg<segments.size();sg++){
						Map<String, Object> segment = segments.get(sg);
						Map<String, Object> departureStation =  (Map<String, Object>)segment.get("departureStation");
						Map<String, Object> arrivalStation =  (Map<String, Object>)segment.get("arrivalStation");

						String tripDepCode = departureStation.get("airportCode").toString();
						String tripDesCode = arrivalStation.get("airportCode").toString();
						String airlineCode = segment.get("carrierCode").toString();
						String fltNo = airlineCode + segment.get("flightNumber").toString();
						String std = segment.get("departDateTime").toString();
						String sta = segment.get("arriveDateTime").toString();
						String tripDepDate = std.substring(0, 10);
						String tripDepTime = std.substring(11, 16);
						String tripDesDate = sta.substring(0, 10);
						String tripDesTime = sta.substring(11, 16);

						CrawlResultInterTrip trip = new CrawlResultInterTrip(airlineCode,fltNo,tripDepCode,tripDesCode,tripDepDate,cabin,sg+1,1);
						trip.setDesDate(tripDesDate);
						trip.setDepTime(tripDepTime);
						trip.setDesTime(tripDesTime);
						trip.setRemainSite(Integer.valueOf(remainSite));
						flightTrips.add(trip);
					}
					if(flightTrips.size()>0){
						b2c.setFlightTrips(flightTrips);
						b2c = CrawlerUtil.calPrice(b2c, ticketPrice, taxPrice,currencyCode,rateMap);
						if("CNY".equals(b2c.getCurrency())){
							crawlResults.add(b2c);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
		}
		return crawlResults;
	}
	/**
	 * 直达航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	private List<CrawlResultBase> owFlight(List<CrawlResultBase> crawlResults,String httpResult,Map<String, Object> resultMap) {
		try {
			List<Map<String, Object>> results = (List<Map<String, Object>>)resultMap.get("Result");
			if(results==null || results.isEmpty() || results.size()==0){
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			for(Map<String, Object> result : results){
				Map<String, Object> fareListOfDay =  (Map<String, Object>)result.get("FareListOfDay");
				Set<String> keySet = fareListOfDay.keySet();
				
				
				CrawlResultB2C b2c =  null;
				for(String key : keySet){
					Map<String, Object> flightInfoList = (Map<String, Object>)fareListOfDay.get(key);
					List<List<Map<String, Object>>> oAndDFares = (List<List<Map<String, Object>>>) flightInfoList.get("OAndDFares");
					if(oAndDFares==null || oAndDFares.isEmpty())continue;
					for(List<Map<String, Object>> oAndDFare : oAndDFares){
						//去掉联程
						if(oAndDFare==null || oAndDFare.isEmpty() || oAndDFare.size()>1)continue;
						
						Map<String, Object> flightInfo = (Map<String, Object>)oAndDFare.get(0);
						
						List<Map<String, Object>> fares = (List<Map<String, Object>>)flightInfo.get("Fares");
						Integer remainSite = 0;
						BigDecimal ticketPrice = new BigDecimal(0);
						BigDecimal salePrice = new BigDecimal(0);
						Map<String,Object> fareca = new HashMap<String, Object>();
						Map<String,Object> faresi = new HashMap<String, Object>();
						Map<String,Object> farein = new HashMap<String, Object>();
						for(Map<String, Object> fare : fares){
							String fareID = fare.get("FareID").toString();
							if(fareID.contains("INCLUSIVE")){
								farein = fare;
							}
							if(fareID.contains("SIMPLE")){
								faresi = fare;
							}
							if(fareID.contains("CAMPAIGN")){
								fareca = fare;
							}
						}
						if(fareca!=null && !fareca.isEmpty()){
							int seats =  Integer.valueOf(fareca.get("Seats").toString());
							if(seats>=3){
								remainSite = seats;
								String[] fareIds = fareca.get("FareID").toString().split("#");
								ticketPrice = new BigDecimal(fareIds[2]);
								salePrice = new BigDecimal(fareIds[3]);
							}
						}
						if(remainSite.intValue()==0){
							if(faresi!=null && !faresi.isEmpty()){
								int seats =  Integer.valueOf(faresi.get("Seats").toString());
								if(seats>=3){
									remainSite = seats;
									String[] fareIds = faresi.get("FareID").toString().split("#");
									ticketPrice = new BigDecimal(fareIds[2]);
									salePrice = new BigDecimal(fareIds[3]);
								}
							}
						}
						if(remainSite.intValue()==0){
							if(farein!=null && !farein.isEmpty()){
								int seats =  Integer.valueOf(farein.get("Seats").toString());
								if(seats>=3){
									remainSite = seats;
									String[] fareIds = farein.get("FareID").toString().split("#");
									ticketPrice = new BigDecimal(fareIds[2]);
									salePrice = new BigDecimal(fareIds[3]);
								}
							}
						}
						if(remainSite.intValue()==0) continue;
						
						String depCode = flightInfo.get("BoardPoint").toString();
						String desCode = flightInfo.get("OffPoint").toString();
						String airlineCode = flightInfo.get("AirlineCode").toString();
						String fltNo = airlineCode+flightInfo.get("FltNumber").toString();
						String std = flightInfo.get("Std").toString();
						String sta = flightInfo.get("Sta").toString();
						
						String[] stds = std.split("\\+");
						std = stds[0];
						stds = std.split("T");
						String depDate = stds[0];
						String depTime = stds[1].substring(0, 5);
						String[] stas = sta.split("\\+");
						sta = stas[0];
						stas = sta.split("T");
						String desDate = stas[0];
						String desTime = stas[1].substring(0, 5);
						
						
						b2c = new CrawlResultB2C(this.getJobDetail(),airlineCode, fltNo, "N", depCode,desCode,depDate, "Y");
						b2c.setDepTime(depTime);
						b2c.setDesTime(desTime);
						b2c.setEndDate(desDate);
						b2c.setRemainSite(remainSite);
						
						BigDecimal rate = CurrencyUtil.getRequest3("HKD", "CNY");
						if(rate.compareTo(BigDecimal.ZERO)==0){
							b2c.setType("此价格的币种类型：HKD");
						}else{
							ticketPrice = ticketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
							salePrice = salePrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
						}

						b2c.setTicketPrice(ticketPrice);
						b2c.setSalePrice(salePrice);

						crawlResults.add(b2c);
					}
				}
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
		}
		return crawlResults;
	}
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document.title().contains("404 Page Not Found") || document.title().contains("403 - Forbidden: Access is denied")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	@Override
	public String httpResult() throws Exception {
		
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);

		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		
		headerMap.put("Host", "booking.tigerairtw.com");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String result = this.httpProxyGet(httpClientSessionVo, "https://booking.tigerairtw.com/?lang=zh-TW", "html");
		headerMap.put("Host", "tiger-wkgk.matchbyte.net");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("Accept-Language", "zh-TW");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Referer", "https://booking.tigerairtw.com/?lang=zh-TW");
		headerMap.put("Origin", "https://booking.tigerairtw.com");
		headerMap.put("Connection", "keep-alive");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String url = filghtUrl.replace("%depCode%", this.getJobDetail().getDepCode())
				.replace("%desCode%", this.getJobDetail().getDesCode())
				.replace("%depDate%",this.getJobDetail().getDepDate());
		
	    result = this.httpProxyGet(httpClientSessionVo, url, "json");
		
		System.out.println(result);
		
		return result;
	}
	

	public static void main(String[] args) {
		System.out.println(new Date().getTime());
	}

}