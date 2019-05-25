package com.aft.crawl.crawler.impl.platform;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * qunar 海南航空 
 */
public class QunarHUCrawler extends Crawler {

	private final static String queryUrl = "http://qunar.hnair.com/hnair/nationalWrapperSearch?searchType=OnewayFlight&fromCity=%depName%&toCity=%desName%&fromDate=%depDate%";
	
	public QunarHUCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object objMsg = mapResult.get("ret");
			if(null == objMsg || !"0".equals(objMsg.toString())) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			List<Map<String, Object>> outlists = (List<Map<String, Object>>)mapResult.get("outlist");
			
			Map<String, Object> outlist = outlists.get(0);
			Map<String, Object> info = (Map<String, Object>)outlist.get("info");
			// 航班号,航司
			String fltNo = info.get("co").toString().trim().toUpperCase();
			String airlineCode = info.get("ca").toString().trim().toUpperCase();
			
			// 判断共享
			String shareFlight = this.getShareFlight(airlineCode);
			
			// 出发,到达
			String depCode = info.get("da").toString().trim().toUpperCase();
			String desCode = info.get("aa").toString().trim().toUpperCase();
			
			// 出发日期
			String depDate = info.get("dd").toString().trim();
			
			// 出发时间
			String depTime = info.get("dt").toString().trim();
			
			// 到达时间
			String desTime = info.get("at").toString().trim();
			
			CrawlResultB2C crawlResult = null;
			List<Map<String, Object>> prlists = (List<Map<String, Object>>)outlist.get("prlist");
			for(Map<String, Object> prlist : prlists) {
				String cabin = prlist.get("cb").toString().trim().toUpperCase();
				if(!this.allowCabin(cabin)) continue ;	// 排除舱位
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
				crawlResult.setDepTime(depTime);	// 出发时间
				crawlResult.setDesTime(desTime);	// 到达时间
				
				// 剩余座位数
				crawlResult.setRemainSite(10);
				
				// 价格
				crawlResult.setTicketPrice(new BigDecimal(prlist.get("pr").toString().trim()).setScale(0));
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
		String url = queryUrl.replaceAll("%depName%", URLEncoder.encode(this.getJobDetail().getDepName(), "UTF-8"))
							.replaceAll("%desName%", URLEncoder.encode(this.getJobDetail().getDesName(), "UTF-8"))
							.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return this.httpProxyGet(url, "json");
	}
}