package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 香港航空  mobile web
 */
public class B2CHXCrawler2 extends Crawler {
															
	private final static String owQueryUrl = "https://m.hongkongairlines.com/ci/index.php/fffticket/search_new?startcity=%depCode%&endcity=%desCode%&date=%depDate%&flighttype=OW&cabintype=E&adultnum=1&childnum=0";
	
	private final static String rtQueryUrl = "https://m.hongkongairlines.com/ci/index.php/fffticket/search_fc_new3?startcity=%depCode%&endcity=%desCode%&date=%depDate%&flighttype=RT&cabintype=E&adultnum=1&childnum=0&date2=%backDate%";

	
	
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	public B2CHXCrawler2(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		
		
		String httpResult =this.httpResult();// this.httpGetResult(this.getFlightUrl());
		if(this.isTimeout()) return null;
		
		System.out.println(httpResult);
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object objMsg = mapResult.get("airItems");
			if(null == objMsg) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			//单程
			if(StringUtils.isEmpty(this.getJobDetail().getBackDate())) {
				crawlResults = this.owFlight(crawlResults,mapResult);
			}else {//往返
				crawlResults = this.rtFlight(crawlResults,mapResult);
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}
	
	/**
	 * 直达航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> owFlight(List<CrawlResultBase> crawlResults,Map<String, Object> mapResult) {
		try {
			CrawlInterResult crawlResult = null;
			List<Map<String, Object>> airItems = (List<Map<String, Object>>)mapResult.get("airItems");
			for(Map<String, Object> airItem : airItems){
				List<Map<String, Object>> cabins = (List<Map<String, Object>>)airItem.get("cabins");
				for(Map<String, Object> cabin : cabins){
					String takeoffDateTime = cabin.get("takeoffDateTime").toString();
					String arrivalDateTime = cabin.get("arrivalDateTime").toString();
					String airline = cabin.get("airline").toString();
					String operateCarrier = cabin.get("operateCarrier").toString();
					String cabinNum = cabin.get("cabinNum").toString();
					String cabinCode = cabin.get("cabinCode").toString();
					Map<String, Object> wsFare = (Map<String, Object>)cabin.get("wsFare");
					String currencyType = wsFare.get("currencyType").toString();
					String baseAmount = wsFare.get("baseAmount").toString();
					
					// 判断共享
					String needShareFlightStr = this.getTimerJob().getParamMapValueByKey("needShareFlight");
					String shareFlight = this.getShareFlight(airline);
					if("Y".equalsIgnoreCase(shareFlight) && !"Y".equalsIgnoreCase(needShareFlightStr)) {
						logger.debug(this.getJobDetail().toStr() + ", 共享航班[" + operateCarrier + "], 忽略!");
						continue ;
					}
					//计算座位数
					int remainSiteGo = CrawlerUtil.getCabinAmount(cabinNum);
					if(remainSiteGo <= 0) continue ;
					
					if(!this.allowCabin(cabinCode)) continue ;	// 排除舱位
					
					crawlResult = new CrawlInterResult(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate());
					crawlResult.setFltNo(operateCarrier);
					crawlResult.setAirlineCode(airline);
					crawlResult.setDepTime(takeoffDateTime.substring(11, 16));
					crawlResult.setDesTime(arrivalDateTime.substring(11, 16));
					crawlResult.setRemainSite(remainSiteGo);
					crawlResult.setShareFlight(shareFlight);
					BigDecimal rate = CurrencyUtil.getRequest3(currencyType, "CNY");
					if(rate.compareTo(BigDecimal.ZERO)==0){
						crawlResult.setCurrency(currencyType);
						crawlResult.setTicketPrice(new BigDecimal(baseAmount));
					}else{
						crawlResult.setCurrency("CNY");
						BigDecimal ticketPrice = new BigDecimal(baseAmount).multiply(rate).setScale(0,BigDecimal.ROUND_UP);
						crawlResult.setTicketPrice(ticketPrice);
					}
					crawlResult.setCabin(cabinCode);							
					crawlResults.add(crawlResult);
				}
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
		}
		return crawlResults;
	}
	/**
	 * 往返航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> rtFlight(List<CrawlResultBase> crawlResults,Map<String, Object> mapResult) {
		try {
			List<Map<String, Object>> airItems = (List<Map<String, Object>>)mapResult.get("airItems");
			CrawlInterResult crawlResult = null;
			for(Map<String, Object> airItem : airItems){
				List<Map<String, Object>> cabins = (List<Map<String, Object>>)airItem.get("cabins");
				for(Map<String, Object> cabin : cabins){
					String takeoffDateTime = cabin.get("takeoffDateTime").toString();
					String arrivalDateTime = cabin.get("arrivalDateTime").toString();
					String airline = cabin.get("airline").toString();
					String operateCarrier = cabin.get("operateCarrier").toString();
					String cabinNum = cabin.get("cabinNum").toString();
					String cabinCode = cabin.get("cabinCode").toString();
					Map<String, Object> wsFare = (Map<String, Object>)cabin.get("wsFare");
					String currencyType = wsFare.get("currencyType").toString();
					String baseAmount = wsFare.get("baseAmount").toString();

					// 判断共享
					String needShareFlightStr = this.getTimerJob().getParamMapValueByKey("needShareFlight");
					String shareFlight = this.getShareFlight(airline);

					if("Y".equalsIgnoreCase(shareFlight) && !"Y".equalsIgnoreCase(needShareFlightStr)) {
						logger.debug(this.getJobDetail().toStr() + ", 共享航班[" + operateCarrier + "], 忽略!");
						continue;
					}
					//计算座位数
					int remainSiteGo = CrawlerUtil.getCabinAmount(cabinNum);
					if(remainSiteGo <= 0) continue ;
					if(!this.allowCabin(cabinCode)) continue ;	// 排除舱位

					List<Map<String, Object>> returnSegments =  (List<Map<String, Object>>)cabin.get("returnSegments");
					for(Map<String, Object> returnSegment : returnSegments){
						crawlResult = new CrawlInterResult(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate(), this.getJobDetail().getBackDate());
						crawlResult.setFltNo(operateCarrier);
						crawlResult.setAirlineCode(airline);
						crawlResult.setDepTime(takeoffDateTime.substring(11, 16));
						crawlResult.setDesTime(arrivalDateTime.substring(11, 16));
						crawlResult.setRemainSite(remainSiteGo);
						crawlResult.setShareFlight(shareFlight);
						BigDecimal rate = CurrencyUtil.getRequest3(currencyType, "CNY");
						if(rate.compareTo(BigDecimal.ZERO)==0){
							crawlResult.setCurrency(currencyType);
							crawlResult.setTicketPrice(new BigDecimal(baseAmount));
						}else{
							crawlResult.setCurrency("CNY");
							BigDecimal ticketPrice = new BigDecimal(baseAmount).multiply(rate).setScale(0,BigDecimal.ROUND_UP);
							crawlResult.setTicketPrice(ticketPrice);
						}
						crawlResult.setCabin(cabinCode);

						String rtakeoffDateTime = returnSegment.get("takeoffDateTime").toString();
						String rarrivalDateTime = returnSegment.get("arrivalDateTime").toString();
						String rairline = returnSegment.get("airline").toString();
						String roperateCarrier = returnSegment.get("operateCarrier").toString();
						Map<String, Object> rcabin = (Map<String, Object>)returnSegment.get("cabin");
						String rcabinNum = rcabin.get("cabinNum").toString();
						String rcabinCode = rcabin.get("cabinCode").toString();
						Map<String, Object> rwsFare = (Map<String, Object>)rcabin.get("wsFare");
						String rbaseAmount = rwsFare.get("salePrice").toString();

						String rshareFlight = this.getShareFlight(rairline);

						if("Y".equalsIgnoreCase(rshareFlight) && !"Y".equalsIgnoreCase(needShareFlightStr)) {
							logger.debug(this.getJobDetail().toStr() + ", 共享航班[" + roperateCarrier + "], 忽略!");
							continue ;
						}
						//计算座位数
						int rremainSiteGo = CrawlerUtil.getCabinAmount(rcabinNum);
						if(rremainSiteGo <= 0) continue ;
						if(!this.allowCabin(rcabinCode)) continue ;	// 排除舱位

						crawlResult.setBackCabin(rcabinCode);
						crawlResult.setBackFltNo(roperateCarrier);
						crawlResult.setBackDepTime(rtakeoffDateTime.substring(11, 16));
						crawlResult.setBackDesTime(rarrivalDateTime.substring(11, 16));
						crawlResult.setBackRemainSite(rremainSiteGo);
						
						crawlResult.setBackShareFlight(rshareFlight);
						
						if(rate.compareTo(BigDecimal.ZERO)==0){
							crawlResult.setBackTicketPrice(new BigDecimal(rbaseAmount));
						}else{
							crawlResult.setCurrency("CNY");
							BigDecimal ticketPrice = new BigDecimal(rbaseAmount).multiply(rate).setScale(0,BigDecimal.ROUND_UP);
							crawlResult.setBackTicketPrice(ticketPrice);
						}
						crawlResults.add(crawlResult);
					}
				}
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
		}

		return crawlResults;
	}

	/**
	 * 获取请求内容
	 * @param url
	 * @param cookieRemark
	 * @param parentCookieRemark
	 * @return
	 */
	private String httpGetResult(String url) {
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		return this.httpProxyGet(httpClientSessionVo, url, "json");
	}
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document.title().contains("404 Page Not Found")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}

