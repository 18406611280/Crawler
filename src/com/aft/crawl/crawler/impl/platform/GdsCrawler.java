package com.aft.crawl.crawler.impl.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.gds.LowPriceSearchRsVO;
import com.aft.crawl.result.vo.gds.PricedItineraryVO;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * gds
 */
public class GdsCrawler extends Crawler {

	private final static String queryUrl = "http://192.168.0.75:8072/policy/rest/gdsRulesResource/queryLowPrice?depCode=%depCode%&desCode=%desCode%&depDate=%depDate%&returnDate=%returnDate%&airlines=%airlines%&fareRule=%fareRule%&maxSolutions=%maxSolutions%&gdsSource=%gdsSource%";
	
	public GdsCrawler(String threadMark) {
		super(threadMark, false);
	}

	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			LowPriceSearchRsVO rsVo = MyJsonTransformUtil.readValue(httpResult, LowPriceSearchRsVO.class);
			if(!"2".equals(rsVo.getStatus())) logger.warn(this.getJobDetail().toStr() + ", 获取航程信息失败:" + httpResult);
			if(null == rsVo.getPricedItinerary()) {
				List<PricedItineraryVO> vos = new ArrayList<PricedItineraryVO>();
				vos.add(new PricedItineraryVO());
				rsVo.setPricedItinerary(vos);
			}
			crawlResults.addAll(rsVo.getPricedItinerary());
			for(CrawlResultBase crawlResult : crawlResults) {
				crawlResult.setPageType(this.getPageType());
				crawlResult.setCrawlMark(this.getJobDetail().getCrawlMark());
				crawlResult.setCrawlStatus(Integer.parseInt(rsVo.getStatus()));	// 2: 采集成功; 3: 采集超时; 4: 异常,失败
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	public String httpResult() throws Exception {
		Map<String, String> map = this.getTimerJob().getParamMap();
		String airlines = map.get("airlines");
		String fareRule = map.get("fareRule");
		String maxSolutions = map.get("maxSolutions");
		String gdsSource = null;
		if(this.getPageType().equals(CrawlerType.geligeoInterType)) gdsSource = PricedItineraryVO.DataSource.Travelport.value;
		else if(this.getPageType().equals(CrawlerType.amaduesInterType)) gdsSource = PricedItineraryVO.DataSource.Amadues.value;
		else if(this.getPageType().equals(CrawlerType.sabreInterType)) gdsSource = PricedItineraryVO.DataSource.Sabre.value;
		String url = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode())
								.replaceAll("%depDate%", this.getJobDetail().getDepDate())
								.replaceAll("%returnDate%", StringUtils.isEmpty(this.getJobDetail().getBackDate()) ? "" : this.getJobDetail().getBackDate())
								.replaceAll("%airlines%", airlines)
								.replaceAll("%fareRule%", fareRule)
								.replaceAll("%maxSolutions%", maxSolutions)
								.replaceAll("%gdsSource%", gdsSource);
		return super.httpProxyGet(url, "json");
	}
}