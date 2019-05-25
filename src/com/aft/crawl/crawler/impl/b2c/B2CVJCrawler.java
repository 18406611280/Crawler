package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;

/**
 * 越捷航空Web官网
 */
public class B2CVJCrawler extends Crawler {
															
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	public B2CVJCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		
		String httpResult =this.httpResult();
		
//		System.out.println(httpResult);
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			Document document = Jsoup.parse(httpResult);
			Elements eleLis = document.getElementsByClass("FlightsGrid");
			if(null == eleLis || eleLis.isEmpty() || eleLis.size()==0) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			crawlResults = this.owFlight(httpResult,crawlResults,eleLis.get(0));
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}
	
	/**
	 * 直达航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	private List<CrawlResultBase> owFlight(String httpResult,List<CrawlResultBase> crawlResults,Element element) {
		try {
			SimpleDateFormat SDF_DMY = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
			Elements gridTravelOptDeps =  element.getElementsByAttributeValueMatching("id", Pattern.compile("gridTravelOptDep\\d+"));
			if(gridTravelOptDeps == null || gridTravelOptDeps.isEmpty() || gridTravelOptDeps.size()==0){
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			CrawlResultB2C b2c =  null;
			for(Element travel : gridTravelOptDeps){
				Elements travelTds = travel.select(">td");
				Elements gridFlightEventds0 = travelTds.get(0).select(">table >tbody > tr > td");
				
				String depDate = gridFlightEventds0.get(0).text().substring(0, 10);
				depDate = SDF_YMD.format(SDF_DMY.parse(depDate));
				String depTime = gridFlightEventds0.get(1).text().substring(0, 5);
				String depCode = gridFlightEventds0.get(1).text().substring(6, 9);
				String desTime = gridFlightEventds0.get(2).text().substring(0, 5);
				String desCode = gridFlightEventds0.get(2).text().substring(6, 9);
				String desDate = this.getJobDetail().getDepDate();
				int dhour = Integer.valueOf(depTime.substring(0, 2));
				int ahour = Integer.valueOf(desTime.substring(0, 2));
				if(dhour>ahour){
					Date date = SDF_YMD.parse(this.getJobDetail().getDepDate());
					Calendar ca = Calendar.getInstance();
					ca.setTime(date);
					ca.add(Calendar.DAY_OF_MONTH, 1);
					date = ca.getTime();
					desDate = SDF_YMD.format(date);
				}
				String fltNo = gridFlightEventds0.get(3).select(">span").text();
				String airlineCode = fltNo.substring(0,2);
				String shareAir = "N";
				if(!"VJ".equals(airlineCode))shareAir = "Y";
				Elements gridFlightEventds1 = travelTds.get(1).select(">table >tbody > tr > td");
				
				for(Element gridFlightEventd : gridFlightEventds1){
					Elements fareinputs = gridFlightEventd.select(">input");
					if(fareinputs==null || fareinputs.isEmpty() || fareinputs.size()==0)continue;
					
					String cabin = gridFlightEventd.attr("data-familyid");
					cabin = cabin.substring(0,1);
					String fare = gridFlightEventd.select(">input#fare").val().replace("CNY", "").replaceAll(",", "").trim();
					String faretaxes = gridFlightEventd.select(">input#fare_taxes").val().replace("CNY","").replaceAll(",", "").trim();
					String charges = gridFlightEventd.select(">input#charges").val().replace("CNY","").replaceAll(",", "").trim();
					
					BigDecimal taxone = new BigDecimal(charges).divide(new BigDecimal(3)).setScale(2, BigDecimal.ROUND_HALF_UP);
					
					b2c = new CrawlResultB2C(this.getJobDetail(),airlineCode, fltNo, shareAir, depCode,desCode,depDate, cabin);
					b2c.setDepTime(depTime);
					b2c.setDesTime(desTime);
					b2c.setEndDate(desDate);
					BigDecimal ticketPrice = new BigDecimal(fare).setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal salePrice = new BigDecimal(faretaxes).add(taxone).setScale(2, BigDecimal.ROUND_HALF_UP);
					b2c.setTicketPrice(ticketPrice);
					b2c.setSalePrice(salePrice);
					b2c.setRemainSite(2);
					crawlResults.add(b2c);
				}
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
		}
		return crawlResults;
	}
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document.title().contains("404 Page Not Found") || document.title().contains("403 - Forbidden: Access is denied")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	@Override
	public String httpResult() throws Exception {
		
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		String depDate = this.getJobDetail().getDepDate();
		depDate = depDate.replaceAll("-", "/");
		
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("lstOrigAP", this.getJobDetail().getDepCode());
		paramMap.put("lstDestAP", this.getJobDetail().getDesCode());
		paramMap.put("dlstDepDate_Day", depDate.substring(8, 10));
		paramMap.put("dlstDepDate_Month", depDate.substring(0, 7));
//		paramMap.put("dlstRetDate_Day", "18");
//		paramMap.put("dlstRetDate_Month", "2017/01");
		paramMap.put("lstCurrency", "CNY");
		paramMap.put("lstResCurrency", "CNY");
		paramMap.put("lstDepDateRange", "0");
		paramMap.put("lstRetDateRange", "0");
		paramMap.put("txtNumAdults", "3");
		paramMap.put("txtNumChildren", "0");
		paramMap.put("txtNumInfants", "0");
		paramMap.put("lstLvlService", "1");
		paramMap.put("blnFares", "False");
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Cache-Control", "max-age=0");
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer", "http://www.vietjetair.com/Sites/Web/zh-CN/Home");
		headerMap.put("Origin", "http://www.vietjetair.com");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "book.vietjetair.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String result =  this.httpProxyPost(httpClientSessionVo, "https://book.vietjetair.com/ameliapost.aspx?lang=zh", paramMap, "html");
//		System.out.println(result);
		
		Document document = Jsoup.parse(result);
		
		String __VIEWSTATE = document.getElementById("__VIEWSTATE").val();
		String __VIEWSTATEGENERATOR = document.getElementById("__VIEWSTATEGENERATOR").val();
		String DebugID = document.getElementById("DebugID").val();
		
		paramMap.put("__VIEWSTATE", __VIEWSTATE);
		paramMap.put("__VIEWSTATEGENERATOR",__VIEWSTATEGENERATOR);
		paramMap.put("DebugID", DebugID);
		
		headerMap.put("Referer", "https://book.vietjetair.com/ameliapost.aspx?lang=zh");
		headerMap.put("Origin", "https://book.vietjetair.com");
		
		result =  this.httpProxyPost(httpClientSessionVo, "https://book.vietjetair.com/ameliapost.aspx?lang=zh", paramMap, "html");
		
//		System.out.println(result);
		
		result =  this.httpProxyPost(httpClientSessionVo, "https://book.vietjetair.com//TravelOptions.aspx?lang=zh&st=pb&sesid=", paramMap, "html");
		return result;
	}
	
	public static void main(String[] args) {
		
	}
}