package com.aft.app.tr.TigerEntity;

public class GetAvailabilityEntity {
	
	public String signature;
	public String currence;
	public String beginStation;
	public String endStation;
	public String beginDate;
	public String endDate;
	
	public GetAvailabilityEntity(String sig, String bSta, String eSta, String bDate, String eDate, String curr){
		currence      = curr == null ? "HKD" : curr;
		signature     = sig;
		beginStation  = bSta;
		endStation    = eSta;
		beginDate     = bDate;
		endDate       = eDate;
	}

	
	public String getEntity(){
		String xmlString = "";
		xmlString = "<soap:Envelope "
				
			+  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+  "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
			+  "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
			+  "xmlns:enum=\"http://schemas.navitaire.com/WebServices/DataContracts/Common/Enumerations\" "
			+  "xmlns:ns4=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\" "
			+  "xmlns=\"http://schemas.navitaire.com/WebServices\" "
			+  "xmlns:book=\"http://schemas.navitaire.com/WebServices/ServiceContracts/BookingService\" "
			+  "xmlns:ns8=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\" "
			+  "xmlns:n17=\"http://schemas.navitaire.com/WebServices/DataContracts/TravelCommerce\" "
			+  "xmlns:ns9=\"http://schemas.navitaire.com/WebServices/DataContracts/Booking\">"
			+  "<soap:Header>"
			+ "<ContractVersion>3413</ContractVersion>"
			//+ "<Signature>w1M2wM7X25E=|Srw435Bky9SiVbsvKS+oFMSk+GlIX7aKqQniHqq0e+bJkZTEb7KN2Z6RQeM9jcNPCw0FAKWWv7dpHpXQ+GdjqH5egx3OsYSHG7st4B+g5GAbn+cuSkW/UuwLPhEs7fs7Z5ZEgxzv1iI=</Signature>"
			+ "<Signature>" + signature + "</Signature>"
			+ "</soap:Header>"
			+ "<soap:Body>"
			+ "<book:GetAvailabilityRequest>"
			+ "<ns9:TripAvailabilityRequest><ns9:AvailabilityRequests>"
			//------------------------------------------------------------------------------------------
			+ "<ns9:AvailabilityRequest>"
			//+ "<ns9:DepartureStation>HKG</ns9:DepartureStation>"
			+ "<ns9:DepartureStation>" + beginStation + "</ns9:DepartureStation>"
			
			//+ "<ns9:ArrivalStation>KUL</ns9:ArrivalStation>"
			+ "<ns9:ArrivalStation>" + endStation + "</ns9:ArrivalStation>"
			
			//+ "<ns9:BeginDate>2016-12-20T00:00:00</ns9:BeginDate>"
			+ "<ns9:BeginDate>" + beginDate + "T00:00:00" + "</ns9:BeginDate>"
			
			//+ "<ns9:EndDate>2016-12-22T00:00:00</ns9:EndDate>"
			+ "<ns9:EndDate>" + endDate + "T00:00:00" + "</ns9:EndDate>"
			
			+ "<ns9:FlightType>All</ns9:FlightType>"
			+ "<ns9:PaxCount>3</ns9:PaxCount>"
			+ "<ns9:Dow>Daily</ns9:Dow>"
			//+ "<ns9:CurrencyCode>HKD</ns9:CurrencyCode>"
			+ "<ns9:CurrencyCode>" + currence + "</ns9:CurrencyCode>"
			
			+ "<ns9:AvailabilityType>Default</ns9:AvailabilityType>"
			+ "<ns9:MaximumConnectingFlights>20</ns9:MaximumConnectingFlights>"
			+ "<ns9:AvailabilityFilter>ExcludeImminent</ns9:AvailabilityFilter>"
			+ "<ns9:FareClassControl>LowestFareClass</ns9:FareClassControl>"
			+ "<ns9:SSRCollectionsMode>Segment</ns9:SSRCollectionsMode>"
			+ "<ns9:PaxPriceTypes><ns9:PaxPriceType>"
			+ "<ns9:PaxType>ADT</ns9:PaxType>"
			+ "</ns9:PaxPriceType><ns9:PaxPriceType><ns9:PaxType>ADT</ns9:PaxType>"
			+ "</ns9:PaxPriceType>"
			+ "<ns9:PaxPriceType><ns9:PaxType>ADT</ns9:PaxType></ns9:PaxPriceType></ns9:PaxPriceTypes>"
			+ "<ns9:IncludeTaxesAndFees>true</ns9:IncludeTaxesAndFees>"
			+ "</ns9:AvailabilityRequest>"
			//----------------------------------------------------------------------------------------------  request ��������˫�̲�ѯ
			/*
			+ "<ns9:AvailabilityRequest>"
			//+ "<ns9:DepartureStation>KUL</ns9:DepartureStation>"
			//+ "<ns9:ArrivalStation>HKG</ns9:ArrivalStation>"
			//+ "<ns9:BeginDate>2016-12-20T00:00:00</ns9:BeginDate>"
			//+ "<ns9:EndDate>2016-12-22T00:00:00</ns9:EndDate>"
			+ "<ns9:FlightType>All</ns9:FlightType>"
			+ "<ns9:PaxCount>3</ns9:PaxCount>"
			+ "<ns9:Dow>Daily</ns9:Dow>"
			//+ "<ns9:CurrencyCode>HKD</ns9:CurrencyCode>"
			+ "<ns9:AvailabilityType>Default</ns9:AvailabilityType>"
			+ "<ns9:MaximumConnectingFlights>20</ns9:MaximumConnectingFlights>"
			+ "<ns9:AvailabilityFilter>ExcludeImminent</ns9:AvailabilityFilter>"
			+ "<ns9:FareClassControl>LowestFareClass</ns9:FareClassControl>"
			+ "<ns9:SSRCollectionsMode>Segment</ns9:SSRCollectionsMode>"
			+ "<ns9:PaxPriceTypes><ns9:PaxPriceType><ns9:PaxType>ADT</ns9:PaxType></ns9:PaxPriceType>"
			+ "<ns9:PaxPriceType>"
			+ "<ns9:PaxType>ADT</ns9:PaxType>"
			+ "</ns9:PaxPriceType>"
			+ "<ns9:PaxPriceType><ns9:PaxType>ADT</ns9:PaxType>"
			+ "</ns9:PaxPriceType></ns9:PaxPriceTypes>"
			+ "<ns9:IncludeTaxesAndFees>true</ns9:IncludeTaxesAndFees>"
			+ "</ns9:AvailabilityRequest>"
			*/
			//--------------------------------------------------------------------------------------------------
			+ "</ns9:AvailabilityRequests>"
			+ "</ns9:TripAvailabilityRequest>"
			+ "</book:GetAvailabilityRequest>"
			+ "</soap:Body>"
			+ "</soap:Envelope>";
				
		return xmlString;
	}
}
