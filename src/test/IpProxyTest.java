package test;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;

import com.aft.utils.http.MyHttpClientUtil;
import com.aft.utils.jackson.MyJsonTransformUtil;

public class IpProxyTest {
	
	private final static String proxysUrl = "http://183.63.110.202:5000/ipProxy/all.action";
	
	// {"success":true,"msg":["124.161.189.47:12089","221.10.90.46:12116","124.161.189.47:12112","221.10.90.131:12146","221.10.90.131:12083","119.5.44.20:12046","124.161.189.47:12071","221.10.170.91:21002","221.10.170.70:20272","221.10.170.70:20792","221.10.170.70:20702","125.64.91.82:20723","125.64.91.85:21093","221.10.90.131:12053","221.10.170.70:20802","125.64.91.90:20191","221.10.90.46:12143","125.64.91.110:21163","221.10.101.103:20591","119.5.153.47:12035","124.161.189.47:12107","221.10.205.22:20091","124.161.179.65:12030","119.5.153.47:12135","221.10.205.20:20161","125.64.91.110:20773","221.10.205.22:21041","124.161.179.65:12044","125.64.91.83:21543","119.5.153.47:12156","125.64.91.87:20703","124.161.179.60:12103","124.161.189.47:12086","221.10.137.8:20066","118.119.102.35:20220","118.119.102.37:20282","118.119.102.36:20038","118.119.102.37:20244","221.10.137.8:20004","118.119.102.37:20276","221.10.137.8:20002","221.10.137.8:20280","221.10.137.8:20282","221.10.137.8:20284","221.10.137.8:20304","171.221.203.128:12089","171.221.203.130:12141","171.221.203.130:12140","125.64.91.84:21403","119.5.44.20:12078","119.7.44.182:12052","124.161.189.47:12079","119.5.44.20:12091","119.5.44.20:12042","119.5.44.20:12059","171.221.203.130:12110","119.5.44.20:12058","221.10.137.8:20272"]}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		String result = MyHttpClientUtil.get(proxysUrl);
		Map<String, Object> resultMap = MyJsonTransformUtil.readValue(result, Map.class);
		List<String> list = (List<String>)resultMap.get("msg");
		for(final String l : list) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String[] ps = l.split(":");
						String msg = MyHttpClientUtil.httpClient("http://www.baidu.com", MyHttpClientUtil.httpGet, null, null, ps[0], Integer.parseInt(ps[1]));
						if(StringUtils.isEmpty(msg)) System.out.println("error:" + l);
						else {
							System.out.println("success:" + l + "\t" + Jsoup.parse(msg).title());
						}
					} catch (Exception e) {
						System.out.println("error:" + l);
					}
				}
			}).start();
		}
	}
}