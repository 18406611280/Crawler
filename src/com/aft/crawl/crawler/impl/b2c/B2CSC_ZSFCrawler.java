package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.MyStringUtil;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.regex.MyRegexUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 山东掌上飞航空 
 */
public class B2CSC_ZSFCrawler extends Crawler {
	
	private String dep;
	private String des;
	private String depDate;
	
	public B2CSC_ZSFCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			Elements trigger = document.getElementsByClass("rdo-trigger");
			CrawlResultB2C crawlResult = null;
			for(Element tri : trigger){
				String remark = tri.attr("data-remark");
				if(!"".equals(remark)) {
					continue;
				}
				String depTime = tri.attr("data-departuredatetime").substring(11);
				String desTime = tri.attr("data-arrivaldatetime").substring(11);
				String facePrice = tri.attr("data-price");
				String airlineCode = tri.attr("data-airline");
				String flightNum = airlineCode+tri.attr("data-flightnumber");
				String cabin = tri.attr("data-classcode");
				String tax = tri.attr("data-tax");
				String cabinNum = tri.attr("data-quantity").replace("个", "").trim();
				if(cabinNum.contains("充足")) cabinNum = "9";
				String shareFlight = this.getShareFlight(airlineCode);
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, flightNum, shareFlight, dep, des, depDate, cabin);
				
				crawlResult.setDepTime(depTime);	// 出发时间
				crawlResult.setDesTime(desTime);	// 到达时间
				
				// 剩余座位数
				crawlResult.setRemainSite(Integer.valueOf(cabinNum));
				
				// 价格
				BigDecimal ticketPrice = new BigDecimal(facePrice);
				crawlResult.setTicketPrice(ticketPrice);
				crawlResult.setSalePrice(new BigDecimal(tax));
				crawlResult.setType("掌上飞");
				crawlResults.add(crawlResult);
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
		if (httpResult!=null &&httpResult.contains("You don't have permission to access")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);
	}

	@Override
	public String httpResult() throws Exception {
		
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
		String Url = "http://sc.travelsky.com/scet/airRouteInfo.do";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "*/*");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Referer","http://www.sda.cn/index.html");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "sc.travelsky.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,Url,"other");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","JSESSIONID=");
		String Webtrends = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","Webtrends=");
		String queryUrl = "http://sc.travelsky.com/scet/queryAv.do?lan=cn";
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		StringBuilder cookie = new StringBuilder();
		cookie.append(JSESSIONID).append(";").append(Webtrends);
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Cookie",cookie);
		String param = "countrytype=0&travelType=0&cityCodeOrg="+dep+"&cityCodeDes="+des+"&takeoffDate="+depDate+"&returnDate="+depDate+"&cabinStage=0&adultNum=1&childNum=0";
		String queryResult = this.httpProxyPost(httpClientSessionVo,queryUrl,param,"other");
		
		String airId = MyStringUtil.getValue("\"airAvailId\" value\\=\"", "\"", queryResult);
		String airUrl = "http://sc.travelsky.com/scet/airAvail.do";
		String param2 = "airAvailId="+airId+"&cityCodeOrg="+dep+"&cityCodeDes="+des+"&takeoffDate="+depDate+"&returnDate="+depDate+"&travelType=0&countrytype=0&needRT=0&cabinStage=0&adultNum=1&childNum=0";
		headerMap.put("Referer","http://sc.travelsky.com/scet/queryAv.do?lan=cn");
		String result = this.httpProxyPost(httpClientSessionVo,airUrl,param2,"other");
		return result;
		
		
	}
}