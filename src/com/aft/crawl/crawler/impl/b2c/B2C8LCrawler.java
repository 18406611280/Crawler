package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 祥鹏航空 
 */
public class B2C8LCrawler extends Crawler {
	
	private final static String indexUrl = "http://www.luckyair.net/";
	
	private final static String queryUrl = "http://www.luckyair.net/flightresult/flightresult2016.action";
	
	public B2C8LCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			Elements eleInputs = document.select("body > div.cabin_con > div.legPrice > input[name=selectedFlight]");
			if(null == eleInputs || eleInputs.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			Map<String, Element> lowMap = new HashMap<String, Element>();
			for(Element eleInput : eleInputs) {
				String code = eleInput.attr("code");
				Element temp = lowMap.get(code);
				if(null == temp) lowMap.put(code, eleInput);
				else if(new BigDecimal(temp.attr("price")).compareTo(new BigDecimal(eleInput.attr("price"))) > 0) lowMap.put(code, eleInput);
			}
			
			CrawlResultB2C crawlResult = null;
			Iterator<Map.Entry<String, Element>> it = lowMap.entrySet().iterator();
			while(it.hasNext()) {
				Element eleInput = it.next().getValue();
//				<input type="hidden"  depTime="2016-09-11 08:25" code="MZMK" price="300.0" cabinCode="I" baseFare="1270.0" seatnum="10"
//					returnPoint="0.0" name="selectedFlight" airporttax="50.0" fuletax="0.0"
//					remark="变更费:不允许变更-不允许变更@退票费:不允许退票-不允许退票@签转:不允许-不允许@不提供免费餐食@不允许退票和变更@不提供免费托运行李@可携带一件重量不超过5公斤体积不超过20×40×55CM的手提行李进入客舱。"
//				  	value="I;8L;9930;JHG;DLU;MZMK;2016-09-11 08:25;2016-09-11 09:20;300.0;50.0;;0.0;1270.0;300.0;0.0;130.0;0.0;false;0.0;0.0;0.0;10;"/> 
				String[] values = eleInput.val().trim().split(";");
				// 航班号,航司
				String airlineCode = values[1].toUpperCase().trim();
				String fltNo = airlineCode + values[2].trim();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				// 舱位
				String cabin = values[0].toUpperCase().trim();
				
				String depCode = values[3].toUpperCase().trim();
				String desCode = values[4].toUpperCase().trim();
				
				// 出发时间
				String depTime = values[6].trim().substring(11);
				
				// 到达时间
				String desTime = values[7].trim().substring(11);
				
				
				String depDate = eleInput.attr("deptime").trim().substring(0, 11);
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
				
				crawlResult.setDepTime(depTime);	// 出发时间
				crawlResult.setDesTime(desTime);	// 到达时间
				
				// 剩余座位数
				crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(eleInput.attr("seatnum").trim()));
				
				crawlResult.setType(eleInput.attr("code").trim());
				
				// 价格
				BigDecimal ticketPrice = new BigDecimal(eleInput.attr("price").trim());
				crawlResult.setTicketPrice(ticketPrice);
				crawlResult.setSalePrice(crawlResult.getTicketPrice());
				crawlResults.add(crawlResult);
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		} finally {
			MyThreadUtils.sleep(5000);	// 中间要停顿5秒
		}
		return crawlResults;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String httpResult() throws Exception {
		if(null == this.getBaseJobParamMap()) {
			synchronized(indexUrl) {
				if(null == this.getBaseJobParamMap()) {
					MyHttpClientResultVo resultVo = super.httpProxyResultVoGet(indexUrl, "html");
					headerMap.put("Cookie", MyHttpClientUtil.getHeaderValue(resultVo.getHeaders(), "Set-Cookie", "JSESSIONID="));
					this.putBaseJobParamMap(headerMap);
				}
			}
		} else headerMap.put("Cookie", this.getBaseJobParamMap().get("Cookie").toString());
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("desc", "");
		paramMap.put("index", "1");
		paramMap.put("flightseq", "1");
		paramMap.put("orgCity", this.getJobDetail().getDepCode());
		paramMap.put("dstCity", this.getJobDetail().getDesCode());
		paramMap.put("flightDate", this.getJobDetail().getDepDate());
		return super.httpProxyPost(queryUrl, paramMap, "html");
	}
}