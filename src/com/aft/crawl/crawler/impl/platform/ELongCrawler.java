package com.aft.crawl.crawler.impl.platform;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 艺龙官网-机票
 */
public class ELongCrawler extends Crawler {

	private final static String queryUrl = "http://flight.elong.com/jajax/OneWay/S?AirCorp=0&ArriveCity=%desCode%&DepartCity=%depCode%&DepartDate=%depDate%+00:00&FlightType=OneWay&OrderBy=Price&PageName=list&grabCode=%grabCode%";
//	private final static String queryUrl = "http://flight.elong.com/jajax/OneWay/S?AirCorp=0&ArriveCity=SHA&ArriveCityName=%E4%B8%8A%E6%B5%B7&ArriveCityNameEn=Shanghai&BackDayCount=4&DayCount=10&DepartCity=BJS&DepartCityName=%E5%8C%97%E4%BA%AC&DepartCityNameEn=Beijing&DepartDate=2017%2F06%2F23+00%3A00&FlightType=OneWay&OrderBy=Price&PageName=list&serviceTime=2017-06-13&grabCode=9208792";
	
	public ELongCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Map<String, Object> value = (Map<String, Object>)mapResult.get("value");
			Object success = mapResult.get("success");
			if(null == value || !"true".equals(success.toString())) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
				return crawlResults;
			}
			
			CrawlResultB2C crawlResult = null;
			List<Map<String, Object>> mainLegs = (List<Map<String, Object>>)value.get("MainLegs");
			
			for(Map<String, Object> mainLeg : mainLegs) {
				
				List<Map<String, Object>> segs = (List<Map<String, Object>>)mainLeg.get("segs");
				//去点联程
				if(segs.size()>1)continue;
				
				Map<String, Object> seg = segs.get(0);
				
				String fltNo = seg.get("fltno").toString();
				String airlineCode = seg.get("corp").toString();
				String depCode = seg.get("dc").toString();
				String desCode = seg.get("ac").toString();
				
				String dtime = seg.get("dtime").toString();
				String atime = seg.get("atime").toString();
				String depDate = dtime.substring(0, 10);
				String depTime = dtime.substring(11,16);
				String desTime = atime.substring(11, 16);
				
//				String tax = seg.get("tax").toString();
				
				List<Map<String, Object>> cabs = (List<Map<String, Object>>)mainLeg.get("cabs");
				
				for(Map<String, Object> cab : cabs){
					String cabin = cab.get("cab").toString();
					if(!this.allowCabin(cabin)) continue ;	// 排除舱位
					String type = cab.get("wname").toString();
					String price = cab.get("price").toString();
					String tc = cab.get("tc").toString();
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, "N", depCode, desCode, depDate, cabin);
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					crawlResult.setType(type);
					// 剩余座位数
					if(tc==null || "".equals(tc)){
						crawlResult.setRemainSite(10);
					}else{
						crawlResult.setRemainSite(Integer.valueOf(tc));
					}
					
					// 价格
					crawlResult.setTicketPrice(new BigDecimal(price).setScale(0));
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
		String url = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
							.replaceAll("%desCode%", this.getJobDetail().getDesCode())
							.replaceAll("%depDate%", this.getJobDetail().getDepDate().replaceAll("-", "/"))
							.replaceAll("%grabCode%",String.valueOf(new Date().getTime()));
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		Map<String,Object> headerMap = new HashMap<String, Object>();
		httpClientSessionVo.setHeaderMap(headerMap);
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "flight.elong.com");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0");
//		
//		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,"http://flight.elong.com", "html");
//		String jssessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
//		String cookieGuid = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "CookieGuid=");
//		String sessionGuid = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "SessionGuid=");
//		String esid = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "Esid=");
//		String cookieInfo = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "com.eLong.CommonService.OrderFromCookieInfo=");
//		String flightCondition="FlightCondition=DepDate="+this.getJobDetail().getDepDate()+"&ArrCode="+this.getJobDetail().getDesCode()+"&DepCode="+this.getJobDetail().getDepCode()+"&FType=0";
//		
//		StringBuilder cookie = new StringBuilder()
//				.append(jssessionId).append(";")
//				.append(cookieGuid).append(";")
//				.append(sessionGuid).append(";")
//				.append(esid).append(";")
//				.append(flightCondition).append(";")
//				.append(cookieInfo);
//		headerMap.put("Cookie", cookie);
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");	
		headerMap.put("Referer", "http://flight.elong.com/search/bjs-sha/?departdate="+this.getJobDetail().getDepDate());	
		headerMap.put("X-Requested-With", "XMLHttpRequest");
//		
		httpClientSessionVo.setHeaderMap(headerMap);
		String httpResult =  this.httpProxyGet(httpClientSessionVo,url,"other");
		System.out.println(httpResult);
		return httpResult;
	}
}