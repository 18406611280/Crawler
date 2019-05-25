package com.aft.crawl.crawler.impl.other;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 王老板找的第三方供应商的数据接口
 */
public class XiaoXiaoCrawler extends Crawler {
															
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	private final static String flightUrl = "http://209.9.106.98:40002/tb_search";
	
	public XiaoXiaoCrawler(String threadMark) {
		super(threadMark);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		
		
		String httpResult =this.httpResult();
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
		if(!"0".equals(mapResult.get("status").toString())){
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			crawlResults = this.owFlight(crawlResults,httpResult,mapResult);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
			throw e;
		} finally {
			httpClientSession.clearDefaultProp();
		}
		return crawlResults;
	}
	
	/**
	 * 直达航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<CrawlResultBase> owFlight(List<CrawlResultBase> crawlResults,String httpResult,Map<String, Object> mapResult) {
		try {
			SimpleDateFormat SDF_YMDHM1 = new SimpleDateFormat("yyyyMMddHHmm");
			SimpleDateFormat SDF_YMDHM2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			List<Map<String, Object>> flightResults = (List<Map<String, Object>>)mapResult.get("routings");
			CrawlInterResult crawlResult =  null;
			for(Map<String, Object> flights : flightResults){
				
				String ticketPrice = flights.get("adultPrice").toString();
				String taxFee = flights.get("adultTax").toString();
				Map<String, Object> rule = (Map<String, Object>)flights.get("rule");
				String refund = rule.get("refund").toString();//退票
				String baggage = rule.get("baggage").toString();//行李
				String other = rule.get("other").toString();//其他
				String endorse = rule.get("endorse").toString();//改签
				refund = URLDecoder.decode(refund, "utf-8");
				baggage = URLDecoder.decode(baggage, "utf-8");
				other = URLDecoder.decode(other, "utf-8");
				endorse = URLDecoder.decode(endorse, "utf-8");
				
				List<Map<String, Object>> fromSegments= (List<Map<String, Object>>)flights.get("fromSegments");
				//去掉联程，暂时不支持
				if(fromSegments==null || fromSegments.isEmpty() || fromSegments.size()>1)continue;
				Map<String, Object> fromSegment = fromSegments.get(0);
				String desCode = fromSegment.get("arrAirport").toString();
				String depCode = fromSegment.get("depAirport").toString();
				String fltNo = fromSegment.get("flightNumber").toString();
				String airlineCode = fromSegment.get("carrier").toString();
				String cabin = fromSegment.get("cabin").toString();
				String depTime = fromSegment.get("depTime").toString();
				String arrTime = fromSegment.get("arrTime").toString();
				depTime = SDF_YMDHM2.format(SDF_YMDHM1.parse(depTime));
				arrTime = SDF_YMDHM2.format(SDF_YMDHM1.parse(arrTime));
				String depDate = depTime.substring(0,10);
				String endDate = arrTime.substring(0,10);
				String desTime = arrTime.substring(11,16);
				String depTimeGo = depTime.substring(11,16);
				
				crawlResult = new CrawlInterResult(this.getJobDetail(), depCode, desCode, depDate);
				crawlResult.setChangeInfo(endorse);		// 改期信息
				crawlResult.setRefundInfo(refund);		// 退票信息
				crawlResult.setQianZhuanInfo(endorse);	// 签转信息
				crawlResult.setFltNo(fltNo);
				crawlResult.setAirlineCode(airlineCode);
				crawlResult.setEndDate(endDate);
				crawlResult.setDepTime(depTimeGo);
				crawlResult.setDesTime(desTime);
				crawlResult.setRemainSite(2);
				crawlResult.setShareFlight("N");
				crawlResult.setCurrency("CNY");
				crawlResult.setCabin(cabin);	
				crawlResult.setTicketPrice(new BigDecimal(ticketPrice));
				crawlResult.setTaxFee(new BigDecimal(taxFee));
				
				crawlResults.add(crawlResult);
			}
		} catch (Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 采集航班信息异常:\r", e);
		}
		return crawlResults;
	}
	
	@Override
	public String httpResult() throws Exception {
		SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat SDF_DMY = new SimpleDateFormat("yyyyMMdd");
		String depDate = SDF_DMY.format(SDF_YMD.parse(this.getJobDetail().getDepDate()));
		String content = "{"+
        	    "\"cid\": \"waqu\","+
        	    "\"tripType\": \"1\","+
        	    "\"fromCity\": \""+this.getJobDetail().getDepCode()+"\","+
        	    "\"toCity\": \""+this.getJobDetail().getDesCode()+"\","+
        	    "\"fromDate\": \""+depDate+"\","+
        	    "\"retDate\": \"\""+
        	"}";
		String result = MyHttpClientUtil.post(flightUrl, content, null);
		return result;
	}
	

	public static void main(String[] args) {
	}

}