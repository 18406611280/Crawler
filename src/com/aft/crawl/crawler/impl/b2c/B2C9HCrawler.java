
package com.aft.crawl.crawler.impl.b2c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import scala.collection.mutable.StringBuilder;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FilghtRule;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 长安航空官网9H
 * @author chenminghong
 */

public class B2C9HCrawler extends Crawler{
	
	public B2C9HCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		FlightData flightData = null;
		try {
		    JSONObject jsonRes = JSONObject.parseObject(httpResult);
		    String data = jsonRes.getString("data");   
		    if(data==null||StringUtils.isEmpty(httpResult)){
		    	logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
		    }
		    JSONObject jsonData = JSONObject.parseObject(data);
		    JSONArray originArray = jsonData.getJSONArray("originDestinations");  
		    JSONObject jsonOrigin = originArray.getJSONObject(0);  
            String dep = jsonOrigin.getString("origin");  
            String des = jsonOrigin.getString("destination");  
            String depDate = jsonOrigin.getString("departureDate");  
            JSONArray airArray = jsonOrigin.getJSONArray("airItineraries");
            for(int i = 0; i < airArray.size(); i++){
            	JSONObject jsonAir = airArray.getJSONObject(i);  
            	JSONObject jsonFlight = jsonAir.getJSONArray("flightSegments").getJSONObject(0);
                String departureDate = jsonFlight.getString("departureDate");
                String departureTime = jsonFlight.getString("departureTime");
                String arrivalDate = jsonFlight.getString("arrivalDate");
                String arrivalTime = jsonFlight.getString("arrivalTime");
                String depTime = departureDate + " "+departureTime;//出发日期时间 2017-11-24 10:00:00
				String arrTime = arrivalDate + " "+arrivalTime;//到达日期时间
                String acType = jsonFlight.getString("aircraftName");//机型
                String flightNumber = jsonFlight.getString("flightNumber");//
                String flightEN = jsonFlight.getString("marketingAirlineCode");//航司
                String flightNo = flightEN+flightNumber;//航班号
                String shareFlight = this.getShareFlight(flightEN);//判断是否共享
                JSONArray priceArray = jsonAir.getJSONArray("airItineraryPrices");
                for(int j = 0; j < priceArray.size(); j++){
                	flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
                	JSONObject jsonPrice = priceArray.getJSONObject(j); 
                	String fareName = jsonPrice.getString("fareFamilyName");//机票类型
                	JSONObject travelerPrice = jsonPrice.getJSONArray("travelerPrices").getJSONObject(0);
                	String ticketPrice = travelerPrice.getString("baseFare");//票面价
                	String currency = travelerPrice.getString("baseFareCurrency");//币种
                	JSONObject farePrice = travelerPrice.getJSONArray("farePrices").getJSONObject(0);
                	String cabin = farePrice.getString("bookingClass");//舱位
                	String cabinCount = farePrice.getString("inventoryQuantity");//舱位数
                	if(cabinCount.equals("0")) cabinCount = "9";
                	String tax = "50";
                	
                	List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
					FlightPrice flightPrice = new FlightPrice();
					flightPrice.setPassengerType("ADT");
					flightPrice.setFare(ticketPrice);//票面价
					flightPrice.setTax(tax);//税费
					flightPrice.setCurrency(currency);//币种
					flightPrice.setEquivFare(ticketPrice);
					flightPrice.setEquivTax(tax);
					flightPrice.setEquivCurrency(currency);
					flightPriceList.add(flightPrice);
					flightData.setMemo(fareName);//备注机票类型
					flightData.setAirlineCode(flightEN);
					flightData.setPrices(flightPriceList);
					
//					List<FilghtRule> filghtRuleList = new ArrayList<FilghtRule>();//规则
//					FilghtRule filghtRule = new FilghtRule();
//					filghtRuleList.add(filghtRule);
//					flightData.setRule(filghtRuleList);
					
					List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(1);
					flightSegment.setAirlineCode(flightEN);
					flightSegment.setFlightNumber(flightNo);
					flightSegment.setDepAirport(dep);
					flightSegment.setDepTime(depTime);
					flightSegment.setArrAirport(des);
					flightSegment.setArrTime(arrTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setCabinCount(cabinCount);
					flightSegment.setAircraftCode(acType);
					flightSegmentList.add(flightSegment);
					flightData.setFromSegments(flightSegmentList);
					crawlResults.add(flightData);
                }
            }
		}catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
			
		}
		return crawlResults;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("出错啦") || httpResult.contains("403拒绝访问")){
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}

	public String httpResult() throws Exception {
		
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String, Object> headerMap1 = new HashMap<String, Object>();
		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap1.put("Accept-Encoding", "gzip, deflate");
		headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap1.put("Connection", "keep-alive");
		headerMap1.put("Host", "uwing.travelsky.com");
		headerMap1.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap1.put("Upgrade-Insecure-Requests", "1");
		httpClientSessionVo.setHeaderMap(headerMap1);
		MyHttpClientResultVo httpVo1 = this.httpProxyResultVoGet(httpClientSessionVo, "http://uwing.travelsky.com/uwing/common/login.jsp","html");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo1.getHeaders(), "Set-Cookie","JSESSIONID=");
		
		StringBuilder cookie1 = new StringBuilder();
		cookie1.append(JSESSIONID);
		headerMap1.put("Referer", "http://uwing.travelsky.com/uwing/common/login.jsp");
		headerMap1.put("Cookie",cookie1);
		String res = this.httpProxyGet(httpClientSessionVo, "http://uwing.travelsky.com/uwing/common/login.jsp","html");
		System.out.println(res);
		headerMap1.put("Accept","*/*");
		headerMap1.remove("Upgrade-Insecure-Requests");
		httpVo1 = this.httpProxyResultVoGet(httpClientSessionVo, "http://uwing.travelsky.com/uwing/verifyCodeCreator?paramName=rand","other");
		
