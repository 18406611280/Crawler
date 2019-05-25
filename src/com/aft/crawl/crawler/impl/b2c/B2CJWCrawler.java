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
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 香草航空Web官网
 */
public class B2CJWCrawler extends Crawler {
															
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	private final static String initCurrency = "NRT,KIX,OKA,ASJ,CTS,HKD-JPY;HKG-HKD;TPE,KHH-TWD;SGN-VND;CEB-PHP;PVG-CNY";
	
	private final static String filghtUrl = "https://www.vanilla-air.com/api/booking/flight-fare/list.json?__ts=%ts%&adultCount=1&childCount=0&couponCode=&currency=HKD&destination=%desCode%&infantCount=0&isMultiFlight=true&origin=%depCode%&searchCurrency=%currency%&targetMonth=%depDate%&version=1.0";
	
	private String currency = "HKD";
	
	public B2CJWCrawler(String threadMark) {
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
			List<Map<String, Object>> results = (List<Map<String, Object>>)resultMap.get("Result");
			if(results==null || results.isEmpty() || results.size()==0){
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			for(Map<String, Object> result : results){
				Map<String, Object> fareListOfDay =  (Map<String, Object>)result.get("FareListOfDay");
				Set<String> keySet = fareListOfDay.keySet();
				String depCode = result.get("BoardPoint").toString();
				String desCode = result.get("OffPoint").toString();
				CrawlResultInter b2c =  null;
				for(String key : keySet){
					Map<String, Object> flightInfoList = (Map<String, Object>)fareListOfDay.get(key);
					List<List<Map<String, Object>>> oAndDFares = (List<List<Map<String, Object>>>) flightInfoList.get("OAndDFares");
					if(oAndDFares==null || oAndDFares.isEmpty())continue;
					for(List<Map<String, Object>> oAndDFare : oAndDFares){
						
						String depDate = flightInfoList.get("FlightDate").toString();
						b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
						b2c.setRouteType("OW");
						List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
						Integer remainSite = 0;
						BigDecimal ticketPrice = new BigDecimal(0);
						BigDecimal salePrice = new BigDecimal(0);
						for(int fi = 0;fi<oAndDFare.size();fi++){
							Map<String, Object> flightInfo = oAndDFare.get(fi);
							List<Map<String, Object>> fares = (List<Map<String, Object>>)flightInfo.get("Fares");
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
								if(fi==0){
									int seats =  Integer.valueOf(fareca.get("Seats").toString());
									remainSite = seats;
								}
								String[] fareIds = fareca.get("FareID").toString().split("#");
								ticketPrice = ticketPrice.add(new BigDecimal(fareIds[2]));
								salePrice = salePrice.add(new BigDecimal(fareIds[3])).add(new BigDecimal(40));
							}
							if(remainSite.intValue()==0){
								if(faresi!=null && !faresi.isEmpty()){
									if(fi==0){
										int seats =  Integer.valueOf(faresi.get("Seats").toString());
										remainSite = seats;
									}
									String[] fareIds = faresi.get("FareID").toString().split("#");
									ticketPrice = ticketPrice.add(new BigDecimal(fareIds[2]));
									salePrice = salePrice.add(new BigDecimal(fareIds[3])).add(new BigDecimal(40));
								}
							}
							if(remainSite.intValue()==0){
								if(farein!=null && !farein.isEmpty()){
									if(fi==0){
										int seats =  Integer.valueOf(farein.get("Seats").toString());
										remainSite = seats;
									}
									String[] fareIds = farein.get("FareID").toString().split("#");
									ticketPrice = ticketPrice.add(new BigDecimal(fareIds[2]));
									salePrice = salePrice.add(new BigDecimal(fareIds[3])).add(new BigDecimal(40));
								}
							}
							if(remainSite.intValue()==0) break;
							
							
							String tripDepCode = flightInfo.get("BoardPoint").toString();
							String tripDesCode = flightInfo.get("OffPoint").toString();
							String airlineCode = flightInfo.get("AirlineCode").toString();
							String fltNo = airlineCode+flightInfo.get("FltNumber").toString();
							String std = flightInfo.get("Std").toString();
							String sta = flightInfo.get("Sta").toString();
							
							String[] stds = std.split("\\+");
							std = stds[0];
							stds = std.split("T");
							String tripDepDate = stds[0];
							String tripDepTime = stds[1].substring(0, 5);
							String[] stas = sta.split("\\+");
							sta = stas[0];
							stas = sta.split("T");
							String tripDesDate = stas[0];
							String tripDesTime = stas[1].substring(0, 5);
							
							CrawlResultInterTrip trip = new CrawlResultInterTrip(airlineCode,fltNo,tripDepCode,tripDesCode,tripDepDate,"Y",fi+1,1);
							trip.setDesDate(tripDesDate);
							trip.setDepTime(tripDepTime);
							trip.setDesTime(tripDesTime);
							trip.setRemainSite(Integer.valueOf(remainSite));
							flightTrips.add(trip);
						}
						if(flightTrips.size()>0){
							b2c.setFlightTrips(flightTrips);
							b2c = CrawlerUtil.calPrice(b2c, ticketPrice, salePrice,"HKD",rateMap);
							if("CNY".equals(b2c.getCurrency())){
								crawlResults.add(b2c);
							}
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
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("Accept-Encoding", "gzip, deflate, sdch, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.vanilla-air.com");
		headerMap.put("Referer", "https://www.vanilla-air.com/hk/");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
		headerMap.put("X-Requested-With", "XMLHttpRequest");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, "https://www.vanilla-air.com/common/data/lowestfarePos.json", "json");
		
		String __VnlUserTrackId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","__VnlUserTrackId");
		if(__VnlUserTrackId==null) return null;
		
		
		
		StringBuilder cookie = new StringBuilder().append(__VnlUserTrackId);
		
		//把首次请求返回的cookies放到header里面进行二次请求
		headerMap.put("Cookie",cookie);
		headerMap.put("Referer", "https://www.vanilla-air.com/hk/booking/");
		headerMap.put("channel", "pc");
		headerMap.remove("X-Requested-With");
		
		String[] currencys = initCurrency.split(";");
		for(String cy : currencys){
			String[] c  = cy.split("-");
			if(c[0].contains(this.getJobDetail().getDepCode())){
				currency = c[1];
				break;
			}
		}
		
		String url = filghtUrl.replace("%depCode%", this.getJobDetail().getDepCode())
				.replace("%desCode%", this.getJobDetail().getDesCode())
				.replace("%ts%",String.valueOf(new Date().getTime()))
				.replace("%currency%",currency)
				.replace("%depDate%",this.getJobDetail().getDepDate().substring(0,7).replace("-", ""));
		
		String result = this.httpProxyGet(httpClientSessionVo, url, "json");
		
//		System.out.println(result);
		
		return result;
	}
	

	public static void main(String[] args) {
		System.out.println(new Date().getTime());
	}

}