package com.aft.crawl.thread;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPoolController {

	private final static ScheduledThreadPoolExecutor schedulePool = new ScheduledThreadPoolExecutor(5);
	
	public static void schedule(Runnable runnable, long period, TimeUnit unit) {
		if(schedulePool.getTaskCount() == schedulePool.getCorePoolSize()) schedulePool.setCorePoolSize(schedulePool.getCorePoolSize() + 1);
		schedulePool.scheduleAtFixedRate(runnable, 0, period, unit);
	}
	
	public static void shutdown() {
		schedulePool.shutdown();
	}
}