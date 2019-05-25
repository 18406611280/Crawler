package com.aft.crawl.result.post.runnable;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.thread.ThreadController;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.utils.http.MyHttpClientUtil;

public class PostTSLCCResultRunnable extends ThreadRunnable {

	private final static Logger logger = Logger.getLogger(PostTSLCCResultRunnable.class);
	
	// 提交地址
	private final String postUrl;
	
	// 提交数据
	private final String postData;
	
	private final String crawlThreadMark;
	
	/**
	 * 
	 * @param jobDetail
	 * @param threadType
	 * @param postUrl
	 * @param postData
	 */
	public PostTSLCCResultRunnable(String crawlThreadMark,JobDetail jobDetail, String threadType, String postUrl, String postData) {
		super(jobDetail, threadType);
		this.postUrl = postUrl;
		this.postData = postData;
		this.crawlThreadMark = crawlThreadMark;
	}

	@Override
	public void run() {
		String httpResult = null;
		try {
			logger.info(this.toStr() + ",发送TS保存信息: " + postData);
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("ACTION", "UpResult");
			paramMap.put("data", postData);
			httpResult = MyHttpClientUtil.post(postUrl, paramMap, "UTF-8");
			logger.info(this.toStr() + ", 发送TS保存返回: " + httpResult);
			//失败重发
			if(!httpResult.contains("success")){
				int count = 0;
				while(count<5){
					count++;
					httpResult = MyHttpClientUtil.post(postUrl, paramMap, "UTF-8");
					logger.info(this.toStr() + ", 发送TS失败重发,保存返回: " + httpResult);
					if(httpResult.contains("success"))break;
				}
			}
		} catch (Exception e) {
			if(e.toString().contains("Connection timed out")){
				logger.error("保存[" + postUrl + "]采集结果[" + postData + "]发送TS保存超时，重新发送!");
				ThreadController.addThread(crawlThreadMark,new PostTSLCCResultRunnable(crawlThreadMark,jobDetail,threadType, postUrl, postData));
			}else{
				httpResult = e.getMessage();
				logger.error("保存[" + postUrl + "]采集结果[" + postData + "]异常:\r", e);
			}
		}
	}
}