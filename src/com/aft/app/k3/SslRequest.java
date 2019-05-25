package com.aft.app.k3;

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

public class SslRequest {
	
	
	public String GetWithoutCA(String Url, String cookie) {
		return this.GetWithoutCA(Url, cookie, null, null);
				
	}
	
	public String GetWithoutCA(String Url, String cookie, String proxyIp, Integer proxyPort) {
		String result = "";
		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { new MyTrustManager() },
					new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new MyHostnameVerifier());

			URL url = new URL(Url);
			
			if(null != proxyIp) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort));
				urlConnection = (HttpURLConnection) url.openConnection(proxy);
			} else urlConnection = (HttpURLConnection) url.openConnection();
			
			urlConnection.setRequestProperty("Cookie", cookie); 
			urlConnection.setConnectTimeout(60000);
			urlConnection.setReadTimeout(60000);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false); 
			InputStream in = urlConnection.getInputStream();
			// ȡ������������ʹ��Reader��ȡ
			reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			/*
			System.out.println("=============================");
			System.out.println("Contents of get request");
			System.out.println("=============================");
			*/
			String lines;
			while ((lines = reader.readLine()) != null) {
				//System.out.println(lines);
				result += lines;
			}
			
			
			/*
			System.out.println("=============================");
			System.out.println("Contents of get request ends");
			System.out.println("=============================");
			*/
			
		} catch (MalformedURLException e) {		
			e.printStackTrace();
		} catch (IOException e) {	
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			urlConnection.disconnect();
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
}
