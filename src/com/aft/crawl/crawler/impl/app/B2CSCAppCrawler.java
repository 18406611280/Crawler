package com.aft.crawl.crawler.impl.app;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.http.conn.ConnectTimeoutException;

import com.aft.app.sc.huicent.b.HttpManeger;
import com.aft.app.sc.huicent.entity.FlightQueryBean;
import com.aft.app.sc.huicent.entity.FlightQueryResult;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 山东航空 app  
 */
public class B2CSCAppCrawler extends Crawler {

	public B2CSCAppCrawler(String threadMark) {
		super(threadMark, "2");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			// {"depDate":"2016-09-30","flightInfos":[{"depTime":"21:20","airlineCode":"SC","fltNo":"SC4630","desTime":"22:45","seatInfos":[{"cabin":"B","remainSite":"10","ticketPrice":"930"},{"cabin":"B","remainSite":"10","ticketPrice":"940"},{"cabin":"Y","remainSite":"10","ticketPrice":"1030"},{"cabin":"F","remainSite":"8","ticketPrice":"2580"}]}],"desName":"厦门","desCode":"XMN","depCode":"CAN","depName":"广州"}
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			if(null != (resultMap.get("msg"))) {
				logger.info(this.getJobDetail().toStr() + ", 返回错误信息:" + httpResult);
				return crawlResults;
			}
			CrawlResultB2C b2c = null;
			String depCode = resultMap.get("depCode").toString();
			String desCode = resultMap.get("desCode").toString();
			String depDate = resultMap.get("depDate").toString();
			List<Map<String, Object>> flightInfos = (List<Map<String, Object>>)resultMap.get("flightInfos");
			for(Map<String, Object> flightInfo : flightInfos) {
				String airlineCode = flightInfo.get("airlineCode").toString();
				String fltNo = flightInfo.get("fltNo").toString();
				String shareFlight = fltNo.startsWith(airlineCode) ? "N" : "Y";
				for(Map<String, Object> seatInfo : ((List<Map<String, Object>>)flightInfo.get("seatInfos"))) {
					String cabin = seatInfo.get("cabin").toString();
					b2c = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					b2c.setDepTime((String)seatInfo.get("depTime"));
					b2c.setDesTime((String)seatInfo.get("arrTime"));
					
					BigDecimal ticketPrice = new BigDecimal(seatInfo.get("ticketPrice").toString());
					int remainSite = Integer.parseInt(seatInfo.get("remainSite").toString());
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
			if(this.isTimeout()) return "{\"msg\": \"超时\"}";
			FlightQueryBean bean = HttpManeger.makeFlightQueryBean(0, this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate(), "", 0);
			
			Object[] objs = ProxyUtil.getProxyInfo(this.threadMark);
			if(null == objs[0]) {
				synchronized(B2CSCAppCrawler.class) {
					objs = ProxyUtil.getProxyInfo(this.threadMark);
					if(null == objs[0]) objs = super.changeProxy();
				}
			}
			
			FlightQueryResult result = HttpManeger.queryInnerFlight(objs[0].toString(), Integer.parseInt(objs[1].toString()), this.getJobDetail(), bean);
			logger.info(this.getJobDetail().toStr() + ", 接口返回:" + ReflectionToStringBuilder.toString(result, ToStringStyle.DEFAULT_STYLE));
			
			if(null == result)return "{\"msg\": \"接口返回null\"}";
			if("S0002".equals(result.status)) {
				super.changeProxy();
				return this.httpResult();
			}
			if(null == result.h) return "{\"msg\": \"this.h 返回null\"}";
			return result.toJson();
		} catch(Exception e) {
			if(e instanceof ConnectTimeoutException) {
				MyThreadUtils.sleep(1000);
				return this.httpResult();
			} else if(e instanceof IllegalArgumentException && "Host name may not contain blanks".equals(e.getMessage())) {
				super.changeProxy();
				return this.httpResult();
			}
			logger.error(this.getJobDetail().toStr() + ", 查询山东航空app异常:\r", e);
			return "{\"msg\": \"接口返回异常:" + e + "\"}";
		}
	}
}