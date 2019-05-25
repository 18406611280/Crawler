package com.aft.crawl.result.kafka.consumer;

import kafka.consumer.ConsumerIterator;

import org.apache.log4j.Logger;

public abstract class KafkaConsumerRunnable implements Runnable {
	
	private final static Logger logger = Logger.getLogger(KafkaConsumerRunnable.class);
	
	private ConsumerIterator<String,String> consumerIterator;
	
	@Override
	public void run() {
		if(null == consumerIterator) return ;
		while(consumerIterator.hasNext()) {
			String message = null;//consumerIterator.next().message();
			try {
				this.dealMessage(message);
			} catch (Exception e) {
				logger.error("消费异常:" + message + "\r", e);
			}
		}
	}
	
	protected abstract void dealMessage(String message);

	public void setConsumerIterator(ConsumerIterator<String, String> consumerIterator) {
		this.consumerIterator = consumerIterator;
	}
}