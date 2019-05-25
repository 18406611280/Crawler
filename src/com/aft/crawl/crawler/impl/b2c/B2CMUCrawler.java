package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.b2c.CrawlResultB2CRt;
import com.aft.crawl.result.vo.common.FilghtRule;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 东方航空 
 */
public class B2CMUCrawler extends Crawler {

	private final static String queryUrl = "http://www.ceair.com/otabooking/flight-search!doFlightSearch.shtml";
	
	private final static String owParams = "{\"tripType\":\"OW\",\"adtCount\":%adtCount%,\"chdCount\":0,\"infCount\":0,\"currency\":\"CNY\",\"sortType\":\"a\",\"segmentList\":[{\"deptCd\":\"%depCode%\",\"arrCd\":\"%desCode%\",\"deptDt\":\"%depDate%\"}],\"sortExec\":\"a\",\"page\":\"0\",\"inter\":0}";
	
	private final static String rtParams = "{\"tripType\":\"RT\",\"adtCount\":1,\"chdCount\":0,\"infCount\":0,\"currency\":\"CNY\",\"sortType\":\"a\",\"segmentList\":[{\"deptCd\":\"%depCode%\",\"arrCd\":\"%desCode%\",\"deptDt\":\"%depDate%\"},{\"deptCd\":\"%desCode%\",\"arrCd\":\"%depCode%\",\"deptDt\":\"%backDate%\"}],\"sortExec\":\"a\",\"page\":\"0\"}";
	
	private final static String interRtParams = "{\"tripType\":\"RT\",\"adtCount\":%adtCount%,\"chdCount\":0,\"infCount\":0,\"currency\":\"CNY\",\"sortType\":\"a\",\"segmentList\":[{\"deptCd\":\"%depCode%\",\"arrCd\":\"%desCode%\",\"deptDt\":\"%depDate%\"}, {\"deptCd\":\"%desCode%\",\"arrCd\":\"%depCode%\",\"deptDt\":\"%backDate%\"}],\"sortExec\":\"a\",\"page\":\"0\",\"inter\":0}";
	private static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
	
	private String dep;
	private String des;
	private String depDate;
	private String backDate;
	private final HashMap<String, String> Refund = new HashMap<>();
	private final HashMap<String, JSONObject> flightMap = new HashMap<>();
	public B2CMUCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object fscKey = mapResult.get("fscKey");
			if("".equals(fscKey) || null == mapResult.get("flightInfo")) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			List<Map<String, Object>> flightInfos = (List<Map<String, Object>>)mapResult.get("flightInfo");
			List<Map<String, Object>> priceProducts = (List<Map<String, Object>>)mapResult.get("searchProduct");
		    JSONObject resultJson = JSONObject.parseObject(httpResult);
		    JSONArray flights = resultJson.getJSONArray("flightInfo");
		    int size = flights.size();
			for (int s=0;s<size;s++) {
				JSONObject fli = flights.getJSONObject(s);
				String index = fli.getString("index");
				flightMap.put(index,fli);
			}
			

