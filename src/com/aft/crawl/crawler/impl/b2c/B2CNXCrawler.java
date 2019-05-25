package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.inter.CrawlInterFlightRoute;
import com.aft.crawl.result.vo.inter.CrawlInterFlightSegment;
import com.aft.crawl.result.vo.inter.CrawlInterFlightTrip;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 澳门航空 
 */
public class B2CNXCrawler extends Crawler {

	private final static String queryUrl = "https://b2c.airmacau.com.mo/nxet/reservation/AVQuery.do?language=CN&orgcity=%depCode%&dstcity=%desCode%&takeoffDate=%depDate%&cabinType=ECONOMY&adultCount=1&childCount=0&tripType=%tripType%&sureDate=1&CURRENCY=CNY&returnDate=%backDate%";

	private final static String brandQueryUrl = "https://b2c.airmacau.com.mo/nxet/reservation/reservation/BrandQuery.do";
	
	private final static String fltReturnUrl = "https://b2c.airmacau.com.mo/nxet/reservation/reservation/FltReturn.do";
	
	private final static String fltPriceCalUrl = "https://b2c.airmacau.com.mo/nxet/reservation/reservation/FltPriceCal.do";
	
	private final static String forPassengerInputUrl = "https://book.hongkongairlines.com/hxet/reservation/forPassengerInput.do";
	
	private final MyHttpClientSession httpClientSession = new MyHttpClientSession();
	
