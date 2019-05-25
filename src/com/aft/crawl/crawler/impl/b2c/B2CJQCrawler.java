package com.aft.crawl.crawler.impl.b2c;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.MyStringUtil;
import com.aft.utils.RuoKuai;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;

import net.sf.json.JSONObject;

/**
 * 捷星航空官网
 */
public class B2CJQCrawler extends Crawler {
	
	private static final String filghtUrl = "https://booking.jetstar.com/au/en/booking/search-flights?adults=%adtCount%&children=0&infants=0&search-origin01=&search-destination01=&calendar-review=1&undefined=&origin1=%depCode%&destination1=%desCode%&departuredate1=%depDate%&AutoSubmit=Y&currency=CNY&dotcomFCPricesHidden=true";
	
	private static final String adtCount = "1";
	
	public B2CJQCrawler(String threadMark) {
		super(threadMark, "0");
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 返回httpResult为空:" + httpResult);
			return null;
		}
		SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat SDF_YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		SimpleDateFormat SDF_DMYHMS = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			//有两种页面返回来，不确定
			Document document = Jsoup.parse(httpResult);
			Elements domesticTab = document.select("table.domestic");
			Elements domesticRadios = document.select("input.js-flight-selection");
			if(null != domesticTab && !domesticTab.isEmpty()&&domesticTab.size()>0) {
				String currency = document.getElementById("datalayer-data").attr("data-currency-code");
				String depCode = document.getElementById("datalayer-flight-search").attr("data-UserChosenOutboundOrigin");
				String desCode = document.getElementById("datalayer-flight-search").attr("data-UserChosenOutboundDestination");
				String depDate = document.getElementById("datalayer-flight-search").attr("data-FlightInitialSearchOutboundDepartDate").substring(0,10);
				Elements domesticTrs = domesticTab.select("tbody>tr");
				CrawlResultB2C b2c = null;
				for(Element domesticTr : domesticTrs){
					Elements domesticTds = domesticTr.select(">td");
					
					if(domesticTds==null || domesticTds.isEmpty() || domesticTds.size()<4)continue;
					
					String isDirect = domesticTds.get(2).select(">span.stops").text();
					//忽略联程
					if(null == isDirect || !isDirect.contains("Direct flight"))continue;
					
					String depTime = domesticTds.get(0).select(">strong").text();
					String desTime = domesticTds.get(1).select(">strong").text();
					if(depTime==null || desTime == null)continue;
					depTime = depTime.substring(0,5);
					desTime = desTime.substring(0,5);
					String fltNo = domesticTds.get(2).select("div.flights >dl >dt >span.flight-no").text();
					fltNo = fltNo.replaceAll(" ", "");
					String airlineCode = fltNo.substring(0, 2);
					Element priceRadio = domesticTds.get(3).select(">div.field >input.radio").get(0);
					if(null == priceRadio)continue;
					String dataprice = priceRadio.attr("data-price");
					String datafeesadt = priceRadio.attr("data-fees-adt");
					Elements sash = domesticTds.get(3).select("div.field >p.counter-sash");
					String remainSite = "10";
					if(sash!=null && !sash.isEmpty() && sash.size()>0){
						String sashtxt = sash.get(0).text();
						if(sashtxt!=null){
							String regex = "\\d*";
							Pattern p = Pattern.compile(regex);
							Matcher m = p.matcher(sashtxt);
							if(m.find()){
								remainSite = m.group();
							}
						}
					}
					String desDate = depDate;
					int dhour = Integer.valueOf(depTime.split(":")[0]);
					int ahour = Integer.valueOf(desTime.split(":")[0]);
					if(dhour>ahour){
						Date date = SDF_YMD.parse(depDate);
						Calendar ca = Calendar.getInstance();
						ca.setTime(date);
						ca.add(Calendar.DAY_OF_MONTH, 1);
						date = ca.getTime();
						desDate = SDF_YMD.format(date);
					}
					b2c = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, "N", depCode, desCode,depDate, "Y");
					b2c.setDepTime(depTime);
					b2c.setDesTime(desTime);
					b2c.setEndDate(desDate);
					b2c.setRemainSite(Integer.valueOf(remainSite));
					BigDecimal ticketPrice = new BigDecimal(dataprice);
					BigDecimal salePrice = new BigDecimal(datafeesadt);
					if("CNY".equals(currency)){
						b2c.setTicketPrice(ticketPrice);
						b2c.setSalePrice(salePrice);
					}else{
						BigDecimal rate = CurrencyUtil.getRequest3(currency, "CNY");
						if(rate.compareTo(BigDecimal.ZERO)==0){
							rate = CurrencyUtil.getRequest3(currency, "CNY");
							if(rate.compareTo(BigDecimal.ZERO)==0){
								b2c.setTicketPrice(ticketPrice);
								b2c.setSalePrice(salePrice);
								b2c.setType("此价格的币种类型："+currency);
							}
						}else{
							BigDecimal cnyPrice = ticketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
							BigDecimal taxPrice = salePrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
							b2c.setTicketPrice(cnyPrice);
							b2c.setSalePrice(taxPrice);
						}
					}
					crawlResults.add(b2c);
				}
				
			}else if( null != domesticRadios && !domesticRadios.isEmpty() && domesticRadios.size()>0){
				String currency = document.getElementById("datalayer-data").attr("data-currency-code");
				String depCode = document.getElementById("js-watch-price-data").attr("data-origin-code");
				String desCode = document.getElementById("js-watch-price-data").attr("data-destination-code");
				String depDate = document.getElementById("js-watch-price-data").attr("data-departure-date");
				
				CrawlResultInter b2c = null;
				for(Element domesticRadio : domesticRadios){
					b2c = new CrawlResultInter(this.getJobDetail(), depCode, desCode, depDate);
					b2c.setRouteType("OW");
					String jsonPrice = domesticRadio.attr("data-price-breakdown");
					JSONObject jo = JSONObject.fromObject(jsonPrice);
					String totalFare =jo.getString("TotalFare");
					String totalCharges =jo.getString("TotalCharges");
					BigDecimal ticketPrice = new BigDecimal(totalFare).divide(new BigDecimal(adtCount)).setScale(0,BigDecimal.ROUND_UP);
					BigDecimal salePrice = new BigDecimal(totalCharges).divide(new BigDecimal(adtCount)).setScale(0,BigDecimal.ROUND_UP);
					b2c = CrawlerUtil.calPrice(b2c, ticketPrice, salePrice, currency,rateMap);
					if(!"CNY".equals(b2c.getCurrency()))continue;//转换币种失败直接废弃这条数据
					
					List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
					String dataFlight = domesticRadio.attr("data-flight");
					String[] segments = dataFlight.split("~\\^");
					for(int i=0;i<segments.length;i++){
						String[] segs= segments[i].split("~");
						String airlineCode = segs[0].trim();
						String tripFltNo = airlineCode+segs[1].trim();
						String tripDepCode = segs[4].trim();
						String departure = segs[5].trim();
						String tripDesCode = segs[6].trim();
						String arrarture = segs[7].trim();
						Date dDate = SDF_DMYHMS.parse(departure);
						Date aDate = SDF_DMYHMS.parse(arrarture);
						String depDateStr = SDF_YMDHMS.format(dDate);
						String arrDateStr = SDF_YMDHMS.format(aDate);
						String tripDepDate = depDateStr.substring(0,10);
						String tripDepTime = depDateStr.substring(11,16);
						String tripDesDate = arrDateStr.substring(0,10);
						String tripDesTime = arrDateStr.substring(11,16);
						
						CrawlResultInterTrip trip = new CrawlResultInterTrip(airlineCode,tripFltNo,tripDepCode,tripDesCode,tripDepDate,"Y",2,i+1,1);
						trip.setDesDate(tripDesDate);
						trip.setDepTime(tripDepTime);
						trip.setDesTime(tripDesTime);
						flightTrips.add(trip);
					}
					b2c.setFlightTrips(flightTrips);
					crawlResults.add(b2c);
				}
			}else{
				logger.info(this.getJobDetail().toStr() + ", 返回错误信息:" + httpResult);
				return crawlResults;
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		 }
		return crawlResults;
	}
