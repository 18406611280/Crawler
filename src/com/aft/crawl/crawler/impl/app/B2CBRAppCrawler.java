package com.aft.crawl.crawler.impl.app;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

import net.sf.json.JSONObject;

/**
 * 长荣航空 app  
 */
public class B2CBRAppCrawler extends Crawler {
	
	
	private final static String sessionUrl = "https://mall.evaair.com/MobileWBS/TripFlow.asmx";
	private final static String queryUrl = "https://book.evaair.com/plnext/mobileNUI4BR/Override.action?";
	private final static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	public B2CBRAppCrawler(String threadMark) {
		super(threadMark, "0");
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		System.out.println(httpResult);
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 返回httpResult为空:" + httpResult);
			return null;
		}
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(httpResult);
			String data = doc.getElementById("application").attr("app-data");
			
//			crawlResults = this.moreFlight(crawlResults, httpResult, journeyDateMarkets);
			
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	private List<CrawlResultBase> moreFlight(List<CrawlResultBase> crawlResults, String httpResult,NodeList journeyDateMarkets) throws Exception {
		CrawlResultInter b2c = null;
		for(int jd=0;jd<journeyDateMarkets.getLength();jd++){
			Element journeyDateMarket = (Element)journeyDateMarkets.item(jd);
			String depCode = journeyDateMarket.getElementsByTagName("DepartureStation").item(0).getTextContent();
			String desCode = journeyDateMarket.getElementsByTagName("ArrivalStation").item(0).getTextContent();
			String depDate = journeyDateMarket.getElementsByTagName("DepartureDate").item(0).getTextContent().substring(0, 10);
			Element Journeys = (Element)journeyDateMarket.getElementsByTagName("Journeys").item(0);
			NodeList  journeys = Journeys.getElementsByTagName("Journey");
			for(int i=0;i<journeys.getLength();i++){
				b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
				b2c.setRouteType("OW");
				Element journey = (Element)journeys.item(i);
				NodeList segments =  journey.getElementsByTagName("Segment");
				List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
				for(int sg = 0;sg<segments.getLength();sg++){
					Element segment = (Element)segments.item(sg);
					String std = segment.getElementsByTagName("STD").item(0).getTextContent();
					String sta = segment.getElementsByTagName("STA").item(0).getTextContent();
					String tripDepDate = std.substring(0, 10);
					String tripDepTime = std.substring(11, 16);
					String tripDesDate = sta.substring(0, 10);
					String tripDesTime = sta.substring(11, 16);
					String tripDepCode = segment.getElementsByTagName("DepartureStation").item(0).getTextContent();
					String tripDesCode = segment.getElementsByTagName("ArrivalStation").item(0).getTextContent();
					
					Element  flightDesignator = (Element)segment.getElementsByTagName("FlightDesignator").item(0);
					String airlineCode  = flightDesignator.getElementsByTagName("a:CarrierCode").item(0).getTextContent().trim();
					String fltNo  = airlineCode + flightDesignator.getElementsByTagName("a:FlightNumber").item(0).getTextContent().trim();
					
					Element fare = (Element)segment.getElementsByTagName("Fare").item(0);
					if(fare==null || fare.getChildNodes().getLength()==0) break;
					String remainSite = fare.getElementsByTagName("AvailableCount").item(0).getTextContent();
					Element paxFare = (Element)fare.getElementsByTagName("PaxFare").item(0);
					NodeList bookingServiceCharges = paxFare.getElementsByTagName("BookingServiceCharge");
					BigDecimal fareAmount = new BigDecimal(0);
					BigDecimal taxAmount = new BigDecimal(0);
					String currencyCode = "";
					for(int j=0;j<bookingServiceCharges.getLength();j++){
						Element bookingServiceCharge = (Element)bookingServiceCharges.item(j);
						String chargeType = bookingServiceCharge.getElementsByTagName("ChargeType").item(0).getTextContent();
						String amount = bookingServiceCharge.getElementsByTagName("Amount").item(0).getTextContent();
						currencyCode = bookingServiceCharge.getElementsByTagName("CurrencyCode").item(0).getTextContent();
						if("FarePrice".equals(chargeType)){
							fareAmount = fareAmount.add(new BigDecimal(amount));
						}else{
							taxAmount = taxAmount.add(new BigDecimal(amount));
						}
					}
					b2c = CrawlerUtil.calPrice(b2c, fareAmount, taxAmount, currencyCode,rateMap);
					if(!"CNY".equals(b2c.getCurrency()))break;//转换币种失败直接废弃这条数据
					
					
					CrawlResultInterTrip trip = new CrawlResultInterTrip(airlineCode,fltNo,tripDepCode,tripDesCode,tripDepDate,"Y",sg+1,1);
					trip.setDesDate(tripDesDate);
					trip.setDepTime(tripDepTime);
					trip.setDesTime(tripDesTime);
					if(remainSite!=null && !"".equals(remainSite)){
						trip.setRemainSite(Integer.valueOf(remainSite));
					}
					flightTrips.add(trip);
				}
				if(flightTrips.size()>0){
					b2c.setFlightTrips(flightTrips);
					crawlResults.add(b2c);
				}
			}
		}
		return crawlResults;
	}

