package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.CloseableHttpClient;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 越南航空 
 */
public class B2CVNCrawler extends Crawler {

	private static String queryUrl = "https://wl-prod.sabresonicweb.com/SSW2010/VNVN/webqtrip.html?searchType=NORMAL&journeySpan=OW&origin=%depCode%&destination=%desCode%&departureDate=%depDate%&numAdults=1&numChildren=0&numInfants=0&alternativeLandingPage=true&promoCode=&lang=zh_CN";
	
	
	

	
	public B2CVNCrawler(String threadMark) {
		super(threadMark);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		String httpResult = this.httpResult();
		if(this.isTimeout()) return crawlResults;
		
		try {
			Pattern p = Pattern.compile("rootElement([\\s\\S]+?)};");
			Matcher m = p.matcher(httpResult);
			if(m.find()){
				String flights = m.group(1);
				logger.info(this.getJobDetail().toStr() + ", 采集航班信息:"+flights);
				flights = "{\"rootElement" + flights +"}";
				Map<String, Object> mapResult = MyJsonTransformUtil.readValue(flights, Map.class);
				Map<String, Object> list_tab = (Map<String, Object>)mapResult.get("rootElement");
				if(list_tab==null) return crawlResults;
				//航班信息
				List<Map<String, Object>> list_proposed_bound = (List<Map<String, Object>>)list_tab.get("children");
				Map<String, Object> children1 = list_proposed_bound.get(1);
				List<Map<String, Object>> children2 = (List<Map<String, Object>>)children1.get("children");
				Map<String, Object> list_recommendation = children2.get(0);
				List<Map<String, Object>> children3 = (List<Map<String, Object>>)list_recommendation.get("children");
				Map<String, Object> recommendation = children3.get(5);
				Map<String, Object> model = (Map<String, Object>)recommendation.get("model");
				List<Map<String, Object>> outbounds = (List<Map<String, Object>>)model.get("outbounds");
				
				for(Map<String, Object> outbound : outbounds){
					List<Map<String, Object>> segments = (List<Map<String, Object>>)outbound.get("segments");
					Map<String, Object> segment = segments.get(0);
					
					List<String> flightNumber = (List<String>)segment.get("flightNumber");
					String fltNo = "VN"+String.valueOf(flightNumber.get(0));
					String departureDate = segment.get("departureDate").toString();
					String depTime = departureDate.substring(11, 16);
					String arrivalDate = segment.get("arrivalDate").toString();
					String desTime = arrivalDate.substring(11, 16);
					
					Map<String, Object> basketsRef = (Map<String, Object>)outbound.get("basketsRef");
					for(String key : basketsRef.keySet()){
						Map<String, Object> basket = (Map<String, Object>)basketsRef.get(key);
						Map<String, Object> seatsRemaining = (Map<String, Object>)basket.get("seatsRemaining");
						String remainSite = seatsRemaining.get("seatsRemaining").toString();// 剩余座位数
						String cabin = seatsRemaining.get("customLabelSuffix").toString();
						Map<String, Object> prices = (Map<String, Object>)basket.get("prices");
						List<Map<String, Object>> moneyElements = (List<Map<String, Object>>)prices.get("moneyElements");
						Map<String, Object> moneyElement = moneyElements.get(0);
						Map<String, Object> moneyTO = (Map<String, Object>)moneyElement.get("moneyTO");
						String ticketPrice = moneyTO.get("amount").toString();
						Map<String, Object> currencys = (Map<String, Object>)moneyTO.get("currency");
						String currency = currencys.get("code").toString();
						
						CrawlInterResult crawlResult = new CrawlInterResult(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate());
						crawlResult.setDepTime(depTime);
						crawlResult.setDesTime(desTime);
						// 判断共享
						String shareFlight = this.getShareFlight("VN");
						crawlResult.setShareFlight(shareFlight);
						crawlResult.setFltNo(fltNo);
						crawlResult.setAirlineCode("VN");
						crawlResult.setCabin(cabin);
						crawlResult.setCurrency(currency);
						
						if(remainSite==null||"".equals(remainSite)||"null".equals(remainSite))remainSite="A";
						crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(1 == remainSite.length() ? remainSite : remainSite.substring(1)));
						if(crawlResult.getRemainSite() <= 0) continue ;
						
//						crawlResult.setTaxFee(taxFee);
						crawlResult.setTicketPrice(new BigDecimal(ticketPrice));
						
						crawlResults.add(crawlResult);
					}
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		}
		return crawlResults;
	}

	@Override
	public String httpResult() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClientCert();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		String httpContent = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
				.replaceAll("%desCode%", this.getJobDetail().getDesCode())
				.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return this.httpProxyGet(httpClientSessionVo, httpContent,"html");
	}
}