package com.aft.crawl.crawler.impl.b2c;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientUtil;

/**
 * 乌鲁木齐航空 
 */
public class B2CUQCrawler extends Crawler {

	private final static String queryUrl = "http://www.urumqi-air.com/flight/searchflight!getFlights.action";
	
	private final static String params = "<flight><tripType>ONEWAY</tripType><orgCity1>%depCode%</orgCity1><dstCity1>%desCode%</dstCity1><flightdate1>%depDate%</flightdate1><index>1</index></flight>";

	public B2CUQCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			org.jdom2.Document jdomDoc = new SAXBuilder().build(new ByteArrayInputStream(httpResult.getBytes("UTF-8")));  
			Element rootEle = jdomDoc.getRootElement(); 
			
			// 出发日期
			String depDate = rootEle.getChildText("flightdate").toString().trim();
			
			// 出发机场
			String depCode = rootEle.getChild("orgCity").getChildText("code").toString().trim();
			
			// 到达机场
			String desCode = rootEle.getChild("dstCity").getChildText("code").toString().trim();
			
			CrawlResultB2C crawlResult = null;
			for(Element segmentEle : rootEle.getChild("segments").getChildren()) {
				// 航班号,航司
				String airlineCode = segmentEle.getChildText("airline").toString().trim().toUpperCase();
				String fltNo = airlineCode + segmentEle.getChildText("flightno").toString().trim();
				
				// 判断共享
				String shareFlight = this.getShareFlight(airlineCode);
				
				// 出发时间
				String depTime = segmentEle.getChildText("deptime").toString().trim();
				
				// 到达时间
				String desTime = segmentEle.getChildText("arrtime").toString().trim();
				
				for(Element productEle : segmentEle.getChild("products").getChildren()) {
					for(Element cabinEle : productEle.getChild("cabins").getChildren()) {
						// 舱位
						String cabin = cabinEle.getAttributeValue("cabinCode").toString().trim().toUpperCase();
						
						crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
						crawlResult.setDepTime(depTime);	// 出发时间
						crawlResult.setDesTime(desTime);	// 到达时间
						
						// 剩余座位数
						String remainSite = cabinEle.getAttributeValue("inventory").toString().trim();
						crawlResult.setRemainSite(Integer.parseInt(remainSite));
						if(crawlResult.getRemainSite() <= 0) continue ;
						
						// 价格
						crawlResult.setTicketPrice(new BigDecimal(cabinEle.getChildText("auditFare").toString().trim()).setScale(0));
						crawlResult.setSalePrice(crawlResult.getTicketPrice());
						crawlResults.add(crawlResult);
					}
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
//	@Override
//	protected boolean requestAgain(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
//		if("html_other".equals(returnType)) return false;
//		
//		Elements eleImgs = document.select("body > div.bg > div.cont > div.c1 > img.img1");
//		if(!eleImgs.isEmpty()) {
//			Crawler.clearAllTemp(this.getTimerJob());
//			headerMap.remove("Cookie");
//			ProxyUtil.changeProxy(this.threadMark, this.getTimerJob().getJobId(), this.getPageType());
//			MyHttpClientResultVo vo = this.httpProxyResultVoGet("http://www.urumqi-air.com/", "html");
//			if(null == vo) return true;
//			
//			Map<String, Object> paramMap = new HashMap<String, Object>();
//			String jSId = MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Set-Cookie", "JSESSIONID=");
//			System.out.println(jSId);
//			
//			paramMap.put("Cookie", jSId);
//			this.putBaseParamMap(paramMap);
//			headerMap.put("Cookie", jSId);
//			
//			this.httpProxyGet("http://www.urumqi-air.com/flight/searchflight.action?tripType=ONEWAY&orgCity1="
//					+ this.getJobDetail().getDepCode() + "&dstCity1=" + this.getJobDetail().getDesCode()
//					+ "&flightdate1=" + this.getJobDetail().getDepDate() + "&flightdate2=", "GB2312", "html_other");
//			return true;
//		}
//		return false;
//	}

	@Override
	protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType) throws Exception {
		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
		if(flag) return true;
		
//		if("html_other".equals(returnType)) return false;
		return false;
	}

	@Override
	public String httpResult() throws Exception {
		if(null == this.getBaseJobParamMap() || this.getBaseJobParamMap().isEmpty()) {
			synchronized(queryUrl) {
				if(null == this.getBaseJobParamMap() || this.getBaseJobParamMap().isEmpty()) {
					headerMap.remove("Cookie");
					MyHttpClientResultVo vo = super.httpProxyResultVoGet("http://www.urumqi-air.com/", "html");
					Map<String, Object> paramMap = new HashMap<String, Object>();
					String jSId = MyHttpClientUtil.getHeaderValue(vo.getHeaders(), "Set-Cookie", "JSESSIONID=");
					System.out.println(jSId);
					
					paramMap.put("Cookie", jSId);
					this.putBaseJobParamMap(paramMap);
					headerMap.put("Cookie", jSId);
					
					this.httpProxyGet("http://www.urumqi-air.com/flight/searchflight.action?tripType=ONEWAY&orgCity1="
							+ this.getJobDetail().getDepCode() + "&dstCity1=" + this.getJobDetail().getDesCode()
							+ "&flightdate1=" + this.getJobDetail().getDepDate() + "&flightdate2=", "GB2312", "html_other");
				}
			}
		}// else if(null == headerMap.get("Cookie")) headerMap.putAll(this.getBaseJobParamMap());
		
		String httpContent = params.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode())
								.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return this.httpProxyPost(queryUrl, httpContent, "GB2312", "xml");
	}
}