	@Override
	public String httpResult() throws Exception {
		String depDate =  this.getJobDetail().getDepDate().replaceAll("-", "/");
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		Map<String,Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Host","mall.evaair.com");
		headerMap.put("Content-Type", "text/xml; charset=utf-8");
		headerMap.put("SOAPAction", "http://tempuri.org/fun_TripFlow");
		headerMap.put("Connection", "Keep-Alive");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String content = "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+"<soap:Body>"
				+"<fun_TripFlow xmlns=\"http://tempuri.org/\">"
						+"<strInput>AD,zh_cn,WiFi,4.4.2,SM-N9006,3.7.9,MIBS,O,"+this.getJobDetail().getDepCode()+","+this.getJobDetail().getDesCode()+","+depDate+",,FALSE,EY,1,0,0,,,,,</strInput>"
					+"</fun_TripFlow>"
				+"</soap:Body>"
			+"</soap:Envelope>";
		
		String httpResult = this.httpProxyPost(httpClientSessionVo,sessionUrl,content,"other");
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(httpResult)));
		Element root = doc.getDocumentElement();
		NodeList fun_TripFlowResult = root.getElementsByTagName("fun_TripFlowResult");
		if(fun_TripFlowResult==null || fun_TripFlowResult.getLength()==0)return "";
		String ftfrJson = fun_TripFlowResult.item(0).getTextContent();
		JSONObject jo = JSONObject.fromObject(ftfrJson);
		JSONObject tripFlow = jo.getJSONArray("TripFlow").getJSONObject(0);
		String url = tripFlow.getString("1AURL");
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("ENCT", tripFlow.getString("ENCT"));
		paramMap.put("ENC", tripFlow.getString("ENC"));
		paramMap.put("LANGUAGE", tripFlow.getString("LANGUAGE"));
		paramMap.put("MT", tripFlow.getString("MT"));
		paramMap.put("SEARCH_PAGE", tripFlow.getString("SEARCH_PAGE"));
		paramMap.put("SITE", tripFlow.getString("J200J200"));
		paramMap.put("UI_EMBEDDED_TRANSACTION", tripFlow.getString("MAvailabilityFlowDispatcher"));
		
		headerMap = new HashMap<String, Object>();
		headerMap.put("Host", "book.evaair.com");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headerMap.put("Origin", "");
		headerMap.put("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; SM-N9006 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
		headerMap.put("content-type", "application/x-www-form-urlencoded");
		headerMap.put("Accept-Encoding", "gzip,deflate");
		headerMap.put("Accept-Language", "zh-CN,en-US;q=0.8");
		headerMap.put("X-Requested-With", "com.evaair.android");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		httpResult = this.httpProxyPost(httpClientSessionVo, url,paramMap,"html");
		System.out.println(httpResult);
		
		return httpResult;
	}
}