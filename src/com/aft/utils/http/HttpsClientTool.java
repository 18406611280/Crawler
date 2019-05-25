package com.aft.utils.http;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import com.aft.crawl.proxy.ProxyUtil;
import com.aft.utils.thread.MyThreadUtils;

@SuppressWarnings("deprecation")
public class HttpsClientTool {

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static CloseableHttpClient getHttpsClient() throws Exception {
		try {
			SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(
					null, new TrustSelfSignedStrategy()).build();
			HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, hostnameVerifier);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslsf).build();

			PoolingHttpClientConnectionManager poolConnManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
			// Increase max total connection to 200
			poolConnManager.setMaxTotal(10);
			// Increase default max connection per route to 20
			poolConnManager.setDefaultMaxPerRoute(20);
			CloseableHttpClient httpClient = HttpClients.custom()
					.setConnectionManager(poolConnManager).build();
			return httpClient;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static CloseableHttpClient getHttpsClientCert() throws Exception  {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() { return null; }

				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

				public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("https", 443, ssf));
			org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager mgr = new org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager(registry);
			return new DefaultHttpClient(mgr, httpClient.getParams());
		} catch (Exception e) {
			throw e;
		}
	}
private static SSLConnectionSocketFactory socketFactory;
	
	//重新验证方法，取消检测SSL
	private static TrustManager manager = new X509TrustManager() {
		
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			// TODO Auto-generated method stub
			
		}
	};
	
	//调用SSL
	private static void enableSSL(){
		try {
			HostnameVerifier hostnameVerifierAllowAll = new HostnameVerifier() {
	            @Override
	            public boolean verify(String name, SSLSession session) {
	                return true;
	            }
	        };
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[]{manager}, new SecureRandom());
//			socketFactory = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
			socketFactory = new SSLConnectionSocketFactory(context,new String[] {"SSLv2Hello","SSLv3","TLSv1","TLSv1.1","TLSv1.2"},null,hostnameVerifierAllowAll);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public static CloseableHttpClient getHttpsClient(String proxyIp,Integer proxyPort) throws Exception  {
		try {
			enableSSL();
			RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setCookieSpec(CookieSpecs.STANDARD_STRICT)
					.setExpectContinueEnabled(true)
					.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM,AuthSchemes.DIGEST))
					.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", socketFactory)
					.build();
			PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			RequestConfig requestConfig = null;
			if(null != proxyIp && null != proxyPort) {
				requestConfig = RequestConfig.copy(defaultRequestConfig)
						.setSocketTimeout(MyHttpClientUtil.defaultSoTimeout)
		                .setConnectTimeout(MyHttpClientUtil.defaultConnTimeout)
		                .setProxy(new HttpHost(proxyIp, proxyPort))
		                .build();
			}else {
				requestConfig = RequestConfig.copy(defaultRequestConfig)
		                .setSocketTimeout(MyHttpClientUtil.defaultSoTimeout)
		                .setConnectTimeout(MyHttpClientUtil.defaultConnTimeout)
		                .build();
			}
			CloseableHttpClient httpsClient = HttpClients.custom()
					.setConnectionManager(connectionManager)
					.setDefaultRequestConfig(requestConfig)
					.setSSLSocketFactory(socketFactory)
					.build();
			return httpsClient;
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * 获取带有线程IP的httpclient
	 * @return
	 * @throws Exception 
	 */
	public static CloseableHttpClient getHttpsClient(String threadMark) throws Exception  {
		if(threadMark!=null){
			Object[] proxyInfos = ProxyUtil.getProxyInfo(threadMark);
			if(null == proxyInfos || proxyInfos[0]==null) {
				synchronized (threadMark) {
					while(null == proxyInfos || proxyInfos[0]==null) {
						proxyInfos = ProxyUtil.getProxyInfo(threadMark);
						MyThreadUtils.sleep(500);
					}
				}
			}
			return getHttpsClient(proxyInfos[0].toString(), Integer.parseInt(proxyInfos[1].toString()));
		}
		return getHttpsClient(null,null);
	}
}