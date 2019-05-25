package com.aft.crawl.crawler.impl.b2c;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FilghtRule;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
public class B2CZHCrawler extends Crawler{
	
	public B2CZHCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		FlightData flightData = null;
		String Departure = this.getJobDetail().getDepCode();//出发地
		String Arrival = this.getJobDetail().getDesCode();//达到地
		String DepDate = this.getJobDetail().getDepDate();//出发时间
		try {
			List<String> results = new ArrayList<String>();
			results = getStringList("acType", "y_classInfo", httpResult);
			if(null == results){
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息;");
				return crawlResults;
			}
			for(String str:results){
				String fltNo = getElementByPattern("flightNo",str);//航班号
				String fltEN = fltNo.substring(0, 2);//航司
				String shareFlight = this.getShareFlight(fltEN);//判断是否共享
				String acType = getElementByPattern("acType",str);//机型
				String orgCity = getElementByPattern("orgCity",str);//出发地
				String dstCity = getElementByPattern("dstCity",str);//到达地
				String orgDate = getElementByPattern("orgDate",str);//出发日期
				String dstDate = getElementByPattern("dstDate",str);//到达日期
				String orgTime = getElementByPattern("orgTime",str);//出发时间
				String dstTime = getElementByPattern("dstTime",str);//到达时间
				String depTime = orgDate + " "+orgTime+":00";//出发日期时间 2017-11-24 10:00:00
				String arrTime = dstDate + " "+dstTime+":00";//到达日期时间
				List<String> priceList = new ArrayList<String>();
				priceList = getStringList("accMileage","ticketPrice", str);
				Set<String> prices=new HashSet<String>(priceList);//去掉重复
				for(String price : prices){
					flightData = new FlightData(this.getJobDetail(), "OW", Departure, Arrival, DepDate);
					String cabinCount = getElementByPattern("storage",price);//舱位数
					if(cabinCount.equals("A")){
						cabinCount="9";
					}
					if(cabinCount.equals("0")){
						continue;
					}
					String cabin = getElementByPattern("classCode",price);//舱位
					String ticketPrice = getElementByPattern("classPrice",price);//票面价
					String changeRefund = getElementByPattern("changeRefund",price);//退票规则
					String endorse= getElementByPattern("ei",price);//签转规则
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					flightData.setRouteType("OW");
					flightData.setAirlineCode(fltEN);//航司
					flightData.setDepAirport(orgCity);//出发地
					flightData.setArrAirport(dstCity);//出发地
					flightData.setGoDate(DepDate);//出发日期
					List<FilghtRule> filghtRuleList = new ArrayList<FilghtRule>();//规则
					FilghtRule filghtRule = new FilghtRule();
					filghtRule.setRefund(changeRefund);//退票规则
					filghtRule.setEndorse(endorse);//转签规则
					filghtRuleList.add(filghtRule);
					flightData.setRule(filghtRuleList);
					
					List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
					FlightPrice flightPrice = new FlightPrice();
					flightPrice.setPassengerType("ADT");
					flightPrice.setFare(ticketPrice);//票面价
					flightPrice.setTax("50");//税费
					flightPrice.setCurrency("CNY");//币种
					flightPrice.setEquivFare(ticketPrice);
					flightPrice.setEquivTax("50");
					flightPrice.setEquivCurrency("CNY");
					flightPriceList.add(flightPrice);
					flightData.setPrices(flightPriceList);
					
					List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(1);
					flightSegment.setAirlineCode(fltEN);
					flightSegment.setFlightNumber(fltNo);
					flightSegment.setDepAirport(orgCity);
					flightSegment.setDepTime(depTime);
					flightSegment.setArrAirport(dstCity);
					flightSegment.setArrTime(arrTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setCabinCount(cabinCount);
					flightSegment.setAircraftCode(acType);
					flightSegmentList.add(flightSegment);
					flightData.setFromSegments(flightSegmentList);
					crawlResults.add(flightData);
				}
			}
		}catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
			
		}
		return crawlResults;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("出错啦") || httpResult.contains("403拒绝访问")){
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}

	public String httpResult() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		String dep = this.getJobDetail().getDepCode();
		String des = this.getJobDetail().getDesCode();
		String depDate = this.getJobDetail().getDepDate();
		String newDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.shenzhenair.com");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		
