package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 天津航空 
 */
public class B2CGSCrawler extends Crawler {

	public final static String ValidateAirFlightSearchForm_url = "http://www.tianjin-air.com/flight/ValidateAirFlightSearchForm";
	public final static String AirLowFareSearch_url = "http://www.tianjin-air.com/flight/AirLowFareSearch";
	public final static String getPriceTrend_url = "http://www.tianjin-air.com/flight/getPriceTrend";
	public final static String AirFareFamiliesForward_url = "http://www.tianjin-air.com/flight/AirFareFamiliesForward";
	
	public B2CGSCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			if(httpResult==null || "".equals(httpResult)){
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Map<String, Object> odGroupsVo = (Map<String, Object>)mapResult.get("odGroupsVo");
			
			if(null == odGroupsVo || odGroupsVo.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			List<Map<String, Object>> helpList = (List<Map<String, Object>>)odGroupsVo.get("odgroupVoList");
			
			if(null == helpList || helpList.isEmpty() || helpList.size()==0) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			
			
			Map<String, Object> odgroupVo = helpList.get(0);
			
			List<Map<String, Object>> flightGroupVoList = (List<Map<String, Object>>)odgroupVo.get("flightGroupVoList");
			
			
			CrawlResultB2C crawlResult = null;
			for(Map<String, Object> flightMap : flightGroupVoList) {	
				
				Map<String, Object> flightVo = (Map<String, Object>)flightMap.get("flightVo");
				
				List<Map<String, Object>> flightSegmentVoList = (List<Map<String, Object>>)flightVo.get("flightSegmentVoList");
				//只要 单程的
				if(flightSegmentVoList==null || flightSegmentVoList.isEmpty() || flightSegmentVoList.size()>1)continue;
				Map<String, Object> flightSegmentVo = (Map<String, Object>)flightSegmentVoList.get(0);
				String departureTime = flightSegmentVo.get("departureTime").toString();
				String depDate = departureTime.substring(0, 10);
				String depTime = departureTime.substring(11,16);
				String arrivalTime = flightSegmentVo.get("arrivalTime").toString();
				String desDate = arrivalTime.substring(0, 10);
				String desTime = arrivalTime.substring(11,16);
				String depCode = flightSegmentVo.get("departureAirportLocationCode").toString();
				String desCode = flightSegmentVo.get("arrivalAirportLocationCode").toString();
				// 航班号, 航司
				String airlineCode = flightSegmentVo.get("marketingAirlineCode").toString();
				String fltNo = airlineCode+flightSegmentVo.get("flightNumber").toString();
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				Map<String, Object> flightItineraryVoMap = (Map<String, Object>)flightMap.get("flightItineraryVoMap");
				
				
				
				for(String productKey : flightItineraryVoMap.keySet()) {
					
					Map<String, Object> productMap = (Map<String, Object>)flightItineraryVoMap.get(productKey);
					
					String productType = productMap.get("fareFamilyName").toString();
					
					List<Map<String, Object>> bookingClassVos = (List<Map<String, Object>>)productMap.get("bookingClassVos");					
					Map<String, Object> bookingClassVo = bookingClassVos.get(0);
					String cabin = bookingClassVo.get("desigCode").toString();
					String remainSite = "10";String.valueOf(bookingClassVo.get("desigQuantity"));
					if(remainSite==null || "".equals(remainSite) || "null".equals(remainSite))remainSite="10";
					
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					crawlResult.setDepTime(depTime);
					crawlResult.setDesTime(desTime);
					crawlResult.setType(productType);
					
					// 价格
					crawlResult.setTicketPrice(new BigDecimal(productMap.get("amount").toString().trim()).setScale(0));
					crawlResult.setSalePrice(crawlResult.getTicketPrice());
					// 剩余座位数
					crawlResult.setRemainSite(Integer.valueOf(remainSite));	// 找不到剩余座位数, 默认都10个
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
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType)
			throws Exception {
		if(httpResult!=null && httpResult.contains("session超时"))return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	@Override
	public String httpResult() throws Exception {
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Host", "www.tianjin-air.com");
		headerMap.put("Referer", "http://www.tianjin-air.com/");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("cabinClass", "Economy");
		paramMap.put("guestTypes[0].amount", "1");
		paramMap.put("guestTypes[0].type", "ADT");
		paramMap.put("guestTypes[1].amount", "0");
		paramMap.put("guestTypes[1].type", "CNN");
		paramMap.put("guestTypes[2].amount", "0");
		paramMap.put("guestTypes[2].type", "INF");
		paramMap.put("inboundOption.departureDate", "");
		paramMap.put("inboundOption.destinationLocationCode", "");
		paramMap.put("inboundOption.originLocationCode", "");
		paramMap.put("outboundOption.departureDate", this.getJobDetail().getDepDate());
		paramMap.put("outboundOption.destinationLocationCode", this.getJobDetail().getDesCode());
		paramMap.put("outboundOption.originLocationCode", this.getJobDetail().getDepCode());
		paramMap.put("tripType", "OW");
		paramMap.put("flexibleSearch", "false");
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo,ValidateAirFlightSearchForm_url,paramMap, "json");
		
		String cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
		headerMap.put("Cookie",cookies);
		
		
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, AirLowFareSearch_url, paramMap, "other");
		
		
		headerMap.put("Referer", "http://www.tianjin-air.com/flight/AirFareFamiliesFlexibleForward");
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo, getPriceTrend_url,"json");
		
		
		
		
		String result = this.httpProxyPost(httpClientSessionVo,AirFareFamiliesForward_url,"", "json");
		
		return result;
	}
}