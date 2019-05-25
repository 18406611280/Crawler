package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.impl.client.CloseableHttpClient;
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

/**
 * 狮子航空网页版
 * @author chenminghong
 *
 */
public class B2CJTCrawler extends Crawler{
    
	private String dep = null;
	private String des = null;
	private String DepDate = null;
	
	public B2CJTCrawler(String threadMark) {
		super(threadMark);
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
//		String result1 = StringTxtUtil.TxtToString("C:\\Users\\Administrator.LUTAO-805171435\\Desktop\\shopp\\jt.txt");
//		setFlightData(result1);
		List<CrawlResultBase> CrawlResultBases = this.httpResultMap();
		if(CrawlResultBases==null) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息" );
		}
		return CrawlResultBases;
	}
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("Connection timed out")){
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	public List<CrawlResultBase> httpResultMap() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		DepDate = this.getJobDetail().getDepDate();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date date = sdf.parse(DepDate); 
		String depDate = sdf2.format(date);
		List<String> stringList = new ArrayList<String>();
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "mobile.lionair.co.id");
		httpClientSessionVo.setHeaderMap(headerMap); 
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://mobile.lionair.co.id/JT","","html");
		String sessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId=");
 		String Location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
 		Location = "https://mobile.lionair.co.id"+Location;
 		String t = Location.substring(Location.length()-2, Location.length());
 		if(sessionId==null){
 			return null;
 		}
		StringBuilder cookie = new StringBuilder();
		cookie.append(sessionId);
		String res1 = this.httpProxyGet(httpClientSessionVo,Location,"html");
		
		headerMap.put("Referer",Location);
		String searchUrl = "https://mobile.lionair.co.id/JT/Search?t="+t;
		String res2 = this.httpProxyGet(httpClientSessionVo,searchUrl,"html");
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Referer",searchUrl);
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("pjourney","1");
		paramMap.put("depCity",dep);
		paramMap.put("arrCity",des);
		paramMap.put("dpd1",depDate);
		paramMap.put("sAdult", "1");
		paramMap.put("sChild", "0");
		paramMap.put("sInfant", "0");
		paramMap.put("cTabID",t);
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://mobile.lionair.co.id/JT/Search/SearchFlight",paramMap,"html");
		String CustomerInfo = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "CustomerInfo=");
		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		if(location.contains("ModifySearch")) return null;
		location = "https://mobile.lionair.co.id"+location;
		cookie.append(";").append(CustomerInfo);
		headerMap.remove("Content-Type");
		String result2 = this.httpProxyGet(httpClientSessionVo,location,"html");
		List<String> params = getSellParams(result2);
		for(String param :params){
			String[] strs = param.split(",");
			String pIBIndex = strs[0];
			String cabin = strs[1].substring(1, 2);
			String pIBAFIndex = strs[3];
			String flightUrl = "https://mobile.lionair.co.id/JT/Flight?t="+t;
			String Param = "pIBIndex="+pIBIndex+"&pIBClass="+cabin+"&pIBAFIndex="+pIBAFIndex+"&IsOBFlight=1&t="+t;
			headerMap.put("Accept","*/*");
			headerMap.put("X-Requested-With","XMLHttpRequest");
			headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
			headerMap.put("Referer",location);
			headerMap.remove("Upgrade-Insecure-Requests");
			httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"https://mobile.lionair.co.id/JT/Flight/SetSelectedFlightIBIndex",Param,"html");
			httpVo = this.httpProxyResultVoGet(httpClientSessionVo,flightUrl,"html");
			
			headerMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			headerMap.put("Cache-Control","no-cache");
			headerMap.put("Upgrade-Insecure-Requests","1");
			headerMap.put("Pragma","no-cache");
			headerMap.put("Referer",flightUrl);
			headerMap.remove("X-Requested-With");
			String result = this.httpProxyGet(httpClientSessionVo,location,"html");
			FlightData flightData = setFlightData(result);
			crawlResults.add(flightData);
		}
		return crawlResults;
	}
	
	public String getAftoken(String str){
		 String stri = null;
		 Pattern par = Pattern.compile("__RequestVerificationToken\" type=\"hidden\" value=\"(.*?)\"");
		 Matcher mat = par.matcher(str);
		 while(mat.find()){  
			   stri = mat.group(1);
        }
		 return stri;
	}
	
	
	    //正则抓去匹配集合
		public static List<String> getSellParams(String str){  
	        List<String> list = new ArrayList<String>();  
	        Pattern pattern = Pattern.compile("javascript\\:setSelectedFlight\\((.*?)\\)");// 匹配的模式  
	        Matcher m = pattern.matcher(str);  
	        while (m.find()) {  
	            int i = 1;  
	            list.add(m.group(i));  
	            i++;  
	        }  
	        return list;  
	    }  
		
		public static List<String> getSubUtil(String sta,String end,String soap){  
	        List<String> list = new ArrayList<String>();  
	        Pattern pattern = Pattern.compile(sta+"(.*?)"+end);// 匹配的模式  
	        Matcher m = pattern.matcher(soap);  
	        while (m.find()) {  
	            int i = 1;  
	            list.add(m.group(i));  
	            i++;  
	        }  
	        return list;  
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
		
		private FlightData setFlightData(String result) throws Exception {
			FlightData flightData = new FlightData(this.getJobDetail(), "OW", null, null, DepDate);
			try{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setAirlineCode("JT");//航司
				result = result.replaceAll("\r|\n*","");
				String element = getValue("divBookingSummary","align-center",result);
				String basePrice = getValue("IDR","\\<\\/p\\>",getValue("Base Fare","Tax", element)).trim().replaceAll(",", "");//票面价
				String taxPrice = getValue("IDR","\\<\\/p\\>",getValue("Tax","Total Amount", element)).trim().replaceAll(",", "");//税费
				String totalPrice = getValue("IDR","\\<\\/p\\>",getValue("Total Amount","resultRow listSlide", element)).trim().replaceAll(",", "");//总价格
				BigDecimal TicketPrice = new BigDecimal(basePrice).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal TaxPrice = new BigDecimal(taxPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
				List<String> flights = getSubUtil("row date_time","fa fa-tag",element);
				List<String> canbins = getSubUtil("<i class=\"fa fa-tag\"></i>","</span>", element);
				int i = 0;
				//设置价格
				List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
				FlightPrice flightPrice = new FlightPrice();
				flightPrice.setPassengerType("ADT");
				flightPrice.setFare(TicketPrice.toString());//票面价
				flightPrice.setTax(TaxPrice.toString());//税费
				flightPrice.setCurrency("IDR");//币种
				flightPrice.setEquivFare(TicketPrice.toString());
				flightPrice.setEquivTax(TaxPrice.toString());
				flightPrice.setEquivCurrency("IDR");
				flightPriceList.add(flightPrice);
				flightData.setPrices(flightPriceList);
				//设置航段
				List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
				String DEP = "";
				String DES = "";
				int fliSize = flights.size();
				for(String flight : flights){
					String cabin = getValue("\\(","\\)", canbins.get(i));
					SimpleDateFormat sdf1 = new SimpleDateFormat("dd MMM yyyy",Locale.US);
					SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
					List<String> doubleDiv = getSubUtil("class=\"double\"","\\<\\/div\\>",flight);
					String div1 = doubleDiv.get(0);
					String div2 = doubleDiv.get(1);
					String dep = getValue("\\<label\\>", "\\<\\/label\\>", div1);
					if(i == 0) {
						DEP = dep;
					}
					String des = getValue("\\<label\\>", "\\<\\/label\\>", div2);
					if(i == fliSize-1) {
						DES = des;
					}
					String depDate = getValue("\\<span\\>", "\\<\\/span\\>", div1).substring(5);
					depDate = sdf2.format(sdf1.parse(depDate));
					String desDate = getValue("\\<span\\>", "\\<\\/span\\>", div2).substring(5);
					desDate = sdf2.format(sdf1.parse(desDate));
					String depTime = getValue("\\<large\\>", "\\<\\/large\\>", div1);
					String desTime = getValue("\\<large\\>", "\\<\\/large\\>", div2);
					String departureTime = depDate+" "+depTime+":00";//出发时间
					String arrivalTime = desDate+" "+desTime+":00";//出发时间
					String fli = getValue("Flight No.\\<\\/span\\>","\\<\\/span\\>", flight);
					String flightNo = getValue("\\<label\\>","\\<\\/label\\>",fli).trim().replace(" ", "");
					String flightEn = flightNo.substring(0, 2);
					
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(i+1);
					flightSegment.setAirlineCode(flightEn);
					flightSegment.setFlightNumber(flightNo);
					flightSegment.setDepAirport(dep);
					flightSegment.setDepTime(departureTime);
					flightSegment.setArrAirport(des);
					flightSegment.setCabinCount("9");
					flightSegment.setArrTime(arrivalTime);
					String shareFlight = this.getShareFlight(flightEn);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setAircraftCode("");
					flightSegmentList.add(flightSegment);
					i++;
				}
				flightData.setFromSegments(flightSegmentList);
				flightData.setDepAirport(DEP);
				flightData.setArrAirport(DES);
			}catch(Exception e) {
				logger.error(this.getJobDetail().toStr() + ", 封装航程信息异常:" + result + "\r", e);
				throw e;	
			}
			return flightData;
		}
		
		@Override
		public String httpResult() throws Exception {
			return null;
		}
}