//      第一次访问
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "http://www.shenzhenair.com/","","html");
		
//		StringBuilder cookie = new StringBuilder().append("sign_flight="+signFlight);
		String AlteonP = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "AlteonP=");
		String signcCookie = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "sign_cookie=");
//		
//		
		StringBuilder cookie = new StringBuilder()
				.append(AlteonP).append(";")
				.append(signcCookie);
		headerMap.put("Cookie", cookie);
		String result1 = this.httpProxyGet(httpClientSessionVo, "http://www.shenzhenair.com/","html");
		String signFlight  = getCookieByPattern("'cookie'", result1);
		cookie.append(";").append("sign_flight="+signFlight);
		
//		headerMap.put("Cookie", cookie);
//		headerMap.put("Referer", "http://www.shenzhenair.com/");
//		String result6 = this.httpProxyGet(httpClientSessionVo, "http://www.shenzhenair.com/","html");
		
		headerMap.put("Cookie", cookie);
		headerMap.put("Accept", "*/*");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		headerMap.put("Referer", "http://www.shenzhenair.com/");
		Map<String,Object> paramMap1 = new HashMap<String, Object>();
		paramMap1.put("sourceId","");
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "http://www.shenzhenair.com/szair_B2C/login/loginOrOut.action",paramMap1,"other");
		String sessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String fox = ";x-apm-brtm-bt-p=Firefox";
		String pv = ";x-apm-brtm-bt-pv=56";
		String res = ";x-apm-brtm-response-bt-id=2";
		
		String url = "http://www.shenzhenair.com/szair_B2C/flightsearch.action?orgCityCode="+dep+"&dstCityCode="+des+"&hcType=DC&orgCity=&orgDate="+depDate+"&dstCity=&dstDate="+newDate;	
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		String result7 = this.httpProxyGet(httpClientSessionVo,url,"html");
		
		cookie.append(";CoreSessionId=0e40ef31531d151adb6aa4128ea691f79cc6b475cbb392a5;_g_sign=3c39ef439631a4916c4ee65ae61b8941");
		cookie.append(";").append(sessionId).append(fox).append(pv).append(res);
		headerMap.put("Cookie", cookie);
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("Referer",url);
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("condition.dstCityCode",des);//达到地
		paramMap.put("condition.dstDate",newDate);
		paramMap.put("condition.orgDate",depDate);
		paramMap.put("condition.hcType","DC");
		paramMap.put("condition.orgCityCode",dep);//出发地
		String result = this.httpProxyPost(httpClientSessionVo,"http://www.shenzhenair.com/szair_B2C/flightSearch.action",paramMap,"other");
		return result;
	}
	
	//通过正则抓取cookie
		public String getCookieByPattern(String ele,String str){
			 String stri = null;
			 Pattern par = Pattern.compile(ele+" : \"(.*?)\",");
			 Matcher mat = par.matcher(str);
			 while(mat.find()){  
				   stri = mat.group(1);
	         }
			 return stri;
		}
		
		public List<String> getStringList(String start,String end,String str){
			 
		     List<String> results = new ArrayList<String>();
			 Pattern r = Pattern.compile(start);
			 Matcher m = r.matcher(str);
			 int num=0;
			 String strs;
			 int one=0;
			 int two=0;
			 while (m.find( )) {
				 num++;
				  one= str.indexOf(start,num+one);
				  two =str.indexOf(end,num+two);
				  strs=str.substring(one,two);
				  results.add(strs);
		    } 
			 return results;
		}
		//通过正则抓取航班信息
		public String getElementByPattern(String ele,String str){//  "flightNo":"ZH9859",
			 String stri = null;
			 Pattern par = Pattern.compile(ele+"\":\"(.*?)\",");
			 Matcher mat = par.matcher(str);
			 while(mat.find()){  
				   stri = mat.group(1);
	         }
			 return stri;
		}

}
