package com.aft.crawl.crawler.impl.other;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.flightchange.CrawlResultFlightChange;

/**
 * 携程 航班动态
 */
public class CtripFlightChangeCrawler extends Crawler {

	private final static String queryUrl = "http://flights.ctrip.com/actualtime/fno-%fltNo%/t%depDate%";
	
	public CtripFlightChangeCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			Element eleDynamicTable = document.select("#base_bd > div.clearfix > div.base_main > div.table_inner > table.dynamic_table").first();
			if(null == eleDynamicTable) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班号信息!" + document.text());
				return null;
			}
			Elements eleTrs = eleDynamicTable.select("> tbody > tr");
			for(int i=1; i<eleTrs.size(); i++) {
				Element eleTr = eleTrs.get(i);
				logger.info(this.getJobDetail().toStr() + ", 航班号信息!" + eleTr.text());
				
				String depCode = eleTr.attr("acity").trim();
				String desCode = eleTr.attr("dcity").trim();
				
				Elements eleTds = eleTr.children();
				String fltNo = eleTds.get(0).select("> strong").first().ownText().trim();
				CrawlResultFlightChange crawlResult = new CrawlResultFlightChange(this.getJobDetail(), fltNo, this.getJobDetail().getDepDate());
				
				crawlResult.setDepCode(depCode);	// 出发三字码
				crawlResult.setDesCode(desCode);	// 到达三字码
				
				// 出发 计划起飞时间
				String planDepTime = eleTds.get(1).select("> div > strong").first().ownText().trim();	// 19:20
				int maoHaoIndex = planDepTime.indexOf(":");
				crawlResult.setPlanDepTime(planDepTime.substring(maoHaoIndex - 2, maoHaoIndex + 3));
				
				// 到达 计划起飞时间
				String planDesTime = eleTds.get(3).select("> div > strong").first().ownText().trim();
				maoHaoIndex = planDesTime.indexOf(":");
				crawlResult.setPlanDesTime(planDesTime.substring(maoHaoIndex - 2, maoHaoIndex + 3));
				
				// 准点率
				String zdl = eleTds.get(4).ownText().trim();	// 历史准点率：73.33%
				if(!zdl.contains("- -")) crawlResult.setZdl(new BigDecimal(zdl.substring(0, zdl.length() - 1)));	// 73.33%
				
				// 飞机状态：计划航班
				String status = eleTds.get(5).select("> strong").first().ownText().trim();
				crawlResult.setStatus(status);
				
				
				// 实际 16:24                 
				// 出发 实际起飞时间
				Elements eleTimes = eleTds.get(6).children();
				String eleTime1 = eleTimes.first().ownText().trim();
				if(eleTime1.contains("实际")) {
					crawlResult.setDepTime(eleTime1.substring(eleTime1.indexOf("实际") + 2).trim());
					
					// 到达 实际起飞时间
					String eleTime2 = eleTimes.last().ownText().trim();
					crawlResult.setDesTime(eleTime2.substring(eleTime2.indexOf("实际") + 2).trim());
				}
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
		String url = queryUrl.replaceAll("%fltNo%", this.getJobDetail().getFltNo())
								.replaceAll("%depDate%", this.getJobDetail().getDepDate().replaceAll("-", ""));
		return super.httpProxyGet(url, "GBK", "html");
	}
}