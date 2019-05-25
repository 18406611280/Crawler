package com.aft.crawl.crawler.impl.b2c;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;

import scala.collection.mutable.StringBuilder;
import sun.misc.BASE64Encoder;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.model.CARequestModel;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.DESUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 南方航空(国际)
 */
public class B2CCZInterCrawler extends Crawler {

	private final static String priceUrl = "https://m.csair.com/CSMBP/data/order/inter/touch/singleWayPrice.do?type=MOBILE&token=E0xywTTmPMVVPd5B8u4cPvBMW2B4ZKPwpZ194hyuI%2FoDWG35pqOxAw%3D%3D&APPTYPE=touch&chanel=touch&DEVICETYPE=Netscape&lang=zh";

	public B2CCZInterCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResultList = this.httpResult();
//		if (this.isTimeout() ||httpResultList==null)return null;

		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
//			for (String httpResult : httpResultList) {
//
//				Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
//				Object objMsg = mapResult.get("dateFlights");
//				if (null == objMsg) {
//					logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:"+ httpResult);
//					return crawlResults;
//				}
//				List<Map<String, Object>> dateflights=(List<Map<String, Object>>)objMsg;
//				FlightData flightData = null;
//				
//				for (Map<String, Object> dateflightMap : dateflights) {
//					//航班信息
//					List<Map<String, Object>> segments = (List<Map<String, Object>>) dateflightMap.get("segments");
//					//舱位价格信息
//					List<Map<String, Object>> prices = (List<Map<String, Object>>) dateflightMap.get("prices");
//					String depAirport="";//起飞地
//					String arrAirport="";//到达地
//					String goDate="";//出发日期
//					String airlineCodes="";
//					List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();//去程航段信息
//					//航段信息
//					for (int i = 0; i < segments.size(); i++) {
//						Map<String, Object> segmentMap = segments.get(i);
//						List<Map<String, Object>> legs = (List<Map<String, Object>>)segmentMap.get("legs");
//						//机型
//						String aircraftCode = legs.get(0).get("planeCnName").toString().trim();
//						
//						String airlineCode = segmentMap.get("carrier").toString().trim().toUpperCase();
//						airlineCodes+=airlineCode+",";
//						String fltNo = airlineCode+segmentMap.get("flightNo").toString().trim();
//						//是否共享
//						String shareFlight="N";
//						Object codeShare = segmentMap.get("codeShare");
//						if (null != codeShare&& Boolean.valueOf(codeShare.toString()))shareFlight = "Y";
//						
//						// 出发,到达
//						String depCode = segmentMap.get("depPort").toString().trim().toUpperCase();
//						String desCode = segmentMap.get("arrPort").toString().trim().toUpperCase();
//						
////					 	// 出发日期
//						String depDate = segmentMap.get("depTime").toString().trim();
//						depDate=depDate.substring(0, 10);
//
//						// 出发时间
//						String depTime = segmentMap.get("depTime").toString().trim();
//						depTime = depTime.substring(11);
//								
//						// 到达时间
//						String desTime = segmentMap.get("arrTime").toString().trim();
//						desTime = desTime.substring(11);
//						
//						String segOrder = segmentMap.get("segOrder").toString().trim();
//						
//						if(i==0){
//							depAirport=depCode;//第一段起点为为出发地
//							goDate=depDate;
//						}
//						if(i==segments.size()-1)arrAirport=desCode;//最后一段到达地为终点
//						
//						FlightSegment flightSegment = new FlightSegment();
//						flightSegment.setTripNo(Integer.valueOf(segOrder));
//						flightSegment.setAirlineCode(airlineCode);
//						flightSegment.setFlightNumber(fltNo);
//						flightSegment.setDepAirport(depCode);
//						flightSegment.setDepTime(depTime);
//						flightSegment.setArrAirport(desCode);
//						flightSegment.setArrTime(desTime);
//						flightSegment.setCodeShare(shareFlight);
//						flightSegment.setAircraftCode(aircraftCode);
//						flightSegmentList.add(flightSegment);
//					}
//						//价格舱位信息
//				for (int j = 0; j < prices.size(); j++) {
//						Map<String, Object> price = prices.get(j);
//						flightData = new FlightData(this.getJobDetail(), "OW",depAirport, arrAirport, goDate);
//						flightData.setFromSegments(flightSegmentList);
//						flightData.setAirlineCode(airlineCodes.substring(0, airlineCodes.length()-1));
//						String cabinCnName = price.get("cabinCnName").toString().trim();
//						flightData.setMemo(cabinCnName);
//						// 价格
//						String adultDisplayFare = price.get("adultDisplayFare").toString().trim();
//						String adultDisplayCurrency = price.get("adultDisplayCurrency").toString().trim();
//						String adultDisplayTax = price.get("adultDisplayTax").toString().trim();
//						List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
//						FlightPrice flightPrice = new FlightPrice();
//						flightPrice.setPassengerType("ADT");
//						flightPrice.setFare(adultDisplayFare);// 票面价
//						flightPrice.setTax(adultDisplayTax);// 税费
//						flightPrice.setCurrency(adultDisplayCurrency);// 币种
//						flightPriceList.add(flightPrice);
//						flightData.setPrices(flightPriceList);
//						
//						List<Map<String, Object>> cabins = (List<Map<String, Object>>)price.get("cabins");
//					//补全每个航段舱位信息	
//					for (int k = 0; k < cabins.size(); k++) {
//						Map<String, Object> cabinMap = cabins.get(k);
//						String cabin = cabinMap.get("name").toString().trim().toUpperCase();
//						String cabinCount = cabinMap.get("bookingClassAvails").toString().trim();
//						String segOrder = cabinMap.get("segOrder").toString().trim();
//						List<FlightSegment> fromSegments = flightData.getFromSegments();
//						Iterator<FlightSegment> iterator = fromSegments.iterator();
//						while(iterator.hasNext()){
//							FlightSegment segment = iterator.next();
//							if(segOrder.equals(String.valueOf(segment.getTripNo()))){
//								segment.setCabin(cabin);
//								segment.setCabinCount(cabinCount);
//							}
//						}
//					}
//					crawlResults.add(flightData);
//					}
//				}
//			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:"+ "\r", e);
			throw e;
		}
		return crawlResults;
	}

	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("ERROR") || httpResult.contains("403")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);

	}
	
	
	
	public String getValue(String start ,String end ,String str){
		 String stri = null;
		 Pattern par = Pattern.compile(start+"(.*?)"+end);
		 Matcher mat = par.matcher(str);
		 while(mat.find()){  
			   stri = mat.group(1);
      }
		 return stri;
	}
	
	public static String dealNotNum(int i, String str){
		String subStr = str.substring(i);
		int s = subStr.indexOf("S");
		String dealStr = subStr.substring(0,s);
	    return dealStr;
	}

