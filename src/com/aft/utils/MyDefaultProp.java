package com.aft.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 默认配置文件加载
 *
 */
public class MyDefaultProp {
	
	private final static Logger logger = Logger.getLogger(MyDefaultProp.class);

	// 自定义默认属性文件
	private static String propCustomFile;
	
	
	
	
	// 是否打开备份开关
	private static boolean backupOpen;
	
	// 采集结果, 保存失败存放目录
	private static String backupFilePath;
	
	// 备份数据中心保存失败的内容
	private static String backupPostDcFailResultFile;
	
	// 备份mq保存失败的内容
	private static String backupPostMqFailResultFile;
	
	// 备份保存失败的内容
	private static String backupPostRemoteFailResultFile;
	
	// 备份更新任务状态结果内容
	private static String backupUpdateJobDetailResultFile;
	
	
	// 自定义...
	
	// 是否本地测试
	private static String userAgent;
	
	// 获取IP代理方式
	private static String proxyType;//1:快代理 2:VPS代理 3:全网代理
	
	// 是否本地测试
	private static boolean localTest;
	
	// 本地测试类型
	private static String testPageType;
	
	private static String operatorName;
	
	private static String commonInfoUrl;
	
	
	private static boolean socketServerOpen;
	
	private static int socketServerPort;
	
	private static String socketEndStr;
	
	
	
	private static String browserPlugin;
	
	private static boolean mozilla;
	
	private static String xulrunner;
	
	private static boolean postCaappOther;
	
	private static String postCaappUrl;
	
	private static String postCheapairUrl;
	
	private static ArrayList<String> caAccountList;
	
	private static boolean postCheapairOpen;
	
	static {
		try {
			
			
			Properties properties = new Properties();
			properties.load(MyDefaultProp.class.getResourceAsStream("/default.properties"));
			propCustomFile = properties.getProperty("prop_custom_file").trim();
			caAccountList = CaAccount.getAccountList();
			backupOpen = Boolean.parseBoolean(properties.getProperty("backup_open").trim());
			backupFilePath = properties.getProperty("backup_file_path").trim();
			
			backupPostDcFailResultFile = properties.getProperty("backup_post_dc_fail_result_file").trim();
			backupPostMqFailResultFile = properties.getProperty("backup_post_mq_fail_result_file").trim();
			backupPostRemoteFailResultFile = properties.getProperty("backup_post_remote_fail_result_file").trim();
			backupUpdateJobDetailResultFile = properties.getProperty("backup_update_jobDetail_result_file").trim();
			
			MyDefaultProp.reloadCustomDefault(properties);
		} catch(Exception e) {
			logger.error("DefaultProperties 加载异常:\r", e);
		}
	}
	
	/**
	 * 重新加载部分属性
	 * 
	 * @throws Exception 
	 */
	public synchronized static void reloadCustomDefault(Properties properties) throws Exception {
		if(StringUtils.isNotEmpty(propCustomFile)) {
			File propFile = new File(propCustomFile);
			if(propFile.exists()) {
				properties = new Properties();
				properties.load(new FileInputStream(propFile));
			}
		}
		if(null == properties) return ;
		userAgent = properties.getProperty("user-agent");
		if(StringUtils.isEmpty(userAgent)) userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0";
		
		proxyType = properties.getProperty("proxyIp_type");
		if(StringUtils.isEmpty(proxyType)) proxyType = "2";
		
		String lt = properties.getProperty("local_test");
		localTest = StringUtils.isEmpty(lt) ? false : Boolean.valueOf(lt.trim());
		
		testPageType = properties.getProperty("test_pageTyge");
		if(null != testPageType) testPageType = testPageType.trim();
		
		operatorName = properties.getProperty("operator_name").trim();
		commonInfoUrl = properties.getProperty("common_info_url").trim();
		
		
		String sso = properties.getProperty("socket_server_open");
		socketServerOpen = StringUtils.isEmpty(sso) ? false : Boolean.valueOf(sso);
		String ssp = properties.getProperty("socket_server_port");
		if(StringUtils.isNotEmpty(ssp)) socketServerPort = Integer.parseInt(ssp.trim());
		socketEndStr = properties.getProperty("socket_end_str");
		
		
		browserPlugin = properties.getProperty("browser_plugin");
		mozilla = "Mozilla".equalsIgnoreCase(properties.getProperty("browser_type"));
		xulrunner = properties.getProperty("xulrunner_name");
		
		String pco = properties.getProperty("post_caapp_other");
		postCaappOther = StringUtils.isEmpty(pco) ? false : Boolean.valueOf(pco.trim());
		postCaappUrl = properties.getProperty("post_caapp_url");
		
		String pcu = properties.getProperty("post_cheapair_open");
		postCheapairOpen = StringUtils.isEmpty(pcu) ? false : Boolean.valueOf(pcu.trim());
		postCheapairUrl = properties.getProperty("post_cheapair_Url");
		
	}
	
	public static String getUserAgent() {
		return userAgent;
	}

	public static boolean getLocalTest() {
		return localTest;
	}

	public static String getTestPageType() {
		return testPageType;
	}

	public static ArrayList getCaAccountList() {
		return caAccountList;
	}

	public static String getPropCustomFile() {
		return propCustomFile;
	}
	
	public static boolean getBackupOpen() {
		return backupOpen;
	}

	public static String getBackupFilePath() {
		return backupFilePath;
	}

	public static String getBackupPostRemoteFailResultFile() {
		return backupPostRemoteFailResultFile;
	}

	public static void setBackupPostRemoteFailResultFile(
			String backupPostRemoteFailResultFile) {
		MyDefaultProp.backupPostRemoteFailResultFile = backupPostRemoteFailResultFile;
	}

	public static String getBackupPostDcFailResultFile() {
		return backupPostDcFailResultFile;
	}

	public static String getBackupPostMqFailResultFile() {
		return backupPostMqFailResultFile;
	}

	public static String getBackupUpdateJobDetailResultFile() {
		return backupUpdateJobDetailResultFile;
	}

	public static String getOperatorName() {
		return operatorName;
	}

	public static String getCommonInfoUrl() {
		return commonInfoUrl;
	}
	
	public static boolean isSocketServerOpen() {
		return socketServerOpen;
	}

	public static int getSocketServerPort() {
		return socketServerPort;
	}
	
	public static String getSocketEndStr() {
		return socketEndStr;
	}

	public static String getBrowserPlugin() {
		return browserPlugin;
	}

	public static boolean getMozilla() {
		return mozilla;
	}

	public static String getXulrunner() {
		return xulrunner;
	}

	public static boolean getPostCaappOther() {
		return postCaappOther;
	}

	public static String getPostCaappUrl() {
		return postCaappUrl;
	}

	public static String getPostCheapairUrl() {
		return postCheapairUrl;
	}

	public static boolean getPostCheapairOpen() {
		return postCheapairOpen;
	}

	public static String getProxyType() {
		return proxyType;
	}
	
}