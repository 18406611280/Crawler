package com.aft.crawl.result.kafka;

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;

/**
 * 提交数据类
 *
 */
public class KafkaProducer {
	
	public final static Logger logger = Logger.getLogger(KafkaProducer.class);
	
	/**
	 * 发送数据
	 * @param mqBrokerList
	 * @param mqTopic
	 * @param message
	 */
	public static boolean producer(String mqBrokerList, String mqTopic, String message) {
		Producer<String, String> producer = null;
		try {
			Properties props = new Properties();
			props.put("request.required.acks", "1");
			props.put("metadata.broker.list", mqBrokerList);
			props.put("serializer.class", "kafka.serializer.StringEncoder"); 		// 配置 value 的序列化
			props.put("key.serializer.class", "kafka.serializer.StringEncoder");	// 配置 key 的序列化
			producer = new Producer<String, String>(new ProducerConfig(props));
			KeyedMessage<String, String> data = new KeyedMessage<String, String>(mqTopic, message);
			producer.send(data);
			producer.close();
			return true;
		} catch (Exception e) {
			logger.error("发送主题[" + mqTopic + "]消息异常:\r", e);
		} finally {
			if(null != producer) {
				producer.close();
				producer = null;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		producer("192.168.0.75:6666", "crawlJob", "{\"xxxx\":\"ccccc\"}");
	}
}