package com.aft.crawl.crawler.impl.b2c;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import scala.collection.mutable.StringBuilder;

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
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 香港航空 pc单程成人 web
 * @author chenminghong
 */
public class B2CHXCrawler extends Crawler {
	
	//新地址  https://m.hongkongairlines.com 
	//单程 https://m.hongkongairlines.com/ci/index.php/fffticket/search_new?startcity=HKG&endcity=PEK&date=2016-12-22&flighttype=OW&cabintype=E&adultnum=1&childnum=0
	//往返https://m.hongkongairlines.com/ci/index.php/fffticket/search_fc_new3?startcity=HKG&endcity=PEK&date=2016-12-24&flighttype=RT&cabintype=E&adultnum=1&childnum=0&date2=2016-12-27
	private static   String savefile = "E:\\test.txt";
	public final static String owQueryUrl = "https://www.hongkongairlines.com/zh_CN/flight/flightBook/flightSearch_search.html";
	private final static String findFlight = "https://book.hongkongairlines.com/hxet/bookingone/FindFlight.do";
	private final static String FlightSearch = "https://book.hongkongairlines.com/hxet/bookingone/FlightSearch.do";
	private final SimpleDateFormat DMY = new SimpleDateFormat("dd/MM/yyyy");
	private final SimpleDateFormat YMD = new SimpleDateFormat("yyyy-MM-dd");
	
	private MyHttpClientSession httpClientSession = null;
	
