package com.aft.app.tz.ScootFly;

import com.aft.app.tz.http.RequestSession;

public class GetSession {
	public RequestSession req;
	public String url ;
	public String auth;
	
	

	public  GetSession(RequestSession request ){
		req = request;
		//url = "https://scootapi.themobilelife.com/api/v2/session/login?platform=android";
		url = Constants.getSessionUrl;
		//auth = "Basic c2Nvb3Q6cXlRN3VOdmVGejlmTGlSaw==";
		auth = Constants.Authorization;
	}

	//public String request(){
	//	return req.request(url, auth, null);
	//}
}