	public B2CNXCrawler(String threadMark) {
		super(threadMark);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		final String flightCookieRemark = "flightCookieRemark";
		String httpResult = this.httpGetResult(this.getFlightUrl(), flightCookieRemark, null);
		if(this.isTimeout()) return null;
		
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			
			Document document = Jsoup.parse(httpResult);
			if(null != document.getElementById("error_page")) return null;
			
			Element eleInsPar2 = document.select("#mainboxall > div.content_inside > div.ins_con1 > div.ins_par2").first();
			if(null != eleInsPar2) crawlResults = this.oneFlight(crawlResults, document, flightCookieRemark);
//			else crawlResults = this.moreFlight(crawlResults, document, flightCookieRemark);
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
	private List<CrawlResultBase> oneFlight(List<CrawlResultBase> crawlResults, Document document, String flightCookieRemark) {
		final Elements eleTgqs = document.select("#mainboxall > div.content_inside > div.ins_con1 > div.ins_par2 > ul.ins_fli_par1 > li");
		if(null == eleTgqs || eleTgqs.isEmpty()) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息....");
			return null;
		}
		final String random = document.getElementById("random").val();
		CrawlInterResult crawlResult = null;
		for(int i=0; i<eleTgqs.size(); i++) {
			try {
				if(i >= 2) break ;	// 只要前2个
				if(this.isTimeout()) return crawlResults;
				
				Element eleTgq = eleTgqs.get(i);
				String tgq = eleTgq.attr("info").trim();
				logger.info(this.getJobDetail().toStr() + ", 退改签信息: " + tgq);
				String[] tgqInfos = tgq.split("\\|");		// 退改签, 早购惠|人民币500元或等值货币|不允许|人民币500元或等值货币|598|不允许

				// 舱位等级
				Element eleBrandrdo = eleTgq.select("> ul > li > input").first();
				final String farefamilyname = eleBrandrdo.val();	// 当前舱位等级 radio 值
				final String brandseq = eleBrandrdo.attr("seqno");
				
				String params = "?_=" + System.currentTimeMillis() + "&farefamilyname=" + farefamilyname + "&brandseq=" + brandseq + "&random=" + random;
				final String brandQuerUrl = brandQueryUrl + params;
				final String brandQuerCookieRemark = "brandQuerCookieRemark";
				final String brandQueryResult = this.httpGetResult(brandQuerUrl, brandQuerCookieRemark, flightCookieRemark);
				logger.info(this.getJobDetail().toStr() + ", 请求舱位等级返回: " + brandQueryResult);
				if(this.isTimeout()) return crawlResults;
				
				String needShareFlightStr = this.getTimerJob().getParamMapValueByKey("needShareFlight");
				
				Document documentBrandrdo = Jsoup.parse(MyJsonTransformUtil.readValue(brandQueryResult, Map.class).get("fltcontext").toString());
				Elements eleFlightGos = documentBrandrdo.select("body > table.dattbl2 > tbody > tr");
				for(int j=1; j<eleFlightGos.size(); j+=2) {			// 航班	离境	抵达	时段	停止	客机	现有机位
					try {
						Element eleTrGo = eleFlightGos.get(j);
						logger.info(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 去程航班信息: " + eleTrGo.text());
						
						Elements eleTdsGo = eleTrGo.select(" > td");
						
						// 航班号, 航司
						String fltNoGo = eleTdsGo.get(0).ownText().replaceAll(" ", "").trim().toUpperCase();
						String airlineCodeGo = fltNoGo.substring(0, 2);

						// 判断共享
						String shareFlight = this.getShareFlight(airlineCodeGo);
						if("Y".equalsIgnoreCase(shareFlight) && !"Y".equalsIgnoreCase(needShareFlightStr)) {
							logger.debug(this.getJobDetail().toStr() + ", 共享航班[" + fltNoGo + "], 忽略!");
							continue ;
						}
						
						// 出发时间
						String depTimeGo = eleTdsGo.get(1).childNode(2).toString().trim();
						
						// 到达时间
						String desTimeGo = eleTdsGo.get(2).childNode(2).toString().trim();
						
						// 去程舱位
						String cabinGo = eleTdsGo.get(4).ownText().trim().toUpperCase();
						if(!this.allowCabin(cabinGo)) continue ;	// 排除舱位
						
						// 剩余座位数, 有位/5 剩馀座位
						String rowspan = eleTdsGo.get(6).attr("rowspan");	// 是否有中转
						if(StringUtils.isNotEmpty(rowspan)) {
							j += Integer.parseInt(rowspan) - 1;
							logger.debug(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 中转去程航班信息: " + eleTrGo.text());
							continue ;
						}
						String siteInfoGo = eleTdsGo.get(6).select("> span").first().ownText().trim();
						int remainSiteGo = CrawlerUtil.getCabinAmount(siteInfoGo.equals(">9") ? "A" : siteInfoGo.substring(0, 1));
						if(remainSiteGo <= 0) continue ;
						
						if(StringUtils.isEmpty(this.getJobDetail().getBackDate())) {	// 单程
							// 选择航班
							Element eleFltcss = eleFlightGos.get(j-1).select("> td.td1 > input.fltcss").first();
							String fltKeyGo = eleFltcss.val().trim();
							params = "?_=" + System.currentTimeMillis() + "&fltkey=" + fltKeyGo + "&currency=CNY" + "&random=" + random + "&farefamilyname=" + farefamilyname;
							final String fltPriceCalCookieRemark = "fltPriceCalCookieRemark";
							String fltPriceCalResult = this.httpGetResult(fltPriceCalUrl + params, fltPriceCalCookieRemark, brandQuerCookieRemark);
							logger.info(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 请求航班号[" + fltNoGo + "]信息返回: " + fltPriceCalResult);
							if(this.isTimeout()) return crawlResults;
							
							crawlResult = new CrawlInterResult(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate());
							crawlResult.setChangeInfo(tgqInfos[1]);		// 改期信息
							crawlResult.setRefundInfo(tgqInfos[2]);		// 退票信息
							crawlResult.setWuJiInfo(tgqInfos[3]);		// 误机信息
							crawlResult.setQianZhuanInfo(tgqInfos[4]);	// 签转信息
							
							crawlResult.setFltNo(fltNoGo);
							crawlResult.setAirlineCode(airlineCodeGo);
							crawlResult.setDepTime(depTimeGo);
							crawlResult.setDesTime(desTimeGo);
							crawlResult.setRemainSite(remainSiteGo);
							
							Map<String, Object> fltJoMap = MyJsonTransformUtil.readValue(fltPriceCalResult, Map.class);
							
							// 票面价
							String priceInfo = fltJoMap.get("convertresult").toString().replaceAll(",", "").trim();
							String currency = priceInfo.substring(0, 3);
							if(!"CNY".equalsIgnoreCase(currency)) {
								logger.warn(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 航班价格不是[CNY]-" + priceInfo);
								continue;
							}
							crawlResult.setCurrency(currency);
							
							Document documentFlt = Jsoup.parse("<table>" + fltJoMap.get("fltcal") + "</table>");
							
							// 价格信息
							Element elePriceInfo = documentFlt.select("body > table > tbody > tr.fltcaltr").first();

							crawlResult.setCabin(cabinGo);							
							
							String ticketPrice = elePriceInfo.child(4).ownText().trim().substring(4).trim().replaceAll(",", "");
							String taxFee = elePriceInfo.child(5).ownText().trim().substring(4).trim().replaceAll(",", "");
							crawlResult.setTicketPrice(new BigDecimal(ticketPrice));
							crawlResult.setTaxFee(new BigDecimal(taxFee));
							crawlResult.setShareFlight(shareFlight);
							crawlResults.add(crawlResult);
						} else {
							// 选择去程航班
							params = "?fltno=" + fltNoGo + "&farefamilyname=" + farefamilyname;
							final String fltReturnCookieRemark = "fltReturnCookieRemark";
							String fltReturnResult = this.httpGetResult(fltReturnUrl + params, fltReturnCookieRemark, brandQuerCookieRemark);
							logger.info(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 请求航班号[" + fltNoGo + "]信息返回: " + fltReturnResult);
							if(this.isTimeout()) return crawlResults;
							
							Document documentFltReturn = Jsoup.parse("<table>" + fltReturnResult + "</table>");
							Elements eleFlightBacks = documentFltReturn.select("body > table > tbody > tr");
							for(int k=1; k<eleFlightBacks.size(); k+=2) {	// 航班	离境	抵达	时段	停止	客机	现有机位
								crawlResult = new CrawlInterResult(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate(), this.getJobDetail().getBackDate());
								crawlResult.setChangeInfo(tgqInfos[1]);		// 改期信息
								crawlResult.setRefundInfo(tgqInfos[2]);		// 退票信息
								crawlResult.setWuJiInfo(tgqInfos[3]);		// 误机信息
								crawlResult.setQianZhuanInfo(tgqInfos[4]);	// 签转信息
								
								crawlResult.setFltNo(fltNoGo);
								crawlResult.setAirlineCode(airlineCodeGo);
								crawlResult.setDepTime(depTimeGo);
								crawlResult.setDesTime(desTimeGo);
								crawlResult.setRemainSite(remainSiteGo);
															
								Element eleTrBack = eleFlightBacks.get(k);
								logger.info(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 返程航班信息: " + eleTrBack.text());
								
								Elements eleTdsBack = eleTrBack.select(" > td");
								
								// 返程航班号, 航司
								String fltNoBack = eleTdsBack.get(0).ownText().replaceAll(" ", "").trim().toUpperCase();
								String airlineCodeBack = fltNoBack.substring(0, 2);

								// 判断共享
								String backShareFlight = this.getShareFlight(airlineCodeBack);
								if("Y".equalsIgnoreCase(shareFlight) && !"Y".equalsIgnoreCase(needShareFlightStr)) {
									logger.debug(this.getJobDetail().toStr() + ", 共享航班[" + fltNoGo + "], 忽略!");
									continue ;
								}
								
								// 返程出发时间
								String depTimeBack = eleTdsBack.get(1).childNode(2).toString().trim();
								
								// 返程到达时间
								String desTimeBack = eleTdsBack.get(2).childNode(2).toString().trim();
								
								String cabinBack = eleTdsBack.get(4).ownText().trim().toUpperCase();
								if(!this.allowCabin(cabinBack)) continue ;	// 排除舱位
								
								// 返程剩余座位数, 有位/5 剩馀座位
								rowspan = eleTdsBack.get(6).attr("rowspan");	// 是否有中转
								if(StringUtils.isNotEmpty(rowspan)) {
									k += Integer.parseInt(rowspan) - 1;
									logger.debug(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 中转返程航班信息: " + eleTrGo.text());
									continue ;
								}
								String siteInfoBack = eleTdsBack.get(6).select("> span").first().ownText().trim();
								int remainSiteBack = CrawlerUtil.getCabinAmount(siteInfoBack.equals(">9") ? "A" : siteInfoGo.substring(0, 1));
								if(remainSiteBack <= 0) continue ;
								
								// 选择航班
								String fltKeyGo = eleFlightBacks.get(k-1).select("> td.td1 > input.fltcss").first().val();
								params = "?_=" + System.currentTimeMillis() + "&fltkey=" + fltKeyGo + "&currency=CNY" + "&random=" + random + "&farefamilyname=" + farefamilyname;
								final String fltPriceCalCookieRemark = "fltPriceCalCookieRemark";
								String fltPriceCalResult = this.httpGetResult(fltPriceCalUrl + params, fltPriceCalCookieRemark, fltReturnCookieRemark);
								logger.info(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 请求返程航班号[" + fltNoBack + "]信息返回: " + fltPriceCalResult);
								if(this.isTimeout()) return crawlResults;
								
								
								Map<String, Object> fltJoMap = MyJsonTransformUtil.readValue(fltPriceCalResult, Map.class);
								
								// 票面价
								String priceInfo = fltJoMap.get("convertresult").toString().replaceAll(",", "").trim();
								String currency = priceInfo.substring(0, 3);
								if(!"CNY".equalsIgnoreCase(currency)) {
									logger.warn(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 航班价格不是[CNY]-" + priceInfo);
									continue;
								}
								
								Document documentFlt = Jsoup.parse("<table>" + fltJoMap.get("fltcal") + "</table>");
								
								// 价格信息
								Element elePriceInfo = documentFlt.select("body > table > tbody > tr.fltcaltr").first();
								
								crawlResult.setCabin(cabinGo);
								crawlResult.setBackCabin(cabinBack);
								
								String ticketPrice = elePriceInfo.child(4).ownText().trim().substring(4).trim().replaceAll(",", "");
								String taxFee = elePriceInfo.child(5).ownText().trim().substring(4).trim().replaceAll(",", "");
								crawlResult.setTicketPrice(new BigDecimal(ticketPrice));
								crawlResult.setTaxFee(new BigDecimal(taxFee));
								
								crawlResult.setBackFltNo(fltNoBack);
								crawlResult.setBackDepTime(depTimeBack);
								crawlResult.setBackDesTime(desTimeBack);
								crawlResult.setBackRemainSite(remainSiteBack);
								
								crawlResult.setShareFlight(shareFlight);
								crawlResult.setBackShareFlight(backShareFlight);
								
								crawlResult.setCurrency(currency);
								crawlResults.add(crawlResult);
							}
						}
					} catch(Exception e) {
						logger.error(this.getJobDetail().toStr() + ", " + tgqInfos[0] + ", 采集航班信息, 处理 航班信息 异常: \r", e);
					}
				}
			} catch(Exception e) {
				logger.error(this.getJobDetail().toStr() + ", 采集舱位等级信息, 处理 舱位等级 异常: \r", e);
			}
		}
		return crawlResults;
	}
	
	/**
	 * 中转航班
	 * @param crawlResults
	 * @param document
	 * @param flightCookieRemark
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<CrawlResultBase> moreFlight(List<CrawlResultBase> crawlResults, Document document, String flightCookieRemark) {
		Element eleTableOW = document.getElementById("tableOW");
		if(null == eleTableOW) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息....");
			return crawlResults;
		}
		
		final String forPassengerInputCookieRemark = "forPassengerInputCookieRemark";
		final String random = document.select("#nextform > input[name=random]").first().val().trim();
		Elements eleFlightOWsGo = document.select("#tableOW > tbody > tr > td.td1 > input[name=flightOW]");
		for(Element eleFlightOWGo : eleFlightOWsGo) {
			String flightOW = eleFlightOWGo.val();
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("language", "CN");
			paramMap.put("tripType", "OW");
			paramMap.put("adultCount", "1");
			paramMap.put("childCount", "0");
			paramMap.put("infantCount", "0");
			paramMap.put("from", "stopOver");
			paramMap.put("querySegSize", "1");
			paramMap.put("cabinType", "ECONOMY");
			paramMap.put("negoAvailable", "false");
			paramMap.put("random", random);
			paramMap.put("flightOW", flightOW);
			paramMap.put("org_cityCode", this.getJobDetail().getDepCode());
			paramMap.put("des_cityCode", this.getJobDetail().getDesCode());
			paramMap.put("departureDate", this.getJobDetail().getDepDate());
			String httpResult = this.httpPostResult(forPassengerInputUrl, paramMap, forPassengerInputCookieRemark, flightCookieRemark);
			if(this.isTimeout()) return null;
			
			document = Jsoup.parse(httpResult);
			Element eleInsPsgrPar = document.select("div.content_inside > div.ins_con1 > div.ins_par1 > div.ins_par1_con > div.ins_psgr_par").first();
			CrawlInterFlightRoute crawlResult = new CrawlInterFlightRoute(this.getJobDetail(), this.getJobDetail().getDepCode(), this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate());
//			crawlResult.setChangeInfo(tgqInfos[1]);		// 改期信息
//			crawlResult.setRefundInfo(tgqInfos[2]);		// 退票信息
//			crawlResult.setWuJiInfo(tgqInfos[3]);		// 误机信息
//			crawlResult.setQianZhuanInfo(tgqInfos[5]);	// 签转信息
			
			String ticketPrice = eleInsPsgrPar.select("> table.dattbl9 > tbody > tr:nth-child(2) > td:nth-child(2)").first().ownText().trim();
			crawlResult.setTicketPrice(new BigDecimal(ticketPrice));
			
			String taxFee = eleInsPsgrPar.select("> table.dattbl5 > tbody > tr > td").first().ownText().trim();
			crawlResult.setTaxFee(new BigDecimal(taxFee.replaceAll(",", "").replaceAll("CNY", "").trim()));
			
			Elements eleTrsGo = eleInsPsgrPar.select("#fareBasisInfo > td > table > tbody > tr");
			for(int i=1; i<eleTrsGo.size(); i++) {
				logger.info(this.getJobDetail().toStr() + ", 航班信息:" + eleTrsGo.get(i).text());
				Elements eleTdGos = eleTrsGo.get(i).children();
				
				String[] depDesCodes = eleTdGos.get(0).ownText().trim().split("-");
				String depCode = depDesCodes[0].trim();
				String desCode = depDesCodes[1].trim();
				
				String fltNoGo = eleTdGos.get(1).ownText().trim();
				String airlineCodeGo = fltNoGo.substring(0, 2);
				
				String cabinGo = "";
				
				int remainSiteGo = 10;
				
				List<CrawlInterFlightSegment> flightSegmentsGo = new ArrayList<CrawlInterFlightSegment>();
				CrawlInterFlightSegment flightSegmentGo = new CrawlInterFlightSegment(airlineCodeGo, fltNoGo, cabinGo,
						depCode, desCode, remainSiteGo, 1);
				flightSegmentsGo.add(flightSegmentGo);
				CrawlInterFlightTrip flightTripGo = new CrawlInterFlightTrip(airlineCodeGo, remainSiteGo, 1, flightSegmentsGo);
				crawlResult.getFlightTrips().add(flightTripGo);
				
				crawlResults.add(crawlResult);
			}
		}
		return crawlResults;
	}

	@Override
	public String httpResult() throws Exception {
		return null;
	}
	
	/**
	 * 获取请求内容
	 * @param url
	 * @param cookieRemark
	 * @param parentCookieRemark
	 * @return
	 */
	private String httpGetResult(String url, String cookieRemark, String parentCookieRemark) {
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession, cookieRemark, parentCookieRemark);
		return this.httpProxyGet(httpClientSessionVo, url, "html");
	}
	
	/**
	 * 获取请求内容
	 * @param url
	 * @param paramMap
	 * @param cookieRemark
	 * @param parentCookieRemark
	 * @return
	 */
	private String httpPostResult(String url, Map<String, Object> paramMap, String cookieRemark, String parentCookieRemark) {
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession, cookieRemark, parentCookieRemark);
		return this.httpProxyPost(httpClientSessionVo, url, paramMap, "html");
	}
	
	/**
	 * 获取url
	 * @return
	 */
	private String getFlightUrl() {
		String flihgtUrl = queryUrl.replaceAll("%depCode%", this.getJobDetail().getDepCode())
					.replaceAll("%desCode%", this.getJobDetail().getDesCode())
					.replaceAll("%depDate%", this.getJobDetail().getDepDate());
		if(StringUtils.isEmpty(this.getJobDetail().getBackDate())) flihgtUrl = flihgtUrl.replaceAll("%tripType%", "OW").replaceAll("&returnDate=%backDate%", "");
		else flihgtUrl = flihgtUrl.replaceAll("%tripType%", "RT").replaceAll("%backDate%", this.getJobDetail().getBackDate());
		return flihgtUrl;
	}
}