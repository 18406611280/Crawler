package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 易捷航空Web官网
 */
public class B2CU2Crawler extends Crawler {
	
	private final String FLIGHTURL = "http://www.easyjet.com/links.mvc?lang=EN&dep=%depCode%&dest=%desCode%&dd=%depDate%&apax=1&cpax=0&ipax=0&SearchFrom=SearchPod2_/cn&isOneWay=on&pid=";
	private final String PRICEURL = "http://www.easyjet.com/EN/BasketView.mvc/AddFlight";
	
	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
	private MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
	private Map<String, Object> headerMap = new HashMap<String, Object>();
	private StringBuilder cookie = new StringBuilder();
	private final SimpleDateFormat DMYHM = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private final SimpleDateFormat YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private final SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat DMY = new SimpleDateFormat("ddMMyyyy");
	
	public B2CU2Crawler(String threadMark) {
		super(threadMark);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		
		String httpResult =this.httpResult();
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		Document document = Jsoup.parse(httpResult);
		Elements days = document.select(".OutboundDaySlider > .day");
		if(null == days || days.size()==0) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			String currency = "";
			Elements options = document.getElementById("DisplayCurrency").select("option");
			for(Element option : options){
				if("selected".equals(option.attr("selected"))){
					currency = option.attr("value");
					break;
				}
			}
			if(currency=="")return crawlResults;
			
			String flightSearchSession = document.getElementById("flightSearchSession").val();
			String __BasketState = document.getElementById("__BasketState").val();
			String flightOptionsState = document.getElementById("FlightOptionsState").val();
			String basketOptions = "";
			
			String regEx = "var BasketOptions = '(\\w+)';";
		    Pattern pattern = Pattern.compile(regEx);
		    Matcher matcher = pattern.matcher(httpResult);
		    if(matcher.find()){
		    	basketOptions = matcher.group(1);
		    }else{
		    	return crawlResults;
		    }
		    
		    String depCode = document.getElementById("acOriginAirportValue").val();
		    String desCode = document.getElementById("acDestinationAirportValue").val();
		    CrawlResultInter b2c = null;
		    for(int i=1;i<days.size()-1;i++){
				Element day = days.get(i);
				String dayDate = day.attr("data-column-date");
				String depDate = YMD.format(DMY.parse(dayDate));
				Elements dayLis = day.select("ul > li");
				
				for(Element dayLi : dayLis){
					
					b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
					b2c.setRouteType("OW");
					
					String flightToAddState = dayLi.attr("id");
					if(flightToAddState == null || "".equals(flightToAddState) || flightToAddState.contains("DayLoading_")){
						continue;
					}
					String result= this.httpResultFilght(flightToAddState, flightSearchSession, basketOptions, flightOptionsState, __BasketState);
					if(result == null || "".equals(result))continue;
					Map<String, Object> resultMap = null;
					try {
						resultMap = MyJsonTransformUtil.readValue(result, Map.class);
					} catch (Exception e) {
						logger.error("获取航班返回异常!",e);
					}
					if(resultMap == null) continue;
					__BasketState = resultMap.get("BasketState").toString();
					String html = resultMap.get("Html").toString();
					Document doc = Jsoup.parse(html);
					String fltNo = doc.getElementById("expandedFlights").select("div.content > div").get(1).select("span").get(1).text();
					fltNo = fltNo.replaceAll("Flight ", "").trim();
				    String airlineCode = "";
				    pattern = Pattern.compile("[A-Z]+");
				    matcher = pattern.matcher(fltNo);
				    if(matcher.find()){
				    	airlineCode = matcher.group();
				    }
				    fltNo = fltNo.replace(airlineCode, "U2");
				    Element lia = dayLi.select("a").get(0);
				    BigDecimal ticketPrice = new BigDecimal(lia.attr("charge-debit-full"));

				    b2c = CrawlerUtil.calPrice(b2c, ticketPrice, new BigDecimal(0), currency,rateMap);
					if(!"CNY".equals(b2c.getCurrency()))continue;//转换币种失败直接废弃这条数据
					
					Elements times = dayLi.select(".time");
					String spanDepDate =  times.get(0).select("span.hidden").get(0).text();
					spanDepDate = spanDepDate.replaceAll("Departure", "").trim();
				    spanDepDate = YMDHM.format(DMYHM.parse(spanDepDate));
					String tripDepDate = spanDepDate.substring(0, 10);
					String tripDepTime = spanDepDate.substring(11,16);
					
					String spanDesDate =  times.get(1).select("span.hidden").get(0).text();
					spanDesDate = spanDesDate.replaceAll("Arrival", "").trim();
				    spanDesDate = YMDHM.format(DMYHM.parse(spanDesDate));
					String tripDesDate = spanDesDate.substring(0, 10);
					String tripDesTime = spanDesDate.substring(11,16);
				    
					String resiteTxt = dayLi.select(".accessibility_hidden").get(0).text();
					String remainSite = "5";
					if(resiteTxt!=null && !"".equals(resiteTxt)){
						regEx = "[0-9]+";
					    pattern = Pattern.compile(regEx);
					    matcher = pattern.matcher(resiteTxt);
					    if(matcher.find()){
					    	remainSite = matcher.group();
					    }
					}
					List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
					CrawlResultInterTrip trip = new CrawlResultInterTrip(airlineCode,fltNo,depCode,desCode,tripDepDate,"Y",1,1);
					trip.setDesDate(tripDesDate);
					trip.setDepTime(tripDepTime);
					trip.setDesTime(tripDesTime);
					if(remainSite!=null && !"".equals(remainSite)){
						trip.setRemainSite(Integer.valueOf(remainSite));
					}
					flightTrips.add(trip);
					b2c.setFlightTrips(flightTrips);
					crawlResults.add(b2c);
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
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document.title().contains("404 Page Not Found") 
				|| document.title().contains("403 - Forbidden: Access is denied")
				|| document.title().contains("Screen scrapers")
				|| document.title().contains("错误")
				|| document.title().contains("Error")
				|| document.title().contains("error")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	@Override
	public String httpResult() throws Exception {
		
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, sdch, br");
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.easyjet.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String flightUrl = FLIGHTURL.replace("%depCode%", this.getJobDetail().getDepCode())
				.replace("%desCode%", this.getJobDetail().getDesCode())
				.replace("%depDate%", this.getJobDetail().getDepDate());
		
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,flightUrl, "html");
		
//		String sessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId=");
//		String akacd_TrueClarity_SC = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "akacd_TrueClarity_SC=");
//		String ej20RecentSearches = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ej20RecentSearches=");
//		String ej20SearchCookie = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ej20SearchCookie=");
//		String ejCC_3 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ejCC_3=");
//		String WPPOD = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "WPPOD=");
//		String VisitorRecognition = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "VisitorRecognition=");
//		String labi = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "labi=");
//		
//		cookie.append(sessionId).append(";")
//				.append(ej20RecentSearches).append(";")
//				.append(ej20SearchCookie).append(";")
//				.append(ejCC_3).append(";")
//				.append(WPPOD).append(";")
//				.append(VisitorRecognition).append(";")
//				.append(labi).append(";")
//				.append(akacd_TrueClarity_SC);
//		headerMap.put("Cookie",cookie);
		
		if(httpVo==null) return "";
		
		String result = httpVo.getHttpResult();
		
//		System.out.println(result);
		
		return result;
	}
	public String httpResultFilght(String flightToAddState,String flightSearchSession,String basketOptions,String flightOptionsState,String __BasketState) throws Exception {
		String result = "";
		try {
			headerMap.put("Referer", "http://www.easyjet.com/EN/Booking.mvc");
			headerMap.put("Origin", "http://www.easyjet.com");
			httpClientSessionVo.setHeaderMap(headerMap);
			
			Map<String,Object> paramMap = new HashMap<String, Object>();
			paramMap.put("flightToAddState", flightToAddState);
			paramMap.put("flightSearchSession", flightSearchSession);
			paramMap.put("basketOptions", basketOptions);
			paramMap.put("flightOptionsState", flightOptionsState);
			paramMap.put("__BasketState", __BasketState);
			result = this.httpProxyPost(httpClientSessionVo,PRICEURL,paramMap,"other");
			
//			System.out.println(result);
			
		} catch (Exception e) {
			logger.error("获取价格出现了异常!",e);
		}
		return result;
	}
	

	public static void main(String[] args) throws ParseException {
		String regEx = "u003cspan\\u003e\\r\\nFlight(\\w+)\\r\\n\\u003c\\/span\\u003e";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher("bsp;12:40\u0026nbsp;-\u0026nbsp;Arr\u0026nbsp;14:00u003cspan\u003e\r\nFlightEZY1697\r\n\u003c/span\u003e\r\n\r\n\r\n\u003cdivclass=\"basket-passenger-grouping\"");
	    if(matcher.find()){
	    	System.out.println(matcher.group(1));
	    }
	}

}