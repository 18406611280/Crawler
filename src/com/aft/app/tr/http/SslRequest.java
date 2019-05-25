package com.aft.app.tr.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public abstract class SslRequest {
	
	public static String getSessionUrl = "https://tigerair.themobilelife.com/api/v2/session/login?platform=android";
	
	public String GetWithoutCA(String Url) {
		String result = "";
		
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new MyTrustManager() },
					new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new MyHostnameVerifier());

			URL url = new URL(Url);
			
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("124.161.189.47", 12079));
			//urlConnection = (HttpURLConnection) url.openConnection(proxy);

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			
			urlConnection.setRequestProperty("Authorization",  "Basic dGlnZXJhaXI6anNwZ0NZcDR0eGdUMnBObQ==");

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
	
	
	public abstract String request( String content);

}
