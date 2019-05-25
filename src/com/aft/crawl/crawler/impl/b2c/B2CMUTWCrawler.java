package com.aft.crawl.crawler.impl.b2c;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.aft.crawl.result.vo.common.FilghtRule;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
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
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 东方航空台湾
 * @author chenminghong
 */
public class B2CMUTWCrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	
	public B2CMUTWCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			if(!httpResult.contains("tripType")){
				return crawlResults;
			}
			JSONObject parse = JSONObject.parseObject(httpResult);
			JSONArray flightList = parse.getJSONArray("flightList");
			int size = flightList.size();
			if(size==0) return crawlResults;
			for(int i = 0; i<size; i++){
				JSONObject flight = flightList.getJSONObject(i);
				JSONArray flightDetail = flight.getJSONArray("flightDetail");
				int flightSize = flightDetail.size();
				List<FlightSegment> flights = new ArrayList<FlightSegment>();
				for(int j=0; j<flightSize; j++){
					FlightSegment fliment = new FlightSegment();
					JSONObject fli = flightDetail.getJSONObject(j);
					fliment.setTripNo(j+1);
					fliment.setFlightNumber(fli.getString("flightNo"));
					fliment.setAirlineCode(fli.getString("airlineCode"));
					fliment.setDepartureTerminal(fli.getString("dTerminal"));
					fliment.setArrivalTerminal(fli.getString("aTerminal"));
					fliment.setDepAirport(fli.getString("dPort"));
					fliment.setArrAirport(fli.getString("aPort"));
					fliment.setDepTime(fli.getString("departTime"));
					fliment.setArrTime(fli.getString("arrivalTime"));
					String share = fli.getString("isCodeShare");
					if(share.equals("false")){
						share = "N";
					}else share = "Y";
					fliment.setAircraftCode(fli.getString("craftType"));
					fliment.setCodeShare(share);
					flights.add(fliment);
				}
				JSONArray fareList = flight.getJSONArray("fareList");
				int fareSize = fareList.size();
				FlightData flightData = null;
				for(int f=0; f<fareSize; f++){
					JSONObject farePrice = fareList.getJSONObject(f);
					String type = farePrice.getString("paxType");
					if(!"ADT".equals(type)) continue;
					flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
					flightData.setAirlineCode("MUTW");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					List<FlightPrice> prices = new ArrayList<FlightPrice>();
					FlightPrice price = new FlightPrice();
					String ticketPrice = farePrice.getString("ticketPrice");
					String taxPrice = farePrice.getString("taxPrice");
					String comment = farePrice.getString("comment");
					String currency = farePrice.getString("currencyCode");
					String cabinNum = farePrice.getString("ticketLack");
					String cabin = farePrice.getString("subClass");
					String[] cas = cabin.split("-");
					price.setCurrency(currency);
					price.setEquivCurrency(currency);
					price.setFare(ticketPrice);
					price.setEquivFare(ticketPrice);
					price.setTax(taxPrice);
					price.setEquivTax(taxPrice);
					price.setPassengerType("ADT");
					prices.add(price);
					flightData.setPrices(prices);
					 
					List<FilghtRule> ruleList = new ArrayList<FilghtRule>();
					FilghtRule rule = new FilghtRule();
					rule.setWay(1);
					rule.setRefund(comment);
					ruleList.add(rule);
					flightData.setRule(ruleList);
					List<FlightSegment> newFss = new ArrayList<FlightSegment>();
					int c = 0;
					for(FlightSegment fs:flights){
						FlightSegment newFs =  (FlightSegment) fs.clone();
						newFs.setCabin(cas[c]);
						newFs.setCabinCount(cabinNum);
						newFss.add(newFs);
						c++;
					}
					flightData.setFromSegments(newFss);
					crawlResults.add(flightData);
				}
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
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = "2019-03-02";//this.getJobDetail().getDepDate();
		String signUrl = "https://tw.ceair.com/hk/booking.html";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "tw.ceair.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
		
		String freshUrl = "https://tw.ceair.com/mub2c/portal/session/fresh?_=";
		Long times = System.currentTimeMillis();
		freshUrl = freshUrl+String.valueOf(times);
		StringBuilder cookie = new StringBuilder();
		cookie.append("global_site_flag=hk_TW");
		headerMap.put("Accept", "text/plain, */*; q=0.01");
		headerMap.remove("Upgrade-Insecure-Requests");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Referer","https://tw.ceair.com/hk/booking.html");
		headerMap.put("Content-Type","application/json; charset=utf-8");
		headerMap.put("Cookie", cookie);
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,freshUrl,"other");
		String SESSION = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "SESSION=");
		//带Y的是经济舱，不带Y的是全部舱位
		String history = "searchConditionHistory={%22travelType%22:%22oneway%22%2C%22byDays%22:false%2C%22pex_adult%22:1%2C%22pex_child%22:0%2C%22pex_infant%22:0%2C%22flightClass%22:%22Y%22%2C%22byPoints%22:false%2C%22tripList%22:[{%22date%22:%22"+depDate+"T06:36:16.473Z%22%2C%22from%22:{%22value%22:%22"+dep+"%22}%2C%22to%22:{%22value%22:%22"+des+"%22}%2C%22viewDate%22:%22"+depDate+"T06:36:16.473Z%22}]}";
		String cookies = "destination="+des+";es_login_status=non-logined;s_cc=true;s_sq=%5B%5BB%5D%5D;";
		cookie.append(";").append(cookies).append(history).append(";").append(SESSION);
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.remove("Content-Type");
		headerMap.put("Cookie", cookie);
		String shopUrl = "https://tw.ceair.com/mub2c/portal/shopping/?adultCount=1&childCount=0&infantCount=0&depCode="+dep+"&arrCode="+des+"&depDate="+depDate+"&isPoints=&classCode=Y&routeType=OW&currency=";
		String result = this.httpProxyGet(httpClientSessionVo,shopUrl,"other");
		return result;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("您所请求的网址（URL）无法获取")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);

	}
}
