package com.aft.app.k3;

public class FlightAvailabilityCookie extends JetStarCookie{
	
	private String isOneWay ;
	private String origin;
	private String destination;
	private String adultCount;
	private String childCount;
	private String infantCount;
	
	public String getCookie(){
		return cookies + isOneWay + origin + destination + adultCount + childCount + infantCount ;
	}
	
	public void setIsOneWay(String bool){
		isOneWay = "isOneWay=" + bool + ";";
	}
	
	public void setOrigin(String city){
		origin = "origin=" + city + ";";
	}
	
	public void setDestination(String city){
		destination = "destination=" + city + ";";
	}
	
	public void setAdultCount(String cnt){
		adultCount = "adultCount=" + cnt + ";";
	}
	
	public void setChildCount(String cnt){
		childCount = "childCount=" + cnt + ";"; 
	}
	
	public void setInfantCount(String cnt){
		infantCount = "infantCount=" + cnt + ";";
	}
}
