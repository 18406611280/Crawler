package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
 * 加拿大航空
 * @author chenminghong
 */

public class B2CACCrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	
	public B2CACCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
//		String httpResult = StringTxtUtil.TxtToString("D:\\test.txt");
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
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
		String Url = "https://www.qatarairways.com/zh-cn/homepage.html";
		Map<String, Object> headerMap1 = new HashMap<String, Object>();
		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap1.put("Accept-Encoding", "gzip, deflate, br");
		headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap1.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap1.put("Upgrade-Insecure-Requests","1");
		headerMap1.put("Connection", "keep-alive");
		headerMap1.put("Host", "www.qatarairways.com");
		httpClientSessionVo.setHeaderMap(headerMap1);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,Url,"other");
		String publicRes = MyStringUtil.getValue("src=\"/public/", "\"", httpVo.getHttpResult());
		String pulbicUrl = "https://www.qatarairways.com/public/"+publicRes;
		String _abck = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","_abck=");
		String subAbck = _abck.replace("_abck=", "");
		String ak_bmsc = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","ak_bmsc=");
		String bm_sz = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","bm_sz=");
		
		String param = "{\"sensor_data\":\"7a74G7m23Vrp0o5c9058291.4-1,2,-94,-100,Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0,uaend,2867,20100101,zh-CN,Gecko,0,0,0,0,381425,1454161,1920,1040,1920,1080,1920,945,1936,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:1,sc:0,wrc:1,isc:95,vib:1,bat:0,x11:0,x12:1,5515,0.625692439312,775105727080,loc:-1,2,-94,-101,do_en,dm_en,t_dis-1,2,-94,-105,0,-1,0,0,1873,-1,0;0,-1,0,1,2082,-1,0;0,-1,0,0,2089,-1,0;0,-1,0,1,2088,-1,0;0,-1,0,1,2874,538,0;0,-1,0,0,2528,936,0;0,-1,0,0,2112,520,0;0,-1,0,0,1805,-1,0;0,-1,0,1,620,-1,0;0,-1,0,1,411,-1,0;0,-1,0,0,1300,-1,0;0,-1,0,0,1081,-1,0;0,-1,0,0,1347,-1,0;0,-1,0,0,1267,-1,0;0,-1,0,0,741,-1,0;0,-1,0,0,835,-1,1;0,-1,0,0,601,-1,0;0,-1,0,0,1127,-1,0;0,-1,0,0,1082,-1,0;0,-1,0,0,873,-1,0;0,-1,0,1,520,-1,0;0,-1,0,1,1473,-1,0;0,-1,0,0,2042,-1,0;0,-1,0,0,904,-1,0;0,-1,0,1,925,-1,0;0,-1,0,0,1919,441,0;0,-1,0,0,1923,1801,0;0,-1,0,0,1807,1685,0;0,-1,0,0,2495,1081,0;0,-1,0,0,2299,2299,0;0,-1,0,0,2305,2305,0;0,-1,0,0,2057,2057,0;0,-1,0,0,2113,2113,0;0,-1,0,0,2332,2332,0;0,-1,0,0,2099,2099,0;0,-1,0,0,2110,2110,0;0,-1,0,0,2440,2440,0;0,-1,0,0,2892,2892,0;-1,2,-94,-102,0,-1,0,0,1873,-1,0;0,-1,0,1,2082,-1,0;0,-1,0,0,2089,-1,0;0,-1,0,1,2088,-1,0;0,-1,0,1,2874,538,0;0,-1,0,0,2528,936,0;0,-1,0,0,2112,520,0;0,-1,0,0,1805,-1,0;0,0,0,1,620,-1,0;0,0,0,1,411,-1,0;0,-1,1,0,1300,-1,0;0,-1,1,0,1081,-1,0;0,-1,1,0,1347,-1,0;0,-1,0,0,1267,-1,0;0,-1,0,0,741,-1,0;0,-1,0,0,835,-1,1;0,-1,0,0,601,-1,0;0,-1,0,0,1127,-1,0;0,0,0,0,1082,-1,0;0,0,0,0,873,-1,0;0,-1,0,1,520,-1,0;0,0,0,1,1473,-1,0;0,-1,0,0,2042,-1,0;0,-1,0,0,904,-1,0;0,-1,0,1,925,-1,0;0,-1,0,0,1919,441,0;0,-1,0,0,1923,1801,0;0,-1,0,0,1807,1685,0;0,-1,0,0,2495,1081,0;0,-1,0,0,2299,2299,0;0,-1,0,0,2305,2305,0;0,-1,0,0,2057,2057,0;0,-1,0,0,2113,2113,0;0,-1,0,0,2332,2332,0;0,-1,0,0,2099,2099,0;0,-1,0,0,2110,2110,0;0,-1,0,0,2440,2440,0;0,-1,0,0,2892,2892,0;0,-1,0,0,-1,2230,0;0,-1,0,0,-1,1983,0;0,-1,0,0,-1,2230,0;0,-1,0,0,-1,1983,0;-1,2,-94,-108,-1,2,-94,-110,0,1,6163,1030,605;1,1,6179,1027,610;2,1,6184,1024,612;3,1,6198,1023,614;4,1,6225,1017,621;5,1,6231,1015,623;6,1,6333,981,640;7,1,6343,976,642;8,1,6344,970,644;9,1,6352,967,646;10,1,6373,958,652;11,1,6375,955,652;12,1,6383,952,655;13,1,6394,949,656;14,1,6409,945,658;15,1,6410,943,659;16,1,6429,934,663;17,1,6434,931,664;18,1,6440,925,667;19,1,6448,919,670;20,1,6455,914,673;21,1,6466,910,675;22,1,6473,907,678;23,1,6479,902,680;24,1,6510,893,685;25,1,6512,891,687;26,1,6528,883,691;27,1,6567,867,701;28,1,6568,863,704;29,1,6578,859,705;30,1,6594,851,709;31,1,6611,846,711;32,1,6628,840,714;33,1,6631,838,714;34,1,6652,835,716;35,1,6658,835,717;36,1,6703,826,719;37,1,6704,825,719;38,1,6715,822,720;39,1,6719,817,721;40,1,6733,814,723;41,1,6740,809,724;42,1,6743,807,724;43,1,6751,803,725;44,1,6759,800,726;45,1,6767,798,727;46,1,6776,796,727;47,1,6791,794,727;48,1,6800,793,727;49,1,6816,791,727;50,1,6823,789,727;51,1,6831,786,727;52,1,6839,783,727;53,1,6847,780,727;54,1,6855,778,727;55,1,6863,775,727;56,1,6871,771,727;57,1,6879,768,727;58,1,6887,764,727;59,1,6903,755,727;60,1,6913,751,727;61,1,6923,746,727;62,1,6935,741,726;63,1,6936,736,725;64,1,6959,723,721;65,1,6960,717,720;66,1,6972,711,717;67,1,6992,702,715;68,1,6993,698,713;69,1,7004,695,712;70,1,7010,691,711;71,1,7016,689,711;72,1,7027,686,708;73,1,7047,682,705;74,1,7087,675,700;75,1,7088,673,700;76,1,7098,671,698;77,1,7123,668,696;78,1,7127,666,695;79,1,7137,664,693;80,1,7154,661,692;81,1,7186,653,685;82,1,7193,650,684;83,1,7200,648,682;84,1,7207,646,681;85,1,7215,644,680;86,1,7223,641,678;87,1,7231,638,677;88,1,7239,635,677;89,1,7256,630,675;90,1,7263,627,674;91,1,7271,626,674;92,1,7281,623,672;93,1,7297,621,671;94,1,7298,620,671;95,1,7304,619,671;96,1,7316,618,670;97,1,7329,617,670;98,1,7355,616,669;99,1,7407,612,667;103,3,7615,603,660,-1;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,-1,2,-94,-112,https://www.qatarairways.com/zh-cn/homepage.html-1,2,-94,-115,1,842486,0,0,0,0,842486,7615,0,"+String.valueOf(System.currentTimeMillis())+",9,16583,0,104,2763,0,0,7621,687859,0,"+subAbck+",8113,925,-986967636,26067376-1,2,-94,-106,1,1-1,2,-94,-119,121,31,33,37,52,50,49,54,49,7,7,96,51,185,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,1868657601;dis;;true;true;true;-480;true;24;24;true;false;unspecified-1,2,-94,-80,5891-1,2,-94,-116,7270796-1,2,-94,-118,220840-1,2,-94,-121,;6;15;0\"}";
		String cookie = CookieUtil.addCookies(_abck,ak_bmsc,bm_sz);
		headerMap1.put("Accept", "*/*");
		headerMap1.put("Content-Type","text/plain;charset=UTF-8");
		headerMap1.put("Referer","https://www.qatarairways.com/zh-cn/homepage.html");
		headerMap1.put("Cookie", cookie);
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,pulbicUrl,param,"other");
		String _abck2 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","_abck=");
		
		String cookie2 = CookieUtil.addCookies(_abck2,ak_bmsc,bm_sz);
		headerMap1.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap1.put("X-Requested-With","XMLHttpRequest");
		headerMap1.put("Cookie", cookie2);
		String bookUrl = "https://www.qatarairways.com/content/Qatar/common/routes/booking_toRoutes/cn/CAN.json";
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,bookUrl,"other");
		String bm_sv = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","bm_sv=");
		
		String showUrl = "https://booking.qatarairways.com/nsp/views/showBooking.action?widget=QR&searchType=F&addTaxToFare=Y&minPurTime=0&upsellCallId=&allowRedemption=Y&flexibleDate=Off&bookingClass=E&tripType=O&selLang=zh&fromStation=CAN&from=%E5%B9%BF%E5%B7%9E&toStation=MXP&to=%E7%B1%B3%E5%85%B0&departingHidden=20-2%E6%9C%88-2019&departing=2019-02-20&returningHidden=&returning=&adults=1&children=0&infants=0&teenager=0&ofw=0&promoCode=";
		String cookie3 = CookieUtil.addCookies(_abck2,ak_bmsc,bm_sz,bm_sv);
		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap1.remove("X-Requested-With");
		headerMap1.put("Cookie", cookie3);
		headerMap1.put("Host", "www.qatarairways.com");
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,showUrl,"","other");
		String BIGipServerbooking = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","BIGipServerbooking-qrcom-pool=");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","JSESSIONID=");
		
		
		
		
		
		
		
		
		
		
		
		
