package com.aft.utils.jackson;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * 有问题...
 *
 */
@Deprecated
public final class MyXmlTransformUtil {
	
	private static final ThreadLocal<XmlMapper> threadLocal = new ThreadLocal<XmlMapper>();
	
	private static XmlMapper getXmlMapper() {
		XmlMapper xmlMapper = threadLocal.get();
		if(null == xmlMapper) {
			JacksonXmlModule module = new JacksonXmlModule();
			module.setDefaultUseWrapper(false);
			xmlMapper = new XmlMapper(module);
			xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			threadLocal.set(xmlMapper);
		}
		return xmlMapper;
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
		return (T)MyXmlTransformUtil.getXmlMapper().readValue(inputStream, valueType);
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
		JavaType javaType = MyXmlTransformUtil.getXmlMapper().getTypeFactory().constructCollectionType(List.class, listBean);
		return (List<T>)MyXmlTransformUtil.getXmlMapper().readValue(inputStream, javaType);
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
		return (T)MyXmlTransformUtil.getXmlMapper().readValue(content, valueType);
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
		JavaType javaType = MyXmlTransformUtil.getXmlMapper().getTypeFactory().constructCollectionType(List.class, listBean);
		return (List<T>)MyXmlTransformUtil.getXmlMapper().readValue(content, javaType);
	}
	
	/**
	 * 转成 xml 串
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String writeValue(Object obj) throws Exception {
		return MyXmlTransformUtil.getXmlMapper().writeValueAsString(obj);
	}
}