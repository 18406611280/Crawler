package com.aft.browser.swt;

import org.eclipse.swt.browser.Browser;

public class TabItemBrowser {

	private final String itemKey;
	
	private final Browser browser;
	
	public TabItemBrowser(String itemKey, Browser browser) {
		this.itemKey = itemKey;
		this.browser = browser;
	}
	
	public String getItemKey() {
		return itemKey;
	}

	public Browser getBrowser() {
		return browser;
	}
}