package com.aft.crawl.crawler.impl.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;

import com.aft.app.k3.FlightAvailabilityCookie;
import com.aft.app.k3.FlightAvalibilityUrl;
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
 * 捷星航空 app
 */
public class B2C3KAppCrawler extends Crawler {
	
	public B2C3KAppCrawler(String threadMark) {
		super(threadMark, "0");
	}
	
	@SuppressWarnings({ "unchecked"})
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		System.out.println(this.getJobDetail().getDepDate()+":"+httpResult);
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 返回httpResult为空:" + httpResult);
			return null;
		}
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			List<Map<String, Object>> outboundJourneys = (List<Map<String, Object>>)resultMap.get("OutboundJourneys");
			if(null == outboundJourneys) {
				logger.info(this.getJobDetail().toStr() + ", 返回错误信息:" + httpResult);
				return crawlResults;
			}
//			crawlResults = this.owFlight(crawlResults, httpResult, outboundJourneys);
			crawlResults = this.moreFlight(crawlResults, httpResult, outboundJourneys);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	/**
	 * 有单程和联程的
	 * @throws Exception 
	 * */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> moreFlight(List<CrawlResultBase> crawlResults,String httpResult,List<Map<String, Object>> outboundJourneys) throws Exception {
		CrawlResultInter b2c = null;
		for(Map<String, Object> outboundJourney : outboundJourneys) {
			String depCode = outboundJourney.get("JourneyOrigin").toString();
			String desCode = outboundJourney.get("JourneyDestination").toString();
			String depDate = outboundJourney.get("JourneyDepartureDateTime").toString().substring(0, 10);
			b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
			b2c.setRouteType("OW");
			List<Map<String, Object>> fares = (List<Map<String, Object>>)outboundJourney.get("Fares");
			if(null == fares || fares.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在 fares信息:" + outboundJourney);
				continue ;
			}
			List<Map<String, Object>> paxFares = (List<Map<String, Object>>)fares.get(0).get("PaxFares");
			Map<String, Object> paxFare = (Map<String, Object>)paxFares.get(0);
			BigDecimal ticketPrice = new BigDecimal(paxFare.get("FareAmount").toString());
			BigDecimal salePrice = new BigDecimal(paxFare.get("TaxAmount").toString());
			String currencyCode = paxFare.get("CurrencyCode").toString();
			b2c = CrawlerUtil.calPrice(b2c, ticketPrice, salePrice, currencyCode,rateMap);
			if(!"CNY".equals(b2c.getCurrency()))continue;//转换币种失败直接废弃这条数据
			
			List<Map<String, Object>> segments = (List<Map<String, Object>>)outboundJourney.get("Segments");
			List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
			for(int i=0;i<segments.size();i++){
				Map<String, Object> segment = segments.get(i);
				String airlineCode = segment.get("CarrierCode").toString().trim();
				String fltNo = airlineCode + segment.get("FlightNumber").toString().trim();
				String tripDepCode = segment.get("Origin").toString().trim();
				String tripDesCode = segment.get("Destination").toString().trim();
				String tripDepDate = segment.get("DepartureDateTime").toString().substring(0, 10);
				String tripDesDate = segment.get("ArrivalDateTime").toString().substring(0, 10);
				String tripDepTime = segment.get("DepartureDateTime").toString().substring(11,16);
				String tripDesTime = segment.get("ArrivalDateTime").toString().substring(11,16);
				CrawlResultInterTrip trip = new CrawlResultInterTrip(airlineCode,fltNo,tripDepCode,tripDesCode,tripDepDate,"Y",2,i+1,1);
				trip.setDesDate(tripDesDate);
				trip.setDepTime(tripDepTime);
				trip.setDesTime(tripDesTime);
				flightTrips.add(trip);
			}
			b2c.setFlightTrips(flightTrips);
			crawlResults.add(b2c);
		}
		return crawlResults;
	}
	/**
	 * 只有单程的
	 * */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> owFlight(List<CrawlResultBase> crawlResults,String httpResult,List<Map<String, Object>> outboundJourneys)throws Exception {
		CrawlResultB2C b2c = null;
		for(Map<String, Object> outboundJourney : outboundJourneys) {
			String depCode = outboundJourney.get("JourneyOrigin").toString();
			String desCode = outboundJourney.get("JourneyDestination").toString();
			String depDate = outboundJourney.get("JourneyDepartureDateTime").toString().substring(0, 10);
			String desDate = outboundJourney.get("JourneyArrivalDateTime").toString().substring(0, 10);
			String depTime = outboundJourney.get("JourneyDepartureDateTime").toString().substring(11,16);
			String desTime = outboundJourney.get("JourneyArrivalDateTime").toString().substring(11,16);
			List<Map<String, Object>> segments = (List<Map<String, Object>>)outboundJourney.get("Segments");
			//忽略中转的
			if(segments!=null && segments.size()>1)continue;
			Map<String, Object> segment = segments.get(0);
			String airlineCode = segment.get("CarrierCode").toString().trim();
			String fltNo = airlineCode + segment.get("FlightNumber").toString().trim();
			String shareFlight = fltNo.startsWith(airlineCode) ? "N" : "Y";
			
			List<Map<String, Object>> fares = (List<Map<String, Object>>)outboundJourney.get("Fares");
			if(null == fares || fares.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在 fares信息:" + outboundJourney);
				continue ;
			}
			List<Map<String, Object>> paxFares = (List<Map<String, Object>>)fares.get(0).get("PaxFares");
			Map<String, Object> paxFare = (Map<String, Object>)paxFares.get(0);
			
			b2c = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, "Y");
			b2c.setDepTime(depTime);
			b2c.setDesTime(desTime);
			b2c.setEndDate(desDate);
			BigDecimal ticketPrice = new BigDecimal(paxFare.get("FareAmount").toString());
			BigDecimal salePrice = new BigDecimal(paxFare.get("TaxAmount").toString());
			String currencyCode = paxFare.get("CurrencyCode").toString();
			if("CNY".equals(currencyCode)){
				b2c.setTicketPrice(ticketPrice);
				b2c.setSalePrice(salePrice);
			}else{
				BigDecimal rate = CurrencyUtil.getRequest3(currencyCode, "CNY");
				if(rate.compareTo(BigDecimal.ZERO)==0){
					rate = CurrencyUtil.getRequest3(currencyCode, "CNY");
					if(rate.compareTo(BigDecimal.ZERO)==0){
						b2c.setTicketPrice(ticketPrice);
						b2c.setSalePrice(salePrice);
						b2c.setType("此价格的币种类型："+currencyCode);
					}
				}else{
					BigDecimal cnyPrice = ticketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
					BigDecimal taxPrice = salePrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
					b2c.setTicketPrice(cnyPrice);
					b2c.setSalePrice(taxPrice);
				}
			}
			b2c.setRemainSite(2);
			crawlResults.add(b2c);
		}
		return crawlResults;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
