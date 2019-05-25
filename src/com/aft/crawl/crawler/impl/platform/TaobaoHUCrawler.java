package com.aft.crawl.crawler.impl.platform;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 阿里旅行 海航旗舰店
 */
public class TaobaoHUCrawler extends Crawler {
	
	private final static String queryUrl = "https://sjipiao.alitrip.com/searchow/search.htm?depCityName=%depName%&arrCityName=%desName%&depDate=%depDate%&tripType=0&agentIds=1046&_input_charset=utf-8&ua=%ua%";
	
	private final static String gaoduanQueryUrl = "https://sjipiao.alitrip.com/searchow/search.htm?_ksTS=%ksTS%_42&callback=jsonp43&type=gaoduan&&depCityName=%depCityName%&arrCityName=%arrCityName%&depDate=%depDate%&tripType=0&agentIds=1046&supplyItinerary=&autoBook=&cabinClass=&searchSource=2&searchBy=et_shop&passengerNum=&_input_charset=utf-8&flightNo=%flightNo%&ua=";
	
	private final static Pattern pattern = Pattern.compile("\\(.*(\\{.*\\})\\)", Pattern.DOTALL);
	
	public TaobaoHUCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			httpResult = TaobaoHUCrawler.patternHttpResult(httpResult);
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object objMsg = mapResult.get("error");
			if(null == objMsg || "-1".equals(objMsg.toString())) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			CrawlResultB2C crawlResult = null;
			Map<String, Object> dateFlightMap = (Map<String, Object>)mapResult.get("data");
			try {
				List<Map<String, Object>> flights = (List<Map<String, Object>>)dateFlightMap.get("flight");
				for(Map<String, Object> flightMap : flights) {
					crawlResult = this.getCrawlResults(flightMap);
					if(crawlResult == null) continue;
					crawlResults.add(crawlResult);
				}
			} catch (Exception e) {
				Map<String, Object> flightMap = (Map<String, Object>)dateFlightMap.get("flight");
				crawlResult = this.getCrawlResults(flightMap);
				if(crawlResult != null) {
					crawlResults.add(crawlResult);
				}
			}
			
