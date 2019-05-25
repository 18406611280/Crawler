package com.aft.crawl.crawler.impl.app;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightSegment;
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
 * 狮子航空 app  
 */
public class B2CJTAppCrawler extends Crawler {
	
	
	private final static String signUrl = "https://mobile.lionair.co.id/GQWCF_FlightEngine/GQDPMobileBookingService.svc/InitializeGQService";
	private final static String queryUrl = "https://mobile.lionair.co.id/GQWCF_FlightEngine/GQDPMobileBookingService.svc/SearchAirlineFlights";
	
	public B2CJTAppCrawler(String threadMark) {
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
			List<Map<String, Object>> outboundJourneys = (List<Map<String, Object>>)resultMap.get("SearchAirlineFlightsResult");
			if(outboundJourneys==null || outboundJourneys.isEmpty() || outboundJourneys.size()==0){
				logger.info("没有航班信息:"+httpResult);
				return null;
			}
//			crawlResults = this.oneFlight(crawlResults,outboundJourneys);
			crawlResults = this.moreFlight(crawlResults,outboundJourneys);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	//旧版本，只能单程
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> oneFlight(List<CrawlResultBase> crawlResults,List<Map<String, Object>> outboundJourneys) {
		SimpleDateFormat SDF_YMDHMS = new SimpleDateFormat("yyyy-MM-dd");
		try {
			CrawlResultB2C b2c = null;
			for(Map<String, Object> flightsResult : outboundJourneys){
				String cabin = "Y";
				Map<String, Object> flight = null;
				Map<String, Object> promoFlight = (Map<String, Object>)flightsResult.get("PromoFlight");
				Map<String, Object> economyFlight = (Map<String, Object>)flightsResult.get("EconomyFlight");
				Map<String, Object> businessFlight = (Map<String, Object>)flightsResult.get("BusinessFlight");
				//获取最低价那个，没有就下一个最低价
				if(promoFlight!=null && promoFlight.size()>0){
					cabin = "P";
					flight = promoFlight;
				}else if(economyFlight!=null && economyFlight.size()>0){
					cabin = "E";
					flight = economyFlight;
				}else if(businessFlight!=null && businessFlight.size()>0){
					cabin = "B";
					flight = businessFlight;
				}else continue;
				
				//过滤中转
				List<Map<String, Object>> outBoundFlights = (List<Map<String, Object>>)flight.get("outBoundFlights");
				List<Map<String, Object>> segments = (List<Map<String, Object>>)outBoundFlights.get(0).get("Segments");
				if(segments.size()>1)continue;
				
				Map<String, Object> segment = segments.get(0);
				String desCode = segment.get("ArrAirPort").toString();
				String depCode = segment.get("DepAirPort").toString();
				String depTime = segment.get("DepTime").toString();
				String desTime = segment.get("ArrTime").toString();
				String airlineCode = segment.get("MarAirLine").toString();
				String fltNo = airlineCode+segment.get("FlightNo").toString();
				String departureDate = segment.get("DepDateTime").toString();
				String arrivalDate = segment.get("ArrDateTime").toString();
				
				if("ID".equals(airlineCode)){
					if("P".equals(cabin)){
						cabin = "K";
					}else if("E".equals(cabin)){
						cabin = "U";
					}
				}
				
				String[] ss = departureDate.split("\\(");
				ss = ss[1].split("\\+");
				Date date = new Date(Long.valueOf(ss[0]));
				String depDate = SDF_YMDHMS.format(date);
				ss = arrivalDate.split("\\(");
				ss = ss[1].split("\\+");
				date = new Date(Long.valueOf(ss[0]));
				String desDate = SDF_YMDHMS.format(date);
				
				Map<String, Object> priceInfo = (Map<String, Object>)flight.get("priceInfo");
				Map<String, Object> adultPrice  = (Map<String, Object>)priceInfo.get("AdultPrice");
				
				String  pricePerPax = adultPrice.get("PricePerPax").toString();
				String  taxPerPax = adultPrice.get("TaxPerPax").toString();

				b2c = new CrawlResultB2C(this.getJobDetail(),airlineCode, fltNo, "N", depCode, desCode, depDate, cabin);
				b2c.setDepTime(depTime);
				b2c.setDesTime(desTime);
				b2c.setEndDate(desDate);
				BigDecimal ticketPrice = new BigDecimal(pricePerPax).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal salePrice = new BigDecimal(taxPerPax	).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal rate = CurrencyUtil.getRequest3("IDR", "CNY");
				if(rate.compareTo(BigDecimal.ZERO)==0){
					rate = CurrencyUtil.getRequest3("IDR", "CNY");
					if(rate.compareTo(BigDecimal.ZERO)==0){
						b2c.setTicketPrice(ticketPrice);
						b2c.setSalePrice(salePrice);
						b2c.setType("此价格的币种类型：IDR");
					}
				}else{
					BigDecimal cnyPrice = ticketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
					BigDecimal taxPrice = salePrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
					b2c.setTicketPrice(cnyPrice);
					b2c.setSalePrice(taxPrice);
				}
				b2c.setRemainSite(2);
				crawlResults.add(b2c);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 解析航班信息异常",e);
		}
		return crawlResults;
	}
	
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult!=null && httpResult.contains("温馨提示")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	//新版本，带有中转的
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> moreFlight(List<CrawlResultBase> crawlResults,List<Map<String, Object>> outboundJourneys) {
		SimpleDateFormat SDF_YMDHMS = new SimpleDateFormat("yyyy-MM-dd");
		try {
			FlightData flightData = null;
			for(Map<String, Object> flightsResult : outboundJourneys){
				String cabin = "Y";
				Map<String, Object> flight = null;
				Map<String, Object> promoFlight = (Map<String, Object>)flightsResult.get("PromoFlight");
				Map<String, Object> economyFlight = (Map<String, Object>)flightsResult.get("EconomyFlight");
				Map<String, Object> businessFlight = (Map<String, Object>)flightsResult.get("BusinessFlight");
				//获取最低价那个，没有就下一个最低价
				if(promoFlight!=null && promoFlight.size()>0){
					cabin = "P";
					flight = promoFlight;
				}else if(economyFlight!=null && economyFlight.size()>0){
					cabin = "E";
					flight = economyFlight;
				}else if(businessFlight!=null && businessFlight.size()>0){
					cabin = "B";
					flight = businessFlight;
				}else continue;
				
				String depCode = flightsResult.get("DepCity").toString();
				String desCode = flightsResult.get("ArrCity").toString();
				String departureDate = flightsResult.get("DepartureDate").toString();
				String[] ss = departureDate.split("\\(");
				ss = ss[1].split("\\+");
				Date date = new Date(Long.valueOf(ss[0]));
				String depDate = SDF_YMDHMS.format(date);
				flightData = new FlightData(this.getJobDetail(), "OW", depCode, desCode, depDate);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String createDate = sdf.format(new Date());
				flightData.setCreateTime(createDate);
				flightData.setRouteType("OW");
				flightData.setAirlineCode("JT");//航司
				flightData.setDepAirport(depCode);//出发地
				flightData.setArrAirport(desCode);//出发地
				flightData.setGoDate(depDate);//出发日期
				Map<String, Object> priceInfo = (Map<String, Object>)flight.get("priceInfo");
				Map<String, Object> adultPrice  = (Map<String, Object>)priceInfo.get("AdultPrice");
				String  pricePerPax = adultPrice.get("PricePerPax").toString();
				String  taxPerPax = adultPrice.get("TaxPerPax").toString();
				BigDecimal ticketPrice = new BigDecimal(pricePerPax).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal salePrice = new BigDecimal(taxPerPax	).setScale(2, BigDecimal.ROUND_HALF_UP);
				flightData = CrawlerUtil.calPrice(flightData, ticketPrice, salePrice, "IDR",rateMap);
				if(flightData==null || !"CNY".equals(flightData.getPrices().get(0).getCurrency()))continue;//转换币种失败直接废弃这条数据
				BigDecimal tp = new BigDecimal(flightData.getPrices().get(0).getFare());
				if(tp.compareTo(new BigDecimal(200))==-1)continue;
				
				
				List<Map<String, Object>> outBoundFlights = (List<Map<String, Object>>)flight.get("outBoundFlights");
				List<Map<String, Object>> segments = (List<Map<String, Object>>)outBoundFlights.get(0).get("Segments");
				List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
				for(int i=0;i<segments.size();i++){
					Map<String, Object> segment = segments.get(i);
					depCode = segment.get("DepAirPort").toString();
					desCode = segment.get("ArrAirPort").toString();
					String depTime = segment.get("DepTime").toString();
					String desTime = segment.get("ArrTime").toString();
					String airlineCode = segment.get("MarAirLine").toString();
					String fltNo = airlineCode+segment.get("FlightNo").toString();
					departureDate = segment.get("DepDateTime").toString();
					String arrivalDate = segment.get("ArrDateTime").toString();
					
					if("ID".equals(airlineCode)){
						if("P".equals(cabin)){
							cabin = "K";
						}else if("E".equals(cabin)){
							cabin = "U";
						}
					}
					
					ss = departureDate.split("\\(");
					ss = ss[1].split("\\+");
					date = new Date(Long.valueOf(ss[0]));
					depDate = SDF_YMDHMS.format(date);
					ss = arrivalDate.split("\\(");
					ss = ss[1].split("\\+");
					date = new Date(Long.valueOf(ss[0]));
					String desDate = SDF_YMDHMS.format(date);
					String shareFlight = this.getShareFlight(airlineCode);
					
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(i+1);
					flightSegment.setAirlineCode(airlineCode);
					flightSegment.setFlightNumber(fltNo);
					flightSegment.setDepAirport(depCode);
					flightSegment.setDepTime(depDate+" "+depTime);
					flightSegment.setArrAirport(desCode);
					flightSegment.setCabinCount("9");
					flightSegment.setArrTime(desDate+" "+desTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
//					flightSegment.setCabinCount(cabinNum);
					flightSegment.setAircraftCode("");
					flightSegmentList.add(flightSegment);
				}
				flightData.setFromSegments(flightSegmentList);
				crawlResults.add(flightData);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 解析航班信息异常",e);
		}
		return crawlResults;
	}
	@Override
	public String httpResult() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String,Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Accept-Encoding","gzip, deflate");
		headerMap.put("Connection","keep-alive");
		headerMap.put("Host","www.lionair.co.id");
		headerMap.put("Upgrade-Insecure-Requests","1");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String res = this.httpProxyGet(httpClientSessionVo, "http://www.lionair.co.id/", "html");
		
		headerMap.put("Host","secure2.lionair.co.id");
		headerMap.put("Accept-Encoding","gzip, deflate, br");
		headerMap.put("Referer","http://www.lionair.co.id/");
		String res1 = this.httpProxyGet(httpClientSessionVo, "https://secure2.lionair.co.id/lionairibe2/OnlineBooking.aspx?depart=DMK&dest.1=KUL&trip_type=one%20way&date.0=8Aug&date.1=&persons.0=1&persons.1=0&persons.2=0&date_flexibility=undefined","html");
//        CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
//		
//		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
//		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
//		
//		Map<String,Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("Accept-Language","zh-CN,en-US;q=0.8");
//		headerMap.put("X-Requested-With","com.goquo.jt.app");
//		headerMap.put("Content-Type","application/json");
//		headerMap.put("Host","mobile.lionair.co.id");
//		httpClientSessionVo.setHeaderMap(headerMap);
		
		String signContext = "{\"B2BID\":\"0\",\"UserLoginId\":\"0\",\"CustomerUserID\":230,\"Language\":\"en-GB\",\"isearchType\":\"15\"}";
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, signUrl, signContext, "json");
		
		String WscContext = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "WscContext");
		if(WscContext==null) return null;
		headerMap.put("WscContext",WscContext);
		String depDate = this.getJobDetail().getDepDate()+" 08:00:00";
		SimpleDateFormat SDF_YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		depDate = String.valueOf(SDF_YMDHMS.parse(depDate).getTime());		
		
		String context ="{\"sd\":{\"Adults\":1,\"AirlineCode\":\"\",\"ArrivalCity\":\""+this.getJobDetail().getDesCode()+"\",\"ArrivalCityName\":null,\"BookingClass\":null,\"CabinClass\":0,\"ChildAge\":[],\"Children\":0,\"CustomerId\":0,\"CustomerType\":0,\"CustomerUserId\":230,\"DepartureCity\":\""+this.getJobDetail().getDepCode()+"\",\"DepartureCityName\":null,\"DepartureDate\":\"/Date("+depDate+")/\",\"DepartureDateGap\":0,\"DirectFlightsOnly\":false,\"Infants\":0,\"IsPackageUpsell\":false,\"JourneyType\":1,\"PreferredCurrency\":\"IDR\",\"ReturnDate\":\"/Date(-2208988800000)/\",\"ReturnDateGap\":0,\"SearchOption\":1},\"fsc\":\"0\"}";
		
		String httpRequset = this.httpProxyPost(httpClientSessionVo, queryUrl,context,"json");
		
//		System.out.println(httpRequset);
		
		return httpRequset;
	}
	public static void main(String[] args) {
		String depdate = "\\/Date(1487984700000+0700)\\/";
		String[] ss = depdate.split("\\(");
		ss = ss[1].split("\\+");
		System.out.println(ss[0]);
		
		long time = Long.valueOf(ss[0]);
		Date date = new Date(time);
		SimpleDateFormat SDF_YMDHMS = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(SDF_YMDHMS.format(date));
	}
}