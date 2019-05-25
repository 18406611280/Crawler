//package com.aft.browser.htmlunit;
//
//import com.aft.utils.thread.MyThreadUtils;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//
//public class MyHtmlUnitUtil {
//	
//	public static void main(String[] args) throws Exception {
//		init();
//	}
//
//	private static void init() throws Exception {
//		WebClient webclient = new WebClient();
//
//		// 这里是配置一下不加载css和javaScript,配置起来很简单，是不是
//		webclient.getOptions().setCssEnabled(false);
//		webclient.getOptions().setJavaScriptEnabled(true);
//
//		// 做的第一件事，去拿到这个网页，只需要调用getPage这个方法即可
//		HtmlPage htmlpage = webclient.getPage("http://www.tianjin-air.com/index.jsp");
//		htmlpage.getElementById("airplaneCity1").setAttribute("value", "CAN");
//		htmlpage.getElementById("airplaneCity2").setAttribute("value", "TSN");
//		htmlpage.getElementById("goday").setAttribute("value", "2016-08-30");
//		
//		htmlpage.executeJavaScript("checkform()");
//		
//		MyThreadUtils.sleep(10000);
//		
//		System.out.println(htmlpage.asXml());
//	}
//}