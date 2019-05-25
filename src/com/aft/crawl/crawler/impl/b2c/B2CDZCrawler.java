package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.StringTxtUtil;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 东海航空 
 */
public class B2CDZCrawler extends Crawler {
	
    private String dep;
    private String des;
    private String depDate;
	public B2CDZCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		try {
			List<CrawlResultBase> CrawlResultList = this.getCrawlerList();
			return CrawlResultList;
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常 ", e);
			throw e;
		}
	}
	
	@Override
	protected boolean returnEmptyChangeIp(String httpResult) {
		return StringUtils.isEmpty(httpResult);
	}

	@Override
	public String httpResult() throws Exception {
		return null;
	}
	
	public List<CrawlResultBase> getCrawlerList() throws Exception{
		CloseableHttpClient httpClient = HttpsClientTool.getHttpsClient(threadMark);
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		dep = this.getJobDetail().getDepCode();
		des = this.getJobDetail().getDesCode();
		depDate = this.getJobDetail().getDepDate();
		String signUrl = "http://www.donghaiair.com/";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String newDate = sdf.format(date);
		Map<String, Object> headerMap = new HashMap<String, Object>();
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		headerMap.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Host", "www.donghaiair.com");
		httpClientSessionVo.setHeaderMap(headerMap);
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo,signUrl,"other");
		headerMap.put("Referer",signUrl);
		String flightUrl = "http://www.donghaiair.com/html/booking-manage/choose-flight-two.html?flightType=1&orgCode="+dep+"&destCode="+des+"&departureDateStr="+depDate+"&returnDateStr="+newDate+"&adult=1&child=0&infant=0&airCode=DZ&direct=true&noneStop=true";
		httpVo = this.httpProxyResultVoGet(httpClientSessionVo,flightUrl,"other");
		headerMap.put("Accept","*/*");
		headerMap.put("Referer",flightUrl);
		headerMap.put("Origin","http://www.donghaiair.com");
		headerMap.put("Host","b2capi.donghaiair.cn");
		headerMap.put("Content-Type","application/json;charset=utf-8");
		String param = "{\"flightType\":\"1\",\"orgCode\":\""+dep+"\",\"destCode\":\""+des+"\",\"departureDateStr\":\""+depDate+"\",\"returnDateStr\":\""+newDate+"\",\"adult\":\"1\",\"child\":\"0\",\"infant\":\"0\"}";
		String searchUrl = "http://b2capi.donghaiair.cn/ibe/flightSearch";
		String result = this.httpProxyPost(httpClientSessionVo,searchUrl, param, "json");
		System.out.println(result);
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(!result.contains("成功")) return crawlResults;
		JSONObject resultObj = JSONObject.parseObject(result);
		JSONArray data = resultObj.getJSONArray("data");
		int size = data.size();
		if(size ==0) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + result);
			return crawlResults;
		}else{
			for(int s = 0; s<size;s++){
				JSONObject flight = data.getJSONObject(s);
				String flyNo = flight.getString("flyNo");
				String fliEN = flyNo.substring(0, 2);
				String shareFlight = this.getShareFlight(fliEN);
				String orgcity = flight.getString("orgcity");
				String dstcity = flight.getString("dstcity");
				String depdate = flight.getString("depdate");
				String arridate = flight.getString("arridate");
				String depTime = depdate.substring(11, 16);
				String arrTime = arridate.substring(11, 16);
				int cabinSize = flight.getJSONArray("cabins").size();
				List<String> cabinList = new ArrayList<String>();
				for(int c = 0; c<cabinSize; c++){
					int index = cabinSize-1-c;
					headerMap.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
					String cParam = "{\"orgCode\":\""+dep+"\",\"destCode\":\""+des+"\",\"departureDateStr\":\""+depDate+"\",\"flightNo\":\""+flyNo+"\",\"cabinType\":"+index+"}";
					String rankUrl = "http://b2capi.donghaiair.cn/ibe/rankInfos";
					String rankResult = this.httpProxyPost(httpClientSessionVo, rankUrl, cParam,"other");
					if(rankResult==null) continue;
					if(!rankResult.contains("成功")) continue;
					cabinList.add(rankResult);
				}
				CrawlResultB2C crawlResult = null;
				for(String cabinResult : cabinList){
					JSONObject cabinObj = JSONObject.parseObject(cabinResult);
					JSONArray datas = cabinObj.getJSONArray("data");
					int dataSize = datas.size();
					for(int a =0; a<dataSize; a++){
						JSONObject Data = datas.getJSONObject(a);
						String seat = Data.getString("seat");
						if(seat.equals("已售罄")) continue;
						if("A".equals(seat)) seat = "9";
						String cabinCode = Data.getString("cabinCode");
						String airportTax = Data.getString("airportTax");
						String fuelTax = Data.getString("fuelTax");
						BigDecimal airtax = new BigDecimal(airportTax);
						BigDecimal fueltax = new BigDecimal(fuelTax);
						BigDecimal tax = airtax.add(fueltax);
						String price = Data.getString("onewayPrice");
						
						crawlResult = new CrawlResultB2C(this.getJobDetail(), fliEN, flyNo, shareFlight, orgcity, dstcity, depDate ,cabinCode);
						crawlResult.setDepTime(depTime);	// 出发时间
						crawlResult.setDesTime(arrTime);	// 到达时间
						crawlResult.setRemainSite(Integer.parseInt(seat));
						crawlResult.setTicketPrice(new BigDecimal(price));
						crawlResult.setSalePrice(tax);
						crawlResults.add(crawlResult);
					}
				}
	        }
			return crawlResults;
		}
		
    }
}