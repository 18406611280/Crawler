package com.aft.crawl.crawler.impl.app;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.eclipse.swt.internal.LONG;
import org.jsoup.nodes.Document;

import scala.collection.mutable.StringBuilder;
import sun.misc.BASE64Encoder;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.crawler.AtomicLock;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.model.CARequestModel;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.proxy.ProxyVo;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.logger.CaAccountLog;
import com.aft.logger.MyCrawlerLogger;
import com.aft.utils.DESUtil;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.cmh.HttpUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 国航APP
 * @author chenminghong
 */
public class B2CCA_MCrawler extends Crawler {

	String dep = null;
	String des = null;
	String depDate = null;
	private static final Object ableLook = new Object();
	private static volatile long firstTime = 0;
	private static volatile long lastTime;
	private static volatile boolean flag = false;
	private static volatile String ziYinNo = "";
	private static volatile String userId = "";
	private static volatile String infoId = "";
	Logger logger1 = Logger.getLogger(CaAccountLog.class);
	public B2CCA_MCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResultList();
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult) || httpResult.equals("") || httpResult==null) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		List<CrawlResultBase> crawlResult = null;
		if(this.getPageType().equals(CrawlerType.B2CCAPageType)){//国内
			crawlResult = getCAInner(crawlResults, httpResult);
		}
		if(this.getPageType().equals(CrawlerType.B2CCAInterPageType)){//国际
			crawlResult = getCAInter(crawlResults, httpResult);
		}
		return crawlResult;
	}

	private List<CrawlResultBase> getCAInter(List<CrawlResultBase> crawlResults, String httpResult) {
		try {
			FlightData flightData = null;
			JSONObject resultObj = JSONObject.parseObject(httpResult);
			JSONObject resp = resultObj.getJSONObject("resp");
			JSONObject Goto = resp.getJSONObject("goto");
			if(Goto==null) return crawlResults;
			JSONArray taxesFeesList = Goto.getJSONArray("taxesFeesList");
			int taxSize = taxesFeesList.size();
			BigDecimal tax = new BigDecimal(0);
			for(int i=0;i<taxSize;i++){
				String taxFeePrice = taxesFeesList.getJSONObject(i).getString("taxFeePrice");
				tax = tax.add(new BigDecimal(taxFeePrice));
			}
			JSONArray flightInfomationList = Goto.getJSONArray("flightInfomationList");
			int flightSize = flightInfomationList.size();
			for(int j=0; j<flightSize; j++){
				JSONObject flight = flightInfomationList.getJSONObject(j);
				flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				String cabinCount = flight.getString("ticketNum");//舱位数
				if(cabinCount.equals("")){
					cabinCount="9";
				}
				String cabin = flight.getString("lowClass");//舱位
				String ticketPrice = null;
				String memo = "";
				String vipPrice= flight.getString("vip_price");//会员价
				if(!"".equals(vipPrice) && vipPrice!=null){
					ticketPrice = vipPrice;
					memo="会员价";
				}else{
					ticketPrice = flight.getString("lowPrice");//票面价
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setAirlineCode("CA");//航司
				flightData.setMemo(memo);
				
				List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
				FlightPrice flightPrice = new FlightPrice();
				flightPrice.setPassengerType("ADT");
				flightPrice.setFare(ticketPrice);//票面价
				flightPrice.setTax(tax.toString());//税费
				flightPrice.setCurrency("CNY");//币种
				flightPrice.setEquivFare(ticketPrice);
				flightPrice.setEquivTax(tax.toString());
				flightPrice.setEquivCurrency("CNY");
				flightPriceList.add(flightPrice);
				flightData.setPrices(flightPriceList);
				
				List<FlightSegment> flightList = new ArrayList<FlightSegment>();
				JSONArray flightSegmentList = flight.getJSONArray("flightSegmentList");
				int flightListSize = flightSegmentList.size();
				for(int t=0; t<flightListSize; t++){
					FlightSegment flightSegment = new FlightSegment();
					JSONObject Flight = flightSegmentList.getJSONObject(t);
					flightSegment.setTripNo(t+1);
					flightSegment.setAirlineCode(Flight.getString("operatingAirline"));
					String shareFlight = this.getShareFlight(Flight.getString("operatingAirline"));//判断是否共享 
					flightSegment.setFlightNumber(Flight.getString("flightNo"));
					flightSegment.setDepAirport(Flight.getString("flightDep"));
					String DepDate = Flight.getString("flightDepdatePlan");
					String depTime = Flight.getString("flightDeptimePlan").substring(0, 8);
					flightSegment.setDepTime(DepDate+" "+depTime);
					String DesDate = Flight.getString("flightArrdatePlan");
					String desTime = Flight.getString("flightArrtimePlan").substring(0, 8);
					flightSegment.setArrAirport(Flight.getString("flightArr"));
					flightSegment.setArrTime(DesDate+" "+desTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setCabinCount(cabinCount);
					flightSegment.setAircraftCode(Flight.getString("flightModel"));
					flightList.add(flightSegment);
				}
				flightData.setFromSegments(flightList);
				crawlResults.add(flightData);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:\r"+httpResult, e);
			throw e;
		}
		return crawlResults;
	}

//	@Override
//	protected boolean needToChangeIp(String httpResult, Document document,
//			Object jsonObject, String returnType) throws Exception {
//		if (httpResult.contains("ERROR")) {
//			return true;
//		}
//		return super.needToChangeIp(httpResult, document, jsonObject,returnType);
//
//	}
	
	public List<CrawlResultBase> getCAInner(List<CrawlResultBase> crawlResults ,String httpResult){
		try {
			FlightData flightData = null;
			JSONObject resultObj = JSONObject.parseObject(httpResult);
			JSONObject resp = resultObj.getJSONObject("resp");
			JSONObject Goto = resp.getJSONObject("goto");
			if(Goto==null) return crawlResults;
			JSONArray taxesFeesList = Goto.getJSONArray("taxesFeesList");
			int taxSize = taxesFeesList.size();
			BigDecimal tax = new BigDecimal(0);
			for(int i=0;i<taxSize;i++){
				String taxFeePrice = taxesFeesList.getJSONObject(i).getString("taxFeePrice");
				tax = tax.add(new BigDecimal(taxFeePrice));
			}
			JSONArray flightInfomationList = Goto.getJSONArray("flightInfomationList");
			int flightSize = flightInfomationList.size();
			for(int j=0; j<flightSize; j++){
				JSONObject flight = flightInfomationList.getJSONObject(j);
				JSONArray flightSegmentList = flight.getJSONArray("flightSegmentList");
				if(flightSegmentList.size()>1) continue;
				flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				String cabinCount = flight.getString("ticketNum");//舱位数
				if(cabinCount.equals("")){
					cabinCount="9";
				}
				String cabin = flight.getString("lowClass");//舱位
				String ticketPrice = null;
				String memo = "";
				String vipPrice= flight.getString("vip_price");//会员价
				if(!"".equals(vipPrice) && vipPrice!=null){
					ticketPrice = vipPrice;
					memo="会员价";
				}else{
					ticketPrice = flight.getString("lowPrice");//票面价
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setAirlineCode("CA");//航司
				flightData.setMemo(memo);
				
				List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
				FlightPrice flightPrice = new FlightPrice();
				flightPrice.setPassengerType("ADT");
				flightPrice.setFare(ticketPrice);//票面价
				flightPrice.setTax(tax.toString());//税费
				flightPrice.setCurrency("CNY");//币种
				flightPrice.setEquivFare(ticketPrice);
				flightPrice.setEquivTax(tax.toString());
				flightPrice.setEquivCurrency("CNY");
				flightPriceList.add(flightPrice);
				flightData.setPrices(flightPriceList); 
				
				List<FlightSegment> flightList = new ArrayList<FlightSegment>();
				FlightSegment flightSegment = new FlightSegment();
				JSONObject Flight = flightSegmentList.getJSONObject(0);
				flightSegment.setTripNo(1);
				flightSegment.setAirlineCode(Flight.getString("operatingAirline"));
				String shareFlight = this.getShareFlight(Flight.getString("operatingAirline"));//判断是否共享 
				flightSegment.setFlightNumber(Flight.getString("flightNo"));
				String depCode = Flight.getString("flightDep");
				String desCode = Flight.getString("flightArr");
				if(!depCode.equals(this.dep) || !desCode.equals(this.des)) {
					continue;
				}
				flightSegment.setDepAirport(depCode);
				String DepDate = Flight.getString("flightDepdatePlan");
				String depTime = Flight.getString("flightDeptimePlan").substring(0, 8);
				flightSegment.setDepTime(DepDate+" "+depTime);
				String DesDate = Flight.getString("flightArrdatePlan");
				String desTime = Flight.getString("flightArrtimePlan").substring(0, 8);
				flightSegment.setArrAirport(desCode);
				flightSegment.setArrTime(DesDate+" "+desTime);
				flightSegment.setCodeShare(shareFlight);
				flightSegment.setCabin(cabin);
				flightSegment.setCabinCount(cabinCount);
				flightSegment.setAircraftCode(Flight.getString("flightModel"));
				flightList.add(flightSegment);
				flightData.setFromSegments(flightList);
				crawlResults.add(flightData);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:\r"+httpResult, e);
			throw e;
		}
		return crawlResults;
	}
	
	@SuppressWarnings("unchecked")
	public String httpResultList() throws Exception {
////		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
//		CloseableHttpClient httpClient = HttpClients.createDefault();
//		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
//		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
//		dep = this.getJobDetail().getDepCode();
//		des = this.getJobDetail().getDesCode();
//		depDate = this.getJobDetail().getDepDate();
//		
//		Map<String, Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//		headerMap.put("Accept-Encoding","gzip, deflate, br");
//		headerMap.put("Upgrade-Insecure-Requests","1");
//		headerMap.put("Connection","keep-alive");
//		headerMap.put("Accept-Language","zh-CN,zh;q=0.9");
//		headerMap.put("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
//		headerMap.put("Host", "m.airchina.com.cn");
//		httpClientSessionVo.setHeaderMap(headerMap);
//		String acUrl = "https://m.airchina.com.cn/ac/";
//		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, acUrl, "other");
//		String result = httpVo.getHttpResult();
//		String TY_SESSION_ID = "TY_SESSION_ID="+UUID.randomUUID().toString();
//		headerMap.remove("Upgrade-Insecure-Requests");
//		headerMap.put("Accept","application/json, text/javascript, */*; q=0.01");
//		headerMap.put("X-Requested-With","XMLHttpRequest");
//		headerMap.put("Referer",acUrl);
//		headerMap.put("Cookie",TY_SESSION_ID);
//		headerMap.put("Origin","https://m.airchina.com.cn");
//		headerMap.put("Content-Type","application/json; charset=UTF-8");
//		String param = "{\"m\":{\"req\":\"{\\\"flag\\\":\\\"2\\\",\\\"cityName\\\":\\\"\\\"}\",\"token\":\"h5001\",\"lang\":\"zh_CN\",\"userInfo1\":\"50700\"},\"a\":\"13\",\"p\":\"87\"}";
//		String jsonUrl = "https://m.airchina.com.cn/ac/c/invoke.json";
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, jsonUrl,param,"other");
//		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
//		headerMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//		headerMap.put("Upgrade-Insecure-Requests","1");
//		headerMap.put("Cookie",TY_SESSION_ID+";"+JSESSIONID+";s_sess=%20s_cc%3Dtrue%3B%20s_sq%3Dacna-nh5-prd%253D%252526pid%25253D%252525E9%252525A6%25252596%252525E9%252525A1%252525B5%252526pidt%25253D1%252526oid%25253Dfunctiononclick%25252528event%25252529%2525257Bhref%25252528%25252527%2525252Fac%2525252Fc%2525252Finvoke%2525252FqryFlights%25252540pg%25252527%25252529%2525257D%252526oidt%25253D2%252526ot%25253DDIV%3B");
//		headerMap.remove("X-Requested-With");
//		headerMap.remove("Content-Type");
//		headerMap.remove("Origin");
//		String pgUrl = "https://m.airchina.com.cn/ac/c/invoke/qryFlights@pg";
//		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,pgUrl,"other");
//		String res = httpVo.getHttpResult();
//		System.out.println(res);
//		headerMap.put("Cache-Control","max-age=0");
//		headerMap.put("Content-Type","application/x-www-form-urlencoded");
//		headerMap.put("Referer",pgUrl);
//		headerMap.put("Origin","https://m.airchina.com.cn");
//		Map<String, Object> paramMap = new HashMap<String, Object>();
//		String timeStamp = String.valueOf(System.currentTimeMillis()).substring(0, 10);
//		String req = "{\"req\":\"{\\\"backDate\\\":\\\"\\\",\\\"version\\\":\\\"4\\\",\\\"org\\\":\\\"CAN\\\",\\\"dst\\\":\\\"CKG\\\",\\\"timestamp\\\":\\\""+timeStamp+"\\\",\\\"flag\\\":\\\"0\\\",\\\"inf\\\":\\\"0\\\",\\\"geeflag\\\":\\\"qryFlights\\\",\\\"date\\\":\\\"2019-03-05\\\",\\\"date1\\\":\\\"\\\",\\\"date2\\\":\\\"\\\",\\\"adt\\\":\\\"1\\\",\\\"cnn\\\":\\\"0\\\",\\\"cabin\\\":\\\"Economy\\\"}\",\"token\":\"h5001\",\"lang\":\"zh_CN\",\"userID\":\"\",\"infoID\":\"\",\"userInfo1\":\"51200\",\"checkToken\":null,\"geetest\":\"false\"}";
//		paramMap.put("a","3");
//		paramMap.put("p","1");
//		paramMap.put("m",req);
//		paramMap.put("rw","1");
//		paramMap.put("backDate","");
//		paramMap.put("version","4");
//		paramMap.put("org","CAN");
//		paramMap.put("dst","CKG");
//		paramMap.put("timestamp",timeStamp);
//		paramMap.put("flag","0");
//		paramMap.put("inf","0");
//		paramMap.put("geeflag","qryFlights");
//		paramMap.put("date","2019-03-05");
//		paramMap.put("backDate","");
//		paramMap.put("org1City","");
//		paramMap.put("dst1City","");
//		paramMap.put("date1","");
//		paramMap.put("org2City","");
//		paramMap.put("dst2City","");
//		paramMap.put("date2","");
//		paramMap.put("adt","1");
//		paramMap.put("cnn","0");
//		paramMap.put("cabin","Economy");
//		
//		String param1 = "a=3&p=1&m=%7B%22req%22%3A%22%7B%5C%22backDate%5C%22%3A%5C%22%5C%22%2C%5C%22version%5C%22%3A%5C%224%5C%22%2C%5C%22org%5C%22%3A%5C%22CAN%5C%22%2C%5C%22dst%5C%22%3A%5C%22PEK%5C%22%2C%5C%22timestamp%5C%22%3A%5C%22"+timeStamp+"%5C%22%2C%5C%22flag%5C%22%3A%5C%220%5C%22%2C%5C%22inf%5C%22%3A%5C%220%5C%22%2C%5C%22geeflag%5C%22%3A%5C%22qryFlights%5C%22%2C%5C%22date%5C%22%3A%5C%222019-03-02%5C%22%2C%5C%22date1%5C%22%3A%5C%22%5C%22%2C%5C%22date2%5C%22%3A%5C%22%5C%22%2C%5C%22adt%5C%22%3A%5C%221%5C%22%2C%5C%22cnn%5C%22%3A%5C%220%5C%22%2C%5C%22cabin%5C%22%3A%5C%22Economy%5C%22%7D%22%2C%22token%22%3A%22h5001%22%2C%22lang%22%3A%22zh_CN%22%2C%22userID%22%3A%22%22%2C%22infoID%22%3A%22%22%2C%22userInfo1%22%3A%2251200%22%2C%22checkToken%22%3Anull%2C%22geetest%22%3A%22false%22%7D&rw=1&backDate=&version=4&org=CAN&dst=PEK&timestamp="+timeStamp+"&flag=0&inf=0&geeflag=qryFlights&orgCity=%E5%B9%BF%E5%B7%9E+%28CAN%29&dstCity=%E5%8C%97%E4%BA%AC+%28PEK%29&date=2019-03-02&backDate=&org1City=&dst1City=&date1=&org2City=&dst2City=&date2=&adt=1&cnn=0&cabin=Economy";
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "https://m.airchina.com.cn/ac/c/invoke",param1,"other");
//		String httpResult = httpVo.getHttpResult();
//		System.out.println(httpResult);
//		return httpResult;
		
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headerMap.put("Accept-Encoding","gzip, deflate, br");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Connection","keep-alive");
		headerMap.put("Accept-Language","zh-CN,zh;q=0.9");
		headerMap.put("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
		headerMap.put("Host", "m.airchina.com.cn");
		httpClientSessionVo.setHeaderMap(headerMap);
		String acUrl = "https://m.airchina.com.cn/ac/c/invoke/qryFlights@pg";
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, acUrl, "other");
		String result = httpVo.getHttpResult();
		headerMap.remove("Upgrade-Insecure-Requests");
		headerMap.put("Accept","application/json, text/javascript, */*; q=0.01");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Referer",acUrl);
		headerMap.put("Origin","https://m.airchina.com.cn");
		headerMap.put("Content-Type","application/json; charset=UTF-8");
		String param = "{\"m\":{\"req\":\"{\\\"type\\\":\\\"8\\\",\\\"lang\\\":\\\"zh_CN\\\"}\",\"type\":\"8\",\"lang\":\"zh_CN\",\"userInfo1\":\"51300\"},\"a\":\"24\",\"p\":\"289\"}";
		String jsonUrl = "https://m.airchina.com.cn/ac/c/invoke.json";
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, jsonUrl,param,"other");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String TY_SESSION_ID = "TY_SESSION_ID="+UUID.randomUUID().toString();
		Long timeStamp = System.currentTimeMillis();
		Long timeS2 = timeStamp +63158400000L;
		String timeS3 = String.valueOf(timeStamp).substring(0, 10);
		String S_PERS = "s_pers=%20s_fid%3D51769EA85F2910FA-05FE9C2AF0826748%7C"+timeS2+"%3B";
		String S_SESS = "s_sess=%20s_cc%3Dtrue%3B%20s_sq%3Dacna-nh5-prd%253D%252526pid%25253DBooking1-%252525E6%2525259C%252525BA%252525E7%252525A5%252525A8%252525E9%252525A2%25252584%252525E8%252525AE%252525A2%252525E9%252525A6%25252596%252525E9%252525A1%252525B5%252526pidt%25253D1%252526oid%25253Dfunctiononclick%25252528event%25252529%2525257Bthisblur%25252528this%25252529%2525257D%252526oidt%25253D2%252526ot%25253DTEXT%3B";
		String req = "a=3&p=1&m=%7B%22req%22%3A%22%7B%5C%22backDate%5C%22%3A%5C%22%5C%22%2C%5C%22version%5C%22%3A%5C%224%5C%22%2C%5C%22org%5C%22%3A%5C%22CTU%5C%22%2C%5C%22dst%5C%22%3A%5C%22PEK%5C%22%2C%5C%22timestamp%5C%22%3A%5C%22"+timeS3+"%5C%22%2C%5C%22flag%5C%22%3A%5C%220%5C%22%2C%5C%22inf%5C%22%3A%5C%220%5C%22%2C%5C%22geeflag%5C%22%3A%5C%22qryFlights%5C%22%2C%5C%22date%5C%22%3A%5C%222019-03-08%5C%22%2C%5C%22date1%5C%22%3A%5C%22%5C%22%2C%5C%22date2%5C%22%3A%5C%22%5C%22%2C%5C%22adt%5C%22%3A%5C%221%5C%22%2C%5C%22cnn%5C%22%3A%5C%220%5C%22%2C%5C%22cabin%5C%22%3A%5C%22Economy%5C%22%7D%22%2C%22token%22%3A%22h5001%22%2C%22lang%22%3A%22zh_CN%22%2C%22userID%22%3A%22%22%2C%22infoID%22%3A%22%22%2C%22userInfo1%22%3A%2251200%22%2C%22checkToken%22%3Anull%2C%22geetest%22%3A%22false%22%7D&rw=1&backDate=&version=4&org=CTU&dst=PEK&timestamp="+timeS3+"&flag=0&inf=0&geeflag=qryFlights&orgCity=%E6%88%90%E9%83%BD+%28CTU%29&dstCity=%E5%8C%97%E4%BA%AC+%28PEK%29&date=2019-03-08&backDate=&org1City=&dst1City=&date1=&org2City=&dst2City=&date2=&adt=1&cnn=0&cabin=Economy";
		String cookies = TY_SESSION_ID+";"+JSESSIONID+";"+S_SESS+";"+S_PERS;
		
		Map<String, String> headerMap1 = new HashMap<String, String>();
		headerMap1.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headerMap1.put("Cache-Control","max-age=0");
		headerMap.put("Accept-Encoding","gzip, deflate, br");
		headerMap1.put("Accept-Language","zh-CN,zh;q=0.9");
		headerMap1.put("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
		headerMap1.put("Cookie",cookies);
		headerMap1.put("Content-Type","application/x-www-form-urlencoded");
		headerMap1.put("Origin","https://m.airchina.com.cn");
		headerMap1.put("Referer",acUrl);
		headerMap1.put("Upgrade-Insecure-Requests","1");
		headerMap1.put("Host", "m.airchina.com.cn");
		headerMap1.put("Connection","keep-alive");
		Map<String, String> paramMap = new HashMap<String, String>();
		String req1 = "{\"req\":\"{\\\"backDate\\\":\\\"\\\",\\\"version\\\":\\\"4\\\",\\\"org\\\":\\\"CAN\\\",\\\"dst\\\":\\\"PEK\\\",\\\"timestamp\\\":\\\""+timeS3+"\\\",\\\"flag\\\":\\\"0\\\",\\\"inf\\\":\\\"0\\\",\\\"geeflag\\\":\\\"qryFlights\\\",\\\"date\\\":\\\"2019-03-10\\\",\\\"date1\\\":\\\"\\\",\\\"date2\\\":\\\"\\\",\\\"adt\\\":\\\"1\\\",\\\"cnn\\\":\\\"0\\\",\\\"cabin\\\":\\\"Economy\\\"}\",\"token\":\"h5001\",\"lang\":\"zh_CN\",\"userID\":\"\",\"infoID\":\"\",\"userInfo1\":\"51200\",\"checkToken\":null,\"geetest\":\"false\"}";
		paramMap.put("a","3");
		paramMap.put("p","1");
		paramMap.put("m",req1);
		paramMap.put("rw","1");
		paramMap.put("backDate","");
		paramMap.put("version","4");
		paramMap.put("org","CAN");
		paramMap.put("dst","PEK");
		paramMap.put("timestamp",timeS3);
		paramMap.put("flag","0");
		paramMap.put("inf","0");
		paramMap.put("geeflag","qryFlights");
		paramMap.put("date","2019-03-10");
		paramMap.put("backDate","");
		paramMap.put("org1City","");
		paramMap.put("dst1City","");
		paramMap.put("date1","");
		paramMap.put("org2City","");
		paramMap.put("dst2City","");
		paramMap.put("date2","");
		paramMap.put("adt","1");
		paramMap.put("cnn","0");
		paramMap.put("cabin","Economy");
		ProxyVo proxyVo = ProxyUtil.getProxy(threadMark);
		String proxyIp = null == proxyVo ? null : proxyVo.getProxyIp();
		Integer proxyPort = null == proxyVo ? null : proxyVo.getProxyPort();
		String res = HttpUtil.doPost("https://m.airchina.com.cn/ac/c/invoke", headerMap1,paramMap,proxyIp,proxyPort);
		return res;
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
	

	@Override
	public String httpResult() throws Exception {
		return null;
	}
}

