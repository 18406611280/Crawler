package com.aft.utils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aft.crawl.crawler.Crawler;
import com.aft.utils.cmh.HttpUtil;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
public class CurrencyUtil {
	public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static String userAgent =  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
 
    //配置您申请的KEY
    public static final String APPKEY ="a3ea017c17a2ee6672febdb206b1db98";
 
    //1.常用汇率查询
    public static void getRequest1(){
        String result =null;
        String url ="http://op.juhe.cn/onebox/exchange/query";//请求接口地址
        Map params = new HashMap();//请求参数
            params.put("key",APPKEY);//应用APPKEY(应用详细页查询)
 
        try {
            result =net(url, params, "GET");
            JSONObject object = JSONObject.fromObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    //2.货币列表
    public static void getRequest2(){
        String result =null;
        String url ="http://op.juhe.cn/onebox/exchange/list";//请求接口地址
        Map params = new HashMap();//请求参数
            params.put("key",APPKEY);//应用APPKEY(应用详细页查询)
 
        try {
            result =net(url, params, "GET");
            JSONObject object = JSONObject.fromObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    //3.实时汇率查询换算
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static BigDecimal getRequest3(String currencyF,String currencyT){
    	BigDecimal rate  = new BigDecimal(0);
    	try {
        String result =null;
        String url ="http://op.juhe.cn/onebox/exchange/currency";//请求接口地址
        Map params = new HashMap();//请求参数
            params.put("from",currencyF);//转换汇率前的货币代码
            params.put("to",currencyT);//转换汇率成的货币代码
            params.put("key",APPKEY);//应用APPKEY(应用详细页查询)
            result =net(url, params, "GET");
            JSONObject object = JSONObject.fromObject(result);
            if(object.getInt("error_code")==0){
                JSONArray ja = object.getJSONArray("result");
                for(int i=0;i<ja.size();i++){
                	JSONObject jo = ja.getJSONObject(i);
                	if(currencyF.equals(jo.get("currencyF").toString())){
                		rate = new BigDecimal(jo.get("result").toString()).setScale(4,BigDecimal.ROUND_HALF_UP);
                		break;
                	}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rate;
    }
    
  //4.实时汇率查询换算
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void putRate(Map<String,BigDecimal> rateMap){
//    	BigDecimal rate  = rateMap.get(currencyF);
//    	if(rate==null){
	    	try {
	        String result =null;
	        String url ="http://hl.anseo.cn/";//请求接口地址
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	        headers.put("Accept-Encoding", "gzip, deflate");
	        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
	        headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");
	        headers.put("Upgrade-Insecure-Requests","1");
	        headers.put("Connection", "keep-alive");
	        headers.put("Host", "hl.anseo.cn");
	        result = HttpUtil.doGet(url, headers, null);
	        Document document = Jsoup.parse(result);
			Element inverse = document.getElementById("inverse");
			Elements li = inverse.getElementsByTag("li");
			int liSize = li.size();
			for(int l = 0; l<liSize; l++){
				Element liEle = li.get(l);
				String text = liEle.text();
				String cf = MyStringUtil.getValue("\\(", "\\)", text).trim();
				String ct = MyStringUtil.getValue("\\=", "人民币", text).trim();
				rateMap.put(cf, new BigDecimal(ct));
			}
	        } catch (Exception e) {
	            e.printStackTrace();
	            return;
//	            return new BigDecimal(0);
	        }
//    	}
//    	rate  = rateMap.get(currencyF);
//    	if(rate==null) rate = new BigDecimal(0);
//        return rate;
    }
 
 
 
    public static void main(String[] args) {
    	System.out.println(getRequest3("BND","CNY"));
    }
 
    /**
     *
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return  网络请求字符串
     * @throws Exception
     */
    public static String net(String strUrl, Map params,String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if(method==null || method.equals("GET")){
            	if(params!=null){
            		strUrl = strUrl+"?"+urlencode(params);
            	}
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if(method==null || method.equals("GET")){
                conn.setRequestMethod("GET");
            }else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params!= null && method.equals("POST")) {
                try {
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                        out.writeBytes(urlencode(params));
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }
 
    //将map型转为请求参数型
    public static String urlencode(Map<String,Object>data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