			if(!this.getPageType().equals(CrawlerType.B2CMURTPageType)) {	// 不是国内往返的都这样处理
				if(StringUtils.isEmpty(this.getJobDetail().getBackDate())) {	// 单程
					if(this.getPageType().equals(CrawlerType.B2CMUPageType)
							|| this.getPageType().equals(CrawlerType.B2CMUMemberPageType)
							|| this.getPageType().equals(CrawlerType.B2CMU_KNPageType)
							|| this.getPageType().equals(CrawlerType.B2CKNPageType)
							|| this.getPageType().equals(CrawlerType.B2CKNAdtGt9PageType)) {	// 国内
						crawlResults = this.domesticOWFilght(crawlResults, flightInfos,priceProducts);
					}else if(this.getPageType().equals(CrawlerType.B2CMUInterPageType)
							|| this.getPageType().equals(CrawlerType.B2CMUInterHdPageType)) {		// 国际单程
						crawlResults = this.interOWFilght(crawlResults,httpResult);
					}
				}else {
					if(this.getPageType().equals(CrawlerType.B2CMURTInterPageType)
							|| this.getPageType().equals(CrawlerType.B2CMURTInterHdPageType)) {	// 国际往返
						crawlResults = this.interRTFilght(crawlResults, httpResult);
					}
				}
			} else if(this.getPageType().equals(CrawlerType.B2CMURTPageType)) {// 国内往返
//				crawlResults = this.domesticRTFilght(crawlResults,productUnits);
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	/**
	 * 国际往返
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> interRTFilght(List<CrawlResultBase> crawlResults, String result){
		FlightData flightData = null;
		JSONObject resultObj = JSONObject.parseObject(result);
		JSONArray searchProduct = resultObj.getJSONArray("searchProduct");
		int priceSize = searchProduct.size();
		for(int i=0; i<priceSize; i++){
			flightData = new FlightData(this.getJobDetail(), "RT", dep, des, depDate, backDate);
			JSONObject priceCode = searchProduct.getJSONObject(i);
			String productName = priceCode.getString("productName");//舱位
			if(productName.contains("头等舱") || productName.contains("多舱位")) {
				continue;
			}
			String ticketPrice = priceCode.getString("salePrice");//票面价
			String referenceTax = priceCode.getString("referenceTax");//税费
			BigDecimal tax = new BigDecimal(0);
			if(referenceTax!=null) {
				tax = new BigDecimal(referenceTax);
			}
			flightData.setAirlineCode("MU");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(new Date());
			flightData.setCreateTime(date);
			List<FlightPrice> prices = new ArrayList<FlightPrice>();
			FlightPrice price = new FlightPrice();
			price.setCurrency("CNY");
			price.setEquivCurrency("CNY");
			price.setFare(ticketPrice);
			price.setEquivFare(ticketPrice);
			price.setTax(tax.toString());
			price.setEquivTax(tax.toString());
			price.setPassengerType("ADT");
			prices.add(price);
			flightData.setPrices(prices);
			
			String refund = Refund.get(ticketPrice);
			JSONObject ref = JSONObject.parseArray(refund).getJSONObject(0);
			JSONObject adtRef = ref.getJSONArray("formItemList").getJSONObject(0);
			JSONObject ruleViewMap = adtRef.getJSONObject("ruleViewMap");
			String bfREFUND = ruleViewMap.getJSONObject("REFUND").getString("bf").replace("<span>", "").replace("</span>", "");//全部退票
			String afREFUND = ruleViewMap.getJSONObject("REFUND").getString("af").replace("<span>", "").replace("</span>", "");//部分退票
			String bfRESCH = ruleViewMap.getJSONObject("RESCH").getString("bf").replace("<span>", "").replace("</span>", "");//全部更改
			String afRESCH = ruleViewMap.getJSONObject("RESCH").getString("af").replace("<span>", "").replace("</span>", "");//部分更改
			String bfCHANGE = ruleViewMap.getJSONObject("CHANGE").getString("bf").replace("<span>", "").replace("</span>", "");//全部签转
			String afCHANGE = ruleViewMap.getJSONObject("CHANGE").getString("af").replace("<span>", "").replace("</span>", "");//部分签转
			
			String Refund = "退票收费[全部未使用:"+bfREFUND+",部分使用:"+afREFUND+"]";
			String Resch = "更改收费[全部未使用:"+bfRESCH+",部分使用:"+afRESCH+"]";
			String Change = "签转收费[全部未使用:"+bfCHANGE+",部分使用:"+afCHANGE+"]";
			List<FilghtRule> ruleList = new ArrayList<FilghtRule>();
			FilghtRule rule = new FilghtRule();
			rule.setWay(1);
			rule.setRefund(Refund+";"+Resch+";"+Change);
			ruleList.add(rule);
			flightData.setRule(ruleList);
			
			String GroupIndex = priceCode.getString("productGroupIndex");// 179-176/177-184
			JSONObject cabin = priceCode.getJSONObject("cabin");
			String cabinCode = cabin.getString("cabinCode");// B-R/L-B
			String cabinNum = cabin.getString("cabinStatus");// 9-5/9-9
			if(cabinNum==null) continue;
			String[] groupIndexs = GroupIndex.split("/");
			String[] cabins = cabinCode.split("/");
			String[] cabinNos = cabinNum.split("/");
			int g =0;
			for(String groupIndex :groupIndexs) {
				String[] indexs = groupIndex.split("-");
				String[] cabinCodes = cabins[g].split("-");
				String[] cabinNums = cabinNos[g].split("-");
				List<FlightSegment> flights = new ArrayList<FlightSegment>();
				int j = 0;
				for(String flightIndex :indexs){
					JSONObject flightInfo = flightMap.get(flightIndex);
					String Cabin = cabinCodes[j];
					String cabinNo = cabinNums[j];
					Integer cabinNumber;
					if(cabinNo.equals("A")) {
						cabinNumber = 9;
					}else {
						cabinNumber = Integer.valueOf(cabinNums[j]);
					}
					String depCode = flightInfo.getJSONObject("departAirport").getString("cityCode");
					String desCode = flightInfo.getJSONObject("arrivalAirport").getString("cityCode");
					String flightNo = flightInfo.getString("flightNo");
					String isCodeShareAirline = flightInfo.getString("isCodeShareAirline");//是否共享
					if("true".equals(isCodeShareAirline)) isCodeShareAirline = "Y";
					else isCodeShareAirline = "N";
					String airlineCode = flightNo.substring(0, 2);
					String departDateTime = flightInfo.getString("departDateTime");
					String arrivalDateTime = flightInfo.getString("arrivalDateTime");
					FlightSegment fliment = new FlightSegment();
					fliment.setTripNo(j+1);
					fliment.setCabin(Cabin);
					fliment.setCabinCount(String.valueOf(cabinNumber));
					fliment.setFlightNumber(flightNo);
					fliment.setAirlineCode(airlineCode);
					fliment.setDepAirport(depCode);
					fliment.setArrAirport(desCode);
					fliment.setDepTime(departDateTime+":00");
					fliment.setArrTime(arrivalDateTime+":00");
					fliment.setCodeShare(isCodeShareAirline);
					flights.add(fliment);
					j++;
				}
				if(g==0) {
					flightData.setFromSegments(flights);
				}else {
					flightData.setRetSegments(flights);
				}
				g++;
			}
			crawlResults.add(flightData);
		}
		return crawlResults;
	}
	/**
	 * 国际单程
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> interOWFilght(List<CrawlResultBase> crawlResults, String result) throws Exception{
		CrawlResultInter b2c = null;
		JSONObject resultObj = JSONObject.parseObject(result);
		JSONArray searchProduct = resultObj.getJSONArray("searchProduct");
		int priceSize = searchProduct.size();
		for(int i=0; i<priceSize; i++){
			b2c = new CrawlResultInter(this.getJobDetail(), dep, des, depDate);
			JSONObject priceCode = searchProduct.getJSONObject(i);
			String ticketPrice = priceCode.getString("salePrice");//票面价
			String referenceTax = priceCode.getString("referenceTax");//税费
			BigDecimal tax = new BigDecimal(0);
			if(referenceTax!=null) {
				tax = new BigDecimal(referenceTax);
			}
			b2c.setRouteType("OW");
			b2c.setTicketPrice(new BigDecimal(ticketPrice));
			b2c.setTaxFee(tax);
			String refund = Refund.get(ticketPrice);
			JSONObject ref = JSONObject.parseArray(refund).getJSONObject(0);
			JSONObject adtRef = ref.getJSONArray("formItemList").getJSONObject(0);
			JSONObject ruleViewMap = adtRef.getJSONObject("ruleViewMap");
			String bfREFUND = ruleViewMap.getJSONObject("REFUND").getString("bf").replace("<span>", "").replace("</span>", "");//全部退票
			String afREFUND = ruleViewMap.getJSONObject("REFUND").getString("af").replace("<span>", "").replace("</span>", "");//部分退票
			String bfRESCH = ruleViewMap.getJSONObject("RESCH").getString("bf").replace("<span>", "").replace("</span>", "");//全部更改
			String afRESCH = ruleViewMap.getJSONObject("RESCH").getString("af").replace("<span>", "").replace("</span>", "");//部分更改
			String bfCHANGE = ruleViewMap.getJSONObject("CHANGE").getString("bf").replace("<span>", "").replace("</span>", "");//全部签转
			String afCHANGE = ruleViewMap.getJSONObject("CHANGE").getString("af").replace("<span>", "").replace("</span>", "");//部分签转
			
			String Refund = "退票收费[全部未使用:"+bfREFUND+",部分使用:"+afREFUND+"]";
			String Resch = "更改收费[全部未使用:"+bfRESCH+",部分使用:"+afRESCH+"]";
			String Change = "签转收费[全部未使用:"+bfCHANGE+",部分使用:"+afCHANGE+"]";
			b2c.setQianZhuanInfo(Change);
			b2c.setChangeInfo(Resch);
			b2c.setRefundInfo(Refund);
			String groupIndex = priceCode.getString("productGroupIndex");
			JSONObject cabin = priceCode.getJSONObject("cabin");
			String cabinCode = cabin.getString("cabinCode");
			String cabinNum = cabin.getString("cabinStatus");
			if(cabinNum==null) continue;
			String[] indexs = groupIndex.split("-");
			String[] cabinCodes = cabinCode.split("-");
			String[] cabinNums = cabinNum.split("-");
			List<CrawlResultInterTrip> flightTrips = new ArrayList<CrawlResultInterTrip>();
			int j = 0;
			for(String flightIndex :indexs){
				JSONObject flightInfo = flightMap.get(flightIndex);
				String Cabin = cabinCodes[j];
				String cabinNo = cabinNums[j];
				Integer cabinNumber;
				if(cabinNo.equals("A")) {
					cabinNumber = 9;
				}else {
					cabinNumber = Integer.valueOf(cabinNums[j]);
				}
				String depCode = flightInfo.getJSONObject("departAirport").getString("cityCode");
				String desCode = flightInfo.getJSONObject("arrivalAirport").getString("cityCode");
				String flightNo = flightInfo.getString("flightNo");
				String isCodeShareAirline = flightInfo.getString("isCodeShareAirline");//是否共享
				if("true".equals(isCodeShareAirline)) isCodeShareAirline = "Y";
				else isCodeShareAirline = "N";
				String airlineCode = flightNo.substring(0, 2);
				String departDateTime = flightInfo.getString("departDateTime");
				String arrivalDateTime = flightInfo.getString("arrivalDateTime");
				String depDate = departDateTime.substring(0, 10);
				String desDate = arrivalDateTime.substring(0, 10);
				String depTime = departDateTime.substring(11, 16);
				String desTime = arrivalDateTime.substring(11, 16);
				CrawlResultInterTrip flightTrip = new CrawlResultInterTrip(airlineCode, flightNo, depCode, desCode, groupIndex, Cabin, j+1, 1);
				flightTrip.setDepDate(depDate);
				flightTrip.setDesDate(desDate);
				flightTrip.setDepTime(depTime);
				flightTrip.setDesTime(desTime);
				flightTrip.setRemainSite(cabinNumber);
				flightTrip.setShareFlight(isCodeShareAirline);
				flightTrips.add(flightTrip);
			}
			b2c.setFlightTrips(flightTrips);
			crawlResults.add(b2c);
		}
		return crawlResults;
	}
	/**
	 * 国内单程
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> domesticOWFilght(List<CrawlResultBase> crawlResults,List<Map<String, Object>> flightInfos,List<Map<String, Object>> priceProducts){
		CrawlResultB2C crawlResult = null;
		for(Map<String, Object> priceMap : priceProducts) {
			    String groupIndex = (String)priceMap.get("productGroupIndex");
			    if(groupIndex.contains("-")) continue;//不要中转
			    Map<String, Object> flightInfo =null;
			    for (Map<String, Object> map : flightInfos) {
			    	if(Integer.parseInt(groupIndex)==(Integer)map.get("index")){
			    		flightInfo=map;
			    		break;
			    	}
				}
				double SalePrice = (double)priceMap.get("salePrice");
				String type = (String)priceMap.get("type");
				String productName = (String)priceMap.get("productName");
				Map<String, Object> cabinObj = (Map<String, Object>)priceMap.get("cabin");
				String cabin = (String)cabinObj.get("cabinCode");
				// 剩余座位数
				String cabinNum = (String)cabinObj.get("cabinStatus").toString().trim();
				int remainSite = CrawlerUtil.getCabinAmount(cabinNum);
				// 票面价
				BigDecimal ticketPrice = BigDecimal.valueOf(SalePrice);
				BigDecimal salePrice = ticketPrice;
				
				String depTime = (String)flightInfo.get("departDateTime");
				String depDate = depTime.substring(0,10);
				String desTime = (String)flightInfo.get("arrivalDateTime");
				String fltNo = (String)flightInfo.get("flightNo");
				String airlineCode = fltNo.substring(0,2);
				boolean isCodeShareAirline = (boolean)flightInfo.get("isCodeShareAirline");
				String Share;
				if(isCodeShareAirline){
					Map<String, Object> operatingAirline = (Map<String, Object>)flightInfo.get("operatingAirline");
					Share = (String)operatingAirline.get("flightNumber");
					if(!Share.contains("FM")) continue;
				}else{
					Share = "N";
				}
				Map<String, Object> departAirport = (Map<String, Object>)flightInfo.get("departAirport");
				String dep = (String)departAirport.get("code");
				Map<String, Object> arrivalAirport = (Map<String, Object>)flightInfo.get("arrivalAirport");
				String des = (String)arrivalAirport.get("code");
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, Share, dep, des, depDate, cabin);
				crawlResult.setDepTime(depTime.substring(11));	// 出发时间
				crawlResult.setDesTime(desTime.substring(11));	// 到达时间
				
				crawlResult.setCabin(cabin);
				
				// 剩余座位数
				crawlResult.setRemainSite(remainSite);
				crawlResult.setTemp(type);
				crawlResult.setType(productName);
				
				// 价格
				crawlResult.setTicketPrice(ticketPrice);
				crawlResult.setSalePrice(salePrice);
				crawlResults.add(crawlResult);
				
		}
		return crawlResults;
	}
	/**
	 * 国内往返
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> domesticRTFilght(List<CrawlResultBase> crawlResults,List<Map<String, Object>> productUnits){
		List<Map<String, Object>> depList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> desList = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> productUnitMap : productUnits) {
			List<Map<String, Object>> oriDestOptions = (List<Map<String, Object>>)productUnitMap.get("oriDestOption");
			
			Map<String, Object> oriDestOptionMap = oriDestOptions.get(0);
			List<Map<String, Object>> flights = (List<Map<String, Object>>)oriDestOptionMap.get("flights");
			if(flights.size() >= 2) break ;
			Map<String, Object> flightsMap = flights.get(0);
			
			// 出发/到达
			String depCode = ((Map<String, Object>)flightsMap.get("departureAirport")).get("code").toString();
			if(this.getJobDetail().getDepCode().equals(depCode)) depList.add(productUnitMap);
			else desList.add(productUnitMap);
		}
		CrawlResultB2CRt crawlResultB2CRt = null;
		for(Map<String, Object> depMap : depList) {
			for(Map<String, Object> desMap : desList) {
				Map<String, Object> productInfo = (Map<String, Object>)depMap.get("productInfo");
				String productName = (String)productInfo.get("productName");
				
				// 价格
				String currency = null;
				BigDecimal salePrice = null;
				List<Map<String, Object>> fareInfoViews = (List<Map<String, Object>>)depMap.get("fareInfoView");
				for(Map<String, Object> fareInfoView : fareInfoViews) {	// 只要成人的
					if(!"ADT".equals(fareInfoView.get("paxType").toString().trim())) continue ;
					
					Map<String, Object> fareMap = (Map<String, Object>)fareInfoView.get("fare");
					currency = fareMap.get("currencyCode").toString().trim();
					salePrice = new BigDecimal(fareMap.get("salePrice").toString().trim()).setScale(0);
					break ;
				}
				
				List<Map<String, Object>> oriDestOptions = (List<Map<String, Object>>)depMap.get("oriDestOption");
				Map<String, Object> oriDestOptionMap = oriDestOptions.get(0);
				List<Map<String, Object>> flights = (List<Map<String, Object>>)oriDestOptionMap.get("flights");
				if(flights.size() >= 2) break ;
				Map<String, Object> flightsMap = flights.get(0);
				
				// 航班号, 航司
				String fltNo = flightsMap.get("flightNumber").toString().trim().toUpperCase();
				String airlineCode = fltNo.substring(0, 2);
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				Object codeShareAirline = flightsMap.get("isCodeShareAirline");
				if(null != codeShareAirline && Boolean.valueOf(codeShareAirline.toString())) shareFlight = "Y";
				
				Map<String, Object> bookingClassAvailMap = (Map<String, Object>)flightsMap.get("bookingClassAvail");
				String cabin = bookingClassAvailMap.get("cabinCode").toString().trim();
				
				// 剩余座位数
				String remainSiteStr = bookingClassAvailMap.get("cabinStatusCode").toString().trim();
				int remainSite = CrawlerUtil.getCabinAmount(remainSiteStr);
				
				// 出发,到达时间
				String depTime = flightsMap.get("departureDateTime").toString().trim().substring(11);	// 2015-07-11 21:15
				String desTime = flightsMap.get("arrivalDateTime").toString().trim().substring(11);
				
				// 出发/到达
				String depCode = ((Map<String, Object>)flightsMap.get("departureAirport")).get("code").toString();
				String desCode = ((Map<String, Object>)flightsMap.get("arrivalAirport")).get("code").toString();
				String depDate = flightsMap.get("departureDateTime").toString().substring(0, 10);
				crawlResultB2CRt = new CrawlResultB2CRt(this.getJobDetail(), depCode, desCode, depDate);
				crawlResultB2CRt.setDepTime(depTime);	// 出发时间
				crawlResultB2CRt.setDesTime(desTime);	// 到达时间
				crawlResultB2CRt.setFltNo(fltNo);
				crawlResultB2CRt.setAirlineCode(airlineCode);
				crawlResultB2CRt.setCabin(cabin);
				crawlResultB2CRt.setRemainSite(remainSite);	// 剩余座位数
				// 价格
				crawlResultB2CRt.setCurrency(currency);
				crawlResultB2CRt.setTicketPrice(salePrice);
				crawlResultB2CRt.setShareFlight(shareFlight);
				crawlResultB2CRt.setType(productName);
				
				
				
				productInfo = (Map<String, Object>)desMap.get("productInfo");
				productName = (String)productInfo.get("productName");
				
				// 价格
				currency = null;
				salePrice = null;
				fareInfoViews = (List<Map<String, Object>>)desMap.get("fareInfoView");
				for(Map<String, Object> fareInfoView : fareInfoViews) {	// 只要成人的
					if(!"ADT".equals(fareInfoView.get("paxType").toString().trim())) continue ;
					
					Map<String, Object> fareMap = (Map<String, Object>)fareInfoView.get("fare");
					currency = fareMap.get("currencyCode").toString().trim();
					salePrice = new BigDecimal(fareMap.get("salePrice").toString().trim()).setScale(0);
					break ;
				}
				
				oriDestOptions = (List<Map<String, Object>>)desMap.get("oriDestOption");
				oriDestOptionMap = oriDestOptions.get(0);
				flights = (List<Map<String, Object>>)oriDestOptionMap.get("flights");
				if(flights.size() >= 2) break ;
				flightsMap = flights.get(0);
				
				// 航班号, 航司
				fltNo = flightsMap.get("flightNumber").toString().trim().toUpperCase();
				airlineCode = fltNo.substring(0, 2);
				
				// 判断共享
				shareFlight = this.getShareFlight(airlineCode);
				codeShareAirline = flightsMap.get("isCodeShareAirline");
				if(null != codeShareAirline && Boolean.valueOf(codeShareAirline.toString())) shareFlight = "Y";
				
				bookingClassAvailMap = (Map<String, Object>)flightsMap.get("bookingClassAvail");
				cabin = bookingClassAvailMap.get("cabinCode").toString().trim();
				
				// 剩余座位数
				remainSiteStr = bookingClassAvailMap.get("cabinStatusCode").toString().trim();
				remainSite = CrawlerUtil.getCabinAmount(remainSiteStr);
				
				// 出发,到达时间
				depTime = flightsMap.get("departureDateTime").toString().trim().substring(11);	// 2015-07-11 21:15
				desTime = flightsMap.get("arrivalDateTime").toString().trim().substring(11);
				
				// 出发/到达
				depCode = ((Map<String, Object>)flightsMap.get("departureAirport")).get("code").toString();
				desCode = ((Map<String, Object>)flightsMap.get("arrivalAirport")).get("code").toString();
				depDate = flightsMap.get("departureDateTime").toString().substring(0, 10);
				
				crawlResultB2CRt.setBackDate(depDate);
				crawlResultB2CRt.setBackDepTime(depTime);
				crawlResultB2CRt.setBackDesTime(desTime);
				crawlResultB2CRt.setBackFltNo(fltNo);
				crawlResultB2CRt.setBackCabin(cabin);
				crawlResultB2CRt.setBackRemainSite(remainSite);
				crawlResultB2CRt.setBackTicketPrice(salePrice);
				crawlResultB2CRt.setBackShareFlight(shareFlight);
				crawlResultB2CRt.setBackType(productName);
				crawlResults.add(crawlResultB2CRt);
			}
		}
		return crawlResults;
	}
	
	@SuppressWarnings("unchecked")
	public String getRuleInfo(Map<String, Object> ruleInfo){
		String crRule = "";
		try {
			String changeRuleJsonStr = String.valueOf(ruleInfo.get("changeRuleJsonStr"));
			String refundRuleJsonStr = String.valueOf(ruleInfo.get("refundRuleJsonStr"));
			String nonendRuleJsonStr = String.valueOf(ruleInfo.get("nonendRuleJsonStr"));
			if(changeRuleJsonStr!=null && !"".equals(changeRuleJsonStr) && !"null".equals(changeRuleJsonStr)){
				List<Map<String, Object>> changeRuleList = MyJsonTransformUtil.readValue(changeRuleJsonStr, List.class);
				if(changeRuleList!=null && !changeRuleList.isEmpty()&&changeRuleList.size()>0){
					Map<String, Object> changeRule = changeRuleList.get(0);
					String ruleStr = String.valueOf(changeRule.get("ruleStr"));
					List<Map<String, Object>> ruleList = MyJsonTransformUtil.readValue(ruleStr, List.class);
					crRule += "改期费:";
					for(int i=0;i<ruleList.size();i++){
						Map<String, Object> rule = ruleList.get(i);
						String fee = rule.get("fee").toString();
						if(i == 0){
							crRule += "起飞前:";
						}else{
							crRule += "起飞后:";
						}
						if("-1".equals(fee)){
							crRule +="不允许;";
						}else{
							crRule += fee +";";
						}
					}
				}
			}
			if(refundRuleJsonStr!=null && !"".equals(refundRuleJsonStr) && !"null".equals(refundRuleJsonStr)){
				List<Map<String, Object>> refundRuleList = MyJsonTransformUtil.readValue(refundRuleJsonStr, List.class);
				if(refundRuleList!=null && !refundRuleList.isEmpty()&&refundRuleList.size()>0){
					Map<String, Object> refundRule = refundRuleList.get(0);
					String ruleStr = String.valueOf(refundRule.get("ruleStr"));
					List<Map<String, Object>> ruleList = MyJsonTransformUtil.readValue(ruleStr, List.class);
					crRule += "退票费:";
					for(int i=0;i<ruleList.size();i++){
						Map<String, Object> rule = ruleList.get(i);
						String fee = rule.get("fee").toString();
						if(i == 0){
							crRule += "起飞前:";
						}else{
							crRule += "起飞后:";
						}
						if("-1".equals(fee)){
							crRule +="不允许;";
						}else{
							crRule += fee +";";
						}
					}
				}
			}
			if(nonendRuleJsonStr!=null && !"".equals(nonendRuleJsonStr) && !"null".equals(nonendRuleJsonStr)){
			List<Map<String, Object>> nonendRuleList = MyJsonTransformUtil.readValue(nonendRuleJsonStr, List.class);
				if(nonendRuleList!=null && !nonendRuleList.isEmpty()&&nonendRuleList.size()>0){
					Map<String, Object> nonendRule = nonendRuleList.get(0);
					String ruleStr = String.valueOf(nonendRule.get("ruleStr"));
					List<Map<String, Object>> ruleList = MyJsonTransformUtil.readValue(ruleStr, List.class);
					crRule += "签转费:";
					for(int i=0;i<ruleList.size();i++){
						Map<String, Object> rule = ruleList.get(i);
						String fee = rule.get("fee").toString();
						if(i == 0){
							crRule += "起飞前:";
						}else{
							crRule += "起飞后:";
						}
						if("-1".equals(fee)){
							crRule +="不允许;";
						}else{
							crRule += fee +";";
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("获取退改签规则异常!",e);
		}	
		return crRule;
	}
	
	@Override
	public String httpResult() throws Exception {
		return httpResult(queryUrl);
	}
	
//	@Override
//	@SuppressWarnings("unchecked")
//	protected boolean requestAgain(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
//		if(null == jsonObject) return false;
//		
//		Map<String, Object> mapResult = (Map<String, Object>)jsonObject;
//		return "SYS11207".equals(mapResult.get("resultCode"));
//	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		if(httpResult.contains("SYS11207") || httpResult.contains("RSD10006") || httpResult.contains("缓存拒绝被访问") ||httpResult.contains("您所在网络的IP请求过于频繁或非正常访问") || httpResult.contains("错误: 不能获取请求的 URL") ) return true;
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		if("other".equals(returnType))return false;
		Map<String, Object> mapResult = (Map<String, Object>)jsonObject;
		return ("RSD10004".equals(mapResult.get("resultCode")));
	}
	
	/**
	 * 获取请求内容
	 * 
	 * @param url
	 * @return
	 */
	public String httpResult(String url) {
		//东航国际
		if(this.getPageType().equals(CrawlerType.B2CMUInterPageType)){
			
//		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
			MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
			dep = this.getJobDetail().getDepCode();
			des = this.getJobDetail().getDesCode();
			depDate = this.getJobDetail().getDepDate();
			
			String Url = "http://www.ceair.com/otabooking/flight-search!doFlightSearch.shtml";
			String reUrl = "http://www.ceair.com/booking/"+dep.toLowerCase()+"-"+des.toLowerCase()+"-"+depDate.replace("-", "").substring(2)+"_CNY.html";
			Map<String, Object> headerMap1 = new HashMap<String, Object>();
			headerMap1.put("Accept", "application/json, text/javascript, */*; q=0.01");
			headerMap1.put("Accept-Encoding", "gzip, deflate");
			headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
			headerMap1.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0");
			headerMap1.put("X-Requested-With","XMLHttpRequest");
			headerMap1.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
			headerMap1.put("Connection", "keep-alive");
			headerMap1.put("Referer",reUrl);
			headerMap1.put("Host", "www.ceair.com");
			httpClientSessionVo.setHeaderMap(headerMap1);
			String param = "_="+UUID.randomUUID().toString().replaceAll("-","")+"&searchCond={\"adtCount\":1,\"chdCount\":0,\"infCount\":0,\"currency\":\"CNY\",\"tripType\":\"OW\",\"recommend\":false,\"reselect\":\"\",\"page\":\"0\",\"sortType\":\"a\",\"sortExec\":\"a\",\"segmentList\":[{\"deptCd\":\""+dep+"\",\"arrCd\":\""+des+"\",\"deptDt\":\""+depDate+"\",\"deptAirport\":\"\",\"arrAirport\":\"\",\"deptCdTxt\":\"\",\"arrCdTxt\":\"\",\"deptCityCode\":\""+dep+"\",\"arrCityCode\":\""+des+"\"}],\"version\":\"A.1.0\"}";
			MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo,Url,param,"other");
			String res = httpVo.getHttpResult();
			JSONObject obj = null;
			JSONArray searchProduct = null;
			int size = 0;
			String fscKey = null;
			try {
				obj = JSONObject.parseObject(res);
				if(obj ==null)return null;
				fscKey = obj.getString("fscKey");
				searchProduct = obj.getJSONArray("searchProduct");
				size = searchProduct.size();
			} catch (Exception e) {
			}
			HashMap<String,String> priceMap = new HashMap<String,String>();
			for(int s=0; s<size; s++) {
				JSONObject pro = searchProduct.getJSONObject(s);
				String salePrice = pro.getString("salePrice");
				String index = pro.getString("index");
				String snk = pro.getString("snk");
				String productCode = pro.getString("productCode");
				priceMap.put(salePrice, index+"|"+snk+"|"+productCode);
			}
			for (Map.Entry<String, String> entry : priceMap.entrySet()) {
				String price = entry.getKey();
				String value = entry.getValue();
				String[] values = value.split("\\|");
				String index = values[0];
				String snk = values[1];
				String productCode = values[2];
				String param1 = "ruleInfoConds=[{\"airPriceUnitIndex\":"+index+",\"mskey\":\""+fscKey+"\",\"snkey\":\""+snk+"\",\"productCode\":\""+productCode+"\",\"inter\":true}]";
				httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"http://www.ceair.com/common/asynchronous-process-comment!getOtaEiCommentRule.shtml",param1,"other");
				String result1= httpVo.getHttpResult();
				Refund.put(price, result1);
			}
			return res;
		}
		//东航国际往返
		if(this.getPageType().equals(CrawlerType.B2CMURTInterPageType)) {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
			MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
			dep = this.getJobDetail().getDepCode();
			des = this.getJobDetail().getDesCode();
			depDate = this.getJobDetail().getDepDate();
			backDate = this.getJobDetail().getBackDate();
			
			String depL = dep.toLowerCase();
			String desL = des.toLowerCase();
			String depDateL = depDate.replace("-", "").substring(2);
			String backDateL = backDate.replace("-", "").substring(2);
			
			String Url = "http://www.ceair.com/otabooking/flight-search!doFlightSearch.shtml";
			
			String reUrl = "http://www.ceair.com/booking/"+depL+"-"+desL+"-"+depDateL+"-"+desL+"-"+depL+"-"+backDateL+".html";
			Map<String, Object> headerMap1 = new HashMap<String, Object>();
			headerMap1.put("Accept", "application/json, text/javascript, */*; q=0.01");
			headerMap1.put("Accept-Encoding", "gzip, deflate");
			headerMap1.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
			headerMap1.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0");
			headerMap1.put("X-Requested-With","XMLHttpRequest");
			headerMap1.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
			headerMap1.put("Connection", "keep-alive");
			headerMap1.put("Referer",reUrl);
			headerMap1.put("Host", "www.ceair.com");
			httpClientSessionVo.setHeaderMap(headerMap1);
			String param = "_="+UUID.randomUUID().toString().replaceAll("-","")+"&searchCond={\"adtCount\":1,\"chdCount\":0,\"infCount\":0,\"currency\":\"CNY\",\"tripType\":\"RT\",\"recommend\":false,\"reselect\":\"\",\"page\":\"0\",\"sortType\":\"a\",\"sortExec\":\"a\",\"segmentList\":[{\"deptCd\":\""+dep+"\",\"arrCd\":\""+des+"\",\"deptDt\":\""+depDate+"\",\"deptAirport\":\"\",\"arrAirport\":\"\",\"deptCdTxt\":\"\",\"arrCdTxt\":\"\",\"deptCityCode\":\""+dep+"\",\"arrCityCode\":\""+des+"\"},{\"deptCd\":\""+des+"\",\"arrCd\":\""+dep+"\",\"deptDt\":\""+backDate+"\",\"deptAirport\":\"\",\"arrAirport\":\"\",\"deptCdTxt\":\"\",\"arrCdTxt\":\"\",\"deptCityCode\":\""+des+"\",\"arrCityCode\":\""+dep+"\"}],\"version\":\"A.1.0\"}";
			MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo,Url,param,"other");
			if(httpVo==null) {
				return null;
			}
			String res = httpVo.getHttpResult();
			JSONObject obj = null;
			JSONArray searchProduct = null;
			int size = 0;
			String fscKey = null;
			try {
				obj = JSONObject.parseObject(res);
				if(obj ==null)return null;
				fscKey = obj.getString("fscKey");
				searchProduct = obj.getJSONArray("searchProduct");
				size = searchProduct.size();
			} catch (Exception e) {
			}
			HashMap<String,String> priceMap = new HashMap<String,String>();
			for(int s=0; s<size; s++) {
				JSONObject pro = searchProduct.getJSONObject(s);
				String salePrice = pro.getString("salePrice");
				String index = pro.getString("index");
				String snk = pro.getString("snk");
				String productCode = pro.getString("productCode");
				priceMap.put(salePrice, index+"|"+snk+"|"+productCode);
			}
			for (Map.Entry<String, String> entry : priceMap.entrySet()) {
				String price = entry.getKey();
				String value = entry.getValue();
				String[] values = value.split("\\|");
				String index = values[0];
				String snk = values[1];
				String productCode = values[2];
				String param1 = "ruleInfoConds=[{\"airPriceUnitIndex\":"+index+",\"mskey\":\""+fscKey+"\",\"snkey\":\""+snk+"\",\"productCode\":\""+productCode+"\",\"inter\":true}]";
				httpVo = this.httpProxyResultVoPost(httpClientSessionVo,"http://www.ceair.com/common/asynchronous-process-comment!getOtaEiCommentRule.shtml",param1,"other");
				String result1= httpVo.getHttpResult();
				Refund.put(price, result1);
			}
			return res;
		}
		String searchCond = owParams;
		if(this.getPageType().equals(CrawlerType.B2CMURTPageType)) searchCond = rtParams.replaceAll("%backDate%", this.getJobDetail().getBackDate());
		if(StringUtils.isNotEmpty(this.getJobDetail().getBackDate())) searchCond = interRtParams.replaceAll("%backDate%", this.getJobDetail().getBackDate());
		
		String adtCount = this.getTimerJob().getParamMapValueByKey("adtCount");
		if(null == adtCount) adtCount = "1";
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		searchCond = searchCond.replaceAll("%depCode%",dep)
							.replaceAll("%desCode%", des)
							.replaceAll("%depDate%", depDate)
							.replaceAll("%adtCount%", adtCount);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("searchCond", searchCond);
		String result =  this.httpProxyPost(url, paramMap, "json");
		return result;
	}
	public static void main(String[] args) {
		String eiComment = "/otabooking/flight-ruleinfo!getRuleInfo.shtml?ruleSearchCond=[{\"airPriceUnitIndex\":\"0\",\"mskey\":\"OW:zh:CNY:a:/CAN:PAR:2017-05-26:,:NEWOTA\",\"snkey\":\"CAN1495755000000PAR1495796700000MU9302-MU569SCW_OD_MU_FFB-H8230.0\",\"travelPurpose\":\"\"}]";
		Pattern pattern = Pattern.compile("a:/(.+):,:NEWOTA");
		Matcher matcher = pattern.matcher(eiComment);
		if(matcher.find()){
			System.out.println(matcher.group(1));
		}
	}
}