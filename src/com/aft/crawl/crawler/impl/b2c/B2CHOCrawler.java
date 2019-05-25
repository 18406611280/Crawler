package com.aft.crawl.crawler.impl.b2c;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 吉祥航空 
 * chenminghong
 */
public class B2CHOCrawler extends Crawler {
	
	private String dep;
	private String des;
	private String depDate;

	
	public B2CHOCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(httpResult==null) return crawlResults;
		try {
			if(httpResult.contains("序列不包含任何匹配元素") || httpResult.contains("服务不可用")) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			JSONObject resultObj = JSONObject.parseObject(httpResult);
			JSONArray flightInfoList = resultObj.getJSONArray("FlightInfoList");
			int flightSize = flightInfoList.size();
			if(null == flightInfoList || flightSize==0) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			for(int f = 0; f<flightSize; f++) {
				JSONObject flight = flightInfoList.getJSONObject(f);
				// 航班号, 航司
				String fltNo = flight.getString("FlightNo").trim().toUpperCase();
				String airlineCode = fltNo.substring(0, 2);
				if(!airlineCode.contains("HO")) continue;
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				Object CodeShare = flight.getString("CodeShare");
				if(null != CodeShare && Boolean.valueOf(CodeShare.toString())) shareFlight = "Y";
				// 出发,到达机场
				String depCode = flight.getString("DepAirport").trim().toUpperCase();
				String desCode = flight.getString("ArrAirport").trim().toUpperCase();
				
				// 出发日期
				String depDate = flight.getString("FlightDate").trim();
				
				// 出发,到达时间
				String depTime = flight.getString("DepDateTime").trim()+":00";
				String desTime = flight.getString("ArrDateTime").trim()+":00";
				
				List<FlightSegment> fss = new ArrayList<FlightSegment>();
				FlightSegment fs = new FlightSegment();
				fs.setTripNo(1);
				fs.setAirlineCode(airlineCode);
				fs.setFlightNumber(fltNo);
				fs.setCodeShare(shareFlight);
				fs.setDepAirport(depCode);
				fs.setArrAirport(desCode);
				fs.setDepTime(depTime);
				fs.setArrTime(desTime);
				fss.add(fs);
				
				JSONArray fareList = flight.getJSONArray("CabinFareList");
				int priceSize = fareList.size();
				if(priceSize == 0) continue ;
				for(int p =0; p<priceSize; p++) {
					JSONObject priceObj = fareList.getJSONObject(p);
					FlightData flightData = new FlightData(this.getJobDetail(), "OW", depCode, desCode, depDate);
					flightData.setAirlineCode("HO");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					
					String cabin = priceObj.getString("CabinCode").trim().toUpperCase();
					String cabinNum = priceObj.getString("CabinNumber").trim();
					if("A".equals(cabinNum)) cabinNum = "9";
					String barePrice = priceObj.getString("PriceValue").trim();
					String tax = priceObj.getString("TaxAmount").trim();
					
					List<FlightPrice> prices = new ArrayList<FlightPrice>();
					FlightPrice fPrice = new FlightPrice();
					fPrice.setCurrency("CNY");
					fPrice.setEquivCurrency("CNY");
					fPrice.setEquivFare(barePrice);
					fPrice.setFare(barePrice);
					fPrice.setEquivTax(tax);
					fPrice.setTax(tax);
					fPrice.setPassengerType("ADT");
					prices.add(fPrice);
					flightData.setPrices(prices);
					
					List<FlightSegment> newFss = new ArrayList<FlightSegment>();
					for(FlightSegment fs1:fss){
						FlightSegment newFs = (FlightSegment)fs1.clone();
						newFs.setCabin(cabin);
						newFs.setCabinCount(cabinNum);
						newFss.add(newFs);
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
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("ERROR: Not Found") || httpResult.contains("对不起！查询超出限制次数") || httpResult.contains("服务不可用")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);
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
		
		String Url = "http://www.juneyaoair.com/pages/Flight/flight_reserve.aspx";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests","1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.juneyaoair.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,Url,"other");
		String NSC = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","NSC_O_C2DXfc_IUUQ_80=");
		StringBuilder cookie = new StringBuilder();
		cookie.append(NSC);
		String flightUrl = "http://www.juneyaoair.com/pages/Flight/flight.aspx?flightType=OW&sendCode="+dep+"&arrCode="+des+"&directType=N&tripType=D&departureDate="+depDate+"&returnDate="+depDate;
		headerMap.put("Referer",Url);
		headerMap.put("Cookie",cookie.toString());
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,flightUrl,"other");
		String NET_SessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie","ASP.NET_SessionId=");
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String queryUrl = "http://www.juneyaoair.com/UnitOrderWebAPI/Book/QueryFlightFareNew?flightType=OW&tripType=D&directType=D&departureDate="+depDate+"&sendCode="+dep+"&arrCode="+des+"&returnDate=&_="+timeStamp;
		cookie.append(";").append(NET_SessionId);
		headerMap.remove("Upgrade-Insecure-Requests");
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Cookie",cookie.toString());
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,queryUrl,"other");
		if(httpVo==null) return null;
		String result = httpVo.getHttpResult();
		return result;
	}
}