	public B2CHXCrawler(String threadMark) {
		super(threadMark);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		List<CrawlResultBase> CrawlResultBases = this.httpResultMap();
		if(CrawlResultBases==null) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息" );
		}
		return CrawlResultBases;
	}
	/**
	 * 单程航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	private List<CrawlResultBase> moreFlight(List<CrawlResultBase> crawlResults,Map<String, Object> resultMap) {
		
		
	return crawlResults;
	}
	
	/**
	 * 获取请求内容
	 * @param url
	 * @param cookieRemark
	 * @param parentCookieRemark
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> httpResultMap() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		String Departure = this.getJobDetail().getDepCode();//出发地   
		String Arrival = this.getJobDetail().getDesCode();//达到地
		String DepDate = this.getJobDetail().getDepDate();//出发时间
		
		Map<String,Object> headerMap = new HashMap<String, Object>();
		httpClientSessionVo.setHeaderMap(headerMap);
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.hongkongairlines.com");
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,"https://www.hongkongairlines.com/zh_CN/homepage","html");
		String ak_bmsc = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ak_bmsc=");
		String akaalb = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "akaalb_www-hongkongairlines-com=");
		//二次
		StringBuilder cookie = new StringBuilder().append("HomeCookiStauts=;hkaRegionAndLang=cn_sc;OriginDC=3;");
		cookie.append(ak_bmsc).append(";").append(akaalb).append(";")
			  .append("recentSearchDeparture=%E5%8C%97%E4%BA%AC("+Departure+")").append(";")
			  .append("recentSearchReturn=%E9%A6%99%E6%B8%AF("+Arrival+")");
		headerMap.put("Cookie", cookie);
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer", "https://www.hongkongairlines.com/zh_CN/homepage");
		
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("flightAmadeusSPBean.fromLocation", Departure);
		paramMap.put("flightAmadeusSPBean.toLocation", Arrival);
		paramMap.put("flightAmadeusSPBean.fromDate", DepDate);
		paramMap.put("flightAmadeusSPBean.toDate", "");
		paramMap.put("flightAmadeusSPBean.tripType", "O");
		paramMap.put("flightAmadeusSPBean.cabin", "E");
		paramMap.put("flightAmadeusSPBean.adult", "1");
		paramMap.put("flightAmadeusSPBean.child", "0");
		paramMap.put("flightAmadeusSPBean.currencyCode", "CNY");
		paramMap.put("countryCodeN", "OLD");
		paramMap.put("bookingCode", "");
		
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, owQueryUrl,paramMap,"other");
		if(httpVo==null) return crawlResults;
	    //三次
		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		String bm_sv = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "bm_sv=");
		
		if(location==null || location.equals(owQueryUrl))return null;	
		StringBuilder cookie1 = new StringBuilder().append(ak_bmsc).append(";").append(bm_sv);
		headerMap.put("Cookie", cookie1);
		headerMap.put("Host", "new.hongkongairlines.com");
		headerMap.remove("Content-Type");
				
 		httpVo = this.httpProxyResultVoGet(httpClientSessionVo, location,"html");
		if(httpVo==null) return crawlResults;	
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		if(JSESSIONID==null || "".equals(JSESSIONID)) return crawlResults;
		cookie1.append(";").append(JSESSIONID);
		String redirectedUrl = location + "&redirected=true";
		headerMap.put("Cookie", cookie1);
		headerMap.put("Referer", location);
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, redirectedUrl,"ConversationID=&ENCRYPTED_QUERY=&QUERY=&redirected=true","html");
		String location1 = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		headerMap.put("Accept-Encoding","gzip, deflate");
		headerMap.remove("Referer");
		headerMap.remove("Content-Type");
		cookie1.append(";").append(JSESSIONID).append(";hkaRegionAndLang=zh_CN");
		headerMap.put("Cookie", cookie1);
		String result1 = this.httpProxyGet(httpClientSessionVo,location1,"html");
		result1 = result1.replaceAll("\r|\n*","");
		String test = getValue("var date\\=\'"+DepDate+"\'", "selected\\=\'false\'", result1);
		if(!test.contains("finalFareAmount")) return crawlResults;
		
		headerMap.put("Accept", "text/html, */*; q=0.01");
		headerMap.put("X-Requested-With", "XMLHttpRequest");
		headerMap.remove("Upgrade-Insecure-Requests");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headerMap.put("Referer","https://new.hongkongairlines.com/hxair/ibe/common/processSearchEntry.do?fromEntryPoint=true");
        
        Map<String,Object> paramMap1 = new HashMap<String, Object>();
		paramMap1.put("Search/calendarOutboundDate",DepDate);
		paramMap1.put("Search/calendarInboundDate","");
		paramMap1.put("Search/calendarSearch", "false");
        String calender = this.httpProxyPost(httpClientSessionVo, "https://new.hongkongairlines.com/hxair/ibe/air/spinnerCalendar.do?spinner=FromFlightCalendar",paramMap1,"html");
        if(calender==null) return crawlResults;
        calender = calender.replaceAll("\r|\n*","");
        List<String> seqNums = getSubUtil("seqNum", "\'", calender);
        int seqNum = seqNums.size()/2;
        for(int t = 1; t<seqNum+1; t++){
	        String[] cabins = {"ECONOMY_LOWEST,FLEXILY_PRODUCT,ECONOMY_PLUS,","BUSINESS_SAVER,BUSINESS_SELECT,BUSINESS_PLUS,"};
	        for(int i=0 ;i<2 ;i++){
		        headerMap.put("Accept", "*/*");
		        Map<String,Object> param3 = new HashMap<String, Object>();
		        param3.put("SequenceNumber",String.valueOf(t));
		        param3.put("FarefamilyCode",cabins[i]);
		        httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "https://new.hongkongairlines.com/hxair/ibe/air/fareRulesTranslate.do",param3,"other");
		        String[] farefamilycodes = cabins[i].split(",");
		        for(int j =0; j<3; j++){
			        headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			        headerMap.remove("X-Requested-With");
			        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
			        headerMap.put("Upgrade-Insecure-Requests","1");      
			        String farefamilycode = farefamilycodes[j];
			        httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "https://new.hongkongairlines.com/hxair/ibe/air/addToCart.do?milesEnable=false","Search%2FsearchType=F&seqNum=1&useLoyalty=false&farefamilycode="+farefamilycode+"&seatClass=+&milesEnableInput=false","other");
					
			        headerMap.remove("Referer");
			        headerMap.remove("Content-Type");            
			        String result = this.httpProxyGet(httpClientSessionVo, "https://new.hongkongairlines.com/hxair/ibe/checkout/shoppingCart.do","html");
			        result = result.replaceAll("\r|\n*","");
			        List<String> resultDatas = getSubUtil("dataObj.orgThreeCode","amount=formatNumber", result);
			        String resultData = resultDatas.get(0);
			        if(resultData == null) continue;
			        FlightData flightData = setFlightData(resultData);
			        crawlResults.add(flightData);
		        }
	        }
        }
		return crawlResults;
	}
	

	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document!=null && document.title().contains("温馨提示")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	public static void main(String[] args) {
//		String itemKey = UUID.randomUUID().toString();
		long epoch = System.currentTimeMillis()/1000;
		System.out.println(epoch);
				
	}
	private FlightData setFlightData(String result) {
		String departureTime = getElementByPattern("itemData.departureTime", result).replace("T", " ");//出发时间
		String depDate = departureTime.substring(0, 10);
		String dep = getElementByPattern("itemData.org", result);//出发地
		String arr = getElementByPattern("itemData.dst", result);//到达地
		FlightData flightData = new FlightData(this.getJobDetail(), "OW", dep, arr, depDate);
		try{
			String cabin = getValue("itemData.cabinClass \\=  code\\+\\'\\(","\\)\\'\\;",result);
			String arrivalTime = getElementByPattern("itemData.arrivalTime", result).replace("T", " ");//到达时间
			String flightNo = getElementByPattern("itemData.flightNo", result);//航班号
			String flightEn = flightNo.substring(0, 2);//航司
//			String totalPrice = getValue("amountOld\\=\'", "\'\\;", result);
			String farePrice = getValue("amount\\=\'", "\'\\;", result);
			List<String> taxs = getSubUtil("finalAdultTaxAmount=", "\\;", result);
			int tax = 0;
			for(String taxindex : taxs){
				tax = Integer.parseInt(taxindex) +tax;
			}
			String Tax = String.valueOf(tax);
			String totalPrice = String.valueOf(tax + Integer.parseInt(farePrice));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(new Date());
			flightData.setCreateTime(date);
			flightData.setRouteType("OW");
			flightData.setAirlineCode(flightEn);//航司
			flightData.setDepAirport(dep);//出发地
			flightData.setArrAirport(arr);//出发地
			flightData.setGoDate(depDate);//出发日期
//			List<FilghtRule> filghtRuleList = new ArrayList<FilghtRule>();//规则
//			FilghtRule filghtRule = new FilghtRule();
//			filghtRule.setRefund("取消及退款费:"+refundInfo);//退票规则
//			filghtRule.setEndorse("更改日期和航班费:"+changeInfo);//转签规则
//			filghtRule.setOther("误机费:"+noshowInfo+";所得金鹏积分:"+mileInfo);
//			filghtRuleList.add(filghtRule);
//			flightData.setRule(filghtRuleList);
			
			List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
			FlightPrice flightPrice = new FlightPrice();
			flightPrice.setPassengerType("ADT");
			flightPrice.setFare(totalPrice);//票面价
			flightPrice.setTax(Tax);//税费
			flightPrice.setCurrency("CNY");//币种
			flightPrice.setEquivFare(totalPrice);
			flightPrice.setEquivTax(Tax);
			flightPrice.setEquivCurrency("CNY");
			flightPriceList.add(flightPrice);
			//儿童价
//			FlightPrice flightPrice2 = new FlightPrice();
//			flightPrice2.setPassengerType("CHD");
//			flightPrice2.setFare(childTotalTkt);//票面价
//			flightPrice2.setTax(childTotalTax);//税费
//			flightPrice2.setCurrency("CNY");//币种
//			flightPrice2.setEquivFare(childTotalTkt);
//			flightPrice2.setEquivTax(childTotalTax);
//			flightPrice2.setEquivCurrency("CNY");
//			flightPriceList.add(flightPrice2);
			flightData.setPrices(flightPriceList);
			
			List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
			FlightSegment flightSegment = new FlightSegment();
			flightSegment.setTripNo(1);
			flightSegment.setAirlineCode(flightEn);
			flightSegment.setFlightNumber(flightNo);
			flightSegment.setDepAirport(dep);
			flightSegment.setDepTime(departureTime);
			flightSegment.setArrAirport(arr);
			flightSegment.setCabinCount("9");
			flightSegment.setArrTime(arrivalTime);
			String shareFlight = this.getShareFlight(flightEn);
			flightSegment.setCodeShare(shareFlight);
			flightSegment.setCabin(cabin);
//			flightSegment.setCabinCount(cabinNum);
			flightSegment.setAircraftCode("");
			flightSegmentList.add(flightSegment);
			flightData.setFromSegments(flightSegmentList);
		}catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + result + "\r", e);
			throw e;	
		}
		return flightData;
	}
	
	
	//正则抓去匹配集合
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
	
	//通过正则抓取航班号
			public String getElementByPattern(String ele,String str){
				 String stri = null;
				 Pattern par = Pattern.compile(ele+" = \'(.*?)\'\\;");
				 Matcher mat = par.matcher(str);
				 while(mat.find()){  
					   stri = mat.group(1);
		         }
				 return stri;
			}
			
			public String getValue(String sta,String end,String str){
				 String stri = null;
				 Pattern par = Pattern.compile(sta+"(.*?)"+end);
				 Matcher mat = par.matcher(str);
				 while(mat.find()){  
					   stri = mat.group(1);
		         }
				 return stri;
			}

			@Override
			public String httpResult() throws Exception {
				return null;
			}
			
}

