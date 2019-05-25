package com.aft.crawl.thread;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aft.crawl.proxy.ProxyUtil;

final class ThreadVo {
	
	// 所属任务id
	private final int jobId;
	
	// 线程
	private final CopyOnWriteArrayList<ChildrenThreadVo> childrenThreadVos = new CopyOnWriteArrayList<ChildrenThreadVo>();
	
	/**
	 * 
	 * @param jobId
	 * @param pageType
	 * @param ipAmount
	 * @param oneIpThread 采集线程数
	 * @param remotePost 远程保存
	 * @param mqPost mq保存
	 * @param dcPost 数据中心接口保存
	 * @param updateJobPost 任务状态保存
	 */
	ThreadVo(int jobId, int ipAmount, int oneIpThread, boolean remotePost, boolean mqPost, boolean dcPost,boolean tsPost, boolean updateJobPost) {
		this.jobId = jobId;
		for(int i=0; i<ipAmount; i++) {
			this.childrenThreadVos.add(this.new ChildrenThreadVo(oneIpThread, remotePost, mqPost, dcPost,tsPost, updateJobPost));
		}
	}
	
	/**
	 * 重新加载线程池数量
	 * @param ipAmount
	 * @param oneIpThread 采集线程数
	 * @param remotePost 远程保存
	 * @param mqPost mq保存
	 * @param dcPost 数据中心接口保存
	 * @param updateJobPost 任务状态保存
	 */
	void reloadChildrenThreadVos(int ipAmount, int oneIpThread, boolean remotePost, boolean mqPost, boolean dcPost,boolean tsPost, boolean updateJobPost) {
		int size = this.childrenThreadVos.size();
		if(ipAmount > size) {			// 新增几个
			for(int i=size; i<ipAmount; i++) {
				this.childrenThreadVos.add(this.new ChildrenThreadVo(oneIpThread, remotePost, mqPost, dcPost,tsPost, updateJobPost));
			}
		} else if(ipAmount < size) {	// 移除多余的
			for(int i=ipAmount-1; i<size-1; i++) {
				ChildrenThreadVo childrenThreadVo = this.childrenThreadVos.get(i);
				ProxyUtil.updateProxyUsed(childrenThreadVo.threadMark);
				childrenThreadVo.shutdownThreadPoolMap();
				this.childrenThreadVos.remove(i);
			}
		}
		
		// 更新已有的...
		for(ChildrenThreadVo childrenThreadVo : this.childrenThreadVos) {
			childrenThreadVo.setThreadPoolMap(oneIpThread, remotePost, mqPost, dcPost,tsPost, updateJobPost);
		}
	}
	
	/**
	 * 释放线程
	 */
	synchronized void shutdownThreadPool() {
		for(ChildrenThreadVo childrenThreadVo : this.childrenThreadVos) {
			childrenThreadVo.shutdownThreadPoolMap();
		}
		this.childrenThreadVos.clear();
	}

	/**
	 * 获取可用 ThreadPoolExecutor
	 * @param threadMark
	 * @param threadType
	 * @return
	 */
	ThreadPoolExecutor getThreadPool(String threadMark, String threadType) {
		ChildrenThreadVo childrenThreadVo = this.getChildrenThreadVo(threadMark, threadType);
		return null == childrenThreadVo ? null : childrenThreadVo.threadPoolMap.get(threadType);
	}
	
	/**
	 * 获取可用 threadMark
	 * @param threadType
	 * @return
	 */
	String getThreadMain(String threadType) {
		ChildrenThreadVo childrenThreadVo = this.getChildrenThreadVo(null, threadType);
		return null == childrenThreadVo ? null : childrenThreadVo.threadMark;
	}
	
	/**
	 * 所有线程池是否都全部完成
	 * @return
	 */
	boolean threadAllCompleted() {
		for(ChildrenThreadVo childrenThreadVo : this.childrenThreadVos) {
			Iterator<Map.Entry<String, ThreadPoolExecutor>> itMap = childrenThreadVo.threadPoolMap.entrySet().iterator();
			while(itMap.hasNext()) {
				ThreadPoolExecutor threadPool = itMap.next().getValue();
				if(0 != threadPool.getActiveCount() || 0 != threadPool.getQueue().size()) return false;
			}
		}
		return true;
	}
	
	/**
	 * 获取 [主线程, 采集线程]数量
	 * @return
	 */
	int[] getCrawlThreadAmount() {
		return new int[]{this.childrenThreadVos.size(), this.childrenThreadVos.get(0).threadPoolMap.get(ThreadType.crawlerType).getCorePoolSize()};
	}
		
