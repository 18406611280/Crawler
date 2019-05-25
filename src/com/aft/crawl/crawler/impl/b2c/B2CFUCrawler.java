package com.aft.crawl.crawler.impl.b2c;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.MyStringUtil;
import com.aft.utils.RuoKuai;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 成都航空 
 * @author chenminghong
 */
public class B2CFUCrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	
	public B2CFUCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			if("".equals(httpResult)){
				return crawlResults;
			}
			if(!httpResult.contains("{\"sta\":\"10000\"}")){
				Object parse = JSONObject.parse(httpResult);
				CrawlResultB2C crawlResult = new CrawlResultB2C(this.getJobDetail(), "", "", "", dep, des, depDate, "ddd");
				crawlResults.add(crawlResult);
			}
//			JSONObject resultObj = JSONObject.parseObject(httpResult);
//			JSONObject searchResult = resultObj.getJSONObject("FlightSearchResults");
//			if(null == searchResult) {
//				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
//				return crawlResults;
//			}
//			JSONArray Flights = searchResult.getJSONArray("Flights");
//		    if(null == Flights) {
//				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
//				return crawlResults;
//			}
//			
//			CrawlResultB2C crawlResult = null;
//
//			JSONObject Flight0 = Flights.getJSONObject(0);
//			JSONArray flights = Flight0.getJSONArray("Flight");
//			int flightSize = flights.size();
//			for(int i =0; i<flightSize; i++){
//				JSONObject flight = flights.getJSONObject(i);
//				JSONObject FlightDetail = flight.getJSONArray("FlightDetails").getJSONObject(0);
//				JSONObject FlightLeg = FlightDetail.getJSONArray("FlightLeg").getJSONObject(0);
//				String airlineCode = FlightLeg.getString("OperatingAirline");//EU
//				String FlightNumber = FlightLeg.getString("FlightNumber");//6665
//				String shareFlight = this.getShareFlight(airlineCode);
//				String flightNo = airlineCode+FlightNumber;
//				JSONObject Departure = FlightLeg.getJSONObject("Departure");
//				String depTime = Departure.getString("Time").substring(0, 5);
//				JSONObject Arrival = FlightLeg.getJSONObject("Arrival");
//				String desTime = Arrival.getString("Time").substring(0, 5);
//				JSONObject Price = flight.getJSONObject("Price");
//				JSONArray FareInfos = Price.getJSONArray("FareInfos");
//				int fareSize = FareInfos.size();
//				for(int j=0; j<fareSize; j++){
//					JSONObject info = FareInfos.getJSONObject(j);
//					JSONArray FareInfo = info.getJSONArray("FareInfo");
//					if(FareInfo==null)continue;
//					JSONObject FareInfo0 = FareInfo.getJSONObject(0);
//					String cabin = FareInfo0.getJSONObject("FareReference").getString("ResBookDesigCode");
//					JSONObject Fare = FareInfo0.getJSONArray("FareInfo").getJSONObject(0).getJSONObject("Fare");
//					String BaseAmount = Fare.getString("BaseAmount");
//					String TaxAmount = Fare.getString("TaxAmount");
//					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, flightNo, shareFlight, dep, des, depDate, cabin);
//					crawlResult.setDepTime(depTime);	// 出发时间
//					crawlResult.setDesTime(desTime);	// 到达时间
//					
//					crawlResult.setRemainSite(9);
//					crawlResult.setTicketPrice(new BigDecimal(BaseAmount));
//					crawlResult.setSalePrice(new BigDecimal(TaxAmount));
//					crawlResults.add(crawlResult);
//				}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
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
		String signUrl = "http://www.fuzhou-air.cn/";

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.fuzhou-air.cn");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String searchUrl = "http://www.fuzhou-air.cn/b2c/static/flightSearch.html?orgCityCode="+dep+"&dstCityCode="+des+"&orgDate="+depDate+"&dstDate=&adult=1&child=0&infant=0&trip=ONEWAY";
		String searchCookie = "FU_historyOfSearch=%7B%22orgCityCode%22%3A%22"+dep+"%22%2C%22dstCityCode%22%3A%22"+des+"%22%2C%22orgDate%22%3A%22"+depDate+"%22%2C%22dstDate%22%3A%22%22%2C%22adult%22%3A%221%22%2C%22child%22%3A%220%22%2C%22infant%22%3A%220%22%2C%22trip%22%3A%22ONEWAY%22%7D";
		StringBuilder cookies = new StringBuilder();
		cookies.append(JSESSIONID).append(";").append(searchCookie).append(";notice-read-ETweb=false").append(";popIndex=1");
		headerMap.put("Cookie", cookies);
		headerMap.put("Referer",signUrl);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,searchUrl,"other");
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Content-Type","application/text; charset=utf-8");
		headerMap.put("Referer",searchUrl);
		headerMap.remove("Upgrade-Insecure-Requests");   
		String httpResult = "";
		httpResult = this.httpProxyPost(httpClientSessionVo,"http://www.fuzhou-air.cn/frontend/api/flight.action", "{\"orgCity\":\""+dep+"\",\"dstCity\":\""+des+"\",\"flightdate\":\""+depDate+"\",\"index\":\"0\",\"tripType\":\"ONEWAY\",\"times\":\"3498306709\",\"desc\":\"coBPtm4BZy5Ly7E1arnlj82vNZvCXom2Oa66lxnGMsKSK3htzLUUBjZHlu5VIIm%2B\"}", "json");
		if(httpResult.contains("{\"sta\":\"10000\"}") || httpResult.contains("{\"sta\":\"10001\"}")){
			super.changeProxy();
			headerMap.put("Accept", "*/*");
			headerMap.remove("X-Requested-With");
			headerMap.remove("Content-Type");
			
			HttpGet httpGet=new HttpGet("http://www.fuzhou-air.cn/hnatravel/imagecodefu?code=0.25077401563615875"); //2、创建请求
	        httpGet.setHeader("Host", "www.fuzhou-air.cn"); 
	        httpGet.setHeader("Accept","*/*"); 
	        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"); 
	        httpGet.setHeader("Accept-Encoding", "gzip, deflate"); 
	        httpGet.setHeader("Cookie",cookies.toString()); 
	        httpGet.setHeader("Referer",searchUrl); 
	        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0"); 
	        CloseableHttpResponse closeableHttpResponse=httpClient.execute(httpGet); //3、执行
	        Header[] headers = closeableHttpResponse.getAllHeaders();
	        String ci = MyHttpClientUtil.getHeaderValue(headers, "Set-Cookie", "ci=");
	        HttpEntity httpEntity=closeableHttpResponse.getEntity(); //4、获取实体
	        String yzm = "";
	        if(httpEntity!=null){
	            InputStream inputStream=httpEntity.getContent();
	            String code = RuoKuai.getCodeByInputStream(inputStream);
	            yzm =MyStringUtil.getValue("<Result>", "</Result>", code);
	        }
	        cookies.append(";").append(ci);
	        headerMap.put("Cookie", cookies);
	        String param2 = "{\"orgCity\":\""+dep+"\",\"dstCity\":\""+des+"\",\"flightdate\":\""+depDate+"\",\"index\":\"0\",\"tripType\":\"ONEWAY\",\"times\":\"3498306709\",\"desc\":\"FsODuR1dkdOqCZ7WVBRLLg9JFKBaaOr2g9S8NQ%2ByFAVOxNa5J9yH/veDN%2Bpk8wMetk5qL5kcTaMpyyAlzkBattfhRjYuZZMw3XrpLXIKAkT%2BZ912WMlhBkkESvXnc2Gj2PxTmMpFY/GemEn3PiNH8PPr5emGtxOwjUb8fBw221yWvSCW%2BjZcGf47aAsutSE/M2KogME49J/Z4H36FAAjVDNKQN3YT/imZtF98L%2B8JaSLCN2tFvxuYybxjxjCU6sloAJZjBUD77ge4JsDjdJdVsgXI96kqo8b6Ikhh2vGXzqIwnKP/Z3XBKy8Ogib5zcmkcKUZpq70BWAzQU8fy38fA==\",\"vc\":\""+yzm+"\"}";
	        httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"http://www.fuzhou-air.cn/frontend/api/flight.action",param2 , "json");
	        String pa = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "pa=");
	        cookies.append(";").append(pa);
	        headerMap.put("Cookie", cookies);
	        String param3 = "{\"orgCity\":\""+dep+"\",\"dstCity\":\""+des+"\",\"flightdate\":\""+depDate+"\",\"index\":\"0\",\"tripType\":\"ONEWAY\",\"times\":\"3498306709\",\"desc\":\"FsODuR1dkdOqCZ7WVBRLLg9JFKBaaOr2g9S8NQ%2ByFAVOxNa5J9yH/veDN%2Bpk8wMetk5qL5kcTaMpyyAlzkBattfhRjYuZZMw3XrpLXIKAkT%2BZ912WMlhBkkESvXnc2Gj2PxTmMpFY/GemEn3PiNH8PPr5emGtxOwjUb8fBw221yWvSCW%2BjZcGf47aAsutSE/M2KogME49J/Z4H36FAAjVDNKQN3YT/imZtF98L%2B8JaSLCN2tFvxuYybxjxjCU6sloAJZjBUD77ge4JsDjdJdVsgXI96kqo8b6Ikhh2vGXzqIwnKP/Z3XBKy8Ogib5zcmkcKUZpq70BWAzQU8fy38fA==\"}";
	        httpResult = this.httpProxyPost(httpClientSessionVo,"http://www.fuzhou-air.cn/frontend/api/flight.action",param3 , "json");
			if(httpResult.contains("{\"sta\":\"10001\"}")){
				httpResult = "";
				super.changeProxy();
			}
		}
		
		//=============================================================
