package com.aft.crawl.crawler.impl.other;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.flightchange.CrawlResultFlightChange;

/**
 * 非常准 航班动态
 */
public class VeryZhunFltNoCrawler extends Crawler {

	private final static String queryUrl = "http://www.veryzhun.com/searchnum.asp?flightnum=%fltNo%";
	
	public VeryZhunFltNoCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			Elements eleNumdaps = document.select("#pagehdr > div.center > div.searchnum > div.numdap");
			Elements eleNuminfos = document.select("#pagehdr > div.center > div.searchnum > div.numinfo");
			Elements eleNumarrs = document.select("#pagehdr > div.center > div.searchnum > div.numarr");
			if(eleNumarrs.isEmpty() || eleNuminfos.isEmpty() || eleNumarrs.isEmpty()) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班号信息!" + document.text());
				return null;
			}
			
			String fltNo = eleNuminfos.first().select("> ul > li.num_here").first().ownText().trim().toUpperCase();
			for(int i=0; i<eleNumdaps.size(); i++) {
				Element eleNumdap = eleNumdaps.get(i);
				Element eleNuminfo = eleNuminfos.get(i);
				Element eleNumarr = eleNumarrs.get(i);
				
				CrawlResultFlightChange crawlResult = new CrawlResultFlightChange(this.getJobDetail(), fltNo, this.getJobDetail().getDepDate());
				
				// 出发三字码
				Element eleA = eleNumdap.select("> ul > li > div.wea > table > tbody > tr > td > a").first();
				if(null == eleA) return null;
				String depCode = eleA.attr("href").trim();
				crawlResult.setDepCode(depCode.substring(depCode.length() - 3));
				
				Elements eleDepPs = eleNumdap.select("> div.numtime > p");	// 19:20/--:--
				// 出发 计划起飞时间
				String planDepTime = eleDepPs.get(0).ownText().trim();	
				crawlResult.setPlanDepTime(planDepTime.substring(planDepTime.indexOf("：") + 1).trim());
				
				// 出发 实际起飞时间
				String depTime = eleDepPs.get(2).ownText().trim();
				crawlResult.setDepTime(depTime.substring(depTime.indexOf("：") + 1).trim());
				
				
				// 到达...............>>>>>>>>>>>>>>>>>>
				
				// 到达三字码
				eleA = eleNumarr.select("> ul > li > div.wea > table > tbody > tr > td > a").first();
				if(null == eleA) return null;
				String desCode = eleA.attr("href").trim();
				crawlResult.setDesCode(desCode.substring(desCode.length() - 3));
				
				Elements eleDesPs = eleNumarr.select("> div.numtime > p");	// 19:20/--:--
				// 到达 计划起飞时间
				String planDesTime = eleDesPs.get(0).ownText().trim();
				crawlResult.setPlanDesTime(planDesTime.substring(planDesTime.indexOf("：") + 1).trim());
				
				// 到达 实际起飞时间
				String desTime = eleDesPs.get(2).ownText().trim();
				crawlResult.setDesTime(desTime.substring(desTime.indexOf("：") + 1).trim());
				
				// 飞机状态：计划航班
				String status = eleNuminfo.select("> div.numtimestate > div > p > span.red").first().ownText();
				crawlResult.setStatus(status);
				
				// 准点率
				Element eleAirname = eleNuminfo.select("> ul > table.tab1 > tbody > tr > td.airname").last();
				if(eleAirname.childNodeSize() <= 2) return null;
				String zdl = eleAirname.childNode(3).toString();	// 历史准点率：73.33%
				zdl = zdl.substring(zdl.indexOf("：") + 1, zdl.indexOf("%")).trim();
				crawlResult.setZdl(new BigDecimal(zdl));
				
				crawlResults.add(crawlResult);
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
		String url = queryUrl.replaceAll("%fltNo%", this.getJobDetail().getFltNo());
		return super.httpProxyGet(url, "GBK", "html");
	}
	
	/**
	 * 获取航班号
	 * @return
	 */
	public static List<String> getFltNos() {
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
		List<String> fltNos = new ArrayList<String>();
		while(true) {
			String httpResult = ProxyUtil.httpProxyGet("http://www.veryzhun.com/planenumber.html", headerMap, 10 * 1000, "GBK");
			if(StringUtils.isEmpty(httpResult)) continue ;
			Document document = Jsoup.parse(httpResult);
			Elements eleAs = document.select("#list > div.list > p > a");
			for(Element eleA : eleAs) {
				String fltNo = eleA.ownText().trim().toUpperCase();
				if(StringUtils.isEmpty(fltNo)) continue ;
				fltNos.add(fltNo);
			}
			break ;
		}
		return fltNos;
	}

}