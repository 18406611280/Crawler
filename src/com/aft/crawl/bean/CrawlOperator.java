package com.aft.crawl.bean;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.aft.utils.MyDefaultProp;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 任务执行者
 * 
 */
public class CrawlOperator implements Serializable {

	private static final long serialVersionUID = -6640067076786680527L;
	
	private final static Logger logger = Logger.getLogger(CrawlOperator.class);

	// 名称
	private String name;

	// ADSL宽带名称
	private String adslTitle;
	
	// ADSL用户名
	private String adslUsername;
	
	// ADSL密码
	private String adslPassword;
	
	// 网卡名称
	private String networkInterface;
	
	private static CrawlOperator crawlOperator;
	
	private CrawlOperator() {}
	
	/**
	 * 请求获取操作者信息
	 */
	@SuppressWarnings("unchecked")
	public synchronized static CrawlOperator loadOperator() {
		try {
			logger.info(MyDefaultProp.getOperatorName() + ", 请求获取操作者信息开始...");
			String result = MyHttpClientUtil.get(CrawlCommon.getOperatorUrl() + "?name=" + MyDefaultProp.getOperatorName());
			logger.info(MyDefaultProp.getOperatorName() + ", 请求获取操作者信息返回:" + result);
			if("null".equalsIgnoreCase(result) || StringUtils.isEmpty(result)) return null;
			
			Map<String, Object> resultMap = MyJsonTransformUtil.readValue(result, Map.class);
			if(null ==crawlOperator) crawlOperator = new CrawlOperator();
			crawlOperator.name = resultMap.get("name").toString().trim();
			crawlOperator.adslTitle = (String)resultMap.get("adslTitle");
			crawlOperator.adslUsername = (String)resultMap.get("adslUsername");
			crawlOperator.adslPassword = (String)resultMap.get("adslPassword");
			crawlOperator.networkInterface = (String)resultMap.get("networkInterface");
			return crawlOperator;
		} catch(Exception e) {
			logger.error(MyDefaultProp.getOperatorName() + ", 请求获取操作者信息异常:\r", e);
		}
		return null;
	}

	public static String getName() {
		return crawlOperator.name;
	}

	public static String getAdslTitle() {
		return crawlOperator.adslTitle;
	}

	public static String getAdslUsername() {
		return crawlOperator.adslUsername;
	}

	public static String getAdslPassword() {
		return crawlOperator.adslPassword;
	}

	public static String getNetworkInterface() {
		return crawlOperator.networkInterface;
	}
}