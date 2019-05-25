package com.aft.app.tz.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

public class SSLPost extends SslRequest {
	
	public String soapAction; 
	public String url;
	public String authorization;
	
	public SSLPost(String Url, String auth, String soapAct){
		soapAction = soapAct;
		authorization = auth;
		url = Url;
		
	}
	
	public HttpURLConnection getSSLConnection(String url, String soapAction, String auth){
		HttpURLConnection conn = super.getSSLConnection(url);
		conn.setRequestProperty("Authorization",  auth);
		conn.setRequestProperty("SOAPAction",  soapAction);
		return conn;
	}
	
	@Override
	public String request( String content) {
		// TODO Auto-generated method stub
		String result = null;
				
		try {		
			HttpURLConnection conn = getSSLConnection(url, soapAction, authorization);
			//conn.setRequestProperty("Authorization",  auth);
			conn.setRequestProperty("Content-Type",  "text/xml; charset=utf-8" );
			conn.setRequestProperty("Content-Length",  "" + content.getBytes().length );
			conn.setRequestMethod("POST");
			//conn.setRequestProperty("SOAPAction", "http://schemas.navitaire.com/WebServices/IBookingManager/UpdatePassengers");
			//conn.setRequestProperty("SOAPAction", "http://schemas.navitaire.com/WebServices/IBookingManager/GetAvailability");
			conn.setDoOutput(true);   
		    conn.setDoInput(true);   
		    conn.setUseCaches(false);  
			
			conn.connect();
			OutputStream out = conn.getOutputStream();
			out.write(content.getBytes());
			out.flush();
			out.close();
			
			int resCode = conn.getResponseCode();
			
			if (200 == resCode){
				StringBuffer sb=new StringBuffer();
			    String readLine=new String();
			    BufferedReader responseReader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			    while((readLine=responseReader.readLine())!=null){
			       sb.append(readLine);
			    }
			    responseReader.close();
			    result = sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
