package com.aft.crawl.crawler.impl.b2c;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FilghtRule;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;
import com.aft.utils.yzm.MyYzm;

/**
 * 四川航空 
 */
public class B2C3UCrawler extends Crawler {

	private final static String loginUrl = "http://www.sichuanair.com/Account/SignIn";

	private final static String loginValidUrl = "http://www.sichuanair.com/Account/SignInAuthValid";

	private final static String yzmUrl = "http://www.sichuanair.com/Base/GetVerifyCode/1";

	private final static String cardParamInfoUrl = "http://www.sichuanair.com/Home/GetCardParamInfo";

	private final static String flightUrl = "http://www.sichuanair.com/ETicket/AirlineList";

	private final static String airlineParamJSON = "{\"AirlineType\":\"Single\",\"IsFixedCabin\":false,\"RouteList\":[{\"RouteIndex\":1,\"OrgCity\":\"%depCode%\",\"DesCity\":\"%desCode%\",\"FlightDate\":\"%depDate%\"}],\"AVType\":0}";

	private final static String memberAirlineParamJSON = "{\"AirlineType\":\"Single\",\"IsFixedCabin\":false,\"RouteList\":[{\"RouteIndex\":1,\"OrgCity\":\"%depCode%\",\"DesCity\":\"%desCode%\",\"FlightDate\":\"%depDate%\"}],\"AVType\":0}";

	private final static String cardAirlineParamJSON = "{\"AirlineType\":\"Single\",\"IsFixedCabin\":false,\"RouteList\":[{\"RouteIndex\":1,\"OrgCity\":\"%depCode%\",\"DesCity\":\"%desCode%\",\"FlightDate\":\"%depDate%\"}],\"CardParamModel\":%cardParamModel%,\"AVType\":1}";

	private final static Pattern pattern = Pattern.compile("arrPageValue\\.AirlineParamJSON = (\\{.+\\});");

	public B2C3UCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if (httpResult==null) return null;
			