//	@SuppressWarnings("unchecked")
	@Override
	public String httpResult() throws Exception {
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		String dep = this.getJobDetail().getDepCode();
		String des = this.getJobDetail().getDesCode();
		String depDate = this.getJobDetail().getDepDate();
		String flightUrl = "https://b2c.csair.com/ita/intl/zh/flights?flex=1&m=0&p=100&t=NKG-TYO-20181123&egs=ITA,ITA";

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "b2c.csair.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,flightUrl,"other");
		String session = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ycyintang.session.id=");
		StringBuilder cookie = new StringBuilder();
		String appUrl = "https://b2c.csair.com/ita/intl/app";
		cookie.append(session);
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer",flightUrl);
		headerMap.put("Cookie",cookie);
		String appStr = this.httpProxyPost(httpClientSessionVo,appUrl,"language=zh&country=cn&m=0&adt=1&cnn=0&inf=0&dep=NKG&depName=NKG&arr=TYO&arrName=TYO&date=20181123&flexible=1","html");
		String execution = getValue("execution=", "\"", appStr);
		String shopUrl = "https://b2c.csair.com/ita/intl/zh/shop/?execution="+execution;
		headerMap.remove("Content-Type");
		headerMap.put("Referer",appUrl);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,shopUrl,"other");
		String WT_flight = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "WT.al_flight=");
		String cz_book = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "cz-book=");
		String cz_book0 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "cz-book-think-0=");
		String cz_book1 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "cz-book-think-1=");
		String cz_book2 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "cz-book-think-2=");
		String cz_book3 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "cz-book-think-3=");
		cookie.append(";").append(WT_flight).append(";").append(cz_book)
		                  .append(";").append(cz_book0)
		                  .append(";").append(cz_book1)
		                  .append(";").append(cz_book2)
		                  .append(";").append(cz_book3);
		long time = System.currentTimeMillis();
		String timeStamp = String.valueOf(time);
		String callBackUrl = "https://b2c.csair.com/B2C40/user/createSid.ao?callback=jQuery110206138386700723023_"+timeStamp+"&_="+timeStamp;
		headerMap.put("Accept", "text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Cookie",cookie);
		headerMap.put("Referer",shopUrl);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,callBackUrl,"other");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		long time2 = System.currentTimeMillis();
		String timeStamp2 = String.valueOf(time2);
		StringBuilder cookie1 = new StringBuilder();
		cookie1.append(session).append(";").append(WT_flight).append(";")
		                       .append(cz_book).append(";").append(JSESSIONID);
		headerMap.put("Cookie",cookie1);
		headerMap.put("Accept","application/json, text/javascript, */*; q=0.01");
		String result = this.httpProxyGet(httpClientSessionVo,"https://b2c.csair.com/ita/rest/intl/shop/calendar?execution="+execution+"&country=cn&move=0&_="+timeStamp2,"other");
		long time3 = System.currentTimeMillis();
		String timeStamp3 = String.valueOf(time3);
		String searchUrl = "https://b2c.csair.com/ita/rest/intl/shop/search?execution="+execution+"&type=sliceGrid&country=cn&language=zh&normal=1&_="+timeStamp3;
		String result2 = this.httpProxyGet(httpClientSessionVo,searchUrl,"json");
		return result2;
	}
	

}