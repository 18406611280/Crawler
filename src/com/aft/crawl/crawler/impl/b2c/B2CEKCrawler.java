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
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 阿联酋航空
 * @author chenminghong
 */

public class B2CEKCrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	
	public B2CEKCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		//加载汇率
		synchronized(Crawler.class){
			if(rateMap.size()==0){
				CurrencyUtil.putRate(rateMap);
			}
		}
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
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		String signUrl = "https://www.emirates.com/cn/chinese/";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.emirates.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
		if(httpVo==null) return null;
		String COOKIE = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
		String resu = httpVo.getHttpResult();
		String akamUrl = MyStringUtil.getValue("\\<noscript\\>\\<img src\\=\"", "\\?a", resu);
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String param3 = "ap=true&bt=0&fonts=41%2C43%2C44%2C45%2C46%2C47%2C48%2C49%2C50%2C51%2C52%2C55%2C56%2C57%2C58%2C59%2C60%2C61%2C62%2C63%2C64%2C65%2C67%2C68%2C69&fh=2b546713c41a929cd4d394582b29987bbb097c98&timing=%7B%221%22%3A26%2C%222%22%3A2971%2C%22profile%22%3A%7B%22bp%22%3A0%2C%22sr%22%3A1%2C%22dp%22%3A0%2C%22lt%22%3A0%2C%22ps%22%3A0%2C%22cv%22%3A10%2C%22fp%22%3A0%2C%22sp%22%3A1%2C%22br%22%3A0%2C%22ieps%22%3A0%2C%22av%22%3A0%2C%22z1%22%3A11%2C%22jsv%22%3A1%2C%22nav%22%3A1%2C%22z2%22%3A6%2C%22fonts%22%3A24%7D%2C%22main%22%3A5311%2C%22compute%22%3A26%2C%22send%22%3A2996%7D&bp=&sr=%7B%22inner%22%3A%5B1920%2C945%5D%2C%22outer%22%3A%5B1936%2C1056%5D%2C%22screen%22%3A%5B-8%2C-8%5D%2C%22pageOffset%22%3A%5B0%2C0%5D%2C%22avail%22%3A%5B1920%2C1040%5D%2C%22size%22%3A%5B1920%2C1080%5D%2C%22client%22%3A%5B1903%2C5423%5D%2C%22colorDepth%22%3A24%2C%22pixelDepth%22%3A24%7D&dp=%7B%22XDomainRequest%22%3A0%2C%22createPopup%22%3A0%2C%22removeEventListener%22%3A1%2C%22globalStorage%22%3A0%2C%22openDatabase%22%3A0%2C%22indexedDB%22%3A1%2C%22attachEvent%22%3A0%2C%22ActiveXObject%22%3A0%2C%22dispatchEvent%22%3A1%2C%22addBehavior%22%3A0%2C%22addEventListener%22%3A1%2C%22detachEvent%22%3A0%2C%22fireEvent%22%3A0%2C%22MutationObserver%22%3A1%2C%22HTMLMenuItemElement%22%3A1%2C%22Int8Array%22%3A1%2C%22postMessage%22%3A1%2C%22querySelector%22%3A1%2C%22getElementsByClassName%22%3A1%2C%22images%22%3A1%2C%22compatMode%22%3A%22CSS1Compat%22%2C%22documentMode%22%3A0%2C%22all%22%3A1%2C%22now%22%3A1%2C%22contextMenu%22%3Anull%7D&lt="+timeStamp+"%2B8&ps=true%2Ctrue&cv=19f5de4bfc86212d44a26e1fab8d56ca489459d4&fp=false&sp=false&br=Firefox&ieps=false&av=false&z=%7B%22a%22%3A1370277411%2C%22b%22%3A1%2C%22c%22%3A0%7D&zh=&jsv=1.5&nav=%7B%22userAgent%22%3A%22Mozilla%2F5.0%20(Windows%20NT%206.1%3B%20Win64%3B%20x64%3B%20rv%3A56.0)%20Gecko%2F20100101%20Firefox%2F56.0%22%2C%22appName%22%3A%22Netscape%22%2C%22appCodeName%22%3A%22Mozilla%22%2C%22appVersion%22%3A%225.0%20(Windows)%22%2C%22appMinorVersion%22%3A0%2C%22product%22%3A%22Gecko%22%2C%22productSub%22%3A%2220100101%22%2C%22vendor%22%3A%22%22%2C%22vendorSub%22%3A%22%22%2C%22buildID%22%3A%2220170926190823%22%2C%22platform%22%3A%22Win64%22%2C%22oscpu%22%3A%22Windows%20NT%206.1%3B%20Win64%3B%20x64%22%2C%22hardwareConcurrency%22%3A4%2C%22language%22%3A%22zh-CN%22%2C%22languages%22%3A%5B%22zh-CN%22%2C%22zh%22%2C%22en-US%22%2C%22en%22%5D%2C%22systemLanguage%22%3A0%2C%22userLanguage%22%3A0%2C%22doNotTrack%22%3A%22unspecified%22%2C%22msDoNotTrack%22%3A0%2C%22cookieEnabled%22%3Atrue%2C%22geolocation%22%3A1%2C%22vibrate%22%3A1%2C%22maxTouchPoints%22%3A0%2C%22webdriver%22%3A0%2C%22plugins%22%3A%5B%5D%7D&t=c7ecf91dbaa5e9cafbcc8a5c5ca786fa65050167&u=6b76242a5ac853538703e8f58839f9d0&fc=true";
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
