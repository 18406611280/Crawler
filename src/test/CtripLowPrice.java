package test;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CtripLowPrice {

	private final static CloseableHttpClient httpClient = HttpClients.createDefault();
	
	public static void main(String[] args) throws Exception {
		HttpPost httpPost = new HttpPost("https://trade.ctrip.com/IntlFlightTrade/");

		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("UserID", "广州市一达二部国际平台"));
		paramList.add(new BasicNameValuePair("Password", "@qun86552402"));
		httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
		HttpResponse httpResponse = httpClient.execute(httpPost);	// 提交请求
		String httpResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
		if(!httpResult.contains("<title>Object moved</title>")) {
			System.out.println("登录失败...\r" + httpResult);
			return ;
		}
		
		
		// 这里写循环...或并发
		httpPost = new HttpPost("https://trade.ctrip.com/IntlFlightTrade/LowPrice");
		paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("TripType", "OW"));
		paramList.add(new BasicNameValuePair("ProductType", "All"));
		paramList.add(new BasicNameValuePair("IsHasTax", "False"));
		paramList.add(new BasicNameValuePair("DepartCity", "HKG"));
		paramList.add(new BasicNameValuePair("ArriveCity", "SIN"));
		paramList.add(new BasicNameValuePair("Owner", ""));
		paramList.add(new BasicNameValuePair("SeatGrade", "Y"));
		paramList.add(new BasicNameValuePair("PassengerNum", "1"));
		paramList.add(new BasicNameValuePair("PassengerEligibility", "ADT"));
		paramList.add(new BasicNameValuePair("OutboundTravelDate", "2016-06-28"));
		paramList.add(new BasicNameValuePair("LowPriceSort", "Price"));
		httpPost.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
		httpResponse = httpClient.execute(httpPost);	// 提交请求
		httpResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
		
		Document doc = Jsoup.parse(httpResult);
		Elements trs = doc.select("#search-result-table > tbody > tr");
		for(Element tr : trs) {
			for(Element td : tr.select("> td")) {
				System.out.print(td.text() + "\t\t");
			}
			System.out.println();
		}
	}
}