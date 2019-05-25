package com.aft.utils.jackson;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class MyJsonTransformUtil {
	
	private static final ThreadLocal<ObjectMapper> threadLocal = new ThreadLocal<ObjectMapper>();
	
	private static ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = threadLocal.get();
		if(null == objectMapper) {
			objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			threadLocal.set(objectMapper);
		}
		return objectMapper;
	}
	
	/**
	 * 转成 valueType 类型
	 * @param <T>
	 * @param inputStream
	 * @param valueType
	 * @return
	 * @throws Exception
	 */
	public static <T> T readValue(InputStream inputStream, Class<T> valueType) throws Exception {
		return (T)MyJsonTransformUtil.getObjectMapper().readValue(inputStream, valueType);
	}
	
	/**
	 * 转成 valueType 类型
	 * @param <T>
	 * @param inputStream
	 * @param valueType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> readValueToList(InputStream inputStream, Class<T> listBean) throws Exception {
		JavaType javaType = MyJsonTransformUtil.getObjectMapper().getTypeFactory().constructCollectionType(List.class, listBean);
		return (List<T>)MyJsonTransformUtil.getObjectMapper().readValue(inputStream, javaType);
	}
	
	/**
	 * 转成 valueType 类型
	 * @param <T>
	 * @param content
	 * @param valueType
	 * @return
	 * @throws Exception
	 */
	public static <T> T readValue(String content, Class<T> valueType) throws Exception {
		return (T)MyJsonTransformUtil.getObjectMapper().readValue(content, valueType);
	}
	
	/**
	 * 转成 valueType 类型
	 * @param <T>
	 * @param content
	 * @param listBean
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> readValueToList(String content, Class<T> listBean) throws Exception {
		JavaType javaType = MyJsonTransformUtil.getObjectMapper().getTypeFactory().constructCollectionType(List.class, listBean);
		return (List<T>)MyJsonTransformUtil.getObjectMapper().readValue(content, javaType);
	}
	
	/**
	 * 转成 json 串
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String writeValue(Object obj) throws Exception {
		return MyJsonTransformUtil.getObjectMapper().writeValueAsString(obj);
	}
	
	/**
	 * 是否是json格式 Object 格式...
	 * @param content
	 * @return
	 */
	public static Object isJson(String content) {
		try {
			Object json = MyJsonTransformUtil.getObjectMapper().readValue(content, Object.class);
			return json;
		} catch(Exception e) {
			return null;
		}
	}
}