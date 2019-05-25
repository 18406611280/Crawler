package test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jdom2.input.SAXBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.ResultPost;
import com.aft.utils.file.MyFileUtils;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

@SuppressWarnings("unchecked")
public class Test {
	
	private static void ddddddd() throws Exception {
//		for(int i=2003; i<=2016; i++) {
//			String result = MyHttpClientUtil.get("http://baidu.lecai.com/lottery/draw/list/50?type=range&start=" + i + "001&end=" + (i + 2) + "001");
//			Document doc = Jsoup.parse(result);
//			StringBuilder sb = new StringBuilder();
//			Elements eleTrs = doc.select("table.historylist > tbody > tr");
//			for(int j=eleTrs.size()-1; j>=0; j--) {
//				Element eleTr = eleTrs.get(j);
//				Element eleA = eleTr.select("> td:eq(0) > a").first();
//				sb.append(eleA.ownText().trim());
//				
//				for(Element eleTd : eleTr.select("> td.balls > table > tbody > tr > td")) {
//					for(Element eleEm : eleTd.select("> em")) {
//						sb.append("\t").append(eleEm.ownText().trim());
//					}
//					sb.append("\t");
//				}
//				sb.append("\r\n");
//			}
//			System.out.println(sb);
//			MyFileUtils.createFile("c:/", "ssq.txt", sb.toString(), true);
//		}
//		for(int i=10; i<=16; i++) {
//			String result = MyHttpClientUtil.get("http://baidu.lecai.com/lottery/draw/list/1?type=range&start=" + i + "001&end=" + (i + 2) + "001");
//			Document doc = Jsoup.parse(result);
//			StringBuilder sb = new StringBuilder();
//			Elements eleTrs = doc.select("table.historylist > tbody > tr");
//			for(int j=eleTrs.size()-1; j>=0; j--) {
//				Element eleTr = eleTrs.get(j);
//				Element eleA = eleTr.select("> td:eq(0) > a").first();
//				sb.append(eleA.ownText().trim());
//				
//				for(Element eleTd : eleTr.select("> td.balls > table > tbody > tr > td")) {
//					for(Element eleEm : eleTd.select("> em")) {
//						sb.append("\t").append(eleEm.ownText().trim());
//					}
//					sb.append("\t");
//				}
//				sb.append("\r\n");
//			}
//			System.out.println(sb);
//			MyFileUtils.createFile("c:/", "dlt.txt", sb.toString(), true);
//		}
	}
	
	public static void main(String[] args) throws Exception {
		
//		ddddddd();
		
//		tiebaBook();
//		String result = MyHttpClientUtil.get("http://www.juneyaoair.com/pages/Flight/flight.aspx?flightType=OW&sendCode=SHA&arrCode=SZX&directType=N&tripType=D&departureDate=2015-07-12&yunsuo_session_verify=73f815339e42aa06e3a102035165fabd");
//		System.out.println(result);
		
//		for(int i=0; i<100; i++) {
//			String result = MyHttpClientUtil.get("http://www.juneyaoair.com/pages/Flight/flight.aspx?flightType=OW&sendCode=SHA&arrCode=SZX&directType=N&tripType=D&departureDate=2015-07-12");
//			System.out.println(result);
//		}

//		gsAir();
		
//		test();
		
//		muCeair();

//		String url = "http://www.juneyaoair.com/UnitOrderWebAPI/Book/QueryFlightFareNew?flightType=OW&tripType=D&directType=D&departureDate=%depDate%&sendCode=%depCode%&arrCode=%desCode%";
//		url = url.replaceAll("%depCode%", "CAN")
//					.replaceAll("%desCode%", "SHA")
//					.replaceAll("%depDate%", "2015-12-02");
//		Map<String, String> headerMap = new HashMap<String, String>();
//		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
//		String httpResult = MyHttpClientUtil.httpClient(url,
////				MyHttpClientUtil.httpGet, null, null, headerMap, null, null,
//				MyHttpClientUtil.httpGet, null, null, headerMap, "119.5.44.20", 12091,
//				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		System.out.println(httpResult);
		
//		fuAir();
		
//		y8Air();
		
//		nsAir();
		
//		mfAir();
		
//		caAir();
		
//		u3Air();
		u3Air2();
		
//		tieba();
		
//		nxAir();
//		hxAir();
		
//		drAir();
		
//		Map<String, String> paramMap = new HashMap<String, String>();
//		paramMap.put("RouteIndex", "1");
//		paramMap.put("AVType", "1");
//		paramMap.put("AirlineType", "Single");
//		paramMap.put("BuyerType", "0");
//		paramMap.put("DesCity", "CAN");
//		paramMap.put("FlightDate", "2016-01-21");
//		paramMap.put("IsFixedCabin", "false");
//		paramMap.put("OrgCity", "CTU");
//		paramMap.put("CardFlag", "2803");
//		paramMap.put("PassKey", "D71202BD31E4DCC4F5E613C5547D8A4F");
//		
//		Map<String, String> headerMap = new HashMap<String, String>();
//		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
//		headerMap.put("Cookie", "ASP.NET_SessionId=jqq1l3uzh0ctlntt3o3pezde");
//		
//		String httpResult = MyHttpClientUtil.httpClient("http://www.scal.com.cn/Web/ETicket/GetSingleChina",
//				MyHttpClientUtil.httpPost, paramMap, headerMap);
//		System.out.println(httpResult);
		
		
//		ozAir();
//		qwAir();
		
//		euAir();
		
//		uqAir();
		
//		changeToFailResult();
		
//		kyAir();
		
//		pnAir();
//		zhAir();
		
//		scAir();
//		jrAir();
//		a6Air();
		
//		dzAir();
		
//		bkAir();
		
//		l8Air();
	}
	
	
	public static void l8Air() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String url = null;
		MyHttpClientResultVo vo = null;
		
		url = "http://www.luckyair.net/";
//		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, null, null, null, null, null,
//				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GBK");
//		System.out.println(vo.getHttpResult());
		
//		String cookie = MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Set-Cookie", "JSESSIONID=");
//		System.out.println(cookie);
		String cookie = "pgv_pvi=7318223872; JSESSIONID=187ED0EEEC1ED5A9873F877D7F50D096.l1; sso_sign_eking=1bd35eb9-08c7-4442-b624-f2bb7c15c8d9; pgv_si=s8531141632";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Cookie", cookie);
		headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		headerMap.put("Referer", "http://www.luckyair.net/flight/searchflight2016.action");
		
		url = "http://www.luckyair.net/flightresult/flightresult2016.action";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("orgCity", "DLU");
		paramMap.put("dstCity", "JHG");
		paramMap.put("flightDate", "2016-09-17");
		paramMap.put("index", 1);
		paramMap.put("flightseq", 1);
		paramMap.put("desc", "");
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, paramMap, null, headerMap, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GBK");
		System.out.println(vo.getHttpResult());
	}
	
	
	public static void bkAir() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String url = null;
		MyHttpClientResultVo vo = null;
		Document doc = null;
		
		vo = MyHttpClientUtil.httpClient(httpClient, "http://bk.travelsky.com/bkair/reservation/indexLowPriceTkt.do", MyHttpClientUtil.httpGet, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Set-Cookie", "JSESSIONID="));
		doc = Jsoup.parse(vo.getHttpResult());
		String lowPriceRequestId = doc.select("#queryFlightLowForm > input[name=lowPriceRequestId]").first().val();
		String queryFlightRequestId = doc.select("#queryFlightLowForm > input[name=queryFlightRequestId]").first().val();
		System.out.println(lowPriceRequestId + "---" + queryFlightRequestId);
		
		url = "http://bk.travelsky.com/bkair/reservation/queryLowPriceFlight.do?&orgCity=YNT&destCity=DLC&takeoffDate=2016-09-23&lowPriceRequestId=" + lowPriceRequestId + "&queryFlightRequestId=" + queryFlightRequestId;
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		doc = Jsoup.parse(vo.getHttpResult());
		System.out.println(vo.getHttpResult());
//		System.out.println(doc.getElementById("result_table").html());
		
		MyThreadUtils.sleep(5000);
		
		url = "http://bk.travelsky.com/bkair/reservation/queryLowPriceFlight.do?&orgCity=YNT&destCity=DLC&takeoffDate=2016-09-30&lowPriceRequestId=" + lowPriceRequestId + "&queryFlightRequestId=" + queryFlightRequestId;
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		doc = Jsoup.parse(vo.getHttpResult());
		System.out.println(vo.getHttpResult());
//		System.out.println(doc.getElementById("result_table").html());
	}
	
	
	public static void dzAir() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String url = null;
		MyHttpClientResultVo vo = null;
		