//		String Url = "https://www.aircanada.com/cn/zh/aco/home.html";
//		Map<String, Object> headerMap1 = new HashMap<String, Object>();
//		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		headerMap1.put("Accept-Encoding", "gzip, deflate, br");
//		headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//		headerMap1.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
//		headerMap1.put("Upgrade-Insecure-Requests","1");
//		headerMap1.put("Connection", "keep-alive");
//		headerMap1.put("Host", "www.aircanada.com");
//		httpClientSessionVo.setHeaderMap(headerMap1);
//		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,Url,"other");
//		String fCookie = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
////		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
//		String result = httpVo.getHttpResult();
//		String assets = MyStringUtil.getValue("src=\"/assets/", "\"", result);
//		String _abck = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","_abck=");
//		
//		String assUrl = "https://www.aircanada.com/assets/"+assets;
//		String param = "{\"sensor_data\":\"7a74G7m23Vrp0o5c9058141.4-1,2,-94,-100,Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0,uaend,2867,20100101,zh-CN,Gecko,0,0,0,0,381421,5945421,1920,1040,1920,1080,1920,945,1936,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:1,sc:0,wrc:1,isc:95,vib:1,bat:0,x11:0,x12:1,5515,0.427023472213,775097972709.5,loc:-1,2,-94,-101,do_en,dm_en,t_dis-1,2,-94,-105,0,-1,0,0,1151,113,0;0,-1,0,0,1200,113,0;0,0,0,0,5546,-1,0;0,0,0,0,6084,-1,0;0,0,0,0,5546,-1,0;0,0,0,0,6084,-1,0;0,0,0,0,2135,-1,0;0,0,0,0,2135,-1,0;0,-1,0,0,1811,-1,0;0,0,0,0,1960,-1,0;0,0,0,0,1960,-1,0;0,0,0,0,1660,-1,0;0,0,0,0,1660,-1,0;0,0,0,0,1660,-1,0;0,-1,0,0,1611,-1,0;0,-1,0,0,936,936,0;0,0,0,0,2001,1027,0;0,0,0,0,2010,1565,0;0,0,0,0,1558,-1,0;0,0,0,0,1558,-1,0;0,0,0,0,1590,-1,0;0,0,0,0,1590,-1,0;0,-1,0,0,1829,-1,0;0,0,0,0,1335,-1,0;0,0,0,0,1607,-1,0;0,0,0,0,1869,-1,0;0,0,0,0,1869,-1,0;0,0,0,0,1816,-1,0;0,0,0,0,1816,-1,0;0,-1,0,0,1417,-1,0;0,-1,0,0,2424,-1,0;0,0,0,0,2124,-1,0;0,0,0,0,1681,-1,0;0,0,0,0,1681,-1,0;0,0,0,0,1698,-1,0;0,0,0,0,2144,-1,0;0,0,0,0,1701,-1,0;0,0,0,0,1701,-1,0;0,0,0,0,1718,-1,0;0,0,0,0,2657,-1,0;0,0,0,0,2657,-1,0;0,-1,0,0,2243,-1,0;0,-1,0,0,2584,-1,0;0,-1,0,0,2587,-1,0;0,-1,0,0,2424,-1,0;0,-1,0,0,2355,-1,0;0,-1,0,0,2787,-1,0;0,-1,0,0,2611,1783,0;0,-1,0,0,2871,1884,0;0,-1,0,0,1884,-1,0;0,-1,0,0,1768,-1,0;0,0,0,0,1677,-1,0;0,-1,0,0,2421,-1,0;0,-1,0,0,2467,-1,0;0,0,0,0,2801,-1,0;0,0,0,0,2801,-1,0;0,0,0,0,2387,-1,0;0,0,0,0,2925,-1,0;0,0,0,0,2711,-1,0;0,0,0,0,2711,-1,0;-1,2,-94,-102,0,-1,0,0,1151,113,0;0,-1,0,0,1200,113,0;0,0,0,0,968,-1,0;0,0,0,0,1506,-1,0;0,0,0,0,1660,-1,0;0,-1,0,0,1611,-1,0;0,-1,0,0,936,936,0;-1,2,-94,-108,-1,2,-94,-110,0,1,6987,1295,116;1,1,7426,1235,187;2,1,7430,1228,193;3,1,7438,1220,201;4,1,7448,1215,205;5,1,7458,1209,210;6,1,7463,1203,214;7,1,7474,1198,219;8,1,7479,1191,225;9,1,7487,1184,230;10,1,7494,1175,234;11,1,7504,1165,240;12,1,7510,1156,245;13,1,7520,1146,251;14,1,7526,1133,258;15,1,7535,1122,263;16,1,7542,1111,268;17,1,7550,1096,273;18,1,7559,1081,281;19,1,7568,1066,287;20,1,7579,1048,294;21,1,7584,1028,301;22,1,7593,1008,307;23,1,7598,992,310;24,1,7606,975,316;25,1,7616,962,319;26,1,7623,951,321;27,1,7632,940,322;28,1,7638,930,325;29,1,7646,921,326;30,1,7654,914,326;31,1,7662,907,326;32,1,7670,899,326;33,1,7678,893,326;34,1,7688,886,326;35,1,7696,884,326;36,1,7702,882,326;37,1,7710,881,326;38,1,7720,878,326;39,1,7734,876,326;40,1,7742,875,326;41,1,7752,874,326;42,1,7766,873,326;43,1,7814,872,326;44,1,7839,871,326;45,1,7848,869,326;46,1,7855,866,326;47,1,7862,862,326;48,1,7870,860,326;49,1,7878,856,326;50,1,7886,853,326;51,1,7894,848,326;52,1,7902,846,326;53,1,7910,845,326;54,1,7918,843,326;55,1,7926,842,326;56,1,7950,841,326;57,1,7999,840,326;58,1,8011,839,326;59,3,8736,839,326,941;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,3,6997;-1,2,-94,-112,https://www.aircanada.com/cn/zh/aco/home.html-1,2,-94,-115,1,539336,0,0,0,0,539336,8736,0,"+String.valueOf(System.currentTimeMillis())+",7,16583,0,60,2763,0,0,8738,460785,0,"+_abck+",7912,29,650834862,26067376-1,2,-94,-106,1,1-1,2,-94,-119,185,135,30,34,35,35,28,33,49,7,8,265,399,177,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,1868657601;dis;;true;true;true;-480;true;24;24;true;false;unspecified-1,2,-94,-80,5891-1,2,-94,-116,148635530-1,2,-94,-118,168499-1,2,-94,-121,;4;24;0\"}";
//		headerMap1.put("Accept", "*/*");
//		headerMap1.put("Cookie",fCookie);
//		headerMap1.put("Content-Type","text/plain;charset=UTF-8");
//		headerMap1.put("Referer","https://www.aircanada.com/cn/zh/aco/home.html");
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,assUrl,param,"other");
//		String lCookie = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
//		headerMap1.put("Accept", "application/json, text/plain, */*");
//		headerMap1.put("Cookie",lCookie);
//		headerMap1.put("Content-Type","application/x-www-form-urlencoded");
//		headerMap1.put("Origin","https://www.aircanada.com");
//		String param2 = "B_LOCATION_TYPE_1=A&COUNTRY=CN&EXTERNAL_ID=GUEST&E_LOCATION_TYPE_1=A&IS_HOME_PAGE=TRUE&LANGUAGE=ZH&LANGUAGE_CHARSET=utf-8&USERID=GUEST&actionName=Override&countryOfResidence=CN&departure1=19%2F02%2F2019&dest1=YVR&numberOfAdults=1&numberOfChildren=0&numberOfInfants=0&numberOfYouth=0&org1=CAN&tripType=O";
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://book.aircanada.com/pl/AConline/en/CreatePNRServlet",param2,"other");
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://book.aircanada.com/pl/AConline/en/CreatePNRServlet",param2,"other");
		
		
//		
//		String general = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","akavpau_www_aircanada_com_general=");
//		String bm_sz = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","bm_sz=");
//		headerMap1.put("Cookie", fCookie);
//		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,location,"other");
//		String ak_bmsc = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","ak_bmsc=");
//		String Cookies = CookieUtil.addCookies(_abck,general,bm_sz,ak_bmsc);
//		headerMap1.put("Cookie", Cookies);
//		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,"https://www.aircanada.com/cn/zh/aco/home.html","other");
		
		
//		String res1 = httpVo.getHttpResult();
//		String pixel = MyStringUtil.getValue("src\\=\"https://www.turkishairlines.com/akam/10/", "\\?", res1);
//		String pub = MyStringUtil.getValue("\'\\/public\\/", "\'", res1);
//		String pageRequestId = MyStringUtil.getValue("PageRequestID\" content\\=\"", "\"", res1);
//		String publicUrl = "https://www.turkishairlines.com/public/"+pub;
//		
//		String pixelUrl = "https://www.turkishairlines.com/akam/10/"+pixel;
//		String timeStamp = String.valueOf(System.currentTimeMillis());
////		String piParam = "ap=true&bt=0&fonts=37%2C38%2C39%2C40%2C41%2C42%2C55%2C56%2C57%2C58%2C59%2C60%2C61%2C62%2C63%2C64%2C65%2C66%2C67%2C68%2C69&fh=136dc235e68ca231e453d3b8905739739097a551&timing=%7B%221%22%3A24%2C%222%22%3A180%2C%223%22%3A282%2C%224%22%3A391%2C%225%22%3A492%2C%226%22%3A594%2C%22profile%22%3A%7B%22bp%22%3A0%2C%22sr%22%3A0%2C%22dp%22%3A1%2C%22lt%22%3A0%2C%22ps%22%3A0%2C%22cv%22%3A12%2C%22fp%22%3A0%2C%22sp%22%3A0%2C%22br%22%3A0%2C%22ieps%22%3A0%2C%22av%22%3A0%2C%22z1%22%3A9%2C%22jsv%22%3A0%2C%22nav%22%3A1%2C%22z2%22%3A9%2C%22z3%22%3A1%2C%22z4%22%3A0%2C%22z5%22%3A0%2C%22z6%22%3A0%2C%22fonts%22%3A76%7D%2C%22main%22%3A8236%2C%22compute%22%3A24%2C%22send%22%3A670%7D&bp=&sr=%7B%22inner%22%3A%5B1536%2C728%5D%2C%22outer%22%3A%5B1550%2C838%5D%2C%22screen%22%3A%5B-7%2C-7%5D%2C%22pageOffset%22%3A%5B0%2C0%5D%2C%22avail%22%3A%5B1536%2C824%5D%2C%22size%22%3A%5B1536%2C864%5D%2C%22client%22%3A%5B1519%2C1369%5D%2C%22colorDepth%22%3A24%2C%22pixelDepth%22%3A24%7D&dp=%7B%22XDomainRequest%22%3A0%2C%22createPopup%22%3A0%2C%22removeEventListener%22%3A1%2C%22globalStorage%22%3A0%2C%22openDatabase%22%3A0%2C%22indexedDB%22%3A1%2C%22attachEvent%22%3A0%2C%22ActiveXObject%22%3A0%2C%22dispatchEvent%22%3A1%2C%22addBehavior%22%3A0%2C%22addEventListener%22%3A1%2C%22detachEvent%22%3A0%2C%22fireEvent%22%3A0%2C%22MutationObserver%22%3A1%2C%22HTMLMenuItemElement%22%3A1%2C%22Int8Array%22%3A1%2C%22postMessage%22%3A1%2C%22querySelector%22%3A1%2C%22getElementsByClassName%22%3A1%2C%22images%22%3A1%2C%22compatMode%22%3A%22CSS1Compat%22%2C%22documentMode%22%3A0%2C%22all%22%3A1%2C%22now%22%3A1%2C%22contextMenu%22%3Anull%7D&lt="+timeStamp+"%2B8&ps=true%2Ctrue&cv=8c4b6b86ede0789c48deda259a57f5a65f58b0fb&fp=false&sp=false&br=Firefox&ieps=false&av=false&z=%7B%22a%22%3A2014980786%2C%22b%22%3A1%2C%22c%22%3A0%7D&zh=&jsv=1.5&nav=%7B%22userAgent%22%3A%22Mozilla%2F5.0%20(Windows%20NT%2010.0%3B%20Win64%3B%20x64%3B%20rv%3A64.0)%20Gecko%2F20100101%20Firefox%2F64.0%22%2C%22appName%22%3A%22Netscape%22%2C%22appCodeName%22%3A%22Mozilla%22%2C%22appVersion%22%3A%225.0%20(Windows)%22%2C%22appMinorVersion%22%3A0%2C%22product%22%3A%22Gecko%22%2C%22productSub%22%3A%2220100101%22%2C%22vendor%22%3A%22%22%2C%22vendorSub%22%3A%22%22%2C%22buildID%22%3A%2220181001000000%22%2C%22platform%22%3A%22Win32%22%2C%22oscpu%22%3A%22Windows%20NT%2010.0%3B%20Win64%3B%20x64%22%2C%22hardwareConcurrency%22%3A8%2C%22language%22%3A%22zh-CN%22%2C%22languages%22%3A%5B%22zh-CN%22%2C%22zh%22%2C%22zh-TW%22%2C%22zh-HK%22%2C%22en-US%22%2C%22en%22%5D%2C%22systemLanguage%22%3A0%2C%22userLanguage%22%3A0%2C%22doNotTrack%22%3A%22unspecified%22%2C%22msDoNotTrack%22%3A0%2C%22cookieEnabled%22%3Atrue%2C%22geolocation%22%3A1%2C%22vibrate%22%3A1%2C%22maxTouchPoints%22%3A0%2C%22webdriver%22%3Afalse%2C%22plugins%22%3A%5B%5D%7D&t=c0b8c51aaf782120ded2ebbc49dae53a99c53bc4&u=4b6a5ae6de47521d80127b49d89e9419&fc=true";
//		String piParam = "ap=true&bt=0&fonts=41%2C43%2C44%2C45%2C46%2C47%2C48%2C49%2C50%2C51%2C52%2C55%2C56%2C57%2C58%2C59%2C60%2C61%2C62%2C63%2C64%2C65%2C67%2C68%2C69&fh=2b546713c41a929cd4d394582b29987bbb097c98&timing=%7B%221%22%3A39%2C%222%22%3A194%2C%223%22%3A298%2C%224%22%3A399%2C%225%22%3A570%2C%22profile%22%3A%7B%22bp%22%3A0%2C%22sr%22%3A0%2C%22dp%22%3A1%2C%22lt%22%3A0%2C%22ps%22%3A0%2C%22cv%22%3A16%2C%22fp%22%3A0%2C%22sp%22%3A1%2C%22br%22%3A0%2C%22ieps%22%3A0%2C%22av%22%3A0%2C%22z1%22%3A15%2C%22jsv%22%3A1%2C%22nav%22%3A2%2C%22z2%22%3A1%2C%22z3%22%3A4%2C%22z4%22%3A0%2C%22z5%22%3A1%2C%22fonts%22%3A117%7D%2C%22main%22%3A9028%2C%22compute%22%3A39%2C%22send%22%3A687%7D&bp=&sr=%7B%22inner%22%3A%5B1920%2C945%5D%2C%22outer%22%3A%5B1936%2C1056%5D%2C%22screen%22%3A%5B-8%2C-8%5D%2C%22pageOffset%22%3A%5B0%2C0%5D%2C%22avail%22%3A%5B1920%2C1040%5D%2C%22size%22%3A%5B1920%2C1080%5D%2C%22client%22%3A%5B1903%2C1369%5D%2C%22colorDepth%22%3A24%2C%22pixelDepth%22%3A24%7D&dp=%7B%22XDomainRequest%22%3A0%2C%22createPopup%22%3A0%2C%22removeEventListener%22%3A1%2C%22globalStorage%22%3A0%2C%22openDatabase%22%3A0%2C%22indexedDB%22%3A1%2C%22attachEvent%22%3A0%2C%22ActiveXObject%22%3A0%2C%22dispatchEvent%22%3A1%2C%22addBehavior%22%3A0%2C%22addEventListener%22%3A1%2C%22detachEvent%22%3A0%2C%22fireEvent%22%3A0%2C%22MutationObserver%22%3A1%2C%22HTMLMenuItemElement%22%3A1%2C%22Int8Array%22%3A1%2C%22postMessage%22%3A1%2C%22querySelector%22%3A1%2C%22getElementsByClassName%22%3A1%2C%22images%22%3A1%2C%22compatMode%22%3A%22CSS1Compat%22%2C%22documentMode%22%3A0%2C%22all%22%3A1%2C%22now%22%3A1%2C%22contextMenu%22%3Anull%7D&lt="+timeStamp+"%2B8&ps=true%2Ctrue&cv=19f5de4bfc86212d44a26e1fab8d56ca489459d4&fp=false&sp=false&br=Firefox&ieps=false&av=false&z=%7B%22a%22%3A1564911027%2C%22b%22%3A1%2C%22c%22%3A0%7D&zh=&jsv=1.5&nav=%7B%22userAgent%22%3A%22Mozilla%2F5.0%20(Windows%20NT%206.1%3B%20Win64%3B%20x64%3B%20rv%3A56.0)%20Gecko%2F20100101%20Firefox%2F56.0%22%2C%22appName%22%3A%22Netscape%22%2C%22appCodeName%22%3A%22Mozilla%22%2C%22appVersion%22%3A%225.0%20(Windows)%22%2C%22appMinorVersion%22%3A0%2C%22product%22%3A%22Gecko%22%2C%22productSub%22%3A%2220100101%22%2C%22vendor%22%3A%22%22%2C%22vendorSub%22%3A%22%22%2C%22buildID%22%3A%2220170926190823%22%2C%22platform%22%3A%22Win64%22%2C%22oscpu%22%3A%22Windows%20NT%206.1%3B%20Win64%3B%20x64%22%2C%22hardwareConcurrency%22%3A4%2C%22language%22%3A%22zh-CN%22%2C%22languages%22%3A%5B%22zh-CN%22%2C%22zh%22%2C%22en-US%22%2C%22en%22%5D%2C%22systemLanguage%22%3A0%2C%22userLanguage%22%3A0%2C%22doNotTrack%22%3A%22unspecified%22%2C%22msDoNotTrack%22%3A0%2C%22cookieEnabled%22%3Atrue%2C%22geolocation%22%3A1%2C%22vibrate%22%3A1%2C%22maxTouchPoints%22%3A0%2C%22webdriver%22%3A0%2C%22plugins%22%3A%5B%5D%7D&t=4334b371b9537885f70b4e19b65e68e867468dd8&u=d672f4ecc1aa5e7dbde27de239897d41&fc=true";
//		headerMap1.put("Accept", "*/*");
//		headerMap1.put("Cookie", fCookie);
//		headerMap1.put("Referer","https://www.turkishairlines.com/");
//		headerMap1.put("Content-Type","application/x-www-form-urlencoded");
//		headerMap1.remove("Upgrade-Insecure-Requests");
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,pixelUrl,piParam,"other");
//		if(httpVo==null) return null;
//		String ak_bmsc = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","ak_bmsc=");
//		String oldAk = MyStringUtil.getValue("ak_bmsc\\=", ";", fCookie);
//		fCookie = fCookie.replace(oldAk, "");
//		fCookie = fCookie.replace("ak_bmsc=", ak_bmsc);
//		String timeStamp1 = String.valueOf(System.currentTimeMillis());
//		String pubParam = "{\"sensor_data\":\"7a74G7m23Vrp0o5c9052371.4-1,2,-94,-100,Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0,uaend,11059,20100101,zh-CN,Gecko,0,0,0,0,380901,2446678,1536,824,1536,864,1536,728,1550,,cpen:0,i1:0,dm:0,cwen:0,non:1,opc:0,fc:1,sc:0,wrc:1,isc:96,vib:1,bat:0,x11:0,x12:1,5555,0.341972873170,774041223338.5,loc:-1,2,-94,-101,do_en,dm_en,t_dis-1,2,-94,-105,-1,2,-94,-102,0,0,1,0,-1,-1,0;0,0,0,0,-1,-1,0;0,0,0,0,833,-1,0;0,0,0,0,763,-1,0;0,-1,0,0,1685,-1,0;0,0,1,0,-1,-1,0;0,0,1,0,-1,-1,0;0,0,1,0,-1,-1,0;0,0,0,0,-1,-1,0;0,-1,0,0,-1,-1,0;0,-1,0,0,994,-1,0;0,-1,0,0,785,-1,0;-1,2,-94,-108,-1,2,-94,-110,0,1,57,1267,722;1,1,71,1267,723;2,1,123,1266,723;3,1,125,1262,727;4,1,1733,1194,720;5,1,1750,1166,704;6,1,1767,1120,678;7,1,1802,1007,592;8,1,1809,984,570;9,1,1817,967,550;10,1,1834,940,516;11,1,1852,930,494;12,1,1867,922,477;13,1,1883,912,462;14,1,1900,900,443;15,1,1917,870,399;16,1,1934,843,365;17,1,1950,824,338;18,1,1968,814,318;19,1,1986,806,301;20,1,2002,800,286;21,1,2017,794,272;22,1,2034,787,258;23,1,2050,781,245;24,1,2066,773,233;25,1,2084,768,222;26,1,2101,764,212;27,1,2117,762,204;28,1,2134,762,203;29,1,2585,762,210;30,1,2600,774,226;31,1,2617,792,251;32,1,2633,821,282;33,1,2650,862,313;34,1,2667,922,349;35,1,2683,988,387;36,1,2699,1055,414;37,1,2716,1140,448;38,1,2733,1186,462;39,1,2748,1207,466;40,1,3000,1400,346;41,1,3016,1402,341;42,1,3033,1406,337;43,1,3050,1409,333;44,1,3066,1411,331;45,1,3083,1414,331;46,1,3150,1416,331;47,1,3166,1421,331;48,1,3183,1428,334;49,1,3200,1433,337;50,1,3216,1436,340;51,1,3235,1440,342;52,1,3250,1446,344;53,1,3266,1452,346;54,1,3283,1456,349;55,1,3299,1461,351;56,1,3316,1465,354;57,1,3332,1467,354;58,1,3349,1471,356;59,1,3366,1475,358;60,1,3383,1478,360;61,1,3399,1479,360;62,1,3416,1481,361;63,1,3433,1482,361;64,1,3467,1483,362;65,1,3499,1484,362;66,1,3600,1486,363;67,1,3633,1488,363;68,1,3750,1487,363;69,1,3766,1481,361;70,1,3783,1466,354;71,1,3799,1447,348;72,1,3816,1421,338;73,1,3833,1383,323;74,1,3849,1336,305;75,1,3867,1279,282;76,1,3882,1215,258;77,1,3900,1149,231;78,1,3916,1061,194;79,1,3933,1018,177;80,1,3949,985,161;81,1,3966,962,150;82,1,3982,949,146;83,1,4000,945,143;84,1,4433,942,142;85,1,4449,938,142;86,1,4466,933,139;87,1,4483,931,138;88,1,4499,930,138;89,1,4549,929,138;90,1,4565,926,136;91,1,4582,922,134;92,1,4599,915,132;93,1,4616,906,130;94,1,4633,895,127;95,1,4648,883,122;96,1,4666,872,118;97,1,4683,862,114;98,1,4699,854,111;99,1,4716,845,106;286,2,19463,-1,-1,1685;322,3,20716,662,382,-1;-1,2,-94,-117,-1,2,-94,-111,-1,2,-94,-109,-1,2,-94,-114,-1,2,-94,-103,2,554;3,9723;0,9838;2,9842;3,11900;1,11900;2,14838;3,19800;3,20559;-1,2,-94,-112,https://www.turkishairlines.com/-1,2,-94,-115,1,497033,0,0,0,0,497033,20716,0,"+timeStamp1+",10,16560,0,323,2760,1,0,20717,345226,0,"+abck+",8191,21,513738083,26067385-1,2,-94,-106,1,1-1,2,-94,-119,0,0,0,0,200,0,0,200,0,0,0,0,200,200,-1,2,-94,-122,0,0,0,0,1,0,0-1,2,-94,-123,-1,2,-94,-70,1436327638;dis;;true;true;true;-480;true;24;24;true;false;unspecified-1,2,-94,-80,5886-1,2,-94,-116,110100400-1,2,-94,-118,159708-1,2,-94,-121,;2;8;0\"}";
//		headerMap1.put("Cookie", fCookie+";startScheduculePageUrl=https://www.turkishairlines.com/");
//		headerMap1.put("Content-Type","text/plain;charset=UTF-8");
//		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,publicUrl,pubParam,"other");
//		String _abck = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","_abck=");
//		if("".equals(_abck)) return null;
//		Cookies.append(";").append(ak_bmsc).append(";").append(_abck);
//		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");//18/01/2019
//		String saveDate = f.format(new Date());
//		String jsessionid = JSESSIONID.replace("JSESSIONID","jsessionid");
//		String jseUrl = "https://www.turkishairlines.com/com.thy.web.online.ibs/ibs/booking/completeschedulepagesinglecity;"+jsessionid;
//		String postParam = "{\"bookings\":[{\"originAirport\":{\"code\":\""+dep+"\",\"domestic\":false,\"multi\":false},\"destinationAirport\":{\"code\":\""+des+"\",\"domestic\":false,\"multi\":false},\"rph\":-1,\"oneWayWeeklyViewInitialyOpen\":false,\"roundTripMatrisViewInitiallyOpen\":false,\"oneWay\":true,\"cabinClass\":\"ECONOMY\",\"originationDateTime\":\""+depDate+"T00:00:00.000Z\",\"returnDateTime\":\"\",\"paxList\":[{\"code\":\"ADULT\",\"count\":1,\"minAge\":12,\"maxAge\":129,\"hasTooltip\":true}],\"savedDate\":\""+saveDate+"\",\"filters\":{\"directFlight\":false}}],\"flexibleDates\":false,\"selectedCabinClass\":\"ECONOMY\"}";
//		headerMap1.put("Cookie", Cookies);
//		headerMap1.put("Accept-Language", "zh");
//		headerMap1.put("X-Requested-With","XMLHttpRequest");
//		headerMap1.put("page","https://www.turkishairlines.com/");
//		headerMap1.put("pageRequestId",pageRequestId);
//		headerMap1.put("requestId",UUID.randomUUID().toString());
//		headerMap1.put("Content-Type","application/json; charset=utf-8");
//		String cidResult = this.httpProxyPost(httpClientSessionVo,jseUrl,postParam,"other");
//		
//		String cid = MyStringUtil.getValue("cId\\=", "\"", cidResult);
//		String bookUrl = "https://www.turkishairlines.com/zh-cn/flights/booking/availability/?cId="+cid;
//		
//		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//		headerMap1.put("Upgrade-Insecure-Requests","1");
//		headerMap1.remove("page");
//		headerMap1.remove("pageRequestId");
//		headerMap1.remove("requestId");
//		headerMap1.remove("X-Requested-With");
//		
//	    String bookResult = this.httpProxyGet(httpClientSessionVo,bookUrl,"other");
//		String pageRequestId2 = MyStringUtil.getValue("PageRequestID\" content\\=\"", "\"", bookResult);
//		headerMap1.put("Accept", "*/*");
//		headerMap1.put("Accept-Language", "zh");
//		headerMap1.put("X-Requested-With","XMLHttpRequest");
//		headerMap1.put("Content-Type","application/json; charset=utf-8");
//		headerMap1.put("page",bookUrl);
//		headerMap1.put("cId",cid);
//		headerMap1.put("Referer",bookUrl);
//		headerMap1.put("country","cn");
//		headerMap1.put("pageRequestId",pageRequestId2);
//		headerMap1.put("requestId",UUID.randomUUID().toString());
//		String timeStamp2 = String.valueOf(System.currentTimeMillis());
//		String avaUrl = "https://www.turkishairlines.com/com.thy.web.online.ibs/ibs/booking/availabilitysinglecityresponse?isPhone=false&_="+timeStamp2;
//		String result = this.httpProxyGet(httpClientSessionVo,avaUrl,"other");
//		if(result.contains("You don't have permission to access")) {
//			super.changeProxy();
//			return null;
//		}
		return "";
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
	
}
