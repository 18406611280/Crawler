package com.aft.utils.htmlunit.http;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.aft.crawl.proxy.ProxyUtil;
import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.thread.MyThreadUtils;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 *  HtmlUnit java 浏览器模拟操作工具
 * Created by liangdongming on 2017/11/17.
 */
public class HtmlUnitUtil {
	
	public final static Logger logger = Logger.getLogger(MyHttpClientUtil.class);
	
	public final static int defaultConnTimeout = 50000;
	
	public final static int defaultSoTimeout = 30 * 1000;
	
	public final static String defaultCharset = "UTF-8";
	
	public final static String httpGet = "GET";
	
	public final static String httpPost = "POST";
	
    /**
     * 获取 WebClient 连接实例
     * @param address
     * @param port
     * @return
     */
    public static WebClient getConnection(String threadMark,boolean javaScript) {
        WebClient webClient = new WebClient();
        init(webClient,javaScript); //加载配置
        if(threadMark!=null){
			Object[] proxyInfos = ProxyUtil.getProxyInfo(threadMark);
			if(null == proxyInfos || proxyInfos[0]==null) {
				synchronized (threadMark) {
					while(null == proxyInfos || proxyInfos[0]==null) {
						proxyInfos = ProxyUtil.getProxyInfo(threadMark);
						MyThreadUtils.sleep(500);
					}
				}
			}
			setProxy(webClient,proxyInfos[0].toString(),Integer.parseInt(proxyInfos[1].toString())); //设置代理IP
		}
        return webClient;
    }

    /**
     * Get请求
     * @param url
     * @return HtmlPage
     * @throws Exception
     */
    public static HtmlPage get(WebClient webClient, String url,Map<String, String> headers) throws Exception {
        WebRequest webRequest = new WebRequest(new URL(url));
        webRequest.setHttpMethod(HttpMethod.GET);
        setHeaders(webRequest, headers);
        HtmlPage htmlPage = webClient.getPage(webRequest);
        return htmlPage;
    }

    /**
     * Post 请求
     * @param url
     * @param params
     * @return HtmlPage
     * @throws Exception
     */
    public static HtmlPage post(WebClient webClient,String url,Map<String, String> headers, Map<String, String> params) throws Exception {
        WebRequest webRequest = new WebRequest(new URL(url));
        webRequest.setHttpMethod(HttpMethod.POST);
        setHeaders(webRequest, headers);
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                webRequest.getRequestParameters().add(new NameValuePair(param.getKey(), param.getValue()));
            }
        }
        HtmlPage htmlPage = webClient.getPage(webRequest);
        return htmlPage;
    }
    /***
     * 加载配置
     */
    private static void init(WebClient webClient,boolean javaScript){
        // 1 启动JS
        webClient.getOptions().setJavaScriptEnabled(javaScript);
        // 2 禁用Css，可避免自动二次请求CSS进行渲染
        webClient.getOptions().setCssEnabled(false);
        // 3 启动客户端重定向
        webClient.getOptions().setRedirectEnabled(true);
        // 4 js运行错误时，是否抛出异常
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        // 5 设置超时
        webClient.getOptions().setTimeout(120000);
        //6 设置忽略证书
        webClient.getOptions().setUseInsecureSSL(true);
        //7 设置Ajax
        //webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        //8设置cookie
        webClient.getCookieManager().setCookiesEnabled(true);
    }

    /**
     * 设置头部信息
     */
    private static void setHeaders(WebRequest webRequest, Map<String, String> headers){
    	if (headers != null && headers.size() > 0) {
        	for (Map.Entry<String, String> header : headers.entrySet()) {
        		webRequest.getAdditionalHeaders().put(header.getKey(), header.getValue());
        	}
        }
    }
    
    /**
     * 设置代理IP
     */
    private static void setProxy(WebClient webClient, String address, int port){
        ProxyConfig proxyConfig = webClient.getOptions().getProxyConfig();
        proxyConfig.setProxyHost(address);
        proxyConfig.setProxyPort(port);
    }
    private static void setProxy(WebRequest webRequest, String address, int port){
    	webRequest.setProxyHost(address);
    	webRequest.setProxyPort(port);
    }
    /**
     * 设置Cookies
     */
    private static void setCookies(WebClient webClient, Set<Cookie> cookies){
    	Iterator<Cookie> i = cookies.iterator();  
        while (i.hasNext()){  
        	webClient.getCookieManager().addCookie(i.next());  
        } 
    }
}
