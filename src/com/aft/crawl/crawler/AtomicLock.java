package com.aft.crawl.crawler;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * 显示锁
 */
public class AtomicLock {
	
	private final static AtomicInteger atmoic = new AtomicInteger(0);
	private static String time;
	
	public static void tryLock(String timeStamp) throws Exception {
		boolean succ = atmoic.compareAndSet(0, 1);
		if(!succ) {
			throw new Exception("Get the look failed");
		}else {
			time = timeStamp;
		}
	}
	
	public static void unLock(String timeStamp) {
		if(atmoic.get() ==0) return;
		if(time.equals(timeStamp)) {
			atmoic.compareAndSet(1,0);
		}
	}

}
