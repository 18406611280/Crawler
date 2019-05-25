package com.aft.crawl.crawler.impl.b2c;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 泰国航空 
 */
public class B2CTGCrawler extends Crawler {

	private final static String queryUrl = "http://booking.thaiairways.com/AIP_ENCTRIPNEW/tripFlowIEncrypt";
	
	private final static String qparams = "txtDepCity=%depCode%&txtArrCity=%desCode%&TripType=O&lstTravellerAdults=1&lstTravellerChildren=0&lstTravellerInfants=0&txtCabinClass=PE&lstDepDay=%depDated%&lstDepMonth=%depDateym%&ex9=China&external_id=CN&LANGUAGE=CN";
	
	private final static String brandQueryUrl = "https://wftc3.e-travel.com/plnext/tgpnext/Override.action";
	
	private final String refererUrl = "http://www.thaiairways.com/zh_CN/book_my_flights/flights/book_flights.page?section=booking";

	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	public B2CTGCrawler(String threadMark) {
		super(threadMark);
		this.headerMap.put("Referer", this.refererUrl);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			if(null != document.getElementById("#form1")) return null;
			String enct = document.select("#form1 > input[name=ENCT]").first().val();
			String enc = document.select("#form1 > input[name=ENC]").first().val();
			String external_id = document.select("#form1 > input[name=EXTERNAL_ID]").first().val();
			String embedded_transaction = document.select("#form1 > input[name=EMBEDDED_TRANSACTION]").first().val();
			String site = document.select("#form1 > input[name=SITE]").first().val();
			String language = document.select("#form1 > input[name=LANGUAGE]").first().val();
			String matrix_calendar = document.select("#form1 > input[name=MATRIX_CALENDAR]").first().val();
			String pricing_type = document.select("#form1 > input[name=PRICING_TYPE]").first().val();
			 
			logger.info("ENCT="+enct+"&EXTERNAL_ID="+external_id+"&EMBEDDED_TRANSACTION="+embedded_transaction+
					"&SITE="+site+"&LANGUAGE="+language+"&MATRIX_CALENDAR="+matrix_calendar+
					"&PRICING_TYPE="+pricing_type+"&ENC="+enc);
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("ENCT", enct);
			paramMap.put("ENC", enc);
			paramMap.put("EXTERNAL_ID", external_id);
			paramMap.put("EMBEDDED_TRANSACTION", embedded_transaction);
			paramMap.put("SITE", site);
			paramMap.put("LANGUAGE", language);
			paramMap.put("MATRIX_CALENDAR", matrix_calendar);
			paramMap.put("PRICING_TYPE", pricing_type);

			httpResult = this.httpProxyPost(brandQueryUrl, paramMap, "gb2312", "html");
			if(this.isTimeout()) return crawlResults;
			
			String[] generatedJSons = httpResult.split("generatedJSon");
			if(generatedJSons.length>=2){
				String reg = "\\s+new\\s+String\\('(.+)'\\);\\s+var\\s+jsonExpression";
				Pattern p = Pattern.compile(reg);
				Matcher m = p.matcher(generatedJSons[1]);
				if(m.find()){
					String flights = m.group(1);
					logger.info(this.getJobDetail().toStr() + ", 采集航班信息:"+flights);
					Map<String, Object> mapResult = MyJsonTransformUtil.readValue(flights, Map.class);
					Map<String, Object> list_tab = (Map<String, Object>)mapResult.get("list_tab");
					if(list_tab==null) return crawlResults;
					//航班信息
					List<Map<String, Object>> list_proposed_bound = (List<Map<String, Object>>)list_tab.get("list_proposed_bound");
					//价格信息
					List<Map<String, Object>> list_recommendation = (List<Map<String, Object>>)list_tab.get("list_recommendation");
					
					for(Map<String, Object> recommendation : list_recommendation){
						BigDecimal ticketPrice = new BigDecimal(0);//票面价
						BigDecimal taxFee = new BigDecimal(0);//税费
						String currency = "CNY";
						List<Map<String, Object>> list_bound = (List<Map<String, Object>>)recommendation.get("list_bound");
						List<Map<String, Object>> list_trip_price = (List<Map<String, Object>>)recommendation.get("list_trip_price");
						for(Map<String, Object> trip_price :list_trip_price){
							ticketPrice = new BigDecimal(String.valueOf(trip_price.get("amount_without_tax")));
							taxFee = new BigDecimal(String.valueOf(trip_price.get("tax")));
							Map<String, Object> currencyMap = (Map<String, Object>)trip_price.get("currency");
							currency = String.valueOf(currencyMap.get("code"));
						}
						for(Map<String, Object> bound :list_bound){
							List<Map<String, Object>> list_flight = (List<Map<String, Object>>)bound.get("list_flight");
							for(Map<String, Object> flight : list_flight){
								Map<String, Object> lsa_debug_info = (Map<String, Object>)flight.get("lsa_debug_info");
								String cabin = String.valueOf(lsa_debug_info.get("rbd"));//舱位
								String flight_number = String.valueOf(lsa_debug_info.get("first_flight_number"));//航班编号(615)
								
								loop:for(Map<String, Object> proposed_bound : list_proposed_bound){
									List<Map<String, Object>> list_flight_pb = (List<Map<String, Object>>)proposed_bound.get("list_flight");
									for(Map<String, Object> flight_pb : list_flight_pb){
										List<Map<String, Object>> list_segment  = (List<Map<String, Object>>)flight_pb.get("list_segment");
										for(Map<String, Object> segment:list_segment){
											String flight_number_s = String.valueOf(segment.get("flight_number"));
											if(!flight_number_s.equals(flight_number))continue;
											String depTime = String.valueOf(segment.get("b_date_formatted_time"));
											String desTime = String.valueOf(segment.get("e_date_formatted_time"));
											Map<String, Object> airline =  (Map<String, Object>)segment.get("airline");
											String airlineCode = String.valueOf(airline.get("code"));
											String fltNo = airlineCode+flight_number;
											
											// 判断共享
											String shareFlight = this.getShareFlight(airlineCode);
											
											CrawlInterResult crawlResult = new CrawlInterResult(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate());
											crawlResult.setDepTime(depTime);
											crawlResult.setDesTime(desTime);
											crawlResult.setShareFlight(shareFlight);
											crawlResult.setFltNo(fltNo);
											crawlResult.setCabin(cabin);
											crawlResult.setAirlineCode(airlineCode);
											crawlResult.setCurrency(currency);
											
											// 剩余座位数
											String remainSite = String.valueOf(flight.get("number_of_last_seats"));
											if(remainSite==null||"".equals(remainSite)||"null".equals(remainSite))remainSite="A";
											crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(1 == remainSite.length() ? remainSite : remainSite.substring(1)));
											if(crawlResult.getRemainSite() <= 0) continue ;
											
											crawlResult.setTaxFee(taxFee);
											crawlResult.setTicketPrice(ticketPrice);
											
											crawlResults.add(crawlResult);
											break loop;
										}
									}
								}
							}
						}
					}		
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}

	@Override
	public String httpResult() throws Exception {
		
		String[] depDates = this.getJobDetail().getDepDate().split("-");
		
		String httpContent = qparams.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode())
								.replaceAll("%depDateym%", depDates[0]+depDates[1])
								.replaceAll("%depDated%", depDates[2]);
		return this.httpProxyGet(queryUrl+"?"+httpContent, "utf-8", "html");
	}
	
	public static void main(String[] args) throws IOException {
		File file = new File("d:\\system.txt");
		FileReader reader = new FileReader(file);
		int fileLen = (int)file.length();
		char[] chars = new char[fileLen];
		reader.read(chars);
		String html = String.valueOf(chars);

		Pattern p = Pattern.compile("addRecommendation\\(([\\s\\S]+?)\\);[\\s\\S]+?addFormatedRecommendation\\(([\\s\\S]+?)\\);");
		Matcher m = p.matcher(html);
		while(m.find()){
			String s = m.group(1).replaceAll("addRecommendation\\(", "").replaceAll("\\);", "").trim();
			String[] a = s.split(",");
			for(int i=0;i<a.length;i++){
				System.out.println(i+"="+a[i].trim());
			}
			System.out.println("结束1");
			s = m.group(2).replaceAll("addRecommendation\\(", "").replaceAll("\\);", "").trim();
			a = s.split(",");
			for(int i=0;i<a.length;i++){
				System.out.println(i+"="+a[i].trim());
			}
			System.out.println("结束2");
		}
		System.out.println("结束...");
	}
}