//		String signUrl = "http://www.fuzhou-air.cn/b2c/static/flightSearch.html?orgCityCode=FOC&dstCityCode=XIY&orgDate=2018-12-28&dstDate=&adult=1&child=0&infant=0&trip=ONEWAY";
//		
//		Map<String, Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		headerMap.put("Accept-Encoding", "gzip, deflate");
//		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//		headerMap.put("User-Agent","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0_1 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A402 Safari/604.1");
//		headerMap.put("Upgrade-Insecure-Requests", "1");
//		headerMap.put("Connection", "keep-alive");
//		headerMap.put("Host", "www.fuzhou-air.cn");
//		httpClientSessionVo.setHeaderMap(headerMap);
//		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
//		
//		headerMap.put("Accept", "*/*");
//		headerMap.put("Referer", signUrl);
//		headerMap.remove("Upgrade-Insecure-Requests");
//		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,"http://www.fuzhou-air.cn/themes/fub2c/script/page/flightSearch.js","other");
//		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
//		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
//		headerMap.put("X-Requested-With","XMLHttpRequest");
//		headerMap.put("Content-Type","application/text; charset=utf-8");
//		headerMap.put("Cookie",JSESSIONID);
		return httpResult;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("您所请求的网址（URL）无法获取")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);

	}
}