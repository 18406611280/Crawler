package com.aft.crawl.crawler.impl.b2c;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.browser.util.CrawlerUtil;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 全日空航空 
 * @author chenminghong
 */
public class B2CNHCrawler extends Crawler {

	private final static String searchUrl = "https://aswbe-i.ana.co.jp/international_asw/pages/revenue/search/roundtrip/search_roundtrip_input.xhtml?rand=%time%&CONNECTION_KIND=HKG&LANG=hk";//香港版
	
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	String dep = null;
	String des = null;
	String depDate = null;
	
	public B2CNHCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult) || httpResult.equals("") || httpResult==null) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			String recommendationList = CrawlerUtil.getValue("recommendationList", "firstOutboundDepartureCountry", httpResult);
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object objMsg = mapResult.get("boundFlightInfoList");
			if (null == objMsg) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:"+ httpResult);
				return crawlResults;
			}
			
			Map<String, Object> boundFlightInfo =((List<Map<String, Object>>)objMsg).get(0);
			FlightData flightData = null;
			List<Map<String, Object>> flightInfoList = (List<Map<String, Object>>) boundFlightInfo.get("flightInfoList");
			
			for (Map<String, Object> flightInfoMap : flightInfoList) {
				List<Map<String, Object>> segmentInfoList = (List<Map<String, Object>>) flightInfoMap.get("segmentInfoList");
				if(segmentInfoList.size()>1) continue ;
				flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				String flightID = flightInfoMap.get("flightID").toString().trim();
				Map<String, Object> segmentInfoMap = segmentInfoList.get(0);
				String departureDate = ((Map<String, Object>)segmentInfoMap.get("departureDateInfo")).get("date").toString();
				String arrivalDate = ((Map<String, Object>)segmentInfoMap.get("arrivalDateInfo")).get("date").toString();
				String depTime = toMyDate(departureDate);
				String desTime = toMyDate(arrivalDate);
				String fltEN = "NH";
				String fltNo = segmentInfoMap.get("flightNumber").toString();
				String shareFlight = this.getShareFlight(fltEN);//判断是否共享 
				List<String> recommenList = CrawlerUtil.getValueList("flightID\":\""+flightID+"\"", "highestClass", recommendationList);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				for(String recommen : recommenList){
					String cabin = CrawlerUtil.getValue("rbd\":\"","\"",recommen);
					if(!"H".equals(cabin)) continue;
					String ticketPrice = CrawlerUtil.getValue("amountWithoutTax\":",",",recommen);
					String tax = CrawlerUtil.getValue("tax\":",",",recommen);
					String cabinCount = CrawlerUtil.getValue("numberOfLastSeats\":",",",recommen);
					
					flightData.setCreateTime(date);
					flightData.setRouteType("OW");
					flightData.setAirlineCode(fltEN);//航司
					flightData.setDepAirport(dep);//出发地
					flightData.setArrAirport(des);//出发地
					flightData.setGoDate(depDate);//出发日期
					
					List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
					FlightPrice flightPrice = new FlightPrice();
					flightPrice.setPassengerType("ADT");
					flightPrice.setFare(ticketPrice);//票面价
					flightPrice.setTax(tax);//税费
					flightPrice.setCurrency("HKD");//币种
					flightPrice.setEquivFare(ticketPrice);
					flightPrice.setEquivTax(tax);
					flightPrice.setEquivCurrency("HKD");
					flightPriceList.add(flightPrice);
					flightData.setPrices(flightPriceList);
					
					List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(1);
					flightSegment.setAirlineCode(fltEN);
					flightSegment.setFlightNumber(fltEN+fltNo);
					flightSegment.setDepAirport(dep);
					flightSegment.setDepTime(depTime);
					flightSegment.setArrAirport(des);
					flightSegment.setArrTime(desTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setCabinCount(cabinCount);
					flightSegmentList.add(flightSegment);
					flightData.setFromSegments(flightSegmentList);
					crawlResults.add(flightData);
				}
			}
			
			
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}
	
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("ERROR") || httpResult.contains("圖像認證") || httpResult.contains("Access Denied")){
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	/**
	 * 获取请求内容
	 * @return
	 */
	public String httpResult() throws Exception{
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		Date date = new Date();
		String newDate = new SimpleDateFormat("yyyyMMdd").format(date);
		String dateTime = getDateTime(date,1);
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.ana.co.jp");
		httpClientSessionVo.setHeaderMap(headerMap);
		String first = this.httpProxyGet(httpClientSessionVo,"https://www.ana.co.jp/zh/hk/","html");
		
		String param = "hiddenSearchMode=ONE_WAY&departureAirportCode%3Afield=%dep%&arrivalAirportCode%3Afield=%des%&departureDate%3Afield=%depDate%&returnDate%3Afield=%newDate%&adultCount=1&youngAdultCount=0&childCount=0&infantCount=0&boardingClass=JDE&promotionCode=&STATIC=STATIC&%E6%90%9C%E5%B0%8B="; //香港
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer","https://www.ana.co.jp/zh/hk/");
		headerMap.put("Host", "aswbe-i.ana.co.jp");
		String postParam = param.replace("%dep%", dep).replace("%des%", des)
				          .replace("%depDate%", depDate.replace("-", ""))
				          .replace("%newDate%", newDate);
		
		String SearchUrl =searchUrl.replace("%time%", dateTime);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, SearchUrl,postParam,"html");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		if(JSESSIONID==null) return null;
		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		if(location ==null || !location.contains("https")) return null; 
		StringBuilder cookie = new StringBuilder().append(JSESSIONID);
		headerMap.put("Cookie",cookie); 
		headerMap.remove("Content-Type");
		String result1 = this.httpProxyGet(httpClientSessionVo,location,"html");
		if(result1==null) return null;
		String result = CrawlerUtil.getValue("Asw.ResultOutput = ", ";", result1);
		return result;
	}
	
	 public String getDateTime(Date date, int hour){   
	        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	        if (date == null) return "";   
	        Calendar cal = Calendar.getInstance();    
	        cal.setTime(date);   
	        cal.add(Calendar.HOUR, hour);// 24小时制   
	        date = cal.getTime();   
	        cal = null;   
	        return format.format(date);   

	    }
	 
	 public String toMyDate(String time) throws ParseException{   
		 SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		 Date oldDate = format.parse(time);
		 SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 return format2.format(oldDate);   
		 
	 }
	 
}