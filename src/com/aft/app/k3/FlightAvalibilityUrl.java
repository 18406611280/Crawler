package com.aft.app.k3;

public class FlightAvalibilityUrl {
	//"https://mobile-hybrid.jetstar.com/Ink.API/api/flightAvailability?Origin=HKG&Destination=TPE&DepartureDate=2016-11-25T00%3A00%3A00.000Z&ReturnDate=&AdultPaxCount=1&ChildPaxCount=0&InfantPaxCount=0&CurrencyCode=CNY&ModeSaleCode=&LocaleKey=zh_CN";
	private static String url = "https://mobile-hybrid.jetstar.com/Ink.API/api/flightAvailability?";
	private String origin;
	private String Destination;
	private String DepartureDate;
	private String currency;
	
	public void setOrigin(String city){
		origin = "Origin=" + city + "&";
	}
	
	public void setDestination(String city){
		Destination = "Destination=" + city + "&";
	}
	
	public void setDepartureData(String date){
		DepartureDate = "DepartureDate=" + date;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getUrl(){
		return url + origin + Destination + DepartureDate + "T00%3A00%3A00.000Z&ReturnDate=&AdultPaxCount=3&ChildPaxCount=0&InfantPaxCount=0&CurrencyCode="+currency+"&ModeSaleCode=&LocaleKey=zh_CN";
	}
}
