package test.https;

import java.util.HashMap;  
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;

import com.aft.utils.http.HttpsClientTool;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;  
//对接口进行测试  
public class TestMain {  
    private String url = "https://aio.hkairlines.com/ac3s/flight/query";  
    private String charset = "utf-8";  
    private HttpClientUtil httpClientUtil = null;  
      
    public TestMain(){  
        httpClientUtil = new HttpClientUtil();  
    }  
      
    public void test(){  
        Map<String,String> createMap = new HashMap<String,String>();  
        createMap.put("bi.ctype","app");  
        createMap.put("bi.av","3.3.0");  	
        createMap.put("orgCity","HKG");  
        createMap.put("dstCity","PEK");  
        createMap.put("tripType","OW");  
        createMap.put("takeoffDate","2017-01-13");  
        createMap.put("returnDate","2017-01-16");  
        createMap.put("seatClass","E");  
        createMap.put("adultNum","1");  
        createMap.put("childNum","0");  
        createMap.put("infantNum","0");  	
        
        createMap.put("sign","B74g46zIthfAedeonOdlFi99imY=");  	
        createMap.put("bi.sid","d71dccf8d950f22fd230ecf5287c41ef");  	
//        String httpOrgCreateTestRtn = httpClientUtil.doPost(url,createMap,charset);  
//        System.out.println("result:"+httpOrgCreateTestRtn);  
    }  
      
    public static void main(String[] args){  
//        TestMain main = new TestMain();  
//        main.test();  
    	HttpClientUtil httpClientUtil = new HttpClientUtil();
    	String result = httpClientUtil.doGet("https://beta.cebupacificair.com/Flight/InternalSelect?o1=MNL&d1=TAG&o2=&d2=&dd1=2017-03-05&ADT=3&CHD=0&INF=0&s=true&mon=true", "221.10.170.70", 20792);
    	System.out.println(result);
    }  
}  