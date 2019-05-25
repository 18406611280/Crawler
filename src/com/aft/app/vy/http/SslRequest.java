package com.aft.app.vy.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public  class SslRequest {
	
	public String url;
	
	public SslRequest(String URL){
		url = URL;
	}
	
	public String Get() {
		String result = "";
		
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new MyTrustManager() },
					new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new MyHostnameVerifier());

			URL UrlObj = new URL(url);	
			HttpURLConnection urlConnection = (HttpURLConnection) UrlObj
					.openConnection();
			
			InputStream in = urlConnection.getInputStream();			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			
			String lines;
			while ((lines = reader.readLine()) != null) {
				result += lines;
			}
			reader.close();
			urlConnection.disconnect();
					
		} catch (MalformedURLException e) {		
			e.printStackTrace();
		} catch (IOException e) {	
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	private class MyHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	private class MyTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)

		throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
	
	public HttpURLConnection getSSLConnection(String Url){
		
		HttpURLConnection urlConnection = null;
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new MyTrustManager() },
					new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new MyHostnameVerifier());

			URL url = new URL(Url);
			
			//���ô���
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("124.161.189.47", 12079));
			//urlConnection = (HttpURLConnection) url.openConnection(proxy);

		    urlConnection = (HttpURLConnection) url.openConnection();			
					
		} catch (MalformedURLException e) {		
			e.printStackTrace();
		} catch (IOException e) {	
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		return urlConnection;
	}
	
	
	public  String Post( String content){
		
		String result = null;		
		try {	
			
			HttpURLConnection conn = getSSLConnection(url);	
			//setHeader(conn);
			//conn.setRequestProperty("X-ink-mac", "z50kWO0549XydImPm3qS7fzsvSoGI+aurUo/fToSrlc=");
			//conn.setRequestProperty("X-ink-temporal",  "MjAxNi0xMi0yMlQwNjo0MzowNy43OTI=");
			conn.setRequestProperty("User-Agent", "Android:1.2.71");
			
			conn.setRequestProperty("Content-Type",  "application/json; charset=utf-8" );
			conn.setRequestProperty("Content-Length",  "" + content.getBytes().length );
			conn.setRequestMethod("POST");			
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
	};
	
	public void setHeader(HttpURLConnection conn){		
		conn.setRequestProperty("X-ink-mac", "z50kWO0549XydImPm3qS7fzsvSoGI+aurUo/fToSrlc=");
		conn.setRequestProperty("X-ink-temporal",  "MjAxNi0xMi0yMlQwNjo0MzowNy43OTI=");
		conn.setRequestProperty("User-Agent", "Android:1.2.71");
	}

}
