package com.aft.utils.thread;

public class MyThreadUtils {

	
	/**
	 * 线程睡眠等待
	 * @param time
	 */
	public final static void sleep(long time) {
		try { Thread.sleep(time); } catch (Exception e) { e.printStackTrace(); }
	}
}