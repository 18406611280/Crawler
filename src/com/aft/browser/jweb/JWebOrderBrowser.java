package com.aft.browser.jweb;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import com.aft.browser.OrderBrowser;
import com.aft.browser.util.IeUtil;
import com.aft.browser.util.MozillaUtil;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.thread.MyThreadUtils;


public final class JWebOrderBrowser extends OrderBrowser {

	private final static Logger logger = Logger.getLogger(JWebOrderBrowser.class);
	
	// 只能一个...多余的会共享session
	private final static CopyOnWriteArrayList<TabItemBrowser> tabItemBrowsers = new CopyOnWriteArrayList<TabItemBrowser>();
	
	private final JFrame frame = new JFrame("JWebBrowser");
	
	private final JTabbedPane jTabbedPane = new JTabbedPane(JTabbedPane.TOP);
	
	static {
		NativeInterface.open();
		NativeInterface.runEventPump();
	}
	
	/**
	 * 初始化
	 */
	@Override
	protected void init() throws Exception {
		this.frame.add(this.jTabbedPane);
		this.frame.setSize(windowWidth, windowHeight);
//		this.frame.invalidate();
		this.frame.setVisible(false);
	}

	@Override
	public void proxy(String proxyIp, int proxyPort) throws Exception {
		if(MyDefaultProp.getMozilla()) MozillaUtil.setMozillaProxy(proxyIp, proxyPort);
		else IeUtil.setProxy(proxyIp, proxyPort);
	}
	
	/**
	 * add browser tab
	 * @param itemKey
	 * @param url
	 * 
	 * @return
	 */
	@Override
	public void addBrowser(final String itemKey, final String url) {
		final JWebBrowser temp = JWebOrderBrowser.getBrowser(itemKey);
		if(null != temp) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					temp.navigate(url);
				}
			});
			return ;
		}
		if(MyDefaultProp.getMozilla())
			System.setProperty("org.eclipse.swt.browser.XULRunnerPath", new File(MyDefaultProp.getXulrunner()).getAbsolutePath());
		final JWebBrowser webBrowser = MyDefaultProp.getMozilla() ? new JWebBrowser(JWebBrowser.useXULRunnerRuntime()) : new JWebBrowser();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				webBrowser.navigate(url);
				webBrowser.setBarsVisible(false);
			}
		});
		this.jTabbedPane.addTab(tabItemBrowsers.size() + "", webBrowser);
		tabItemBrowsers.add(new TabItemBrowser(itemKey, webBrowser));
	}
	
	
	/**
	 * 获取游览器
	 * 
	 * @param itemKey
	 * @return
	 */
	private static JWebBrowser getBrowser(String itemKey) {
		for(TabItemBrowser tabItemBrowser : tabItemBrowsers) {
			if(!tabItemBrowser.getItemKey().equals(itemKey)) continue ;
			return tabItemBrowser.getBrowser();
		}
		return null;
	}
	
	@Override
	public synchronized String getBrowserText(final String itemKey) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				browserText = JWebOrderBrowser.getBrowser(itemKey).getHTMLContent();
			}
		});
		return browserText;
	}
	
	@Override
	public synchronized String getBrowserUrl(final String itemKey) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				browserUrl = JWebOrderBrowser.getBrowser(itemKey).getResourceLocation();
			}
		});
		return browserUrl;
	}
	
	@Override
	public synchronized Object evaluateJs(final String itemKey, final String jsCode) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					evaluateResult = JWebOrderBrowser.getBrowser(itemKey).executeJavascriptWithResult(jsCode);
				} catch(Exception e) {
					evaluateResult = null;
					logger.error("处理页面 js[" + jsCode + "], 异常:\r", e);
				}
			}
		});
		return evaluateResult;
	}
	
	@Override
	public synchronized boolean executeJs(final String itemKey, final String jsCode) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					JWebOrderBrowser.getBrowser(itemKey).executeJavascript(jsCode);
				} catch(Exception e) {
					executeResult = false;
					logger.error("处理页面 js[" + jsCode + "], 异常:\r", e);
				}
			}
		});
		return executeResult;
	}
	
	@Override
	public synchronized void setBrowserUrl(final String itemKey, final String url) {
		final String srcUrl = this.getBrowserUrl(itemKey);
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				logger.info("跳转地址:" + url);
				JWebOrderBrowser.getBrowser(itemKey).navigate(url);
			}
		});
		while(JWebOrderBrowser.getBrowser(itemKey).equals(srcUrl)) MyThreadUtils.sleep(100);
	}
	
	@Override
	public synchronized void refresh(final String itemKey) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				JWebOrderBrowser.getBrowser(itemKey).reloadPage();
			}
		});
	}
}