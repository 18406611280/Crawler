package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 新加坡航空
 * @author chenminghong
 */

public class B2CSQCrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	
	public B2CSQCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(httpResult==null) return crawlResults;
		try {
			Document document = Jsoup.parse(httpResult);
			Elements flightEles = document.getElementsByClass("EK_flight");
			int eleSize = flightEles.size();
			if(eleSize==0) return crawlResults;
			for(int f = 0;f<eleSize; f++) {
				Element fli = flightEles.get(f);
				Elements sections = fli.getElementsByTag("section");
				int secs = sections.size();
				List<FlightSegment> flights = new ArrayList<FlightSegment>();
				boolean ifAddOneDay = false;
				for(int s=0; s<secs; s++) {
					FlightSegment fliment = new FlightSegment();
					Element sec = sections.get(s);
					String mese = sec.text();
					String dep = MyStringUtil.getValueByPattern("出发(\\s*\\w{3})", mese).trim();
					String des = MyStringUtil.getValueByPattern("抵达(\\s*\\w{3})", mese).trim();
					String flightNo = MyStringUtil.getValueByPattern("航班号码(\\s*\\w*)", mese).trim();
					String flightEN = flightNo.substring(0,2);
					String shareFlight = this.getShareFlight(flightEN);
					List<String> times = MyStringUtil.getValuesByPattern("(\\d{2}:\\d{2}\\s*\\+?\\d?)", mese);
					String depTime = times.get(0).trim();
					String desTime = times.get(1).trim();
					String desDate = depDate;
					if(ifAddOneDay){
						depDate = addOneday(depDate);//加一天
						desDate = addOneday(desDate);//加一天
					}
					if(desTime.contains("+")){
						desTime = desTime.substring(0, 5);
						desDate = addOneday(desDate);//加一天
						ifAddOneDay=true;
					}
					fliment.setTripNo(s+1);
					fliment.setFlightNumber(flightNo);
					fliment.setAirlineCode(flightEN);
					fliment.setCodeShare(shareFlight);
					fliment.setDepAirport(dep);
					fliment.setArrAirport(des);
					fliment.setDepTime(depDate+" "+depTime+":00");
					fliment.setArrTime(desDate+" "+desTime+":00");
					flights.add(fliment);
				}
				Elements prices =  fli.select(".ts-fbr-brand-table__cell.ts-fbr-brand-table__cell--select.ts-fbr-brand-table__cell--status-default");
				int priceSize = prices.size();
				for(int p=0; p<priceSize; p++){
					FlightData flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				    flightData.setAirlineCode("EK");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					Element price = prices.get(p);
					Element brand = price.getElementsByClass("brandInfo").get(0);
					String value = brand.attr("value");
					JSONObject parse = JSONObject.parseObject(value);
					String totalPrice = parse.getString("TotalFare");
					BigDecimal allPrice = new BigDecimal(totalPrice);
					String tax = parse.getString("Tax");
					BigDecimal Tax = new BigDecimal(tax);
					BigDecimal ticketPrice = allPrice.subtract(Tax);
					String currency = parse.getString("Currency").trim();
					String RBD = parse.getString("RBD");
					String[] rbds = RBD.split(",");
					int rbdIndex = 0;
					List<FlightSegment> newFlights = new ArrayList<FlightSegment>();
					for(FlightSegment fliSegment:flights){
						FlightSegment segmentClone = (FlightSegment)fliSegment.clone();
						segmentClone.setCabin(rbds[rbdIndex]);
						newFlights.add(segmentClone);
						rbdIndex++;
					}
					BigDecimal rate = rateMap.get(currency);
					if("CNY".equals(currency)){
						rateMap.put(currency, new BigDecimal(1));
						rate = new BigDecimal(1);
					}
					if("ZWD".equals(currency)){
						rateMap.put(currency, new BigDecimal(0.0187));
						rate = new BigDecimal(0.0187);
					}
					if(rate==null) rate = new BigDecimal(0);
//					else{
//						if(rate==null || rate.intValue()==0){
//							synchronized(this){
//								rate = CurrencyUtil.getRate(rateMap,currency, "CNY");
//							}
//						}
//					}
					BigDecimal cnyPrice = null;
					BigDecimal cnyTaxPrice = null;
					flightData.setFromSegments(newFlights);
					List<FlightPrice> flightPrice = new ArrayList<FlightPrice>();
					FlightPrice fPrice = new FlightPrice();
					if("0".equals(rate.toString())){
						cnyPrice = ticketPrice.setScale(2,BigDecimal.ROUND_UP);
						cnyTaxPrice = Tax.setScale(2,BigDecimal.ROUND_UP);
						fPrice.setCurrency(currency);
						fPrice.setEquivCurrency(currency);
						flightData.setMemo(currency);
					}else{
						cnyPrice = ticketPrice.multiply(rate).setScale(2,BigDecimal.ROUND_UP);
						cnyTaxPrice = Tax.multiply(rate).setScale(2,BigDecimal.ROUND_UP);
						fPrice.setCurrency("CNY");
						fPrice.setEquivCurrency("CNY");
					}
					fPrice.setEquivFare(cnyPrice.toString());
					fPrice.setFare(cnyPrice.toString());
					fPrice.setEquivTax(cnyTaxPrice.toString());
					fPrice.setTax(cnyTaxPrice.toString());
					fPrice.setPassengerType("ADT");
					flightPrice.add(fPrice);
					flightData.setPrices(flightPrice);
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
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
		String Url = "https://www.singaporeair.com/zh_CN/plan-and-book/your-booking/searchflight/";
		Map<String, Object> headerMap1 = new HashMap<String, Object>();
		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap1.put("Accept-Encoding", "gzip, deflate, br");
		headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap1.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap1.put("Upgrade-Insecure-Requests","1");
		headerMap1.put("origin","https://www.singaporeair.com");
		headerMap1.put("Connection", "keep-alive");
		headerMap1.put("Host", "www.singaporeair.com");
		httpClientSessionVo.setHeaderMap(headerMap1);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,Url,"other");
		String cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
		
		String AWSALB1 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","AWSALB=");
		String AWSELB1 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","AWSELB=");
		String JSESSIONID1 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","JSESSIONID=");
		String dtCookie1 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","dtCookie=");
		String AKAMAI = "AKAMAI_HOME_PAGE_ACCESSED=true;AKAMAI_SAA_AIRPORT_COOKIE=SIN;AKAMAI_SAA_COUNTRY_COOKIE=CN;AKAMAI_SAA_DEVICE_COOKIE=desktop;AKAMAI_SAA_LOCALE_COOKIE=zh_CN";
		String loginCo = "FARE_DEALS_LISTING_COOKIE=false;LOGIN_COOKIE=false;LOGIN_POPUP_COOKIE=false;RU_LOGIN_COOKIE=false;saadevice=desktop;SQCLOGIN_COOKIE=false;cookieStateSet=closedCookies;";
		
		StringBuilder Cookie = new StringBuilder();
		String cookieFi = Cookie.append(JSESSIONID1).append(";").append(dtCookie1).append(";").append(AWSELB1)
		      .append(";").append(AKAMAI).append(";").append(loginCo).toString();
		StringBuilder CookieN = new StringBuilder();
		CookieN.append(cookieFi).append(";").append(AWSALB1);
		
		String js1Url = "https://www.singaporeair.com/sngprrdstl.js";
		headerMap1.put("Accept", "*/*");
		headerMap1.put("Cookie", cookies);
		headerMap1.remove("Upgrade-Insecure-Requests");
		headerMap1.remove("origin");
		headerMap1.put("Referer", "https://www.singaporeair.com/zh_CN/plan-and-book/your-booking/searchflight/");
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,js1Url,"other");
		String jsValue1 = httpVo.getHttpResult();
		String Pid1 = MyStringUtil.getValue("sngprrdstl\\.js\\?PID\\=", "\"", jsValue1);
		String ajax_header1 = MyStringUtil.getValue("ajax_header:\"", "\"", jsValue1);
		String pid1Url = "https://www.singaporeair.com/sngprrdstl.js?PID="+Pid1;
		String timeStamp1 = String.valueOf(System.currentTimeMillis());
		String param1 = "p=%7B%22proof%22%3A%22c2%3A"+timeStamp1+"%3AKf7iQszXiMcMXUsiPT0T%22%2C%22fp2%22%3A%7B%22userAgent%22%3A%22Mozilla%2F5.0(WindowsNT6.1%3BWin64%3Bx64%3Brv%3A56.0)Gecko%2F20100101Firefox%2F56.0%22%2C%22language%22%3A%22zh-CN%22%2C%22screen%22%3A%7B%22width%22%3A1920%2C%22height%22%3A1080%2C%22availHeight%22%3A1040%2C%22availWidth%22%3A1920%2C%22pixelDepth%22%3A24%2C%22innerWidth%22%3A1920%2C%22innerHeight%22%3A945%2C%22outerWidth%22%3A1936%2C%22outerHeight%22%3A1056%2C%22devicePixelRatio%22%3A1%7D%2C%22timezone%22%3A8%2C%22indexedDb%22%3Atrue%2C%22addBehavior%22%3Afalse%2C%22openDatabase%22%3Afalse%2C%22cpuClass%22%3A%22unknown%22%2C%22platform%22%3A%22Win64%22%2C%22doNotTrack%22%3A%22unspecified%22%2C%22plugins%22%3A%22%22%2C%22canvas%22%3A%7B%22winding%22%3A%22yes%22%2C%22towebp%22%3Afalse%2C%22blending%22%3Atrue%2C%22img%22%3A%22e46580f95cc399295ccba20e421bb3a2c1a0563a%22%7D%2C%22webGL%22%3A%7B%22img%22%3A%228d37e15cc9363584537e76e4d202a7e8e811da59%22%2C%22extensions%22%3A%22ANGLE_instanced_arrays%3BEXT_blend_minmax%3BEXT_color_buffer_half_float%3BEXT_frag_depth%3BEXT_shader_texture_lod%3BEXT_texture_filter_anisotropic%3BEXT_disjoint_timer_query%3BOES_element_index_uint%3BOES_standard_derivatives%3BOES_texture_float%3BOES_texture_float_linear%3BOES_texture_half_float%3BOES_texture_half_float_linear%3BOES_vertex_array_object%3BWEBGL_color_buffer_float%3BWEBGL_compressed_texture_s3tc%3BWEBGL_debug_renderer_info%3BWEBGL_debug_shaders%3BWEBGL_depth_texture%3BWEBGL_draw_buffers%3BWEBGL_lose_context%3BMOZ_WEBGL_lose_context%3BMOZ_WEBGL_compressed_texture_s3tc%3BMOZ_WEBGL_depth_texture%22%2C%22aliasedlinewidthrange%22%3A%22%5B1%2C1%5D%22%2C%22aliasedpointsizerange%22%3A%22%5B1%2C1024%5D%22%2C%22alphabits%22%3A8%2C%22antialiasing%22%3A%22yes%22%2C%22bluebits%22%3A8%2C%22depthbits%22%3A16%2C%22greenbits%22%3A8%2C%22maxanisotropy%22%3A16%2C%22maxcombinedtextureimageunits%22%3A32%2C%22maxcubemaptexturesize%22%3A16384%2C%22maxfragmentuniformvectors%22%3A1024%2C%22maxrenderbuffersize%22%3A16384%2C%22maxtextureimageunits%22%3A16%2C%22maxtexturesize%22%3A16384%2C%22maxvaryingvectors%22%3A30%2C%22maxvertexattribs%22%3A16%2C%22maxvertextextureimageunits%22%3A16%2C%22maxvertexuniformvectors%22%3A4096%2C%22maxviewportdims%22%3A%22%5B32767%2C32767%5D%22%2C%22redbits%22%3A8%2C%22renderer%22%3A%22Mozilla%22%2C%22shadinglanguageversion%22%3A%22WebGLGLSLES1.0%22%2C%22stencilbits%22%3A0%2C%22vendor%22%3A%22Mozilla%22%2C%22version%22%3A%22WebGL1.0%22%2C%22vertexshaderhighfloatprecision%22%3A23%2C%22vertexshaderhighfloatprecisionrangeMin%22%3A127%2C%22vertexshaderhighfloatprecisionrangeMax%22%3A127%2C%22vertexshadermediumfloatprecision%22%3A23%2C%22vertexshadermediumfloatprecisionrangeMin%22%3A127%2C%22vertexshadermediumfloatprecisionrangeMax%22%3A127%2C%22vertexshaderlowfloatprecision%22%3A23%2C%22vertexshaderlowfloatprecisionrangeMin%22%3A127%2C%22vertexshaderlowfloatprecisionrangeMax%22%3A127%2C%22fragmentshaderhighfloatprecision%22%3A23%2C%22fragmentshaderhighfloatprecisionrangeMin%22%3A127%2C%22fragmentshaderhighfloatprecisionrangeMax%22%3A127%2C%22fragmentshadermediumfloatprecision%22%3A23%2C%22fragmentshadermediumfloatprecisionrangeMin%22%3A127%2C%22fragmentshadermediumfloatprecisionrangeMax%22%3A127%2C%22fragmentshaderlowfloatprecision%22%3A23%2C%22fragmentshaderlowfloatprecisionrangeMin%22%3A127%2C%22fragmentshaderlowfloatprecisionrangeMax%22%3A127%2C%22vertexshaderhighintprecision%22%3A0%2C%22vertexshaderhighintprecisionrangeMin%22%3A31%2C%22vertexshaderhighintprecisionrangeMax%22%3A30%2C%22vertexshadermediumintprecision%22%3A0%2C%22vertexshadermediumintprecisionrangeMin%22%3A31%2C%22vertexshadermediumintprecisionrangeMax%22%3A30%2C%22vertexshaderlowintprecision%22%3A0%2C%22vertexshaderlowintprecisionrangeMin%22%3A31%2C%22vertexshaderlowintprecisionrangeMax%22%3A30%2C%22fragmentshaderhighintprecision%22%3A0%2C%22fragmentshaderhighintprecisionrangeMin%22%3A31%2C%22fragmentshaderhighintprecisionrangeMax%22%3A30%2C%22fragmentshadermediumintprecision%22%3A0%2C%22fragmentshadermediumintprecisionrangeMin%22%3A31%2C%22fragmentshadermediumintprecisionrangeMax%22%3A30%2C%22fragmentshaderlowintprecision%22%3A0%2C%22fragmentshaderlowintprecisionrangeMin%22%3A31%2C%22fragmentshaderlowintprecisionrangeMax%22%3A30%7D%2C%22touch%22%3A%7B%22maxTouchPoints%22%3A0%2C%22touchEvent%22%3Afalse%2C%22touchStart%22%3Afalse%7D%2C%22video%22%3A%7B%22ogg%22%3A%22probably%22%2C%22h264%22%3A%22probably%22%2C%22webm%22%3A%22probably%22%7D%2C%22audio%22%3A%7B%22ogg%22%3A%22probably%22%2C%22mp3%22%3A%22maybe%22%2C%22wav%22%3A%22probably%22%2C%22m4a%22%3A%22maybe%22%7D%2C%22vendor%22%3A%22%22%2C%22product%22%3A%22Gecko%22%2C%22productSub%22%3A%2220100101%22%2C%22browser%22%3A%7B%22ie%22%3Afalse%2C%22chrome%22%3Afalse%2C%22webdriver%22%3Afalse%7D%2C%22window%22%3A%7B%22historyLength%22%3A2%2C%22hardwareConcurrency%22%3A4%2C%22iframe%22%3Afalse%7D%2C%22fonts%22%3A%22Batang%3BCalibri%3BCentury%3BLeelawadee%3BMarlett%3BPMingLiU%3BSimHei%3BVrinda%22%7D%2C%22cookies%22%3A1%2C%22setTimeout%22%3A1%2C%22setInterval%22%3A1%2C%22appName%22%3A%22Netscape%22%2C%22platform%22%3A%22Win64%22%2C%22syslang%22%3A%22zh-CN%22%2C%22userlang%22%3A%22zh-CN%22%2C%22cpu%22%3A%22WindowsNT6.1%3BWin64%3Bx64%22%2C%22productSub%22%3A%2220100101%22%2C%22plugins%22%3A%7B%7D%2C%22mimeTypes%22%3A%7B%7D%2C%22screen%22%3A%7B%22width%22%3A1920%2C%22height%22%3A1080%2C%22colorDepth%22%3A24%7D%2C%22fonts%22%3A%7B%220%22%3A%22Calibri%22%2C%221%22%3A%22Cambria%22%2C%222%22%3A%22Constantia%22%2C%223%22%3A%22Georgia%22%2C%224%22%3A%22SegoeUI%22%2C%225%22%3A%22Candara%22%2C%226%22%3A%22TrebuchetMS%22%2C%227%22%3A%22Verdana%22%2C%228%22%3A%22Consolas%22%2C%229%22%3A%22LucidaConsole%22%2C%2210%22%3A%22BitstreamVeraSansMono%22%2C%2211%22%3A%22CourierNew%22%2C%2212%22%3A%22Courier%22%7D%7D";
		headerMap1.put("Content-Type","text/plain;charset=UTF-8");
		headerMap1.put("X-Distil-Ajax",ajax_header1);
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,pid1Url,param1,"other");
		String Dcookie = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
		
