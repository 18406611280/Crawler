package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;

/**
 * 飞萤航空Web官网
 * @author chenminghong
 */
public class B2CFYCrawler extends Crawler {
	
	private String dep = null;
	private String des = null;
	private String depDate = null;
	
	public B2CFYCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		List<CrawlResultBase> CrawlResultBases = this.httpResultMap();
		if(CrawlResultBases==null) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息" );
		}
		return CrawlResultBases;
	}
	
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document.title().contains("404 Page Not Found") || document.title().contains("403 - Forbidden: Access is denied")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	public List<CrawlResultBase> httpResultMap() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("__EVENTARGUMENT", "");
		paramMap.put("__EVENTTARGET", "");
		paramMap.put("pageToken", "");
		paramMap.put("Page", "Select");
		paramMap.put("ControlGroupSearchView$ButtonSubmit", "Search");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListFareTypes", "");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$RadioButtonMarketStructure", "OneWay");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$TextBoxMarketOrigin1",dep);
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$TextBoxMarketDestination1",des);
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketDateRange1", "");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketDay1",depDate.substring(8,10));
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketMonth1",depDate.substring(0,7));
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketDateRange2", "");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketDay2", "");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListMarketMonth2", "");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_ADT", "1");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListPassengerType_INFANT", "0");
		paramMap.put("test", "");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$PromotionCode", "");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListCurrency", "MYR");
		paramMap.put("ControlGroupSearchView$AvailabilitySearchInputSearchView$DropDownListSearchBy", "");
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer", "http://www.fireflyz.com.my/");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "booking.fireflyz.com.my");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "https://booking.fireflyz.com.my/Search.aspx", paramMap, "html");
		
		String sessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId=");
		String skysales = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "skysales=");
		
		
		StringBuilder cookie = new StringBuilder()
				.append(sessionId).append(";")
				.append(skysales);
		
		//把首次请求返回的cookies放到header里面进行二次请求
		headerMap.put("Cookie",cookie);
		String result1 = this.httpProxyGet(httpClientSessionVo, "https://booking.fireflyz.com.my/Select.aspx", "html");
		List<String> results = getSubUtil("name\\=\"ControlGroupSelectView\\$AvailabilityInputSelectView\\$market1\" value=\"","\"", result1);
		Collections.reverse(results);
		if(results==null) return null;
		headerMap.put("Accept", "text/html, */*");
		headerMap.put("X-Requested-With", "XMLHttpRequest");
		headerMap.put("Referer", "https://booking.fireflyz.com.my/Select.aspx");
		List<String> datas = new ArrayList<String>();
		for(String pa : results){
			pa = pa.replaceAll(" ", "%20").replaceAll("\\^", "%5E").replaceAll("\\|", "%7C");
			String url = "https://booking.fireflyz.com.my/TaxAndFeeInclusiveDisplayAjax-resource.aspx?flightKeys="+pa+"&numberOfMarkets=1&keyDelimeter=%2C";
			String result = this.httpProxyGet(httpClientSessionVo,url, "html");
			if(result==null) continue;
			datas.add(result);
		}
		for(String result :datas){
			FlightData flightData = setFlightData(result);
			crawlResults.add(flightData);
		}
		return crawlResults;
	}
	
	private FlightData setFlightData(String result) {
		FlightData flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(new Date());
			flightData.setCreateTime(date);
			flightData.setAirlineCode("FY");//航司
			result = result.replaceAll("\r|\n*","");
			List<String> depDes = getSubUtil("booking\\-summary\\-bg\\.png\\)\\;text\\-align\\:center\\;font\\-size\\:10pt\\;font\\-weight\\:bold\\;\">","\\<\\/td\\>",result);
			List<String> times = getSubUtil("text-align\\:center\\;font\\-size\\:8pt\\;\"\\>","\\<\\/td\\>",result);
			List<String> flights = getSubUtil("Flight","\\<\\/div\\>",result);
			String ticketPrice = getValue("1 x","\\<\\/td\\>",result).replaceAll("≈", "").replaceAll("MYR","").replaceAll(",", "").trim();
			String totalPrice = getValue("\\<td style\\=\"font\\-size\\:8pt\\;font\\-weight\\:bold\\;padding\\:2px\\;\" align=\"right\"\\>","\\<\\/td\\>",result).replaceAll("≈", "").replaceAll("MYR","").replaceAll(",", "").trim();
			
			BigDecimal TicketPrice = new BigDecimal(ticketPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal TotalPrice = new BigDecimal(totalPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal taxPrice = TotalPrice.subtract(TicketPrice);
			
			BigDecimal rate = CurrencyUtil.getRequest3("MYR", "CNY");
			BigDecimal cnyPrice = TicketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
			BigDecimal cnyTaxPrice = taxPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
			int i = depDes.size()/2;
			//设置价格
			List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
			FlightPrice flightPrice = new FlightPrice();
			flightPrice.setPassengerType("ADT");
			flightPrice.setFare(cnyPrice.toString());//票面价
			flightPrice.setEquivFare(cnyPrice.toString());
			flightPrice.setTax(cnyTaxPrice.toString());//税费
			flightPrice.setEquivTax(cnyTaxPrice.toString());
			flightPrice.setCurrency("CNY");//币种
			flightPrice.setEquivCurrency("CNY");
			flightPriceList.add(flightPrice);
			flightData.setPrices(flightPriceList);
			//设置航段
			List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
			for(int j=0;j<i;j++){
				int t = j*2;
				String dep = depDes.get(t);
				String des = depDes.get(t+1);
				String depTime = times.get(t);
				String desTime = times.get(t+1);
				String departureTime = depDate+" "+depTime;//出发时间
				String arrivalTime = depDate+" "+desTime;//出发时间
				String flightNo = flights.get(j+1).trim();
				String flightEn = flightNo.substring(0, 2);
				
				FlightSegment flightSegment = new FlightSegment();
				flightSegment.setTripNo(j+1);
				flightSegment.setAirlineCode(flightEn);
				flightSegment.setFlightNumber(flightNo);
				flightSegment.setDepAirport(dep);
				flightSegment.setDepTime(departureTime);
				flightSegment.setArrAirport(des);
				flightSegment.setCabinCount("9");
				flightSegment.setArrTime(arrivalTime);
				String shareFlight = this.getShareFlight(flightEn);
				flightSegment.setCodeShare(shareFlight);
				flightSegment.setCabin("");
				flightSegment.setAircraftCode("");
				flightSegmentList.add(flightSegment);
			}
			flightData.setFromSegments(flightSegmentList);
		}catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 封装航程信息异常:" + result + "\r", e);
			throw e;	
		}
		return flightData;
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
	

	@Override
	public String httpResult() throws Exception {
		return null;
	}

}