//==================================================================		
		String dep = this.getJobDetail().getDepCode();
		String des = this.getJobDetail().getDesCode();
		String depDate = this.getJobDetail().getDepDate();
		
		String param =getJsParam(dep,des,depDate).replaceAll("\"", "\\\\\"");
		String jsParam = "q="+getJsParam("\""+param+"\"");
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.airchangan.com");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		httpClientSessionVo.setHeaderMap(headerMap);
		
//      第一次访问
 		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "http://www.airchangan.com/","","html");
		String testApp = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","testApp=");
		String route = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","route=");
		
		StringBuilder cookie = new StringBuilder()
				.append(testApp).append(";")
				.append(route);
		headerMap.put("Cookie",cookie);
		String result1 = this.httpProxyGet(httpClientSessionVo,"http://www.airchangan.com/portal","html");
		
		String param1 = "q={\"pos\":\"CHANGANAIR_WEB\",\"cabinClass\":\"Economy\",\"currencyType\":\"CNY\",\"flexible\":\"N\",\"languageCode\":\"\",\"tripType\":\"OW\",\"guestTypes\":[{\"code\":\"ADT\",\"amount\":1},{\"code\":\"CNN\",\"amount\":0},{\"code\":\"INF\",\"amount\":0}],\"outboundOption\":{\"originLocationCode\":\""+dep+"\",\"destinationLocationCode\":\""+des+"\",\"departureDate\":\""+depDate+"\"}}";
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer", "http://www.airchangan.com/portal");
		String result2 = this.httpProxyPost(httpClientSessionVo, "http://www.airchangan.com/airEye/flight/select?pos=portal",param1,"html");
		
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("X-Requested-With", "XMLHttpRequest");
		headerMap.put("Referer", "http://www.airchangan.com/airEye/flight/select?pos=portal");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		headerMap.remove("Upgrade-Insecure-Requests");
		String result = this.httpProxyPost(httpClientSessionVo, "http://www.airchangan.com/flight/searchFlightInfo",jsParam,"json");
		if(!result.contains("flightSegments")) return null;//没有航班信息
		return result;
	}
	
		//参数编码
		public String getJsParam(String data) throws ScriptException, NoSuchMethodException
		{
		   ScriptEngineManager manager = new ScriptEngineManager();
		   ScriptEngine engine = manager.getEngineByName("js");
		   String js = "function getParam(data){var e= data;for (var t = String.fromCharCode(e.charCodeAt(0) + e.length), n = 1; n < e.length; n++) t += String.fromCharCode(e.charCodeAt(n) + e.charCodeAt(n - 1)); r = escape(t); return r}";
		   engine.eval(js);
		   Invocable invocable = (Invocable) engine;
		   String result = (String) invocable.invokeFunction("getParam",data);
		   return result;
		}
		
		public String getJsParam(String dep,String des,String depDate){
			String param = "{\"tripType\":\"OW\",\"outboundOption\":{\"originLocationCode\":\""+dep+"\",\"destinationLocationCode\":\""+des+"\",\"departureDate\":\""+depDate+"\"},\"multiCityOptions\":[],\"inboundOption\":{\"departureDate\":\"\"},\"guestTypes\":[{\"code\":\"ADT\",\"amount\":1},{\"code\":\"CNN\",\"amount\":0},{\"code\":\"INF\",\"amount\":0}],\"cabinClass\":\"Economy\",\"currencyType\":\"CNY\",\"isDomestic\":\"Y\",\"languageCode\":\"zh_CN\",\"flexible\":true}";
			return param;
		}
		
}

