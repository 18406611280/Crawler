package com.aft.crawl.crawler.impl.b2c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.DESUtil;
import com.aft.utils.MyStringUtil;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;

/**
 * 青岛航空 
 * @author chenminghong
 */
public class B2CQWCrawler extends Crawler {

	String dep;
	String des;
	String depDate;
	public B2CQWCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
//		String httpResult = StringTxtUtil.TxtToString("D:\\QD.txt");
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if("".equals(httpResult) || httpResult==null){
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			Document document = Jsoup.parse(httpResult);
			Element contentDiv = document.getElementsByClass("content").first();
			if(null == contentDiv) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
            Elements container = contentDiv.select("> div");
			for(Element eleItem : container) {
				Element eleFltNo = eleItem.select(" > div > div.detail > div.crumb > div").first();
				String crumb = eleFltNo.ownText();
				String flightNo = MyStringUtil.getValue("青岛航空 \\| ", " \\| ", crumb).trim();
				String airlineCode = flightNo.substring(0, 2);
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				// 出发时间-到达时间
				Elements timeDivs = eleItem.select(" > div > div.detail > div.route > div.time");
				Element depTimeDiv = timeDivs.get(0).select("> div").first();
				String depTime = depTimeDiv.ownText().trim();
				Element desTimeDiv = timeDivs.get(1).select("> div").first();
				String desTime = desTimeDiv.ownText().trim();
				
				Elements priceDetails = eleItem.select("div.priceDetail.clearfix");
				for(Element priceDetail : priceDetails) {
					Element cabinDiv = priceDetail.select(">div >div >div >div ").first();
					String memo = cabinDiv.ownText();
					String cabin = cabinDiv.select(">span").first().ownText().substring(1, 2);
					Element cabinNumD = priceDetail.select("div.surplus").first();
					String cabinNum = cabinNumD.ownText().replaceAll("仅剩","").replaceAll("张", "").trim();
					if(cabinNum.contains("充足")){
						cabinNum = "9";
					}
					Element buyTicketBox = priceDetail.select("div.buyTicketBox").first();
					String ticketPrice = buyTicketBox.select("> div > div").first().ownText().trim();
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, flightNo, shareFlight, dep, des, depDate, cabin);
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					
					crawlResult.setRemainSite(Integer.parseInt(cabinNum));
					crawlResult.setTicketPrice(new BigDecimal(ticketPrice));
					crawlResult.setSalePrice(crawlResult.getTicketPrice());
					crawlResult.setType(memo);
					crawlResults.add(crawlResult);
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType)
			throws Exception {
		if(httpResult.contains("您的IP被拦截") || httpResult.contains("\\<input type=\"text\" id\\=\"fromcity\" value\\=\"null\""))return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	@Override
	public String httpResult() throws Exception {
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		String signUrl = "http://www.qdairlines.com/";
//		String signUrl = "http://www.qdairlines.com/reservation/search.do";
		boolean flag = true;
		String result = "";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.9");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.92 Safari/537.36");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.qdairlines.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
		String res1 = httpVo.getHttpResult();
		System.out.println(res1);
		res1 = res1.replaceAll("\r|\n*","");
		String form = MyStringUtil.getValue("\\<form name\\=\"AirQueryForm\"", "\\<\\/form\\>", res1);
		String random = MyStringUtil.getValue("text\\/javascript\" src\\=\"\\/js\\/home.js\\?random\\=","\"", res1);
		String randomUrL = "http://www.qdairlines.com/js/index_new.js?random="+random;
		String aaa = MyStringUtil.getValue("\\<input type\\=\"hidden\" value\\=\"W", "\" id\\=\"aaa", form);
		aaa="W"+aaa;
		String TOKEN = MyStringUtil.getValue("name=\"org.apache.struts.taglib.html.TOKEN\" value=\"", "\"", form);
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		
		headerMap.put("Accept", "*/*");
		headerMap.put("Cookie",JSESSIONID);
		headerMap.put("Referer","http://www.qdairlines.com/");
		headerMap.remove("Upgrade-Insecure-Requests");
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,randomUrL,"other");
		String res2 = httpVo.getHttpResult();
		String remindId = MyStringUtil.getValue("remindId\',\'", "\'", res2);
		String UM_distinctid= UUID.randomUUID().toString();//169285fcaca793-027dafee45322d-661f1574-1fa400-169285fcacb15b
		String cookie1 = JSESSIONID+";remindId="+remindId;//+";UM_distinctid="+UM_distinctid;
		headerMap.put("Cookie",cookie1);
		headerMap.put("Accept", "text/plain, */*; q=0.01");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Origin","http://www.qdairlines.com");
		headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		Map<String, Object> param3 = new HashMap<String, Object>();
		param3.put("param", "{\"opration\":\"midata\",\"val\":\"TAO\"}");
		String abc = this.httpProxyPost(httpClientSessionVo,"http://www.qdairlines.com/getContentBaseData.do",param3,"other");
		abc = this.httpProxyPost(httpClientSessionVo,"http://www.qdairlines.com/getContentBaseData.do",param3,"other");
		Map<String, Object> param4 = new HashMap<String, Object>();
		param4.put("param", "{\"opration\":\"midata\",\"val\":\"CSX\"}");
		String def = this.httpProxyPost(httpClientSessionVo,"http://www.qdairlines.com/getContentBaseData.do",param4,"other");
		def = this.httpProxyPost(httpClientSessionVo,"http://www.qdairlines.com/getContentBaseData.do",param4,"other");
		
//		String form = MyStringUtil.getValue("\\<form id\\=\"reSearch_form\" name=\"AirQueryForm\"", "\\<\\/form\\>", res1);
	    List<String> keyList = MyStringUtil.getValueList("<input type=\"hidden\"  name\\=\"", "\"", form);
	    List<String> keyVaule = new ArrayList<String>(); 
	    if(keyList.size()>0){
	    	for(String key :keyList){
	    		String value = MyStringUtil.getValue("\\<input type\\=\"hidden\"  name\\=\""+key+"\" value\\=\"", "\"", form);
	    		keyVaule.add(key+"|"+value);
	    	}
	    }
			headerMap.put("Cookie",cookie1);
			headerMap.put("Cache-Control","max-age=0");
			headerMap.put("X-Requested-With", "XMLHttpRequest");
			headerMap.put("Referer", signUrl);
			headerMap.put("Origin", "http://www.qdairlines.com");
			headerMap.put("Content-Type","application/x-www-form-urlencoded");
			headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			headerMap.remove("X-Requested-With");
			headerMap.put("Upgrade-Insecure-Requests", "1");
			String param = "aaa="+aaa+"&adt=1&chd=0&journeyType=OW&orgCity=TAO&destCity=CSX&retDate=&depDate=2019-02-28&secueCode=&specialTicketId=&abc="+abc+"&def="+def+"&org.apache.struts.taglib.html.TOKEN="+TOKEN;
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("aaa", aaa);
			paramMap.put("adt", "1");
			paramMap.put("chd", "0");
			paramMap.put("journeyType", "OW");
			paramMap.put("orgCity","TAO");
//			paramMap.put("destCityInput","长沙");
//			paramMap.put("orgCityInput","青岛");
			paramMap.put("destCity","CSX");
			paramMap.put("depDate", "2019-03-05");
			paramMap.put("retDate","");
			paramMap.put("secueCode","");
			paramMap.put("specialTicketId","");
			paramMap.put("abc",abc);
			paramMap.put("def",def);
			paramMap.put("org.apache.struts.taglib.html.TOKEN",TOKEN);
			if(keyList.size()>0){
				for (String kv : keyVaule) { 
					String[] KV = kv.split("\\|");
					paramMap.put(KV[0],KV[1]);
					param = param+"&"+KV[0]+"="+KV[1];
				}
			}
			result = this.httpProxyPost(httpClientSessionVo,"http://www.qdairlines.com/FlightSearch.do",paramMap,"other");
			writeFile(result);
			System.out.println(result);
			super.changeProxy();
		return result;
	}
	
	public void writeFile(String str) {
		try {

			   File file = new File("D:\\filename.txt");
			   if (!file.exists()) {
			    file.createNewFile();
			   }
			   FileWriter fw = new FileWriter(file.getAbsoluteFile());
			   BufferedWriter bw = new BufferedWriter(fw);
			   bw.write(str);
			   bw.close();
			   System.out.println("Done");
			  } catch (IOException e) {
			   e.printStackTrace();
			  }
	}
}