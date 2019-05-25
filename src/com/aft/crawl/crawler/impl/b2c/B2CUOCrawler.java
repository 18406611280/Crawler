package com.aft.crawl.crawler.impl.b2c;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FilghtRule;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;


/**
 * 香港快运航空 
 * @author chenminghong
 */
public class B2CUOCrawler extends Crawler{
	
	private final static String bookUrl = "https://booking.hkexpress.com/";
	
	private final static String selectUrl = "https://booking.hkexpress.com/en-US/select?origin=%dep%&destination=%des%";
	
	private final static String searchUrl = "https://booking.hkexpress.com/en-US/Search/FareSelect";
	
	private String dep = null;
	
	private String des = null;
	
	private String DepDate = null;
	
	public B2CUOCrawler(String threadMark) {
		super(threadMark);
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		List<String> resultList = this.httpResultList();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		FlightData flightData = null;
		if(resultList == null) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息");
			return crawlResults;
		}
			for(String httpResult : resultList){
				try {
				flightData = new FlightData(this.getJobDetail(), "OW", dep, des, DepDate);
				String flightNo = getValue("Flight:", "</b>", httpResult).trim();
				String flightEn = flightNo.substring(0, 2);
				String shareFlight = this.getShareFlight(flightEn);
				String startTime = getValue("Departs</span><time>", "</time>", httpResult);
				String endTime = getValue("Arrives</span><time>", "</time>", httpResult);
				String arrTime = DepDate+" "+endTime+":00";;
				if(Integer.parseInt(startTime.replace(":", "")) > Integer.parseInt(endTime.replace(":", ""))){
					arrTime = addOneDay(DepDate)+" "+endTime+":00";
				}else{
					arrTime=DepDate+" "+endTime+":00";
				}
				String depTime = DepDate+" "+startTime+":00";
				String price1 = getValue(">","</span>",getValue("data-pointscost=","</th>", httpResult));
				String ticketPrice = price1.substring(3).replace(",","").trim();
				String totalPrice = getValue("class=\"totalPackage\">", "</span>", httpResult);
				if(totalPrice.contains(",")){
					 totalPrice = totalPrice.replace(",", "");
				}
				int total = Integer.parseInt(totalPrice);
				int price = Integer.parseInt(ticketPrice);
				String tax = String.valueOf(total-price);
				String currency = getValue("class=\"currency\">", "</span>", httpResult);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setRouteType("OW");
				flightData.setAirlineCode(flightEn);//航司
				flightData.setDepAirport(dep);//出发地
				flightData.setArrAirport(des);//出发地
				flightData.setGoDate(DepDate);//出发日期
				List<FilghtRule> filghtRuleList = new ArrayList<FilghtRule>();//规则
				flightData.setRule(filghtRuleList);
				
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
				
				List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
				FlightSegment flightSegment = new FlightSegment();
				flightSegment.setTripNo(1);
				flightSegment.setAirlineCode(flightEn);
				flightSegment.setFlightNumber(flightNo);
				flightSegment.setDepAirport(dep);
				flightSegment.setDepTime(depTime);
				flightSegment.setArrAirport(des);
				flightSegment.setArrTime(arrTime);
				flightSegment.setCodeShare(shareFlight);
				flightSegmentList.add(flightSegment);
				flightData.setFromSegments(flightSegmentList);
				crawlResults.add(flightData);
		        } catch(Exception e) {
				logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
				throw e;
	        	}
	}
			return crawlResults;
	}
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("出错啦") || httpResult.contains("403")){
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	public List<String> httpResultList() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		DepDate = this.getJobDetail().getDepDate();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Date date = sdf.parse(DepDate); 
		String depDate = sdf2.format(date);
		List<String> stringList = new ArrayList<String>();
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		StringBuilder cookie = new StringBuilder().append("s_sq=hkexpress-web-prd%3D%2526c.%2526a.%2526activitymap.%2526page%253Dwww.hkexpress.com%25252Fen-hk%2526link%253DSearch%2526region%253Dsearch_flight%2526pageIDType%253D1%2526.activitymap%2526.a%2526.c%2526pid%253Dwww.hkexpress.com%25252Fen-hk%2526pidt%253D1%2526oid%253DSearch%2526oidt%253D3%2526ot%253DSUBMIT");
		headerMap.put("Cookie",cookie);
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer", "https://www.hkexpress.com/en-hk"); 
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "booking.hkexpress.com");
		httpClientSessionVo.setHeaderMap(headerMap); 
		
		String param = "request=booking&SearchType=Oneway&OriginStation="+dep+"&DestinationStation="+des+"&Adults=1&Children=0&Infants=0&FlexibleDate=false&LowFareFinderSelected=false&MultiOriginStation1=&MultiDestinationStation1="+des+"&MultiOriginStation2=&MultiDestinationStation2=&getAdults=1+Adult+&getChildren=0+Child&getInfants=0+Infant&multisearch_from2=&multisearch_to2=&DepartureDate="+depDate+"&promotionCode=";
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo,bookUrl,param,"html");
 		String NET_SessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId=");
 		if(NET_SessionId==null){
 			return null;
 		}
		String SERVERID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "SERVERID=");
		
		cookie.append(";").append(NET_SessionId)
		      .append(";").append(SERVERID);
		headerMap.put("Cookie", cookie);
		headerMap.remove("Content-Type");
		
		String SelectUrl = selectUrl.replace("%dep%", dep).replace("%des%", des);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,SelectUrl," html");
		String Token = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "__RequestVerificationToken=");
		String acw_tc = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "acw_tc=");
		cookie.append(";").append(Token)
	          .append(";").append(acw_tc);
	    String result1 = httpVo.getHttpResult();
	    String aftoken = getAftoken(result1);
	    List<String> keyList = getSellKeys(result1);
	    
	    headerMap.put("Cookie", cookie);
	    headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
	    headerMap.put("X-Requested-With", "XMLHttpRequest");
	    headerMap.put("Content-Type", "application/json");
	    headerMap.put("Referer",SelectUrl); 
	    for(String key : keyList){
	        String jsonParam = "{\"JourneyFareSellKeys\":[\""+key+"\"],\"aftoken\":\""+aftoken+"\"}";
	        String result = this.httpProxyPost(httpClientSessionVo,searchUrl,jsonParam,"html");
	        if(result !=null &&result.contains("Flight")){
	        	stringList.add(result);
	        }  
	    }
		return stringList;
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
		public static List<String> getSellKeys(String str){  
	        List<String> list = new ArrayList<String>();  
	        Pattern pattern = Pattern.compile("<input type=\"radio\" value=\"(.*?)\"");// 匹配的模式  
	        Matcher m = pattern.matcher(str);  
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
		
		//日期加一天
		public String addOneDay(String depDate) throws ParseException{
			    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");  
		        Date data = f.parse(depDate);
		        Calendar c = Calendar.getInstance();  
		        c.setTime(data);  
		        c.add(Calendar.DAY_OF_MONTH, 1);// 今天+1天  
		   
		        Date twoDay = c.getTime();  
		        String TwoDay = f.format(twoDay);
		        return TwoDay;
		}
		
		@Override
		public String httpResult() throws Exception {
			return null;
		}

}
