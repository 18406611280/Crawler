package com.aft.browser;

import org.apache.log4j.Logger;

import com.aft.browser.jweb.JWebOrderBrowser;
import com.aft.browser.swt.SwtOrderBrowser;
import com.aft.utils.MyDefaultProp;

public abstract class OrderBrowser implements OrderBrowserInterface {
	
	private final static Logger logger = Logger.getLogger(OrderBrowser.class);
	
	public final static int windowWidth = 1024;
	
	public final static int windowHeight = 768;
	
	protected static Object evaluateResult;
	
	protected static boolean executeResult;
	
	protected static String browserText;
	
	protected static String browserUrl;
	
	private static OrderBrowser orderBrowser = null;
	
	/**
	 * 初始化
	 * @throws Exception
	 */
	protected abstract void init() throws Exception;
	
	/**
	 * 新开browser
	 * @param itemKey
	 * @param url
	 * @throws Exception
	 */
	public abstract void addBrowser(String itemKey, String url) throws Exception;
	
	/**
	 * 实例游览器对象
	 * @return
	 * @throws Exception
	 */
	public synchronized final static void initOrderBrowser() throws Exception {
		if(null != orderBrowser) return ;
		logger.info("browser start >>>>>>>>>>>");
		if("swt".equalsIgnoreCase(MyDefaultProp.getBrowserPlugin())) orderBrowser = new SwtOrderBrowser();
		else if("jweb".equalsIgnoreCase(MyDefaultProp.getBrowserPlugin())) orderBrowser = new JWebOrderBrowser();
		if(null == orderBrowser) {
			logger.warn("browser不存在此插件名称!");
			return ;
		}
		orderBrowser.init();
	}

	public static OrderBrowser getOrderBrowser() {
		return orderBrowser;
	}
}