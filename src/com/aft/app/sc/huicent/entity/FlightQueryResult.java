package com.aft.app.sc.huicent.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.aft.utils.jackson.MyJsonTransformUtil;

@SuppressWarnings({"unused", "unchecked"})
public class FlightQueryResult implements Serializable {
	private static final long serialVersionUID = -2956164276643488313L;
	public String a;
    public String b;
    public String c;
    public String d;
    public String e;
    public String f;
    public int g;
    public ArrayList h;
    public ArrayList i;
    public ArrayList j;
    public String k;
    public String depDate;
    public String status;
    public String errorMsg;
    
    public FlightQueryResult() {
        super();
        this.k = "false";
    }
    
    public void a(String arg1) {
        this.a = arg1;
    }

    public void a(int arg1) {
        this.g = arg1;
    }

    public void a(ArrayList arg1) {
        this.h = arg1;
    }

    public String a() {
        return this.d;
    }

    public void b(String arg1) {
        this.b = arg1;
    }

    public void b(ArrayList arg1) {
        this.i = arg1;
    }

    public String b() {
        return this.e;
    }

    public void c(String arg1) {
        this.c = arg1;
    }

    public void c(ArrayList arg1) {
        this.j = arg1;
    }

    public ArrayList c() {
        return this.h;
    }

    public void d(String arg1) {
        this.d = arg1;
    }

    public ArrayList d() {
        return this.i;
    }

    public int describeContents() {
        return 0;
    }

    public void e(String arg1) {
        this.e = arg1;
    }

    public String e_getResultFlag() {
        return this.k;
    }

    public void f(String arg1) {
        this.f = arg1;
    }

    public void g(String arg1) {
        this.k = arg1;
    }
    
    public String toString() {
    	return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
    }
    
    public String toJson() throws Exception {
    	Map<String, Object> jsonMap = new HashMap<String, Object>();
    	jsonMap.put("depCode", this.a);
    	jsonMap.put("desCode", this.b);
    	jsonMap.put("depName", this.d);
    	jsonMap.put("desName", this.e);
    	jsonMap.put("depDate", this.depDate);
    	
    	if(null != this.h) {
	    	List<Map<String, Object>> flightInfos = new ArrayList<Map<String,Object>>();
	    	jsonMap.put("flightInfos", flightInfos);
	    	for(Object obj : this.h) {
	    		FlightInfo info = (FlightInfo)obj;
	    		Map<String, Object> flightInfoMap = new HashMap<String, Object>();
	    		
	    		flightInfoMap.put("fltNo", info.b);
	    		flightInfoMap.put("airlineCode", info.g);
	    		flightInfoMap.put("depTime", info.c);
	    		flightInfoMap.put("desTime", info.f);
	    		
	    		List<Map<String, Object>> seatInfos = new ArrayList<Map<String,Object>>();
	    		flightInfoMap.put("seatInfos", seatInfos);
	    		for(Object obj1 : info.G) {
	    			SeatInfo seatInfo = (SeatInfo)obj1;
	    			Map<String, Object> seatInfoMap = new HashMap<String, Object>();
	    			seatInfoMap.put("cabin", seatInfo.a);
	    			seatInfoMap.put("remainSite", seatInfo.c);
	    			seatInfoMap.put("ticketPrice", seatInfo.p);
	    			
	    			seatInfos.add(seatInfoMap);
	    		}
	    		flightInfos.add(flightInfoMap);
	    	}
    	}
    	return MyJsonTransformUtil.writeValue(jsonMap);
    }
}
