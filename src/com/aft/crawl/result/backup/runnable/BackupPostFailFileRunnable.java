package com.aft.crawl.result.backup.runnable;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.crawl.bean.JobDetail;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.crawl.thread.ThreadType;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.file.MyFileUtils;

public class BackupPostFailFileRunnable extends ThreadRunnable {
	
	private final static Logger logger = Logger.getLogger(BackupPostFailFileRunnable.class);
	
	private final String postUrl;
	
	private final String postData;
	
	// 完整的行, 不需要添加时间等等...优先处理
	private final List<String> postDatas;
	
	public BackupPostFailFileRunnable(JobDetail jobDetail, String threadType, String postUrl, String postData) {
		super(jobDetail, threadType);
		this.postUrl = postUrl;
		this.postData = postData;
		this.postDatas = null;
	}
	
	public BackupPostFailFileRunnable(String threadType, List<String> postDatas) {
		super(null, threadType);
		this.postUrl = null;
		this.postData = null;
		this.postDatas = postDatas;
	}

	@Override
	public void run() {
		try {
			String fileName = null;
			if(ThreadType.singleBackupPostDcFailFileType.equals(this.threadType)) fileName = MyDefaultProp.getBackupPostDcFailResultFile();
			else if(ThreadType.singleBackupPostMqFailFileType.equals(this.threadType)) fileName = MyDefaultProp.getBackupPostMqFailResultFile();
			else if(ThreadType.singleBackupPostRemoteFailFileType.equals(this.threadType)) fileName = MyDefaultProp.getBackupPostRemoteFailResultFile();
			else if(ThreadType.singleBackupPostJobDetailFailFileType.equals(this.threadType)) fileName = MyDefaultProp.getBackupUpdateJobDetailResultFile();
			if(StringUtils.isEmpty(fileName)) {
				logger.warn(this.toStr() + ", 记录保存失败内容不存在此类型!");
				return ;
			}
			
			if(null != postDatas) {
				for(String saveData : postDatas) {
					MyFileUtils.createFile(MyDefaultProp.getBackupFilePath(), fileName, saveData, true);
				}
			} else if(null != postData) {
				String saveData = MyDateFormatUtils.SDF_YYYYMMDDHHMMSSSSS().format(new Date()) + "\t" + this.postUrl + "\t" + this.postData;
				MyFileUtils.createFile(MyDefaultProp.getBackupFilePath(), fileName, saveData, true);
			}
		} catch(Exception e) {
			logger.error(this.toStr() + ", 记录保存失败内容异常:" + this.postData + "\r", e);
		}
	}
}