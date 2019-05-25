package com.aft.crawl.crawler.impl.b2c;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.utils.http.MyHttpClientResultVo;
import com.aft.utils.http.MyHttpClientSession;
import com.aft.utils.http.MyHttpClientSessionVo;
import com.aft.utils.http.MyHttpClientUtil;

/**
 * 海南航空 
 */
public class B2CHUInternationalCrawler extends Crawler {

//	private final static String queryUrl = "http://et.hnair.com/huet/bc10_av.do?orgID=HUAIRNEW&queryModel=mixquery&tripType=ONEWAY&orgCity=%depCode%&dstCity=%desCode%&takeoffDate=%depDate%";
//	private final static String queryUrl = "http://new.hnair.com/hainanair/ibe/deeplink/ancillary.do?DD1=2017-11-06&DD2=&TA=1&TC=0&TI=&ORI=CAN&DES=HAK&SC=Y&ICS=F&PT=F&FLC=1&NOR=&PACK=T&redirected=true";
	private final static String queryUrl  = "http://new.hnair.com/hainanair/ibe/deeplink/ancillary.do?DD1=%depDate%&DD2=&TA=1&TC=0&TI=&ORI=%depCode%&DES=%desCode%&SC=Y&ICS=F&PT=F&FLC=1&NOR=&PACK=T&redirected=true";
	private final static String post2Url  = "http://new.hnair.com/hainanair/ibe/common/processSearch.do";
	private final static String post3Url  = "http://new.hnair.com/hainanair/ibe/air/searchResults.do";
	private final static Pattern pattern = Pattern.compile("\\((.+)\\)");
	
	public B2CHUInternationalCrawler(String threadMark) {
		super(threadMark);
	}
	
	@Override
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		if(StringUtils.isEmpty(httpResult)) {
			logger.info(this.getJobDetail().toStr() + ", 不存在航班信息:" + httpResult);
			return crawlResults;
		}
		try {
			List<String> results = new ArrayList<String>();
			results = getStringList("AirlineCarrierCN", "Flights[position]", httpResult);
			if(results.isEmpty()||null == results) {
				logger.info(this.getJobDetail().toStr() + ", 不存在航班信息;");
				return crawlResults;
			}
			CrawlResultB2C crawlResult = null;
			//先试试原来的crawlResult保存数据国际的是否有问题
//			int i= 0;
		airline:for(String str:results){
				List<String> prices = new ArrayList<String>();
				String AirlineCarrierEN = getElementByPattern("flightInfo.AirlineCarrierEN",str);//航司
				String FlightNumber = getElementByPattern("flightInfo.FlightNumber",str);//航班号 
				String DepartureIATA = getElementByPattern("flightInfo.DepartureIATA",str);//出发地 
				String ArrivalIATA = getElementByPattern("flightInfo.ArrivalIATA",str);//到达地 
				String DepartureDate = getElementByPattern("flightInfo.DepartureDate",str);//出发日期
				String ArrivalDate = getElementByPattern("flightInfo.ArrivalDate",str);//出发日期
				String DepartureTime = getElementByPattern("flightInfo.DepartureTime",str);//出发时间 
				String ArrivalTime = getElementByPattern("flightInfo.ArrivalTime",str);//到达时间 
				String seatNum = getElementByPattern("flightInfo.seatNum",str);//到达时间 
				prices = getStringList("fareinfo.CabinType", "Prices[fareFamilyCode]", str);
			/*	FlightData flightData = new FlightData(this.getJobDetail(), "OW", DepartureIATA, ArrivalIATA, ArrivalDate);
				List<FlightSegment> flightSegmentList = new ArrayList<FlightSegment>();
				FlightSegment flightSegment = new FlightSegment();
				flightSegment.setDepartureTerminal("");
				flightSegment.setTripNo(i++);//航段
				flightSegment.setAirlineCode(AirlineCarrierEN);//航司
				flightSegment.setFlightNumber(FlightNumber);//航班号
				flightSegment.setDepAirport(DepartureIATA);// 出发地
				flightSegment.setArrAirport(ArrivalIATA);//到达地
				flightSegment.setDepTime(ArrivalDate);//起飞时间2017-04-01 07:40:00
				flightSegment.setArrTime(ArrivalTime);//到达时间
*/				
				for(String price:prices){
					String cabin = getElementByPattern("fareinfo.CabinType",price);//舱位
					String TotalFare = getElementByPattern("fareinfo.BaseAmount",price);//票面价
					if("0".equals(TotalFare)){
						break airline;
					}
					String CNTax = getElementByPattern("fareinfo.CNTax",price);//税费
					// 判断共享
					String shareFlight = this.getShareFlight(AirlineCarrierEN);

					crawlResult = new CrawlResultB2C(this.getJobDetail(), AirlineCarrierEN, FlightNumber, shareFlight, DepartureIATA, ArrivalIATA, DepartureDate, cabin);
					crawlResult.setDepTime(DepartureTime);	// 出发时间
					crawlResult.setDesTime(ArrivalTime);	// 到达时间
					crawlResult.setEndDate(ArrivalDate);//到达日期
					crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(seatNum));		// 默认
					crawlResult.setTicketPrice(new BigDecimal(TotalFare));
					if("".equals(CNTax)){
						CNTax = "0";
					}
					crawlResult.setSalePrice(new BigDecimal(CNTax));
					crawlResults.add(crawlResult);
				}
			}

		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
		protected boolean needToChangeIp(String httpResult, Document document, Object jsonObject, String returnType)
				throws Exception {
			if(httpResult.contains("抱歉，遇到些问题"))return true;
			return super.needToChangeIp(httpResult, document, jsonObject, returnType);
		}
	
