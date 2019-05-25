package com.aft.crawl.result.kafka.runnable;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.CrawlCommon;
import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.backup.runnable.BackupPostFailFileRunnable;
import com.aft.crawl.result.kafka.KafkaProducer;
import com.aft.crawl.thread.ThreadController;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.crawl.thread.ThreadType;
import com.aft.utils.MyDefaultProp;

public class PostMqResultRunnable extends ThreadRunnable {

	private final static Logger logger = Logger.getLogger(PostMqResultRunnable.class);
	
	private final String mqTopic;
	
	private final String message;
	
	public PostMqResultRunnable(JobDetail jobDetail, String mqTopic, String message) {
		super(jobDetail, ThreadType.postMqResultType);
		this.mqTopic = mqTopic;
		this.message = message;
	}

	@Override
	public void run() {
		try {
			logger.info(this.toStr() + ", 发送主题[" + CrawlCommon.getMqSaveDataTopic() + "], 消息[" + this.message + "]开始!");
			boolean ok = false;//KafkaProducer.producer(CrawlCommon.getMqBrokerList(), this.mqTopic, this.message);
			logger.info(this.toStr() + ", 发送主题[" + CrawlCommon.getMqSaveDataTopic() + "], 消息完成!");
			if(!ok) {
				logger.warn(this.toStr() + ", 发送主题[" + CrawlCommon.getMqSaveDataTopic() + "], 消息异常...");
				String postUrl = CrawlCommon.getMqBrokerList() + "|" + this.mqTopic;
				if(MyDefaultProp.getBackupOpen()) ThreadController.addSingleThreadPool(new BackupPostFailFileRunnable(this.jobDetail, ThreadType.singleBackupPostMqFailFileType, postUrl, this.message));
			}
		} catch(Exception e) {
			logger.error(this.toStr() + ", 发送主题[" + CrawlCommon.getMqSaveDataTopic() + "], 消息[" + this.message + "]异常:\r", e);
		}
	}
}