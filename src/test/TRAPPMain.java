package test;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aft.utils.file.MyFileUtils;

public class TRAPPMain {

	public static void main(String args[]){
		tigerFlyRun();
		
	}
	
	public static void tigerFlyRun(){
		
//		RequestSession req = new RequestSession(Constants.getSessionUrl, Constants.Authorization);
//		String result = req.request(null);
//		
//		System.out.println(result);
//		System.out.println(TokenParser.parse(result));
//		
//		String url = Constants.bookingUrl;
//		String sig = TokenParser.parse(result);
//		
//		SSLPost post = new SSLPost(url, Constants.Authorization, Constants.SOAPGetAvailability);
//		
//		String curr = null;
//		GetAvailabilityEntity entity = new GetAvailabilityEntity(sig, "HKG", "KUL", "2016-12-28", "2016-12-28", curr);
//		String e = entity.getEntity();
//		String baos = post.request( e);
//		
//		System.out.print(baos);
		
		
		try {
			String content = new String(MyFileUtils.getByteArrayByFile("C:\\Users\\thinkpad\\Desktop\\HU航司抓取分析.txt"),"UTF-8");
			
			String regex = "";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(content);
			while(matcher.find()){
				System.out.println(matcher.group());
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
