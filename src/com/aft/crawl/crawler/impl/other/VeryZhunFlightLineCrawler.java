package com.aft.crawl.crawler.impl.other;

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
 * 非常准 航班动态 航线页面
 */
public class VeryZhunFlightLineCrawler extends Crawler {

	private final static String queryUrl = "http://m.veryzhun.com/flightstatus/search.asp?fafrom=%depCode%&fato=%desCode%";
	
	public VeryZhunFlightLineCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			Elements eleLis = document.select("#main > div.homeNavigation > li");
			if(null == eleLis || eleLis.isEmpty() || eleLis.size() <= 8 || eleLis.get(2).ownText().contains("抱歉")) {
				logger.info(this.getJobDetail().toStr() + ", 不存在页码信息!" + document.text());
				return crawlResults;
			}
			
			if(0 != (eleLis.size() - 7) % 8) {
				logger.warn(this.getJobDetail().toStr() + ", 航班li数量有问题....");
				return crawlResults;
			}
			
			int flightAmount = (eleLis.size() - 7) / 8;
			logger.info(this.getJobDetail().toStr() + ", 航班数:" + flightAmount);
			int index = 2;
			CrawlResultFlightChange crawlResult = null;
			for(int i=0; i<flightAmount; i++) {
				crawlResult = new CrawlResultFlightChange(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate());
				
				// 航班号
				String fltNo = eleLis.get(index++).child(0).ownText().trim();
				crawlResult.setFltNo(fltNo.substring(fltNo.indexOf(":") + 1).trim());
				logger.debug(this.getJobDetail().toStr() + "-" + i + ", 航班号:" + fltNo);
				
				// 航司
				crawlResult.setAirlineCode(crawlResult.getFltNo().substring(0, 2));
				
				// 出发 计划起飞时间
				String planDepTime = eleLis.get(index++).ownText().trim();
				crawlResult.setPlanDepTime(planDepTime.substring(planDepTime.indexOf("：") + 1).trim());
				
				// 到达 计划起飞时间
				String planDesTime = eleLis.get(index++).childNode(0).toString().trim();
				crawlResult.setPlanDesTime(planDesTime.substring(planDesTime.indexOf("：") + 1).trim());
				
				Element eleLi = eleLis.get(index++);
				// 出发 实际起飞时间
				String depTime = eleLi.childNode(0).toString().trim();
				crawlResult.setDepTime(depTime.substring(depTime.indexOf("：") + 1).trim());
				
				// 到达 实际起飞时间
				String desTime = eleLi.childNode(2).toString().trim();
				crawlResult.setDesTime(desTime.substring(desTime.indexOf("：") + 1).trim());
				
				index += 2;
				
				// 飞机状态：计划航班
				String status = eleLis.get(index++).child(0).ownText().trim();
				crawlResult.setStatus(status);
				crawlResults.add(crawlResult);
				++index;
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	
	@Override
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
		return document.title().contains("登录");
	}

	@Override
	public String httpResult() throws Exception {
		String url = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode());
		return super.httpProxyGet(url, "GBK", "html");
	}
}