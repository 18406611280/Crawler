package com.aft.crawl.crawler.impl.app;

import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.aft.app.tr.Tiger.Constants;
import com.aft.app.tr.TigerEntity.GetAvailabilityEntity;
import com.aft.app.tr.utils.TokenParser;
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

/**
 * 虎航航空 app  
 */
public class B2CTRAppCrawler extends Crawler {
	
	
	private final static String queryUrl = "https://tigerair.themobilelife.com/BookingManager.svc";
	
	public B2CTRAppCrawler(String threadMark) {
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
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(httpResult)));
			Element root = doc.getDocumentElement();
			NodeList journeyDateMarkets = root.getElementsByTagName("JourneyDateMarket");
			if(journeyDateMarkets==null || journeyDateMarkets.getLength()==0){
				logger.info("没有航班信息");
				return crawlResults;
			}
			
			//只有单程
			//crawlResults = this.owFlight(crawlResults, httpResult, journeyDateMarkets);
			//有单程和联程
			crawlResults = this.moreFlight(crawlResults, httpResult, journeyDateMarkets);
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
					BigDecimal taxAmount =  new BigDecimal(0);
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
	private List<CrawlResultBase> owFlight(List<CrawlResultBase> crawlResults, String httpResult,NodeList journeyDateMarkets) throws Exception {
		CrawlResultB2C b2c = null;
		for(int jd=0;jd<journeyDateMarkets.getLength();jd++){
			Element journeyDateMarket = (Element)journeyDateMarkets.item(jd);
			Element Journeys = (Element)journeyDateMarket.getElementsByTagName("Journeys").item(0);
			NodeList  journeys = Journeys.getElementsByTagName("Journey");
			for(int i=0;i<journeys.getLength();i++){
				Element journey = (Element)journeys.item(i);
				NodeList segments =  journey.getElementsByTagName("Segment");
				//过滤联程和往返
				if(segments.getLength()>1)continue;
				
				Element segment = (Element)segments.item(0);
				
				String std = segment.getElementsByTagName("STD").item(0).getTextContent();
				String sta = segment.getElementsByTagName("STA").item(0).getTextContent();
				String depDate = std.substring(0, 10);
				String depTime = std.substring(11, 16);
				String desDate = sta.substring(0, 10);
				String desTime = sta.substring(11, 16);
				String depCode = segment.getElementsByTagName("DepartureStation").item(0).getTextContent();
				String desCode = segment.getElementsByTagName("ArrivalStation").item(0).getTextContent();
				
				Element  flightDesignator = (Element)segment.getElementsByTagName("FlightDesignator").item(0);
				String airlineCode  = flightDesignator.getElementsByTagName("a:CarrierCode").item(0).getTextContent().trim();
				String fltNo  = airlineCode + flightDesignator.getElementsByTagName("a:FlightNumber").item(0).getTextContent().trim();
				
				
				
				Element fare = (Element)segment.getElementsByTagName("Fare").item(0);
				
				String remainSite = fare.getElementsByTagName("AvailableCount").item(0).getTextContent();
				
				NodeList bookingServiceCharges = fare.getElementsByTagName("BookingServiceCharge");
				String fareAmount = "0";
				String taxAmount = "0";
				String currencyCode = "";
				for(int j=0;j<bookingServiceCharges.getLength();j++){
					Element bookingServiceCharge = (Element)bookingServiceCharges.item(j);
					String chargeType = bookingServiceCharge.getElementsByTagName("ChargeType").item(0).getTextContent();
					String amount = bookingServiceCharge.getElementsByTagName("Amount").item(0).getTextContent();
					currencyCode = bookingServiceCharge.getElementsByTagName("CurrencyCode").item(0).getTextContent();
					if("FarePrice".equals(chargeType)){
						fareAmount = amount;
					}else if("Tax".equals(chargeType)){
						taxAmount = amount;
					}
				}
				b2c = new CrawlResultB2C(this.getJobDetail(),airlineCode, fltNo, "N", depCode, desCode, depDate, "Y");
				b2c.setDepTime(depTime);
				b2c.setDesTime(desTime);
				b2c.setEndDate(desDate);
				BigDecimal ticketPrice = new BigDecimal(fareAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal salePrice = new BigDecimal(taxAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
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
				if(remainSite!=null && !"".equals(remainSite)){
					b2c.setRemainSite(Integer.valueOf(remainSite));
				}
				crawlResults.add(b2c);
			}
		}
		return crawlResults;
	}
	@Override
	public String httpResult() throws Exception {
//		RequestSession req = new RequestSession(Constants.getSessionUrl, Constants.Authorization);
//		String result = req.request(null);
		SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
		String endDate = this.getJobDetail().getDepDate();
		Date depDate = YMD.parse(this.getJobDetail().getDepDate());
		Calendar ca = Calendar.getInstance();
		ca.setTime(depDate);
		ca.add(Calendar.DATE, -9);
		depDate = ca.getTime();
		String beginDate = YMD.format(depDate);
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);//HttpClients.createDefault();
		
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		Map<String,Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Authorization", Constants.Authorization);
		httpClientSessionVo.setHeaderMap(headerMap);
		String result = this.httpProxyGet(httpClientSessionVo,Constants.getSessionUrl ,"other");
		
		String sig = TokenParser.parse(result);
		headerMap.put("SOAPAction", Constants.SOAPGetAvailability);
		GetAvailabilityEntity entity = new GetAvailabilityEntity(sig, this.getJobDetail().getDepCode(),this.getJobDetail().getDesCode(),beginDate,endDate, this.getJobDetail().getCurrency());
		String e = entity.getEntity();
		
		headerMap.put("Content-Type",  "text/xml; charset=utf-8" );
		
		String httpRequset = this.httpProxyPost(httpClientSessionVo, queryUrl,e,"other");
//		System.out.println(httpRequset);	
//		SSLPost post = new SSLPost(queryUrl, Constants.Authorization, Constants.SOAPGetAvailability);
//		String httpRequset = post.request(e);
		return httpRequset;
	}
}