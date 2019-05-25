package com.aft.browser.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author chenminghong
 * 抓取工具
 */
public class CrawlerUtil {
	
		//日期加天数
		public static Date addDays(Date date,int days){
		        Calendar c = Calendar.getInstance();  
		        c.setTime(date);  
		        c.add(Calendar.DAY_OF_MONTH,days);// 今天+天数
		        Date newDate = c.getTime();  
		        return newDate;
		}
	
		//正则抓取匹配集合值
		public static List<String> getValueList(String start , String end , String result){  
	        List<String> list = new ArrayList<String>();  
	        Pattern pattern = Pattern.compile(start+"(.*?)"+end);// 匹配的模式  
	        Matcher m = pattern.matcher(result);  
	        while (m.find()) {  
	            int i = 1;  
	            list.add(m.group(i));  
	            i++;  
	        }  
	        return list;  
	    }  
		
		//正则抓取匹配的值
		public static String getValue(String start ,String end ,String result){
			 String stri = null;
			 Pattern par = Pattern.compile(start+"(.*?)"+end);
			 Matcher mat = par.matcher(result);
			 while(mat.find()){  
				   stri = mat.group(1);
	        }
			 return stri;
		}
		
		//日期加小时
		public static Date addHours(Date date, int hours){   
	        Calendar cal = Calendar.getInstance();   
	        cal.setTime(date);   
	        cal.add(Calendar.HOUR, hours);// 24小时制   
	        Date newDate = cal.getTime();   
	        return newDate;   

	    }

}
