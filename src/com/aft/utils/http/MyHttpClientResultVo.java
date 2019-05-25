package com.aft.utils.http;

import org.apache.http.Header;

public class MyHttpClientResultVo {

	private String httpResult;
	
	private Header[] headers;
	
	public MyHttpClientResultVo(String httpResult) {
		this(httpResult, null);
	}

	public MyHttpClientResultVo(String httpResult, Header[] headers) {
		this.httpResult = httpResult;
		this.headers = headers;
	}

	public String getHttpResult() {
		return httpResult;
	}

	public void setHttpResult(String httpResult) {
		this.httpResult = httpResult;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}
}