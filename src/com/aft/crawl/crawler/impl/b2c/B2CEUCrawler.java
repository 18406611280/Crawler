package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.nodes.Document;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.MyStringUtil;
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
public class B2CEUCrawler extends Crawler {

	private String dep;
	private String des;
	private String depDate;
	
	public B2CEUCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			JSONObject resultObj = JSONObject.parseObject(httpResult);
			JSONObject searchResult = resultObj.getJSONObject("FlightSearchResults");
			if(null == searchResult) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			JSONArray Flights = searchResult.getJSONArray("Flights");
		    if(null == Flights) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;

			JSONObject Flight0 = Flights.getJSONObject(0);
			JSONArray flights = Flight0.getJSONArray("Flight");
			int flightSize = flights.size();
			for(int i =0; i<flightSize; i++){
				JSONObject flight = flights.getJSONObject(i);
				JSONObject FlightDetail = flight.getJSONArray("FlightDetails").getJSONObject(0);
				JSONObject FlightLeg = FlightDetail.getJSONArray("FlightLeg").getJSONObject(0);
				String airlineCode = FlightLeg.getString("OperatingAirline");//EU
				String FlightNumber = FlightLeg.getString("FlightNumber");//6665
				String shareFlight = this.getShareFlight(airlineCode);
				String flightNo = airlineCode+FlightNumber;
				JSONObject Departure = FlightLeg.getJSONObject("Departure");
				String depTime = Departure.getString("Time").substring(0, 5);
				JSONObject Arrival = FlightLeg.getJSONObject("Arrival");
				String desTime = Arrival.getString("Time").substring(0, 5);
				JSONObject Price = flight.getJSONObject("Price");
				JSONArray FareInfos = Price.getJSONArray("FareInfos");
				int fareSize = FareInfos.size();
				for(int j=0; j<fareSize; j++){
					JSONObject info = FareInfos.getJSONObject(j);
					JSONArray FareInfo = info.getJSONArray("FareInfo");
					if(FareInfo==null)continue;
					JSONObject FareInfo0 = FareInfo.getJSONObject(0);
					String cabin = FareInfo0.getJSONObject("FareReference").getString("ResBookDesigCode");
					JSONObject Fare = FareInfo0.getJSONArray("FareInfo").getJSONObject(0).getJSONObject("Fare");
					String BaseAmount = Fare.getString("BaseAmount");
					String TaxAmount = Fare.getString("TaxAmount");
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, flightNo, shareFlight, dep, des, depDate, cabin);
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					
					crawlResult.setRemainSite(9);
					crawlResult.setTicketPrice(new BigDecimal(BaseAmount));
					crawlResult.setSalePrice(new BigDecimal(TaxAmount));
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
	public String httpResult() throws Exception {
		
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
//		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		String signUrl = "https://www.cdal.com.cn/";

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate, br");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.cdal.com.cn");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
		String result1 = httpVo.getHttpResult();
		String ver = MyStringUtil.getValue("travelskyJS\\/homePage\\/homepage\\.js\\?ver\\=", "\"", result1);
		String verUrl = "https://www.cdal.com.cn/stdair/airline/eu/travelskyJS/homePage/homepage.js?ver="+ver;
		String cookies = MyHttpClientUtil.getHeaderValues(httpVo.getHeaders(), "Set-Cookie");
//		String Webtrends = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "Webtrends=");
//		StringBuilder cookie = new StringBuilder();
//		cookie.append(JSESSIONID).append(";").append(Webtrends);
		headerMap.remove("Upgrade-Insecure-Requests");
		headerMap.put("Cookie", cookies);
//		headerMap.put("Accept", "*/*");
		headerMap.put("Referer", "https://www.cdal.com.cn/");
//		String result2 = this.httpProxyGet(httpClientSessionVo,verUrl,"other");
//		String userIp = MyStringUtil.getValue("UserIP\":\"", "\"", result2);
		Object[] proxyInfo = ProxyUtil.getProxyInfo(threadMark);
		String userIp = (String)proxyInfo[0];
		String depCode = "CITY_"+dep+"_CN";
		String desCode = "CITY_"+des+"_CN";
		String param = "{\"url\":\"http://10.8.208.29/euair/ibe/common/processSearchForm.do\",\"data\":{\"SearchPanelForm\":{\"SearchType\":\"F\",\"action\":\"/common/processSearchForm.do\",\"method\":\"post\",\"name\":\"plan_trip\",\"PageInfo\":{\"ConversationID\":\"OJ1504076504131\",\"Currency\":\"CNY\",\"CurrencyRoundTo\":\"0.01\",\"FromServicing\":\"false\",\"Language\":\"zh\",\"LayoutType\":\"\",\"Locale\":\"zh_CN\",\"NavSelect\":\"\",\"ReadOnly\":\"false\",\"SessionID\":\"6618C586E726D1904F67C660113CB0EC.TRPIBEServer1\",\"SessionPageRandom\":\"\",\"UserIP\":\""+userIp+"\",\"skin\":\"hainan\",\"POS\":{\"CompanyCode\":\"ibe\"}},\"Restrictions\":{\"AdultLimit\":\"5\",\"HomePage\":\"true\",\"MaxChildren\":\"5\",\"MaxNumLegs\":\"4\",\"PostBookingDeeplink\":\"false\",\"ViewOnly\":\"false\"},\"SearchTypeInput\":{\"name\":\"Search/searchType\",\"type\":\"fixed\",\"value\":\"F\"},\"XSellModeInput\":{\"name\":\"xSellMode\",\"type\":\"fixed\",\"value\":\"false\"},\"DropOffLocationRequiredInput\":{\"name\":\"dropOffLocationRequired\",\"type\":\"fixed\",\"value\":\"false\"},\"NumNightsInput\":{\"name\":\"Search/DateInformation/numNights\",\"type\":\"fixed\",\"value\":\"1\"},\"FlightSearch\":{\"SearchTypeValidatorInput\":{\"name\":\"searchTypeValidator\",\"type\":\"fixed\",\"value\":\"F\"},\"Location\":{\"OriginInput\":{\"display\":\"BEIJINGSHOUDUGUOJI\",\"name\":\"Search/OriginDestinationInformation/Origin/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\""+depCode+"\"},\"DestinationInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Destination/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\""+desCode+"\"}},\"AirlineModeInput\":{\"name\":\"Search/AirlineMode\",\"type\":\"fixed\",\"value\":\"false\"},\"FlightTypeInput\":{\"name\":\"Search/flightType\",\"type\":\"list\",\"value\":\"oneway\",\"option\":[{\"value\":\"oneway\"},{\"selected\":\"selected\",\"value\":\"return\"},{\"value\":\"multicity\"}]},\"Calendar\":{\"DepartDateInput\":{\"name\":\"Search/DateInformation/departDate\",\"type\":\"calendar\",\"value\":\""+depDate+"\"},\"ReturnDateInput\":{\"name\":\"Search/DateInformation/returnDate\",\"type\":\"calendar\",\"value\":\"\"},\"DepartTimeInput\":{\"name\":\"Search/AirDepartTime\",\"type\":\"clock\",\"value\":\"Any\"},\"ReturnTimeInput\":{\"name\":\"Search/AirReturnTime\",\"type\":\"clock\",\"value\":\"Any\"}},\"CalendarSearchInput\":{\"name\":\"Search/calendarSearch\",\"type\":\"boolean\",\"value\":\"true\"},\"CalendarSearchedInput\":{\"name\":\"Search/calendarSearched\",\"type\":\"fixed\",\"value\":\"false\"},\"Passengers\":{\"AdultsInput\":{\"name\":\"Search/Passengers/adults\",\"type\":\"number\",\"value\":\"1\"},\"DisabledMilitaryInput\":{\"name\":\"Search/Passengers/adults/@GM\",\"type\":\"number\",\"value\":\"0\"},\"DisabledPoliceInput\":{\"name\":\"Search/Passengers/adults/@JC\",\"type\":\"number\",\"value\":\"0\"},\"ChildrenInput\":{\"name\":\"Search/Passengers/children\",\"type\":\"number\",\"value\":\"0\"},\"InfantsInput\":{\"name\":\"Search/Passengers/infants\",\"type\":\"number\",\"value\":\"0\"}},\"MoreOptionsInput\":{\"name\":\"Search/moreOptions\",\"type\":\"boolean\",\"value\":\"true\"},\"AdditionalOptions\":{\"SeatClassInput\":{\"name\":\"Search/seatClass\",\"type\":\"list\",\"value\":\"Y\"},\"AirlinePrefInput\":{\"multiple\":\"multiple\",\"name\":\"Search/airlinePrefs/airlinePref\",\"type\":\"list\",\"value\":\"\"},\"AirDirectOnlyInput\":{\"name\":\"Search/AirDirectOnly\",\"type\":\"boolean\",\"value\":\"1\"},\"RestrictionTypeInput\":{\"name\":\"Search/restrictionType\",\"type\":\"list\",\"value\":\"Restricted\",\"option\":[{\"selected\":\"selected\",\"value\":\"Restricted\"},{\"value\":\"FullyFlexible\"}]}},\"MultiCitySearchPanel\":{\"Flight\":[{\"display\":\"true\",\"Location\":{\"OriginInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Origin$1$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"},\"DestinationInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Destination$1$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"}},\"DepartDateInput\":{\"name\":\"Search/DateInformation$1$/departDate\",\"type\":\"calendar\",\"value\":\"2017-09-13\"},\"DepartTimeInput\":{\"name\":\"Search/DateInformation$1$/departTime\",\"type\":\"clock\",\"value\":\"Any\"}},{\"display\":\"true\",\"Location\":{\"OriginInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Origin$2$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"},\"DestinationInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Destination$2$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"}},\"DepartDateInput\":{\"name\":\"Search/DateInformation$2$/departDate\",\"type\":\"calendar\",\"value\":\"2017-09-20\"},\"DepartTimeInput\":{\"name\":\"Search/DateInformation$2$/departTime\",\"type\":\"clock\",\"value\":\"Any\"}},{\"display\":\"true\",\"Location\":{\"OriginInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Origin$3$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"},\"DestinationInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Destination$3$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"}},\"DepartDateInput\":{\"name\":\"Search/DateInformation$3$/departDate\",\"type\":\"calendar\",\"value\":\"2017-09-27\"},\"DepartTimeInput\":{\"name\":\"Search/DateInformation$3$/departTime\",\"type\":\"clock\",\"value\":\"Any\"}},{\"Location\":{\"OriginInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Origin$4$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"},\"DestinationInput\":{\"display\":\"\",\"name\":\"Search/OriginDestinationInformation/Destination$4$/location\",\"parameters\":\"&searchableOnly=false&locationType=airport\",\"readonly\":\"false\",\"type\":\"location\",\"value\":\"\"}},\"DepartDateInput\":{\"name\":\"Search/DateInformation$4$/departDate\",\"type\":\"calendar\",\"value\":\"2017-10-04\"},\"DepartTimeInput\":{\"name\":\"Search/DateInformation$4$/departTime\",\"type\":\"clock\",\"value\":\"Any\"}}]}},\"MilesEnableInput\":{\"name\":\"milesEnable\",\"type\":\"fixed\",\"value\":\"\"}}}}";
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("X-Requested-With", "XMLHttpRequest");
		headerMap.put("Content-Type", "application/json; charset=utf-8");
		String result3 = this.httpProxyPost(httpClientSessionVo,"https://www.cdal.com.cn/app/spring/common/ajaxCommon", param, "json");
		String result = this.httpProxyPost(httpClientSessionVo,"https://www.cdal.com.cn/app/spring/common/ajaxCommon", param, "json");
		System.out.println(result);
		return result;
	}
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document,Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("ERROR")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);

	}
}