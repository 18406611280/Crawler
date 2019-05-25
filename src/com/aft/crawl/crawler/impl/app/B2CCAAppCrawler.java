package com.aft.crawl.crawler.impl.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.app.ca.CaService;
import com.aft.crawl.crawler.impl.app.ca.FlightQuery;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 国际航空 app  
 */
public class B2CCAAppCrawler extends Crawler {

	private final static CaService caService = new CaService();
	
	public B2CCAAppCrawler(String threadMark) {
		super(threadMark, "0");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			// {"statuMessage":[{"airlineCode":"CA","flightNo":"CA1309","dep":"PEK","arr":"CAN","depTime":"18:00","arrTime":"21:20","depDate":"2016-06-07","flightDetails":[{"cabin":"U","price":"1450","seatSum":"10"},{"cabin":"Y","price":"1810","seatSum":"10"},{"cabin":"A","price":"1910","seatSum":"10"}]},{"airlineCode":"CA","flightNo":"CA1351","dep":"PEK","arr":"CAN","depTime":"07:35","arrTime":"10:55","depDate":"2016-06-07","flightDetails":[{"cabin":"U","price":"1450","seatSum":"10"},{"cabin":"Y","price":"1810","seatSum":"10"},{"cabin":"A","price":"1910","seatSum":"10"}]},{"airlineCode":"CA","flightNo":"CA1329","dep":"PEK","arr":"CAN","depTime":"21:00","arrTime":"00:20","depDate":"2016-06-07","flightDetails":[{"cabin":"U","price":"1450","seatSum":"10"},{"cabin":"Y","price":"1810","seatSum":"10"},{"cabin":"A","price":"1910","seatSum":"10"}]},{"airlineCode":"CA","flightNo":"CA1315","dep":"PEK","arr":"CAN","depTime":"11:00","arrTime":"14:15","depDate":"2016-06-07","flightDetails":[{"cabin":"U","price":"1450","seatSum":"10"},{"cabin":"Y","price":"1810","seatSum":"10"},{"cabin":"A","price":"2290","seatSum":"7"}]},{"airlineCode":"CA","flightNo":"CA1321","dep":"PEK","arr":"CAN","depTime":"09:00","arrTime":"12:25","depDate":"2016-06-07","flightDetails":[{"cabin":"U","price":"1450","seatSum":"10"},{"cabin":"Y","price":"1810","seatSum":"10"},{"cabin":"A","price":"2290","seatSum":"10"}]},{"airlineCode":"CA","flightNo":"CA1301","dep":"PEK","arr":"CAN","depTime":"15:00","arrTime":"18:15","depDate":"2016-06-07","flightDetails":[{"cabin":"U","price":"1450","seatSum":"10"},{"cabin":"Y","price":"1810","seatSum":"10"},{"cabin":"A","price":"2290","seatSum":"10"}]}],"statuCode":"success"}
			System.out.println(httpResult);
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			if(!"success".equals(resultMap.get("statuCode"))) {
				logger.info(this.getJobDetail().toStr() + ", 返回错误信息:" + resultMap.get("msg"));
				return crawlResults;
			}
			List<Map<String, Object>> messageList = (List<Map<String, Object>>)resultMap.get("statuMessage");
			CrawlResultB2C b2c = null;
			for(Map<String, Object> messageMap : messageList) {
				String airlineCode = messageMap.get("airlineCode").toString();
				String fltNo = messageMap.get("flightNo").toString();
				String depCode = messageMap.get("dep").toString();
				String desCode = messageMap.get("arr").toString();
				String depDate = messageMap.get("depDate").toString();
				String shareFlight = fltNo.startsWith(airlineCode) ? "N" : "Y";
				for(Map<String, Object> map : ((List<Map<String, Object>>)messageMap.get("flightDetails"))) {
					String cabin = map.get("cabin").toString();
					b2c = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					b2c.setDepTime((String)map.get("depTime"));
					b2c.setDesTime((String)map.get("arrTime"));
					
					BigDecimal ticketPrice = new BigDecimal(map.get("price").toString());
					int remainSite = Integer.parseInt(map.get("seatSum").toString());
					b2c.setTicketPrice(ticketPrice);
					b2c.setSalePrice(ticketPrice);
					b2c.setRemainSite(remainSite);
					crawlResults.add(b2c);
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}

	@Override
	public String httpResult() {		
		try {
			FlightQuery query = new FlightQuery();
			query.setArr(this.getJobDetail().getDesCode());
			query.setDep(this.getJobDetail().getDepCode());
			query.setDepDate(this.getJobDetail().getDepDate());
			
			Object[] proxyInfos = ProxyUtil.getProxyInfo(this.threadMark);
			
			query.setIp(proxyInfos[0].toString());
			query.setPort(Integer.parseInt(proxyInfos[1].toString()));
			Map<String,Object> resultMap = caService.flightQuery(this.getJobDetail(), this.threadMark, query);
			
//			if("-1".equals(resultMap.get("statuCode"))) {
//				MyFileUtils.createFile("c:/", "caIp.txt", MyDateFormatUtils.SDF_YYYYMMDDHHMMSSSSS().format(new Date()) + "\t" + MyJsonTransformUtil.writeValue(proxyInfos), true);
//				ProxyUtil.changeProxy(this.threadMark, this.getTimerJob().getJobId(), this.getPageType());
//				return this.httpResult();
//			}
			return MyJsonTransformUtil.writeValue(resultMap);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 查询国航app异常:\r", e);
			return null;
		}
	}
}