			// 获取更多舱位
//			if(!crawlResults.isEmpty()) crawlResults = this.crawl(crawlResults);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<CrawlResultBase> crawl(List<CrawlResultBase> crawlResults) throws Exception {
		List<CrawlResultBase> gcrawlResults = new ArrayList<CrawlResultBase>();
		String httpResult = "";
		try {
			for(CrawlResultBase craw : crawlResults) {
				CrawlResultB2C b2c = (CrawlResultB2C)craw;
				httpResult = this.httpResult(b2c.getFltNo());
				if(this.isTimeout()) continue;
				
				httpResult = TaobaoHUCrawler.patternHttpResult(httpResult);
				Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
				Object objMsg = mapResult.get("error");
				if(null == objMsg || "-1".equals(objMsg.toString())) {
					logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
					continue;
				}

				CrawlResultB2C crawlResult = null;
				Map<String, Object> dateFlightMap = (Map<String, Object>)mapResult.get("data");
				Map<String, Object> flights = (Map<String, Object>)dateFlightMap.get("flight");

				CrawlerType crawlerType = CrawlerType.getCrawlerType(this.getJobDetail().getPageType());

				// 航班号,航司
				String fltNo = flights.get("flightNo").toString().trim().toUpperCase();
				String airlineCode = flights.get("airlineCode").toString().trim().toUpperCase();

				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);

				// 出发,到达
				String depCode = flights.get("depAirport").toString().trim().toUpperCase();
				String desCode = flights.get("arrAirport").toString().trim().toUpperCase();

				// 出发日期
				String startTime = flights.get("depTime").toString().trim();
				String depDate = startTime.substring(0,10);
				
				// 出发时间
				String depTime = startTime.substring(11, 16);
				
				// 到达日期
				String endTime = flights.get("arrTime").toString().trim();

				// 到达时间
				String desTime = endTime.substring(11,16);

				List<Map<String, Object>> cabins = (List<Map<String, Object>>)dateFlightMap.get("cabin");
				for(Map<String, Object> cabinMap:cabins){
					String cabin  = cabinMap.get("cabin").toString().trim().toUpperCase();
					if(!this.allowCabin(cabin)) continue ;	// 排除舱位

					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间

					// 剩余座位数
					String remainSite = String.valueOf(cabinMap.get("cabinNum"));
					if(remainSite==null||"".equals(remainSite)||"null".equals(remainSite))remainSite="A";
					crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(1 == remainSite.length() ? remainSite : remainSite.substring(1)));
					if(crawlResult.getRemainSite() <= 0) continue ;

					// 价格
					crawlResult.setTicketPrice(new BigDecimal(cabinMap.get("originPrice").toString().trim()).setScale(0));
					crawlResult.setSalePrice(crawlResult.getTicketPrice());
					gcrawlResults.add(crawlResult);
				}
			}
			crawlResults.addAll(gcrawlResults);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@SuppressWarnings("unchecked")
	public CrawlResultB2C getCrawlResults(Map<String, Object> flightMap) throws Exception{
		if(flightMap == null || flightMap.isEmpty()) return null;
		// 航班号,航司
		String fltNo = flightMap.get("flightNo").toString().trim().toUpperCase();
		String airlineCode = flightMap.get("airlineCode").toString().trim().toUpperCase();
		
		// 判断共享
		String shareFlight = this.getShareFlight(airlineCode);
		
		// 出发,到达
		String depCode = flightMap.get("depAirport").toString().trim().toUpperCase();
		String desCode = flightMap.get("arrAirport").toString().trim().toUpperCase();
		
		// 出发日期
		String startTime = flightMap.get("depTime").toString().trim();
		String depDate = startTime.substring(0,10);
		
		// 出发时间
		String depTime = startTime.substring(11, 16);
		
		// 到达日期
		String endTime = flightMap.get("arrTime").toString().trim();
		
		// 到达时间
		String desTime = endTime.substring(11,16);
		
		Map<String, Object> cabinMap = (Map<String, Object>)flightMap.get("cabin");
		
		String cabin = cabinMap.get("cabin").toString().trim().toUpperCase();
		if(!this.allowCabin(cabin)) return null ;	// 排除舱位

		CrawlResultB2C crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
		crawlResult.setDepTime(depTime);	// 出发时间
		crawlResult.setDesTime(desTime);	// 到达时间

		// 剩余座位数
		String remainSite = String.valueOf(cabinMap.get("cabinNum"));
		if(remainSite==null||"".equals(remainSite)||"null".equals(remainSite))remainSite="A";
		crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(1 == remainSite.length() ? remainSite : remainSite.substring(1)));
		if(crawlResult.getRemainSite() <= 0) return null ;

		// 价格
		crawlResult.setTicketPrice(new BigDecimal(cabinMap.get("originPrice").toString().trim()).setScale(0));
		crawlResult.setSalePrice(crawlResult.getTicketPrice());
		
		return crawlResult;
	}
	
	@Override
	public String httpResult() {
		String qUrl = "";
		try {
			qUrl = queryUrl
//									.replaceAll("%callback%", "jsonp29")
									.replaceAll("%depName%", URLEncoder.encode(this.getJobDetail().getDepName(), "UTF-8"))
									.replaceAll("%desName%", URLEncoder.encode(this.getJobDetail().getDesName(), "UTF-8"))
									.replaceAll("%depDate%", this.getJobDetail().getDepDate())
									.replaceAll("%ua%", UUID.randomUUID().toString());
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", httpResult url异常", e);
		}
		return this.httpProxyGet(qUrl, "other");
	}
	
	/**
	 * 更多舱位
	 * @param fltNo
	 * @return
	 */
	public String httpResult(String fltNo) {
		String qUrl = gaoduanQueryUrl.replaceAll("%ksTS%", String.valueOf(System.currentTimeMillis()))
//						.replaceAll("%depName%", URLEncoder.encode(this.getJobDetail().getDepName(), "UTF-8"))
//						.replaceAll("%desName%", URLEncoder.encode(this.getJobDetail().getDesName(), "UTF-8"))
						.replaceAll("%depDate%", this.getJobDetail().getDepDate())
						.replaceAll("%flightNo%", fltNo)
						.replaceAll("%ua%", UUID.randomUUID().toString());
		return this.httpProxyGet(qUrl, "other");
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
		return httpResult.contains("rgv587_flag");
	}
	
	/**
	 * 
	 * @param httpResult
	 * @return
	 */
	public static String patternHttpResult(String httpResult) {
		Matcher matcher = pattern.matcher(httpResult);
		if(matcher.find()) return matcher.group(1);
		return httpResult;
	}
}