package com.aft.utils.cmh;

import org.apache.http.client.methods.CloseableHttpResponse;

public class HttpResponseVo {
	
	private String result;
	private CloseableHttpResponse response;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public CloseableHttpResponse getResponse() {
		return response;
	}
	public void setResponse(CloseableHttpResponse response) {
		this.response = response;
	}
	
	

}
