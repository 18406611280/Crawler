package com.aft.crawl.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.utils.MyDefaultProp;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 任务 bean
 */
public class JobDetail implements Serializable {

	private final static Logger logger = Logger.getLogger(JobDetail.class);
	
	private static final long serialVersionUID = 3210884796154003758L;
	
	/** 所属任务 */
	private TimerJob timerJob;

	private Integer jobDetailId;
	
	/**  航司 */
	private String airlineCode;
	
	/** 航班号 */
	private String fltNo;
	
	/** 机场 */
	private String airport;
	
	/** 出发城市Cd */
	private String depCode;

	/** 到达城市Cd */
	private String desCode;
	
	/** 出发城市 */
	private String depName;
	
	/** 到达城市 */
	private String desName;
	
	/** 出发日期(yyyy-MM-dd) */
	private String depDate;
	
	/** 返程日期(yyyy-MM-dd) */
	private String backDate;
	
	/**币种*/
	private String currency;
	
	private JobDetail(TimerJob timerJob) {
		this.timerJob = timerJob;
	}
	
	/**
	 * 获取 mq 任务
	 * @param timerJob
	 * @return
	 * @throws Exception
	 */
	public static List<JobDetail> mqJobs(TimerJob timerJob) {
		List<JobDetail> jobDetails = new ArrayList<JobDetail>();
//		CrawlExt crawlExt = CrawlExt.getCrawlExt(pageType);
		
		return jobDetails;
	}
	
	
	/**
	 * 本地测试用
	 * @return
	 */
	private static List<JobDetail> testJd(TimerJob timerJob) {
		if(!timerJob.getPageType().equals(MyDefaultProp.getTestPageType())) return null;
		
		List<JobDetail> jobDetails = new ArrayList<JobDetail>();
		Calendar calendar = Calendar.getInstance();
		Calendar bcalendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 5);
		bcalendar.add(Calendar.DATE, 7);
		for(int i=0; i<30; i++) {
			JobDetail jobDetail = new JobDetail(timerJob);
			jobDetail.jobDetailId = i;
			jobDetail.depCode = "TPE";
			jobDetail.desCode = "LJG";
//			jobDetail.depName = "重庆";
//			jobDetail.desName = "西安";
//			jobDetail.fltNo = "MU223";
//			jobDetail.airlineCode = "MU";
//			jobDetail.depDate = "2018-08-03";
//			jobDetail.currency = "SGD";
			jobDetail.depDate = MyDateFormatUtils.SDF_YYYYMMDD().format(calendar.getTime());
//			jobDetail.backDate = MyDateFormatUtils.SDF_YYYYMMDD().format(bcalendar.getTime());
			bcalendar.add(Calendar.DATE, 1);
			calendar.add(Calendar.DATE, 1);
			jobDetails.add(jobDetail);
		}
		return jobDetails;
	}
	
	/**
	 * 获取 remote 远程任务
	 * @param timerJob
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<JobDetail> remoteJobs(TimerJob timerJob) {
		if(MyDefaultProp.getLocalTest()) return JobDetail.testJd(timerJob);
		
		logger.info(timerJob.getTimerJobKey() + ", 获取 remote 远程任务开始...");
		List<JobDetail> jobDetails = new ArrayList<JobDetail>();
		try {
			int amount = timerJob.getIpAmount() * timerJob.getOneIpThread() * 2;
			String result = MyHttpClientUtil.get(CrawlCommon.getJobDetailUrl()
					 + "?pageType=" + timerJob.getPageType() + "&timerJobId=" + timerJob.getJobId()
					  + "&crawlMark=" + timerJob.getCrawlMark() + "&amount=" + amount);
			logger.info(timerJob.getTimerJobKey() + ", 获取 remote [" + CrawlCommon.getJobDetailUrl() + "]远程任务返回:" + result);
			List<Map> results = MyJsonTransformUtil.readValueToList(result, Map.class);
			JobDetail jobDetail = null;
			for(Map<String, Object> resultMap : results) {
				jobDetail = new JobDetail(timerJob);
				Object jobDetailId = resultMap.get("id");
				if(null != jobDetailId) jobDetail.jobDetailId = Integer.parseInt(jobDetailId.toString().trim());
				
				Object airlineCode = resultMap.get("airlineCode");
				if(null != airlineCode) jobDetail.airlineCode = airlineCode.toString().trim().toUpperCase();
				
				Object fltNo = resultMap.get("fltNo");
				if(null != fltNo) jobDetail.fltNo = fltNo.toString().trim().toUpperCase();
				
				Object airport = resultMap.get("airport");
				if(null != airport) jobDetail.airport = airport.toString().trim().toUpperCase();
				
				Object depCode = resultMap.get("depCode");
				if(null != depCode) jobDetail.depCode = depCode.toString().trim().toUpperCase();
				
				Object desCode = resultMap.get("desCode");
				if(null != desCode) jobDetail.desCode = desCode.toString().trim().toUpperCase();
				
				Object depName = resultMap.get("depName");
				if(null != depName) jobDetail.depName = depName.toString().trim();
				
				Object desName = resultMap.get("desName");
				if(null != desName) jobDetail.desName = desName.toString().trim();
				
				Object depDate = resultMap.get("depDate");
				if(null != depDate) jobDetail.depDate = depDate.toString().trim();
				
				Object backDate = resultMap.get("backDate");
				if(null != backDate) jobDetail.backDate = backDate.toString().trim();
				
				Object currency = resultMap.get("currency");
				if(null != currency) jobDetail.currency = currency.toString().trim();
				
				jobDetails.add(jobDetail);
			}
		} catch(Exception e) {
			logger.error(timerJob.getTimerJobKey() + ", 获取 remote [" + CrawlCommon.getJobDetailUrl() + "]远程任务异常:\r", e);
		}
		return jobDetails;
	}
	
	/**
	 * 是否还有其他任务
	 * @param pageType
	 * @param jobId
	 * @param crawlMark
	 * @return
	 * @throws Exception
	 */
	public static boolean hasMoreJobDetail(String pageType, int jobId, String crawlMark) throws Exception {
		String result = MyHttpClientUtil.get(CrawlCommon.getJobDetailExistsUrl() + "?pageType=" + pageType + "&timerJobId=" + jobId + "&crawlMark=" + crawlMark);
		logger.info(pageType + "-" + jobId + ", 获取是否还有其他任务返回:" + result);
		return StringUtils.isEmpty(result) ? true : "\"true\"".equals(result);
	}
	
	public String getPageType() {
		return this.timerJob.getPageType();
	}
	
	public String getCrawlMark() {
		return this.timerJob.getCrawlMark();
	}
	
	public TimerJob getTimerJob() {
		return timerJob;
	}

	public Integer getJobDetailId() {
		return jobDetailId;
	}

	public String getAirlineCode() {
		return airlineCode;
	}

	public String getFltNo() {
		return fltNo;
	}
	
	public String getAirport() {
		return airport;
	}

	public String getDepCode() {
		return depCode;
	}

	public String getDesCode() {
		return desCode;
	}

	public String getDepName() {
		return depName;
	}

	public String getDesName() {
		return desName;
	}

	public String getDepDate() {
		return depDate;
	}
	
	public String getBackDate() {
		return backDate;
	}

	public String getCurrency() {
		return currency;
	}

	public String toStr() {
		return this.timerJob.getPageType() + "|" + this.timerJob.getCrawlMark()
				+ (null == this.jobDetailId ? "" : "|" + this.jobDetailId)
				+ (StringUtils.isEmpty(this.fltNo) ? "" : "|" + this.fltNo)
				+ (StringUtils.isEmpty(this.airport) ? "" : "|" + this.airport)
				+ (StringUtils.isEmpty(this.depCode) ? "" : "|" + this.depCode)
				+ (StringUtils.isEmpty(this.desCode) ? "" : "|" + this.desCode)
				+ (StringUtils.isEmpty(this.depDate) ? "" : "|" + this.depDate)
				+ (StringUtils.isEmpty(this.backDate) ? "" : "|" + this.backDate)
				+ (StringUtils.isEmpty(this.currency) ? "" : "|" + this.currency);
	}
}