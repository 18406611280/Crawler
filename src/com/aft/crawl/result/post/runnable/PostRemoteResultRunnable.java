package com.aft.crawl.result.post.runnable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.result.ResultPost;
import com.aft.crawl.result.backup.runnable.BackupPostFailFileRunnable;
import com.aft.crawl.thread.ThreadController;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.crawl.thread.ThreadType;
import com.aft.utils.MyDefaultProp;

public class PostRemoteResultRunnable extends ThreadRunnable {

	private final static Logger logger = Logger.getLogger(PostRemoteResultRunnable.class);
	
	// 提交地址
	private final String postUrl;
	
	// 提交数据
	private final String postData;
	
	/**
	 * 
	 * @param jobDetail
	 * @param threadType
	 * @param postUrl
	 * @param postData
	 */
	public PostRemoteResultRunnable(JobDetail jobDetail, String threadType, String postUrl, String postData) {
		super(jobDetail, threadType);
		this.postUrl = postUrl;
		this.postData = postData;
	}

	@Override
	public void run() {
		String httpResult = ResultPost.postCrawlResult(this.postUrl, this.postData);
		logger.info(this.toStr() + ", 保存返回: " + httpResult);
		if(ResultPost.postSuccess(httpResult)) return ;
		
		String threadType = null;
		if(ThreadType.postDcResultType.equals(this.threadType)) threadType = ThreadType.singleBackupPostDcFailFileType;
		else if(ThreadType.postDcResultType.equals(this.threadType)) threadType = ThreadType.singleBackupPostDcFailFileType;
		else if(ThreadType.postRemoteResultType.equals(this.threadType)) threadType = ThreadType.singleBackupPostRemoteFailFileType;
		else if(ThreadType.postJobDetailStatusType.equals(this.threadType)) threadType = ThreadType.singleBackupPostJobDetailFailFileType;
		if(StringUtils.isEmpty(threadType)) return ;
		
		if(MyDefaultProp.getBackupOpen()) ThreadController.addSingleThreadPool(new BackupPostFailFileRunnable(this.jobDetail, threadType, this.postUrl, this.postData));
	}
}