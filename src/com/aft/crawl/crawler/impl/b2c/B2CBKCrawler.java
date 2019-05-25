package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.bean.CrawlCommon;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.proxy.ProxyUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;
import com.aft.utils.thread.MyThreadUtils;

/**
 * 奥凯航空 
 * 只能一线程一ip, 中间间隔5s
 */
public class B2CBKCrawler extends Crawler {
	
	private final static String indexUrl = "http://bk.travelsky.com/bkair/reservation/indexLowPriceTkt.do";
	
	private final static String queryUrl = "http://bk.travelsky.com/bkair/reservation/queryLowPriceFlight.do";
	
	private final static String ipListStr = "ipList";
	
	private final static String threadMarkParamMapStr = "threadMarkParamMap";
	
	private int maxTry = 5;
	
	public B2CBKCrawler(String threadMark) {
		super(threadMark, false);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document document = Jsoup.parse(httpResult);
			Elements eleDivs = document.select("#result_table > div.moreTicket");
			if(null == eleDivs || eleDivs.isEmpty()) {
				Element ele = document.getElementById("result_table");
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + (null == ele ? httpResult : ele.html()));
				return crawlResults;
			}
			
			eleDivs = document.select("#result_table > div");
			CrawlResultB2C crawlResult = null;
			for(int i=1; i<eleDivs.size(); i+=4) {
				Elements eleLis = eleDivs.get(i).select("> div.information_list > ul > li");
				
				// 航班号,航司
				String fltNo = eleLis.get(0).ownText().toUpperCase().trim();
				String airlineCode = fltNo.substring(0, 2).trim();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				// 出发时间
				String depTime = eleLis.get(1).ownText().trim();
				
				// 到达时间
				String desTime = eleLis.get(1).select("> p").first().ownText().trim();
				
				
				Elements eleInputs = eleDivs.get(i).select("> div.information_list > ul > li > input[name=inter]");
				Elements eleInputs1 = eleDivs.get(i+2).select("> ul > li > input[name=inter]");
				if(null != eleInputs1 && !eleInputs1.isEmpty()) eleInputs.addAll(eleInputs1);
				
				for(Element eleInput : eleInputs) {
					// showEI(this,"0", "0", "","Z","1","1","2","50.000000","0","390.000000")
					String onclick = eleInput.attr("onclick").replaceAll("\"", "");
					String[] onclicks = onclick.substring(onclick.indexOf("("), onclick.indexOf(")")).split(",");
					
					// 舱位
					String cabin = onclicks[4].toUpperCase();
					
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight,
							this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate(), cabin);
					
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					
					// 剩余座位数
					crawlResult.setRemainSite(10);
					
					// 价格
					BigDecimal ticketPrice = new BigDecimal(onclicks[10].trim());
					crawlResult.setTicketPrice(ticketPrice);
					crawlResult.setSalePrice(crawlResult.getTicketPrice());
					crawlResults.add(crawlResult);
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		} finally {
			maxTry = 5;
			MyThreadUtils.sleep(5000);	// 中间要停顿5秒
		}
		return crawlResults;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected boolean returnEmptyChangeIp(String httpResult) throws Exception {
		if(StringUtils.isEmpty(httpResult)) {
			Map<String, Map<String, Object>> threadMarkParamMap = (Map<String, Map<String, Object>>)this.getBaseJobParamMap().get(this.threadMark);
			if(null != threadMarkParamMap.get(threadMarkParamMapStr)) threadMarkParamMap.get(threadMarkParamMapStr).clear();
			logger.info(this.getJobDetail().toStr() + ", 要切换ip了...");
			MyThreadUtils.sleep(1000);	// 中间要停顿5秒
			return true;
		}
		return false;
	}