//		String form1Url = "https://www.singaporeair.com/getAirportListJson.form?locale=zh_CN&undefined";
//		headerMap1.put("X-Requested-With","XMLHttpRequest");
//		headerMap1.remove("Content-Type");
//		headerMap1.put("Cookie", CookieN.toString()+";"+Dcookie);
//		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,form1Url,"other");
//		String AWSALB2 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","AWSALB=");
//		String rt = ";RT=\"sl=1&ss=1547713632561&tt=36140&obo=0&bcn=%2F%2F1288af19.akstat.io%2F&sh=1547713668711%3D1%3A0%3A36140&dm=singaporeair.com&si=5bf76063-9c61-492e-9a0a-b8e064bb1e5b&ld=1547713668712&nu=&cl=1547713683494&r=https%3A%2F%2Fwww.singaporeair.com%2Fzh_CN%2Fplan-and-book%2Fyour-booking%2Fsearchflight%2F&ul=1547713683540\"";
//		String COOKIE1 = cookieFi+";"+AWSALB2+";"+Dcookie+";dtSa=true%7CC%7C-1%7C%E6%90%9C%E7%B4%A2%7C-%7C1547713683405%7C313634078_777%7Chttps%3A%2F%2Fwww.singaporeair.com%2Fzh_5FCN%2Fplan-and-book%2Fyour-booking%2Fsearchflight%2F%7C%E6%90%9C%E7%B4%A2%E8%88%AA%E7%8F%AD%7C1547713682290%7C;dtPC=2$313634078_777h-vLEHCHEPIDKDVFKANBJJPHIBLSNIJPLOI;ins-gaSSId=fbcea4f9-8bf9-0b95-224f-9f826287bddb_1547713675;rxVisitor=1547713634084ORT8261G1695U9HB8NOV4RSFGCSBSMKJ;spUID=1547713676609b54c82e885.1b43c55e;_cls_s=c4387912-147b-4b82-b1b3-dd0e84cdaf40:0;_cls_v=41e30093-69a7-4922-8113-705126b18f33"+rt;
		headerMap1.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap1.put("Cookie", cookies+";"+Dcookie);
		headerMap1.put("Content-Type","application/x-www-form-urlencoded");
		headerMap1.remove("X-Requested-With");
		headerMap1.remove("X-Distil-Ajax");
		headerMap1.put("Upgrade-Insecure-Requests","1");
		String formHomeUrl = "https://www.singaporeair.com/booking-flow.form";
		String param3 = "fromHomePage=true&isSecondaryLanding=true&origin=CAN&destination=SIN&_tripType=on&tripType=O&departureMonth=24%2F02%2F2019&cabinClass=Y&numOfAdults=1&numOfChildren=0&numOfInfants=0&_eventId_flightSearchEvent=&isLoggedInUser=&numOfChildNominees=&numOfAdultNominees=";
		Map<String, Object> param2 = new HashMap<String, Object>();
		param2.put("fromHomePage", "true");
		param2.put("isSecondaryLanding", "true");
		param2.put("origin", "CAN");
		param2.put("destination", "KUL");
		param2.put("_tripType", "on");
		param2.put("tripType", "O");
		param2.put("departureMonth", "28/01/2019");
		param2.put("cabinClass", "Y");
		param2.put("numOfAdults", "1");
		param2.put("numOfChildren", "0");
		param2.put("numOfInfants", "0");
		param2.put("_eventId_flightSearchEvent", "");
		param2.put("isLoggedInUser", "");
		param2.put("numOfChildNominees", "");
		param2.put("numOfAdultNominees", "");
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,formHomeUrl,param3,"other");
		String RSS = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","RSS=");
		headerMap1.put("Cookie", cookies+";"+Dcookie+";"+RSS);
		headerMap1.put("origin","https://www.singaporeair.com");
		String URL = "https://www.singaporeair.com/booking-flow.form?execution=e1s1";
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,URL,"other");
		
		
		
		//////////////////////////////////////////////
		String homeUrl = "https://www.singaporeair.com/zh_CN/cn/home";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("origin","https://www.singaporeair.com");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.singaporeair.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,homeUrl,"other");
		
		String jsUrl = "https://www.singaporeair.com/sngprrdstl.js";
		headerMap.put("Accept", "*/*");
		headerMap.put("Referer", "https://www.singaporeair.com/zh_CN/cn/home");
		headerMap.remove("Upgrade-Insecure-Requests");
		headerMap.remove("origin");
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,jsUrl,"other");
		String jsValue = httpVo.getHttpResult();
		String Pid = MyStringUtil.getValue("sngprrdstl\\.js\\?PID\\=", "\"", jsValue);
		String ajax_header = MyStringUtil.getValue("ajax_header:\"", "\"", jsValue);
		String pidUrl = "https://www.singaporeair.com/sngprrdstl.js?PID="+Pid;
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String pidParam = "p=%7B%22proof%22%3A%221c5%3A"+timeStamp+"%3A7mQrYhEEUtudlEcBxz7i%22%2C%22fp2%22%3A%7B%22userAgent%22%3A%22Mozilla%2F5.0(WindowsNT6.1%3BWin64%3Bx64%3Brv%3A56.0)Gecko%2F20100101Firefox%2F56.0%22%2C%22language%22%3A%22zh-CN%22%2C%22screen%22%3A%7B%22width%22%3A1920%2C%22height%22%3A1080%2C%22availHeight%22%3A1040%2C%22availWidth%22%3A1920%2C%22pixelDepth%22%3A24%2C%22innerWidth%22%3A1920%2C%22innerHeight%22%3A945%2C%22outerWidth%22%3A1936%2C%22outerHeight%22%3A1056%2C%22devicePixelRatio%22%3A1%7D%2C%22timezone%22%3A8%2C%22indexedDb%22%3Atrue%2C%22addBehavior%22%3Afalse%2C%22openDatabase%22%3Afalse%2C%22cpuClass%22%3A%22unknown%22%2C%22platform%22%3A%22Win64%22%2C%22doNotTrack%22%3A%22unspecified%22%2C%22plugins%22%3A%22%22%2C%22canvas%22%3A%7B%22winding%22%3A%22yes%22%2C%22towebp%22%3Afalse%2C%22blending%22%3Atrue%2C%22img%22%3A%22e46580f95cc399295ccba20e421bb3a2c1a0563a%22%7D%2C%22webGL%22%3A%7B%22img%22%3A%228d37e15cc9363584537e76e4d202a7e8e811da59%22%2C%22extensions%22%3A%22ANGLE_instanced_arrays%3BEXT_blend_minmax%3BEXT_color_buffer_half_float%3BEXT_frag_depth%3BEXT_shader_texture_lod%3BEXT_texture_filter_anisotropic%3BEXT_disjoint_timer_query%3BOES_element_index_uint%3BOES_standard_derivatives%3BOES_texture_float%3BOES_texture_float_linear%3BOES_texture_half_float%3BOES_texture_half_float_linear%3BOES_vertex_array_object%3BWEBGL_color_buffer_float%3BWEBGL_compressed_texture_s3tc%3BWEBGL_debug_renderer_info%3BWEBGL_debug_shaders%3BWEBGL_depth_texture%3BWEBGL_draw_buffers%3BWEBGL_lose_context%3BMOZ_WEBGL_lose_context%3BMOZ_WEBGL_compressed_texture_s3tc%3BMOZ_WEBGL_depth_texture%22%2C%22aliasedlinewidthrange%22%3A%22%5B1%2C1%5D%22%2C%22aliasedpointsizerange%22%3A%22%5B1%2C1024%5D%22%2C%22alphabits%22%3A8%2C%22antialiasing%22%3A%22yes%22%2C%22bluebits%22%3A8%2C%22depthbits%22%3A16%2C%22greenbits%22%3A8%2C%22maxanisotropy%22%3A16%2C%22maxcombinedtextureimageunits%22%3A32%2C%22maxcubemaptexturesize%22%3A16384%2C%22maxfragmentuniformvectors%22%3A1024%2C%22maxrenderbuffersize%22%3A16384%2C%22maxtextureimageunits%22%3A16%2C%22maxtexturesize%22%3A16384%2C%22maxvaryingvectors%22%3A30%2C%22maxvertexattribs%22%3A16%2C%22maxvertextextureimageunits%22%3A16%2C%22maxvertexuniformvectors%22%3A4096%2C%22maxviewportdims%22%3A%22%5B32767%2C32767%5D%22%2C%22redbits%22%3A8%2C%22renderer%22%3A%22Mozilla%22%2C%22shadinglanguageversion%22%3A%22WebGLGLSLES1.0%22%2C%22stencilbits%22%3A0%2C%22vendor%22%3A%22Mozilla%22%2C%22version%22%3A%22WebGL1.0%22%2C%22vertexshaderhighfloatprecision%22%3A23%2C%22vertexshaderhighfloatprecisionrangeMin%22%3A127%2C%22vertexshaderhighfloatprecisionrangeMax%22%3A127%2C%22vertexshadermediumfloatprecision%22%3A23%2C%22vertexshadermediumfloatprecisionrangeMin%22%3A127%2C%22vertexshadermediumfloatprecisionrangeMax%22%3A127%2C%22vertexshaderlowfloatprecision%22%3A23%2C%22vertexshaderlowfloatprecisionrangeMin%22%3A127%2C%22vertexshaderlowfloatprecisionrangeMax%22%3A127%2C%22fragmentshaderhighfloatprecision%22%3A23%2C%22fragmentshaderhighfloatprecisionrangeMin%22%3A127%2C%22fragmentshaderhighfloatprecisionrangeMax%22%3A127%2C%22fragmentshadermediumfloatprecision%22%3A23%2C%22fragmentshadermediumfloatprecisionrangeMin%22%3A127%2C%22fragmentshadermediumfloatprecisionrangeMax%22%3A127%2C%22fragmentshaderlowfloatprecision%22%3A23%2C%22fragmentshaderlowfloatprecisionrangeMin%22%3A127%2C%22fragmentshaderlowfloatprecisionrangeMax%22%3A127%2C%22vertexshaderhighintprecision%22%3A0%2C%22vertexshaderhighintprecisionrangeMin%22%3A31%2C%22vertexshaderhighintprecisionrangeMax%22%3A30%2C%22vertexshadermediumintprecision%22%3A0%2C%22vertexshadermediumintprecisionrangeMin%22%3A31%2C%22vertexshadermediumintprecisionrangeMax%22%3A30%2C%22vertexshaderlowintprecision%22%3A0%2C%22vertexshaderlowintprecisionrangeMin%22%3A31%2C%22vertexshaderlowintprecisionrangeMax%22%3A30%2C%22fragmentshaderhighintprecision%22%3A0%2C%22fragmentshaderhighintprecisionrangeMin%22%3A31%2C%22fragmentshaderhighintprecisionrangeMax%22%3A30%2C%22fragmentshadermediumintprecision%22%3A0%2C%22fragmentshadermediumintprecisionrangeMin%22%3A31%2C%22fragmentshadermediumintprecisionrangeMax%22%3A30%2C%22fragmentshaderlowintprecision%22%3A0%2C%22fragmentshaderlowintprecisionrangeMin%22%3A31%2C%22fragmentshaderlowintprecisionrangeMax%22%3A30%7D%2C%22touch%22%3A%7B%22maxTouchPoints%22%3A0%2C%22touchEvent%22%3Afalse%2C%22touchStart%22%3Afalse%7D%2C%22video%22%3A%7B%22ogg%22%3A%22probably%22%2C%22h264%22%3A%22probably%22%2C%22webm%22%3A%22probably%22%7D%2C%22audio%22%3A%7B%22ogg%22%3A%22probably%22%2C%22mp3%22%3A%22maybe%22%2C%22wav%22%3A%22probably%22%2C%22m4a%22%3A%22maybe%22%7D%2C%22vendor%22%3A%22%22%2C%22product%22%3A%22Gecko%22%2C%22productSub%22%3A%2220100101%22%2C%22browser%22%3A%7B%22ie%22%3Afalse%2C%22chrome%22%3Afalse%2C%22webdriver%22%3Afalse%7D%2C%22window%22%3A%7B%22historyLength%22%3A2%2C%22hardwareConcurrency%22%3A4%2C%22iframe%22%3Afalse%7D%2C%22fonts%22%3A%22Batang%3BCalibri%3BCentury%3BLeelawadee%3BMarlett%3BPMingLiU%3BSimHei%3BVrinda%22%7D%2C%22cookies%22%3A1%2C%22setTimeout%22%3A1%2C%22setInterval%22%3A1%2C%22appName%22%3A%22Netscape%22%2C%22platform%22%3A%22Win64%22%2C%22syslang%22%3A%22zh-CN%22%2C%22userlang%22%3A%22zh-CN%22%2C%22cpu%22%3A%22WindowsNT6.1%3BWin64%3Bx64%22%2C%22productSub%22%3A%2220100101%22%2C%22plugins%22%3A%7B%7D%2C%22mimeTypes%22%3A%7B%7D%2C%22screen%22%3A%7B%22width%22%3A1920%2C%22height%22%3A1080%2C%22colorDepth%22%3A24%7D%2C%22fonts%22%3A%7B%220%22%3A%22Calibri%22%2C%221%22%3A%22Cambria%22%2C%222%22%3A%22Constantia%22%2C%223%22%3A%22Georgia%22%2C%224%22%3A%22SegoeUI%22%2C%225%22%3A%22Candara%22%2C%226%22%3A%22TrebuchetMS%22%2C%227%22%3A%22Verdana%22%2C%228%22%3A%22Consolas%22%2C%229%22%3A%22LucidaConsole%22%2C%2210%22%3A%22BitstreamVeraSansMono%22%2C%2211%22%3A%22CourierNew%22%2C%2212%22%3A%22Courier%22%7D%7D";
	    httpVo = this.httpProxyResultVoPost(httpClientSessionVo,pidUrl,pidParam,"other");
	    cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");	
	    
		String homeformUrl ="https://www.singaporeair.com/home-breakingalert.form";
	    headerMap.put("X-Requested-With","XMLHttpRequest");
	    headerMap.put("X-Distil-Ajax",ajax_header);
	    httpVo = this.httpProxyResultVoGet(httpClientSessionVo,homeformUrl,"other");
	    String ptk = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ptk=");
	    String cookie1 = ";cookieStateSet=closedCookies";
	    String cookie2 = cookies+cookie1+";"+ptk;
	    
	    String localeUrl = "https://www.singaporeair.com/getAirportListJson.form?locale=zh_CN&airportCode=&ipTocitySupported=true&customOrigin=true";
	    headerMap.put("Cookie",cookie2);
	    httpVo = this.httpProxyResultVoPost(httpClientSessionVo,localeUrl,"","other");
	    String cookie3 = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
	    String cookie4 = cookie2+";"+cookie3;
	    headerMap.put("Cookie",cookie4);
	    httpVo = this.httpProxyResultVoGet(httpClientSessionVo,localeUrl,"other");
	    String AWSELB = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "AWSALB=");
	    String oldSa = MyStringUtil.getValue("AWSALB\\=", "\\;", cookie4);
	    cookie4 = cookie4.replace(oldSa, "");
	    cookie4 = cookie4.replace("AWSALB=", AWSELB);
