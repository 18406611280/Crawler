package com.aft.crawl.crawler;

import java.util.concurrent.atomic.AtomicInteger;


public class AtomicLook {
	
	private final static AtomicInteger atmoic = new AtomicInteger(0);
	private static Thread currentThread;
	
	public static void tryLook() throws Exception {
		boolean succ = atmoic.compareAndSet(0, 1);
		if(!succ) {
			throw new Exception("Get the look failed");
		}else {
			currentThread = Thread.currentThread();
		}
	}
	
	public static void unLook() {
		if(atmoic.get() ==0) return;
		if(currentThread==Thread.currentThread()) {
			atmoic.compareAndSet(1,0);
		}
	}

}
