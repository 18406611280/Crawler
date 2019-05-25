package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.MyStringUtil;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 马来西亚航空
 * @author chenminghong
 */
public class B2CMHCrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	private final static Map<String,String> dateMap = new HashMap<String,String>();
	
	public B2CMHCrawler(String threadMark) {
		super(threadMark);
	}
	
	static{
		dateMap.put("Jan", "01");
		dateMap.put("Feb", "02");
		dateMap.put("Mar", "03");
		dateMap.put("Apr", "04");
		dateMap.put("May", "05");
		dateMap.put("Jun", "06");
		dateMap.put("Jul", "07");
		dateMap.put("Aug", "08");
		dateMap.put("Sep", "09");
		dateMap.put("Oct", "10");
		dateMap.put("Nov", "11");
		dateMap.put("Dec", "12");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(httpResult==null) return crawlResults;
		try {
			JSONObject parse = JSONObject.parseObject(httpResult);
			JSONObject pageConfig = parse.getJSONObject("pageDefinitionConfig");
			String title = pageConfig.getString("title");
			if(title.contains("错误") || title.contains("日历")) return crawlResults;
			JSONObject pageData = pageConfig.getJSONObject("pageData");
			JSONObject business = pageData.getJSONObject("business");
			JSONObject Availability = business.getJSONObject("Availability");
			JSONObject proposedBound = Availability.getJSONArray("proposedBounds").getJSONObject(0);
			JSONArray proposedFlightsGroup = proposedBound.getJSONArray("proposedFlightsGroup");
			int size = proposedFlightsGroup.size();
			if(size==0) return crawlResults;
			Map<String,List<FlightSegment>> flightMap = new HashMap<String,List<FlightSegment>>();
			for(int i = 0; i<size; i++){
				JSONObject flight = proposedFlightsGroup.getJSONObject(i);
				String flightId = flight.getString("proposedBoundId");
				JSONArray segments = flight.getJSONArray("segments");
				int flightSize = segments.size();
				List<FlightSegment> flights = new ArrayList<FlightSegment>();
				for(int j=0; j<flightSize; j++){
					FlightSegment fliment = new FlightSegment();
					JSONObject fli = segments.getJSONObject(j);
					fliment.setTripNo(j+1);
					String flightEN = fli.getJSONObject("airline").getString("code");
					String beginDate = fli.getString("beginDate");//Jan 17, 2019 4:55:00 PM
					String beyear = beginDate.substring(beginDate.indexOf(",")+2,beginDate.indexOf(",")+6);
					String bemouth = dateMap.get(beginDate.substring(0,3));
					String beri = getRi(beginDate.substring(4,beginDate.indexOf(",")));
					String beTime = getTime(beginDate.substring(beginDate.indexOf(",")+7));
					String depTime = beyear+"-"+bemouth+"-"+beri+" "+beTime;//出发日期
					String endDate = fli.getString("endDate");
					String endyear = endDate.substring(endDate.indexOf(",")+2,endDate.indexOf(",")+6);
					String endmouth = dateMap.get(endDate.substring(0,3));
					String endri = getRi(endDate.substring(4,endDate.indexOf(",")));
					String endTime = getTime(endDate.substring(endDate.indexOf(",")+7));
					String desTime = endyear+"-"+endmouth+"-"+endri+" "+endTime;//到达日期
					
					fliment.setFlightNumber(flightEN+fli.getString("flightNumber"));
					fliment.setAirlineCode(flightEN);
					fliment.setDepAirport(fli.getJSONObject("beginLocation").getString("locationCode"));
					fliment.setArrAirport(fli.getJSONObject("endLocation").getString("locationCode"));
					fliment.setDepTime(depTime);
					fliment.setArrTime(desTime);
					flights.add(fliment);
				}
				flightMap.put(flightId, flights);
			}
			JSONArray recommendations = Availability.getJSONArray("recommendations");
			int priceSize = recommendations.size();
			for(int p = 0; p<priceSize; p++){
				JSONObject price = recommendations.getJSONObject(p);
				JSONObject reco = price.getJSONArray("recoAmountList").getJSONObject(0);
				JSONObject recoAmount = reco.getJSONObject("recoAmount");
				String ticketPrice = recoAmount.getString("amountWithoutTax");
				String tax = recoAmount.getString("tax");
				String currency = recoAmount.getJSONObject("currency").getString("code");
				BigDecimal rate = rateMap.get(currency);
				if("CNY".equals(currency)){
					rateMap.put(currency, new BigDecimal(1));
					rate = new BigDecimal(1);
				}
				else if("NPR".equals(currency)){
					rateMap.put(currency, new BigDecimal(0.061));
					rate = new BigDecimal(0.061);
				}else{
					if(rate==null || rate.intValue()==0){
						rate = CurrencyUtil.getRequest3(currency, "CNY");
					}
					rateMap.put(currency, rate);
				}
				BigDecimal cnyPrice = new BigDecimal(ticketPrice).multiply(rate).setScale(2,BigDecimal.ROUND_UP);
				BigDecimal cnyTaxPrice = new BigDecimal(tax).multiply(rate).setScale(2,BigDecimal.ROUND_UP);
				//封装价格
				List<FlightPrice> prices = new ArrayList<FlightPrice>();
				FlightPrice fPrice = new FlightPrice();
				fPrice.setCurrency("CNY");
				fPrice.setEquivCurrency("CNY");
				fPrice.setEquivFare(cnyPrice.toString());
				fPrice.setFare(cnyPrice.toString());
				fPrice.setEquivTax(cnyTaxPrice.toString());
				fPrice.setTax(cnyTaxPrice.toString());
				fPrice.setPassengerType("ADT");
				prices.add(fPrice);
				
				JSONObject bound = price.getJSONArray("bounds").getJSONObject(0);
				JSONArray group = bound.getJSONArray("flightGroupList");
				int groupSize = group.size();
				for(int g=0; g<groupSize; g++){
					FlightData flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				    flightData.setAirlineCode("MH");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
				    flightData.setPrices(prices);
					JSONObject flight = group.getJSONObject(g);
					String flightId = flight.getString("flightId");
					String cabinNum = flight.getString("numberOfSeatsLeft");
					String rbd = flight.getString("rbd");
					List<FlightSegment> fliSeg = flightMap.get(flightId);
					List<FlightSegment> newFli = new ArrayList<FlightSegment>();
					for(FlightSegment fs: fliSeg){
						FlightSegment newfs = new FlightSegment();
						newfs.setFlightNumber(fs.getFlightNumber());
						newfs.setAirlineCode(fs.getAirlineCode());
						newfs.setDepAirport(fs.getDepAirport());
						newfs.setArrAirport(fs.getArrAirport());
						newfs.setDepTime(fs.getDepTime());
						newfs.setArrTime(fs.getArrTime());
						newfs.setCabin(rbd);
						newfs.setCabinCount(cabinNum);
						newFli.add(newfs);
					}
					flightData.setFromSegments(newFli);
					crawlResults.add(flightData);
				}
				
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	public String httpResult() throws Exception {
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		String signUrl = "https://www.malaysiaairlines.com/cn/zh_CN.html";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.malaysiaairlines.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
		if(httpVo==null) return null;
		String cfduid = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "__cfduid=");
		StringBuilder cookie = new StringBuilder();
		cookie.append(cfduid);
		String listUrl = "https://www.malaysiaairlines.com/bin/services/new/getCountrylistJSON";
		headerMap.put("Accept", "*/*");
		headerMap.remove("Upgrade-Insecure-Requests");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Referer",signUrl);
		headerMap.put("Cookie",cookie);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,listUrl,"other");
		if(httpVo==null) return null;
		String AWSELB = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "AWSELB=");
		cookie.append(";").append(AWSELB);
		headerMap.put("Cookie",cookie);
		headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		String ibeUrl = "https://www.malaysiaairlines.com/bin/services/new/ibe";
		String param = "url=ARRANGE_BY%3DR%26BOOKING_FLOW%3DREVENUE%26B_ANY_TIME_1%3DTRUE%26B_ANY_TIME_2%3DTRUE%26B_DATE_1%3D"+depDate.replace("-", "")+"1200%26B_DATE_2%3D%26B_LOCATION_1%3D"+dep+"%26B_LOCATION_2%3D"+des+"%26COMMERCIAL_FARE_FAMILY_1%3DCFFECO%26COMMERCIAL_FARE_FAMILY_2%3D%26COMMERCIAL_FARE_FAMILY_3%3D%26DATE_RANGE_QUALIFIER_1%3DC%26DATE_RANGE_QUALIFIER_2%3DC%26DATE_RANGE_VALUE_1%3D3%26DATE_RANGE_VALUE_2%3D3%26DISPLAY_TYPE%3D2%26EMBEDDED_TRANSACTION%3DFlexPricerAvailability%26EXTERNAL_ID%3DBOOKING%26E_LOCATION_1%3D"+des+"%26E_LOCATION_2%3D"+dep+"%26HAS_INFANT_1%3DFALSE%26HAS_INFANT_2%3DFALSE%26HAS_INFANT_3%3DFALSE%26HAS_INFANT_4%3DFALSE%26HAS_INFANT_5%3DFALSE%26HAS_INFANT_6%3DFALSE%26HAS_INFANT_7%3DFALSE%26HAS_INFANT_8%3DFALSE%26HAS_INFANT_9%3DFALSE%26PRICING_TYPE%3DO%26REFRESH%3D0%26SITE%3Ddesktop%26TRAVELLER_TYPE_1%3DADT%26TRAVELLER_TYPE_2%3D%26TRAVELLER_TYPE_3%3D%26TRAVELLER_TYPE_4%3D%26TRAVELLER_TYPE_5%3D%26TRAVELLER_TYPE_6%3D%26TRAVELLER_TYPE_7%3D%26TRAVELLER_TYPE_8%3D%26TRAVELLER_TYPE_9%3D%26TRIP_FLOW%3DYES%26TRIP_TYPE%3DO%26ADULT_COUNT%3D1%26CHILD_COUNT%3D0%26INFANT_COUNT%3D0%26param_cug%3D%26RDM_MEM_ID%3D%26RDM_MEM_LN%3D%26RDM_MEM_PIN%3D%26RDM_PAYMENT%3Dcash%26SO_GL%3D";
		String ibeResult = this.httpProxyPost(httpClientSessionVo,ibeUrl,param,"other");
		if(ibeResult==null) return null;
		String SITE = MyStringUtil.getValue("SITE=", "&", ibeResult);
		String ENC = MyStringUtil.getValue("ENC=", "\"", ibeResult);
		String searchUrl = "https://fly.malaysiaairlines.com/plnext/MASAirways/Override.action";
		String param2 = "0=EMBEDDED_TRANSACTION&1=ENCT&2=LANGUAGE&3=SITE&4=ENC&EMBEDDED_TRANSACTION=FlexPricerAvailability&ENCT=1&LANGUAGE=CN&SITE="+SITE+"&ENC="+ENC;
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.remove("X-Requested-With");
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Host", "fly.malaysiaairlines.com");
		String result = this.httpProxyPost(httpClientSessionVo,searchUrl,param2,"other");
		if(result==null) return null;
		if(!result.contains("config :")) return null;
		result = result.replaceAll("\r|\n*","");
		String httpResult = MyStringUtil.getValue("config \\:", ", pageEngine", result).trim();
		return httpResult;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("Malaysia Airlines - 错误") || httpResult.contains("需要访问者运行Javascript")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);
	}
	
	public String getRi(String ri){
		int riInt = Integer.parseInt(ri);
		if(riInt<10){
			ri = "0"+ri;
		}
		return ri;
	}
	public String getTime(String time){ //4:55:00 PM -> 16:55:00 
		String h = time.substring(0, time.indexOf(":"));
		String m = time.substring(time.indexOf(":"),time.indexOf(":")+6);
		if(time.contains("PM")){
			if(!"12".equals(h)){
				int ho = Integer.parseInt(h);
				ho = ho+12;
				h = String.valueOf(ho);
			}
		}else{
			int ho = Integer.parseInt(h);
			if(ho<10){
				h="0"+h;
			}
		}
		return h+m;
	}
	
	
}
