package com.aft.crawl.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import scala.math.BigDecimal;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.SwitchTypeEnum;
import com.aft.crawl.bean.CrawlCommon;
import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.bean.TimerJob;
import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.result.kafka.runnable.PostMqResultRunnable;
import com.aft.crawl.result.post.runnable.PostJxdMtResultRunnable;
import com.aft.crawl.result.post.runnable.PostRemoteResultRunnable;
import com.aft.crawl.result.post.runnable.PostTSLCCResultRunnable;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.PostMqDcVo;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.b2c.CrawlResultB2CRt;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.crawl.result.vo.inter.CrawlInterResult;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.lcc.LCCPostDataConversion;
import com.aft.crawl.thread.ThreadController;
import com.aft.crawl.thread.ThreadType;
import com.aft.logger.MyCrawlerLogger;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.copy.MyCopyUtils;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

public final class ResultPost {
	
	private final static Logger logger = Logger.getLogger(ResultPost.class);
	
	/**
	 * 处理东航会员价
	 * 
	 * @param jobDetail
	 * @param crawler
	 * @param crawlResults
	 */
	public static void muMemberPrice(String threadMark, JobDetail jobDetail, List<CrawlResultBase> crawlResults) {
		try {
			if(crawlResults.isEmpty() || !jobDetail.getPageType().equals(CrawlerType.B2CMUPageType)) return ;	//  分离单独的会员价
			
			CrawlerType crawlerType = CrawlerType.getCrawlerType(CrawlerType.B2CMUMemberPageType);
			if(null == crawlerType) return ;
			
			List<CrawlResultBase> crs = new ArrayList<CrawlResultBase>();
			for(CrawlResultBase crawlResult : crawlResults) {
				if(!((CrawlResultB2C)crawlResult).getTemp().startsWith("会员价-")) continue ;
				CrawlResultB2C cr = MyCopyUtils.deepCopy((CrawlResultB2C)crawlResult);
				cr.setPageType(crawlerType.getPageType());
				cr.setPageTypeMemo(crawlerType.getPageTypeMemo());
				crs.add(cr);
			}
			
			ResultPost.saveFileLogger(jobDetail.getTimerJob(), crs);		// 特殊处理, 别乱动...
			
			ResultPost.filterResult(jobDetail.getTimerJob(), crs, false);	// 排序,过滤
			
			ResultPost.postResult(threadMark, jobDetail, crs);
		} catch(Exception e) {
			logger.error("分离单独的会员价异常:\r", e);
		}
	}
	
	/**
	 * 保存结果文件
	 * @param timerJob
	 * @param crawlResults
	 */
	public static void saveFileLogger(TimerJob timerJob, List<CrawlResultBase> crawlResults) {
		if(crawlResults.isEmpty()) return ;
		try {
			String pageType = crawlResults.get(0).getPageType();
			SwitchTypeEnum switchTypeEnum = SwitchTypeEnum.getSwitchTypeEnum(pageType);
			if(null == switchTypeEnum) return ;
			Logger saveFileLogger = MyCrawlerLogger.getSaveFileLogger(timerJob, pageType);
			if(null == saveFileLogger) return ;
			for(CrawlResultBase crawlResult : crawlResults) {
				saveFileLogger.info(crawlResult.toSaveFileStr());
			}
		} catch(Exception e) {
			logger.error("saveFileLogger异常:\r", e);
		}
	}
	
	/**
	 * 排序, 过滤 只限国内, 国际
	 * @param timerJob
	 * @param crawlResults
	 */
	public static void filterResult(TimerJob timerJob, List<CrawlResultBase> crawlResults) {
		ResultPost.filterResult(timerJob, crawlResults, true);
	}
	
