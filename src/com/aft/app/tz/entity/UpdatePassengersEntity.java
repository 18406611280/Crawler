package com.aft.app.tz.entity;

public class UpdatePassengersEntity {

	public String signature;
	
	public UpdatePassengersEntity(String sig){
		signature = sig;
	}
	
	public String getEntity(){
		
		String xmlString = "";
		
		xmlString  = "<?xml version=\"1.0\"?>"
				+ "<soap:Envelope"
				+" xmlns:xsi=" + "\"http://www.w3.org/2001/XMLSchema-instance\"" 
				+" xmlns:xsd=" + "\"http://www.w3.org/2001/XMLSchema\"" 
				+" xmlns:soap=" + "\"http://schemas.xmlsoap.org/soap/envelope/\"" 
				+" xmlns:enum=" + "\"http://schemas.navitaire.com/WebServices/DataContracts/Common/Enumerations\"" 
				+" xmlns:ns4=" + "\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"" 
				+" xmlns=" + "\"http://schemas.navitaire.com/WebServices\"" 
				+" xmlns:book=" + "\"http://schemas.navitaire.com/WebServices/ServiceContracts/BookingService\"" 
				+" xmlns:ns8=" + "\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"" 
				+" xmlns:n17=" + "\"http://schemas.navitaire.com/WebServices/DataContracts/TravelCommerce\"" 
				+" xmlns:ns9=" + "\"http://schemas.navitaire.com/WebServices/DataContracts/Booking\">"
				+ " <soap:Header>"
				+"<ContractVersion>348</ContractVersion>"
				//+"<Signature>NZTRX45MWOE=|EhOW0wu6YcsDahHWyYmtN4bLyLFCTVqLRDOJhl7Iej4egtndlhHsu5LcaM8xreodqHPJ17e1btCbWh//z6YiHhP2F+Gw5IDYnAXfXPX5m6KV+orZBRG8fdi5hkc1pswGnL2hYkCgeV0=</Signature>"
				+  "<Signature>" + signature + "</Signature>"
				+ "</soap:Header>"
				
				+ "<soap:Body>"
				+ "<book:UpdatePassengersRequest><book:updatePassengersRequestData>"
				+ "<ns9:Passengers><ns9:Passenger>"
				+ "<ns8:State>New</ns8:State>"
				+ "<ns9:PassengerTypeInfos><ns9:PassengerTypeInfo><ns9:DOB>0001-01-01T00:00:00</ns9:DOB><ns9:PaxType>ADT</ns9:PaxType></ns9:PassengerTypeInfo>"
				+ "</ns9:PassengerTypeInfos>"
				+ "</ns9:Passenger></ns9:Passengers>"
				+ "</book:updatePassengersRequestData></book:UpdatePassengersRequest>"
				+ "</soap:Body></soap:Envelope>";
		
		return xmlString;
	}
}
