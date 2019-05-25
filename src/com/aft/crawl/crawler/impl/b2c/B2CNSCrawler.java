package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 河北航空 
 */
public class B2CNSCrawler extends Crawler {

	private final static String queryUrl = "http://www.hbhk.com.cn/dwr/call/plaincall/flightQueryServer.readIBE.dwr";
	
	private final static String params = "callCount=1\rwindowName=\rc0-scriptName=flightQueryServer\rc0-methodName=readIBE\rc0-id=0\rc0-param0=string:01\rc0-param1=string:%depCode%\rc0-param2=string:%desCode%\rc0-param3=string:\rc0-param4=string:%depDate%\rc0-param5=string:\rbatchId=4\rpage=%2FflightQuery.action\rhttpSessionId=\rscriptSessionId=700A199024C4F281C0E70D36B1CD0C6C";
	
	private final static Pattern pattern = Pattern.compile("flightResultStart=(\\[\\{.*\\}\\]);", Pattern.DOTALL);
	
	public B2CNSCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		Matcher matcher = pattern.matcher(httpResult);
		if(!matcher.find()) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			List<Map> listResult = (List<Map>)MyJsonTransformUtil.readValueToList(matcher.group(1), Map.class);
			
			String fltNoTemp = null;	// 只要最低价, 最低价放第一个的, 所以
			CrawlResultB2C crawlResult = null;
			for(Map map : listResult) {
				// 航班号,航司
				String fltNo = map.get("flightNo").toString().trim().toUpperCase();
				if(null != fltNoTemp && fltNoTemp.equals(fltNo)) continue ;
				fltNoTemp = fltNo;
				String airlineCode = fltNo.substring(0, 2);
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				String tStar = map.get("tStar").toString().trim();
				String tDest = map.get("tDest").toString().trim();
				// 出发日期
				String depDate = tStar.substring(0, 10);
				
				// 出发机场
				String depCode = map.get("cStar").toString().trim();
				
				// 到达机场
				String desCode = map.get("cDest").toString().trim();
				
				// 出发时间
				String depTime = tStar.substring(11, 16);
				
				// 到达时间
				String desTime = tDest.substring(11, 16);
				
				// 舱位
				String cabin = map.get("seatInfo").toString().trim().toUpperCase();
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
				crawlResult.setDepTime(depTime);	// 出发时间
				crawlResult.setDesTime(desTime);	// 到达时间
				
				// 剩余座位数
				crawlResult.setRemainSite(Integer.parseInt(map.get("surplusSeatNum").toString().trim()));
				
				// 价格
				crawlResult.setTicketPrice(new BigDecimal(map.get("ticketPrice").toString().trim()).setScale(0));
				crawlResult.setSalePrice(crawlResult.getTicketPrice());
				crawlResults.add(crawlResult);
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	public String httpResult() throws Exception {
		String httpContent = params.replaceAll("%depCode%", this.getJobDetail().getDepCode())
							.replaceAll("%desCode%", this.getJobDetail().getDesCode())
							.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return this.httpProxyPost(queryUrl, httpContent, "other");
	}
}