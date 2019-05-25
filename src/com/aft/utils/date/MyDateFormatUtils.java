package com.aft.utils.date;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


/**
 * 日期格式化辅助
 * @author Administrator
 *
 */
public class MyDateFormatUtils {
	
	private static final ThreadLocal<Map<String, DateFormat>> threadLocal = new ThreadLocal<Map<String, DateFormat>>();
	
	/**
	 * 格式化(yyyy-MM-dd-HH)
	 * @return
	 */
	public static DateFormat SDF_YYYYMMDDHH_1() {
		return getDateFormat("yyyy-MM-dd-HH");
	}
	
	/**
	 * 格式化(yyyyMMddHHmm)
	 * @return
	 */
	public static DateFormat SDF_YYYYMMDDHHMM_1() {
		return getDateFormat("yyyyMMddHHmm");
	}
	
	
	/**
	 * 格式化(yyyy-MM-dd HH:mm:ss)
	 * @return
	 */
	public static DateFormat SDF_YYYYMMDDHHMMSS() {
		return getDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 格式化(yyyy-MM-dd HH:mm:ss SSS)
	 * @return
	 */
	public static DateFormat SDF_YYYYMMDDHHMMSSSSS() {
		return getDateFormat("yyyy-MM-dd HH:mm:ss SSS");
	}
	
	/**
	 * 格式化(HH:mm:ss)
	 * @return
	 */
	public static DateFormat SDF_HHMMSS() {
		return getDateFormat("HH:mm:ss");
	}
	
	/**
	 * 格式化(HH:mm)
	 * @return
	 */
	public static DateFormat SDF_HHMM() {
		return getDateFormat("HH:mm");
	}
	
	/**
	 * 格式化(H:mm)
	 * @return
	 */
	public static DateFormat SDF_HMM() {
		return getDateFormat("H:mm");
	}
	
	/**
	 * 格式化(H:m)
	 * @return
	 */
	public static DateFormat SDF_HM() {
		return getDateFormat("H:m");
	}
	
	/**
	 * 格式化(yyyy-MM-dd)
	 * @return
	 */
	public static DateFormat SDF_YYYYMMDD() {
		return getDateFormat("yyyy-MM-dd");
	}
	
	/**
	 * 格式化(yyyyMMdd)
	 * @return
	 */
	public static DateFormat SDF_YYYYMMDD_1() {
		return getDateFormat("yyyyMMdd");
	}
	
	/**
	 * 格式化(EEE, d MMM yyyy HH:mm:ss Z)
	 * @param locale 有且只能一个
	 * @return
	 */
	public static DateFormat SDF_EEEdMMMYYYYMMDDHHMMSSZ(Locale... locale) {
		return getDateFormat("EEE, d MMM yyyy HH:mm:ss Z", locale);
	}
	
	/**
	 * 增加/减少天数,并格式化 (yyyy-MM-dd)
	 * @param now 
	 * @param date 
	 * @param day 增加/减少的天数
	 * @return
	 */
	public static final String dealDayAndFormat(Date date, int day) {
		return dealDayAndFormat(date, day, null);
	}
	
	/**
	 * 增加/减少天数
	 * @param date 
	 * @param day 增加/减少的天数
	 * @param format 格式化格式 
	 * @return
	 */
	public static final String dealDayAndFormat(Date date, int day, String format) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, day);
		return StringUtils.isEmpty(format) ? SDF_YYYYMMDD().format(calendar.getTime()) : new SimpleDateFormat(format).format(calendar.getTime());
	}
	
	/**
	 * 格式化
	 * @param key
	 * @return
	 */
	public static DateFormat getDateFormat(String key, Locale... locale) {
		Map<String, DateFormat> dfMap = threadLocal.get();
		if(null == dfMap) dfMap = new HashMap<String, DateFormat>();
		if(null == dfMap.get(key)) {
			if(null != locale && 0 != locale.length) dfMap.put(key, new SimpleDateFormat(key, locale[0]));
			else dfMap.put(key, new SimpleDateFormat(key));
		}
		return dfMap.get(key);
	}
	/**
	 * 获取航班到达日期
	 * 
	 * @return
	 */
	public static String getArrivalDate(Date goDate,String depTime,String arrTime){
		depTime = depTime.replace(":","");
		arrTime = arrTime.replace(":","");
		int dtime = Integer.parseInt(depTime);
		int atime = Integer.parseInt(arrTime);
		String arr1 = arrTime.substring(0, 2);//截取小时
		String arr2 = arrTime.substring(2, 4);//截取分钟
		Format f = new SimpleDateFormat("yyyy-MM-dd");
		if(dtime > atime){
			Calendar c = Calendar.getInstance();  
		    c.setTime(goDate);  
		    c.add(Calendar.DAY_OF_MONTH, 1);// +1天  
		    Date tomorrow = c.getTime();
		    String tomTime = f.format(tomorrow);
		    String arrivalTime = tomTime+" "+arr1+":"+arr2;// "yyyy-MM-dd HH:mm:ss"
		    return arrivalTime;
		}else{
			String dayTime = f.format(goDate);
			String arrivalTime = dayTime+" "+arr1+":"+arr2;
			return arrivalTime;
		}
	}
}