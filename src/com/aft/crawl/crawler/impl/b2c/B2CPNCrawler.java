package com.aft.crawl.crawler.impl.b2c;

import java.net.SocketException;
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

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.proxy.ProxyVo;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.cmh.HttpUtil;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 西部航空 
 */
public class B2CPNCrawler extends Crawler {
	
	private String dep;
	private String des;
	private String depDate;
	
	private final static String selectUrl = "http://www.westair.cn/mainprocess/select";
	
	private final static String resultUrl = "http://www.westair.cn/mainprocess/flight/searchFlightInfo";
	
	public B2CPNCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		String httpResult = StringTxtUtil.TxtToString("D:\\test.txt");
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		FlightData flightData = null;
		try {
		    JSONObject jsonRes = JSONObject.parseObject(httpResult);
		    JSONObject data = jsonRes.getJSONObject("data");
		    String hna_message = data.getString("hna_message");   
		    if(data!=null && !hna_message.contains("操作成功")){
		    	logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
		    }
		    JSONObject originDestinations = data.getJSONArray("originDestinations").getJSONObject(0);
		    JSONArray airItineraries = originDestinations.getJSONArray("airItineraries");  
		    int flightSize = airItineraries.size();
		    for(int i = 0; i<flightSize; i++) {
		    	JSONObject jsonAir = airItineraries.getJSONObject(i);  
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
		    		String tax = "90";
		    		
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
	protected boolean exceptionNeedToChangeIp(Exception e) {
		return (e instanceof SocketException && "Connection reset".equals(e.getMessage())) ? true : false;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType)
			throws Exception {
		if(httpResult.contains("您所请求的网址（URL）无法获取"))return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	@Override
	public String httpResult() throws Exception {
		
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.westair.cn");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, selectUrl,"other");
		String nodeWeb = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","nodeWeb=");
		
//		headerMap.remove("Upgrade-Insecure-Requests");
//        headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
//        headerMap.put("X-Requested-With","XMLHttpRequest");
//        headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
//        headerMap.put("Referer","http://www.westair.cn/mainprocess/select");
//        headerMap.put("Cookie",nodeWeb);
//        String param =getJsParam(dep,des,depDate);
//		String jsParam = getJsParam(param);
//        Map<String, Object> param2 = new HashMap<String, Object>();
//        param2.put("q", jsParam);
//        String re = this.httpProxyPost(httpClientSessionVo, resultUrl,param2,"other");
//		String res = httpVo.getHttpResult();
        Map<String, String> headerMap1 = new HashMap<String, String>();
        headerMap1.put("Host", "www.westair.cn"); 
        headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"); 
        headerMap1.put("Accept-Encoding", "gzip, deflate"); 
        headerMap1.put("Connection", "keep-alive"); 
        headerMap1.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
        headerMap1.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headerMap1.put("X-Requested-With","XMLHttpRequest");
        headerMap1.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        headerMap1.put("Referer","http://www.westair.cn/mainprocess/select");
        headerMap1.put("Cookie",nodeWeb);
        
        String param =getJsParam(dep,des,depDate);
		String jsParam = getJsParam(param);
        Map<String, String> param2 = new HashMap<String, String>();
        param2.put("q", jsParam);
        ProxyVo proxyVo = ProxyUtil.getProxy(threadMark);
		String proxyIp = null == proxyVo ? null : proxyVo.getProxyIp();
		Integer proxyPort = null == proxyVo ? null : proxyVo.getProxyPort();
        String res = HttpUtil.doPost(resultUrl, headerMap1,param2,proxyIp,proxyPort);
        if(!res.contains("\"code\":200")) {
        	super.changeProxy();
        }
		return res;
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
			String param = "{\"tripType\":\"OW\",\"outboundOption\":{\"departureDate\":\""+depDate+"\",\"desIsDomestic\":\"Y\",\"destinationLocationCode\":\""+des+"\",\"destinationLocationName\":\"\",\"oriIsDomestic\":\"N\",\"originLocationCode\":\""+dep+"\",\"originLocationName\":\"\"},\"multiCityOptions\":[],\"inboundOption\":{\"departureDate\":\"\"},\"guestTypes\":[{\"amount\":1,\"code\":\"ADT\"},{\"amount\":0,\"code\":\"CNN\"},{\"amount\":0,\"code\":\"INF\"},{\"amount\":0,\"code\":\"SOL\"},{\"amount\":0,\"code\":\"POL\"}],\"cabinClass\":\"Economy\",\"currencyType\":\"CNY\",\"isDomestic\":\"N\",\"flexible\":\"N\",\"langType\":\"zh_CN\",\"specialGuestType\":\"\",\"platformType\":\"pc\"}";
			return param;
		}
}