//	    String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
	    
//	    String defaltCookie = ";AKAMAI_HOME_PAGE_ACCESSED=true;SQCLOGIN_COOKIE=false;LOGIN_POPUP_COOKIE=false;FARE_DEALS_LISTING_COOKIE=false;RU_LOGIN_COOKIE=false;LOGIN_COOKIE=false;AKAMAI_SAA_COUNTRY_COOKIE=CN;AKAMAI_SAA_AIRPORT_COOKIE=SIN;AKAMAI_SAA_LOCALE_COOKIE=zh_CN;saadevice=desktop;AKAMAI_SAA_DEVICE_COOKIE=desktop";
	    headerMap.remove("X-Requested-With");
	    headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");	
	    headerMap.put("Content-Type","application/x-www-form-urlencoded");
	    headerMap.put("Upgrade-Insecure-Requests","1");
	    headerMap.remove("X-Distil-Ajax");
//	    String param = "fromHomePage=true&origin=CAN&destination=KUL&_tripType=on&tripType=O&departureMonth=28%2F01%2F2019&cabinClass=Y&numOfAdults=1&numOfChildren=0&numOfInfants=0&_eventId_flightSearchEvent=&isLoggedInUser=false&numOfChildNominees=&numOfAdultNominees=";
	    headerMap.put("Cookie",cookie4);
	    String formUrl = "https://www.singaporeair.com/booking-flow.form";
	    Map<String, Object> param = new HashMap<String, Object>();
	    param.put("", "");
	    httpVo = this.httpProxyResultVoPost(httpClientSessionVo,formUrl,param,"other");
	    String result2 = httpVo.getHttpResult();
	    cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");		
	    
	    
	    
	    
	    
	    
	    
		if(httpVo==null) return null;
		String COOKIE = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
		String resu = httpVo.getHttpResult();
		String akamUrl = MyStringUtil.getValue("\\<noscript\\>\\<img src\\=\"", "\\?a", resu);
		timeStamp = String.valueOf(System.currentTimeMillis());
		param3 = "ap=true&bt=0&fonts=41%2C43%2C44%2C45%2C46%2C47%2C48%2C49%2C50%2C51%2C52%2C55%2C56%2C57%2C58%2C59%2C60%2C61%2C62%2C63%2C64%2C65%2C67%2C68%2C69&fh=2b546713c41a929cd4d394582b29987bbb097c98&timing=%7B%221%22%3A26%2C%222%22%3A2971%2C%22profile%22%3A%7B%22bp%22%3A0%2C%22sr%22%3A1%2C%22dp%22%3A0%2C%22lt%22%3A0%2C%22ps%22%3A0%2C%22cv%22%3A10%2C%22fp%22%3A0%2C%22sp%22%3A1%2C%22br%22%3A0%2C%22ieps%22%3A0%2C%22av%22%3A0%2C%22z1%22%3A11%2C%22jsv%22%3A1%2C%22nav%22%3A1%2C%22z2%22%3A6%2C%22fonts%22%3A24%7D%2C%22main%22%3A5311%2C%22compute%22%3A26%2C%22send%22%3A2996%7D&bp=&sr=%7B%22inner%22%3A%5B1920%2C945%5D%2C%22outer%22%3A%5B1936%2C1056%5D%2C%22screen%22%3A%5B-8%2C-8%5D%2C%22pageOffset%22%3A%5B0%2C0%5D%2C%22avail%22%3A%5B1920%2C1040%5D%2C%22size%22%3A%5B1920%2C1080%5D%2C%22client%22%3A%5B1903%2C5423%5D%2C%22colorDepth%22%3A24%2C%22pixelDepth%22%3A24%7D&dp=%7B%22XDomainRequest%22%3A0%2C%22createPopup%22%3A0%2C%22removeEventListener%22%3A1%2C%22globalStorage%22%3A0%2C%22openDatabase%22%3A0%2C%22indexedDB%22%3A1%2C%22attachEvent%22%3A0%2C%22ActiveXObject%22%3A0%2C%22dispatchEvent%22%3A1%2C%22addBehavior%22%3A0%2C%22addEventListener%22%3A1%2C%22detachEvent%22%3A0%2C%22fireEvent%22%3A0%2C%22MutationObserver%22%3A1%2C%22HTMLMenuItemElement%22%3A1%2C%22Int8Array%22%3A1%2C%22postMessage%22%3A1%2C%22querySelector%22%3A1%2C%22getElementsByClassName%22%3A1%2C%22images%22%3A1%2C%22compatMode%22%3A%22CSS1Compat%22%2C%22documentMode%22%3A0%2C%22all%22%3A1%2C%22now%22%3A1%2C%22contextMenu%22%3Anull%7D&lt="+timeStamp+"%2B8&ps=true%2Ctrue&cv=19f5de4bfc86212d44a26e1fab8d56ca489459d4&fp=false&sp=false&br=Firefox&ieps=false&av=false&z=%7B%22a%22%3A1370277411%2C%22b%22%3A1%2C%22c%22%3A0%7D&zh=&jsv=1.5&nav=%7B%22userAgent%22%3A%22Mozilla%2F5.0%20(Windows%20NT%206.1%3B%20Win64%3B%20x64%3B%20rv%3A56.0)%20Gecko%2F20100101%20Firefox%2F56.0%22%2C%22appName%22%3A%22Netscape%22%2C%22appCodeName%22%3A%22Mozilla%22%2C%22appVersion%22%3A%225.0%20(Windows)%22%2C%22appMinorVersion%22%3A0%2C%22product%22%3A%22Gecko%22%2C%22productSub%22%3A%2220100101%22%2C%22vendor%22%3A%22%22%2C%22vendorSub%22%3A%22%22%2C%22buildID%22%3A%2220170926190823%22%2C%22platform%22%3A%22Win64%22%2C%22oscpu%22%3A%22Windows%20NT%206.1%3B%20Win64%3B%20x64%22%2C%22hardwareConcurrency%22%3A4%2C%22language%22%3A%22zh-CN%22%2C%22languages%22%3A%5B%22zh-CN%22%2C%22zh%22%2C%22en-US%22%2C%22en%22%5D%2C%22systemLanguage%22%3A0%2C%22userLanguage%22%3A0%2C%22doNotTrack%22%3A%22unspecified%22%2C%22msDoNotTrack%22%3A0%2C%22cookieEnabled%22%3Atrue%2C%22geolocation%22%3A1%2C%22vibrate%22%3A1%2C%22maxTouchPoints%22%3A0%2C%22webdriver%22%3A0%2C%22plugins%22%3A%5B%5D%7D&t=c7ecf91dbaa5e9cafbcc8a5c5ca786fa65050167&u=6b76242a5ac853538703e8f58839f9d0&fc=true";
		headerMap.put("Accept", "*/*");
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Referer","https://www.emirates.com/cn/chinese/");
		headerMap.put("Cookie",COOKIE);
		headerMap.remove("Upgrade-Insecure-Requests");
		if(akamUrl==null) return null;
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,akamUrl,param3,"other");
		if(httpVo==null) return null;
		String akbmsc = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ak_bmsc=");
		String oldAk = MyStringUtil.getValue("ak_bmsc=", ";", COOKIE);
		COOKIE = COOKIE.replace("ak_bmsc="+oldAk+";","");
		COOKIE = COOKIE+";"+akbmsc;
		SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd");
		Date DepDate = sf1.parse(depDate);
		SimpleDateFormat sf2 = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
		SimpleDateFormat sf3 = new SimpleDateFormat("ddMMyyyy");
		SimpleDateFormat sf4 = new SimpleDateFormat("ddMMyy");
		String sfDate2 = sf2.format(DepDate);
		String sfDate3 = sf3.format(DepDate);
		String sfDate4 = sf4.format(DepDate);
		String pageUrl = "https://www.emirates.com/sessionhandler.aspx?pageurl=/IBE.aspx&pub=/cn/chinese&j=f&section=IBE";
		String param4 = "j=t&seldcity1="+dep+"&selacity1="+des+"&selddate1="+sfDate2+"&departDate="+sfDate3+"&depShortDate="+sfDate4+"&seladate1=&retShortDate=&seladults=1&selofw=0&selteenager=0&selchildren=0&selinfants=0&selcabinclass=0&selcabinclass1=&showsearch=false&showTeenager=false&showOFW=false&resultby=0&TID=OW&chkFlexibleDates=false&multiCity=";
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Cookie",COOKIE);
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,pageUrl,param4,"other");
		if(httpVo==null) return null;
		String bm_sv = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","bm_sv=");
		COOKIE = COOKIE + ";"+bm_sv;
		headerMap.put("Referer","https://www.emirates.com/");
		headerMap.put("Cookie",COOKIE);
		headerMap.put("Host", "fly4.emirates.com");
		String param5 = "pageurl=%2FIBE.aspx&j=f&section=IBE&j=t&seldcity1="+dep+"&selacity1="+des+"&selddate1="+sfDate2+"&departDate="+sfDate3+"&depShortDate="+sfDate4+"&seladate1=&retShortDate=&seladults=1&selofw=0&selteenager=0&selchildren=0&selinfants=0&selcabinclass=0&selcabinclass1=&showsearch=false&showTeenager=false&showOFW=false&resultby=0&TID=OW&chkFlexibleDates=false&multiCity=&bsp=www.emirates.com";
		String sessionUrl = "https://fly4.emirates.com/CAB/SessionHandler.aspx?target=%2fIBE.aspx&pub=%2fcn%2fchinese&h=db3d79c331f42db012734a209aad98bdd51274b&FlexOnly=";
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,sessionUrl,param5,"other");
		String NET_SessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","ASP.NET_SessionId=");
		String NSC_JCF = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","NSC_JCF=");
		String AvaUrl = "https://fly4.emirates.com/CAB/IBE/SearchAvailability.aspx";
		COOKIE = COOKIE + ";"+NET_SessionId+";"+NSC_JCF;
		headerMap.put("Cookie",COOKIE);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,AvaUrl,"other");
		if(httpVo==null) return null;
		String result = httpVo.getHttpResult();
		return result;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
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