//	@Override
//	protected boolean needToChangeIp(String httpResult, Document document,
//			Object jsonObject, String returnType) throws Exception {
//		if(httpResult.contains("Internal Server Error") 
//				|| httpResult.contains("The service is unavailable")
//				|| httpResult.contains("Access Denied")) return true;
//		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
//	}	
	@Override
	public String httpResult() throws Exception {
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		//==================================test============================================
//		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		headerMap.put("Accept-Encoding", "gzip, deflate, br");
//		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
//		headerMap.put("Upgrade-Insecure-Requests", "1");
//		headerMap.put("Connection", "keep-alive");
//		headerMap.put("Host", "lanmeiairlines.com");
//		
//		httpClientSessionVo.setHeaderMap(headerMap);
//		
//		MyHttpClientResultVo httpVo  = this.httpProxyResultVoGet(httpClientSessionVo, "https://lanmeiairlines.com/index.html?lang=en","html");
//		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
//		headerMap.put("X-Requested-With", "XMLHttpRequest");
//		headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//		headerMap.put("Referer", "https://lanmeiairlines.com/index.html?lang=en");
//		String res1 = this.httpProxyPost(httpClientSessionVo, "https://lanmeiairlines.com/ticketRecord/checkTicket.jhtml","adultCount=1&childCount=0&infantCount=0&takeoffDate=2018-10-17&returnDate=2018-10-11&orgcity=PNH&dstcity=CAN&tripType=OW&language=US&CURRENCY=USD","html");
//		JSONObject object1 = JSONObject.fromObject(res1);
//		String port = object1.getString("port");
//		String sign = object1.getString("sign");
//		String userIp = object1.getString("userIp");
//
//		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		headerMap.put("Accept-Encoding", "gzip, deflate");
//		headerMap.remove("X-Requested-With");
//		headerMap.remove("Referer");
//		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
//		headerMap.put("Host", "b2c.lanmeiairlines.com");
//		String param1 = "tripType=OW&userIp="+userIp+"&sign="+sign+"&rad="+port+"&cabinType=ECONOMY&orgcity=PNH&dstcity=CAN&takeoffDate=2018-10-17&returnDate=2018-10-11&adultCount=1&childCount=0&language=US&CURRENCY=USD";
//	    httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "http://b2c.lanmeiairlines.com/lqWeb/reservation/getImage.do",param1,"html");
//	    String Cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
//	    String status = "error";
//	    String code = null;//验证码
//	    while (!"success".equals(status)){
//		    Date date = new Date();
//		    String stamp = dateToStamp(date);
//		    String imagUrl = "http://b2c.lanmeiairlines.com/lqWeb/servlet/GetVertifyCode?option=getVertifyCode&timestamp="+stamp;
//		    headerMap.remove("Content-Type");
//		    headerMap.put("Cookie",Cookies);
//		    headerMap.put("Accept", "*/*");
//		    headerMap.put("Referer", "http://b2c.lanmeiairlines.com/lqWeb/reservation/getImage.do");
//		    File file = new File("resource/antiVc/img/JQ.jpg");
//		    String fileFullName = file.getCanonicalPath();
//			MyHttpClientUtil.download(imagUrl, this.headerMap, fileFullName, this.getCrawlExt().getOneWaitTime());
//			String yzm = RuoKuai.getCode("resource/antiVc/img/JQ.jpg");
//	        code =MyStringUtil.getValue("<Result>", "</Result>", yzm);
//	        
//	        headerMap.put("Accept", "text/plain, */*; q=0.01");
//	        headerMap.put("X-Requested-With", "XMLHttpRequest");
//	        headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//	        String param2 = "option=checkCode&vertifyCode="+code;
//	        status = this.httpProxyPost(httpClientSessionVo,"http://b2c.lanmeiairlines.com/lqWeb/servlet/GetVertifyCode",param2,"html");
//        }
//	    String param3 ="vertifyCode="+code+"&token=&childCount=0&takeoffDate=2018-10-17&cabinType=ECONOMY&adultCount=1&orgcity=PNH&language=US&returnDate=2018-10-11&dstcity=CAN&tripType=OW&CURRENCY=USD&sureDate=&email=&orgcitycode=PNH&pageConstant=&referer=&userIp="+userIp+"&rad="+port+"&sign="+sign;
//	    headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//	    headerMap.remove("X-Requested-With");
//	    headerMap.put("Content-Type", "application/x-www-form-urlencoded");
//	    String result = this.httpProxyPost(httpClientSessionVo,"http://b2c.lanmeiairlines.com/lqWeb/reservation/reservation/AVQuery.do",param3,"html");
//	    System.out.println(result);
        
        
//	    InputStream inputStream = MyHttpClientUtil.getInputStream(httpClient,imagUrl, headerMap, 100000);
	    
		
		//==================================================================================
		
		SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
//		SimpleDateFormat SDF_DMY = new SimpleDateFormat("dd/MM/yyyy");
//		SimpleDateFormat SDF_DMY = new SimpleDateFormat("dd-MM-yyyy");
//		Date date = SDF_YMD.parse(this.getJobDetail().getDepDate());
//		String depDate = SDF_DMY.format(date);
		
//		Map<String,Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:54.0) Gecko/20100101 Firefox/54.0");
//		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		headerMap.put("Content-Type",  "application/x-www-form-urlencoded" );
//		headerMap.put("Host", "book.jetstar.com");
//		headerMap.put("Referer", "http://www.jetstar.com/cn/zh/home");
//		headerMap.put("Accept-Encoding", "gzip, deflate, br");
//		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//		headerMap.put("Upgrade-Insecure-Requests", "1");
//		headerMap.put("Connection", "keep-alive");
//		headerMap.put("Cache-Control", "no-cache");
//		headerMap.put("Pragma", "no-cache");
//		
//		httpClientSessionVo.setHeaderMap(headerMap);
//		
//		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,"https://book.jetstar.com/?culture=zh-cn", "html");
//		Header[] header = httpVo.getHeaders();
//		String cookies = "";
//		for(int i=0;i<header.length;i++){
//			if("Set-Cookie".equals(header[i].getName())){
//				cookies += header[i].getValue().split(";")[0]+";";
//			}
//		}
//		
//		headerMap.put("Cookie", cookies);
//		Document document = Jsoup.parse(httpVo.getHttpResult());
//		
//		String __EVENTARGUMENT = document.getElementById("eventTarget").val();
//		String __EVENTTARGET = document.getElementById("eventArgument").val();
//		String __VIEWSTATE = document.getElementById("viewState").val();
//		String pageToken = document.select("[name=pageToken]").get(0).val();
//		
//		String preString = "ControlGroupSearchView%24AvailabilitySearchInputSearchView%24";
//		Map<String,Object> paramMap = new HashMap<String, Object>();
//		paramMap.put(preString + "ButtonSubmit", "");
//        paramMap.put(preString + "DropDownListMultiPassengerType_ADT", "1");
//        paramMap.put(preString + "DropDownListMultiPassengerType_CHD", "0");
//        paramMap.put(preString + "DropDownListMultiPassengerType_INFANT", "0");
//        paramMap.put(preString + "DropDownListPassengerType_ADT", "1");
//        paramMap.put(preString + "DropDownListPassengerType_CHD", "0");
//        paramMap.put(preString + "DropDownListPassengerType_INFANT", "0");
//        paramMap.put(preString + "RadioButtonMarketStructure", "OneWay");
//        paramMap.put(preString + "RadioButtonSearchBy", "SearchStandard");
//        paramMap.put(preString + "TextBoxMarketDestination1", "Singapore+%28SIN%29");
//        paramMap.put(preString + "TextBoxMarketDestination2", "");
//        paramMap.put(preString + "TextBoxMarketDestination3", "");
//        paramMap.put(preString + "TextBoxMarketDestination4", "");
//        paramMap.put(preString + "TextBoxMarketDestination5", "");
//        paramMap.put(preString + "TextBoxMarketDestination6", "");
//        paramMap.put(preString + "TextBoxMarketOrigin1", "Guangzhou+%28CAN%29");
//        paramMap.put(preString + "TextBoxMarketOrigin2", "");
//        paramMap.put(preString + "TextBoxMarketOrigin3", "");
//        paramMap.put(preString + "TextBoxMarketOrigin4", "");
//        paramMap.put(preString + "TextBoxMarketOrigin5", "");
//        paramMap.put(preString + "TextBoxMarketOrigin6", "");
//        paramMap.put(preString + "TextBoxMultiCityDestination1", "Destination");
//        paramMap.put(preString + "TextBoxMultiCityDestination2", "Destination");
//        paramMap.put(preString + "TextBoxMultiCityOrigin1", "Origin");
//        paramMap.put(preString + "TextBoxMultiCityOrigin2", "Origin");
//        paramMap.put(preString + "TextboxDepartureDate1", "29%2F07%2F2017");
//        paramMap.put(preString + "TextboxDepartureMultiDate1", "");
//        paramMap.put(preString + "TextboxDepartureMultiDate2", "");
//        paramMap.put(preString + "numberTrips", "2");
//        paramMap.put("__EVENTARGUMENT", __EVENTARGUMENT);
//        paramMap.put("__EVENTTARGET", __EVENTTARGET);
//        paramMap.put("__VIEWSTATE", __VIEWSTATE);
//        paramMap.put("pageToken", pageToken);
//        paramMap.put("locale", "en-AU");
//		
//		
//		String result = this.httpProxyPost(httpClientSessionVo, "https://book.jetstar.com/Search.aspx", paramMap, "html");
        
        
 
		
		String url = filghtUrl
				.replace("%depCode%", this.getJobDetail().getDepCode())
				.replace("%desCode%", this.getJobDetail().getDesCode())
				.replace("%depDate%", this.getJobDetail().getDepDate())
				.replace("%adtCount%", adtCount)+"&var="+System.currentTimeMillis();
		
		Map<String,Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//		headerMap.put("Accept-Encoding", "gzip, deflate, sdch, br");
//		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
//		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
//		headerMap.put("Upgrade-Insecure-Requests", "1");
//		headerMap.put("Connection", "keep-alive");
//		headerMap.put("Host", "booking.jetstar.com");
//		headerMap.put("Cache-Control", "no-cache, no-store, must-revalidate");
//		headerMap.put("Pragma", "no-cache");
//		headerMap.put("Cache_Control", "no-cache");
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.jetstar.com");

		httpClientSessionVo.setHeaderMap(headerMap);
		
		MyHttpClientResultVo httpVo  = this.httpProxyResultVoGet(httpClientSessionVo, "https://www.jetstar.com/cn/zh/flights?origin=CAN&destination=SIN&flight-type=1&selected-departure-date=15-10-2018&adult=1&flexible=1&currency=CNY","html");
//		MyHttpClientResultVo httpVo  = this.httpProxyResultVoGet(httpClientSessionVo, "https://www.jetstar.com/cn/zh/flights","html");
//		System.out.println(httpVo.getHttpResult());
//		String pixelUrl = MyStringUtil.getValue("\\<img src=\"https://www.jetstar.com", "\\?a\\=",httpVo.getHttpResult());
//		pixelUrl = "https://www.jetstar.com"+pixelUrl;
		String cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
//		String AKA_A2 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","AKA_A2=");
//		String session_id = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","bm_mi=");
//		String bm_sz = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","bm_sz=");
//		String user_location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","user-location=");
//		String _abck = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","_abck=");
//		String ak_bmsc = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","ak_bmsc=");
//		StringBuilder cookies = new StringBuilder().append(ak_bmsc);
		
//		headerMap.put("Accept", "*/*");
//		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
//		headerMap.put("Referer", "https://www.jetstar.com/cn/zh/flights?origin=CAN&destination=SIN&flight-type=1&selected-departure-date=15-10-2018&adult=1&flexible=1&currency=CNY");
//		headerMap.remove("Upgrade-Insecure-Requests");
//		String param ="ap=true&bt=0&fonts=41%2C43%2C44%2C45%2C46%2C47%2C48%2C49%2C50%2C51%2C52%2C55%2C56%2C57%2C58%2C59%2C60%2C61%2C62%2C63%2C64%2C65%2C67%2C68%2C69&fh=2b546713c41a929cd4d394582b29987bbb097c98&timing=%7B%221%22%3A41%2C%222%22%3A210%2C%223%22%3A317%2C%224%22%3A422%2C%225%22%3A732%2C%22profile%22%3A%7B%22bp%22%3A0%2C%22sr%22%3A5%2C%22dp%22%3A1%2C%22lt%22%3A0%2C%22ps%22%3A0%2C%22cv%22%3A17%2C%22fp%22%3A1%2C%22sp%22%3A0%2C%22br%22%3A0%2C%22ieps%22%3A1%2C%22av%22%3A0%2C%22z1%22%3A9%2C%22jsv%22%3A1%2C%22nav%22%3A4%2C%22z2%22%3A1%2C%22z3%22%3A7%2C%22z4%22%3A0%2C%22z5%22%3A1%2C%22fonts%22%3A109%7D%2C%22main%22%3A1890%2C%22compute%22%3A41%2C%22send%22%3A841%7D&bp=&sr=%7B%22inner%22%3A%5B1920%2C945%5D%2C%22outer%22%3A%5B1936%2C1056%5D%2C%22screen%22%3A%5B-8%2C-8%5D%2C%22pageOffset%22%3A%5B0%2C0%5D%2C%22avail%22%3A%5B1920%2C1040%5D%2C%22size%22%3A%5B1920%2C1080%5D%2C%22client%22%3A%5B1903%2C4884%5D%2C%22colorDepth%22%3A24%2C%22pixelDepth%22%3A24%7D&dp=%7B%22XDomainRequest%22%3A0%2C%22createPopup%22%3A0%2C%22removeEventListener%22%3A1%2C%22globalStorage%22%3A0%2C%22openDatabase%22%3A0%2C%22indexedDB%22%3A1%2C%22attachEvent%22%3A0%2C%22ActiveXObject%22%3A0%2C%22dispatchEvent%22%3A1%2C%22addBehavior%22%3A0%2C%22addEventListener%22%3A1%2C%22detachEvent%22%3A0%2C%22fireEvent%22%3A0%2C%22MutationObserver%22%3A1%2C%22HTMLMenuItemElement%22%3A1%2C%22Int8Array%22%3A1%2C%22postMessage%22%3A1%2C%22querySelector%22%3A1%2C%22getElementsByClassName%22%3A1%2C%22images%22%3A1%2C%22compatMode%22%3A%22CSS1Compat%22%2C%22documentMode%22%3A0%2C%22all%22%3A1%2C%22now%22%3A1%2C%22contextMenu%22%3Anull%7D&lt=1539159899058%2B8&ps=true%2Ctrue&cv=19f5de4bfc86212d44a26e1fab8d56ca489459d4&fp=false&sp=false&br=Firefox&ieps=false&av=false&z=%7B%22a%22%3A172961767%2C%22b%22%3A1%2C%22c%22%3A0%7D&zh=&jsv=1.5&nav=%7B%22userAgent%22%3A%22Mozilla%2F5.0%20(Windows%20NT%206.1%3B%20Win64%3B%20x64%3B%20rv%3A56.0)%20Gecko%2F20100101%20Firefox%2F56.0%22%2C%22appName%22%3A%22Netscape%22%2C%22appCodeName%22%3A%22Mozilla%22%2C%22appVersion%22%3A%225.0%20(Windows)%22%2C%22appMinorVersion%22%3A0%2C%22product%22%3A%22Gecko%22%2C%22productSub%22%3A%2220100101%22%2C%22vendor%22%3A%22%22%2C%22vendorSub%22%3A%22%22%2C%22buildID%22%3A%2220170926190823%22%2C%22platform%22%3A%22Win64%22%2C%22oscpu%22%3A%22Windows%20NT%206.1%3B%20Win64%3B%20x64%22%2C%22hardwareConcurrency%22%3A4%2C%22language%22%3A%22zh-CN%22%2C%22languages%22%3A%5B%22zh-CN%22%2C%22zh%22%2C%22en-US%22%2C%22en%22%5D%2C%22systemLanguage%22%3A0%2C%22userLanguage%22%3A0%2C%22doNotTrack%22%3A%22unspecified%22%2C%22msDoNotTrack%22%3A0%2C%22cookieEnabled%22%3Atrue%2C%22geolocation%22%3A1%2C%22vibrate%22%3A1%2C%22maxTouchPoints%22%3A0%2C%22webdriver%22%3A0%2C%22plugins%22%3A%5B%5D%7D&t=a96fe0888efd48c54cf70efcfeab93e9ed666c6c&u=702599446bc75b10889de6b3f69bc1db&fc=true";
//		String param = "ap=true&bt=0&fonts=41,43,44,45,46,47,48,49,50,51,52,55,56,57,58,59,60,61,62,63,64,65,67,68,69&fh=2b546713c41a929cd4d394582b29987bbb097c98&timing={\"1\":35,\"2\":284,\"3\":579,\"profile\":{\"bp\":0,\"sr\":5,\"dp\":1,\"lt\":0,\"ps\":1,\"cv\":10,\"fp\":0,\"sp\":1,\"br\":0,\"ieps\":0,\"av\":0,\"z1\":13,\"jsv\":1,\"nav\":1,\"z2\":0,\"z3\":4,\"fonts\":19},\"main\":1397,\"compute\":35,\"send\":598}&bp=&sr={\"inner\":[1920,945],\"outer\":[1936,1056],\"screen\":[-8,-8],\"pageOffset\":[0,0],\"avail\":[1920,1040],\"size\":[1920,1080],\"client\":[1903,2632],\"colorDepth\":24,\"pixelDepth\":24}&dp={\"XDomainRequest\":0,\"createPopup\":0,\"removeEventListener\":1,\"globalStorage\":0,\"openDatabase\":0,\"indexedDB\":1,\"attachEvent\":0,\"ActiveXObject\":0,\"dispatchEvent\":1,\"addBehavior\":0,\"addEventListener\":1,\"detachEvent\":0,\"fireEvent\":0,\"MutationObserver\":1,\"HTMLMenuItemElement\":1,\"Int8Array\":1,\"postMessage\":1,\"querySelector\":1,\"getElementsByClassName\":1,\"images\":1,\"compatMode\":\"CSS1Compat\",\"documentMode\":0,\"all\":1,\"now\":1,\"contextMenu\":null}&lt=1539049451538+8&ps=true,true&cv=19f5de4bfc86212d44a26e1fab8d56ca489459d4&fp=false&sp=false&br=Firefox&ieps=false&av=false&z={\"a\":172961767,\"b\":1,\"c\":0}&zh=&jsv=1.5&nav={\"userAgent\":\"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0\",\"appName\":\"Netscape\",\"appCodeName\":\"Mozilla\",\"appVersion\":\"5.0 (Windows)\",\"appMinorVersion\":0,\"product\":\"Gecko\",\"productSub\":\"20100101\",\"vendor\":\"\",\"vendorSub\":\"\",\"buildID\":\"20170926190823\",\"platform\":\"Win64\",\"oscpu\":\"Windows NT 6.1; Win64; x64\",\"hardwareConcurrency\":4,\"language\":\"zh-CN\",\"languages\":[\"zh-CN\",\"zh\",\"en-US\",\"en\"],\"systemLanguage\":0,\"userLanguage\":0,\"doNotTrack\":\"unspecified\",\"msDoNotTrack\":0,\"cookieEnabled\":true,\"geolocation\":1,\"vibrate\":1,\"maxTouchPoints\":0,\"webdriver\":0,\"plugins\":[]}&t=a96fe0888efd48c54cf70efcfeab93e9ed666c6c&u=542c7349d1aa5eacaaa115b02c07e164&fc=true";
//		String param = "ap=true&bt=0&fonts=41%2C43%2C44%2C45%2C46%2C47%2C48%2C49%2C50%2C51%2C52%2C55%2C56%2C57%2C58%2C59%2C60%2C61%2C62%2C63%2C64%2C65%2C67%2C68%2C69&fh=2b546713c41a929cd4d394582b29987bbb097c98&timing=%7B%221%22:35%2C%222%22:284%2C%223%22:579%2C%22profile%22:%7B%22bp%22:0%2C%22sr%22:5%2C%22dp%22:1%2C%22lt%22:0%2C%22ps%22:1%2C%22cv%22:10%2C%22fp%22:0%2C%22sp%22:1%2C%22br%22:0%2C%22ieps%22:0%2C%22av%22:0%2C%22z1%22:13%2C%22jsv%22:1%2C%22nav%22:1%2C%22z2%22:0%2C%22z3%22:4%2C%22fonts%22:19%7D%2C%22main%22:1397%2C%22compute%22:35%2C%22send%22:598%7D&bp=&sr=%7B%22inner%22:%5B1920%2C945%5D%2C%22outer%22:%5B1936%2C1056%5D%2C%22screen%22:%5B-8%2C-8%5D%2C%22pageOffset%22:%5B0%2C0%5D%2C%22avail%22:%5B1920%2C1040%5D%2C%22size%22:%5B1920%2C1080%5D%2C%22client%22:%5B1903%2C2632%5D%2C%22colorDepth%22:24%2C%22pixelDepth%22:24%7D&dp=%7B%22XDomainRequest%22:0%2C%22createPopup%22:0%2C%22removeEventListener%22:1%2C%22globalStorage%22:0%2C%22openDatabase%22:0%2C%22indexedDB%22:1%2C%22attachEvent%22:0%2C%22ActiveXObject%22:0%2C%22dispatchEvent%22:1%2C%22addBehavior%22:0%2C%22addEventListener%22:1%2C%22detachEvent%22:0%2C%22fireEvent%22:0%2C%22MutationObserver%22:1%2C%22HTMLMenuItemElement%22:1%2C%22Int8Array%22:1%2C%22postMessage%22:1%2C%22querySelector%22:1%2C%22getElementsByClassName%22:1%2C%22images%22:1%2C%22compatMode%22:%22CSS1Compat%22%2C%22documentMode%22:0%2C%22all%22:1%2C%22now%22:1%2C%22contextMenu%22:null%7D&lt=1539049451538%2B8&ps=true%2Ctrue&cv=19f5de4bfc86212d44a26e1fab8d56ca489459d4&fp=false&sp=false&br=Firefox&ieps=false&av=false&z=%7B%22a%22:172961767%2C%22b%22:1%2C%22c%22:0%7D&zh=&jsv=1.5&nav=%7B%22userAgent%22:%22Mozilla%2F5.0%20(Windows%20NT%206.1%3B%20Win64%3B%20x64%3B%20rv:56.0)%20Gecko%2F20100101%20Firefox%2F56.0%22%2C%22appName%22:%22Netscape%22%2C%22appCodeName%22:%22Mozilla%22%2C%22appVersion%22:%225.0%20(Windows)%22%2C%22appMinorVersion%22:0%2C%22product%22:%22Gecko%22%2C%22productSub%22:%2220100101%22%2C%22vendor%22:%22%22%2C%22vendorSub%22:%22%22%2C%22buildID%22:%2220170926190823%22%2C%22platform%22:%22Win64%22%2C%22oscpu%22:%22Windows%20NT%206.1%3B%20Win64%3B%20x64%22%2C%22hardwareConcurrency%22:4%2C%22language%22:%22zh-CN%22%2C%22languages%22:%5B%22zh-CN%22%2C%22zh%22%2C%22en-US%22%2C%22en%22%5D%2C%22systemLanguage%22:0%2C%22userLanguage%22:0%2C%22doNotTrack%22:%22unspecified%22%2C%22msDoNotTrack%22:0%2C%22cookieEnabled%22:true%2C%22geolocation%22:1%2C%22vibrate%22:1%2C%22maxTouchPoints%22:0%2C%22webdriver%22:0%2C%22plugins%22:%5B%5D%7D&t=a96fe0888efd48c54cf70efcfeab93e9ed666c6c&u=542c7349d1aa5eacaaa115b02c07e164&fc=true";
//		httpVo  = this.httpProxyResultVoPost(httpClientSessionVo,pixelUrl,param,"other");
		
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("X-Requested-With", "XMLHttpRequest");
		headerMap.put("Referer","https://www.jetstar.com/cn/zh/flights?origin=CAN&destination=SIN&flight-type=1&selected-departure-date=15-10-2018&adult=1&flexible=1&currency=CNY");
		httpVo  = this.httpProxyResultVoGet(httpClientSessionVo, "https://www.jetstar.com/cn/zh/services/travelalertsservice","html");
		
		headerMap.put("Accept", "*/*");
		headerMap.put("Referer","https://www.jetstar.com/cn/zh/flights?origin=CAN&destination=SIN&flight-type=1&selected-departure-date=15-10-2018&adult=1&flexible=1&currency=CNY");
		headerMap.put("Origin", "https://www.jetstar.com");
		headerMap.put("Host", "inventory-reserveair-ae.azurewebsites.net");
		String res = this.httpProxyGet(httpClientSessionVo,"https://inventory-reserveair-ae.azurewebsites.net/api/query/availability/aggregated?flightCount=1&cultureInfo=zh-CN&from=20181015&end=20181016&departures=CAN&arrivals=SIN&paxCount=1&includeFees=false&currencyCode=CNY&direction=outbound","html");
		JSONObject object = JSONObject.fromObject(res);
		String CorrelationId = object.getString("correlationId");
		String currency = object.getString("currencyCode");
		JSONObject routes = object.getJSONObject("routes");
		JSONObject cansin = routes.getJSONObject("cansin");
		JSONObject flights = cansin.getJSONObject("flights");
		JSONObject dateObj = flights.getJSONArray("20181015").getJSONObject(0);
		String FlightId = dateObj.getString("flightId");
		String departureTime = dateObj.getString("departureTime");
		String arrivalTime = dateObj.getString("arrivalTime");
		String price = dateObj.getString("price");
		String searchUrl ="https://booking.jetstar.com/cn/zh/booking/search-flights?origin1=CAN&destination1=SIN&departuredate1=2018-10-15&adults=1&children=0&infants=0&AutoSubmit=Y&currency=CNY&dotcomFCPricesHidden=true&dotcomFCOutboundFlightId=%FlightId%&dotcomFCOutboundFare=852.57&dotcomFCOutboundPriceShown=false&dotcomFCOutboundDepartureTime=2018-10-15T03%3A35%3A00&dotcomFCOutboundArrivalTime=2018-10-15T10%3A10%3A00&dotcomFCOutboundCorrelationId=%CorrelationId%&dotcomFCOutboundMemberPrice=false";
		String SearchUrl = searchUrl.replace("%FlightId%", FlightId).replace("%CorrelationId%", CorrelationId);
		
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Host", "booking.jetstar.com");
		headerMap.remove("Origin");
		headerMap.put("Cookie",cookies);
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,SearchUrl,"","other");
		
		System.out.println(httpVo.getHttpResult());
		
		return null;

	}
	
	public static void main(String[] args) throws Exception {
		
//		String flights = "BL~ 613~ ~~CAN~04/29/2017 03:35~SGN~04/29/2017 05:20~^BL~ 655~ ~~SGN~04/29/2017 07:10~SIN~04/29/2017 10:10~";
//		String[] strs = flights.split("~\\^");
//		for(String str: strs){
//			String[] segs= str.split("~");
//			for(int i=0;i<segs.length;i++){
//				System.out.println(i+":"+segs[i].trim());
//			}
//		}
		
		
		
		String fline = "PUS-TPE;TAE-TPE;TAE-DMK;TAE-MFM;CJU-TPE;FUK-TPE;FUK-DMK;FUK-MFM;HKD-TPE;HKD-DMK;HKD-MFM;NGO-TPE;NGO-DMK;NGO-MFM;OKJ-TPE;OKA-TPE;OKA-DMK;OKA-MFM;SDJ-TPE;SDJ-DMK;SDJ-MFM;HND-TPE;HND-DMK;HND-MFM;KIX-TPE;KIX-KHH;KIX-DMK;KIX-MFM;NRT-TPE;NRT-KHH;NRT-DMK;NRT-MFM;RMQ-MFM;TPE-PUS;TPE-TAE;TPE-CJU;TPE-FUK;TPE-HKD;TPE-NGO;TPE-OKJ;TPE-OKA;TPE-SDJ;TPE-HND;TPE-KIX;TPE-NRT;TPE-DMK;TPE-MFM;KHH-OKA;KHH-KIX;KHH-NRT;KHH-DMK;KHH-MFM;DMK-TAE;DMK-FUK;DMK-HKD;DMK-NGO;DMK-OKA;DMK-SDJ;DMK-HND;DMK-KIX;DMK-NRT;DMK-TPE;DMK-KHH;DMK-MFM;MFM-TAE;MFM-FUK;MFM-HKD;MFM-NGO;MFM-OKA;MFM-SDJ;MFM-HND;MFM-KIX;MFM-NRT;MFM-RMQ;MFM-TPE;MFM-KHH;MFM-DMK";
		String fcurr = "CAN-CNY;SGN-VND;SWA-CNY;SIN-SGD;WUH-CNY;CXR-VND;BNK-AUD;SYD-AUD;BNE-AUD;CNS-AUD;HBA-AUD;MEL-AUD;NTL-AUD;PPP-AUD;TSV-AUD;DPS-IDR;DRW-AUD;OOL-AUD;ADL-AUD;HTI-AUD;AVV-AUD;LST-AUD;MCY-AUD;PER-AUD;KIX-JPY;AKL-NZD;ZQN-NZD;BKK-THB;CHC-NZD;HNL-USD;NAN-AUD;HKG-HKG;HAN-VND;KNO-IDR;SUB-IDR;FUK-JPY;NRT-JPY;KOJ-JPY;NGO-JPY;MYJ-JPY;OKA-JPY;SPK-JPY;TAK-JPY;OIT-JPY;KUL-MYR;RGN-USD;NPE-NZD;NSN-NZD;WLG-NZD;DUD-NZD;NPL-NZD;PMR-NZD;MNL-PHP;TPE-TWD;HKT-THB;DAD-VND;BMV-VND;VII-VND;DLI-VND;HPH-VND;TBB-VND;HUI-VND;PXU-VND;VCL-VND;PQC-VND;UIH-VND;MKY-AUD;AYQ-AUD;RAR-NZD;PKU-IDR;KMJ-JPY;PEN-MYR;HAK-CNY;VDH-VND;PNH-USD;REP-USD;KWE-CNY;CGK-IDR;PLM-IDR;SYX-CNY;HIS-AUD;CTS-JPY;MFM-MOP;THD-VND;PVG-CNY";
		String[] lineList = fline.split(";");
		String[] currList = fcurr.split(";");
		String nline = "";
		int i=1;
		for(String lines : lineList){
			String[] line = lines.split("-");
			for(String currs : currList){
				String[] curr = currs.split("-");
				if(line[0].equals(curr[0])){
					nline +=lines+"-"+curr[1]+";";
					System.out.println(i+":"+lines+"-"+curr[1]+";");
					i++;
				}
			}
		}
		System.out.println(nline);
	}
	
	public static String dateToStamp(Date date) throws Exception{
        String res;
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        Date d = simpleDateFormat.parse(format);
        long ts = d.getTime();
        res = String.valueOf(ts);
        return res;
    }
}