	@Override
	protected boolean requestAgain(String httpResult, Document document,
			Object jsonObject, String returnType) throws Exception {
		Element ele = document.select("table > tbody > tr > td > table > tbody > tr > td > table > tbody > tr > td").first();
		if(null != ele && ele.ownText().trim().contains("重复查询")) {
			MyThreadUtils.sleep(5000);
			return true;
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String httpResult() throws Exception {
		this.getProxyIp();
		
		// ip线程基础共享参数
		Map<String, Map<String, Object>> threadMarkParamMap = (Map<String, Map<String, Object>>)this.getBaseJobParamMap().get(this.threadMark);
		if(null == threadMarkParamMap) {
			threadMarkParamMap = new HashMap<String, Map<String,Object>>();
			this.getBaseJobParamMap().put(this.threadMark, threadMarkParamMap);
		}
		Map<String, Object> threadMarkMap = threadMarkParamMap.get(threadMarkParamMapStr);
		if(null == threadMarkMap || threadMarkMap.isEmpty()) {
			this.changeProxyIp();
			
			MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(new MyHttpClientSession(HttpClients.createDefault()), null);
			String httpResult = super.httpProxyGet(httpClientSessionVo, indexUrl, "html");
			if(null == httpResult) {	// 超时的...
				if(--maxTry < 0) return null;
				MyThreadUtils.sleep(1000);
				return this.httpResult();
			}
			Document doc = Jsoup.parse(httpResult);
			String lowPriceRequestId = doc.select("#queryFlightLowForm > input[name=lowPriceRequestId]").first().val();
			String queryFlightRequestId = doc.select("#queryFlightLowForm > input[name=queryFlightRequestId]").first().val();
			if(null == lowPriceRequestId || null == queryFlightRequestId) {
				if(--maxTry < 0) return null;
				MyThreadUtils.sleep(1000);
				return this.httpResult();
			}
			
			if(null == threadMarkMap) {
				threadMarkMap = new HashMap<String, Object>();
				threadMarkParamMap.put(threadMarkParamMapStr, threadMarkMap);
			}
			threadMarkMap.put("lowPriceRequestId", lowPriceRequestId);
			threadMarkMap.put("queryFlightRequestId", queryFlightRequestId);
			threadMarkMap.put("httpClientSessionVo", httpClientSessionVo);
		}
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("tripType", "0");
		paramMap.put("orgCity", this.getJobDetail().getDepCode());
		paramMap.put("destCity", this.getJobDetail().getDesCode());
		paramMap.put("takeoffDate", this.getJobDetail().getDepDate());
		paramMap.put("lowPriceRequestId", threadMarkMap.get("lowPriceRequestId").toString());
		paramMap.put("queryFlightRequestId", threadMarkMap.get("queryFlightRequestId").toString());
		return super.httpProxyPost((MyHttpClientSessionVo)threadMarkMap.get("httpClientSessionVo"), queryUrl, paramMap, "html");
	}
	
	/**
	 * 获取ip集合
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private final void getProxyIp() throws Exception {
		if(null != this.getBaseJobParamMap() && null != this.getBaseJobParamMap().get(ipListStr)) return ;
		synchronized(indexUrl) {
			if(null != this.getBaseJobParamMap() && null != this.getBaseJobParamMap().get(ipListStr)) return ;
			// {"success":true,"msg":["124.161.189.47:12089","221.10.90.46:12116","124.161.189.47:12112"]}
			this.putBaseJobParamMap(new HashMap<String, Object>());
			String httpResult = MyHttpClientUtil.get(CrawlCommon.getAllProxyIpUrl());
			logger.info(this.getJobDetail().toStr() + ", 获取所有代理返回:" + httpResult);
			Map<String, Object> jsonMap = MyJsonTransformUtil.readValue(httpResult, Map.class);
			List<String> ipList = (List<String>)jsonMap.get("msg");
			if(null != ipList && !ipList.isEmpty()) this.getBaseJobParamMap().put(ipListStr, ipList);
		}
	}
	
	/**
	 * 切换ip
	 * @throws Exception 
	 * 
	 */
	@SuppressWarnings("unchecked")
	private final void changeProxyIp() throws Exception {
		logger.info(this.getJobDetail().toStr() + ", 要切换ip中...");
		if(null == this.getBaseJobParamMap()) return ;
		if(null == this.getBaseJobParamMap().get(ipListStr)) return ;
		
		logger.info(this.getJobDetail().toStr() + ", 要切换ip中, 进入同步锁...");
		synchronized(ipListStr) {
			for(String proxy : (List<String>)this.getBaseJobParamMap().get(ipListStr)) {
				String[] proxys = proxy.split(":");
				if(ProxyUtil.existProxy(proxys[0], Integer.parseInt(proxys[1]))) continue ;
				ProxyUtil.setProxy(this.threadMark, this.getTimerJob().getJobId(), this.getPageType(), proxys[0], Integer.parseInt(proxys[1]));
				logger.info(this.getJobDetail().toStr() + ", threadMark[" + this.threadMark + "], 使用代理:" + proxy);
				break ;
			}
		}
	}
}