		Document document = Jsoup.parse(httpResult);

		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		Element main = document.getElementById("main");
		if(null == main) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		FlightData flightData = null;
		String Departure = document.getElementById("Search/OriginDestinationInformation/Origin/location").val().split("_")[1];//出发地
		String Arrival = document.getElementById("Search/OriginDestinationInformation/Destination/location").val().split("_")[1];//达到地
		String DepDate = document.getElementById("Search/DateInformation/departDate").val();//出发时间
		Elements eleItems = main.select("div.brand-tb > div.tbd-section");//单段
		Elements multiEleItems = main.select("div.combine-box > div.multi-tb > div.tbd-section");//多段
		if(eleItems!=null){//单段的
			for(Element eleItem : eleItems){
				Element routeInfo = eleItem.select("> div.tb-tr.cl > div.tb-td.tb-tt > div.route-info.cl").first();//单段航班
				Element routeTime = eleItem.select("> div.tb-tr.cl > div.tb-td.tb-tt > div.route-start-end.cl").first();
				Elements prices = eleItem.select("> div.tb-data-detail > div.data-detail-item.cl");//获取航班各票价
				Element eleFltNo = routeInfo.select("> span.flight-code").first();
				String fltNo = eleFltNo.ownText().trim().toUpperCase();
				String fltEN = fltNo.substring(0, 2);//航司
//				String filNum = fltNo.substring(2);//航班号
				String shareFlight = this.getShareFlight(fltEN);//判断是否共享
				String planeTrigger = routeInfo.select("> div.plane-type > span.plane-trigger").first().ownText().trim();//机型
				String departureTime = routeTime.select("> div.route-start > p.route-time").first().ownText().trim();//出发时间  10:00
				String departureDate = routeTime.select("> div.route-start > p.route-date").first().ownText().trim();//出发日期
				String arrivalTime = routeTime.select("> div.route-end > p.route-time").first().ownText().trim();//到达时间 12:00
				String arrivalDate = routeTime.select("> div.route-end > p.route-date").first().ownText().trim();//到达日期
				String depTime = departureDate + " "+departureTime+":00";//出发日期时间 2017-11-24 10:00:00
				String arrTime = arrivalDate + " "+arrivalTime+":00";//到达日期时间
				for(Element price : prices){
					flightData = new FlightData(this.getJobDetail(), "OW", Departure, Arrival, departureDate);
					String cabinEle = price.getElementsByClass("week-color").first().ownText().trim();
					String cabin = cabinEle.substring(0, cabinEle.length()-1);//舱位
					String remain = price.getElementsByClass("single-ticket-remain").first().ownText();
					String cabinCount = "9";
					if(remain!=null && !remain.isEmpty()){
						Pattern pattern = Pattern.compile("剩余(\\d+)张票");
						Matcher matcher = pattern.matcher(remain);
						if(matcher.find()) {
							cabinCount = matcher.group(1);
						}
					}
					Element ticketEle = price.getElementsByClass("ticket-list").first();
					Elements lis = ticketEle.getElementsByTag("li");
					String ticketPrice =lis.get(0).ownText().trim().substring(4);//票面价
					String jiJianTax =lis.get(1).ownText().trim().substring(4);//机建税
					String ranYouTax =lis.get(2).ownText().trim().substring(4);//燃油税
					String tax = new Integer(Integer.parseInt(jiJianTax)+Integer.parseInt(ranYouTax)).toString();//总税费
					Element rule = price.getElementsByClass("hover-con").first();//退改规则
					Elements trdts = rule.getElementsByClass("tr-dt").first().getElementsByTag("td");
					Elements trdds = rule.getElementsByClass("tr-dd").first().getElementsByTag("td");
					String reschedul = trdts.get(0).ownText().trim()+trdds.get(0).ownText().trim()+";"
							+trdts.get(1).ownText().trim()+trdds.get(1).ownText().trim();//改签规则
					String refund = trdts.get(2).ownText().trim()+trdds.get(2).ownText().trim()+";"
							+trdts.get(3).ownText().trim()+trdds.get(3).ownText().trim();//退票规则
					String endorse = trdts.get(4).ownText().trim();//签转规则
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String date = sdf.format(new Date());
					flightData.setCreateTime(date);
					flightData.setRouteType("OW");
					flightData.setAirlineCode(fltEN);//航司
					flightData.setDepAirport(Departure);//出发地
					flightData.setArrAirport(Arrival);//出发地
					flightData.setGoDate(departureDate);//出发日期
					List<FilghtRule> filghtRuleList = new ArrayList<FilghtRule>();//规则
					FilghtRule filghtRule = new FilghtRule();
					filghtRule.setRefund(refund);//退票规则
					filghtRule.setEndorse(endorse);//转签规则
					filghtRule.setOther("改签规则:"+reschedul);//其他规则
					filghtRuleList.add(filghtRule);
					flightData.setRule(filghtRuleList);

					List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
					FlightPrice flightPrice = new FlightPrice();
					flightPrice.setPassengerType("ADT");
					flightPrice.setFare(ticketPrice);//票面价
					flightPrice.setTax(tax);//税费
					flightPrice.setCurrency("CNY");//币种
					flightPrice.setEquivFare(ticketPrice);
					flightPrice.setEquivTax(tax);
					flightPrice.setEquivCurrency("CNY");
					flightPriceList.add(flightPrice);
					flightData.setPrices(flightPriceList);

					List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
					FlightSegment flightSegment = new FlightSegment();
					flightSegment.setTripNo(1);
					flightSegment.setAirlineCode(fltEN);
					flightSegment.setFlightNumber(fltNo);
					flightSegment.setDepAirport(Departure);
					flightSegment.setDepTime(depTime);
					flightSegment.setArrAirport(Arrival);
					flightSegment.setArrTime(arrTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setCabinCount(cabinCount);
					flightSegment.setAircraftCode(planeTrigger);
					flightSegmentList.add(flightSegment);
					flightData.setFromSegments(flightSegmentList);
					crawlResults.add(flightData);
				}
			}
			if(multiEleItems!=null){//多航段
				for(Element eleItem : multiEleItems){
					flightData = new FlightData(this.getJobDetail(), "OW", Departure, Arrival, DepDate);
					Elements multiRoutes = eleItem.select("> div.tb-tr.cl > div.tb-td.tb-tt > div.multi-wrap.cl");//多段航班
					String ticketPrice = eleItem.getElementsByClass("ticket-price").first().ownText().trim();//票面价
					Elements ticketPrices = eleItem.select("> div.tb-data-detail > div.data-multi-item.cl > div.data-multi-cell");//获取航班各票价
					List<String> cabins = new ArrayList<String>();
					int tolPrice=0;//总票面价
					for(Element price:ticketPrices){
						String cabin1 = price.getElementsByClass("week-color").first().ownText().trim();
						String cabin = cabin1.substring(0, cabin1.length()-1);
						String ticketSum = price.getElementsByClass("ticket-sum").first().ownText().trim().substring(4);//总票价
						tolPrice = tolPrice+Integer.parseInt(ticketSum);
						cabins.add(cabin);
					}
					String tax = new Integer(tolPrice-Integer.parseInt(ticketPrice)).toString();//总税费
					//封装票价
					List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
					FlightPrice flightPrice = new FlightPrice();
					flightPrice.setPassengerType("ADT");
					flightPrice.setFare(ticketPrice);//票面价
					flightPrice.setTax(tax);//税费
					flightPrice.setCurrency("CNY");//币种
					flightPrice.setEquivFare(ticketPrice);
					flightPrice.setEquivTax(tax);
					flightPrice.setEquivCurrency("CNY");
					flightPriceList.add(flightPrice);
					flightData.setPrices(flightPriceList);
					//航段
					int j = 0;
					List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
					for(Element route:multiRoutes){
						Element eleFltNo = route.getElementsByClass("flight-code").first();
						String fltNo = eleFltNo.ownText().trim().toUpperCase();
						String fltEN = fltNo.substring(0, 2);//航司
						String filNum = fltNo.substring(2);//航班号
						String shareFlight = this.getShareFlight(fltEN);//判断是否共享
						String planeTrigger = route.getElementsByClass("plane-trigger").first().ownText().trim();//机型
						Element startEle = route.select("> div.route-start-end.cl > div.route-start > p.route-date").first();//出发
						Element endEle = route.select("> div.route-start-end.cl > div.route-end > p.route-date").first();//到达
						String departureTime = startEle.select("> span.route-time").first().ownText().trim();//出发时间  10:00
						String departureDate = startEle.ownText().trim();//出发日期
						String arrivalTime = endEle.select("> span.route-time").first().ownText().trim();//到达时间 12:00
						String arrivalDate = endEle.ownText().trim();;//到达日期
						String depTime = departureDate + " "+departureTime+":00";//出发日期时间 2017-11-24 10:00:00
						String arrTime = arrivalDate + " "+arrivalTime+":00";//到达日期时间
						String cabin = cabins.get(j);//舱位
						FlightSegment flightSegment = new FlightSegment();
						flightSegment.setTripNo(j+1);
						flightSegment.setAirlineCode(fltEN);
						flightSegment.setFlightNumber(filNum);
						flightSegment.setDepAirport(Departure);
						flightSegment.setDepTime(depTime);
						flightSegment.setArrAirport(Arrival);
						flightSegment.setArrTime(arrTime);
						flightSegment.setCodeShare(shareFlight);
						flightSegment.setCabin(cabin);
						flightSegment.setAircraftCode(planeTrigger);
						flightSegmentList.add(flightSegment);
						j=j+1;
					}
					flightData.setFromSegments(flightSegmentList);
				}
			}
		}
		return crawlResults;
	}

