package com.aft.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.aft.utils.date.MyDateFormatUtils;


public class MyStringUtil {
	
	public static final String fileSeparator = System.getProperty("file.separator");
	
	// 项目根目录
	public final static String contextPath = new File(MyStringUtil.class.getResource("/").getFile()).getParentFile().getParent();
	
	/**
	 * 获取路径或者全名
	 * @param pathOrFileName
	 * @return
	 */
	public static String getFilePath(String... pathOrFileName) {
		if(null == pathOrFileName || pathOrFileName.length < 1) return null;
		StringBuilder sb = new StringBuilder("");
		for(int i=0,len=pathOrFileName.length; i<len; i++) {
			if(StringUtils.isEmpty(pathOrFileName[i])) continue;
			sb.append(pathOrFileName[i]);
			if(i < (len-1) && !sb.toString().endsWith(fileSeparator)) sb.append(fileSeparator);
		}
		return sb.toString();
	}
	
	/**
	 * 获取路径或者全名
	 * @param basePath
	 * @param pathOrFileName
	 * @return
	 */
	public static String getFilePath(String basePath, String[] pathOrFileName) {
		return MyStringUtil.getFilePath(basePath, getFilePath(pathOrFileName));
	}
	
	/**
	 * values 用splice连接 去重复
	 * @param values
	 * @param splice
	 * @return
	 */
	public static String spliceValueNotRepeat(String[] values, String splice) {
		return MyStringUtil.spliceValueNotRepeat(Arrays.asList(values), splice);
	}
	
	/**
	 * 将 values 值 用 splice拼接起来, 忽略 空值 去除重复  eg: value + splice....
	 * @param values
	 * @param splice
	 * @return
	 */
	public static String spliceValueNotRepeat(Collection<String> values, String splice) {
		return MyStringUtil.spliceValue(values, splice, Integer.MAX_VALUE, true);
	}
	
	/**
	 * 将 values 值  用 splice拼接起来, 忽略 空值 保留重复  eg: value + splice....
	 * @param values
	 * @param splice
	 * @return
	 */
	public static String spliceValueRepeat(Collection<String> values, String splice) {
		return MyStringUtil.spliceValue(values, splice, Integer.MAX_VALUE, false);
	}
	
	/**
	 * 将 values 值 用 splice拼接起来, 忽略 空值 eg: value + splice....
	 * @param values
	 * @param joinBefore 分割组装前添加
	 * @param splice
	 * @param repeat 是否去重复 true:去除重复,反之保留
	 * @return
	 */
	public static String spliceValue(Collection<String> values, String splice, int spliceAmount, boolean repeat) {
		int len = values.size() >= spliceAmount ? spliceAmount : values.size();
		Iterator<String> it = values.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()) {
			String value = it.next();
			if(StringUtils.isEmpty(value)) continue;
			if(len-- <= 0) break;
			sb.append(value);
			if(it.hasNext()) sb.append(splice);
		}
		return sb.toString();
	}
	
	/**
	 * 24小时内
	 * @param time
	 * @return
	 */
	public static String toHHmmss(long time) {
		return MyDateFormatUtils.SDF_HHMMSS().format(new Date(System.currentTimeMillis() - time - 8*60*60*1000));
	}
	/**
	 * 获取正则表达式的值
	 * @param start
	 * @param end
	 * @param str
	 * @return
	 */
	public static String getValue(String start ,String end ,String str){
		 String stri = null;
		 Pattern par = Pattern.compile(start+"(.*?)"+end);
		 Matcher mat = par.matcher(str);
		 while(mat.find()){  
			   stri = mat.group(1);
      }
		 return stri;
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
	//正则抓取元素
	public static String getValueByPattern(String pattern ,String str){
		 String stri = null;
		 Pattern par = Pattern.compile(pattern);
		 Matcher mat = par.matcher(str);
		 while(mat.find()){  
			   stri = mat.group(1);
     }
		 return stri;
	}
	
	//正则抓取匹配集合值
	public static List<String> getValuesByPattern(String pattern, String result){  
		List<String> list = new ArrayList<String>();  
		Pattern pat = Pattern.compile(pattern);// 匹配的模式  
		Matcher m = pat.matcher(result);  
		while (m.find()) {  
			int i = 1;  
			list.add(m.group(i));  
			i++;  
		}  
		return list;  
	}  
}