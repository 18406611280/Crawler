package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import scala.collection.mutable.StringBuilder;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 厦门航空 
 * @author chenminghong
 */
public class B2CMFCrawler extends Crawler {

	private final static String flightUrl = "http://et.xiamenair.com/xiamenair/book/findFlights.action?tripType=0&queryFlightInfo=%depCode%,%desCode%,%depDate%";
	
	private final static String queryUrl = "http://et.xiamenair.com/xiamenair/book/findFlights.json?lang=zh&tripType=0&r=%random%&takeoffDate=%takeoffDate%&returnDate=&orgCity=%orgCity%&dstCity=%dstCity%";
	
	public B2CMFCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object objMsg = mapResult.get("resultCode");
			if(!"00".equals(objMsg)) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
			List<Map<String, Object>> flights = (List<Map<String, Object>>)mapResult.get("flightInfos1");
			for(Map<String, Object> flightMap : flights) {
				// 航班号,航司
				String airlineCode = flightMap.get("airline").toString().trim().toUpperCase();
				String fltNo = airlineCode + flightMap.get("fltNo").toString().trim();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				Object codeShare = flightMap.get("codeShare");
				if(null != codeShare && Boolean.valueOf(codeShare.toString())) shareFlight = "Y";
				
				// 出发时间
				String depTime = flightMap.get("takeoffTime").toString().trim().substring(11);
				
				// 到达时间
				String desTime = flightMap.get("arrivalTime").toString().trim().substring(11);
				
				// 出发,到达
				String depCode = flightMap.get("org").toString().trim().toUpperCase();
				String desCode = flightMap.get("dst").toString().trim().toUpperCase();
				
				// 出发日期
				String depDate = flightMap.get("takeoffTime").toString().trim().substring(0, 10);
				
				Map<String, Object> brandMap = (Map<String, Object>)flightMap.get("cBrand");
//				if(null == brandMap) brandMap = (Map<String, Object>)flightMap.get("fBrand");
				if(null == brandMap) brandMap = (Map<String, Object>)flightMap.get("yBrand");
				if(null == brandMap) continue;
				
				String cabin = brandMap.get("cabin").toString().trim().toUpperCase();
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
				crawlResult.setDepTime(depTime);	// 出发时间
				crawlResult.setDesTime(desTime);	// 到达时间
				
				// 剩余座位数
				String remainSite = brandMap.get("seats").toString().trim();
				crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(1 == remainSite.length() ? remainSite : remainSite.substring(1)));
				
				// 价格
				crawlResult.setTicketPrice(new BigDecimal(brandMap.get("price").toString().trim()).setScale(0));
				crawlResult.setSalePrice(crawlResult.getTicketPrice());
				crawlResults.add(crawlResult);
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
		
		String dep = this.getJobDetail().getDepCode();
		String des = this.getJobDetail().getDesCode();
		String depDate = this.getJobDetail().getDepDate();
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"); 
 		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.xiamenair.com");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		httpClientSessionVo.setHeaderMap(headerMap);
		
//      第一次访问
		String res = this.httpProxyGet(httpClientSessionVo, "https://www.xiamenair.com/zh-cn/","html");
 		
 		headerMap.put("Referer", "https://www.xiamenair.com/zh-cn/");
 		headerMap.put("Host", "et.xiamenair.com");
 		String findUrl = "https://et.xiamenair.com/xiamenair/book/findFlights.action?lang=zh&tripType=0&queryFlightInfo="+dep+","+des+","+depDate;
 		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,findUrl,"html");
 		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","JSESSIONID=");
 		String Webtrends = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","Webtrends=");
 		String XLB = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","X-LB=");
 		String findResult = httpVo.getHttpResult();
 		String random1 = getRandom(findResult);
 		
		Calendar ca = Calendar.getInstance();
		Date tDate = ca.getTime(); 
		ca.add(Calendar.SECOND, -20);
		Date eDate = ca.getTime();
		
		StringBuilder cookie = new StringBuilder()
		.append(JSESSIONID).append(";")
		.append(Webtrends).append(";")
		.append(XLB).append(";WT_FPC=id=").append("24b01bacb6be7acb5d0"+tDate.getTime())
		.append(":").append("lv=").append(tDate.getTime()).append(":")
		.append("ss=").append(tDate.getTime());
		headerMap.put("Cookie",cookie);
        String loadUrl = "https://et.xiamenair.com/xiamenair/city/loadCity.json?type=0&_="+eDate.getTime();
        headerMap.put("Accept","application/json, text/javascript, */*; q=0.01");
        headerMap.put("X-Requested-With","XMLHttpRequest");
        headerMap.remove("Upgrade-Insecure-Requests");
        headerMap.put("Referer",findUrl);
        httpVo = this.httpProxyResultVoGet(httpClientSessionVo,loadUrl,"json");
        String url = "https://et.xiamenair.com/xiamenair/book/findFlights.json?r="+random1+"&lang=zh&takeoffDate="+depDate+"&returnDate=&orgCity="+dep+"&dstCity="+des+"&tripType=0&channelId=1&_="+tDate.getTime();
        headerMap.put("Accept","*/*");
        String result = this.httpProxyGet(httpClientSessionVo,url,"json");
     	return result;
		}
		
	
	public String getRandom(String str){
		 String stri = null;
		 Pattern par = Pattern.compile("random\" type=\"hidden\" value=\"(.*?)\"");
		 Matcher mat = par.matcher(str);
		 while(mat.find()){  
			   stri = mat.group(1);
        }
		 return stri;
	}
}