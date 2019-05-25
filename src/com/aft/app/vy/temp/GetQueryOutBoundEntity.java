package com.aft.app.vy.temp;

public class GetQueryOutBoundEntity {
	public String ArrivalStation;
	public String DepartureStation;
	public String MarketDateDeparture;
	public String Currence;
	
	public GetQueryOutBoundEntity(String departure , String arrival, String departureDate, String curr){
		ArrivalStation = arrival;
		DepartureStation = departure;
		MarketDateDeparture =  departureDate;
		Currence = curr;
	}
	
	public String getEntity(){
		String jsString = "";
		jsString = "{\"AirportDateTimeList\":"
				+ "[{\"ArrivalStation\":"  + "\"" + ArrivalStation + "\"" + ","
				+ "\"DepartureStation\":"  + "\"" + DepartureStation + "\"" + ","
				+ "\"MarketDateDeparture\":" + "\"" + MarketDateDeparture + "\""
				+ "}],"
				+ "\"CurrencyCode\":"  + "\"" + Currence + "\"" + ","
				+ "\"Paxs\":[{\"PaxType\":\"ADT\",\"Quantity\":1},{\"PaxType\":\"CHD\",\"Quantity\":0},{\"PaxType\":\"INF\",\"Quantity\":0}],\"DiscountType\":0,\"PromoType\":0,\"AppVersion\":\"6.4.0\",\"Coordinates\":\"39.905498333333334 116.39099833333333\",\"DeviceType\":\"AND\",\"IP\":\"172.16.151.15\",\"InstallationID\":\"3497F67ED9C10000\",\"Xid\":\"xxxx\",\"TokenID\":\"67a2317d-8553-437c-a575-7e104f6b51e4\",\"Language\":\"EN\",\"OsVersion\":\"android : 4.4.2 : KITKAT : sdk\u003d19\",\"TimeZone\":\"8\",\"IsPushAction\":false,\"IsFenceAction\":false}";
		return jsString;
	}
}
