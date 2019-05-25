package com.aft.crawl.thread;

public interface ThreadType {

	// 采集主线程
	final static String crawlerMainType = "crawlerMain";
	
	// 采集子线程
	final static String crawlerType = "crawler";
	
	
	// 保存远程结果
	final static String postRemoteResultType = "postRemoteResult";
	
	// 保存mq结果
	final static String postMqResultType = "postMqResult";
	
	// 保存dc接口结果
	final static String postDcResultType = "postDcResult";
	
	// 保存ts接口结果
	final static String postTsResultType = "postTsResult";
	
	// 保存 other 接口结果
	final static String postOtherResultType = "postOtherResult";
	
	// 保存任务状态
	final static String postJobDetailStatusType = "postJobDetailStatus";
	
	
	
	// 一下单线程处理
	
	// 保存新增航线
	final static String singleSaveCrawlInfoType = "singleSaveCrawlInfoType";
	
	// 保存远程结果失败另存文件
	final static String singleBackupPostRemoteFailFileType = "singleBackupPostRemoteFailFileType";
	
	// 保存dc接口结果失败另存文件
	final static String singleBackupPostDcFailFileType = "singleBackupPostDcFailFileType";
	
	// 保存mq结果失败另存文件
	final static String singleBackupPostMqFailFileType = "singleBackupPostMqFailFileType";
	
	// 保存任务状态失败另存文件
	final static String singleBackupPostJobDetailFailFileType = "singleBackupPostJobDetailFailFileType";
}