	public String httpResult() throws Exception{
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		MyHttpClientSession httpClientSession = new MyHttpClientSession(httpClient);
		MyHttpClientSessionVo httpClientSessionVo = new MyHttpClientSessionVo(httpClientSession);
		
		Map<String, Object> headerMap = new HashMap<String, Object>();
		httpClientSessionVo.setHeaderMap(headerMap);
		
		String url = queryUrl.replace("%depCode%", this.getJobDetail().getDepCode())
				.replace("%desCode%", this.getJobDetail().getDesCode())
				.replace("%depDate%", this.getJobDetail().getDepDate());
		
		StringBuilder cookie = new StringBuilder().append("sk_ql=http://www.hnair.com/");
		
		headerMap.put("Host", "new.hnair.com");
		headerMap.put("Connection", "keep-alive");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
		headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headerMap.put("Referer", "http://www.hnair.com/");
		headerMap.put("Accept-Encoding", "gzip, deflate");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
		headerMap.put("Cookie", cookie);
		
		//第一次请求
		MyHttpClientResultVo httpVo = this.httpProxyResultVoGet(httpClientSessionVo, url.replaceAll("&redirected=true", ""), "html");
		
		String sessionId = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "JSESSIONID=");
		String skysales = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Set-Cookie", "Webtrends=");
		
		
		
		cookie.append(";").append(sessionId).append(";").append(skysales);
		
		headerMap.put("Cookie", cookie);
		headerMap.put("Cache-Control", "max-age=0");
		headerMap.put("Origin", "http://new.hnair.com");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		headerMap.put("Referer", url.replaceAll("&redirected=true", ""));
		
		//第二次请求
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, url,"ConversationID=&ENCRYPTED_QUERY=&QUERY=&redirected=true", "other");
		
		String location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		if(location==null) return null;
		headerMap.remove("Content-Type");
		//第三次请求
		String result = this.httpProxyGet(httpClientSessionVo, location, "html");
		
		if(result==null)return null;
		headerMap.put("Referer", location);
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		
		//第四次请求
		httpVo = this.httpProxyResultVoPost(httpClientSessionVo, post2Url, "", "other");
	
		location = MyHttpClientUtil.getHeaderValue(httpVo.getHeaders(), "Location");
		if(location==null) return null;
		
		
		//第五次请求
		result = this.httpProxyPost(httpClientSessionVo, location, "other");
		
		return result;
	}
	
	//通过正则抓取航班信息
	public String getElementByPattern(String ele,String str){
		 String stri = null;
		 Pattern par = Pattern.compile(ele+" = '(.*?)';");
		 Matcher mat = par.matcher(str);
		 List<String> changeList  = new ArrayList<>();
		 while(mat.find()){  
			   stri = mat.group(1);
			   changeList.add(stri);
         }
		 return stri;
	}
	
	//通过正则抓取航班信息
		public List<String> getElementByPatterns(String ele,String str){
			 String stri = null;
			 Pattern par = Pattern.compile(ele+" = '(.*?)';");
			 Matcher mat = par.matcher(str);
			 List<String> changeList  = new ArrayList<>();
			 while(mat.find()){  
				   stri = mat.group(1);
				   changeList.add(stri);
	         }
			 return changeList;
		}
		
		//正则抓去匹配集合
		/*public static List<String> getStringList(String sta,String end,String soap){  
	        List<String> list = new ArrayList<String>();  
	        Pattern pattern = Pattern.compile(sta+"(.*?)"+end);// 匹配的模式  
	        Matcher m = pattern.matcher(soap);  
	        while (m.find()) {  
	            int i = 1;  
	            list.add(m.group(i));  
	            i++;  
	        }  
	        return list;  
	    }  */

	public List<String> getStringList(String start,String end,String str){
	 
	     List<String> results = new ArrayList<String>();
		 Pattern r = Pattern.compile(start);
		 Matcher m = r.matcher(str);
		 int num=0;
		 String strs = "";
		 int one=0;
		 int two=0;
		 int length = str.length();
		 while (m.find()) {
			 num++;
			  one= str.indexOf(start,num+one);
			  two =str.indexOf(end,num+two);
			  if(one<=-1||two<=-1||one>length||two>length||one>two){
				  continue;
			  }else{
				  strs=str.substring(one,two);
			  }
			  results.add(strs);
	    } 
		 return results;
	}
}