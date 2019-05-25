package com.aft.app.tr.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class RequestSession extends SslRequest{
	
	String url;
	String authorization;
	
	public RequestSession(String Url, String auth){
		url = Url;
		authorization = auth;
	}

	//public static final String url = "https://tigerair.themobilelife.com/api/v2/session/login?platform=android"; // tigerflight
	//public static final String url = "https://scootapi.themobilelife.com/api/v2/session/login?platform=android"; // scootfly
	@Override
	public String request(String content) {
		String result = null;
		try{
			// TODO Auto-generated method stub
			
		
			HttpURLConnection conn = getSSLConnection(url);
			//if (conn == null ) return null;
			//conn.setRequestProperty("Authorization",  "Basic dGlnZXJhaXI6anNwZ0NZcDR0eGdUMnBObQ=="); //tigerFlight
			//conn.setRequestProperty("Authorization", "Basic c2Nvb3Q6cXlRN3VOdmVGejlmTGlSaw=="); //scootFly
			
			conn.setRequestProperty("Authorization",  authorization);
		
			InputStream in = conn.getInputStream();
			// ȡ������������ʹ��Reader��ȡ
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
			String lines;
			while ((lines = reader.readLine()) != null) {
				result += lines;
			}
			reader.close();
			// �Ͽ�����
			conn.disconnect();
				
		} catch (MalformedURLException e) {		
			e.printStackTrace();
		} catch (IOException e) {	
			e.printStackTrace();
		}		
		return result;
	}
}