//		url = "http://b2c.donghaiair.com/dz/FlightSearch.do?orgCity=HRB&destCity=HAK&journeyType=OW&depDate=2016-09-08&adt=1";
//		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpGet, null, null, null, null, null,
//				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		System.out.println(vo.getHttpResult());
		
		url = "http://b2c.donghaiair.com/dz/FlightSearch.do?orgCity=CGO&destCity=HAK&journeyType=OW&depDate=2016-09-02&retDate=2016-09-02&adt=5&chd=0";
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(vo.getHttpResult());
	}
	
	public static void a6Air() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String url = null;
		MyHttpClientResultVo vo = null;
		
		url = "http://www.redair.cn/booking/ajaxFlightSearch?airwayType=DC&orgCity=KMG&dstCity=KHN&flightDate=2016-08-25";
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpGet, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(vo.getHttpResult());
	}
	
	
	public static void jrAir() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Referer", "http://www.joy-air.com/pssui/joyairportal/views/");
//		headerMap.put("Cookie", "JSESSIONID=5FF3E6F6068A38FEF26FEF1D695B0ACB-n1");
		String url = null;
		MyHttpClientResultVo vo = null;
		
		url = "http://www.joy-air.com/pssweb/ota/flights?adultNum=1&childNum=0&depDate=2016-08-31&dstcity=THQ&flightWayType=OW&orgcity=SIA";
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpGet, null, null, headerMap, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(vo.getHttpResult());
	}
	
	public static void scAir() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Referer", "http://www.shandongair.com.cn/");
		String url = null;
		MyHttpClientResultVo vo = null;
		Document doc = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