	/**
	 * 排序, 过滤 只限国内, 国际
	 * @param timerJob
	 * @param crawlResults
	 * @param filter 是否过滤条件
	 */
	public static void filterResult(TimerJob timerJob, List<CrawlResultBase> crawlResults, boolean filter) {
		if(crawlResults.isEmpty()) return ;
		final CrawlResultBase resultBase = crawlResults.get(0);
		if(!(resultBase instanceof CrawlResultB2C) && !(resultBase instanceof CrawlInterResult) &&!(resultBase instanceof FlightData)) return ;
		// 排序, 价格低到高
		Collections.sort(crawlResults, new Comparator<CrawlResultBase>() {
			@Override
			public int compare(CrawlResultBase o1, CrawlResultBase o2) {
				if(resultBase instanceof CrawlResultB2C) {
					CrawlResultB2C b2cResult1 = (CrawlResultB2C)o1;
					CrawlResultB2C b2cResult2 = (CrawlResultB2C)o2;
					int c1 = b2cResult1.getFltNo().compareTo(b2cResult2.getFltNo());
					if(0 != c1) return c1;
					return b2cResult1.getTicketPrice().compareTo(b2cResult2.getTicketPrice());
				}else if(resultBase instanceof FlightData) {
						FlightData fData = (FlightData)resultBase;
						if("OW".equals(fData.getRouteType()) && fData.getFromSegments().size()==1){
							//单程一段情况,适用国内，国际有需要在另加
							FlightData fData1 = (FlightData)o1;
							FlightData fData2 = (FlightData)o2;
							FlightSegment flightSegment1 = fData1.getFromSegments().get(0);
							FlightSegment flightSegment2 = fData2.getFromSegments().get(0);
							int c1 =flightSegment1.getFlightNumber().compareTo(flightSegment2.getFlightNumber());
							if(0 != c1) return c1;
							FlightPrice flightPrice1 = fData1.getPrices().get(0);
							FlightPrice flightPrice2 = fData2.getPrices().get(0);
							String p1 = flightPrice1.getFare();
							if(p1.contains(".")) p1 = p1.substring(0, p1.indexOf("."));
							String p2 = flightPrice2.getFare();
							if(p2.contains(".")) p2 = p2.substring(0, p2.indexOf("."));
							return Integer.valueOf(p1).compareTo(Integer.valueOf(p2));
						}else return 0;
				} else if(resultBase instanceof CrawlInterResult) {
					CrawlInterResult interResult1 = (CrawlInterResult)o1;
					CrawlInterResult interResult2 = (CrawlInterResult)o2;
					String fltNo1 = interResult1.getFltNo() + interResult1.getBackFltNo();
					String fltNo2 = interResult2.getFltNo() + interResult2.getBackFltNo();
					
					int c1 = fltNo1.compareTo(fltNo2);
					if(0 != c1) return c1;
					return interResult1.getTicketPrice().compareTo(interResult2.getTicketPrice());
				} else if(resultBase instanceof CrawlResultB2CRt) {
					CrawlResultB2CRt b2cResult1 = (CrawlResultB2CRt)o1;
					CrawlResultB2CRt b2cResult2 = (CrawlResultB2CRt)o2;
					String fltNo1 = b2cResult1.getFltNo() + b2cResult1.getBackFltNo();
					String fltNo2 = b2cResult2.getFltNo() + b2cResult2.getBackFltNo();
					
					int c1 = fltNo1.compareTo(fltNo2);
					if(0 != c1) return c1;
					return b2cResult1.getTicketPrice().compareTo(b2cResult2.getTicketPrice());
				} else return 0;
			}
		});
		if(!filter) return ;
		
		// 过滤座位数
		String minRemainSiteStr = timerJob.getParamMapValueByKey("minRemainSite");
		int minRemainSite = StringUtils.isEmpty(minRemainSiteStr) ? 1 : Integer.parseInt(minRemainSiteStr.trim());
		
		// 只要最低舱位数量
		String maxCabinAmountStr = timerJob.getParamMapValueByKey("maxCabinAmount");
		int defaultMaxCabinAmount = 1000;
		int maxCabinAmount = StringUtils.isEmpty(maxCabinAmountStr) ? defaultMaxCabinAmount : Integer.parseInt(maxCabinAmountStr.trim());
		Map<String, Integer> cabinAmountMap = new HashMap<String, Integer>();
		
		// 是否共享
		String needShareFlight = timerJob.getParamMapValueByKey("needShareFlight");
		
		Iterator<CrawlResultBase> itResult = crawlResults.iterator();
		while(itResult.hasNext()) {
			CrawlResultBase crawlResult = itResult.next();
			int remainSite = 0;
			String cabin = null;
			String backCabin = null;
			Integer backRemainSite = null;
			String shareFlight = null;
			String backShareFlight = null;
			
			String fltNoStr = null;
			if(crawlResult instanceof CrawlResultB2C) {
				CrawlResultB2C b2cResult = (CrawlResultB2C)crawlResult;
				cabin = b2cResult.getCabin();
				remainSite = b2cResult.getRemainSite();
				shareFlight = b2cResult.getShareFlight();
				
				fltNoStr = b2cResult.getFltNo();
			}else if(crawlResult instanceof FlightData) {
				FlightData flightData = (FlightData)crawlResult;
				if("OW".equals(flightData.getRouteType()) && flightData.getFromSegments().size()==1){
					//单程一段情况,适用国内，国际有需要在另加
					FlightSegment flightSegment = flightData.getFromSegments().get(0);
					cabin = flightSegment.getCabin();
					if(cabin==null)continue;
					remainSite =Integer.valueOf(flightSegment.getCabinCount().trim());
					shareFlight=flightSegment.getCodeShare();
					fltNoStr=flightSegment.getFlightNumber();
				}else break;
			} else if(crawlResult instanceof CrawlInterResult) {
				CrawlInterResult interResult = (CrawlInterResult)crawlResult;
				cabin = interResult.getCabin();
				backCabin = interResult.getBackCabin();
				
				remainSite = interResult.getRemainSite();
				backRemainSite = interResult.getBackRemainSite();
				
				shareFlight = interResult.getShareFlight();
				backShareFlight = interResult.getBackShareFlight();
				
				fltNoStr = interResult.getFltNo() + "-" + interResult.getBackFltNo();
			} else if(crawlResult instanceof CrawlResultB2CRt) {
				CrawlResultB2CRt b2cResultRt = (CrawlResultB2CRt)crawlResult;
				cabin = b2cResultRt.getCabin();
				backCabin = b2cResultRt.getBackCabin();
				
				remainSite = b2cResultRt.getRemainSite();
				backRemainSite = b2cResultRt.getBackRemainSite();
				
				shareFlight = b2cResultRt.getShareFlight();
				backShareFlight = b2cResultRt.getBackShareFlight();
				
				fltNoStr = b2cResultRt.getFltNo() + "-" + b2cResultRt.getBackFltNo();
			} else break ;
			
			// 移除不符合舱位数量的
			if(defaultMaxCabinAmount != maxCabinAmount) {
				Integer amount = cabinAmountMap.get(fltNoStr);
				if(null == amount) amount = 0; 
				if(amount >= maxCabinAmount) {
					itResult.remove();
					continue ;
				}
				cabinAmountMap.put(fltNoStr, amount + 1);
			}
			
			// 移除不符合舱位的
			if(!Crawler.allowCabin(timerJob, cabin) || (StringUtils.isNotEmpty(backCabin) && !Crawler.allowCabin(timerJob, backCabin))) {
				itResult.remove();
				continue ;
			}
			
			// 移除不符合舱位数的
			if(remainSite < minRemainSite || (null != backRemainSite && backRemainSite < minRemainSite)) {
				itResult.remove();
				continue ;
			}
			
			// 移除不符合共享的
			if(!"Y".equalsIgnoreCase(needShareFlight) && ("Y".equalsIgnoreCase(shareFlight) || "Y".equalsIgnoreCase(backShareFlight))) {
				itResult.remove();
				continue ;
			}
		}
	}
	
