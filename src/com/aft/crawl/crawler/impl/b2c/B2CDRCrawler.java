package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 瑞丽航空 
 */
public class B2CDRCrawler extends Crawler {

	private final static String queryUrl = "http://www.rlair.net/data.ashx/FlightServiceV2/QuerySeat.json";
	
	private final static String airportUrl = "http://www.rlair.net/data.ashx/BasicInfoService/Airports.json?bust=v9.3";
	
	private static String ipc = "";
	
	public B2CDRCrawler(String threadMark) {
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
			Object objMsg = mapResult.get("success");
			if(null == objMsg || !(Boolean)objMsg) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
			Map<String, Object> result = (Map<String, Object>)mapResult.get("result");
			List<Map<String, Object>> flights = (List<Map<String, Object>>)result.get("FlightList");
			for(Map<String, Object> flightMap : flights) {
				// 出发,到达
				String depCode = flightMap.get("DeparturePort").toString().trim();
				String desCode = flightMap.get("DestinationPort").toString().trim();
				
				Map<String, Object> segmentListMap = ((List<Map<String, Object>>)flightMap.get("SegmentList")).get(0);
				// 航班号,航司
				String fltNo = segmentListMap.get("FlightNo").toString().trim().toUpperCase();
				String airlineCode = segmentListMap.get("Airline").toString().trim().toUpperCase();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				Object CodeShare = segmentListMap.get("CodeShare");
				if(null != CodeShare && Boolean.valueOf(CodeShare.toString())) shareFlight = "Y";
				
				// 出发日期
				String departureTime = segmentListMap.get("DepartureTime").toString().trim();
				String arriveTime = segmentListMap.get("ArriveTime").toString().trim();
				String depDate = departureTime.substring(0, 10);
				String depTime = departureTime.substring(11, 16);
				String desTime = arriveTime.substring(11, 16);
				
				List<Map<String, Object>> productListMap = (List<Map<String, Object>>)segmentListMap.get("ProductList");
				for(Map<String, Object> productMap : productListMap) {
					List<Map<String, Object>> fareClassListMap = (List<Map<String, Object>>)productMap.get("FareClassList");
					for(Map<String, Object> fareClassMap : fareClassListMap) {
						String cabin = fareClassMap.get("MainCode").toString().trim();
						
						// 价格
						Map<String, Object> adultFare = (Map<String, Object>)fareClassMap.get("AdultFare");
						BigDecimal ticketPrice = new BigDecimal(adultFare.get("Price").toString()).setScale(0);
						
						crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
						crawlResult.setDepTime(depTime);	// 出发时间
						crawlResult.setDesTime(desTime);	// 到达时间
						
						crawlResult.setRemainSite(Integer.parseInt(fareClassMap.get("Available").toString().trim()));
						crawlResult.setTicketPrice(ticketPrice);
						crawlResult.setSalePrice(ticketPrice);
						crawlResults.add(crawlResult);
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
		this.setHeaderMap();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("roundTrip", "false");
		paramMap.put("orgCity", this.getJobDetail().getDepCode());
		paramMap.put("dstCity", this.getJobDetail().getDesCode());
		paramMap.put("flightDate", this.getJobDetail().getDepDate());
		String flightInfo = paramMap.get("orgCity").toString() + paramMap.get("dstCity") + paramMap.get("flightDate");
		String inParam = "aEfew#2_9-___HhsTwW###*Vvv"
			+ Base64.encodeBase64String(("a22~@$$$94723$$@__3cA3Fe6578AOOO31H324^$##$%#098OO00" + flightInfo + ipc).getBytes())
			+ "a987#$%_323dd3";
		paramMap.put("inParam", inParam);
		return this.httpProxyPost(queryUrl, paramMap, "json");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
		Map<String, Object> mapResult = (Map<String, Object>)jsonObject;
		if((Boolean)mapResult.get("success")) return false;
		
		String message = (String)mapResult.get("message");
		if(null != message && message.contains("限制访问")) return true;
		return false;
	}
	
	/**
	 * 设置cookie sessionid, ipc
	 */
	private void setHeaderMap() {
		if(StringUtils.isNotEmpty(ipc)) return ;
		synchronized(ipc) {
			if(StringUtils.isNotEmpty(ipc)) return ;
			while(true) {
				try {
					if(this.isTimeout()) return ;
					MyHttpClientResultVo httpVo = MyHttpClientUtil.httpClientGet(airportUrl);
					String aspSession = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId=");
					ipc = aspSession.split("=")[1];
					headerMap.put("Cookie", aspSession + ";ipc=" + ipc);
					break ;
				} catch(Exception e) {
					logger.error(this.getJobDetail().toStr() + ", 获取[sessionid, ipc]异常\r", e);
					MyThreadUtils.sleep(sleepTime);
				}
			}
		}
	}
}