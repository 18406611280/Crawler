package test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.aft.utils.date.MyDateFormatUtils;
import com.aft.utils.http.MyHttpClientUtil;

public class Testt {
	
	private static int errorAmount = 0;
	
	private static int successAmount = 0;

	public static void main(String[] args) throws Exception {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				String ip = null;
				try {
					String result = MyHttpClientUtil.httpClient("http://1212.ip138.com/ic.asp", MyHttpClientUtil.httpGet,
							null, null, null, "125.64.91.82", 20723, 2 * 1000, 2 * 1000, "GBK");
					if(StringUtils.isEmpty(result)) {
						errorAmount ++;
						return ;
					}
					Document doc = Jsoup.parse(result);
					Element ele = doc.select("body > center").first();
					if(null == ele || !ele.ownText().contains("您的IP是：")) {
						errorAmount ++;
						return ;
					}
					ip = ele.ownText();
					ip = ip.substring(ip.indexOf("[") + 1, ip.lastIndexOf("]"));
					successAmount ++;
				} catch (Exception e) {
					errorAmount ++;
				} finally {
					System.out.println(MyDateFormatUtils.SDF_YYYYMMDDHHMMSS().format(new Date()) + ": successAmount[" + successAmount + "], errorAmount[" + errorAmount + "], ip[" + ip + "]");
				}
			}
		}, 0, 5 * 1000);
	}
}