	/**
	 * 保存...
	 * @param crawlThreadMark
	 * @param jobDetail
	 * @param crawlResults
	 */
	public static void postResult(String crawlThreadMark, JobDetail jobDetail, List<CrawlResultBase> crawlResults) {
		try {
			final String pageType = jobDetail.getPageType();
			final String postData = MyJsonTransformUtil.writeValue(crawlResults);
			CrawlExt crawlExt = CrawlExt.getCrawlExt(pageType);
			if(MyDefaultProp.getPostCheapairOpen()){
				for(CrawlResultBase b2c : crawlResults){
					if(b2c instanceof CrawlResultInter) {
						crawlExt.setSaveRemoteUrl(MyDefaultProp.getPostCheapairUrl());
					}
					break;
				}
			}
			logger.info(jobDetail.toStr() + ", 结果数据:" + postData);
			if(crawlResults.isEmpty()) return ;
			
			
			if(crawlExt.getSaveRemote() && StringUtils.isNotEmpty(crawlExt.getSaveRemoteUrl()))
				ThreadController.addThread(crawlThreadMark, new PostRemoteResultRunnable(jobDetail, ThreadType.postRemoteResultType, crawlExt.getSaveRemoteUrl(), postData));
			
			// ca app 额外处理
			if(MyDefaultProp.getPostCaappOther() && CrawlerType.B2CCAAppPageType.equals(jobDetail.getPageType())) {
				ThreadController.addThread(crawlThreadMark, new PostRemoteResultRunnable(jobDetail, ThreadType.postOtherResultType,
						MyDefaultProp.getPostCaappUrl(), MyJsonTransformUtil.writeValue(ResultPost.toCaAppDatas(crawlResults))));
			}
			
			if((crawlExt.getSaveDc() && StringUtils.isNotEmpty(crawlExt.getSaveDcUrl()))|| "all_realtime".equals(MyDefaultProp.getOperatorName())) {
				//发去MT
				String dcPostData = MyJsonTransformUtil.writeValue(LCCPostDataConversion.toThisMT(crawlResults));
				ThreadController.addThread(crawlThreadMark, new PostJxdMtResultRunnable(crawlThreadMark,jobDetail, ThreadType.postDcResultType, crawlExt.getSaveDcUrl(), dcPostData));
				//发去腾叔
				String tsPostData = MyJsonTransformUtil.writeValue(LCCPostDataConversion.toThisTS(crawlResults));
				ThreadController.addThread(crawlThreadMark, new PostTSLCCResultRunnable(crawlThreadMark,jobDetail, ThreadType.postTsResultType, crawlExt.getSaveTsUrl(), tsPostData));
			}
			if((crawlExt.getSaveTs() && StringUtils.isNotEmpty(crawlExt.getSaveTsUrl()))) {
				String dcPostData = MyJsonTransformUtil.writeValue(LCCPostDataConversion.toThisTS(crawlResults));
				ThreadController.addThread(crawlThreadMark, new PostTSLCCResultRunnable(crawlThreadMark,jobDetail, ThreadType.postTsResultType, crawlExt.getSaveTsUrl(), dcPostData));
			}
			
			if(crawlExt.getMqPost()) {
				String mqType = "nfdfare";
				if(CrawlerType.veryZhunFltNoPageType.equalsIgnoreCase(jobDetail.getPageType())) mqType = "flightChange";
				else if(CrawlerType.sinaWeatherPageType.equalsIgnoreCase(jobDetail.getPageType())) mqType = "weatherData";
				else if(CrawlerType.veryZhunFlightLinePageType.equalsIgnoreCase(jobDetail.getPageType())) mqType = "rtFlight";
				String message = MyJsonTransformUtil.writeValue(new PostMqDcVo(mqType, postData));
				ThreadController.addThread(crawlThreadMark, new PostMqResultRunnable(jobDetail, CrawlCommon.getMqSaveDataTopic(), message));
			}
		} catch(Exception e) {
			logger.error(jobDetail.toStr() + ", 提交保存采集结果异常:\r", e);
		}
	}
	
