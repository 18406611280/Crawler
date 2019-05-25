package com.aft.crawl.result.jobDetail.runnable;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.CrawlCommon;
import com.aft.crawl.bean.CrawlExt;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.kafka.runnable.PostMqResultRunnable;
import com.aft.crawl.result.post.runnable.PostRemoteResultRunnable;
import com.aft.crawl.result.vo.jobDetail.JobDetailResultVo;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.crawl.thread.ThreadType;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.jackson.MyJsonTransformUtil;

public class UpdateJobDetailRunnable extends ThreadRunnable {
	
	private final static Logger logger = Logger.getLogger(UpdateJobDetailRunnable.class);
	
	private final JobDetailResultVo jobDetailResultVo;
	
	public UpdateJobDetailRunnable(JobDetail jobDetail, String threadType, JobDetailResultVo jobDetailResultVo) {
		super(jobDetail, threadType);
		this.jobDetailResultVo = jobDetailResultVo;
	}
	
	@Override
	public void run() {
		try {
			CrawlExt crawlExt = CrawlExt.getCrawlExt(this.jobDetail.getPageType());
			if(2 == crawlExt.getUpdateJobType()) {	// post方式
				String postData = "pageType=" + this.jobDetail.getPageType()
									+ "&timerJobId=" + this.jobDetail.getTimerJob().getJobId()
									+ "&jobDetailId=" + this.jobDetail.getJobDetailId()
									+ "&status=" + jobDetailResultVo.getStatus()
									+ "&operator=" + MyDefaultProp.getOperatorName()
									+ "&resultAmount=" + jobDetailResultVo.getResultAmount()
									+ "&crawlTime=" + jobDetailResultVo.getCrawlTime();
				String postUrl = CrawlCommon.getJobDetailResultUrl() + "?" + postData;
				new PostRemoteResultRunnable(this.jobDetail, ThreadType.postJobDetailStatusType, postUrl, postData).run();;
			} else {	// mq方式
				String message = MyJsonTransformUtil.writeValue(jobDetailResultVo);
				new PostMqResultRunnable(this.jobDetail, CrawlCommon.getMqUpdateJobDetailTopic(), message).run();
			}
		} catch(Exception e) {
			logger.error(this.toStr() + ", 更新任务[" + this.jobDetail.getJobDetailId() + "]状态异常:\r", e);
		}
	}
}