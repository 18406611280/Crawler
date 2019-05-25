package com.aft.logger;

import java.io.File;
import java.util.Date;
import java.util.Enumeration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.aft.crawl.CrawlerType;
import com.aft.crawl.SwitchTypeEnum;
import com.aft.crawl.bean.TimerJob;
import com.aft.swing.CrawlerWin;
import com.aft.utils.MyStringUtil;
import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.thread.MyThreadUtils;

public class MyCrawlerLogger {
	
	private final static String crawlerLogger = "com.aft.crawl.crawler";
	
	private final static String crawlerAppenderName = "crawler";
	
	private final static String saveFileLogger = "com.aft.saveFile";
	
	private final static String saveFileAppenderName = "saveFile";
	
	/**
	 * 获取logger
	 * @param timerJob
	 * @return
	 */
	public static Logger getCrawlerLogger(TimerJob timerJob) {
		String timerJobKey = "pageType";
		String pageType = "timerJobKey";
		if(null != timerJob) {
			pageType = timerJob.getPageType();
			timerJobKey = timerJob.getTimerJobKey();
		}
		
		String loggerName = "logger_crawler_" + timerJobKey;
		Logger logger = LogManager.exists(loggerName);
		if(null != logger) return logger;
		
		synchronized(crawlerLogger) {
			logger = LogManager.exists(loggerName);
			if(null != logger) return logger;
			
			logger = Logger.getLogger(loggerName);
			logger.setAdditivity(false);
			
			Logger defLog = LogManager.exists(crawlerLogger);
			MyCrawlerRollingFileAppender defAppender = (MyCrawlerRollingFileAppender)defLog.getAppender(crawlerAppenderName);
			
			String appenderName = "appender_crawler_" + timerJobKey;
			String appenderFileName = "appender_crawler";
			Class<?> crawlerClass = CrawlerType.getCrawlerClass(pageType);
			if(null != crawlerClass) appenderFileName = crawlerClass.getSimpleName() + "-" + timerJobKey + ".log";
			logger.addAppender(MyCrawlerLogger.copyAppender(appenderName, appenderFileName, defAppender));
			logger.addAppender(defLog.getAppender("exception"));
			if(null != defLog.getAppender("console")) logger.addAppender(defLog.getAppender("console"));
			return logger;
		}
	}
	
	/**
	 * 获取logger
	 * @param timerJob
	 * @return
	 */
	public static Logger getSaveFileLogger(TimerJob timerJob) {
		return MyCrawlerLogger.getSaveFileLogger(timerJob, null);
	}
	
	/**
	 * 获取logger
	 * @param timerJob
	 * @param pageType 不为空 替换 timerJob的 pageType
	 * @return
	 */
	public static Logger getSaveFileLogger(TimerJob timerJob, String pageType) {
		if(null == pageType) pageType = timerJob.getPageType();
		
		String loggerName = "logger_saveFile_" + timerJob.getTimerJobKey().replaceAll(timerJob.getPageType(), pageType);
		String appenderName = "appender_saveFile_" + timerJob.getTimerJobKey().replaceAll(timerJob.getPageType(), pageType);
		
		Logger logger = LogManager.exists(loggerName);
		if(null != logger && logger.getAllAppenders().hasMoreElements()) return logger;
		
		synchronized(saveFileLogger) {
			logger = LogManager.exists(loggerName);
			if(null != logger && logger.getAllAppenders().hasMoreElements()) return logger;
			
			if(null == logger) {
				logger = Logger.getLogger(loggerName);
				logger.setAdditivity(false);
			}
			
			Logger defLog = LogManager.exists(saveFileLogger);
			MyCrawlerRollingFileAppender defAppender = (MyCrawlerRollingFileAppender)defLog.getAppender(saveFileAppenderName);
			
			SwitchTypeEnum switchTypeEnum = SwitchTypeEnum.getSwitchTypeEnum(pageType);
			String appenderFileName = TimerJob.getTimerJobKey(switchTypeEnum.name(), timerJob.getJobId()) + "-" + timerJob.getCrawlMark() + ".csv";
			logger.addAppender(MyCrawlerLogger.copyAppender(appenderName, appenderFileName, defAppender));
			return logger;
		}
	}
	
	/**
	 * 复制appender
	 * @param appenderName
	 * @param appenderFileName
	 * @param srcAppender
	 * @return
	 */
	private static MyCrawlerRollingFileAppender copyAppender(String appenderName, String appenderFileName, MyCrawlerRollingFileAppender srcAppender) {
		MyCrawlerRollingFileAppender newAppender = new MyCrawlerRollingFileAppender();
		newAppender.setName(appenderName);
		newAppender.setAppend(srcAppender.getAppend());
		newAppender.setMaxBackupIndex(srcAppender.getMaxBackupIndex());
		newAppender.setMaximumFileSize(srcAppender.getMaximumFileSize());
		newAppender.setLayout(srcAppender.getLayout());
		newAppender.setSavePath(srcAppender.getSavePath());
		
		// 配置里带了yyyy-MM-dd用来替换当天日期
		String savePath = srcAppender.getSavePath();
		if(savePath.contains("yyyy-MM-dd")) savePath = savePath.replaceAll("yyyy-MM-dd", MyDateFormatUtils.SDF_YYYYMMDD().format(new Date()));
		
		newAppender.setFile(MyStringUtil.getFilePath(savePath, appenderFileName));
		newAppender.activateOptions();
		return newAppender;
	}
	
	/**
	 * 获取保存文件路径(日期的上一个目录)
	 * 
	 * @return
	 */
	public static File getSaveFilePath() {
		Logger defLog = Logger.getLogger(saveFileLogger);
		MyCrawlerRollingFileAppender appender = (MyCrawlerRollingFileAppender)defLog.getAppender(saveFileAppenderName);
		return new File(appender.getSavePath().replaceAll("/yyyy-MM-dd", ""));
	}
	
	/**
	 * 关闭文件输出
	 * 删除logger-->appender
	 * @param timerJob
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void closeSaveFile(TimerJob timerJob) {
		if(CrawlerWin.isClosing()) return ;
		
		String loggerName = "logger_saveFile_" + timerJob.getTimerJobKey();
		final Logger logger = LogManager.exists(loggerName);
		if(null == logger) return ;
		
		MyThreadUtils.sleep(60 * 1000);	// 延迟一分钟...
		Enumeration<MyCrawlerRollingFileAppender> enumeration = logger.getAllAppenders();
		while(enumeration.hasMoreElements()) {
			enumeration.nextElement().closeFile();
		}
		logger.removeAllAppenders();
	}
}