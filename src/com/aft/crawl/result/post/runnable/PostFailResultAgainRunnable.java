package com.aft.crawl.result.post.runnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.crawl.result.ResultPost;
import com.aft.crawl.result.backup.runnable.BackupPostFailFileRunnable;
import com.aft.crawl.result.kafka.KafkaProducer;
import com.aft.crawl.thread.ThreadController;
import com.aft.crawl.thread.ThreadRunnable;
import com.aft.crawl.thread.ThreadType;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.date.MyDateFormatUtils;

public class PostFailResultAgainRunnable extends ThreadRunnable {
	
	private final static Logger logger = Logger.getLogger(PostFailResultAgainRunnable.class);
	
	public PostFailResultAgainRunnable(String threadType) {
		super(null, threadType);
	}

	@Override
	public void run() {
		File file = null;
		if(ThreadType.singleBackupPostDcFailFileType.equals(this.threadType))
			file = new File(MyDefaultProp.getBackupFilePath(), MyDefaultProp.getBackupPostDcFailResultFile());
		else if(ThreadType.singleBackupPostMqFailFileType.equals(this.threadType))
			file = new File(MyDefaultProp.getBackupFilePath(), MyDefaultProp.getBackupPostMqFailResultFile());
		else if(ThreadType.singleBackupPostRemoteFailFileType.equals(this.threadType))
			file = new File(MyDefaultProp.getBackupFilePath(), MyDefaultProp.getBackupPostRemoteFailResultFile());
		else if(ThreadType.singleBackupPostJobDetailFailFileType.equals(this.threadType))
			file = new File(MyDefaultProp.getBackupFilePath(), MyDefaultProp.getBackupUpdateJobDetailResultFile());
		if(!file.exists() || 0 == file.length()) return ;
		
		File tempFile = new File(file.getPath() + ".bak");
		if(!tempFile.exists()) return ;
		List<String> postList = this.getFileContents(tempFile);
		List<String> postFails = new ArrayList<String>();
		try {
			file.renameTo(tempFile);	// 先改名
			if(postList.isEmpty()) return ;
			logger.info(this.toStr() + ", 保存失败内容再次提交, 数量:" + postList.size());
			
			Iterator<String> itPostList = postList.iterator();
			int successAmount = 0;
			while(itPostList.hasNext()) {
				String line = itPostList.next();
				String[] lines = line.split("\t");
				
				String result = null;
				if(ThreadType.singleBackupPostMqFailFileType.equals(this.threadType)) {
					String[] mqInfos = lines[1].split("\\|");
					result = String.valueOf(KafkaProducer.producer(mqInfos[0], mqInfos[1], lines[2]));
				} else {
					result = ResultPost.postCrawlResult(lines[1], lines.length >= 3 ? lines[2] : null);
				}
				logger.info(this.toStr() + ", 保存失败内容再次提交[" + lines[0] + "], 返回:" + result);
				if(!ResultPost.postSuccess(result)) {
					postFails.add(line);
					continue ;
				}
				++successAmount;
				itPostList.remove();
			}
			logger.info(this.toStr() + ", 保存失败内容再次提交, 成功数量:" + successAmount);
		} catch(Exception e) {
			logger.error(this.toStr() + ", 保存失败内容再次提交异常\r", e);
			if(MyDefaultProp.getBackupOpen()) ThreadController.addSingleThreadPool(new BackupPostFailFileRunnable(this.threadType, postList));	// 异常, 剩余的添加回去
		} finally {
			tempFile.delete();	// 删除备份的
			ThreadController.addSingleThreadPool(new BackupPostFailFileRunnable(this.threadType, postFails));
			logger.info(this.toStr() + ", 保存失败内容再次提交, 处理完成!");
		}
	}
	
	/**
	 * 获取文件内容
	 * @param file
	 * @return
	 */
	private List<String> getFileContents(File file) {
		List<String> postList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR_OF_DAY, -1);
			String dateTime = MyDateFormatUtils.SDF_YYYYMMDDHHMMSSSSS().format(calendar.getTime());

			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			while(null != (line = br.readLine())) {
				if(StringUtils.isEmpty(line)) continue ;			// 空行忽略
				String[] lines = line.split("\t");
				if(dateTime.compareTo(lines[0]) >= 0) continue ;	// 一小时前的忽略
				postList.add(line);
			}
			br.close();
		} catch(Exception e) {
			logger.error(this.toStr() + ",  获取文件内容异常\r", e);
		} finally {
			if(null != br) br = null;
		}
		return postList;
	}
}