	/**
	 * 获取url
	 * @return
	 */
	private String getFlightUrl() {
		String flihgtUrl = "";
		if(StringUtils.isEmpty(this.getJobDetail().getBackDate())){
			flihgtUrl = owQueryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
					.replaceAll("%desCode%", this.getJobDetail().getDesCode())
					.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		}else{
			flihgtUrl = rtQueryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
					.replaceAll("%desCode%", this.getJobDetail().getDesCode())
					.replaceAll("%depDate%", this.getJobDetail().getDepDate())
					.replaceAll("%backDate%", this.getJobDetail().getBackDate());
		}
		return flihgtUrl;
	}

	@Override
	public String httpResult() throws Exception {
//		Map<String,Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("ai.cc", "2001");
//		paramMap.put("ai.cp", "2b504aadcc87a325fe190566d96257c0");
//		paramMap.put("ai.store", "yyb");
//		paramMap.put("bi.mi", "NMAf2badc0a9c686b43cb49a82e6c21feff");
//		paramMap.put("bi.mFlag", "01");
//		paramMap.put("bi.ctoken", "NMAf2badc0a9c686b43cb49a82e6c21feff");
//		paramMap.put("bi.dv", "Android");
//		paramMap.put("bi.ctype", "app");
//		paramMap.put("bi.lan", "CN");
//		paramMap.put("bi.ov", "4.4.2");
//		paramMap.put("bi.dm", "GT-I8262D");
//		paramMap.put("bi.dn", "t03gzc");
//		paramMap.put("bi.cl", "(98.5639624247689,37.00121614057693)");
//		paramMap.put("bi.cln", "香港");
//		paramMap.put("bi.av", "3.3.0");
//		paramMap.put("bi.sid", "563d4285aa25a7dd69b5939cb15fb818");
//		paramMap.put("orgCity", "HKG");
//		paramMap.put("dstCity", "BKK");
//		paramMap.put("tripType", "OW");
//		paramMap.put("takeoffDate", "2017-01-06");
//		paramMap.put("returnDate", "2017-01-09");
//		paramMap.put("seatClass", "E");
//		paramMap.put("adultNum", "1");
//		paramMap.put("childNum", "0");
//		paramMap.put("infantNum", "0");
//		paramMap.put("newVersion", "1");
//		paramMap.put("sign", "CJ0sFf9j6MJYTe56XVbcWTeOQDA=");
//			
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClientCert();
//		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
//		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
//		
//		Map<String, Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("Accept-Encoding", "gzip");
//		headerMap.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-cn; GT-I8262D Build/KOT49H) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
//		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
//		headerMap.put("Connection", "Keep-Alive");
//		headerMap.put("Host", "aio.hkairlines.com");
//		httpClientSessionVo.setHeaderMap(headerMap);
//		
//		String result =  this.httpProxyPost(httpClientSessionVo, "https://aio.hkairlines.com/ac3s/flight/query", paramMap, "json");
//		System.out.println(result);
		
		
		//测试5J
		
		String url = "https://cebu-booking.ink-global.com/api/tenants/cebu/availability;ADT=1?from=HKG&to=MNL&departureDate=2017-01-11";
		String httpContent = "{\"key\":\"2KtfdXanHgjFvRNET9\"}";
		SimpleDateFormat SDF_YMDTHMS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String date = SDF_YMDTHMS.format(new Date());
		String temporal = Base64.encodeBase64String(date.getBytes());
		
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClientCert();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		
		headerMap.put("Accept", "application/vnd.inkglobal.flights.v1+json");
		headerMap.put("Accept-Encoding", "gzip");
		headerMap.put("User-Agent", "Android:1.2.71");
		headerMap.put("Content-Type", "application/json; charset=utf-8");
		headerMap.put("X-ink-mac", "bMI79Xehmedc5VGygeBAIFd3jWln7UPhpkT1Ool9ZRc=");
		headerMap.put("X-ink-temporal", temporal);
		
		headerMap.put("Connection", "Keep-Alive");
		headerMap.put("Host", "cebu-booking.ink-global.com");
		
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String result = this.httpProxyPost(httpClientSessionVo, url, httpContent, "json");
		
		System.out.println(result);
		
		return "";
	}
	
	public static void main(String[] args) {
		
	}
}