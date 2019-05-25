package com.aft.crawl.crawler.impl.b2c;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 阿提哈德航空 
 */
public class B2CEYCrawler extends Crawler {

	private final static String queryUrl = "https://booking.etihad.com/SSW2010/EYEY/webqtrip.html?alternativeLandingPage=true&lang=zh&origin=%depCode%&destination=%desCode%&journeySpan=OW&departureDate=%depDate%&cabinClass=ECONOMY&numAdults=1&numChildren=0&numInfants=0&promoCode=&callbacktype=formbookflight&searchHotels=false&searchCars=false&cid=";

	
	public B2CEYCrawler(String threadMark) {
		super(threadMark);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout() || httpResult.indexOf("搜索页")!=-1) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Pattern p = Pattern.compile("var\\s+templateData\\s+=\\s+([\\s\\S]+?)};");
			Matcher m = p.matcher(httpResult);
			if(m.find()){
				String flights = m.group(1)+"}";
				logger.info(this.getJobDetail().toStr() + ", 采集航班信息:"+flights);
				Map<String, Object> mapResult = MyJsonTransformUtil.readValue(flights, Map.class);
				Map<String, Object> list_tab = (Map<String, Object>)mapResult.get("rootElement");
				if(list_tab==null) return null;
				//航班信息
				List<Map<String, Object>> list_proposed_bound = (List<Map<String, Object>>)list_tab.get("children");
				List<Map<String, Object>> list_recommendation = (List<Map<String, Object>>)list_proposed_bound.get(1).get("children");
				List<Map<String, Object>> list_bound = (List<Map<String, Object>>)list_recommendation.get(0).get("children");
				Map<String, Object> model = (Map<String, Object>)list_bound.get(4).get("model");
				List<Map<String, Object>>  outbounds = (List<Map<String, Object>>)model.get("outbounds");
				
				for(Map<String, Object> outbound:outbounds){
					Map<String, Object>  basketsRef = (Map<String, Object>)outbound.get("basketsRef");
					List<Map<String, Object>>  segments = (List<Map<String, Object>>)outbound.get("segments");
					if(segments.size()>1)continue;//暂时只需要单航段的
					Map<String, Object> segment = segments.get(0);
					List<Integer> flightNumbers = (List<Integer>)segment.get("flightNumber");
					Integer flightNumber = flightNumbers.get(0);//航班号
					String departureDate = String.valueOf(segment.get("departureDate"));
					String depTime = departureDate.substring(11, 16);
					String arrivalDate = String.valueOf(segment.get("arrivalDate"));
					String desTime = arrivalDate.substring(11, 16);
					
					for(String key :basketsRef.keySet()){
						Map<String, Object> values = (Map<String, Object>)basketsRef.get(key);
						Map<String, Object> seatsRemaining  = (Map<String, Object>)values.get("seatsRemaining");
						String cabin = String.valueOf(seatsRemaining.get("customLabelSuffix"));//仓位
						String remainSite = String.valueOf(seatsRemaining.get("seatsRemaining"));//座位数
						
						Map<String, Object> prices  = (Map<String, Object>)values.get("prices"); 
						Map<String, Object> currencyForFirstMoneyElement = (Map<String, Object>)prices.get("currencyForFirstMoneyElement");
						String currency = String.valueOf(currencyForFirstMoneyElement.get("code"));
						List<Map<String, Object>> moneyElements  = (List<Map<String, Object>>)prices.get("moneyElements");
						
						BigDecimal taxFee = new BigDecimal(0);
						BigDecimal ticketPrice = new BigDecimal(0);
						for(Map<String, Object> moneyElement:moneyElements){
							
							Map<String, Object> moneyTO = (Map<String, Object>)moneyElement.get("moneyTO");
							
							String amount = String.valueOf(moneyTO.get("amount"));
							
							if(moneyElement.get("type").equals("TAX")){
								taxFee = taxFee.add(new BigDecimal(amount));
							}else if(moneyElement.get("type").equals("FARE")){
								ticketPrice = ticketPrice.add(new BigDecimal(amount));
							}
						}
						// 判断共享
						String shareFlight = this.getShareFlight("EY");
						CrawlInterResult crawlResult = new CrawlInterResult(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate());
						crawlResult.setDepTime(depTime);
						crawlResult.setDesTime(desTime);
						crawlResult.setShareFlight(shareFlight);
						crawlResult.setFltNo("EY"+flightNumber);
						crawlResult.setCabin(cabin);
						crawlResult.setAirlineCode("EY");
						crawlResult.setCurrency(currency);
						
						// 剩余座位数
						if(remainSite==null||"".equals(remainSite)||"null".equals(remainSite))remainSite="A";
						crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(1 == remainSite.length() ? remainSite : remainSite.substring(1)));
						if(crawlResult.getRemainSite() <= 0) continue ;
						
						crawlResult.setTaxFee(taxFee);
						crawlResult.setTicketPrice(ticketPrice);
						crawlResults.add(crawlResult);
					}
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} 
		return crawlResults;
	}

	@Override
	public String httpResult() throws Exception {
		
		String httpContent = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode())
								.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return this.httpProxyGet(httpContent, "utf-8", "html");
	}
	
	public static void main(String[] args) throws IOException {
		File file = new File("d:\\system.txt");
		FileReader reader = new FileReader(file);
		int fileLen = (int)file.length();
		char[] chars = new char[fileLen];
		reader.read(chars);
		String html = String.valueOf(chars);

		Pattern p = Pattern.compile("var\\s+templateData\\s+=\\s+([\\s\\S]+?)};");
		Matcher m = p.matcher(html);
		if(m.find()){
			System.out.println(m.group(1));
		}
		System.out.println("结束...");
	}
}