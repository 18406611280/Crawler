package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 四川航空(国际)
 */
public class B2C3UInterCrawler extends Crawler {

	private final static String flightUrl = "http://www.sichuanair.com/ETicket/InterAirlineList";

	private final static String queryUrl = "http://www.sichuanair.com/ETicket/GetInterSingleChina";
	
	private final static String airlineParamJSON = "{\"AirlineType\":\"Single\",\"IsFixedCabin\":false,\"RouteList\":[{\"RouteIndex\":1,\"OrgCity\":\"%depCode%\",\"DesCity\":\"%desCode%\",\"FlightDate\":\"%depDate%\"}],\"AVType\":0}";
	
	private final static Pattern pattern = Pattern.compile("arrPageValue\\.AirlineParamJSON = (\\{.+\\});");
	
	public B2C3UInterCrawler(String threadMark) {
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
			Object objMsg = mapResult.get("Result");
			Object airlineListJSON = mapResult.get("AirlineListJSON");
			if(null == objMsg || !(Boolean)objMsg || null == airlineListJSON) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			mapResult = MyJsonTransformUtil.readValue(airlineListJSON.toString(), Map.class);
			Map<String, Object> flightInfoMap = (Map<String, Object>)mapResult.get("FlightInfo");
			
			// 航司,航班号
			String fltNo = flightInfoMap.get("FlightNo").toString().trim().toUpperCase();
			String airlineCode = fltNo.substring(0, 2);
			
			// 判断共享
			String shareFlight = this.getShareFlight(airlineCode);

			Map<String, Object> currentCabinsMap = (Map<String, Object>)mapResult.get("CurrentCabins");
			String cabin = currentCabinsMap.get("CabinsNO").toString().trim().toUpperCase();
			if(!this.allowCabin(cabin)) return crawlResults;	// 排除舱位
			
			// 出发,到达
			String depCode = flightInfoMap.get("OrgCity").toString().trim().toUpperCase();
			String desCode = flightInfoMap.get("DesCity").toString().trim().toUpperCase();
			
			String takeOffTime = flightInfoMap.get("TakeOffTime").toString().trim();
			// 出发日期
			String depDate = takeOffTime.substring(0, 10);
			
			// 出发时间
			String depTime = takeOffTime.substring(11, 16);
			
			// 到达时间
			String desTime = flightInfoMap.get("ArriveTime").toString().trim().substring(11, 16);
			
			CrawlInterResult crawlResult = new CrawlInterResult(this.getJobDetail(), depCode, desCode, depDate);
			
			crawlResult.setCabin(cabin);
			crawlResult.setFltNo(fltNo);
			crawlResult.setAirlineCode(airlineCode);
			crawlResult.setDepTime(depTime);
			crawlResult.setDesTime(desTime);
			crawlResult.setCurrency("CNY");
//			crawlResult.setQianZhuanInfo(currentCabinsMap.get("ADRule").toString().trim());
			crawlResult.setShareFlight(shareFlight);
			
			
			// 剩余座位数
			crawlResult.setRemainSite(Integer.parseInt(currentCabinsMap.get("Amount").toString().trim()));
			
			// 价格
			crawlResult.setTaxFee(new BigDecimal(mapResult.get("ADTotalTax").toString().trim()).setScale(0));
			crawlResult.setTicketPrice(new BigDecimal(currentCabinsMap.get("ADBasePrice").toString().trim()).setScale(0));
			crawlResults.add(crawlResult);
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
		if("json".equals(returnType)) {
			Map<String, Object> map = (Map<String, Object>)jsonObject;
			Object message = map.get("Message");
			return null != message && message.toString().contains("系统");
		}
		return false;
	}
	

	@Override
	public String httpResult() throws Exception {
		this.getPassKey();			// 不登录/不金卡
		Map<String, Object> paramMap = this.getBaseJobParamMap();
		if(null == paramMap || paramMap.isEmpty()) {
			logger.warn(this.getJobDetail().toStr() + ", 获取航程 passKey 失败...");
			return "{\"errorMsg\":\"获取航程 passKey 失败\"}";
		}
		
		paramMap.put("OrgCity", this.getJobDetail().getDepCode());
		paramMap.put("DesCity", this.getJobDetail().getDesCode());
		paramMap.put("FlightDate", this.getJobDetail().getDepDate());
		return this.httpProxyPost(queryUrl, paramMap, "json");
	}
	/**
	 * 直接获取 PassKey
	 */
	@SuppressWarnings("unchecked")
	private void getPassKey() {
		Map<String, Object> paramMap = this.getBaseJobParamMap();
		if(null != paramMap && !paramMap.isEmpty()) return ;
		synchronized(flightUrl) {
			paramMap = this.getBaseJobParamMap();
			if(null != paramMap && !paramMap.isEmpty()) return ;
			String httpResult = null;
			try {
				Map<String, Object> postMap = new HashMap<String, Object>();
				String json = airlineParamJSON.replaceAll("%depCode%", this.getJobDetail().getDepCode())
												.replaceAll("%desCode%", this.getJobDetail().getDesCode())
												.replaceAll("%depDate%", this.getJobDetail().getDepDate());
				postMap.put("AirlineParamJSON", json);
				httpResult = this.httpProxyPost(flightUrl, postMap, "html");
				if(StringUtils.isEmpty(httpResult)) {
					logger.info(this.getJobDetail().toStr() + ", 登录获取 PassKey 返回[null]");
					return ;
				}
				Matcher matcher = pattern.matcher(httpResult);
				if(!matcher.find()) {
					logger.info(this.getJobDetail().toStr() + ", 登录获取 PassKey 返回:" + httpResult);
					return ;
				}
				Map<String, Object> resultMap = MyJsonTransformUtil.readValue(matcher.group(1), Map.class);
				paramMap = new HashMap<String, Object>();
				paramMap.put("RouteIndex", "1");
				paramMap.put("AVType", resultMap.get("AVType").toString());
				paramMap.put("PassKey", resultMap.get("PassKey").toString());
				paramMap.put("BuyerType", resultMap.get("BuyerType").toString());
				paramMap.put("AirlineType", resultMap.get("AirlineType").toString());
				paramMap.put("IsFixedCabin", resultMap.get("IsFixedCabin").toString());
				this.putBaseJobParamMap(paramMap);
				logger.info(this.getJobDetail().toStr() + ", 获取 PassKey[" + paramMap.get("PassKey") + "] 成功!");
			} catch(Exception e) {
				logger.error(this.getJobDetail().toStr() + ", 请求 httpResult 获取 PassKey 异常:" + httpResult, e);
			}
		}
	}
}