	@Override
	public String httpResult() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);

		String dep = this.getJobDetail().getDepCode();
		String des = this.getJobDetail().getDesCode();
		String depDate = this.getJobDetail().getDepDate();
		Map<String,Object> paramMap = new HashMap<String, Object>();
		paramMap.put("Search/AirlineMode","false");
		paramMap.put("Search/calendarCacheSearchDays","60");
		paramMap.put("Search/calendarSearched","false");
		paramMap.put("dropOffLocationRequired","false");
		paramMap.put("Search/searchType","F");
		paramMap.put("searchTypeValidator","F");
		paramMap.put("xSellMode","false");
		paramMap.put("Search/flightType","oneway");
		paramMap.put("destinationLocationSearchBoxType","L");
		paramMap.put("Search/isUserPrice","1");
		paramMap.put("Search/OriginDestinationInformation/Origin/location",newCityName(dep));
		paramMap.put("Search/OriginDestinationInformation/Destination/location",newCityName(des));
		paramMap.put("Search/DateInformation/departDate_display",depDate);
		paramMap.put("Search/DateInformation/departDate",depDate);
		//		paramMap.put("Search/DateInformation/returnDate","2017-11-26");
		paramMap.put("Search/calendarSearch","false");
		paramMap.put("Search/Passengers/adults","1");
		paramMap.put("Search/Passengers/children","0");
		paramMap.put("Search/seatClass","Y");
		paramMap.put("Search/promotionCode","");

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html, */*; q=0.01");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.sichuanair.com");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Referer", "http://www.sichuanair.com/3uair/ibe/common/homeRedirect.do");
		httpClientSessionVo.setHeaderMap(headerMap);



		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, "http://www.sichuanair.com/pages/hotCity.html","other");
		if(httpVo==null) return null;
		String Webtrends = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "Webtrends=");
		String xlb = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "X-LB=");


		StringBuilder cookie = new StringBuilder()
				.append(Webtrends).append(";")
				.append(xlb);

		headerMap.put("Cookie",cookie);
		headerMap.put("Accept","*/*");

		httpVo = this.httpProxyResultVoGet(httpClientSessionVo, "http://flights.sichuanair.com/3uair/ibe/hierarchy/getLocationCodeAjax.do?locationCode=HAK&method=IATACodeToLocationId&ConversationID=","other");
		if(httpVo==null) return null;
			
		String sessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		cookie.append(";").append(sessionId);

		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "http://flights.sichuanair.com/3uair/ibe/common/processSearchForm.do",paramMap,"other");
		if(httpVo==null) return null;
		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");

		String result = this.httpProxyGet(httpClientSessionVo,location, "html");
		return result;
	}
	//新的城市参数格式如：“CITY_CTU_CN”
	public String newCityName(String city){
		return "CITY_"+city+"_CN";
	}

	@Override
	@SuppressWarnings("unchecked")
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;

		// {"Result":false,"Message":"系统繁忙！请稍候重试...","IsShowAgain":false}
		if("json".equals(returnType)) {
			Map<String, Object> map = (Map<String, Object>)jsonObject;
			Object message = map.get("Message");
			return null != message && message.toString().contains("系统");
		}
		return false;
	}


	/**
	 * {"CardNO": "0928030000002523"}
	 * 金卡获取 PassKey
	 * 
	 * {"UserName": "850754391", "Password": "CAN12345"}
	 * 登录获取 PassKey
	 * 
	 * 直接获取 PassKey
	 */
	@SuppressWarnings("unchecked")
	private void getPassKey() {
		Map<String, Object> paramMap = this.getBaseJobParamMap();
		if(null != paramMap && !paramMap.isEmpty()) return ;
		synchronized(CrawlerType.B2C3UMemberPageType.equals(this.getPageType()) ? memberAirlineParamJSON
				: CrawlerType.B2C3UCardPageType.equals(this.getPageType()) ? cardAirlineParamJSON : airlineParamJSON) {
			paramMap = this.getBaseJobParamMap();
			if(null != paramMap && !paramMap.isEmpty()) return ;
			String httpResult = null;
			try {
				Map<String, Object> postMap = new HashMap<String, Object>();
				String cardFlag = null;
				if(CrawlerType.B2C3UMemberPageType.equals(this.getPageType())) {
					Map<String, String> jsonMap = this.getTimerJob().getParamMap();
					postMap.put("UserName", jsonMap.get("UserName"));
					postMap.put("Password", jsonMap.get("Password"));
					MyHttpClientResultVo httpVo = MyHttpClientUtil.httpClientGet(loginUrl, headerMap);
					headerMap.put("Cookie", MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId="));
					String json = memberAirlineParamJSON.replaceAll("%depCode%", this.getJobDetail().getDepCode())
							.replaceAll("%desCode%", this.getJobDetail().getDesCode())
							.replaceAll("%depDate%", this.getJobDetail().getDepDate());
					postMap.put("AirlineParamJSON", json);
					logger.info(this.getJobDetail().toStr() + ", 获取登录 AirlineParamJSON:" + json);
					while(true) {
						if(this.isTimeout()) return ;
						File file = new File("resource/antiVc/img/3uCode" + this.getTimerJob().getTimerJobKey() + ".jpg");
						String fileFullName = file.getCanonicalPath();
						MyHttpClientUtil.download(yzmUrl, this.headerMap, fileFullName, this.getCrawlExt().getOneWaitTime());
						logger.info(this.getJobDetail().toStr() + ", 下载验证码完成!");
						String code = MyYzm.antiV3UCode(fileFullName).trim();
						logger.info(this.getJobDetail().toStr() + ", 验证码:" + code);

						postMap.put("ValidateCode", code);
						httpResult = MyHttpClientUtil.httpClient(loginValidUrl, MyHttpClientUtil.httpPost, postMap, headerMap);
						logger.info(this.getJobDetail().toStr() + ", 请求验证码返回:" + httpResult);
						if(StringUtils.isEmpty(httpResult)) {
							MyThreadUtils.sleep(sleepTime);
							continue ;
						}
						break ;
						//						Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
						//						Map<String, Object> loginModel = (Map<String, Object> )resultMap.get("LoginModel");
						//						if("1".equals(loginModel.get("MemberID").toString())) break ;
					}
				} else if(CrawlerType.B2C3UCardPageType.equals(this.getPageType())) {
					Map<String, String> jsonMap = this.getTimerJob().getParamMap();
					postMap.put("CardNO", jsonMap.get("CardNO"));
					MyHttpClientResultVo httpVo = MyHttpClientUtil.httpClientGet(loginUrl, headerMap);
					headerMap.put("Cookie", MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId="));
					httpResult = MyHttpClientUtil.httpClient(cardParamInfoUrl, MyHttpClientUtil.httpPost, postMap, headerMap);
					Map<String, Object> cardParamModelMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
					String cardParamModel = cardParamModelMap.get("CardParamModel").toString().replaceAll("\\\\", "");
					String json = cardAirlineParamJSON.replaceAll("%depCode%", this.getJobDetail().getDepCode())
							.replaceAll("%desCode%", this.getJobDetail().getDesCode())
							.replaceAll("%depDate%", this.getJobDetail().getDepDate())
							.replaceAll("%cardParamModel%", cardParamModel);
					postMap.put("AirlineParamJSON", json);
					logger.info(this.getJobDetail().toStr() + ", 获取金卡 AirlineParamJSON:" + json);
					cardFlag = MyJsonTransformUtil.readValue(cardParamModel, Map.class).get("CardFlag").toString();
				} else {
					String json = airlineParamJSON.replaceAll("%depCode%", this.getJobDetail().getDepCode())
							.replaceAll("%desCode%", this.getJobDetail().getDesCode())
							.replaceAll("%depDate%", this.getJobDetail().getDepDate());
					postMap.put("AirlineParamJSON", json);
					logger.info(this.getJobDetail().toStr() + ", 获取普通 AirlineParamJSON:" + json);
				}

				httpResult = this.httpProxyPost(flightUrl, postMap, "html");
				if(StringUtils.isEmpty(httpResult)) {
					logger.info(this.getJobDetail().toStr() + ", 登录获取 PassKey 返回[null]");
					return ;
				}
				Matcher matcher = pattern.matcher(httpResult);
				if(!matcher.find()) {
					logger.info(this.getJobDetail().toStr() + ", 登录获取 PassKey 返回:" + httpResult);
					return ;
				}
				String passKeyInfo = matcher.group(1);
				logger.info(this.getJobDetail().toStr() + ", 获取 PassKey 成功!" + passKeyInfo);
				Map<String, Object> resultMap = MyJsonTransformUtil.readValue(passKeyInfo, Map.class);
				headerMap.remove("Cookie");
				paramMap = new HashMap<String, Object>();
				paramMap.put("RouteIndex", "1");
				paramMap.put("AVType", resultMap.get("AVType").toString());
				paramMap.put("PassKey", resultMap.get("PassKey").toString());
				paramMap.put("BuyerType", resultMap.get("BuyerType").toString());
				paramMap.put("AirlineType", resultMap.get("AirlineType").toString());
				paramMap.put("IsFixedCabin", resultMap.get("IsFixedCabin").toString());
				if(CrawlerType.B2C3UCardPageType.equals(this.getPageType())) paramMap.put("CardFlag", cardFlag);

				this.putBaseJobParamMap(paramMap);
				logger.info(this.getJobDetail().toStr() + ", 登录获取 PassKey 成功!");
			} catch(Exception e) {
				logger.error(this.getJobDetail().toStr() + ", 请求 httpResult 登录获取 PassKey 异常:" + httpResult, e);
			}
		}
	}
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("剩余(\\d+)张票");
		Matcher matcher = pattern.matcher("剩余2张票");
		if(matcher.find()) {
			System.out.println(matcher.group(1));
		}
	}
}