package com.aft.crawl.crawler.impl.b2c;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.utils.cmh.HttpResponseVo;
import com.aft.utils.cmh.HttpUtil;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.MyStringUtil;
import com.aft.utils.RuoKuai;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 鹏明B2B平台
 * @author chenminghong
 */
public class B2CPMCrawler extends Crawler {
	
	private static String JSESSIONID = "";//"cps_b_170~34C3399FA8C9679F8B28DF7AB9CAEB96";
	private static String CPSBLUSER = "";//"d4aace20c88e9a509115730a0316d8ae";
	private static String loginCookie = "";//登录需要的cookie

	public B2CPMCrawler(String threadMark) {
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
			List<String> valueList = MyStringUtil.getValueList("loadMoreCw", "\\,this", httpResult);
			if(valueList.size()==0) return crawlResults;
			CrawlResultB2C crawlResult = null;
			for(String flight : valueList){
				int index = flight.indexOf(",");
				flight = flight.substring(index+1);
				JSONObject flightObj = JSONObject.parseObject(flight);
				String cabin = flightObj.getString("flight.cabinMinPubTar");
				String depTime = flightObj.getString("flight.depTime");
				String arrTime = flightObj.getString("flight.arrTime");
				String lowPrice = flightObj.getString("flight.lowPrice");
				String airConFee = flightObj.getString("flight.airConFee");
				String fuelTax = flightObj.getString("flight.fuelTax");
				BigDecimal airCon = new BigDecimal(airConFee);
				BigDecimal fueltax = new BigDecimal(fuelTax);
				BigDecimal tax = airCon.add(fueltax);
				String flightNo = flightObj.getString("flight.flightNo");
				String flightEN = flightObj.getString("flight.airways");
				String shareFlight = this.getShareFlight(flightEN);
				String depCity = flightObj.getString("flight.depCity");
				String arrCity = flightObj.getString("flight.arrCity");
				String depDate = flightObj.getString("flight.depDate");
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), flightEN, flightNo, shareFlight, depCity, arrCity, depDate, cabin);
				crawlResult.setDepTime(depTime);	// 出发时间
				crawlResult.setDesTime(arrTime);	// 到达时间
				
				crawlResult.setRemainSite(9);
				crawlResult.setTicketPrice(new BigDecimal(lowPrice));
				crawlResult.setSalePrice(tax);
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
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		String dep = this.getJobDetail().getDepCode();
		String des = this.getJobDetail().getDesCode();
		String depDate = this.getJobDetail().getDepDate();
		String signUrl = "http://www.airpp.net/airsb/main.jsp";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Referer","http://www.airpp.net/airsb/index.jsp");
		headerMap.put("Host", "www.airpp.net");
		httpClientSessionVo.setHeaderMap(headerMap);
		StringBuilder cookie = new StringBuilder();
		cookie.append(loginCookie);
		headerMap.put("Cookie", cookie);
		MyHttpClientResultVo httpVo = null;
//		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
//		headerMap.put("Referer",signUrl);
//		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,"http://www.airpp.net/airsb/main!right.shtml","other");
//		String ck = "home_search_data=%7b%22hclx%22%3a%221%22%2c%22cfcity%22%3a%22"+dep+"%22%2c%22ddcity%22%3a%22"+des+"%22%2c%22cfdate%22%3a%22"+depDate+"%22%2c%22cjrlx%22%3a%221%22%2c%22hkgs%22%3a%22ZH%22%2c%22isXzCwlx%22%3a%22%22%7d";
//		cookie.append(";").append(ck);
//		headerMap.put("Cookie", cookie);
//		headerMap.put("Referer","http://www.airpp.net/airsb/main!right.shtml");
		String searchUrl = "http://www.airpp.net/airsb/ticket/zwyd/search/index.jsp";
//		String res1 = this.httpProxyGet(httpClientSessionVo,searchUrl,"other");
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Referer",searchUrl);
		headerMap.put("Cookie", loginCookie+";home_search_data=");
		String param = "searchMethod=1&hclx=1&cfcity="+dep+"&ddcity="+des+"&cfdate="+depDate+"&zzcitymc2=&zzcity2=&ddcitymc2=&ddcity2=&cfdate2=&avhBean.cjrlx=1&avhBean.hkgs=ZH&avhBean.isXzCwlx=";
		String result = this.httpProxyPost(httpClientSessionVo,"http://www.airpp.net/airsb/ticket/book/search!bookSearch.shtml",param, "other");
		if("302 Moved Temporarily".equals(result)){//重新登录
			Map<String, String> header = new HashMap<String, String>();
			header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			header.put("Accept-Encoding", "gzip, deflate");
			header.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			header.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
			header.put("Upgrade-Insecure-Requests", "1");
			header.put("Connection", "keep-alive");
			header.put("Host", "www.airpp.net");
			HttpResponseVo doGetResponse = HttpUtil.doGetResponse("http://www.airpp.net/airsb/index.jsp", header,null);
			String JSESSIONID = HttpUtil.getHeadValue(doGetResponse.getResponse(), "Set-Cookie","JSESSIONID=");
			StringBuilder cookies = new StringBuilder();
			cookies.append(JSESSIONID);
			
			HttpGet httpGet=new HttpGet("http://www.airpp.net/airsb/common/checkCode.jsp"); //2、创建请求
	        httpGet.setHeader("Host", "www.airpp.net"); 
	        httpGet.setHeader("Accept","*/*"); 
	        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"); 
	        httpGet.setHeader("Accept-Encoding", "gzip, deflate"); 
	        httpGet.setHeader("Cookie",cookies.toString()); 
	        httpGet.setHeader("Referer","http://www.airpp.net/airsb/index.jsp"); 
	        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0"); 
	        CloseableHttpResponse closeableHttpResponse=httpClient.execute(httpGet); //3、执行
	        HttpEntity httpEntity=closeableHttpResponse.getEntity(); //4、获取实体
	        String yzm = "";
	        if(httpEntity!=null){
	            InputStream inputStream=httpEntity.getContent();
	            String code = RuoKuai.getCodeByInputStream(inputStream);
	            yzm =MyStringUtil.getValue("<Result>", "</Result>", code);
	        }
	        
	        header.put("Accept", "*/*");
	        header.put("X-Requested-With","XMLHttpRequest");
	        header.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
	        header.put("Referer","http://www.airpp.net/airsb/index.jsp");
	        header.put("Cookie",cookies.toString()); 
	        header.remove("Upgrade-Insecure-Requests");
	        
	        Map<String, String> loginParam = new HashMap<String, String>();
	        loginParam.put("username", "CAN527ADMIN");
	        loginParam.put("password", "62CE2F811DE6A96D9B3E6D7103445C30");
	        loginParam.put("yzm", yzm);
	        doGetResponse = HttpUtil.doPostResponse("http://www.airpp.net/airsb/logon!logon.shtml", header,loginParam);
	        String CPSBLUSER = HttpUtil.getHeadValue(doGetResponse.getResponse(), "Set-Cookie","CPSBLUSER=");
	        loginCookie = JSESSIONID+";"+CPSBLUSER;
	        System.out.println(loginCookie);
		}
 		return result;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("403")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);

	}
}
