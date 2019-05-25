package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;
import scala.collection.mutable.StringBuilder;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.MyStringUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;

/**
 * 乐桃航空Web官网
 * @author chenminghong
 */
public class B2CMMCrawler extends Crawler {
															
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	private final String initCurrency = "CNY-CNY;HK$-HKD;NT$-TWD;￥-JPY;THB-THB;₩-KRW";
	
	public B2CMMCrawler(String threadMark) {
		super(threadMark);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		String httpResult =this.httpResult();
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult) || !httpResult.contains("var flightResults")) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			crawlResults = this.owFlight(crawlResults,httpResult);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}
	
	private List<CrawlResultBase> owFlight(List<CrawlResultBase> crawlResults,String httpResult) {
		
		String flightResults = MyStringUtil.getValue("var flightResults =", ";", httpResult).trim();
		String symbol = MyStringUtil.getValue("RiotControl\\.trigger\\(\'initCurrency\'\\, \'", "\'\\);", httpResult).trim();
		String currency =null;//币种
		if(symbol!=null && !"".equals(symbol)){
			logger.info("获取到币种符号："+symbol);
			String[] currs = initCurrency.split(";");
			for(String curr : currs){
				String[] cr = curr.split("-");
				if(cr[0].equals(symbol)){
					currency = cr[1];
					break;
				}
			} 
		}
		if(flightResults==null){
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		JSONArray array = JSONArray.fromObject(flightResults).getJSONArray(0);
		int size = array.size();
		for(int i=0;i<size;i++){
			JSONObject flightResult= array.getJSONObject(i);
			String flightNumber = flightResult.getString("flightNumber");
			String airlineCode = flightNumber.substring(0, 2);
			String dep = flightResult.getString("originCode");
			String des = flightResult.getString("destinationCode");
			String departureTime = flightResult.getString("departureTime");
			String arrivalTime = flightResult.getString("arrivalTime");
			departureTime = departureTime.replaceAll("/", "-");
			arrivalTime = arrivalTime.replaceAll("/", "-");
			String depDate = departureTime.substring(0, 10);
			String depTime = departureTime.substring(11, 16);
			String desDate = arrivalTime.substring(0, 10);
			String desTime = arrivalTime.substring(11, 16);
			String taxAdult = flightResult.getString("taxAdult");//税费
			JSONObject fares = flightResult.getJSONObject("fares");
			List<JSONObject> jsonList = new ArrayList<JSONObject>();
			if(fares.toString().contains("happy")){
				JSONObject happy = fares.getJSONObject("happy");
				jsonList.add(happy);
			}
			else if(fares.toString().contains("happlus")){
				JSONObject happlus = fares.getJSONObject("happlus");
				jsonList.add(happlus);
			}
			else if(fares.toString().contains("prime")){
				JSONObject prime = fares.getJSONObject("prime");
				jsonList.add(prime);
			}
			for(JSONObject jsonObject:jsonList){
				String farePrice = jsonObject.getString("fare");
				String cabin = jsonObject.getString("bookingClass");
				String seat = jsonObject.getString("seat").trim();
				CrawlResultB2C b2c =  null;
				b2c = new CrawlResultB2C(this.getJobDetail(),airlineCode, flightNumber, "N", dep,des,depDate,cabin);
				b2c.setDepTime(depTime);
				b2c.setDesTime(desTime);
				b2c.setEndDate(desDate);
				
				b2c.setRemainSite(Integer.valueOf(seat));
				BigDecimal ticketPrice = new BigDecimal(farePrice).setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal salePrice = new BigDecimal(taxAdult).setScale(2, BigDecimal.ROUND_HALF_UP);
				
				if(currency!=null && !"".equals(currency)){
					if(!currency.equals("CNY")){
						BigDecimal rate = CurrencyUtil.getRequest3(currency, "CNY");
						if(rate.compareTo(BigDecimal.ZERO)==0){
							b2c.setType("此价格的币种类型："+ currency);
						}else{
							ticketPrice = ticketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
							salePrice = salePrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
							b2c.setType("币种转换:"+ currency+"—>CNY");
						}
					}
				}else{
					b2c.setType("此价格的币种类型："+ symbol);
				}
				b2c.setTicketPrice(ticketPrice);
				b2c.setSalePrice(salePrice);
				crawlResults.add(b2c);
			}
			
		}
		return crawlResults;
	}

	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("Unable to determine IP address from host name") ||httpResult.contains("Your IP address")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	
	@Override
	public String httpResult() throws Exception {
		
		String dep = this.getJobDetail().getDepCode();
		String des = this.getJobDetail().getDesCode();
		
		SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat SDF_DMY = new SimpleDateFormat("yyyy/MM/dd");
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		String DepDate = this.getJobDetail().getDepDate();
		String depDate = SDF_DMY.format(SDF_YMD.parse(DepDate));
		
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("flight_search_parameter[0][departure_date]", depDate);
		paramMap.put("flight_search_parameter[0][departure_airport_code]",dep);
		paramMap.put("flight_search_parameter[0][arrival_airport_code]",des);
		paramMap.put("flight_search_parameter[0][is_return]","false");
		paramMap.put("flight_search_parameter[0][return_date]", "");
		paramMap.put("adult_count", "1");
		paramMap.put("child_count", "0");
		paramMap.put("infant_count", "0");
		paramMap.put("r", "static_search");
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer", "https://www.flypeach.com/pc/cn");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "booking.flypeach.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "https://booking.flypeach.com/cn", paramMap, "html");
		String session_id = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","_session_id=");
		String reqid = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","reqid=");
		if(session_id==null || reqid==null) return null;
		StringBuilder cookie = new StringBuilder();
		cookie.append(session_id).append(";").append(reqid);
		headerMap.put("Cookie", cookie);
		headerMap.put("Referer", "https://booking.flypeach.com/cn");
		
		String result = this.httpProxyGet(httpClientSessionVo, "https://booking.flypeach.com/cn/flight_search", "html");
		
		return result;
	}
	

	public static void main(String[] args) {
	}

}