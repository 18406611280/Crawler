package com.aft.browser.jweb;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class TabItemBrowser {

	private final String itemKey;
	
	private final JWebBrowser browser;
	
	public TabItemBrowser(String itemKey, JWebBrowser browser) {
		this.itemKey = itemKey;
		this.browser = browser;
	}
	
	public String getItemKey() {
		return itemKey;
	}

	public JWebBrowser getBrowser() {
		return browser;
	}
}