	/**
	 * 获取可用 ChildrenThreadVo
	 * @param threadMark
	 * @param threadType
	 * @return
	 */
	private ChildrenThreadVo getChildrenThreadVo(String threadMark, String threadType) {
		for(ChildrenThreadVo childrenThreadVo : this.childrenThreadVos) {
			if(null != threadMark && !childrenThreadVo.threadMark.equals(threadMark)) continue ;
			ThreadPoolExecutor threadPool = childrenThreadVo.threadPoolMap.get(threadType);
			if(null == threadPool || threadPool.isShutdown()) continue ;
			if(!ThreadType.crawlerType.equals(threadType)) return childrenThreadVo;
			
			if(threadPool.getActiveCount() >= threadPool.getCorePoolSize()) continue ;
			return childrenThreadVo;
		}
		return null;
	}

	/**
	 * 所属任务id
	 * @return
	 */
	public int getJobId() {
		return jobId;
	}
	
	private class ChildrenThreadVo {
	
		// 线程唯一标识
		private final String threadMark;
		
		// 线程类型集合
		private final Map<String, ThreadPoolExecutor> threadPoolMap = new HashMap<String, ThreadPoolExecutor>();
		
		/**
		 * 
		 * @param oneIpThread 采集线程数
		 * @param remotePost 远程保存
		 * @param mqPost mq保存
		 * @param dcPost 数据中心接口保存
		 * @param updateJobPost 任务状态保存
		 */
		private ChildrenThreadVo(int oneIpThread, boolean lutaoPost, boolean mqPost, boolean dcPost,boolean tsPost, boolean updateJobPost) {
			this.threadMark = jobId + "_" + UUID.randomUUID();
			this.setThreadPoolMap(oneIpThread, lutaoPost, mqPost, dcPost,tsPost, updateJobPost);
		}
		
		/**
		 * 设置线程池
		 * @param oneIpThread 采集线程数
		 * @param remotePost 远程保存
		 * @param mqPost mq保存
		 * @param dcPost 数据中心接口保存
		 * @param updateJobPost 任务状态保存
		 */
		private synchronized void setThreadPoolMap(int oneIpThread, boolean remotePost, boolean mqPost, boolean dcPost,boolean tsPost, boolean updateJobPost) {
			// 修改线程池数量
			this.setThreadPool(true, ThreadType.crawlerType, oneIpThread);

			// 以下的只能删除或者新增
			int amount = oneIpThread%5 == 0 ?  oneIpThread/5 : oneIpThread/5 + 1;
			this.setThreadPool(remotePost,  ThreadType.postRemoteResultType, amount);
			
			this.setThreadPool(mqPost,  ThreadType.postMqResultType, amount);
			
			this.setThreadPool(dcPost,  ThreadType.postDcResultType, amount);
			
			this.setThreadPool(tsPost,  ThreadType.postTsResultType, amount);
			
			this.setThreadPool(dcPost,  ThreadType.postOtherResultType, amount);
			
			this.setThreadPool(updateJobPost,  ThreadType.postJobDetailStatusType, amount);
		}
		
		/**
		 * 设置线程信息
		 * @param use
		 * @param threadType
		 * @param amount
		 */
		private void setThreadPool(boolean use, String threadType, int amount) {
			ThreadPoolExecutor threadPoolExecutor = this.threadPoolMap.get(threadType);
			if(use) {
				String threadName = jobId + "-" + threadType;
				if(null == threadPoolExecutor) this.threadPoolMap.put(threadType, this.getThreadPoolExecutor(threadName, amount, amount));
				else if(amount != threadPoolExecutor.getCorePoolSize()) {
					threadPoolExecutor.setCorePoolSize(amount);
					threadPoolExecutor.setMaximumPoolSize(amount);
				}
			} else if(null != threadPoolExecutor) {
				threadPoolExecutor.shutdown();
				this.threadPoolMap.remove(threadType);
			}
		}
		
		/**
		 * 返回 ThreadPoolExecutor
		 * @param threadName
		 * @param corePoolSize
		 * @param maximumPoolSize
		 * @return
		 */
		private ThreadPoolExecutor getThreadPoolExecutor(final String threadName, int corePoolSize, int maximumPoolSize) {
			ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 1,
					TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
			threadPoolExecutor.allowCoreThreadTimeOut(true);
			threadPoolExecutor.setThreadFactory(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
		            return new Thread(r, threadName);
				}
			});
			return threadPoolExecutor;
		}
		
		/**
		 * shutdown threadPoolMap
		 */
		private void shutdownThreadPoolMap() {
			Iterator<Map.Entry<String, ThreadPoolExecutor>> itMap = this.threadPoolMap.entrySet().iterator();
			while(itMap.hasNext()) {
				ThreadPoolExecutor threadPoolExecutor = itMap.next().getValue();
				if(!threadPoolExecutor.isShutdown()) threadPoolExecutor.shutdown();
			}
			this.threadPoolMap.clear();
		}
	}
}