	/**
	 * 提交请求
	 * @param postUrl
	 * @param postData
	 * 
	 * @return
	 */
	public static String postCrawlResult(String postUrl, String postData) {
		String httpResult = null;
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			if(StringUtils.isNotEmpty(postData)) paramMap.put("result", Base64.encodeBase64String(postData.getBytes("UTF-8")));
			httpResult = MyHttpClientUtil.post(postUrl, paramMap);
		} catch (Exception e) {
			httpResult = e.getMessage();
			logger.error("保存[" + postUrl + "]采集结果[" + postData + "]异常:\r", e);
		}
		return httpResult;
	}

	/**
	 * 保存数据是否成功
	 * @param postResult
	 * @return
	 */
	public static boolean postSuccess(String postResult) {
		if(null == postResult) return false;
		return postResult.contains("成功") || postResult.contains("true");
	}
	
	/**
	 * 转
	 * @param crawlResults
	 * @return
	 */
	public static List<Object> toCaAppDatas(List<CrawlResultBase> crawlResults) {
		List<Object> vos = new ArrayList<Object>();
		if(null == crawlResults || crawlResults.isEmpty()) return vos;
		Date now = new Date();
		for(CrawlResultBase crawlResult : crawlResults) {
			CrawlResultB2C b2c = (CrawlResultB2C)crawlResult;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("pageType", b2c.getPageType());
			map.put("pageTypeMemo", b2c.getPageTypeMemo());
			map.put("airlineCode", b2c.getAirlineCode());
			map.put("depCode", b2c.getDepCode());
			map.put("desCode", b2c.getDesCode());
			map.put("crawlMark", b2c.getCrawlMark());
			map.put("fltNo", b2c.getFltNo());
			map.put("cabin", b2c.getCabin());
			map.put("startDate", b2c.getStartDate());
			map.put("endDate", b2c.getEndDate());
			map.put("ticketPrice", b2c.getTicketPrice());
			map.put("salePrice", b2c.getSalePrice());
			map.put("remainSite", b2c.getRemainSite());
			map.put("updateTimes", now.getTime());
			vos.add(map);
		}
		return vos;
	}
}