//		url = "http://sc.travelsky.com/scet/calendarFlightSearch.do?usedfor=1&&orgcity=XMN&descity=ZUH&deptdate=20160829";
//		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpGet, null, null, headerMap, null, null,
//				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		System.out.println(vo.getHttpResult());
//		
//		doc = Jsoup.parse(vo.getHttpResult());
//		headerMap.put("Referer", url);
//		for(Element ele : doc.getElementById("AutoForm").select("input")) {
//			paramMap.put(ele.attr("name"), ele.val());
//		}
//		url = "http://sc.travelsky.com" + doc.getElementById("AutoForm").attr("action");
//		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, paramMap, null, headerMap, null, null,
//				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		System.out.println(vo.getHttpResult());
		
		
		paramMap.put("countrytype", "0");
		paramMap.put("travelType", "0");
		paramMap.put("cityCodeOrg", "CAN");
		paramMap.put("cityCodeDes", "TAO");
		paramMap.put("takeoffDate", "2016-08-25");
		paramMap.put("cabinStage", "0");
		paramMap.put("adultNum", "1");
		paramMap.put("childNum", "0");
		url = "http://sc.travelsky.com/scet/queryAv.do?lan=cn";
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, paramMap, null, headerMap, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(vo.getHttpResult());
		
		doc = Jsoup.parse(vo.getHttpResult());
		headerMap.put("Referer", url);
		paramMap.clear();
		for(Element ele : doc.select("#main > form[name=queryAv]").first().select("input")) {
			paramMap.put(ele.attr("name"), ele.val());
		}
		url = "http://sc.travelsky.com" + doc.select("#main > form[name=queryAv]").first().attr("action");
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, paramMap, null, headerMap, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(vo.getHttpResult() + "---");
		
	}
	
	public static void zhAir() throws Exception {
		String url = "http://www.shenzhenair.com/szair_B2C/toFlightSearchPage.action";
		String param = "condition.originalPage=index&condition.hcType=DC&condition.orgCityCode=NNG&condition.dstCityCode=CKG&condition.orgDate=2016-11-03";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(httpClient, url + "?" + param, MyHttpClientUtil.httpPost, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		String result = vo.getHttpResult();
		System.out.println(result);
		
		url = "http://www.shenzhenair.com/szair_B2C/flightSearch.action";
		param = "condition.orgCityCode=NNG&condition.dstCityCode=CKG&condition.hcType=DC&condition.orgDate=2016-11-03&condition.dstDate=%24%7BsearchCondition.dstDate%7D";
		vo = MyHttpClientUtil.httpClient(httpClient, url + "?" + param, MyHttpClientUtil.httpPost, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		result = vo.getHttpResult();
		System.out.println(result);
	}
	
	public static void pnAir() throws Exception {
		String url = "http://www.westair.cn/InternetBooking/AirLowFareSearchExt.do";
		String param = "lang=zh_CN&pos=WESTAIR_WEB&tripType=OW&outboundOption.originLocationCode=CKG&outboundOption.destinationLocationCode=SYX&flexibleSearch=false&outboundOption.departureDay=18&outboundOption.departureMonth=8&outboundOption.departureYear=2016&airDate1=2016-08-18&inboundOption.departureDay=&inboundOption.departureMonth=&inboundOption.departureYear=&airDate2=&guestTypes%5B0%5D.type=ADT&guestTypes%5B0%5D.amount=1&guestTypes%5B1%5D.type=CNN&guestTypes%5B1%5D.amount=0&guestTypes%5B2%5D.type=INF&guestTypes%5B2%5D.amount=0&coupon=&reservationInput=";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(httpClient, url + "?" + param, MyHttpClientUtil.httpPost, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		String result = vo.getHttpResult();
		System.out.println(result);
		
		url = "http://www.westair.cn/InternetBooking/AirFareFamiliesFlexibleForward.do";
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpGet, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		result = vo.getHttpResult();
		System.out.println(result);
		
		httpClient = HttpClients.createDefault();
		url = "http://www.westair.cn/InternetBooking/AirLowFareSearchExt.do";
		param = "lang=zh_CN&pos=WESTAIR_WEB&tripType=OW&outboundOption.originLocationCode=CKG&outboundOption.destinationLocationCode=SYX&flexibleSearch=false&outboundOption.departureDay=28&outboundOption.departureMonth=8&outboundOption.departureYear=2016&airDate1=2016-08-28&inboundOption.departureDay=&inboundOption.departureMonth=&inboundOption.departureYear=&airDate2=&guestTypes%5B0%5D.type=ADT&guestTypes%5B0%5D.amount=1&guestTypes%5B1%5D.type=CNN&guestTypes%5B1%5D.amount=0&guestTypes%5B2%5D.type=INF&guestTypes%5B2%5D.amount=0&coupon=&reservationInput=";
		vo = MyHttpClientUtil.httpClient(httpClient, url + "?" + param, MyHttpClientUtil.httpPost, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		result = vo.getHttpResult();
		System.out.println(result);
		
		vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpGet, null, null, null, null, null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		result = vo.getHttpResult();
		System.out.println(result);
	}
	
	public static void kyAir() throws Exception {
		boolean proxy = true;
		String proxyIp = "119.5.44.20";
		int proxyPort = 12046;
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
		
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
//		paramMap.put("scene", "register");
		paramMap.put("orgCity", "KMG");
		paramMap.put("dstCity", "PVG");
		paramMap.put("flightDates", "2016-08-20");
		paramMap.put("flightType", "DC");
		paramMap.put("source", "airkunming");
//		?appkey=1988
//		&scene=register
//		&token=19881452993407713391425421151452993407713075
//		&secret=
//		&orgCity=KMG
//		&dstCity=PVG
//		&flightDates=2016-01-20
//		&flightType=DC
//		&source=airkunming
				
		String httpResult = MyHttpClientUtil.httpClient("http://www.airkunming.com/booking/flightSearch",
				MyHttpClientUtil.httpPost, paramMap, null, headerMap, proxy ? proxyIp : null, proxy ? proxyPort : null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(httpResult);
	}
	
//	69305C2B10D5078DA7F62B073E511BF8
	public static void uqAir() throws Exception {
		
		boolean proxy = true;
		String proxyIp = "119.5.44.20";
		int proxyPort = 12046;
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(null, "http://www.urumqi-air.com/",
				MyHttpClientUtil.httpGet, null, null, headerMap, proxy ? proxyIp : null, proxy ? proxyPort : null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
//		System.out.println(vo.getHttpResult());
		String jSId = MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		System.out.println(jSId);
		
//		headerMap.put("Cookie", "JSESSIONID=69305C2B10D5078DA7F62B073E511BF8.u1");
		headerMap.put("Cookie", jSId);
		
		String httpResult = MyHttpClientUtil.httpClient("http://www.urumqi-air.com/flight/searchflight.action?tripType=ONEWAY&orgCity1=URC&dstCity1=HAK&flightdate1=2016-01-20&flightdate2=",
				MyHttpClientUtil.httpGet, null, null, headerMap, proxy ? proxyIp : null, proxy ? proxyPort : null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
		System.out.println(httpResult);
		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		
		String httpContent = "<flight><tripType>ONEWAY</tripType><orgCity1>URC</orgCity1><dstCity1>CSX</dstCity1><flightdate1>2016-01-20</flightdate1><index>1</index></flight>";
		String httpResult1 = MyHttpClientUtil.httpClient("http://www.urumqi-air.com/flight/searchflight!getFlights.action", MyHttpClientUtil.httpPost,
				null, httpContent, headerMap, proxy ? proxyIp : null, proxy ? proxyPort : null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
		System.out.println(httpResult1);
		org.jdom2.Document jdomDoc = new SAXBuilder().build(new ByteArrayInputStream(httpResult1.getBytes("UTF-8")));  
		org.jdom2.Element rootEle = jdomDoc.getRootElement();
		for(org.jdom2.Element segmentEle : rootEle.getChild("segments").getChildren()) {
			for(org.jdom2.Element productEle : segmentEle.getChild("products").getChildren()) {
				for(org.jdom2.Element cabinEle : productEle.getChild("cabins").getChildren()) {
					String cabin = cabinEle.getAttributeValue("cabinCode").toString().trim().toUpperCase();
					System.out.println(cabin);
				}
			}
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		
		
//		proxy = false;
//		proxyIp = "124.161.189.47";
//		proxyPort = 12112;
		httpContent = "<flight><tripType>ONEWAY</tripType><orgCity1>URC</orgCity1><dstCity1>CSX</dstCity1><flightdate1>2016-01-22</flightdate1><index>1</index></flight>";
		String httpResult2 = MyHttpClientUtil.httpClient("http://www.urumqi-air.com/flight/searchflight!getFlights.action", MyHttpClientUtil.httpPost,
				null, httpContent, headerMap, proxy ? proxyIp : null, proxy ? proxyPort : null,
				MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
		System.out.println(httpResult2);
		org.jdom2.Document jdomDoc2 = new SAXBuilder().build(new ByteArrayInputStream(httpResult2.getBytes("UTF-8")));  
		org.jdom2.Element rootEle2 = jdomDoc2.getRootElement();
		for(org.jdom2.Element segmentEle : rootEle2.getChild("segments").getChildren()) {
			for(org.jdom2.Element productEle : segmentEle.getChild("products").getChildren()) {
				for(org.jdom2.Element cabinEle : productEle.getChild("cabins").getChildren()) {
					String cabin = cabinEle.getAttributeValue("cabinCode").toString().trim().toUpperCase();
					System.out.println(cabin);
				}
			}
		}
	}
	
	public static void tieba() throws Exception {
		String forumId = "13766389";
		String threadId = "4097316161";
		while(true) {
			String url = "http://tieba.baidu.com/novel/getNextChapterThread?forum_id=" + forumId + "&thread_id=" + threadId;
			String httpResult = MyHttpClientUtil.get(url);
			Map<String, Object> map = MyJsonTransformUtil.readValue(httpResult, Map.class);
			if(Boolean.parseBoolean(map.get("error").toString())) {
				System.out.println(httpResult);
				break ;
			}
			Map<String, Object> mapData = (Map<String, Object>)map.get("data");
			threadId = mapData.get("thread_id").toString();
			httpResult = MyHttpClientUtil.get("http://tieba.baidu.com/p/" + threadId);
			Document doc = Jsoup.parse(httpResult);
			String[] titles = doc.title().trim().split("_");
			System.out.println(titles[0]);
			Elements eles = doc.select("#j_p_postlist > div.l_post > div.d_post_content_main > div.p_content  > cc > div.novel-post-section > div.novel-post-content");
			if(null == eles || eles.isEmpty()) return ;
			String content = titles[0] + "\r\n" + eles.first().html().replaceAll("<br>", "");
			MyFileUtils.createFile("c:/", titles[1].substring(0, titles[1].length() - 1) + ".txt", content, true);
		}
	}
	
	public static void changeToFailResult() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("c:/post.log"), "GBK"));
			List<String> postList = new ArrayList<String>();
			String line = null;
			while(null != (line = br.readLine())) {
				if(StringUtils.isEmpty(line)) continue ;	// 绌鸿蹇界暐
				if(!line.contains("鍙戦�佹暟鎹�") || !line.contains("PUT916")) continue ;
				postList.add(line.substring(line.indexOf("[{")));
			}
			br.close();
			if(postList.isEmpty()) {
				System.out.println("....o");
				return ;
			}
			
			System.out.println("鏁版嵁閲囬泦绋嬪簭, 淇濆瓨澶辫触鍐呭鍐嶆鎻愪氦, 鏁伴噺:" + postList.size());
			Iterator<String> itPostList = postList.iterator();
			int successAmount = 0;
			while(itPostList.hasNext()) {
				line = itPostList.next();
//				String result = ResultPost.postCrawlerResult("http://192.168.8.202:8080/platformv2/crawler/hkFlightLine/save.action", line);	// 鎻愪氦璇锋眰
				String result = ResultPost.postCrawlResult("http://192.168.0.252:5000/crawler/hkFlightLine/save.action", line);	// 鎻愪氦璇锋眰
				System.out.println("鏁版嵁閲囬泦绋嬪簭, 淇濆瓨澶辫触鍐呭鍐嶆鎻愪氦, 鍓╀綑鏁伴噺:" + postList.size() + ", 杩斿洖:" + result);
				if(ResultPost.postSuccess(result)) {
					++successAmount;
					itPostList.remove();
				}
			}
			System.out.println("鏁版嵁閲囬泦绋嬪簭, 淇濆瓨澶辫触鍐呭鍐嶆鎻愪氦, 鎴愬姛鏁伴噺:" + successAmount);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(null != br) br = null;
		}
	}
	
	public static void mfAir() throws Exception {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
		headerMap.put("Accept-Encoding", "deflate");
		
		MyHttpClientResultVo httpVo = MyHttpClientUtil.httpClient(null, "http://et.xiamenair.com/xiamenair/book/findFlights.action?tripType=0&queryFlightInfo=XMN,SHA,2016-07-26",
				MyHttpClientUtil.httpGet, null, null, headerMap, null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		headerMap.put("Cookie", MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID="));
		System.out.println(httpVo.getHttpResult());
		Document doc = Jsoup.parse(httpVo.getHttpResult());
		String url = "http://et.xiamenair.com/xiamenair/book/findFlights.json?lang=zh&tripType=0" +
						"&r=" + doc.getElementById("random").val() +
						"&takeoffDate=" + doc.getElementById("takeoffDate").val() +
						"&returnDate=" + doc.getElementById("returnDate").val() +
						"&orgCity=" + doc.getElementById("orgCity").val() +
						"&dstCity=" + doc.getElementById("dstCity").val();
		System.out.println(url);
		
		String httpResult = MyHttpClientUtil.httpClient(url,
				MyHttpClientUtil.httpGet, null, null, headerMap, null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(httpResult);
	}
	
	
	public static void qwAir() throws Exception {
		Map<String, Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("Cookie", "JSESSIONID=3A19E05F37E51EE7D29A6EECF0166D52.tomcat2");
		
		headerMap.put("Referer", "http://www.qdairlines.com/index.jsp");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("specialTicketId", "special");
		paramMap.put("orgCity", "TAO");
		paramMap.put("destCity", "KMG");
		paramMap.put("journeyType", "OW");
		paramMap.put("depDate", "2016-08-08");
//		paramMap.put("orgCityInput", "闈掑矝");
//		paramMap.put("destCityInput", "鎴愰兘");
		paramMap.put("chd", "0");
		paramMap.put("adt", "1");
//		paramMap.put("retDate", "2015-12-05");
//		paramMap.put("secueCode", "");
		
		
//		String httpResult = MyHttpClientUtil.httpClient("http://www.qdairlines.com/FlightSearch.do?specialTicketId=special&orgCity=TAO&destCity=CTU&journeyType=OW&depDate=2015-12-08&=&adt=1&chd=0",
		String httpResult = MyHttpClientUtil.httpClient("http://www.qdairlines.com/FlightSearch.do",
				MyHttpClientUtil.httpPost, paramMap, headerMap);
		System.out.println(httpResult);
	}
	
	
	public static void ozAir() throws Exception {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Cookie", "UISESSIONID=StuJvwwOS1LEVDzGVOZY0cTmT3vIZHcduuSoDIXkgZaZmuWSJyRC!-1710325268!1200763475");
		
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("sessionUniqueKey", "a29cd76d-5ffd-45bb-f6b4-69fd25158f0d");
		paramMap.put("departureDate", "20151216");
		paramMap.put("arrivalDate", "");
		paramMap.put("departureArea", "CN");
		paramMap.put("arrivalArea", "KR");
		paramMap.put("departureAirport", "CAN");
		paramMap.put("arrivalAirport", "ICN");
		paramMap.put("tripType", "OW");
		paramMap.put("domIntType", "I");
		paramMap.put("cabinClass", "T");
		paramMap.put("childCount", "0");
		paramMap.put("infantCount", "0");
		paramMap.put("adultCount", "1");
		paramMap.put("totalPaxCount", "1");
		paramMap.put("openDepartureArea1", "CN");
		paramMap.put("openDepartureAirport1", "CAN");
		paramMap.put("openArrivalArea1", "KR");
		paramMap.put("openArrivalAirport1", "ICN");
		paramMap.put("selDepartureDate", "20151216");
		paramMap.put("selArrivalDate", "");
		paramMap.put("hidCallPage", "CAL_OF_INT");
		paramMap.put("hidPageType", "S");
		paramMap.put("hidDepartureArea", "");
		
		
		
		
		String url = "https://cn.flyasiana.com/I/ch/RevenueInternationalFareDrivenFlightSelect.do?HtHS5PIsQAUvwU2NjO/LMg===1450784368&sessionUniqueKey=cce9509a-939b-4aeb-e063-8648f32c72f4&departureDate=20151226&arrivalDate=20151226&departureArea=CN&arrivalArea=KR&departureAirport=CAN&arrivalAirport=ICN&tripType=OW&domIntType=I&cabinClass=T&openDepartureArea1=CN&openDepartureAirport1=CAN&openArrivalArea1=KR&openArrivalAirport1=ICN&selDepartureDate=20151226&selArrivalDate=20151226&hidCallPage=CAL_OF_INT&hidPageType=S&hidDepartureArea=&totalPaxCount=1&adultCount=1&childCount=0&infantCount=0";
		System.out.println(url);
		String httpResult = MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpPost, null, headerMap);
		System.out.println(httpResult);
	}
	
	public static void drAir() throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("orgCity", "CTU");
		paramMap.put("dstCity", "KMG");
		paramMap.put("flightDate", "2016-07-15");
		paramMap.put("returnDate", "");
		paramMap.put("roundTrip", "false");
		paramMap.put("inParam", "baseu$f1iegwgw%FE@##lefe@#$@ADSFgg~````88c2RmI0AjRyQzMjM0Xl5MJF9fXzIzNDM0NjM0QCMjYWZ+IWBgYDswME9PQUZXRUZXXiNAQEAjQCFLTUdDVFUyMDE2LTA3LTE1ZzUxMGNuM21oMjRxbWtvcHhoY3N2dHcxa987#$%_323dd3");
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
		headerMap.put("Cookie", "CNZZDATA1253040769=118021827-1462175433-%7C1467854635; ASP.NET_SessionId=g510cn3mh24qmkopxhcsvtw1; ipc=g510cn3mh24qmkopxhcsvtw1");
//		headerMap.put("Referer", "http://www.rlair.net/flightQueryResultV2.html?orgCity=CTU&dstCity=HRB&flightDate=2015-11-30&returnDate=&roundTrip=false");
		
		String httpResult = MyHttpClientUtil.httpClient("http://www.rlair.net/data.ashx/FlightServiceV2/QuerySeat.json", MyHttpClientUtil.httpPost,
				paramMap, null, headerMap, null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(httpResult);
	}
	
	public static void euAir() throws Exception {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
		headerMap.put("Referer", "http://www.rlair.net/flightQueryResult.html?orgCity=KMG&dstCity=BHY&flightDate=2015-09-20&returnDate=&roundTrip=false");
		String param = "orgCity=CTU&takeoffDate=2016-08-18&tripType=0&destCity=XMN&adultNum=1";
		String httpResult = MyHttpClientUtil.httpClient("http://b2c.cdal.com.cn/euair/reservation/flightQuery.do?" + param,
				MyHttpClientUtil.httpPost,
				null, null, headerMap, null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		System.out.println(httpResult);
		org.jsoup.nodes.Document doc = Jsoup.parse(httpResult);
		System.out.println(doc.getElementById("result_table").html());
	}
	
	
	public static void nsAir() throws Exception {
		String httpContent = "callCount=1\r" +
		"windowName=\r" +
		"c0-scriptName=flightQueryServer\r" +
		"c0-methodName=readIBE\r" +
		"c0-id=0\r" +
		"c0-param0=string:01\r" +
		"c0-param1=string:CAN\r" +
		"c0-param2=string:SJW\r" +
		"c0-param3=string:\r" +
		"c0-param4=string:2016-08-14\r" +
		"c0-param5=string:\r" +
		"batchId=4\r" +
		"page=%2FflightQuery.action\r" +
		"httpSessionId=\r" +
		"scriptSessionId=700A199024C4F281C8889936B14D0C6C";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0");
		
		String httpResult = MyHttpClientUtil.httpClient("http://www.hbhk.com.cn/dwr/call/plaincall/flightQueryServer.readIBE.dwr", MyHttpClientUtil.httpPost,
				null, httpContent, headerMap,
				null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		System.out.println(httpResult);
		Pattern pattern = Pattern.compile("flightResultStart=(\\[\\{.*\\}\\]);", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(httpResult);
		if(matcher.find()) {
			System.out.println(matcher.group(1));
			List<Map> list = MyJsonTransformUtil.readValueToList(matcher.group(1), Map.class);
			System.out.println(list.size());
		}
	}
	
	
	public static void fuAir() throws Exception {
	 String httpContent = "<flight><tripType>ONEWAY</tripType><orgCity1>FOC</orgCity1><dstCity1>PEK</dstCity1><flightdate1>2016-08-29</flightdate1><index>1</index><times></times><desc></desc></flight>";
			Map<String, Object> headerMap = new HashMap<String, Object>();
//			headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/43.0");
	//		headerMap.put("Referer", "http://www.fuzhou-air.cn/flight/searchflight.action?tripType=ONEWAY&orgCity1=FOC&dstCity1=CGO&flightdate1=2016-07-15&flightdate2=&adult=1&child=0&infant=0");
			headerMap.put("ContentType", "text/xml;charset=utf-8");
			
			String httpResult = MyHttpClientUtil.httpClient("http://www.fuzhou-air.cn/flight/searchflight!getFlights.action", MyHttpClientUtil.httpPost,
					null, httpContent, headerMap,
					"125.64.91.110", 20773, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
			System.out.println(httpResult);
			SAXBuilder builder = new SAXBuilder();
			org.jdom2.Document doc = builder.build(new ByteArrayInputStream(httpResult.getBytes("UTF-8")));
			org.jdom2.Element element = doc.getRootElement(); 
			System.out.println(element.getChild("orgCity").getChild("code").getText());;
			
			System.out.println("----");
	}
	
	public static void y8Air() throws Exception {
		String httpContent = "<flight><tripType>ONEWAY</tripType><orgCity1>SYX</orgCity1><dstCity1>PVG</dstCity1><flightdate1>2016-08-02</flightdate1><index>1</index></flight>";
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/43.0");
//		headerMap.put("Referer", "http://www.fuzhou-air.cn/flight/searchflight.action?tripType=ONEWAY&orgCity1=FOC&dstCity1=CGO&flightdate1=2016-07-15&flightdate2=&adult=1&child=0&infant=0");
//		headerMap.put("Cookie", "JSESSIONID=A49E578D711FB2D7608E42E262AC146B.d1;");
		
		String httpResult = MyHttpClientUtil.httpClient("http://www.yzr.com.cn/flight/searchflight!getFlights.action", MyHttpClientUtil.httpPost,
				null, httpContent, headerMap,
				"125.64.91.110", 20773, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
		System.out.println(httpResult);
//		SAXBuilder builder = new SAXBuilder();
//		org.jdom2.Document doc = builder.build(new ByteArrayInputStream(httpResult.getBytes("UTF-8")));  
//		org.jdom2.Element element = doc.getRootElement(); 
//		System.out.println(element.getChild("orgCity").getChild("code").getText());;
		
		System.out.println("----");
	}
	
	public static void hoAir() {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
		String proxySignMark = "xxx";
		ProxyUtil.changeProxy(proxySignMark, "", 0, "test");
		String httpResult = ProxyUtil.httpProxy(null, proxySignMark,
				"http://www.juneyaoair.com/pages/Flight/flight.aspx?flightType=OW&sendCity=%E5%B9%BF%E5%B7%9E&sendCode=CAN&arrCity=%E4%B8%8A%E6%B5%B7&arrCode=SHA&directType=N&tripType=D&departureDate=2015-07-30&returnDate=2015-07-30",
				MyHttpClientUtil.httpGet, null, null, headerMap, 100000, "GBK");
		System.out.println(httpResult);
	}
	
	public static void caAir() throws Exception {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");
//		MyHttpClientResultVo httpVo = MyHttpClientUtil.httpClient(null,
//				"http://et.airchina.com.cn/InternetBooking/AirLowFareSearchExternal.do" +
//				"?tripType=OW&searchType=FARE&flexibleSearch=false&directFlightsOnly=false" +
//				"&fareOptions=1.FAR.X&outboundOption.originLocationCode=PEK&outboundOption.destinationLocationCode=CAN" +
//				"&outboundOption.departureDay=11&outboundOption.departureMonth=12&outboundOption.departureYear=2015" +
//				"&outboundOption.departureTime=NA&guestTypes%5B0%5D.type=ADT&guestTypes%5B0%5D.amount=1" +
//				"&guestTypes%5B1%5D.type=CNN&guestTypes%5B1%5D.amount=0&pos=AIRCHINA_CN&lang=zh_CN&guestTypes%5B2%5D.type=INF" +
//				"&guestTypes%5B2%5D.amount=0",
//				MyHttpClientUtil.httpGet, null, null, headerMap,
//				"125.64.91.90", 20191, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		headerMap.put("Cookie", MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID="));
//		String httpResult = httpVo.getHttpResult();
//		System.out.println(httpResult);
////		formManager.submitExternal({url: 'http://et.airchina.com.cn:80/InternetBooking/AirLowFareSearchExt.do;jsessionid=B1F16696D287436BA33EFB0FD841969D', failureUrl: '//static.airchina.wscdns.com/InternetBooking/zh_CN/error_500.html?version=201505131931', interstitialDelay: -1});
//		Pattern pattern = Pattern.compile("formManager\\.submitExternal\\(\\{url: '(.+)', failureUrl:");
//		Matcher matcher = pattern.matcher(httpResult);
//		if(matcher.find()) {
//			String dd = matcher.group(1);
//			System.out.println(dd);
//			Thread.sleep(10000);
//			httpResult = MyHttpClientUtil.httpClient(dd, MyHttpClientUtil.httpGet, null, null, headerMap,
//					"125.64.91.90", 20191, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//			System.out.println(httpResult);
//			
//			
//			
//		}
		
		headerMap.put("Cookie", "JSESSIONID=6A6E919004C2B0FA3BB9B4EDE03446A3;");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("isFareFamilySearchResult", "true");
		paramMap.put("selectedItineraries", "0,26");
		paramMap.put("selectedFlightIds", "0,26");
		paramMap.put("combinabilityReloadRequired", "false");
		paramMap.put("flightIndex", "");
		paramMap.put("flowStep", "AIR_COMBINABLE_FARE_FAMILIES_SEARCH_RESULTS");
		paramMap.put("alignment", "horizontal");
		paramMap.put("context", "airSelection");
		String httpResult = MyHttpClientUtil.httpClient("http://et.airchina.com.cn/InternetBooking/AirSelectOWCFlight.do",
				MyHttpClientUtil.httpPost, paramMap, null, headerMap,
				"125.64.91.90", 20191, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		System.out.println(httpResult);
		if(StringUtils.isEmpty(httpResult)) {
			Thread.sleep(1000);
			caAir();
			return ;
		}
		Pattern pattern = Pattern.compile("\\{([.\\s\\S]+)\\}");
		Matcher matcher = pattern.matcher(httpResult);
		if(matcher.find()) {
			System.out.println(matcher.group(1));
			Map<String, Object> map = MyJsonTransformUtil.readValue(matcher.group(1), Map.class);
			String bottomBot = (String)map.get("bottomBot");
			String d = URLDecoder.decode(bottomBot, "UTF-8");
			System.out.println(d);
		}
	}
	
	public static void u3Air() throws Exception {
		String url = "http://www.scal.com.cn/ETicket/AirlineList";
		String url_1 = "http://www.scal.com.cn/ETicket/GetSingleChina";
		
		String yzmUrl = "http://www.scal.com.cn/Base/GetVerifyCode";
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");

		MyHttpClientResultVo httpVo = MyHttpClientUtil.httpClient(null, yzmUrl, MyHttpClientUtil.httpGet, null, null, headerMap,
				"124.161.189.47", 12086, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
		headerMap.put("Cookie", MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId="));
		
		String cardNo = "0928030000002523";
		String httpResult = MyHttpClientUtil.httpClient("http://www.scal.com.cn/Web/Home/GetCardParamInfo?CardNO=" + cardNo,
				MyHttpClientUtil.httpPost, null, null, headerMap,
				"124.161.189.47", 12086, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GBK");
		System.out.println(httpResult);
		Map<String, Object> map = MyJsonTransformUtil.readValue(httpResult, Map.class);
		String cardParamModel = map.get("CardParamModel").toString().replaceAll("\\\\", "");
		System.out.println(cardParamModel);
		
		String depCode = "CTU";
		String desCode = "CAN";
		String depDate = "2016-11-21";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("AirlineParamJSON", "{\"AirlineType\":\"Single\",\"IsFixedCabin\":false,\"RouteList\":[{\"RouteIndex\":1,\"OrgCity\":\"" + depCode + "\",\"DesCity\":\"" + desCode + "\",\"FlightDate\":\"" + depDate + "\"}],\"CardParamModel\":" + cardParamModel + ",\"AVType\":1}");
		httpResult = MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpPost, paramMap, headerMap);
		Pattern pattern = Pattern.compile("arrPageValue\\.AirlineParamJSON = (\\{.+\\});");
//		System.out.println(httpResult);
		Matcher m = pattern.matcher(httpResult);
		if(m.find()) {
			String s = m.group(1);
			System.out.println(s);
			System.out.println("----");
			Map<String, Object> map2 = MyJsonTransformUtil.readValue(s, Map.class);
			
			paramMap.clear();
//			headerMap.remove("Cookie");
			paramMap.put("RouteIndex", "1");
			paramMap.put("AVType", map2.get("AVType").toString());
			paramMap.put("AirlineType", map2.get("AirlineType").toString());
			paramMap.put("BuyerType", map2.get("BuyerType").toString());
			paramMap.put("DesCity", desCode);
			paramMap.put("FlightDate", depDate);
			paramMap.put("IsFixedCabin", map2.get("IsFixedCabin").toString());
			paramMap.put("OrgCity", depCode);
			paramMap.put("CardFlag", MyJsonTransformUtil.readValue(cardParamModel, Map.class).get("CardFlag").toString());
			paramMap.put("PassKey", map2.get("PassKey").toString());
			
			System.out.println(MyJsonTransformUtil.writeValue(paramMap));
			httpResult = MyHttpClientUtil.httpClient(url_1, MyHttpClientUtil.httpPost, paramMap, headerMap,
					"124.161.189.47", 12086, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
			System.out.println(httpResult);
		}
	}
	
	public static void u3Air2() throws Exception {
		String url = "http://www.scal.com.cn/ETicket/AirlineList";
		String url_1 = "http://www.scal.com.cn/ETicket/GetSingleChina";
//		
//		String loginUrl = "http://www.scal.com.cn/Web/Account/SignIn";
//		String loginUrl_1 = "http://www.scal.com.cn/Web/Account/SignInAuthValid";
//		
//		String yzmUrl = "http://www.scal.com.cn/Web/Base/GetVerifyCode/1";
//		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
		headerMap.put("Referer", "http://www.scal.com.cn/");
		headerMap.put("Host", "www.scal.com.cn");
		headerMap.put("Cookie", "ASP.NET_SessionId=tflrgyw5qyzjwjsb2cqpdhuv; WT_FPC=id=206f9439851e3c8d3571479176396665:lv=1479176400515:ss=1479176396665; WEBTRENDS_ID=219.137.36.100-3437426816.30555879::E4311D37601E45E0FDFA40961D14FB40; Hm_lvt_207d513b3c253127044258a9e3099f5d=1479176397; Hm_lpvt_207d513b3c253127044258a9e3099f5d=1479176397; testcookie=testvalue; ScalAirlineHistory=Single%2FCTU%2FCAN%2F2016-12-13%2F%2F0; Hm_lvt_ec38b67ee7760ef598731ebbe9f543b9=1479176400; Hm_lpvt_ec38b67ee7760ef598731ebbe9f543b9=1479176400");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String httpResult = null;
//		
//		MyHttpClientResultVo httpVo = MyHttpClientUtil.httpClient(null, loginUrl, MyHttpClientUtil.httpGet, null, null, headerMap, null, null, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, MyHttpClientUtil.defaultCharset);
//		headerMap.put("Cookie", MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "ASP.NET_SessionId="));
//		paramMap.put("UserName", "709871540");
//		paramMap.put("Password", "yd201515");
//		while(true) {
//			MyHttpClientUtil.download(yzmUrl, headerMap, "c:/xxxx.jpg", 10000);
//			String code = MyYzm.antiV3UCode("c:/xxxx.jpg");
//			System.out.println("----" + code + "--");
//			paramMap.put("ValidateCode", code.trim());
//			httpResult = MyHttpClientUtil.httpClient(loginUrl_1, MyHttpClientUtil.httpPost, paramMap, headerMap);
//			System.out.println(httpResult);
//			if(StringUtils.isEmpty(httpResult)) continue ;
//			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
//			Map<String, Object> loginModel = (Map<String, Object> )resultMap.get("LoginModel");
//			System.out.println(loginModel.get("LoginCode") + "---" + loginModel.get("LoginMessage"));
//			if("0".equals(loginModel.get("LoginCode").toString())) break ;
//		}
//		
		String depCode = "CTU";
		String desCode = "CAN";
		String depDate = "2016-12-21";
//		paramMap.clear();
//		paramMap.put("AirlineParamJSON", "{\"AirlineType\":\"Single\",\"IsFixedCabin\":false,\"RouteList\":[{\"RouteIndex\":1,\"OrgCity\":\"" + depCode + "\",\"DesCity\":\"" + desCode + "\",\"FlightDate\":\"" + depDate + "\"}],\"AVType\":0}");
		httpResult = MyHttpClientUtil.httpClient(url, MyHttpClientUtil.httpPost, paramMap, headerMap, "GBK");
		Pattern pattern = Pattern.compile("arrPageValue\\.AirlineParamJSON = (\\{.+\\});");
		System.out.println(httpResult);
		Matcher m = pattern.matcher(httpResult);
		if(m.find()) {
			String s = m.group(1);
			System.out.println(s);
			System.out.println("----");
			Map<String, Object> map = MyJsonTransformUtil.readValue(s, Map.class);
			
			paramMap.clear();
			headerMap.remove("Cookie");
			paramMap.put("AVType", map.get("AVType").toString());
			paramMap.put("AirlineType", map.get("AirlineType").toString());
			paramMap.put("BuyerType", map.get("BuyerType").toString());
			paramMap.put("DesCity", desCode);
			paramMap.put("FlightDate", depDate);
			paramMap.put("IsFixedCabin", map.get("IsFixedCabin").toString());
			paramMap.put("OrgCity", depCode);
			paramMap.put("PassKey", map.get("PassKey").toString());
			paramMap.put("RouteIndex", "1");
			httpResult = MyHttpClientUtil.httpClient(url_1, MyHttpClientUtil.httpPost, paramMap, headerMap, "GBK");
			System.out.println(httpResult);
		}
		
		
		
		
//		headerMap.put("Cookie", "ASP.NET_SessionId=ybwmjtkixjersdxl3oebkuk2;");
//		paramMap.put("UserName", "850754391");
//		paramMap.put("Password", "CAN12345");
//		paramMap.put("ValidateCode", "7qdd");
//		httpResult = MyHttpClientUtil.httpClient(loginUrl_1, MyHttpClientUtil.httpPost, paramMap, headerMap);
//		System.out.println(httpResult);
		
		
		
		
//		headerMap.put("Cookie", "ASP.NET_SessionId=ybwmjtkixjersdxl3oebkuk2;");
//		MyHttpClientUtil.download(yzmUrl, headerMap, "c:/xxxx.jpg", 10000);
//		String code = MyYzm.antiV3UCode("c:/xxxx.jpg");
//		System.out.println(code);
		
		
//		paramMap.clear();
//		paramMap.put("AVType", "0");
//		paramMap.put("AirlineType", "Single");
//		paramMap.put("BuyerType", "0");
//		paramMap.put("DesCity", "SYX");
//		paramMap.put("FlightDate", "2016-12-16");
//		paramMap.put("IsFixedCabin", "false");
//		paramMap.put("OrgCity", "CTU");
//		paramMap.put("PassKey", "3807208448F077E4FBC633AAA6AB2320");
//		paramMap.put("RouteIndex", "1");
//		paramMap.put("IsFixedCabin", "false");
//		String httpResult = MyHttpClientUtil.httpClient(url_1, MyHttpClientUtil.httpPost, paramMap, headerMap, "GBK");
//		String httpResult = MyHttpClientUtil.httpClient(url_1 + "?RouteIndex=1&RouteName=%E5%8D%95%26nbsp%3B%26nbsp%3B%26nbsp%3B%26nbsp%3B%E7%A8%8B&OrgCity=CGO&DesCity=HRB&OrgCityName=%E9%83%91%E5%B7%9E&DesCityName=%E5%93%88%E5%B0%94%E6%BB%A8&FlightDate=2016-01-25&AirlineType=Single&AVType=0&CardFlag=&Flag=&BuyerType=LF04&IsFixedCabin=false&PassKey=109948FE910BEA83E1E9463F721B6637", MyHttpClientUtil.httpPost, paramMap, headerMap);
//		System.out.println(httpResult);
		
//		Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
//		Object airlineListJSON = mapResult.get("AirlineListJSON");
//		mapResult = MyJsonTransformUtil.readValue(airlineListJSON.toString(), Map.class);
//		List<Map<String, Object>> flights = (List<Map<String, Object>>)mapResult.get("SRI_FlightList");
//		for(Map<String, Object> flightMap : flights) {
//			String fltNo = flightMap.get("FlightNo").toString().trim().toUpperCase();
//			// 鍏朵粬鑸变綅
//			List<Map<String, Object>> cabinList = (List<Map<String, Object>>)flightMap.get("SRI_CabinList");
//			if(null == cabinList) cabinList = new ArrayList<Map<String,Object>>();
//			cabinList.add(0, (Map<String, Object>)flightMap.get("CabinModel"));
//			Collections.sort(cabinList, new Comparator<Map<String, Object>>() {
//				@Override
//				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//					BigDecimal o1Price = new BigDecimal(o1.get("RealPrice").toString().trim()).setScale(0);
//					BigDecimal o2Price = new BigDecimal(o2.get("RealPrice").toString().trim()).setScale(0);
//					return o1Price.compareTo(o2Price);
//				}
//			});
//			
//			for(Map<String, Object> cabinMap : cabinList) {
//				String cabinAmoutn = cabinMap.get("NewAmount").toString().trim();
//				System.out.println(fltNo + "\t" + new BigDecimal(cabinMap.get("RealPrice").toString().trim()).setScale(0) + "\t" + cabinAmoutn);
//			}
//		}
	}
	
//	headerMap.put("Accept-Encoding", "gzip, deflate");
//	headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//	headerMap.put("Accept", "text/plain, */*; q=0.01");
//	headerMap.put("Host", "www.tianjin-air.com");
//	headerMap.put("Cache-Control", "no-cache");
//	headerMap.put("Pragma", "no-cache");
//	headerMap.put("Connection", "keep-alive");
//	headerMap.put("X-Requested-With", "XMLHttpRequest");
//	headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	public static void gsAir() throws Exception {
//		Map<String, Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
//		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(null, "http://www.tianjin-air.com",
//				MyHttpClientUtil.httpPost, new HashMap<String, String>(), null, headerMap, "124.161.189.48", 12021, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
//		
//		String jSId = MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Set-Cookie", "JSESSIONID=");
//		System.out.println(jSId);
//		jSId = "JSESSIONID=91DDC2511B6F08A41C42E4E7F7B0C117.g2";
//		
//		headerMap.put("Cookie", jSId);
//		String httpResult = MyHttpClientUtil.httpClient("http://www.tianjin-air.com/flight/searchflight.action?area=LOCAL&tripType=ONEWAY&orgCity=TSN&dstCity=CSX&flightDate=2016-01-21&returnDate=&adult=1&child=0&infant=0&cabinType=economyClass",
//				MyHttpClientUtil.httpPost, new HashMap<String, String>(), headerMap, "124.161.189.48", 12021, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
//		System.out.println(httpResult);
//		
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		
//		headerMap.put("Cookie", jSId);
//		headerMap.put("Referer", "http://www.tianjin-air.com/flight/searchflight.action");
//		String httpResult1 = MyHttpClientUtil.httpClient("http://www.tianjin-air.com/flight/flightresult.action?orgCity=TSN&dstCity=CSX&flightDate=2016-01-21&cabinType=economyClass&isReplace=&fingerPrint=3206884908&hasImgValid=N",
//				MyHttpClientUtil.httpPost, new HashMap<String, String>(), headerMap, "124.161.189.48", 12021, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GB2312");
//		System.out.println(httpResult1);
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.tianjin-air.com");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
		
		String url = "http://www.tianjin-air.com";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientResultVo vo = MyHttpClientUtil.httpClient(httpClient, url, MyHttpClientUtil.httpPost, null, null, headerMap,
				"125.64.91.110", 21163, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GBK");
		String result = vo.getHttpResult();
		System.out.println(result);
		
		for(Header header : vo.getHeaders()) {
			System.out.println(header.getName() + "---" + header.getValue());
		}
		System.out.println("-----------------");
		headerMap.put("Set-Cookie", MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Set-Cookie", "JSESSIONID="));
		
		String location = MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Location");
		vo = MyHttpClientUtil.httpClient(httpClient, location, MyHttpClientUtil.httpGet, null, null, headerMap,
				"125.64.91.110", 21163, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GBK");
		result = vo.getHttpResult();
		System.out.println(result);
		
		for(Header header : vo.getHeaders()) {
			System.out.println(header.getName() + "---" + header.getValue());
		}
		System.out.println("-----------------");

		
		url = "http://www.tianjin-air.com/flight/flightresult.action";
		String param = "orgCity=TSN&dstCity=CSX&flightDate=2016-09-16&cabinType=economyClass&isReplace=&rep=";
		vo = MyHttpClientUtil.httpClient(httpClient, url + "?" + param, MyHttpClientUtil.httpGet, null, null, headerMap,
				"125.64.91.110", 21163, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GBK");
		result = vo.getHttpResult();
		System.out.println(result);
		
		System.out.println("-----------------");
//		
//		
//		url = "http://www.tianjin-air.com/flight/flightresult.action";
//		param = "orgCity=TSN&dstCity=CSX&flightDate=2016-09-16&cabinType=economyClass&isReplace=&rep=";
//		vo = MyHttpClientUtil.httpClient(httpClient, url + "?" + param, MyHttpClientUtil.httpPost, null, null, headerMap,
//				"125.64.91.110", 21163, MyHttpClientUtil.defaultConnTimeout, MyHttpClientUtil.defaultSoTimeout, "GBK");
//		result = vo.getHttpResult();
//		System.out.println(result);
	}
	
	public static void nxAir() throws Exception {
		String url = "https://book.hongkongairlines.com/hxet/reservation/forPassengerInput.do";
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Cookie", "JSESSIONID=0000UZ6giH_WJpba8YrN2wmwQJj:nc9rkl4x1;");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
//		headerMap.put("Referer", "https://book.hongkongairlines.com/hxet/reservation/AVQuery.do?language=CN&orgcity=HKG&dstcity=TXL&takeoffDate=2015-09-28&cabinType=ECONOMY&adultCount=1&childCount=0&tripType=OW&sureDate=1&currencyCode=CNY");
		MyHttpClientSession hcs = new MyHttpClientSession();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("random", "1658");
		paramMap.put("from", "stopOver");
		paramMap.put("cabinType", "ECONOMY");
		paramMap.put("adultCount", "1");
		paramMap.put("childCount", "0");
		paramMap.put("infantCount", "0");
		paramMap.put("language", "CN");
		
		paramMap.put("querySegSize", "1");
		paramMap.put("org_cityCode", "HKG");
		paramMap.put("des_cityCode", "TXL");
		paramMap.put("tripType", "OW");
		paramMap.put("departureDate", "2015-09-28");
		paramMap.put("negoAvailable", "false");
		paramMap.put("flightOW", "[{seg_index:'0',flightNo:'HX336',isSelfSupport:'0',isDirect:'null',org_cityName:'null',org_cityCode:'HKG',des_cityName:'null',des_cityCode:'PEK',departureDate:'2015-09-28 07:40:00',arriveDate:'2015-09-28 10:20:00',duration:'2h40m',stopOffNum:'0',aircraft:'333',availability:'good',flightClass:[{name:'Y',av:'A',subClass:''},{name:'B',av:'A',subClass:''},{name:'H',av:'A',subClass:''},{name:'K',av:'A',subClass:''},{name:'L',av:'A',subClass:''},{name:'M',av:'A',subClass:''},{name:'N',av:'A',subClass:''},{name:'S',av:'A',subClass:''},{name:'X',av:'A',subClass:''},{name:'Q',av:'A',subClass:''},{name:'T',av:'A',subClass:''},{name:'V',av:'A',subClass:''},{name:'W',av:'1',subClass:''}],term:[{dep:'1',arr:'T2'}],mealCode:'B',isCodeShare:'false',carrier:'HX336',asr:'true'},{seg_index:'1',flightNo:'HU489',isSelfSupport:'0',isDirect:'null',org_cityName:'null',org_cityCode:'PEK',des_cityName:'null',des_cityCode:'TXL',departureDate:'2015-09-28 13:20:00',arriveDate:'2015-09-28 17:40:00',duration:'10h20m',stopOffNum:'0',aircraft:'333',availability:'good',flightClass:[{name:'B',av:'A',subClass:''},{name:'H',av:'A',subClass:''},{name:'K',av:'A',subClass:''},{name:'L',av:'A',subClass:''},{name:'M',av:'A',subClass:''},{name:'X',av:'A',subClass:''},{name:'S',av:'4',subClass:''}],term:[{dep:'T2',arr:'--'}],mealCode:'L',isCodeShare:'false',carrier:'HU489',asr:'true'}]");
//		paramMap.put("pek_departureDate", "201509280740");
//		paramMap.put("pek_arriveDate", "201509282340");
//		paramMap.put("pek_departureDate", "201509281535");
//		paramMap.put("pek_arriveDate", "201509292340");
//		paramMap.put("pek_departureDate", "201509282010");
//		paramMap.put("pek_arriveDate", "201509292340");
		String httpResult = hcs.httpPost(url, paramMap, null, null);
//		String httpResult = hcs.httpPost(url, paramMap, "221.10.101.103", 20591);
		System.out.println(httpResult);
		
//		Set<Map<String, String>> list = new HashSet<Map<String,String>>();
//		String[] fls = new String[]{"TSN-XIY"};
//		for(String fl : fls) {
//			String depCode = fl.substring(0, 3);
//			String desCode = fl.substring(4);
//			Calendar calendar = Calendar.getInstance();
//			for(int i=0; i<60; i++) {
//				calendar.add(Calendar.DATE, 1);
//				String depDate = MyDateFormatUtils.SDF_YYYYMMDD().format(calendar.getTime());
//				Map<String, String> map = new HashMap<String, String>();
//				map.put("depCode", depCode);
//				map.put("desCode", desCode);
//				map.put("depDate", depDate);
//				list.add(map);
//			}
//		}
//		for(Map<String, String> map : list) {
//			try {
//				Map<String, Object> paramMap = new HashMap<String, Object>();
//				paramMap.put("cabinType", "economyClass");
//				paramMap.put("fingerPrint", "2375729691");
//				paramMap.put("flightDate", map.get("depDate").replaceAll("-", ""));
//				paramMap.put("hasImgValid", "N");
//				paramMap.put("isReplace", "");
//				paramMap.put("dstCity", map.get("desCode"));
//				paramMap.put("orgCity", map.get("depCode"));
//				String httpResult = hcs.httpPost(url, paramMap, "221.10.101.103", 20591);
//				System.out.println(httpResult);
//				Thread.sleep(1000);
//			} catch(Exception e) {
//				System.out.println(e);
//			}
//		}
	}
	public static void hxAir() throws Exception {
		String url = "https://book.hongkongairlines.com/hxet/reservation/forPassengerInput.do";
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Cookie", "JSESSIONID=0000UZ6giH_WJpba8YrN2wmwQJj:nc9rkl4x1;");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
//		headerMap.put("Referer", "https://book.hongkongairlines.com/hxet/reservation/AVQuery.do?language=CN&orgcity=HKG&dstcity=TXL&takeoffDate=2015-09-28&cabinType=ECONOMY&adultCount=1&childCount=0&tripType=OW&sureDate=1&currencyCode=CNY");
		MyHttpClientSession hcs = new MyHttpClientSession();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("random", "1658");
		paramMap.put("from", "stopOver");
		paramMap.put("cabinType", "ECONOMY");
		paramMap.put("adultCount", "1");
		paramMap.put("childCount", "0");
		paramMap.put("infantCount", "0");
		paramMap.put("language", "CN");
		
		paramMap.put("querySegSize", "1");
		paramMap.put("org_cityCode", "HKG");
		paramMap.put("des_cityCode", "TXL");
		paramMap.put("tripType", "OW");
		paramMap.put("departureDate", "2016-08-28");
		paramMap.put("negoAvailable", "false");
		paramMap.put("flightOW", "[{seg_index:'0',flightNo:'HX336',isSelfSupport:'0',isDirect:'null',org_cityName:'null',org_cityCode:'HKG',des_cityName:'null',des_cityCode:'PEK',departureDate:'2015-09-28 07:40:00',arriveDate:'2015-09-28 10:20:00',duration:'2h40m',stopOffNum:'0',aircraft:'333',availability:'good',flightClass:[{name:'Y',av:'A',subClass:''},{name:'B',av:'A',subClass:''},{name:'H',av:'A',subClass:''},{name:'K',av:'A',subClass:''},{name:'L',av:'A',subClass:''},{name:'M',av:'A',subClass:''},{name:'N',av:'A',subClass:''},{name:'S',av:'A',subClass:''},{name:'X',av:'A',subClass:''},{name:'Q',av:'A',subClass:''},{name:'T',av:'A',subClass:''},{name:'V',av:'A',subClass:''},{name:'W',av:'1',subClass:''}],term:[{dep:'1',arr:'T2'}],mealCode:'B',isCodeShare:'false',carrier:'HX336',asr:'true'},{seg_index:'1',flightNo:'HU489',isSelfSupport:'0',isDirect:'null',org_cityName:'null',org_cityCode:'PEK',des_cityName:'null',des_cityCode:'TXL',departureDate:'2015-09-28 13:20:00',arriveDate:'2015-09-28 17:40:00',duration:'10h20m',stopOffNum:'0',aircraft:'333',availability:'good',flightClass:[{name:'B',av:'A',subClass:''},{name:'H',av:'A',subClass:''},{name:'K',av:'A',subClass:''},{name:'L',av:'A',subClass:''},{name:'M',av:'A',subClass:''},{name:'X',av:'A',subClass:''},{name:'S',av:'4',subClass:''}],term:[{dep:'T2',arr:'--'}],mealCode:'L',isCodeShare:'false',carrier:'HU489',asr:'true'}]");
//		paramMap.put("pek_departureDate", "201509280740");
//		paramMap.put("pek_arriveDate", "201509282340");
//		paramMap.put("pek_departureDate", "201509281535");
//		paramMap.put("pek_arriveDate", "201509292340");
//		paramMap.put("pek_departureDate", "201509282010");
//		paramMap.put("pek_arriveDate", "201509292340");
		String httpResult = hcs.httpPost(url, paramMap, null, null);
//		String httpResult = hcs.httpPost(url, paramMap, "221.10.101.103", 20591);
		System.out.println(httpResult);
		
//		Set<Map<String, String>> list = new HashSet<Map<String,String>>();
//		String[] fls = new String[]{"TSN-XIY"};
//		for(String fl : fls) {
//			String depCode = fl.substring(0, 3);
//			String desCode = fl.substring(4);
//			Calendar calendar = Calendar.getInstance();
//			for(int i=0; i<60; i++) {
//				calendar.add(Calendar.DATE, 1);
//				String depDate = MyDateFormatUtils.SDF_YYYYMMDD().format(calendar.getTime());
//				Map<String, String> map = new HashMap<String, String>();
//				map.put("depCode", depCode);
//				map.put("desCode", desCode);
//				map.put("depDate", depDate);
//				list.add(map);
//			}
//		}
//		for(Map<String, String> map : list) {
//			try {
//				Map<String, Object> paramMap = new HashMap<String, Object>();
//				paramMap.put("cabinType", "economyClass");
//				paramMap.put("fingerPrint", "2375729691");
//				paramMap.put("flightDate", map.get("depDate").replaceAll("-", ""));
//				paramMap.put("hasImgValid", "N");
//				paramMap.put("isReplace", "");
//				paramMap.put("dstCity", map.get("desCode"));
//				paramMap.put("orgCity", map.get("depCode"));
//				String httpResult = hcs.httpPost(url, paramMap, "221.10.101.103", 20591);
//				System.out.println(httpResult);
//				Thread.sleep(1000);
//			} catch(Exception e) {
//				System.out.println(e);
//			}
//		}
	}
	
	public static void muCeair() throws Exception {
		String url = "http://www.ceair.com/otabooking/flight-search!doFlightSearch.shtml";
//		String url = "http://www.flycua.com/otabooking/flight-search!doFlightSearch.shtml";
		
		String params = "{\"tripType\":\"OW\",\"adtCount\":1,\"chdCount\":0,\"infCount\":0,\"currency\":\"CNY\",\"sortType\":\"a\",\"segmentList\":[{\"deptCd\":\"%depCode%\",\"arrCd\":\"%desCode%\",\"deptDt\":\"%depDate%\",\"deptCityCode\":\"%depCode%\",\"arrCityCode\":\"%depDate%\"}],\"sortExec\":\"a\",\"page\":\"0\"}";
		String searchCond = params.replaceAll("%depCode%", "LHR")
									.replaceAll("%desCode%", "SHA")
									.replaceAll("%depDate%", "2015-11-12");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("searchCond", searchCond);
		String httpResult = MyHttpClientUtil.post(url, paramMap);
		System.out.println(httpResult);
		
		Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
		Map<String, Object> airResultDto = (Map<String, Object>)mapResult.get("airResultDto");
		List<Map<String, Object>> productUnits = (List<Map<String, Object>>)airResultDto.get("productUnits");
		for(Map<String, Object> productUnitMap : productUnits) {
			List<Map<String, Object>> oriDestOptions = (List<Map<String, Object>>)productUnitMap.get("oriDestOption");
			for(int i=0; i<oriDestOptions.size(); i++) {
				Map<String, Object> oriDestOptionMap = oriDestOptions.get(i);
				List<Map<String, Object>> flights = (List<Map<String, Object>>)oriDestOptionMap.get("flights");
				if(flights.size() >= 2) System.out.println("vvvvvvvvvvvvvvvv");
			}
		}
		
//		url = "http://www.ceair.com/otabooking/paxinfo-input!init.shtml";
//		
//		Map<String, Object> headerMap = new HashMap<String, Object>();
//		headerMap.put("Cookie", "JSESSIONID=0000mgEgKdlfstH65Q6R_GjTDio:17o8bnes7");
//		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0");
//		MyHttpClientSession hcs = new MyHttpClientSession(headerMap);
//		httpResult = hcs.httpPost(url, paramMap, "112.192.40.247", 12043);
//		System.out.println(httpResult);
		System.out.println("---ok");
	}
	
	public static void tiebaBook() throws Exception {
//		String url = "http://www.bsloong.com/qd/qingdi/page/238/";
//		while(true) {
//			try {
//				System.out.println("url:" + url);
//				Document doc = Jsoup.parse(new URL(url), 10000);
//				Element titleEle = doc.select("#bgdiv > .box_con > .bookname > h1").first();
//				if(null == titleEle) break ;
//				
//				String title = titleEle.text();
//				System.out.println("title:" + title);
//				String html = doc.getElementById("booktext").html();
//				if(!html.contains("鏈珷鑺備负绌虹珷鑺傦紒")) {
//					html = html.substring(html.indexOf("<br>"), html.lastIndexOf("<br>"));
//					html = html.replaceAll("<br>", "");
//					html = html.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\t");
//					html = "\r\n\t\t\t\t\t\t\t\t" + title + "\r\n" + html;
//					MyFileUtils.createFile("c:/", "姝︾澶╀笅.txt", html, true);
//				}
//				url = doc.select("#bgdiv > div.box_con > div.bottem > a").get(3).attr("href");
//				if("index.html".equals(url)) break ;
////				url = "http://www.aiquxs.com/read/2/2007/" + url;
//				MyThreadUtils.sleep(100);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		

//		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
//		try {
//			for(int i=238; i>=0; i--) {
//				String url = "http://www.bsloong.com/qd/qingdi/page/" + i + "/";
//				System.out.println("url:" + url);
//				Document doc = Jsoup.parse(new URL(url), 10000);
//				Elements eleLis = doc.select("body > section.container > div.content-wrap > div.content > article.excerpt");
//				for(int j=eleLis.size()-1; j>=0; j--) {
//					Element eleLi = eleLis.get(j);
//					Element eleA = eleLi.select("header > h2 > a").first();
//					if(null == eleA) continue ;
//					String title = eleA.ownText().trim();
//					if(!title.startsWith("绗�")) continue ;
//					String href = eleA.attr("href");
//					
//					Map<String, String> map = new HashMap<String, String>();
//					map.put("title", title);
//					map.put("href", href);
//					list.add(map);
//				}
//				
//				MyThreadUtils.sleep(500);
//			}
//			
//			System.out.println(MyJsonTransformUtil.writeValue(list));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		List<Map> list1 = MyJsonTransformUtil.readValueToList(new FileInputStream("c:/vvvv.txt"), Map.class);
		for(Map map : list1) {
			try {
				Document doc = Jsoup.connect(map.get("href").toString()).timeout(10000).cookie("Cookie", "Hm_lvt_1e2f3541c3f3559c21daecd94650dc23=1475051991; Hm_lpvt_1e2f3541c3f3559c21daecd94650dc23=1475116297; wordpress_test_cookie=WP+Cookie+check; wordpress_logged_in_4fe63830c9cf4c47149f29c90ac843ba=base2%7C1476326047%7CfAdsDW8DFGnStCsw2eZNRZ06Ytbtn1qV7mVWgoqtyvW%7C6eb1889e63c1390bd83fdf47bbf41e0afdbac5e63bf8f78295b9c42d044c6fdb").get();
				System.out.println("url:" + map.get("href"));
				
				System.out.println("title:" + map.get("title"));
				String html = doc.select("body > section.container > div.content-wrap > div.content > article.article-content").first().html();
//				html = html.substring(html.indexOf("<br>"), html.lastIndexOf("<br>"));
//				html = html.replaceAll("<br>", "");
				html = html.replaceAll("<p>", "  ");
				html = html.replaceAll("</p>", "");
//				html = html.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\t");
				html = "\r\n\t\t\t\t\t\t\t\t" + map.get("title") + "\r\n" + html;
				MyFileUtils.createFile("c:/", "鎯呭笣.txt", html, true);
				MyThreadUtils.sleep(100);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}