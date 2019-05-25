package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.MyStringUtil;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;

/**
 * chenminghong
 * 海南航空 
 */
public class B2CHUCrawler extends Crawler {

	private final static String flightUrl  = "https://new.hnair.com/hainanair/ibe/common/flightSearch.do";
	private final static String searchUrl  = "https://new.hnair.com/hainanair/ibe/common/processSearchForm.do";
	private String dep;
	private String des;
	private String depDate;
	public B2CHUCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		httpResult = httpResult.replaceAll("\r|\n*","");
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			List<String> results = MyStringUtil.getValueList("var Flight \\= \\{\\}\\;", "Flights\\[position\\] \\= Flight\\;", httpResult);
			if(null == results) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息;");
				return crawlResults;
			}
			CrawlResultB2C crawlResult = null;
			for(String str:results){
				if(str.contains("SegmentRPH= '2'")) continue;
				String AirlineCarrierEN = MyStringUtil.getValue("Segment.carrierAirlineEN \\= \'", "\'", str);//航司
				if(!"HU".equals(AirlineCarrierEN)) continue;
				String FlightNumber = MyStringUtil.getValue("Segment.marketingFlightNum \\= \'", "\'", str);//航班号 
				String DepartureIATA = MyStringUtil.getValue("Segment.departureIATA \\= \'", "\'", str);//出发地 
				String ArrivalIATA = MyStringUtil.getValue("Segment.arrivalIATA \\= \'", "\'", str);//到达地 
				String DepartureDate = MyStringUtil.getValue("Segment.departureDate \\= \'", "\'", str);//出发日期
				String ArrivalDate = MyStringUtil.getValue("Segment.arrivalDate \\= \'", "\'", str);//到达日期
				String DepartureTime = MyStringUtil.getValue("Segment.departureTime \\= \'", "\'", str);//出发时间 
				String ArrivalTime = MyStringUtil.getValue("Segment.arrivalTime \\= \'", "\'", str);//到达时间 
				
				List<String> prices = MyStringUtil.getValueList("var FareInfos\\=\\{\\}\\;", "FareInfos\\[FareInfosCode\\]\\=FareInfo\\;", str);
				for(String price:prices){
					if(price.contains("fareFamilyCode='BUSINESS'")) continue;
					String cabin = MyStringUtil.getValue("FareInfo.resBookDesigCode\\=\'", "\'", price);//舱位
					String baseAmount = MyStringUtil.getValue("priceDetails.baseAmount \\=\'", "\'", price);//票面价
					String CNTax = MyStringUtil.getValue("priceDetails.CNTax \\= \'", "\'", price);//税费
					String seatNum = MyStringUtil.getValue("seatDetails.seatNum \\=\'", "\'", price);//税费
					// 判断共享
					String shareFlight = this.getShareFlight(AirlineCarrierEN);

					crawlResult = new CrawlResultB2C(this.getJobDetail(), AirlineCarrierEN, AirlineCarrierEN+FlightNumber, shareFlight, DepartureIATA, ArrivalIATA, DepartureDate, cabin);
					crawlResult.setDepTime(DepartureTime);	// 出发时间
					crawlResult.setDesTime(ArrivalTime);	// 到达时间
					crawlResult.setEndDate(ArrivalDate);//到达日期
					crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(seatNum));		// 默认
					crawlResult.setTicketPrice(new BigDecimal(baseAmount));
					crawlResult.setSalePrice(new BigDecimal(CNTax));
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
			if(httpResult.contains("抱歉，遇到些问题"))return true;
			return super.needToChangeIp(httpResult, document, jsonObject, returnType);
		}
	
	public String httpResult() throws Exception{
		
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		httpClientSessionVo.setHeaderMap(headerMap);
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "new.hnair.com");
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, flightUrl, "other");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String Webtrends = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "Webtrends=");
		String X_LB = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "X-LB=");
		
		StringBuilder cookie = new StringBuilder();
		cookie.append(JSESSIONID).append(";").append(Webtrends).append(";").append(X_LB);
		
		headerMap.put("Cookie", cookie);
		headerMap.put("Cache-Control", "max-age=0");
		headerMap.put("Origin", "http://new.hnair.com");
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Referer", "https://new.hnair.com/hainanair/ibe/common/flightSearch.do");
		String param = "Search%2FAirlineMode=false&Search%2FcalendarCacheSearchDays=60&Search%2FcalendarSearched=false&dropOffLocationRequired=false&Search%2FsearchType=F&searchTypeValidator=F&xSellMode=false&Search%2FflightType=oneway&destinationLocationSearchBoxType=L&Search%2FAirDirectOnly=0&Search%2FseatClass=A&Search%2FOriginDestinationInformation%2FOrigin%2Flocation=CITY_"+dep+"_CN&Search%2FOriginDestinationInformation%2FOrigin%2Flocation_input=&Search%2FOriginDestinationInformation%2FDestination%2Flocation=CITY_"+des+"_CN&Search%2FOriginDestinationInformation%2FDestination%2Flocation_input=&Search%2FDateInformation%2FdepartDate="+depDate+"&Search%2FDateInformation%2FreturnDate=&Search%2FPassengers%2Fadults=1&Search%2FPassengers%2Fchildren=0&Search%2FPassengers%2Finfants=0&Search%2FPassengers%2FMilitaryDisabled=0&Search%2FPassengers%2FPoliceDisabled=0&Search%2FpromotionCode=";
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, searchUrl,param, "other");
		
		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		if(location==null) return null;
		headerMap.remove("Content-Type");
		String result = this.httpProxyGet(httpClientSessionVo, location, "other");
		return result;
	}
	
}