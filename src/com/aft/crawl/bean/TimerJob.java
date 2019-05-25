package com.aft.crawl.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerType;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

public class TimerJob implements Serializable {

	private static final long serialVersionUID = 6341727400633027608L;

	private final static Logger logger = Logger.getLogger(TimerJob.class);
	
	private int jobId;
	
	// 任务名称
	private String jobName;
	
	// 类型
	private String pageType;
	
	// 采集标识(yyyyMMddhhMM)
	private String crawlMark;
	
	// 本地任务采集线程数
	private int ipAmount;
	
	// 线程数(本地配置文件模式下, 单个线程可在并发线程数,共享ip)
	private int oneIpThread;

	// 允许舱位 多个 ; 隔开
	private String[] allowCabins;
	
	// 参数 json 形式
	private Map<String, String> paramMap;
	
	private static CopyOnWriteArrayList<TimerJob> timerJobs = new CopyOnWriteArrayList<TimerJob>();

	private TimerJob(String pageType, int jobId) {
		this.pageType = pageType;
		this.jobId = jobId;
	}
	
	/**
	 * 获取当前任务
	 * @param jobId
	 * @return
	 */
	public static TimerJob getTimerJob(int jobId) {
		for(TimerJob timerJob : timerJobs) {
			if(timerJob.getJobId() != jobId) continue ;
			return timerJob;
		}
		return null;
	}
	
	public static CopyOnWriteArrayList<TimerJob> getTimerJobs() {
		return timerJobs;
	}

	/**
	 * 加载任务列表
	 * @param timerJobUrl
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized static boolean loadTimerJobs() {
		if(MyDefaultProp.getLocalTest()) {
			TimerJob timerJob = TimerJob.getTimerJob(0);
			if(null != timerJob) return true;
			timerJob = new TimerJob(MyDefaultProp.getTestPageType(), 0);
			timerJob.jobName = "测试";
			timerJob.crawlMark = "0000000000000";
			timerJob.ipAmount = 1;
			timerJob.oneIpThread = 1;
			timerJob.allowCabins = null;
			try {
				timerJob.paramMap = MyJsonTransformUtil.readValue("{\"UserName\": \"709871540\", \"Password\": \"yd201515\",\"maxCabinAmount\":\"4\", \"CardNO\":\"0928030000002523\",\"needShareFlight\":\"Y\"}", Map.class);
			} catch (Exception e) {
				e.printStackTrace();
			};
			timerJobs.add(timerJob);
			return true;
		}
		
		try {
			logger.info(MyDefaultProp.getOperatorName() + ", 获取任务列表信息[" + CrawlCommon.getTimeJobUrl() + "]开始...");
			String result = MyHttpClientUtil.get(CrawlCommon.getTimeJobUrl() + "?operator=" + MyDefaultProp.getOperatorName());
			logger.info(MyDefaultProp.getOperatorName() + ", 获取任务列表信息[" + CrawlCommon.getTimeJobUrl() + "]返回:" + result);
			if("null".equalsIgnoreCase(result) || StringUtils.isEmpty(result)) {
				logger.info(MyDefaultProp.getOperatorName() + ", 返回任务列表信息为空...");
				return false;
			}
			List<Map> results = MyJsonTransformUtil.readValueToList(result, Map.class);
			if(results.isEmpty()) {
				logger.info(MyDefaultProp.getOperatorName() + ", 不存在任务列表信息, 清空所有任务列表信息...");
				timerJobs.clear();
				return true;
			}
			
			// 移除不存在的...
			loop: for(TimerJob timerJob : timerJobs) {
				for(Map<String, Object> resultMap : results) {
					if(timerJob.getJobId() == Integer.parseInt(resultMap.get("id").toString().trim())) continue loop;
				}
				timerJobs.remove(timerJob);
			}
			
			// 修改或新增...
			TimerJob timerJob = null;
			for(Map<String, Object> resultMap : results) {
				String pageType = resultMap.get("pageType").toString().trim();
				CrawlerType crawlerType = CrawlerType.getCrawlerType(pageType);
				if(null == crawlerType) {	// 不支持
					logger.warn(MyDefaultProp.getOperatorName() + ", 采集类型[" + pageType + "]不支持...");
					CrawlerWin.logger(MyDefaultProp.getOperatorName() + ", 采集类型[" + pageType + "]不支持!");
					continue ;
				}
				
				int id = Integer.parseInt(resultMap.get("id").toString().trim());
				timerJob = TimerJob.getTimerJob(id);
				if(null == timerJob) {	// 新增的
					timerJob = new TimerJob(pageType, id);
					timerJobs.add(timerJob);
				}				
				timerJob.jobName = (String)resultMap.get("remark");
				timerJob.crawlMark = resultMap.get("lastCrawlMark").toString().trim();
				timerJob.ipAmount = Integer.parseInt(resultMap.get("ipAmount").toString().trim());
				timerJob.oneIpThread = Integer.parseInt(resultMap.get("oneIpThread").toString().trim());
				
				String allowCabin = (String)resultMap.get("allowCabin");
				if(StringUtils.isNotEmpty(allowCabin)) timerJob.allowCabins = allowCabin.split(";");
				
				String param = (String)resultMap.get("param");
				if(StringUtils.isNotEmpty(param)) timerJob.paramMap = MyJsonTransformUtil.readValue(param, Map.class);
			}
			return true;
		} catch(Exception e) {
			logger.error(MyDefaultProp.getOperatorName() + ", 获取任务列表信息[" + CrawlCommon.getTimeJobUrl() + "]异常:\r", e);
		}
		return false;
	}
	
	/**
	 * 获取 key
	 * @param pageType
	 * @param jobId
	 * @return
	 */
	public static String getTimerJobKey(String pageType, Integer jobId) {
		return pageType + (null == jobId ? "" : "-" + jobId);
	}
	
	/**
	 * 获取任务唯一 key
	 * @return
	 */
	public String getTimerJobKey() {
		return TimerJob.getTimerJobKey(this.pageType, this.jobId);
	}

	public int getJobId() {
		return jobId;
	}
	
	public String getJobName() {
		return jobName;
	}

	public String getPageType() {
		return pageType;
	}

	public String getCrawlMark() {
		return crawlMark;
	}

	public int getIpAmount() {
		return ipAmount;
	}

	public int getOneIpThread() {
		return oneIpThread;
	}

	public String[] getAllowCabins() {
		return allowCabins;
	}

	public Map<String, String> getParamMap() {
		return paramMap;
	}
	
	/**
	 * 获取 paramMap 中的key
	 * @param key
	 * @return
	 */
	public String getParamMapValueByKey(String key) {
		return null == paramMap ? null : paramMap.get(key);
	}
}