package com.aft.browser.swt;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.aft.browser.OrderBrowser;
import com.aft.browser.util.IeUtil;
import com.aft.browser.util.MozillaUtil;
import com.aft.utils.MyDefaultProp;
import com.aft.utils.thread.MyThreadUtils;


public final class SwtOrderBrowser extends OrderBrowser {

	private final static Logger logger = Logger.getLogger(SwtOrderBrowser.class);
	
	private final static Display display = Display.getDefault();
	
	private final static Shell shell = new Shell();
	
	private final static CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
	
	// 只能一个...多余的会共享session
	private final static CopyOnWriteArrayList<TabItemBrowser> tabItemBrowsers = new CopyOnWriteArrayList<TabItemBrowser>();
	
	/**
	 * 初始化
	 */
	@Override
	protected void init() throws Exception {
		if(MyDefaultProp.getMozilla())
			System.setProperty("org.eclipse.swt.browser.XULRunnerPath", new File(MyDefaultProp.getXulrunner()).getAbsolutePath());
		shell.setText("browser");
		shell.setSize(windowWidth, windowHeight);
		shell.setLayout(new GridLayout(1, false));
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent event) {
				MessageBox messagebox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                messagebox.setText("程序提示");
                messagebox.setMessage("确定退出?") ;
                if(SWT.YES == messagebox.open()) {
                	logger.info("程序退出...");
                	event.doit = true;
                	System.exit(0);
                } event.doit = false;
			}
		});
		
		SwtOrderBrowser.this.createTabFolder();
		
		Rectangle clientArea = Display.getCurrent().getClientArea();
		shell.setLocation(clientArea.width/2 - shell.getSize().x/2, clientArea.height/2 - shell.getSize().y/2);
		shell.layout();
		shell.open();
		shell.setVisible(false);
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

	@Override
	public void proxy(String proxyIp, int proxyPort) throws Exception {
		if(MyDefaultProp.getMozilla()) MozillaUtil.setMozillaProxy(proxyIp, proxyPort);
		else IeUtil.setProxy(proxyIp, proxyPort);
	}
	
	@Override
	public void addBrowser(final String itemKey, final String url) {
		final Browser temp = SwtOrderBrowser.getBrowser(itemKey);
		if(null != temp) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					temp.setUrl(url);
				}
			});
			return ;
		}
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				final CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
				tabItem.setShowClose(true);
				tabItem.setText("新开窗口");
				final Composite itemComp = new Composite(tabFolder, SWT.NONE);
				itemComp.setLayout(new FillLayout());
				
				// SWT.WEBKIT
				// SWT.MOZILLA
				final Browser browser = new Browser(itemComp, MyDefaultProp.getMozilla() ? SWT.MOZILLA : SWT.NONE);
		        browser.addOpenWindowListener(new OpenWindowListener() {
					public void open(WindowEvent event) {
						final Browser browser2 = new Browser(itemComp, MyDefaultProp.getMozilla() ? SWT.MOZILLA : SWT.NONE);
						event.browser = browser2;	// 将e的事件用我的浏览器打开
						event.display.asyncExec(new Runnable() {	// 必须要有
							public void run() {
								browser.setUrl(browser2.getUrl());
							}
						});
					}
				});
//				
				if(null != url) browser.setUrl(url);				
				tabItem.setControl(itemComp);
				tabFolder.setSelection(tabItem);
				tabItemBrowsers.add(new TabItemBrowser(itemKey, browser));
			}
		});
	}
	

	/**
	 * 中部窗体
	 */
	private void createTabFolder() {
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent event) {
				event.doit = false;
			}
		});
		tabFolder.setTabHeight(20);
		tabFolder.setSimple(false); 	// 设置圆角
//		this.addBrowser("xxx", "www.baidu.com");
//		tabFolder.setSelection(0);
	}
	
	/**
	 * 获取游览器
	 * 
	 * @param itemKey
	 * @return
	 */
	private static Browser getBrowser(String itemKey) {
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
				browserText = SwtOrderBrowser.getBrowser(itemKey).getText();
			}
		});
		return browserText;
	}
	
	@Override
	public synchronized String getBrowserUrl(final String itemKey) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				browserUrl = SwtOrderBrowser.getBrowser(itemKey).getUrl();
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
					evaluateResult = SwtOrderBrowser.getBrowser(itemKey).evaluate(jsCode);
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
					executeResult = SwtOrderBrowser.getBrowser(itemKey).execute(jsCode);
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
				SwtOrderBrowser.getBrowser(itemKey).setUrl(url);
			}
		});
		while(SwtOrderBrowser.getBrowser(itemKey).equals(srcUrl)) MyThreadUtils.sleep(100);
	}
	
	@Override
	public synchronized void refresh(final String itemKey) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				SwtOrderBrowser.getBrowser(itemKey).refresh();
			}
		});
	}
}