package com.aft.app.sc.huicent.b;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.aft.app.sc.HttpConstants;
import com.aft.app.sc.huicent.entity.FlightQueryBean;
import com.aft.app.sc.huicent.entity.FlightQueryResult;
import com.aft.crawl.bean.CrawlCommon;
import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.JobDetail;
import com.aft.logger.MyCrawlerLogger;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

@SuppressWarnings({"unused", "unchecked"})
public class HttpManeger {
	
	public static FlightQueryBean makeFlightQueryBean(int isInter, String depCity,
			String desCity, String goTime, String backTime, int isRoungTrip) {
		// isInter �� ��ʻ��ǹ���
		// depcity, descity, �����У��յ����
		// gotime ,backtime : ��ʱ�䣬����ʱ��
		// isrounDTRIP : �Ƿ�˫��
		FlightQueryBean bean = new FlightQueryBean();

		bean.a("");
		bean.b("");
		bean.a(isInter);
		bean.d(depCity);
		bean.g(desCity);
		bean.j(goTime.replace("-", ""));
		if (isRoungTrip == 0) {
			bean.o("");
		} else {
			bean.o(backTime.replace("-", ""));
		}
		bean.c("");
		bean.e("");
		bean.f("SC");
		bean.h("");
		bean.i("");
		bean.k("");
		bean.l("");
		bean.m("0");
		bean.n("");
		bean.p("");
		bean.q("");
		bean.r("");
		bean.s("");
		bean.t("");
		return bean;
	}

	// ��ѯ����
	@SuppressWarnings("deprecation")
	public static FlightQueryResult queryInnerFlight(String proxyIp, int proxyPort, JobDetail jobDetail, Object content) throws Exception {
		HttpPost httpPost = new HttpPost(HttpConstants.hostName4);
		EntityTools_ct entityTools = new EntityTools_ct();
		FlightQueryBean bean = (FlightQueryBean)content;
		httpPost.setEntity(new EntityTemplate(new ProducerFlightQuery_z(bean, entityTools)));
		
		int soTimeout = MyHttpClientUtil.defaultSoTimeout;
		if(null != jobDetail) soTimeout = CrawlExt.getCrawlExt(jobDetail.getPageType()).getOneWaitTime();
		
		if(null != proxyIp) httpPost.setConfig(RequestConfig.custom().setSocketTimeout(soTimeout).setConnectTimeout(MyHttpClientUtil.defaultConnTimeout).setProxy(new HttpHost(proxyIp, proxyPort)).build());
		else httpPost.setConfig(RequestConfig.custom().setSocketTimeout(soTimeout).setConnectTimeout(MyHttpClientUtil.defaultConnTimeout).build());
		
//		List<Header> headers = new ArrayList<Header>();
//		headers.add(new BasicHeader("User-Agent", "Android"));
//		headers.add(new BasicHeader("Charset", "UTF-8"));
//		httpPost.setHeaders(headers.toArray(new Header[headers.size()]));
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		FlightQueryResult flightQueryResult = new FlightQueryResult();
		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == 200) {
				InputStream responseInput = httpResponse.getEntity().getContent();
				DataInputStream dataInput = new DataInputStream(responseInput);
				entityTools.ParseFlightQueryResult(dataInput, flightQueryResult);
				flightQueryResult.depDate = MyDateFormatUtils.SDF_YYYYMMDD().format(MyDateFormatUtils.SDF_YYYYMMDD_1().parse(bean.l_time));
			} else if(httpResponse.getStatusLine().getStatusCode() == 502) {
				flightQueryResult.status = "S0002";
				flightQueryResult.errorMsg = "代理 502";
			} else {
				flightQueryResult.status = "S0002";
				flightQueryResult.errorMsg = "返回状态不在200, 而是[" + httpResponse.getStatusLine().getStatusCode() + "]";
			}
		} catch (Exception e) {
			if(null != jobDetail) MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", sc app 请求异常:\r", e);
//			else e.printStackTrace();
			System.out.println(e);
		} finally {
			httpClient.close();
		}
		return flightQueryResult;
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		FlightQueryBean  bean = HttpManeger.makeFlightQueryBean( 0, "CAN", "XMN" , "2016-11-29", "", 0);
		FlightQueryResult result1 = null;
		
		System.out.println(HttpConstants.getTokenId());
		result1 = HttpManeger.queryInnerFlight("119.5.44.20", 12078, null, bean);
		System.out.println(ReflectionToStringBuilder.toString(result1, ToStringStyle.DEFAULT_STYLE));
		if(null != result1) System.out.println(result1.toJson());
//		
//		MyThreadUtils.sleep(1000);
//		result1 = HttpManeger.queryInnerFlight("125.64.91.83", 21543, null, bean);
//		System.out.println(ReflectionToStringBuilder.toString(result1, ToStringStyle.DEFAULT_STYLE));
//		if(null != result1) System.out.println(result1.toJson());
//		
//		MyThreadUtils.sleep(1000);
//		result1 = HttpManeger.queryInnerFlight("125.64.91.84", 21403, null, bean);
//		System.out.println(ReflectionToStringBuilder.toString(result1, ToStringStyle.DEFAULT_STYLE));
//		if(null != result1) System.out.println(result1.toJson());
//		
//		MyThreadUtils.sleep(1000);
//		result1 = HttpManeger.queryInnerFlight("125.64.91.85", 21093, null, bean);
//		System.out.println(ReflectionToStringBuilder.toString(result1, ToStringStyle.DEFAULT_STYLE));
//		if(null != result1) System.out.println(result1.toJson());
//		
//		MyThreadUtils.sleep(1000);
//		result1 = HttpManeger.queryInnerFlight("125.64.91.87", 20703, null, bean);
//		System.out.println(ReflectionToStringBuilder.toString(result1, ToStringStyle.DEFAULT_STYLE));
//		if(null != result1) System.out.println(result1.toJson());
		
//		String httpResult = MyHttpClientUtil.get("http://192.168.0.252:5000/ipProxy/all.action");
//		Map<String, Object> jsonMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
//		List<String> ipList = (List<String>)jsonMap.get("msg");
//		
//		String proxy = ipList.get(new Random().nextInt(59));
//		System.out.println(proxy);
//		String[] proxys = proxy.split(":");
//		FlightQueryResult result1 = HttpManeger.queryInnerFlight(proxys[0], Integer.parseInt(proxys[1]), null, bean);
//		System.out.println(ReflectionToStringBuilder.toString(result1, ToStringStyle.DEFAULT_STYLE));
//		if(null != result1) System.out.println(result1.toJson());
		
		
//		for(String proxy : ipList) {
//			System.out.println("proxy:" + proxy);
//			String[] proxys = proxy.split(":");
//			FlightQueryResult result1 = HttpManeger.queryInnerFlight(proxys[0], Integer.parseInt(proxys[1]), null, bean);
//			System.out.println(ReflectionToStringBuilder.toString(result1, ToStringStyle.DEFAULT_STYLE));
//			if(null != result1) System.out.println(result1.toJson());
//			MyThreadUtils.sleep(5000);
//			
//			System.out.println();
//			System.out.println();
//		}
	}
}