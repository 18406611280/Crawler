package com.aft.crawl.crawler.impl.other;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.jackson.MyJsonTransformUtil;

public class EtermCabinAmountCrawler extends Crawler {
	
//	private final static String queryUrl = "http://api.fly517.com:889/PidServiceAVN.asmx/AVN?UserAct=%userName%&UserPwd=%password%&UserKey=%userKey%&FlightNo=%fltNo%&DepDate=%depDate%";
	private final static String queryUrl = "http://192.168.0.253:9099/sh/eterm/etermAvh.action?airlineCode=%airlineCode%&depCode=%depCode%&desCode=%desCode%&depDate=%depDate%&account=%account%&officeNo=%officeNo%";
	
	public EtermCabinAmountCrawler(String threadMark) {
		super(threadMark, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object success = mapResult.get("success");
			if(null == success || !(Boolean)success) {
				logger.info(this.getJobDetail().toStr() + ", 查询返回:" + mapResult.get("msg"));
				return null;
			}
			
			CrawlResultB2C crawlResult = null;
			Map<String, Map<String, String>> datas = (Map<String, Map<String, String>>)mapResult.get("data");
			if(null == datas) return null;
			
			Iterator<Map.Entry<String, Map<String, String>>> it = datas.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, Map<String, String>> entry = it.next();
				String fltNo = entry.getKey();
				Iterator<Map.Entry<String,String>> it1 = entry.getValue().entrySet().iterator();
				while(it1.hasNext()) {
					Map.Entry<String,String> entry1 = it1.next();
					String cabin = entry1.getKey();
					String cabinAmount = entry1.getValue();
					
					crawlResult = new CrawlResultB2C(this.getJobDetail(), this.getJobDetail().getAirlineCode(),
							fltNo, "N", this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate(), cabin);
					crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(cabinAmount));
					crawlResult.setTicketPrice(new BigDecimal(0));
					crawlResult.setSalePrice(crawlResult.getTicketPrice());
					crawlResults.add(crawlResult);
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	@Override
	public String httpResult() throws Exception {
		return this.httpProxyGet(queryUrl.replaceAll("%airlineCode%", this.getJobDetail().getAirlineCode())
										.replaceAll("%depCode%", this.getJobDetail().getDepCode())
										.replaceAll("%desCode%", this.getJobDetail().getDesCode())
										.replaceAll("%depDate%", this.getJobDetail().getDepDate())
										.replaceAll("%account%", this.getTimerJob().getParamMapValueByKey("account"))
										.replaceAll("%officeNo%", this.getTimerJob().getParamMapValueByKey("officeNo")), "json");
	}
}