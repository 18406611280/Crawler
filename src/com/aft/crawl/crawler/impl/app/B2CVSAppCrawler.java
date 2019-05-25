package com.aft.crawl.crawler.impl.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;


/**
 * 维珍航空 app  
 */
public class B2CVSAppCrawler extends Crawler {
	
	private final static String queryUrl = "https://m.virginatlantic.com/mwsb/service/itinerarySearch";
	private final static String queryContent = "{\"slices\":[{\"date\":\"%depDate%\",\"origin\":\"%depCode%\",\"destination\":\"%desCode%\",\"sort\":\"customScore\"}],\"constraints\":[],\"version\":\"2\",\"type\":\"ONE_WAY\",\"pax\":\"1\",\"shopType\":\"Revenue\",\"credentials\":{\"cookies\":[]}}";
	
	public B2CVSAppCrawler(String threadMark) {
		super(threadMark, "0");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 返回httpResult为空:" + httpResult);
			return null;
		}
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			List<Map<String, Object>> itineraries = (List<Map<String, Object>>)resultMap.get("itineraries");
			if(itineraries==null || itineraries.isEmpty()){
				logger.info("没有航班信息");
				return crawlResults;
			}
			
			Map<String, Object> origin = (Map<String, Object>)resultMap.get("origin");
			Map<String, Object> destination = (Map<String, Object>)resultMap.get("destination");
			String depCode = origin.get("code").toString();
			String desCode = destination.get("code").toString();
			String depDate = resultMap.get("startDate").toString();
			for(Map<String, Object> itinerarie:itineraries){
				
				Map<String, Object> cabins = (Map<String, Object>)itinerarie.get("cabins");
				for(String key : cabins.keySet()){
					CrawlResultInter b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
					b2c.setRouteType("OW");
					Map<String, Object> rev = (Map<String, Object>)cabins.get(key);
					Map<String, Object> fare = (Map<String, Object>)rev.get("fare");
					String currencySymbol = fare.get("currencySymbol").toString();
					String cabinCode = fare.get("cabinCode").toString();
					String currencyCode = fare.get("currencyCode").toString();
					String base = fare.get("base").toString().replaceAll(",", "");
					String tax = fare.get("tax").toString().replaceAll(",", "");
					base = base.substring(currencySymbol.length());
					tax = tax.substring(currencySymbol.length());
					b2c = CrawlerUtil.calPrice(b2c, new BigDecimal(base), new BigDecimal(tax), currencyCode,rateMap);
					if(!"CNY".equals(b2c.getCurrency()))continue;//转换币种失败直接废弃这条数据
					
					Map<String, Object> slice = (Map<String, Object>)itinerarie.get("slice");
					List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
					List<Map<String, Object>> flights = (List<Map<String, Object>>)slice.get("flights");
					for(int i=0;i<flights.size();i++){
						Map<String, Object> flight = flights.get(i);
						String tripDepDate = flight.get("departDate").toString();
						String tripDepTime = flight.get("departTime").toString();
						String tripDesDate = flight.get("arriveDate").toString();
						String tripDesTime = flight.get("arriveTime").toString();
						Map<String, Object> tripOrigin = (Map<String, Object>)flight.get("origin");
						Map<String, Object> tripDestination = (Map<String, Object>)flight.get("destination");
						Map<String, Object> marketAirline = (Map<String, Object>)flight.get("marketAirline");
						String tripDepCode = tripOrigin.get("code").toString();
						String tripDesCode = tripDestination.get("code").toString();
						String airlineCode = marketAirline.get("code").toString();
						String fltNo = airlineCode + flight.get("flightNumber").toString();
						CrawlResultInterTrip t = new CrawlResultInterTrip(airlineCode,fltNo,tripDepCode,tripDesCode,tripDepDate,cabinCode,i+1,1);
						t.setDesDate(tripDesDate);
						t.setDepTime(tripDepTime);
						t.setDesTime(tripDesTime);
						t.setRemainSite(2);
						flightTrips.add(t);
					}
					if(flightTrips.size()>0){
						b2c.setFlightTrips(flightTrips);
						crawlResults.add(b2c);
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
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document.title().contains("404 Not Found")
				|| document.title().contains("Error")
				|| document.title().contains("error")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	@Override
	public String httpResult() throws Exception {
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		Map<String,Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Host", "m.virginatlantic.com");
		headerMap.put("Accept", "text/plain, application/json, */*");
		headerMap.put("Content-Type", "application/vnd.delta.itinerarycriteria+json");
		headerMap.put("response-json", "true");
		headerMap.put("Accept-Encoding", "gzip");
		headerMap.put("User-Agent", "Virgin Atlantic Android, Android 4.4.2, 3.2, Phone");
		headerMap.put("Tlaosloc", "gn=bookFlight:_search&ch=booking");
		headerMap.put("Tlaoscnx", "Wi-Fi");
		headerMap.put("Tlaosid", "Android 4.4.2, samsung SCH-I919U, 3.2");
		headerMap.put("Connection", "Keep-Alive");

		httpClientSessionVo.setHeaderMap(headerMap);
	
		String httpContent = queryContent.replace("%depDate%", this.getJobDetail().getDepDate())
				.replace("%depCode%", this.getJobDetail().getDepCode())
				.replace("%desCode%", this.getJobDetail().getDesCode());
		
		String httpResult = this.httpProxyPost(httpClientSessionVo, queryUrl, httpContent, "json");
//		System.out.println(httpResult);
		return httpResult;
	}
	public static void main(String[] args) {
		String str = "HK$12590.00";
		str = str.replaceAll("$", "");
		System.out.println(str);
	}
}