package com.aft.crawl.crawler.impl.other;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.weather.CrawlResultWeather;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.jackson.MyJsonTransformUtil;

public class SinaWeatherCrawler extends Crawler {
	
	private final static String queryUrl = "http://php.weather.sina.com.cn/interface/index.php?c=wairport&a=airport_forecast&auth_type=uuid&auth_value=0123456789012345&startday=0&lenday=3&airport=%airport%";
	
	public SinaWeatherCrawler(String threadMark) {
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
			Map<String, Object> status = (Map<String, Object>)mapResult.get("status");
			if(null == status || !"0".equals(status.get("code"))) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return null;
			}
			
			CrawlResultWeather crawlResult = null;
			List<Map<String, Object>> datas = (List<Map<String, Object>>)mapResult.get("data");
			for(Map<String, Object> data : datas) {
				if(this.isTimeout()) return null;
				
				List<Map<String, Object>> days = (List<Map<String, Object>>)data.get("days");
				for(Map<String, Object> day : days) {
					crawlResult = new CrawlResultWeather(this.getJobDetail(), this.getJobDetail().getAirport());
					crawlResult.setCondition((String)day.get("s1"));
					crawlResult.setHightTemperature((String)day.get("t1"));
					crawlResult.setLowTemperature((String)day.get("t2"));
					crawlResult.setWind((String)day.get("p1"));
					crawlResult.setWeatherDate(MyDateFormatUtils.SDF_YYYYMMDD().format(MyDateFormatUtils.SDF_EEEdMMMYYYYMMDDHHMMSSZ(Locale.US).parse((String)day.get("date"))));
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
		return this.httpProxyGet(queryUrl.replaceAll("%airport%", this.getJobDetail().getAirport()), "json");
	}
}