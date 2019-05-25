package com.aft.logger;

import org.apache.log4j.RollingFileAppender;

public class MyCrawlerRollingFileAppender extends RollingFileAppender {
	
	private String savePath;
	
	@Override
	public void closeFile() {
		super.closeFile();
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
}