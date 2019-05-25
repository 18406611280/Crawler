package com.aft.crawl.result.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

import org.apache.log4j.Logger;

import com.aft.crawl.result.kafka.consumer.KafkaConsumerRunnable;

public class KafkaConsumer {
	
	private final static Logger logger = Logger.getLogger(KafkaConsumer.class);
	
	private static int errorAmount = 0;
	
	private static int maxErrorAmount = 10;
	
	public static void main(String[] args) throws Exception {
		List<KafkaConsumerRunnable> kafkaConsumerRunnables = new ArrayList<KafkaConsumerRunnable>();
//		kafkaConsumerRunnables.add(new CrawlerJobDetailRunnable());
//		kafkaConsumerRunnables.add(new CrawlerJobDetailRunnable());
		consume("192.168.0.72:2181,192.168.0.74:2181,192.168.0.75:2181", "updateCrawlJob-8", "updateCrawlJob", kafkaConsumerRunnables);
	}
	
	/**
	 * 消费
	 * @param zookeeper
	 * @param groupId
	 * @param topicName
	 * @param kafkaConsumerRunnables
	 * @return
	 */
	public static void consume(String zookeeper, String groupId, String topicName,
			List<KafkaConsumerRunnable> kafkaConsumerRunnables) throws Exception {
		try {
	    	Properties props = new Properties();
	        props.put("zookeeper.connect", zookeeper);
	        props.put("group.id", groupId);
	        props.put("auto.offset.reset", "smallest");
			props.put("serializer.class", "kafka.serializer.StringEncoder");
			props.put("zookeeper.session.timeout.ms", "10000");		// zk连接超时
			props.put("zookeeper.connection.timeout.ms", "10000");
			props.put("zookeeper.sync.time.ms", "2000");
			props.put("auto.commit.enable", "true");
			props.put("auto.commit.interval.ms", "2000");

			ConsumerConfig config = new ConsumerConfig(props);
			
			ConsumerConnector consumer = Consumer.createJavaConsumerConnector(config);
			Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
			topicCountMap.put(topicName, kafkaConsumerRunnables.size());	// 多少个线程
			
			ExecutorService threadPool = Executors.newFixedThreadPool(kafkaConsumerRunnables.size());

			StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
			StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
			Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);
			
			List<KafkaStream<String,String>> streams = consumerMap.get(topicName);
			for(int i=0; i<streams.size(); i++) {
//				KafkaStream<String, String> stream = streams.get(i);
//				kafkaConsumerRunnables.get(i).setConsumerIterator(stream.iterator());
//				threadPool.execute(kafkaConsumerRunnables.get(i));
			}
			threadPool.shutdown();
			errorAmount = 0;
		} catch(Exception e) {
			if(errorAmount++ >= maxErrorAmount) {
				logger.error("kafka 处理[" + zookeeper + "], [" + groupId + "], [" + topicName + "]连续[" + maxErrorAmount + "]异常, 跳出...");
				return ;
			}
			logger.error("kafka 处理[" + zookeeper + "], [" + groupId + "], [" + topicName + "]异常:\r", e);
			KafkaConsumer.consume(zookeeper, groupId, topicName, kafkaConsumerRunnables);
		}
	}
}
