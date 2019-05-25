package com.aft.app.k3;

public class TestGetFlightAvaliable {

	public static void main(String args[]){
		
		FlightAvailabilityCookie cookie = new FlightAvailabilityCookie();
		//set cookie ����
		cookie.setAdultCount("3");
		cookie.setChildCount("0");
		cookie.setDestination("TPE");
		cookie.setIsOneWay("true");
		cookie.setInfantCount("0");
		cookie.setOrigin("HKG");
		SslRequest req = new SslRequest();
		String temp = cookie.getCookie();
		//String t = "fsOrigin=; fsDestination=; fsFlightNumber=; AMCVS_8D0D1C8B532B54B40A490D4D%40AdobeOrg=1; s_nr=1479889065588; optimizelyExp=7731351814%2C7730041952%2C7825040071%2C7600140283%2C7672982377%2C7514601422%2C7879187823%2C7825181399%2C6338571223%2C7723991577%2C7145120252%2C7133201725%2C7617193661; AMCV_8D0D1C8B532B54B40A490D4D%40AdobeOrg=-179204249%7CMCIDTS%7C17129%7CMCMID%7C73947336037717250701904081528214231711%7CMCAAMLH-1480491068%7C11%7CMCAAMB-1480587216%7CcIBAx_aQzFEHcPoEv0GwcQ%7CMCOPTOUT-1479989616s%7CNONE%7CMCAID%7CNONE; bid_JSaU2TcvPguuhpZfmwr34R7R8Wo7moKH=ca4d92c5-b063-4906-aa6f-0306fcfb2d6e; gpv_mpl=1; gpv_pn=m%3A%20%2Fbooking%2Fselect-departure-flight%2Fsearch; gpv_v1=m%3A%20%2Fbooking; s_cc=true; aam_tnt=aamsegid%3D4328853; aam_uuid=75684822040044038651583949237030151432; s_sq=jetstarmobileappprd%3D%2526pid%253Dm%25253A%252520%25252Fbooking%25252Fselect-departure-flight%25252Fsearch%2526pidt%253D1%2526oid%253D%2525E5%252585%2525B3%2525E9%252597%2525AD%2526oidt%253D3%2526ot%253DSUBMIT; optimizelySegments=%7B%221769861189%22%3A%22gc%22%2C%221772450570%22%3A%22true%22%2C%221782270528%22%3A%22direct%22%2C%222159600287%22%3A%22none%22%7D; optimizelyBuckets=%7B%7D; optimizelyEndUserId=oeu1479886242822r0.9956864828709513; kvplocale=; previousSearches=; optimizelyPendingLogEvents=%5B%5D; isOneWay=true; origin=HKG; destination=NRT; adultCount=1; childCount=0; infantCount=0; culture=%u4E2D%u56FD%20%28%u4E2D%u6587%29; _ga=GA1.2.2054824987.1479886245;"; 
		
		
		//set url ����
		FlightAvalibilityUrl url =  new FlightAvalibilityUrl();
		url.setDepartureData("2017-02-11");
		url.setDestination("HAN");
		url.setOrigin("CAN");
		String testUrl = url.getUrl();
		//String tempUrl = "https://mobile-hybrid.jetstar.com/Ink.API/api/flightAvailability?Origin=HKG&Destination=TPE&DepartureDate=2016-11-26T00%3A00%3A00.000Z&ReturnDate=&AdultPaxCount=1&ChildPaxCount=0&InfantPaxCount=0&CurrencyCode=HKD&ModeSaleCode=&LocaleKey=zh_CN";
		
		//����
		String result = req.GetWithoutCA(testUrl, temp, "118.119.102.37", 20282);	
		System.out.print(result);
	}
	
}


