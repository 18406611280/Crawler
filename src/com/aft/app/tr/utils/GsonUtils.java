package com.aft.app.tr.utils;

import java.lang.reflect.Type;

import com.google.gson.Gson;

//GSON �ӿ�
public class GsonUtils {
	
	private static Gson gson;
	
	static {
		gson = new Gson();
	}

	public static Object fromJson(String json, Class obj){
		return gson.fromJson(json, obj);
	}
	
	public static Object toJson(Object json){
		return gson.toJson(json);
	}
	
	public static Object fromJson(String json, Type t){
		return gson.fromJson(json, t);
	}
}
