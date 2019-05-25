package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;

/**
 * 宿务太平洋航空Web官网
 * @author chenminghong
 */
public class B2C5JCrawler extends Crawler {
	
	private final String flightUrl = "https://beta.cebupacificair.com/Flight/InternalSelect?o1=%depCode%&d1=%desCode%&o2=&d2=&dd1=%depDate%&ADT=3&CHD=0&INF=0&s=true&mon=true";
	
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	public B2C5JCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		
		String httpResult =this.httpResult();
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			Document document = Jsoup.parse(httpResult);
			Element availabilityTable = document.getElementById("depart-table");
			if(null == availabilityTable) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			crawlResults = this.owFlight(httpResult,crawlResults,availabilityTable);
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
			SimpleDateFormat SDF_DMY = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			SimpleDateFormat SDF_HM = new SimpleDateFormat("MM/dd/yyyy HH:mm");
			SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat SDF_TIM = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Elements gridTravelOptDeps =  element.select(" >tbody >tr");
			if(gridTravelOptDeps == null || gridTravelOptDeps.isEmpty() || gridTravelOptDeps.size()==0 || gridTravelOptDeps.size()==1){
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			FlightData flightData =  null;
			for(Element travel : gridTravelOptDeps){
				Elements thDivs = travel.select(">th >div");
				//过滤联程
				if(thDivs== null || thDivs.size()==0 || thDivs.size()>1)continue;
				String fltNo = thDivs.get(0).select(">span.flight-number").text().replaceAll(" ", "");
				String airlineCode = fltNo.substring(0,2);

				Elements travelTds = travel.select(">td");
				
				Elements td0Div = travelTds.get(0).select("div.text-center");
				
				String depTime = td0Div.get(0).text().replace("H", "").trim();
				depTime = depTime.substring(0,2)+":"+depTime.substring(2,4);
				String desTime = td0Div.get(1).text().replace("H", "").trim();
				desTime = desTime.substring(0,2)+":"+desTime.substring(2,4);
				
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
				
				Elements td2Div = travelTds.get(2).select("div.text-center");
				String depCode = td2Div.get(0).text();
				String desCode = td2Div.get(1).text();
				
				Elements fareColtds = travel.select(">td.fare-bundle-radio-container");
				//获取最低那个价格，第一个就是
				Element fareCol = fareColtds.get(0);

				Elements inputRadio = fareCol.select("input.departFlightClick");
				if(inputRadio==null || inputRadio.size()==0)continue;
				String depDate = inputRadio.get(0).attr("data-departingtime");
				String value = inputRadio.get(0).attr("value");
				String DepTime = getValue(depCode+"\\~", "\\~", value);
				Date dpTime = SDF_HM.parse(DepTime);
				String deptime = SDF_TIM.format(dpTime);
				String ArrTime = getValue(desCode+"\\~", "\\~", value);
				Date arTime = SDF_HM.parse(ArrTime);
				String arrtime = SDF_TIM.format(arTime);
				Date dp = SDF_DMY.parse(depDate);
				depDate = SDF_YMD.format(dp);
				String lebeltxt = fareCol.select("label").text();
				if(lebeltxt==null || "".equals(lebeltxt))continue;
				lebeltxt = lebeltxt.trim().replaceAll(" ", "").replaceAll(",", "");

				String price = lebeltxt.substring(3);
				String currency = lebeltxt.substring(0,3);
				
				flightData = new FlightData(this.getJobDetail(), "OW", depCode, desCode, depDate);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setRouteType("OW");
				flightData.setAirlineCode("5J");//航司
				flightData.setDepAirport(depCode);//出发地
				flightData.setArrAirport(desCode);//出发地
				flightData.setGoDate(depDate);//出发日期
				BigDecimal ticketPrice = new BigDecimal(0);
				if(currency==null || "CNY".equals(currency)){
					ticketPrice = new BigDecimal(price);
				}else{
					BigDecimal rate = CurrencyUtil.getRequest3(currency, "CNY");
					if(rate.compareTo(BigDecimal.ZERO)==0){
						ticketPrice = new BigDecimal(price);
						flightData.setMemo("此价格的币种类型："+ currency);
					}else{
						ticketPrice = new BigDecimal(price).multiply(rate).setScale(0,BigDecimal.ROUND_UP);
					}
				}
				List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
				FlightPrice flightPrice = new FlightPrice();
				flightPrice.setPassengerType("ADT");
				flightPrice.setFare(ticketPrice.toString());//票面价
				flightPrice.setTax(new BigDecimal(0).toString());//税费
				flightPrice.setCurrency("CNY");//币种
				flightPrice.setEquivFare(ticketPrice.toString());
				flightPrice.setEquivTax(new BigDecimal(0).toString());
				flightPrice.setEquivCurrency("CNY");
				flightPriceList.add(flightPrice);
				flightData.setPrices(flightPriceList);
				
				List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
				FlightSegment flightSegment = new FlightSegment();
				flightSegment.setTripNo(1);
				flightSegment.setAirlineCode(airlineCode);
				flightSegment.setFlightNumber(fltNo);
				flightSegment.setDepAirport(depCode);
				flightSegment.setDepTime(deptime);
				flightSegment.setArrAirport(desCode);
				flightSegment.setArrTime(arrtime);
				String shareFlight = this.getShareFlight(airlineCode);
				flightSegment.setCodeShare(shareFlight);
				flightSegment.setCabin("");
				flightSegment.setCabinCount("9");
				flightSegment.setAircraftCode("");
				flightSegmentList.add(flightSegment);
				flightData.setFromSegments(flightSegmentList);
				crawlResults.add(flightData);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
		}
		return crawlResults;
	}
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if(document.title().contains("Cannot connect to origin")) return true;
		if(httpResult.contains("Are you human") || httpResult.contains("403 Forbidden")) return true;
		return super.needToChangeIp(httpResult, document, jsonObject, returnType);
	}
	@Override
	public String httpResult() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String,Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String url = flightUrl.replace("%depCode%", this.getJobDetail().getDepCode()).replace("%desCode%", this.getJobDetail().getDesCode()).replace("%depDate%",this.getJobDetail().getDepDate());
//		String url = flightUrl.replace("%depCode%", "MNL").replace("%desCode%", "TAG").replace("%depDate%", "2017-03-05");
		
//		String result = this.httpProxyGet(url,"html"); //this.httpProxyGet(httpClientSessionVo,url , "html");
		String result = this.httpProxyGet(httpClientSessionVo,url ,"html");
//		System.out.println(result);
		return result;
	}
     
	public String getValue(String sta,String end,String str){
		 String stri = null;
		 Pattern par = Pattern.compile(sta+"(.*?)"+end);
		 Matcher mat = par.matcher(str);
		 while(mat.find()){  
			   stri = mat.group(1);
        }
		 return stri;
	}
	
	public static void main(String[] args) {
		
	}
}