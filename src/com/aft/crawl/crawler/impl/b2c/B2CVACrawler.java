package com.aft.crawl.crawler.impl.b2c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.MyStringUtil;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.cookie.CookieUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 澳大利亚航空
 * @author chenminghong
 */

public class B2CVACrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	
	public B2CVACrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
//		String httpResult = StringTxtUtil.TxtToString("D:\\test.txt");
//		String jsonResult = MyStringUtil.getValue("var templateData \\=", "\"AIR\\_SELECT\\_PAGE\"\\}", httpResult).trim()+"\"AIR_SELECT_PAGE\"}";
//		System.out.println(jsonResult);
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(httpResult==null) return crawlResults;
		try {
			JSONObject parse = JSONObject.parseObject(httpResult);
			String type = parse.getString("type");
			if(!"SUCCESS".equals(type)) return crawlResults;
			JSONObject data = parse.getJSONObject("data");
			JSONArray dations = data.getJSONArray("availableRecommendations");
			int dationSize = dations.size();
			if(dationSize==0) return crawlResults;
			Map<String ,String> priceMap = new HashMap<String ,String>();
			for(int p = 0; p<dationSize; p++){
				JSONObject priObj = dations.getJSONObject(p);
				JSONObject amadeus =  priObj.getJSONObject("amadeusFareBreakdown");
				JSONObject typeFares =  amadeus.getJSONArray("passengerTypeFares").getJSONObject(0);
				String amount = typeFares.getString("amount");//总价
				String baseAmount = typeFares.getString("baseAmount");//票面价
				priceMap.put(amount,baseAmount);
			}
			JSONObject route = data.getJSONArray("routes").getJSONObject(0);
			JSONObject day = route.getJSONArray("days").getJSONObject(0);
			JSONArray flights = day.getJSONArray("flights");
			int flightSize = flights.size();
			FlightData flightData = null;
			for(int f = 0;f<flightSize; f++){
				JSONObject flight = flights.getJSONObject(f);
				JSONObject booking = flight.getJSONObject("bookingPriceTagHolder");
				JSONArray priceTags = booking.getJSONArray("priceTags");
				int priSize = priceTags.size();
				if(priSize==0) continue;
				
				//航段
				List<FlightSegment> fss = new ArrayList<FlightSegment>();
				JSONArray segments = flight.getJSONArray("segments");
				int segmentSize = segments.size();
				for(int s = 0;s<segmentSize; s++){
					FlightSegment fs = new FlightSegment();
					fs.setTripNo(s+1);
					JSONObject seg = segments.getJSONObject(s);
					String depCode = seg.getJSONObject("originAirport").getString("code");
					String desCode = seg.getJSONObject("destinationAirport").getString("code");
					String flightNumber = seg.getString("flightNumber");
					String flightEn = flightNumber.substring(0,2);
					SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String depTime = seg.getString("departureDateTimeDisplay").substring(0,19);
					String desTime = seg.getString("arrivalDateTimeDisplay").substring(0, 19);
					depTime = sdf2.format(sdf1.parse(depTime));
					desTime = sdf2.format(sdf1.parse(desTime));
					fs.setAirlineCode(flightEn);
					fs.setFlightNumber(flightNumber);
					fs.setCodeShare("N");
					fs.setDepAirport(depCode);
					fs.setArrAirport(desCode);
					fs.setDepTime(depTime);
					fs.setArrTime(desTime);
					fss.add(fs);
				}
				for(int p=0; p<priSize; p++){
					JSONObject priObj = priceTags.getJSONObject(p);
					JSONObject converted = priObj.getJSONObject("convertedPrices");
					JSONArray flightClasses = priObj.getJSONArray("flightClasses");
					JSONObject CNY = converted.getJSONObject("CNY");
					String amount = CNY.getString("amount");
					String barePri = priceMap.get(amount);
					if(barePri==null) continue;
					BigDecimal totalPri = null;
					if(amount.contains("E+")){
						totalPri = getEprice(amount);
					}else{
						totalPri = new BigDecimal(amount).setScale(2,BigDecimal.ROUND_UP);
					}
					BigDecimal bare = new BigDecimal(barePri).setScale(2, BigDecimal.ROUND_UP);
					BigDecimal tax = totalPri.subtract(bare);
					
					flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				    flightData.setAirlineCode("TK");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					
					List<FlightPrice> prices = new ArrayList<FlightPrice>();
					FlightPrice fPrice = new FlightPrice();
					fPrice.setCurrency("CNY");
					fPrice.setEquivCurrency("CNY");
					fPrice.setEquivFare(barePri);
					fPrice.setFare(barePri);
					fPrice.setEquivTax(tax.toString());
					fPrice.setTax(tax.toString());
					fPrice.setPassengerType("ADT");
					prices.add(fPrice);
					flightData.setPrices(prices);
					
					List<FlightSegment> newFss = new ArrayList<FlightSegment>();
					int c = 0;
					for(FlightSegment fs:fss){
						FlightSegment newFs = (FlightSegment)fs.clone();
						String cabin = flightClasses.getString(c);
						newFs.setCabin(cabin);
						newFs.setCabinCount("9");
						newFss.add(newFs);
						c++;
					}
					flightData.setFromSegments(newFss);
					crawlResults.add(flightData);
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	private BigDecimal getEprice(String amount) {//1.85E+4 --> 18500
		String[] ss = amount.split("E+");
		String s1 = ss[0];
		String s2 = ss[1].substring(1);
		Integer num = Integer.valueOf(s2);
		BigDecimal tenV = new BigDecimal(10);
		while(num>1){
			tenV = tenV.multiply(new BigDecimal(10));
			num--;
		}
		return tenV.multiply(new BigDecimal(s1)).setScale(2, BigDecimal.ROUND_UP);
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
		
		Long timeStamp = System.currentTimeMillis();
		Long timeStamp2 = timeStamp-100;
		String timeS = String.valueOf(timeStamp);
//		String Url = "https://securepubads.g.doubleclick.net/gampad/ads?gdfp_req=1&pvsid=1008123150670933&correlator=89078307130800&output=json_html&callback=googletag.impl.pubads.setAdContentsBySlotForSync&impl=ss&adsid=NT&json_a=1&eid=953563515%2C21063218%2C21063156&vrg=2019021101&guci=2.2.0.0.2.2.0.0&plat=1%3A1081352%2C2%3A1081352%2C8%3A32776&sc=1&sfv=1-0-32&iu_parts=7421032%2CVirginHomeBelowFold&enc_prev_ius=%2F0%2F1&prev_iu_szs=728x90&eri=32&cust_params=pos%3Dau%26lang%3Den&cookie_enabled=1&bc=13&abxe=1&lmt="+timeS.substring(0,10)+"&dt="+timeS+"&dlt="+String.valueOf(timeStamp2)+"&idt=1252&frm=20&biw=1920&bih=945&oid=3&adxs=596&adys=4591&adks=2005175832&ucis=1&ifi=1&u_tz=480&u_his=3&u_h=1080&u_w=1920&u_ah=1040&u_aw=1920&u_cd=24&u_sd=1&flash=0&url=https%3A%2F%2Fwww.virginaustralia.com%2Fau%2Fcn%2F&dssz=23&icsg=176112&std=4&csl=58&vis=1&scr_x=0&scr_y=0&psz=1920x120&msz=728x-1&ga_vid=988649040."+timeS.substring(0,10)+"&ga_sid="+timeS.substring(0,10)+"&ga_hid=1726543791&fws=4";
//		String url = "https://securepubads.g.doubleclick.net/gampad/ads?gdfp_req=1&pvsid=501131187954762&correlator=1710724549424395&output=json_html&callback=googletag.impl.pubads.setAdContentsBySlotForSync&impl=ss&adsid=NT&json_a=1&eid=953563515%2C21061742%2C21062069%2C21063065%2C21063137&vrg=306&guci=2.2.0.0.2.2.0.0&plat=1%3A1081352%2C2%3A1081352%2C8%3A32776&sc=1&sfv=1-0-32&iu_parts=7421032%2CVirginHomeBelowFold&enc_prev_ius=%2F0%2F1&prev_iu_szs=728x90&eri=32&cust_params=pos%3Dau%26lang%3Den&cookie_enabled=1&bc=13&abxe=1&lmt=1550538251&dt=1550538251571&dlt=1550538174207&idt=2115&frm=20&biw=1920&bih=945&oid=3&adxs=596&adys=4591&adks=2005175832&ucis=1&ifi=1&u_tz=480&u_his=3&u_h=1080&u_w=1920&u_ah=1040&u_aw=1920&u_cd=24&u_sd=1&flash=0&url=https%3A%2F%2Fwww.virginaustralia.com%2Fau%2Fcn%2F&dssz=23&icsg=176112&std=4&csl=58&vis=1&scr_x=0&scr_y=0&psz=1920x120&msz=728x-1&ga_vid=1703755692.1550538252&ga_sid=1550538252&ga_hid=968095188&fws=4";
		String Url = "https://securepubads.g.doubleclick.net/gampad/ads?gdfp_req=1&pvsid=478529810357357&correlator=4363002130370121&output=json_html&callback=googletag.impl.pubads.setAdContentsBySlotForSync&impl=ss&adsid=NT&json_a=1&eid=21061865&vrg=308&guci=2.2.0.0.2.2.0.0&plat=1%3A1081352%2C2%3A1081352%2C8%3A32776&sc=1&sfv=1-0-32&iu_parts=7421032%2CVirginHomeBelowFold&enc_prev_ius=%2F0%2F1&prev_iu_szs=728x90&eri=32&cust_params=pos%3Dau%26lang%3Den&cookie_enabled=1&bc=13&abxe=1&lmt=1550803152&dt=1550803152908&dlt=1550803130843&idt=1423&frm=20&biw=1920&bih=945&oid=3&adxs=596&adys=4591&adks=2005175832&ucis=1&ifi=1&u_tz=480&u_his=3&u_h=1080&u_w=1920&u_ah=1040&u_aw=1920&u_cd=24&u_sd=1&flash=0&url=https%3A%2F%2Fwww.virginaustralia.com%2Fau%2Fcn%2F&dssz=31&icsg=721354752&std=4&csl=58&vis=1&scr_x=0&scr_y=0&psz=1920x120&msz=728x-1&ga_vid=1084670072.1550803153&ga_sid=1550803153&ga_hid=698037104&fws=4";
		Map<String, Object> headerMap1 = new HashMap<String, Object>();
		headerMap1.put("Accept", "*/*");
		headerMap1.put("Accept-Encoding", "gzip, deflate, br");
		headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap1.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap1.put("Referer","https://www.virginaustralia.com/au/cn/");
		headerMap1.put("Connection", "keep-alive");
		headerMap1.put("Host", "securepubads.g.doubleclick.net");
//		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,Url,"other");
//		String result = httpVo.getHttpResult();
//		String id = MyStringUtil.getValue("\"_value_\":\"ID=", "\"", result);
//		String cookie = "__gads=ID="+id;
		
//		headerMap1.put("Accept", "application/json, text/javascript, */*; q=0.01");
//		headerMap1.put("X-Requested-With","XMLHttpRequest");
//		headerMap1.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
//		headerMap1.put("Host", "www.virginaustralia.com");
//		headerMap1.put("Cookie",cookie);
//		String param1 = "recent-searches=-1&use_points=0&trip_type=1&origin=SYD&originSurrogate=%E6%82%89%E5%B0%BC+(SYD)&flights-origins-list=&destination=CTU&destinationSurrogate=%E6%88%90%E9%83%BD+(CTU)&flights-destinations-list=ADL&AcccOrigin1=AU&AcccOrigin=AU&date_start_display=20+Feb%2C+2019&date_start=20+Feb%2C+2019&date_end_display=&date_end=&date_flexible=0&is_vipr=0&adults=1&children=0&infants=0&update-guest-button=%E6%9B%B4%E6%96%B0%E4%B9%98%E5%AE%A2&travel_class=E&promoCode=&ap3-flights-submit=%E6%9F%A5%E8%AF%A2%E8%88%AA%E7%8F%AD&travelOptionsCarRental=true&travelOptionsCarRental_cb=on";
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://www.virginaustralia.com/au/cn/mp-asset/json/submission/findFlight.json",param1,"other");
//		String postRes = httpVo.getHttpResult();
		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap1.put("Content-Type","application/x-www-form-urlencoded");
		headerMap1.put("Upgrade-Insecure-Requests","1");
//		headerMap1.put("Cookie",cookie);
		headerMap1.put("Host", "cn.fly.virginaustralia.com");
		String param = "isAward=FALSE&journeySpan=OW&cabinClass=ECONOMY&origin=SYD&destination=CSX&departureDate=2019-02-26&numAdults=1&numChildren=0&numInfants=0&checkInDate=2019-02-26&checkOutDate=2019-02-28&hotelCheckInDateText=2019-02-26&hotelCheckOutDateText=2019-02-28&promoCode=&origin1=SYD&destination1=CSX&alternativeLandingPage=true&departureDateText=2019-02-26&searchType=NORMAL&redemptionBooking=FALSE&countAdult=1&countChild=0&countInfant=0&searchCars=true&searchHotels=true&referrerCode=&cabin=ECONOMY&tripType=OW";
//		String param = "isAward%3DFALSE%26journeySpan%3DOW%26cabinClass%3DECONOMY%26origin%DdSYD%26destination%3DCSX%26departureDate%3D2019-02-26%26numAdults%3D1%26numChildren%3D0%26numInfants%3D0%26checkInDate%3D2019-02-26%26checkOutDate%3D2019-02-28%26hotelCheckInDateText%3D2019-02-26%26hotelCheckOutDateText%3D2019-02-28%26promoCode%3D%26origin1%3DSYD%26destination1%3DCSX%26alternativeLandingPage%3Dtrue%26departureDateText%3D2019-02-26%26searchType%3DNORMAL%26redemptionBooking%3DFALSE%26countAdult%3D1%26countChild%3D0%26countInfant%3D0%26searchCars%3Dtrue%26searchHotels%3Dtrue%26referrerCode%3D%26cabin%3DECONOMY%26tripType%3DOW";
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://cn.fly.virginaustralia.com/SSW2010/VAVA/webqtrip.html",param,"other");
		String location = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Location");
		String cookie2 = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
//		cookie = cookie+";"+cookie2;
		headerMap1.remove("Content-Type");
		headerMap1.put("Cookie",cookie2);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,location,"other");
		String res = httpVo.getHttpResult();
		System.out.println(res);
		writeFile(res);
//		String jsonResult = MyStringUtil.getValue("var templateData \\=", "\"AIR\\_SELECT\\_PAGE\"\\}", res).trim()+"\"AIR_SELECT_PAGE\"}";
//		System.out.println(jsonResult);
		return res;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
//		if (httpResult!=null &&httpResult.contains("You don't have permission to access")) {
//			return true;
//		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);
	}
	
	public static String addOneday(String today){
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		try{
			Date d = new Date(f.parse(today).getTime()+24*3600*1000);
			return f.format(d);
			}
		catch(Exception e){
			return today;
		}
	}
	
	public void writeFile(String str) {
		try {

			   File file = new File("D:\\filename.txt");
			   if (!file.exists()) {
			    file.createNewFile();
			   }
			   FileWriter fw = new FileWriter(file.getAbsoluteFile());
			   BufferedWriter bw = new BufferedWriter(fw);
			   bw.write(str);
			   bw.close();
			   System.out.println("Done");
			  } catch (IOException e) {
			   e.printStackTrace();
			  }
	}
	
}
