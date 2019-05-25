package test.https;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.aft.utils.http.MyHttpClientUtil;

/* 
 * 利用HttpClient进行post请求的工具类 
 */  
public class HttpClientUtil {  
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
    private class MyHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}

	}
    
    public static String doPost(String url,Map<String,Object> paramMap,Map<String,Object> headerMap,String proxyIp,Integer proxyPort,String charset){  
        HttpClient httpClient = null;  
        HttpPost httpPost = null;  
        String result = null;  
        try{
//        	Security.addProvider(new BouncyCastleProvider());
            httpClient = new SSLClient();
            
            httpPost = new HttpPost(url);  
            //设置参数  
            List<NameValuePair> list = new ArrayList<NameValuePair>();  
            Iterator iterator = paramMap.entrySet().iterator();  
            while(iterator.hasNext()){  
                Entry<String,String> elem = (Entry<String, String>) iterator.next();  
                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));  
            }  
            if(list.size() > 0){  
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);  
                httpPost.setEntity(entity);  
            }
            Header[] headers = null;
            if(null != headerMap && !headerMap.isEmpty()) headers = MyHttpClientUtil.toHeaders(headerMap);
            if(null != headers) httpPost.setHeaders(headers);
            if(null != proxyIp && null != proxyPort){
            	httpPost.setConfig(RequestConfig.custom().setProxy(new HttpHost(proxyIp, proxyPort)).build());
            }
            HttpResponse response = httpClient.execute(httpPost);  
            if(response != null){  
                HttpEntity resEntity = response.getEntity();  
                if(resEntity != null){  
                    result = EntityUtils.toString(resEntity,charset);  
                }  
            }  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
        return result;  
    } 
    public String doGet(String url,String proxyIp,Integer proxyPort){  
    	StringBuffer sb = new StringBuffer();
    	//创建HttpClient实例
    	HttpClient client = getHttpClient(proxyIp,proxyPort);
    	//创建httpGet
    	HttpGet httpGet = new HttpGet(url);
    	//执行
    	try {
    		HttpResponse response = client.execute(httpGet);

    		HttpEntity entry = response.getEntity();

    		if(entry != null)
    		{
    			InputStreamReader is = new InputStreamReader(entry.getContent());
    			BufferedReader br = new BufferedReader(is);
    			String str = null;
    			while((str = br.readLine()) != null)
    			{
    				sb.append(str.trim());
    			}
    			br.close();
    		}
    	} catch (ClientProtocolException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	System.out.println(sb.toString());
    	return sb.toString();
    }

    //设置代理
    public static HttpClient getHttpClient(String proxyIp,Integer proxyPort) {

    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	HttpHost proxy = new HttpHost(proxyIp,proxyPort);
    	httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
    	return httpClient;
    }
    public String GetWithoutCA(String Url,String proxyIp, Integer proxyPort) {
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
			
			urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0"); 
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
}  
