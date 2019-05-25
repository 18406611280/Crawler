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
 * 深圳M端
 * @author chenminghong
 */
public class B2CZH_MCrawler extends Crawler {

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
	public B2CZH_MCrawler(String threadMark) {
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
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept","application/json, text/javascript, */*; q=0.01");
		headerMap.put("Accept-Encoding","gzip, deflate");
		headerMap.put("Connection","keep-alive");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Origin","http://m.shenzhenair.com");
		headerMap.put("Referer","http://m.shenzhenair.com/webresource-micro/");
		headerMap.put("Accept-Language","zh-CN,zh;q=0.9");
		headerMap.put("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
		headerMap.put("Host", "m.shenzhenair.com");
		StringBuilder sb = new StringBuilder();
		sb.append("sajssdk_2015_cross_new_user=1;sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%221697a1b4b3a32b-0188fc37d6e69d-2d604637-304500-1697a1b4b3c253%22%2C%22%24device_id%22%3A%221697a1b4b3a32b-0188fc37d6e69d-2d604637-304500-1697a1b4b3c253%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%9B%B4%E6%8E%A5%E6%B5%81%E9%87%8F%22%2C%22%24latest_referrer%22%3A%22%22%2C%22%24latest_referrer_host%22%3A%22%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC_%E7%9B%B4%E6%8E%A5%E6%89%93%E5%BC%80%22%7D%7D");
		headerMap.put("Cookie", sb.toString());
		httpClientSessionVo.setHeaderMap(headerMap);
		String acUrl = "http://m.shenzhenair.com/weixin_front/books.do?method=getCitys";
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, acUrl,"","other");
		
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		sb.append(";").append(JSESSIONID);
		headerMap.put("Cookie", sb.toString());
		headerMap.put("Accept","application/json, text/javascript, */*; q=0.01");
		headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		headerMap.put("Referer","http://m.shenzhenair.com/webresource-micro/queryFlights.html");
		String param = "request=%7B%22depdate%22%3A%222019-03-16%22%2C%22hctype%22%3A%22DC%22%2C%22orgcity%22%3A%22SZX%22%2C%22dstcity%22%3A%22PEK%22%2C%22depdate2%22%3A%22%22%7D";
		String queryUrl = "http://m.shenzhenair.com/weixin_front/books.do?method=queryFlightsView";
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, queryUrl,param,"other");
		String result = httpVo.getHttpResult();
		return result;
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


