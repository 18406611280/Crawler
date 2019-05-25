package com.aft.app.tz.entity;

public class GetAvailabilityEntity {
	public String signature;
	public String departureStation;
	public String arriveStation;
	public String beginDate;
	public String endDate;
	public String currence; //= "SGD";
	
	
	public GetAvailabilityEntity(String sig, String depStation, String arvStation, String bDate, String eDate, String curr){
		signature = sig;
		departureStation = depStation;
		arriveStation = arvStation;
		beginDate = bDate;
		endDate = eDate;
		currence = curr == null? "CNY" : curr;		
	}
	
	public String getEntity(){
		String xmlString = "";
		
		xmlString = "<soap:Envelope" 
				+	" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+   " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
				+   "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+   "xmlns:enum=\"http://schemas.navitaire.com/WebServices/DataContracts/Common/Enumerations\" "
				+   "xmlns:ns4=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\" "
				+   "xmlns=\"http://schemas.navitaire.com/WebServices\" "
				+   "xmlns:book=\"http://schemas.navitaire.com/WebServices/ServiceContracts/BookingService\" "
				+   "xmlns:ns8=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\" "
				+   "xmlns:n17=\"http://schemas.navitaire.com/WebServices/DataContracts/TravelCommerce\" "
				+   "xmlns:ns9=\"http://schemas.navitaire.com/WebServices/DataContracts/Booking\">"
				+   "<soap:Header>"
				+   "<ContractVersion>348</ContractVersion>"
				//+   "<Signature>NZTRX45MWOE=|EhOW0wu6YcsDahHWyYmtN4bLyLFCTVqLRDOJhl7Iej4egtndlhHsu5LcaM8xreodqHPJ17e1btCbWh//z6YiHhP2F+Gw5IDYnAXfXPX5m6KV+orZBRG8fdi5hkc1pswGnL2hYkCgeV0=</Signature>"
				+   "<Signature>" +  signature  + "</Signature>"
				+   "</soap:Header>"
				+   "<soap:Body>"
				+   "<book:GetAvailabilityRequest><ns9:TripAvailabilityRequest><ns9:AvailabilityRequests><ns9:AvailabilityRequest>"
				
				//+   "<ns9:DepartureStation>SIN</ns9:DepartureStation>"
				+   "<ns9:DepartureStation>" + departureStation + "</ns9:DepartureStation>" 
				
				//+   "<ns9:ArrivalStation>CAN</ns9:ArrivalStation>"
				+   "<ns9:ArrivalStation>" + arriveStation + "</ns9:ArrivalStation>"
				
				//+   "<ns9:BeginDate>2016-12-18T00:00:00</ns9:BeginDate>" 
				+   "<ns9:BeginDate>" + beginDate + "T00:00:00" + "</ns9:BeginDate>" 
				
				//+   "<ns9:EndDate>2016-12-20T00:00:00</ns9:EndDate>"
				+   "<ns9:EndDate>" + endDate + "T00:00:00" + "</ns9:EndDate>"
				
				
				
				+   "<ns9:FlightType>All</ns9:FlightType>" 
				+   "<ns9:PaxCount>1</ns9:PaxCount>"
				+   "<ns9:Dow>Daily</ns9:Dow>"
				
				//+   "<ns9:CurrencyCode>SGD</ns9:CurrencyCode>"
				+   "<ns9:CurrencyCode>" + currence + "</ns9:CurrencyCode>" 
				
				+   "<ns9:AvailabilityType>Default</ns9:AvailabilityType>"
				+   "<ns9:MaximumConnectingFlights>20</ns9:MaximumConnectingFlights>"
				+   "<ns9:AvailabilityFilter>ExcludeUnavailable</ns9:AvailabilityFilter>"
				+   "<ns9:FareClassControl>LowestFareClass</ns9:FareClassControl>" 
				+   "<ns9:SSRCollectionsMode>Segment</ns9:SSRCollectionsMode>"
				+   "<ns9:PaxPriceTypes><ns9:PaxPriceType>"
				+   "<ns9:PaxType>ADT</ns9:PaxType>"
				+   "</ns9:PaxPriceType></ns9:PaxPriceTypes>"
				+   "<ns9:IncludeTaxesAndFees>true</ns9:IncludeTaxesAndFees>"
				+   "<ns9:PaxResidentCountry>SG</ns9:PaxResidentCountry>"
				+   "</ns9:AvailabilityRequest></ns9:AvailabilityRequests></ns9:TripAvailabilityRequest></book:GetAvailabilityRequest></soap:Body></soap:Envelope>";
		
		return xmlString;
	}
}
