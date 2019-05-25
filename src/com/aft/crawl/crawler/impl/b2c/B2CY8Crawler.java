package com.aft.crawl.crawler.impl.b2c;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;

/**
 * 扬子江航空 
 */
public class B2CY8Crawler extends Crawler {

	private final static String queryUrl = "http://www.yzr.com.cn/flight/searchflight!getFlights.action";
	
//	private final static String refererUrl = "http://www.fuzhou-air.cn/flight/searchflight.action?tripType=ONEWAY&orgCity1=FOC&dstCity1=XIY&flightdate1=%depDate%&flightdate2=";
	
	private final static String params = "<flight><tripType>ONEWAY</tripType><orgCity1>%depCode%</orgCity1><dstCity1>%desCode%</dstCity1><flightdate1>%depDate%</flightdate1><index>1</index></flight>";
	
	public B2CY8Crawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			Document jdomDoc = new SAXBuilder().build(new ByteArrayInputStream(httpResult.getBytes("UTF-8")));  
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
				
				for(Element productEle : segmentEle.getChild("products").getChildren()) {	// 只要网站共享中的最低价
//					String productCode = productEle.getAttributeValue("code");
//					if(!"WONLY".equals(productCode)) continue ;
					List<Element> cabinEles = productEle.getChild("cabins").getChildren();
					Element cabinEle = cabinEles.get(cabinEles.size() - 1);
					// 舱位
					String cabin = cabinEle.getAttributeValue("cabinCode").toString().trim().toUpperCase();
					
					crawlResult = new CrawlResultB2C(this.getJobDetail(), airlineCode, fltNo, shareFlight, depCode, desCode, depDate, cabin);
					crawlResult.setDepTime(depTime);	// 出发时间
					crawlResult.setDesTime(desTime);	// 到达时间
					
					// 剩余座位数
					crawlResult.setRemainSite(Integer.parseInt(cabinEle.getAttributeValue("inventory").toString().trim()));
					
					// 价格
					crawlResult.setTicketPrice(new BigDecimal(cabinEle.getChildText("auditFare").toString().trim()).setScale(0));
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
	
	
//	@Override
//	protected boolean requestAgain(String httpResult, org.jsoup.nodes.Document document, Object jsonObject, String returnType) throws Exception {
//		org.jsoup.nodes.Element eleImg = document.select("body > div.bg > div.cont > div.c1 > img.img1").first();
//		boolean flag = null != eleImg;
//		return flag;
//	}

//	@Override
//	@SuppressWarnings("unchecked")
//	protected boolean needToChangeIp(String httpResult, org.jsoup.nodes.Document document, Object jsonObject, String returnType) throws Exception {
//		boolean flag = super.needToChangeIp(httpResult, document, jsonObject, returnType);
//		if(flag) return true;
//		return httpResult.contains("sphinx='sphinx'");
//	}
	
	@Override
	public String httpResult() throws Exception {
//		if(null == headerMap.get("Cookie")) {
//			synchronized(B2CY8Crawler.class) {
//				if(null == headerMap.get("Cookie")) {
//					headerMap.put("Referer", refererUrl.replaceAll("%depDate%", this.getJobDetail().getDepDate()));
//					headerMap.put("Cookie", this.getTimerJob().getParamMapValueByKey("jSessdionId"));
//				}
//			}
//		}
		String httpContent = params.replaceAll("%depCode%", this.getJobDetail().getDepCode())
								.replaceAll("%desCode%", this.getJobDetail().getDesCode())
								.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		return this.httpProxyPost(queryUrl, httpContent, "GB2312", "xml");
	}
}