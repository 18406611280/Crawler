package com.aft.crawl.crawler.impl.b2c;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
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
 * 微笑航空Web官网
 */
public class B2CWECrawler extends Crawler {
	
	private String dep = null;
	private String des = null;
	private String depDate = null;
	
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	public B2CWECrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		String json =this.httpResult();
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(json)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + json);
			return crawlResults;
		}
		try {
			crawlResults = this.owFlight(json);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}
	
	private List<CrawlResultBase> owFlight(String json) {
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		JSONObject jsonRoot = JSONObject.parseObject(json);
		JSONObject config = jsonRoot.getJSONObject("pageDefinitionConfig");
		JSONObject pageData = config.getJSONObject("pageData");
		JSONObject business = pageData.getJSONObject("business");
		JSONObject Availability = business.getJSONObject("Availability");
		if(Availability==null) return null;
		JSONArray proposedBound = Availability.getJSONArray("proposedBounds");
	    JSONObject proposedBounds = proposedBound.getJSONObject(0);
		JSONObject currencyBean = Availability.getJSONObject("currencyBean");
	    String currency = currencyBean.getString("code");//币种
		JSONArray proposedFlightsGroups = proposedBounds.getJSONArray("proposedFlightsGroup");
		JSONArray recommendationList = Availability.getJSONArray("recommendationList");
		Map<String,JSONArray> segmentsMap = new HashMap<String, JSONArray>();
		for(int j = 0;j<proposedFlightsGroups.size();j++){
			JSONObject proposedFlightsGroup = proposedFlightsGroups.getJSONObject(j);
			String boundId = proposedFlightsGroup.getString("proposedBoundId");
			JSONArray segments = proposedFlightsGroup.getJSONArray("segments");
			segmentsMap.put(boundId, segments);
		}
		for(int t =0 ;t<recommendationList.size();t++){
			JSONObject recommendation = recommendationList.getJSONObject(t);
			JSONObject bound = recommendation.getJSONArray("bounds").getJSONObject(0);
			JSONObject boundAmount = bound.getJSONObject("boundAmount");
			String ticketPrice = boundAmount.getString("amountWithoutTax");//票面价
			String tax = boundAmount.getString("tax");//税费
			JSONArray flightGroupList = bound.getJSONArray("flightGroupList");
			for(int i = 0;i<flightGroupList.size();i++){
				JSONObject flightGroup = flightGroupList.getJSONObject(i);
				String flightId = flightGroup.getString("flightId");
				String cabinNumber = flightGroup.getString("numberOfSeatsLeft");//舱位数
				String cabin = flightGroup.getString("rbd");//舱位
				FlightData flightData = new FlightData(this.getJobDetail(),"OW", dep, des, depDate);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setAirlineCode("WE");//航司
				
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
				flightData.setPrices(flightPriceList);
				
				//设置航段
				JSONArray segments = segmentsMap.get(flightId);
				List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
				for(int s=0;s<segments.size();s++){
					JSONObject segment = segments.getJSONObject(s);
					String depTime = segment.getString("beginDate");
					depTime = depTime.substring(depTime.length()-11,depTime.length()).trim();
					String desTime = segment.getString("endDate");
					desTime = desTime.substring(desTime.length()-11,desTime.length()).trim();
					String departureTime = depDate+" "+depTime;//出发时间
					String arrivalTime = depDate+" "+desTime;//出发时间
					String flightEn = segment.getJSONObject("airline").getString("code");//航司
					String flightNo = flightEn+segment.getString("flightNumber").trim();//航班号
					
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(s+1);
					flightSegment.setAirlineCode(flightEn);
					flightSegment.setFlightNumber(flightNo);
					flightSegment.setDepAirport(dep);
					flightSegment.setDepTime(departureTime);
					flightSegment.setArrAirport(des);
					flightSegment.setCabinCount(cabinNumber);
					flightSegment.setArrTime(arrivalTime);
					String shareFlight = this.getShareFlight(flightEn);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setAircraftCode("");
					flightSegmentList.add(flightSegment);
				}
				flightData.setFromSegments(flightSegmentList);
				crawlResults.add(flightData);
			}
		}
		
		return crawlResults;
	}
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("需要访问者运行Javascript")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	
	
	public String httpResult() throws Exception{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, sdch");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.221 Safari/537.36 SE 2.X MetaSr 1.0");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.thaismileair.com");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		httpClientSessionVo.setHeaderMap(headerMap);
		//第一次请求
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://www.thaismileair.com/zh","","other");
		String cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
		if(cookies==null) {
			System.out.println("---------------------------");
			return null;
		}
		StringBuilder cookie = new StringBuilder(cookies);
		headerMap.put("Cookie",cookie);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,"https://www.thaismileair.com/zh","html");
		if(httpVo==null){
			System.out.println("========================");
			return null;	
		}
		String indexJsp = httpVo.getHttpResult();
		indexJsp = indexJsp.replaceAll("\r|\n*","");
		String param = getValue("\\/thaismiletripflow\\/tripflow4_for_new_booking.php\\?\'\\+param\\+\'","\'", indexJsp);
		if(param==null)return null;
		String bookUrl = "https://www.thaismileair.com/thaismiletripflow/tripflow4_for_new_booking.php?o1="+dep+"&d1="+des+"&smltype=&ADT=1&CHD=0&inl=0&lang=CN&dd1="+depDate+param;
		String AWSALB = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "AWSALB=");
		String nlbi = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "nlbi_1702285=");
		cookie.append(";").append(AWSALB).append(";").append(nlbi);
		headerMap.put("Cookie",cookie);
		headerMap.put("Referer", "https://www.thaismileair.com/zh/");
		String result1 = this.httpProxyGet(httpClientSessionVo,bookUrl,"html");
		if(result1==null)return null;
		String action1 = getValue("action='", "\\'", result1);
		if(action1==null)return null;
		String uid = getValue("name='uid' value='", "\\'", result1);
		String pwd = getValue("name='pwd' value='", "\\'", result1);
		String LANGUAGE = getValue("name='LANGUAGE' value='", "\\'", result1);
		String SITE = getValue("name='SITE' value='", "\\'", result1);
		String EMBEDDED_TRANSACTION = getValue("name='EMBEDDED_TRANSACTION' value='", "\\'", result1);
		String enc_params = getValue("name='enc_params' value='", "\\'", result1);
		
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("uid", uid);
		paramMap.put("pwd", pwd);
		paramMap.put("LANGUAGE", LANGUAGE);
		paramMap.put("SITE", SITE);
		paramMap.put("EMBEDDED_TRANSACTION", EMBEDDED_TRANSACTION);
		paramMap.put("enc_params", enc_params);
		headerMap.put("Accept-Encoding","gzip, deflate");
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Referer",bookUrl);
		headerMap.put("Origin","https://www.thaismileair.com");
		headerMap.put("Host", "api.thaismileair.com");
		String result2 = this.httpProxyPost(httpClientSessionVo,action1,paramMap,"html");
		if(result2==null)return null;
		String action2 = "https://book.thaismileair.com/plnext/ThaiSmile/Override.action";
		String TRANSACTION = getValue("name=\"EMBEDDED_TRANSACTION\" value=\"", "\"", result2);
		String site = getValue("name=\"SITE\" value=\"", "\"", result2);
		String enct = getValue("name=\"ENCT\" value=\"", "\"", result2);
		String enc = getValue("name=\"ENC\" value=\"", "\"", result2);
		
		headerMap.put("Referer",action1);
		headerMap.put("Origin","https://api.thaismileair.com");
		headerMap.put("Host", "book.thaismileair.com");
		Map<String,Object> paramMap2 = new HashMap<String, Object>();
		paramMap2.put("EMBEDDED_TRANSACTION", TRANSACTION);
		paramMap2.put("LANGUAGE", "CN");
		paramMap2.put("SITE", site);
		paramMap2.put("ENCT", enct);
		paramMap2.put("ENC", enc);
		String result = this.httpProxyPost(httpClientSessionVo,action2,paramMap2,"html");
		System.out.println(result);
		if(result==null) return null;
		result = result.replaceAll("\r|\n*","");
		String json = getValue("config \\: ", "\\, pageEngine", result);
		return json;
	}
	
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat f=new SimpleDateFormat("EEE, dd MMM yyyy",Locale.ENGLISH);
		SimpleDateFormat f2=new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZ yyyy",Locale.ENGLISH);
		SimpleDateFormat f1=new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat f3=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(f1.format(f.parse("Tue, 04 Sep 2018")));
//		System.out.println(f3.format(f2.parse("Tue Jul 25 14:31:46 CST 2017")));
		
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
}