package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyStringUtil;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * chenminghong
 * 海航国际 
 */
public class B2CHUInterCrawler extends Crawler {

	private final static String flightUrl  = "https://new.hnair.com/hainanair/ibe/common/flightSearch.do";
	private final static String searchUrl  = "https://new.hnair.com/hainanair/ibe/common/processSearchForm.do";
	private final HashMap<String, String> detailMap = new HashMap<>();
	private String dep;
	private String des;
	private String depDate;
	public B2CHUInterCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
//		String httpResult = StringTxtUtil.TxtToString("D:\\test.txt");
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(httpResult==null) return crawlResults;
		httpResult = httpResult.replaceAll("\r|\n*","");
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			List<String> results = MyStringUtil.getValueList("var Flight \\= \\{\\}\\;", "Flights\\[position\\] \\= Flight\\;", httpResult);
			if(null == results) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息;");
				return crawlResults;
			}
			for(String strs:results){
				String segs = MyStringUtil.getValue("OD.Segments\\=\\{\\}\\;", "Itinerary.Tags\\=ODTags\\;", strs);
				List<String> flis = MyStringUtil.getValueList("var Segment\\=\\{\\}\\;", "OD.Segments\\[SegmentRPH\\]\\=Segment\\;", segs);
				List<FlightSegment> FlightSegments = new ArrayList<FlightSegment>();
				int i = 1;
				for(String str : flis) {
					String AirlineCarrierEN = MyStringUtil.getValue("Segment.carrierAirlineEN \\= \'", "\'", str);//航司
					String FlightNumber = MyStringUtil.getValue("Segment.marketingFlightNum \\= \'", "\'", str);//航班号 
					String DepartureIATA = MyStringUtil.getValue("Segment.departureIATA \\= \'", "\'", str);//出发地 
					String ArrivalIATA = MyStringUtil.getValue("Segment.arrivalIATA \\= \'", "\'", str);//到达地 
					String DepartureDate = MyStringUtil.getValue("Segment.departureDate \\= \'", "\'", str);//出发日期
					String ArrivalDate = MyStringUtil.getValue("Segment.arrivalDate \\= \'", "\'", str);//到达日期
					String DepartureTime = MyStringUtil.getValue("Segment.departureTime \\= \'", "\'", str);//出发时间 
					String ArrivalTime = MyStringUtil.getValue("Segment.arrivalTime \\= \'", "\'", str);//到达时间 
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(i);
					flightSegment.setAirlineCode(AirlineCarrierEN);
					String shareFlight = this.getShareFlight(AirlineCarrierEN+FlightNumber);//判断是否共享 
					flightSegment.setFlightNumber(AirlineCarrierEN+FlightNumber);
					flightSegment.setDepAirport(DepartureIATA);
					flightSegment.setDepTime(DepartureDate+" "+DepartureTime+":00");
					flightSegment.setArrAirport(ArrivalIATA);
					flightSegment.setArrTime(ArrivalDate+" "+ArrivalTime+":00");
					flightSegment.setCodeShare(shareFlight);
					FlightSegments.add(flightSegment);
					i++;
				}
				String fliDetail = MyStringUtil.getValue("EncryptedFlightDetails\\=\'", "\'", strs);
				String priceValue = detailMap.get(fliDetail.substring(fliDetail.length()-20, fliDetail.length()));
				if(priceValue==null) continue;
				JSONObject jsonObject = JSONObject.parseObject(priceValue);
				JSONObject rs = jsonObject.getJSONObject("BrandSearchRS");
				JSONObject brand = rs.getJSONObject("1").getJSONObject("Brands");
				List<String> displayBrands =MyStringUtil.getValueList("displayBrands.push\\(\'", "\'", httpResult);
				for(String brands :displayBrands) {
					JSONObject less = brand.getJSONObject(brands+"MemberPrice");
					if(less==null) continue;
					JSONObject ADT = less.getJSONObject("ADT");
					JSONObject cabinCodes = less.getJSONObject("cabinCodes");
					String TotalFare = ADT.getString("TotalFare");
					String BaseAmount = ADT.getString("BaseAmount");
					BigDecimal tax = new BigDecimal(TotalFare).subtract(new BigDecimal(BaseAmount));
					List<FlightPrice> priceList = new ArrayList<>();
					FlightPrice price = new FlightPrice();
					price.setPassengerType("ADT");
					price.setCurrency("CNY");
					price.setEquivCurrency("CNY");
					price.setEquivFare(BaseAmount);
					price.setFare(BaseAmount);
					price.setEquivTax(tax.toString());
					price.setTax(tax.toString());
					priceList.add(price);
					FlightData flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
					flightData.setAirlineCode("HU");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					flightData.setPrices(priceList);
					int f = 0;
					List<FlightSegment> fss = new ArrayList<>();
					for(FlightSegment fs :FlightSegments) {
						FlightSegment newFli = (FlightSegment) fs.clone();
						if(f==0) {
							newFli.setCabin(cabinCodes.getJSONObject("01000101").getString("ResBookDesigCode"));
							newFli.setCabinCount(cabinCodes.getJSONObject("01000101").getString("ResBookDesigQuantity"));
						}else {
							newFli.setCabin(cabinCodes.getJSONObject("01000102").getString("ResBookDesigCode"));
							newFli.setCabinCount(cabinCodes.getJSONObject("01000102").getString("ResBookDesigQuantity"));
						}
						fss.add(newFli);
						f++;
					}
					flightData.setFromSegments(fss);
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
		protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType)
				throws Exception {
			if(httpResult.contains("抱歉，遇到些问题"))return true;
			return super.needToChangeIp(httpResult, document, jsonObject, returnType);
		}
	
	public String httpResult() throws Exception{
		ArrayList<String> list = new ArrayList<String>();
		list.sort(new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		});
		
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(this.threadMark);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		httpClientSessionVo.setHeaderMap(headerMap);
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "new.hnair.com");
		
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, flightUrl, "other");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String Webtrends = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "Webtrends=");
		String X_LB = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "X-LB=");
		
		StringBuilder cookie = new StringBuilder();
		cookie.append(JSESSIONID).append(";").append(Webtrends).append(";").append(X_LB);
		
		headerMap.put("Cookie", cookie);
		headerMap.put("Accept", "*/*");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Referer", "https://new.hnair.com/hainanair/ibe/common/flightSearch.do");
		String desUrl = "https://new.hnair.com/hainanair/ibe/hierarchy/getLocationCodeAjax.do?locationCode="+des+"&method=IATACodeToLocationId&ConversationID=";
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo, desUrl, "other");
		String desRes = httpVo.getHttpResult();
		String desCode = MyStringUtil.getValue("\\{\"airportCode\"\\:\"", "\"", desRes);
		headerMap.put("Cookie", cookie);
		headerMap.remove("X-Requested-With");
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Cache-Control", "max-age=0");
		headerMap.put("Origin", "http://new.hnair.com");
		headerMap.put("Content-Type","application/x-www-form-urlencoded");
		headerMap.put("Referer", "https://new.hnair.com/hainanair/ibe/common/flightSearch.do");
		String param = "Search%2FAirlineMode=false&Search%2FcalendarCacheSearchDays=60&Search%2FcalendarSearched=false&dropOffLocationRequired=false&Search%2FsearchType=F&searchTypeValidator=F&xSellMode=false&Search%2FflightType=oneway&destinationLocationSearchBoxType=L&Search%2FAirDirectOnly=0&Search%2FseatClass=A&Search%2FOriginDestinationInformation%2FOrigin%2Flocation=CITY_"+dep+"_CN&Search%2FOriginDestinationInformation%2FOrigin%2Flocation_input=&Search%2FOriginDestinationInformation%2FDestination%2Flocation="+desCode+"&Search%2FOriginDestinationInformation%2FDestination%2Flocation_input=&Search%2FDateInformation%2FdepartDate="+depDate+"&Search%2FDateInformation%2FreturnDate=&Search%2FPassengers%2Fadults=1&Search%2FPassengers%2Fchildren=0&Search%2FPassengers%2Finfants=0&Search%2FPassengers%2FMilitaryDisabled=0&Search%2FPassengers%2FPoliceDisabled=0&Search%2FpromotionCode=";
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, searchUrl,param, "other");
		if(httpVo==null) return null;
		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		if(location==null) return null;
		headerMap.remove("Content-Type");
		String result = this.httpProxyGet(httpClientSessionVo, location, "other");
//		StringTxtUtil.write("D:\\FILE.txt", result);
		List<String> details = MyStringUtil.getValueList("EncryptedFlightDetails\\=\'", "\'", result);
		if(details.size()==0) return null;
		String detailUrl = "https://new.hnair.com/hainanair/ibe/air/processInternationalBrandSearch.do";
		headerMap.put("Accept", "*/*");
		headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
		headerMap.put("X-Requested-With","XMLHttpRequest");
		headerMap.put("Referer",location);
		int d = 1;
		for(String detail : details) {
			String par = "encryptedFlightDetails="+detail+"&seqNum="+d+"&Search%2FseatClass=A";
			httpVo = this.httpProxyResultVoPost(httpClientSessionVo, detailUrl,par, "other");
			if(httpVo==null) {
				d++;
				continue;
			}
			String detailRes = httpVo.getHttpResult();
			detailMap.put(detail.substring(detail.length()-20, detail.length()), detailRes);
			d++;
		}
		return result;
	}
	

	
}