//		System.out.println(this.getJobDetail().getDepCode()+"-"+this.getJobDetail().getDesCode()+"-"+this.getJobDetail().getDepDate()+":"+httpResult);
		if(httpResult.contains("DepartureDateTime") && !httpResult.contains("DepartureDateTime\":\""+this.getJobDetail().getDepDate())){
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	@Override
	public String httpResult() throws Exception {
		FlightAvailabilityCookie cookie = new FlightAvailabilityCookie();
		cookie.setAdultCount("3");
		cookie.setChildCount("0");
		cookie.setDestination(this.getJobDetail().getDesCode());
		cookie.setIsOneWay("true");
		cookie.setInfantCount("0");
		cookie.setOrigin(this.getJobDetail().getDepCode());
		String temp = cookie.getCookie();
		
		FlightAvalibilityUrl url =  new FlightAvalibilityUrl();
		url.setDepartureData(this.getJobDetail().getDepDate());
		url.setDestination(this.getJobDetail().getDesCode());
		url.setOrigin(this.getJobDetail().getDepCode());
		url.setCurrency(this.getJobDetail().getCurrency());
		String testUrl = url.getUrl();
		
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		Map<String,Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Host", "mobile-hybrid.jetstar.com");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("X-Jetstar-Device-Model", "SM-N9006");
		headerMap.put("X-Jetstar-OS-Type", "Android");
		headerMap.put("X-Jetstar-OS-Version", "4.4.2");
		headerMap.put("Content-Type", "application/json");
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("X-Jetstar-Device-Manufacturer", "samsung");
		headerMap.put("X-Jetstar-Network-Type", "WIFI");
		headerMap.put("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; SM-N9006 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
		headerMap.put("X-Source", "APIAndroid");
		headerMap.put("X-Jetstar-App-Version", "4.0.0");
		headerMap.put("Referer", "https://mobile-hybrid.jetstar.com/?appvi=null");
		headerMap.put("Accept-Encoding", "gzip,deflate");
		headerMap.put("Accept-Language", "zh-CN,en-US;q=0.8");
		headerMap.put("X-Requested-With", "com.ink.jetstar.mobile.app");
		headerMap.put("Cookie", temp);
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String httpResult = this.httpProxyGet(httpClientSessionVo,testUrl ,"json");
		return httpResult;
	}
}