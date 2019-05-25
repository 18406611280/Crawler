package com.aft.crawl.crawler.impl.b2c;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.eclipse.swt.internal.LONG;
import org.jsoup.nodes.Document;

import scala.collection.mutable.StringBuilder;
import sun.misc.BASE64Encoder;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.crawler.AtomicLock;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.model.CARequestModel;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.logger.CaAccountLog;
import com.aft.logger.MyCrawlerLogger;
import com.aft.utils.DESUtil;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 国航APP
 * @author chenminghong
 */
public class B2CCACrawler extends Crawler {

	String dep = null;
	String des = null;
	String depDate = null;
	private static final Object ableLook = new Object();
	private static volatile long firstTime = 0;
	private static volatile long lastTime;
	private static volatile boolean flag = false;
	private static volatile String ziYinNo = "";
	private static volatile String userId = "";
	private static volatile String infoId = "";
	Logger logger1 = Logger.getLogger(CaAccountLog.class);
	public B2CCACrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResultList();
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult) || httpResult.equals("") || httpResult==null) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		List<CrawlResultBase> crawlResult = null;
		if(this.getPageType().equals(CrawlerType.B2CCAPageType)){//国内
			crawlResult = getCAInner(crawlResults, httpResult);
		}
		if(this.getPageType().equals(CrawlerType.B2CCAInterPageType)){//国际
			crawlResult = getCAInter(crawlResults, httpResult);
		}
		return crawlResult;
	}

	private List<CrawlResultBase> getCAInter(List<CrawlResultBase> crawlResults, String httpResult) {
		try {
			FlightData flightData = null;
			JSONObject resultObj = JSONObject.parseObject(httpResult);
			JSONObject resp = resultObj.getJSONObject("resp");
			JSONObject Goto = resp.getJSONObject("goto");
			if(Goto==null) return crawlResults;
			JSONArray taxesFeesList = Goto.getJSONArray("taxesFeesList");
			int taxSize = taxesFeesList.size();
			BigDecimal tax = new BigDecimal(0);
			for(int i=0;i<taxSize;i++){
				String taxFeePrice = taxesFeesList.getJSONObject(i).getString("taxFeePrice");
				tax = tax.add(new BigDecimal(taxFeePrice));
			}
			JSONArray flightInfomationList = Goto.getJSONArray("flightInfomationList");
			int flightSize = flightInfomationList.size();
			for(int j=0; j<flightSize; j++){
				JSONObject flight = flightInfomationList.getJSONObject(j);
				flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				String cabinCount = flight.getString("ticketNum");//舱位数
				if(cabinCount.equals("")){
					cabinCount="9";
				}
				String cabin = flight.getString("lowClass");//舱位
				String ticketPrice = null;
				String memo = "";
				String vipPrice= flight.getString("vip_price");//会员价
				if(!"".equals(vipPrice) && vipPrice!=null){
					ticketPrice = vipPrice;
					memo="会员价";
				}else{
					ticketPrice = flight.getString("lowPrice");//票面价
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setAirlineCode("CA");//航司
				flightData.setMemo(memo);
				
				List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
				FlightPrice flightPrice = new FlightPrice();
				flightPrice.setPassengerType("ADT");
				flightPrice.setFare(ticketPrice);//票面价
				flightPrice.setTax(tax.toString());//税费
				flightPrice.setCurrency("CNY");//币种
				flightPrice.setEquivFare(ticketPrice);
				flightPrice.setEquivTax(tax.toString());
				flightPrice.setEquivCurrency("CNY");
				flightPriceList.add(flightPrice);
				flightData.setPrices(flightPriceList);
				
				List<FlightSegment> flightList = new ArrayList<FlightSegment>();
				JSONArray flightSegmentList = flight.getJSONArray("flightSegmentList");
				int flightListSize = flightSegmentList.size();
				for(int t=0; t<flightListSize; t++){
					FlightSegment flightSegment = new FlightSegment();
					JSONObject Flight = flightSegmentList.getJSONObject(t);
					flightSegment.setTripNo(t+1);
					flightSegment.setAirlineCode(Flight.getString("operatingAirline"));
					String shareFlight = this.getShareFlight(Flight.getString("operatingAirline"));//判断是否共享 
					flightSegment.setFlightNumber(Flight.getString("flightNo"));
					flightSegment.setDepAirport(Flight.getString("flightDep"));
					String DepDate = Flight.getString("flightDepdatePlan");
					String depTime = Flight.getString("flightDeptimePlan").substring(0, 8);
					flightSegment.setDepTime(DepDate+" "+depTime);
					String DesDate = Flight.getString("flightArrdatePlan");
					String desTime = Flight.getString("flightArrtimePlan").substring(0, 8);
					flightSegment.setArrAirport(Flight.getString("flightArr"));
					flightSegment.setArrTime(DesDate+" "+desTime);
					flightSegment.setCodeShare(shareFlight);
					flightSegment.setCabin(cabin);
					flightSegment.setCabinCount(cabinCount);
					flightSegment.setAircraftCode(Flight.getString("flightModel"));
					flightList.add(flightSegment);
				}
				flightData.setFromSegments(flightList);
				crawlResults.add(flightData);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:\r"+httpResult, e);
			throw e;
		}
		return crawlResults;
	}

	@Override
	protected boolean needToChangeIp(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		if (httpResult.contains("ERROR") || httpResult.contains("URL")) {
			return true;
		}
		return super.needToChangeIp(httpResult, document, jsonObject,returnType);

	}
	
	public List<CrawlResultBase> getCAInner(List<CrawlResultBase> crawlResults ,String httpResult){
		try {
			FlightData flightData = null;
			JSONObject resultObj = JSONObject.parseObject(httpResult);
			JSONObject resp = resultObj.getJSONObject("resp");
			JSONObject Goto = resp.getJSONObject("goto");
			if(Goto==null) return crawlResults;
			JSONArray taxesFeesList = Goto.getJSONArray("taxesFeesList");
			int taxSize = taxesFeesList.size();
			BigDecimal tax = new BigDecimal(0);
			for(int i=0;i<taxSize;i++){
				String taxFeePrice = taxesFeesList.getJSONObject(i).getString("taxFeePrice");
				tax = tax.add(new BigDecimal(taxFeePrice));
			}
			JSONArray flightInfomationList = Goto.getJSONArray("flightInfomationList");
			int flightSize = flightInfomationList.size();
			for(int j=0; j<flightSize; j++){
				JSONObject flight = flightInfomationList.getJSONObject(j);
				JSONArray flightSegmentList = flight.getJSONArray("flightSegmentList");
				if(flightSegmentList.size()>1) continue;
				flightData = new FlightData(this.getJobDetail(), "OW", dep, des, depDate);
				String cabinCount = flight.getString("ticketNum");//舱位数
				if(cabinCount.equals("")){
					cabinCount="9";
				}
				String cabin = flight.getString("lowClass");//舱位
				String ticketPrice = null;
				String memo = "";
				String vipPrice= flight.getString("vip_price");//会员价
				if(!"".equals(vipPrice) && vipPrice!=null){
					ticketPrice = vipPrice;
					memo="会员价";
				}else{
					ticketPrice = flight.getString("lowPrice");//票面价
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = sdf.format(new Date());
				flightData.setCreateTime(date);
				flightData.setAirlineCode("CA");//航司
				flightData.setMemo(memo);
				
				List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
				FlightPrice flightPrice = new FlightPrice();
				flightPrice.setPassengerType("ADT");
				flightPrice.setFare(ticketPrice);//票面价
				flightPrice.setTax(tax.toString());//税费
				flightPrice.setCurrency("CNY");//币种
				flightPrice.setEquivFare(ticketPrice);
				flightPrice.setEquivTax(tax.toString());
				flightPrice.setEquivCurrency("CNY");
				flightPriceList.add(flightPrice);
				flightData.setPrices(flightPriceList); 
				
				List<FlightSegment> flightList = new ArrayList<FlightSegment>();
				FlightSegment flightSegment = new FlightSegment();
				JSONObject Flight = flightSegmentList.getJSONObject(0);
				flightSegment.setTripNo(1);
				flightSegment.setAirlineCode(Flight.getString("operatingAirline"));
				String shareFlight = this.getShareFlight(Flight.getString("operatingAirline"));//判断是否共享 
				flightSegment.setFlightNumber(Flight.getString("flightNo"));
				String depCode = Flight.getString("flightDep");
				String desCode = Flight.getString("flightArr");
				if(!depCode.equals(this.dep) || !desCode.equals(this.des)) {
					continue;
				}
				flightSegment.setDepAirport(depCode);
				String DepDate = Flight.getString("flightDepdatePlan");
				String depTime = Flight.getString("flightDeptimePlan").substring(0, 8);
				flightSegment.setDepTime(DepDate+" "+depTime);
				String DesDate = Flight.getString("flightArrdatePlan");
				String desTime = Flight.getString("flightArrtimePlan").substring(0, 8);
				flightSegment.setArrAirport(desCode);
				flightSegment.setArrTime(DesDate+" "+desTime);
				flightSegment.setCodeShare(shareFlight);
				flightSegment.setCabin(cabin);
				flightSegment.setCabinCount(cabinCount);
				flightSegment.setAircraftCode(Flight.getString("flightModel"));
				flightList.add(flightSegment);
				flightData.setFromSegments(flightList);
				crawlResults.add(flightData);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:\r"+httpResult, e);
			throw e;
		}
		return crawlResults;
	}
	
	@SuppressWarnings("unchecked")
	public String httpResultList() throws Exception {
		if(firstTime ==0) {
			synchronized (B2CCACrawler.class) {
				if(firstTime ==0) {
					firstTime = System.currentTimeMillis();
				}
			}
		}
		lastTime = System.currentTimeMillis(); 
		if(lastTime - firstTime > 150000) {//25分钟获取一次
			String timeStamp1 = String.valueOf(System.currentTimeMillis());
			try {
				AtomicLock.tryLock(timeStamp1);
				ReentrantLock lock = new ReentrantLock();
				lock.lock();
				changeNo();
				firstTime = System.currentTimeMillis();
			}catch(Exception e){
				Thread.sleep(10000);
				return null;
			}finally {
				AtomicLock.unLock(timeStamp1);
			}
		}
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		
		///////////////////////////

		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Host", "m.airchina.com.cn:9061");
		headerMap.put("X-Requested-With", "XMLHttpRequest");
		headerMap.put("User-Agent","AirChina/5.10.0.4 (iPhone; iOS 9.3.2; Scale/2.00)/WLNativeAPI/6.3.0.0");
		headerMap.put("x-wl-clientlog-osversion", "9.3.2");
		headerMap.put("x-wl-clientlog-env", "iphone");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("x-wl-clientlog-appname", "AirChina");
		headerMap.put("Accept-Language","zh-Hans-CN");
		headerMap.put("Accept-Encoding","gzip, deflate");
		headerMap.put("x-wl-app-version","1.0");
		headerMap.put("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
		httpClientSessionVo.setHeaderMap(headerMap);
		
		Date currentTime = new Date();
		String desTimeStamp = String.valueOf(currentTime.getTime());
		String userInfo3 = DESUtil.strEnc(desTimeStamp, "DF", "48", "A5");//des加密
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String timeStamp = desTimeStamp.substring(0,10);
		String req = "{\n  \"airArrTransport\" : \"\",\n  \"basePrice\" : \"\",\n  \"airTransportflag\" : \"1\",\n  \"airTransportType\" : \"\",\n  \"classDesc\" : \"\",\n  \"IOSUSERSYSTEMDATE\" : \""+dateString+"-Asia\\/Shanghai (GMT+8) offset 28800\",\n  \"flag\" : \"0\",\n  \"inf\" : \"0\",\n  \"version\" : \"4\",\n  \"mileageFlag\" : \"0\",\n  \"date\" : \""+depDate+"\",\n  \"airDepTransport\" : \"\",\n  \"org\" : \""+dep+"\",\n  \"cabin\" : \"Economy\",\n  \"timestamp\" : \""+timeStamp+"\",\n  \"backDate\" : \"\",\n  \"searchId\" : \"\",\n  \"dst\" : \""+des+"\",\n  \"cnn\" : \"0\",\n  \"acePageReq\" : \"1\",\n  \"adt\" : \"1\",\n  \"crmPersonType\" : \"\"\n}";
		String Info2 = "a3b691ae7fe943da5e4ee83e0143819ba23ca34e384f3274022748516fb513d453E0551000";
//		String MD5UserInfo2 = encode(Info2);
		String subReq = req.substring(0,50);
//		String MD5UserInfo4 = encode(subReq);
		String token = "a3b691ae7fe943da5e4ee83e0143819ba23ca34e384f3274022748516fb513d4";
		String userInfo1 = "51000";
		String userInfo4 = "1FE3A25E78DFC6B931813CFFCD640F1B";//encode(subReq);
//		String userInfo2 = encode(Info2).toUpperCase();//"077B64C35BE047F3DFD7BF03C7A12938";//encode(Info2)
		String userInfo2 = "47850A67106FCEC9DD067F91B53F5822";//可变
		
		CARequestModel reqModel = new CARequestModel();
		reqModel.setSecureToken("AppSecureToken");
		reqModel.setUserInfo3(userInfo3);
		reqModel.setUserID(userId);//可变
		reqModel.setZiYinNo(ziYinNo);//可变
		reqModel.setReq(req.replaceAll("\"", "\\\""));
		reqModel.setMobileSysVer("9.3.2");
		reqModel.setUserInfo4(userInfo4);
		reqModel.setDeviceId("DE47B02A-8B52-4CCE-AAB6-9EC7CA9277D8.");//可变
		reqModel.setUserInfo1(userInfo1);
		reqModel.setCrmMemberId("1-M0KUGJS");
		reqModel.setDeviceType("iPhone");
		reqModel.setToken(token);
		reqModel.setAppVer("5.10.0");
		reqModel.setLang("zh_CN");
		reqModel.setUserInfo2(userInfo2);
		reqModel.setInfoID(infoId);//可变
		net.sf.json.JSONObject reqJson = net.sf.json.JSONObject.fromObject(reqModel);
		String reqJsonStr = "["+reqJson.toString()+"]";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("__wl_deviceCtx","A8mAT69tym89sBAA");
		paramMap.put("adapter","ACFlight");
		paramMap.put("compressResponse","true");
		paramMap.put("isAjaxRequest","true");
		paramMap.put("procedure","qryFlights");
		paramMap.put("parameters",reqJsonStr);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoPost(httpClientSessionVo, "https://m.airchina.com.cn:9061/worklight/apps/services/api/AirChina/iphone/query", paramMap, "other");
		if(httpVo==null) return null;
		String res2 = httpVo.getHttpResult();
		System.out.println(res2);
		String COOKIE = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "WL_PERSISTENT_COOKIE=");
		String JSESSIONID = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String NSC_xpslmjhiu = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "NSC_xpslmjhiu=");
		if("".equals(COOKIE) || "".equals(JSESSIONID) || "".equals(NSC_xpslmjhiu)){
			return null;
		}
		StringBuilder cookie = new StringBuilder();
		cookie.append(COOKIE).append(";").append(JSESSIONID).append(";").append(NSC_xpslmjhiu);
		String deviceId	 = "9848EC93-4C2B-4880-B898-33A9C442F2E5";
		String tracking_id = "41B6AB18-308C-4692-B9B1-497C4065A60F";
		String res = httpVo.getHttpResult();
		String Challenge = getValue("WL-Challenge-Data\"\\:\"", "\\+", res);
		String Realm = getRealm(Challenge);
		String WL_Instance_Id = getValue("WL-Instance-Id\"\\:\"", "\"", res);
		String Token = getValue("token\"\\:\"", "\"", res);
		String Authorization = "{\"wl_deviceNoProvisioningRealm\":{\"ID\":{\"app\":{\"id\":\"AirChina\",\"version\":\"1.0\"},\"device\":{\"environment\":\"iOS\",\"id\":\""+deviceId+"\",\"os\":\"8.1.2\",\"model\":\"iPhone\"},\"custom\":{},\"token\":\""+Token+"\"}},\"wl_authenticityRealm\":\""+Realm+"\"}";
		headerMap.put("WL-Instance-Id",WL_Instance_Id);
		headerMap.put("Cookie",cookie);
		headerMap.put("x-wl-clientlog-osversion","8.1.2");
		headerMap.put("x-wl-clientlog-env","iphone");
		headerMap.put("x-wl-clientlog-deviceId",deviceId);
		headerMap.put("x-wl-clientlog-model","iPhone7,2");
		headerMap.put("x-wl-analytics-tracking-id",tracking_id);
		headerMap.put("Authorization",Authorization);
		headerMap.put("Accept-Language","zh-Hans");
		MyHttpClientResultVo httpVo2 = this.httpProxyResultVoPost(httpClientSessionVo, "https://m.airchina.com.cn:9061/worklight/apps/services/api/AirChina/iphone/query", paramMap, "other");
		if(httpVo2==null) return null;
		String result = httpVo2.getHttpResult();
		if(result.contains("很抱歉目前无法为您提供服务") || result.contains("验证不通过") || result.contains("网络非常繁忙")) {
			String timeStamp1 = String.valueOf(System.currentTimeMillis());
			try {
//				AtomicLock.tryLock(timeStamp1);
				changeNo();
			}catch(Exception e){
				Thread.sleep(10000);
				return null;
			}finally {
//				AtomicLock.unLock(timeStamp1);
			}
		}
		return result;
	}
	
	private void changeNo() throws Exception {
		
		ArrayList accountList = MyDefaultProp.getCaAccountList();
		int accountSize = accountList.size();
		Random rs=new Random();
        int index=rs.nextInt(accountSize);
        String result = (String) accountList.get(index);
        String account = result.split("-")[0];
        String password = result.split("-")[1];
        
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String, Object> headerMap1 = new HashMap<String, Object>();
		headerMap1.put("Accept","*/*");
		headerMap1.put("Accept-Encoding","gzip, deflate");
		headerMap1.put("Accept-Language","zh-Hans");
		headerMap1.put("User-Agent","AirChina/5.15.1.0 (iPhone; iOS 8.1.2; Scale/2.00)/WLNativeAPI/6.3.0.0");
		headerMap1.put("X-Requested-With", "XMLHttpRequest");
		headerMap1.put("Content-Type","application/x-www-form-urlencoded; charset=utf-8");
		headerMap1.put("Host", "m.airchina.com.cn:9061");
		headerMap1.put("x-wl-clientlog-deviceId", "9848EC93-4C2B-4880-B898-33A9C442F2E5");
		headerMap1.put("x-wl-app-version","1.0");
		headerMap1.put("x-wl-clientlog-appname", "AirChina");
		headerMap1.put("x-wl-clientlog-appversion","1.0");
		headerMap1.put("x-wl-clientlog-env", "iphone");
		headerMap1.put("x-wl-analytics-tracking-id","8B167ACA-9B63-4D1F-B1F5-F6AEAACEFAB6");
		headerMap1.put("x-wl-clientlog-model","iPhone7,2");
		headerMap1.put("x-wl-clientlog-osversion", "8.1.2");
		headerMap1.put("x-wl-platform-version","6.3.0.0");
		headerMap1.put("Connection", "keep-alive");
//		headerMap1.put("Content-Length","1581");
//		headerMap1.put("X-Tingyun-Id","UiLVhboHIX8;c=2;r=136372411;u=93014766b8ef0b81ccab462459e17bda::C7EEB65BDA4F02C6");
		httpClientSessionVo.setHeaderMap(headerMap1);
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date newDate = new Date();
		String dateString1 = formatter1.format(newDate);
		String desTimeStamp1 = String.valueOf(newDate.getTime());
		String UserInfo3 = DESUtil.strEnc(desTimeStamp1, "DF", "48", "A5");//des加密
		String req1 = "{\n  \"userId\" : \"\",\n  \"IOSUSERSYSTEMDATE\" : \""+dateString1+"-Asia\\/Shanghai (GMT+8) offset 28800\"\n}";
		String subReq1 = req1.substring(0,50);
		String userInfo4 = encode(subReq1).toUpperCase();
		String reqJsonStr0 = "__wl_deviceCtx=ALLgh_5kqs1jtBAA&adapter=ACCommon&compressResponse=true&isAjaxRequest=true&parameters=%5B%7B%22secureToken%22%3A%22AppSecureToken%22%2C%22userInfo3%22%3A%22"+UserInfo3+"%22%2C%22userID%22%3A%22%22%2C%22ziYinNo%22%3A%22%22%2C%22req%22%3A%22%7B%5Cn%20%20%5C%22userId%5C%22%20%3A%20%5C%22%5C%22%2C%5Cn%20%20%5C%22IOSUSERSYSTEMDATE%5C%22%20%3A%20%5C%22"+dateString1+"-Asia%5C%5C%2FShanghai%20%28GMT%2B8%29%20offset%2028800%5C%22%5Cn%7D%22%2C%22mobileSysVer%22%3A%228.1.2%22%2C%22userInfo4%22%3A%22"+userInfo4+"%22%2C%22deviceId%22%3A%22DE47B02A-8B52-4CCE-AAB6-9EC7CA9277D8%22%2C%22userInfo1%22%3A%2251501%22%2C%22crmMemberId%22%3A%22%22%2C%22deviceType%22%3A%22iPhone%22%2C%22token%22%3A%2211111111%22%2C%22appVer%22%3A%225.15.1%22%2C%22deviceModel%22%3A%22iPhone7%2C2%22%2C%22lang%22%3A%22zh_CN%22%2C%22userInfo2%22%3A%22090F6C76DC1C0FC48BB418CC1F553E6B%22%2C%22infoID%22%3A%22%22%7D%5D&procedure=hasUpgrade";
		
		MyHttpClientResultVo httpVo1 = this.httpProxyResultVoPost(httpClientSessionVo, "https://m.airchina.com.cn:9061/worklight/apps/services/api/AirChina/iphone/query", reqJsonStr0, "other");
		if(httpVo1==null) return;
		String COOKIE1 = MyHttpClientUtil.getHeaderValue(httpVo1.getHeaders(), "Set-Cookie", "WL_PERSISTENT_COOKIE=");
		String JSESSIONID1 = MyHttpClientUtil.getHeaderValue(httpVo1.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String NSC_xpslmjhiu1 = MyHttpClientUtil.getHeaderValue(httpVo1.getHeaders(), "Set-Cookie", "NSC_xpslmjhiu=");
		if("".equals(COOKIE1) || "".equals(JSESSIONID1) || "".equals(NSC_xpslmjhiu1)){
			return;
		}
		StringBuilder cookie1 = new StringBuilder();
		cookie1.append(COOKIE1).append(";").append(JSESSIONID1).append(";").append(NSC_xpslmjhiu1);
		
		String deviceId1	 = "9848EC93-4C2B-4880-B898-33A9C442F2E5";
		String res1 = httpVo1.getHttpResult();
		String Challenge1 = getValue("WL-Challenge-Data\"\\:\"", "\\+", res1);
		String Realm1 = getRealm(Challenge1);
		String WL_Instance_Id1 = getValue("WL-Instance-Id\"\\:\"", "\"", res1);
		String Token1 = getValue("token\"\\:\"", "\"", res1);
		String Authorization1 = "{\"wl_deviceNoProvisioningRealm\":{\"ID\":{\"app\":{\"id\":\"AirChina\",\"version\":\"1.0\"},\"device\":{\"environment\":\"iOS\",\"id\":\""+deviceId1+"\",\"os\":\"8.1.2\",\"model\":\"iPhone\"},\"custom\":{},\"token\":\""+Token1+"\"}},\"wl_authenticityRealm\":\""+Realm1+"\"}";
		headerMap1.put("WL-Instance-Id",WL_Instance_Id1);
		headerMap1.put("Cookie",cookie1);
		headerMap1.put("Authorization",Authorization1);
		httpVo1 = this.httpProxyResultVoPost(httpClientSessionVo, "https://m.airchina.com.cn:9061/worklight/apps/services/api/AirChina/iphone/query", reqJsonStr0, "other");
		
		Date date2 = new Date();
		String dateString2 = formatter1.format(date2);
		String desTimeStamp2 = String.valueOf(date2.getTime());
		String User_Info3 = DESUtil.strEnc(desTimeStamp2, "DF", "48", "A5");//des加密
		String req2 = "{\n  \"loginName\" : \""+account+"\",\n  \"IOSUSERSYSTEMDATE\" : \"2019-02-14 15:50:19-Asia\\/Shanghai (GMT+8) offset 28800\",\n  \"version\" : \"1\",\n  \"mobileType\" : \"Iphone\",\n  \"cId\" : \"1949543342ea7f70abd5e84927b135c8\",\n  \"appVer\" : \"5.15.1\",\n  \"operType\" : \"0\",\n  \"key\" : \"\",\n  \"loginType\" : \"4\",\n  \"password\" : \"820314\",\n  \"sysVer\" : \"8.1.2\",\n  \"registerType\" : \"0\"\n}";
		String subReq2 = req2.substring(0,50);
		String user_Info4 = encode(subReq2).toUpperCase();
		String reqJsonStr1 = "[{\"secureToken\":\"AppSecureToken\",\"userInfo3\":\""+User_Info3+"\",\"userID\":\"\",\"ziYinNo\":\"\",\"req\":\"{\\n  \\\"loginName\\\" : \\\""+account+"\\\",\\n  \\\"IOSUSERSYSTEMDATE\\\" : \\\""+dateString2+"-Asia\\\\/Shanghai (GMT+8) offset 28800\\\",\\n  \\\"version\\\" : \\\"1\\\",\\n  \\\"mobileType\\\" : \\\"Iphone\\\",\\n  \\\"cId\\\" : \\\"1949543342ea7f70abd5e84927b135c8\\\",\\n  \\\"appVer\\\" : \\\"5.15.1\\\",\\n  \\\"operType\\\" : \\\"0\\\",\\n  \\\"key\\\" : \\\"\\\",\\n  \\\"loginType\\\" : \\\"4\\\",\\n  \\\"password\\\" : \\\""+password+"\\\",\\n  \\\"sysVer\\\" : \\\"8.1.2\\\",\\n  \\\"registerType\\\" : \\\"0\\\"\\n}\",\"mobileSysVer\":\"8.1.2\",\"userInfo4\":\""+user_Info4+"\",\"deviceId\":\"DE47B02A-8B52-4CCE-AAB6-9EC7CA9277D8\",\"userInfo1\":\"51501\",\"crmMemberId\":\"\",\"deviceType\":\"iPhone\",\"token\":\"a3b691ae7fe943da5e4ee83e0143819ba23ca34e384f3274022748516fb513d4\",\"appVer\":\"5.15.1\",\"deviceModel\":\"iPhone7,2\",\"lang\":\"zh_CN\",\"userInfo2\":\"BAE1E5A6A929E92B69C6C1648FC3017B\",\"infoID\":\"\"}]";
		
		Map<String, Object> paramMap1 = new HashMap<String, Object>();
		paramMap1.put("__wl_deviceCtx","ALLgh_5kqs1jtBAA");
		paramMap1.put("adapter","ACLogin");
		paramMap1.put("compressResponse","true");
		paramMap1.put("isAjaxRequest","true");
		paramMap1.put("procedure","login");
		paramMap1.put("parameters",reqJsonStr1);
		headerMap1.remove("Authorization");
		httpVo1 = this.httpProxyResultVoPost(httpClientSessionVo, "https://m.airchina.com.cn:9061/worklight/apps/services/api/AirChina/iphone/query", paramMap1, "other");
		
		String loginRes = httpVo1.getHttpResult();
		JSONObject loginOj = JSONObject.parseObject(loginRes);
		JSONObject reqObj = loginOj.getJSONObject("resp");
		String msg = reqObj.getString("msg");
		if(msg.contains("用户信息异常")) {
			String ziYinNo = reqObj.getString("ziYinNo");
			logger1.info(ziYinNo+"-"+password);
			return;
		}
		ziYinNo = reqObj.getString("ziYinNo");
		if(ziYinNo==null) ziYinNo = "";
		userId = reqObj.getString("userId");
		if(userId==null) userId = "";
		infoId = reqObj.getString("InfoId");
		if(infoId==null) infoId = "";
		flag = true;
	}
	
	public String getRealm(String zvog) throws UnsupportedEncodingException{
		byte[] data = new byte[2048];
		String param = "com.airchina.CAMobile";
		char[] strzv = zvog.toCharArray();
		byte[] strpa = param.getBytes("UTF-8");
	    int number=0;
	    byte[] strair = "AirChina".getBytes("UTF-8");
	    if(strzv!=null && strpa!=null && strair!=null){
	    	
	    	int  i;
	        for(i=0; i<strzv.length; i++){
	            int num1 = Integer.parseInt(zvog.substring(i, i+3));
	            int num2 = Integer.parseInt(zvog.substring(i+3, i+6));
	            i += 6;
	            char ch = strzv[i];
	            
	            if(ch > 77){
	                if(ch == 78){
	                    number = getNum(data ,strair, number ,num1 ,num2);
	                }else if(ch != 83 && ch == 88){
	                    String notnum = dealNotNum(i+1, zvog);
	                    char[] not = notnum.toCharArray();
	                    int j=0;
	                    int n=notnum.length();
	                    for(j=0; j<number; j++){
	                        data[j] ^= not[j - j/n*n];
	                    }
	                    i += notnum.length()+1;
	                }
	            }else if(ch == 67){
	            	number = getNum(data ,strpa, number ,num1 ,num2);
	            }
	        }
	    }
	    int len = data.length;
	    for(int j =0;j<len;j++){
	    	byte by = data[j];
	    	if(by==0){
	    		len =j;
	    		break;
	    	}
	    }
	    byte[] data2 = new byte[len];
	    for(int t= 0;t<len;t++){
	    	data2[t] = data[t];
	    }
	    
	    BASE64Encoder encoder = new BASE64Encoder();
	    String encoded = "i"+encoder.encode(data2);
		return encoded.trim();
	}
	
	
	public static int getNum(byte[] data, byte[] str, int number,int num1,int num2){
	    int len = str.length;
	    int n=0;
	    int m=0;
	    int result = 0;
	    if(num1 - num1/len*len > num2 - num2/len*len){
	        n = num1 - num1/len*len;
	        m = num2 - num2/len*len;
	    }else{
	        m = num1 - num1/len*len;
	        n = num2 - num2/len*len;
	    }
	    if(n >= len){
	        result = number;
	    }else{
	        result = number + n - m + 1;
	        System.arraycopy(str, m, data, number, n - m + 1);
	        
	    }
	    return result;
	}
	
	public String getValue(String start ,String end ,String str){
		 String stri = null;
		 Pattern par = Pattern.compile(start+"(.*?)"+end);
		 Matcher mat = par.matcher(str);
		 while(mat.find()){  
			   stri = mat.group(1);
      }
		 return stri;
	}
	
	public static String dealNotNum(int i, String str){
		String subStr = str.substring(i);
		int s = subStr.indexOf("S");
		String dealStr = subStr.substring(0,s);
	    return dealStr;
	}

	//MD5加密
	public static String encode(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String md5Result = null;
        if(null == data){
            return md5Result;
        }

        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes("UTF-8"));
            byte b[] = md.digest();
            int i;
            StringBuffer sb = new StringBuffer("");
            for(int offset = 0; offset < b.length; offset ++){
                i = b[offset];
                if(i< 0){
                    i += 256;
                }
                if(i < 16){
                    sb.append("0");
                }
                sb.append(Integer.toHexString(i));
            }
            md5Result = sb.toString();
        } catch (Exception e) {
        }
        return md5Result;
    }
	
	@Override
	public String httpResult() throws Exception {
		return null;
	}
}
