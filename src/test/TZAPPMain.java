package test;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.aft.app.tz.ScootFly.Constants;
import com.aft.app.tz.entity.GetAvailabilityEntity;
import com.aft.app.tz.http.RequestSession;
import com.aft.app.tz.http.SSLPost;
import com.aft.app.tz.utils.TokenParser;

public class TZAPPMain {

	public static void main(String args[]) throws Exception{
	
		RequestSession req = new RequestSession(Constants.getSessionUrl, Constants.Authorization);
		String result = req.request(null);
		
		System.out.println(result);
		System.out.println(TokenParser.parse(result));
		
		String url = "https://scootapi.themobilelife.com/BookingManager.svc";
		String sig = TokenParser.parse(result);
			
		SSLPost post = new SSLPost(url, Constants.Authorization, Constants.SOAPGetAvailability);
		
		GetAvailabilityEntity entity = new GetAvailabilityEntity(sig, "SIN", "CAN", "2016-12-22", "2016-12-22", "SGD");
		String e = entity.getEntity();
		String baos = post.request( e);
		
		System.out.print(baos);	
		
		
		
//		String baos ="<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><GetAvailabilityByTripResponse xmlns=\"http://schemas.navitaire.com/WebServices/ServiceContracts/BookingService\"><GetTripAvailabilityResponse xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Booking\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"><OtherServiceInfoList xmlns:a=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"/><Schedules><ArrayOfJourneyDateMarket><JourneyDateMarket><DepartureDate>2016-12-20T00:00:00</DepartureDate><DepartureStation>SIN</DepartureStation><ArrivalStation>CAN</ArrivalStation><Journeys><Journey><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><NotForGeneralUse>false</NotForGeneralUse><Segments><Segment><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><ActionStatusCode>KK</ActionStatusCode><ArrivalStation>CAN</ArrivalStation><CabinOfService> </CabinOfService><ChangeReasonCode/><DepartureStation>SIN</DepartureStation><PriorityCode/><SegmentType/><STA>2016-12-20T09:55:00</STA><STD>2016-12-20T06:05:00</STD><International>true</International><FlightDesignator xmlns:a=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"><a:CarrierCode>TR</a:CarrierCode><a:FlightNumber>2986</a:FlightNumber><a:OpSuffix> </a:OpSuffix></FlightDesignator><XrefFlightDesignator i:nil=\"true\" xmlns:a=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"/><Fares><Fare i:type=\"AvailableFare\"><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><ClassOfService>W1</ClassOfService><ClassType/><RuleTariff/><CarrierCode>TZ</CarrierCode><RuleNumber>6000</RuleNumber><FareBasisCode>W1TRA</FareBasisCode><FareSequence>54</FareSequence><FareClassOfService>W1</FareClassOfService><FareStatus>Default</FareStatus><FareApplicationType>Route</FareApplicationType><OriginalClassOfService>W1</OriginalClassOfService><XrefClassOfService/><PaxFares><PaxFare><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><PaxType>ADT</PaxType><PaxDiscountCode/><FareDiscountCode/><ServiceCharges><BookingServiceCharge><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><ChargeType>FarePrice</ChargeType><CollectType>SellerChargeable</CollectType><ChargeCode/><TicketCode/><CurrencyCode>SGD</CurrencyCode><Amount>135.0000</Amount><ChargeDetail/><ForeignCurrencyCode>SGD</ForeignCurrencyCode><ForeignAmount>135.0000</ForeignAmount></BookingServiceCharge><BookingServiceCharge><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><ChargeType>Tax</ChargeType><CollectType>SellerChargeable</CollectType><ChargeCode/><TicketCode/><CurrencyCode>SGD</CurrencyCode><Amount>34.00000000</Amount><ChargeDetail>TaxFeeSum</ChargeDetail><ForeignCurrencyCode>SGD</ForeignCurrencyCode><ForeignAmount>34.00000000</ForeignAmount></BookingServiceCharge></ServiceCharges></PaxFare></PaxFares><ProductClass>E1</ProductClass><IsAllotmentMarketFare>false</IsAllotmentMarketFare><TravelClassCode> </TravelClassCode><FareSellKey>0~W1~~W1TRA~6000~~54~X</FareSellKey><InboundOutbound>None</InboundOutbound><AvailableCount>1</AvailableCount><Status>Active</Status><SSRIndexes xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"/></Fare></Fares><Legs><Leg><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><ArrivalStation>CAN</ArrivalStation><DepartureStation>SIN</DepartureStation><STA>2016-12-20T09:55:00</STA><STD>2016-12-20T06:05:00</STD><FlightDesignator xmlns:a=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"><a:CarrierCode>TR</a:CarrierCode><a:FlightNumber>2986</a:FlightNumber><a:OpSuffix> </a:OpSuffix></FlightDesignator><LegInfo><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><AdjustedCapacity>180</AdjustedCapacity><EquipmentType>320</EquipmentType><EquipmentTypeSuffix>TRB</EquipmentTypeSuffix><ArrivalTerminal>1</ArrivalTerminal><ArrvLTV>480</ArrvLTV><Capacity>180</Capacity><CodeShareIndicator> </CodeShareIndicator><DepartureTerminal>2</DepartureTerminal><DeptLTV>480</DeptLTV><ETicket>true</ETicket><FlifoUpdated>false</FlifoUpdated><IROP>false</IROP><Status>Normal</Status><Lid>0</Lid><OnTime> </OnTime><PaxSTA>2016-12-20T09:55:00</PaxSTA><PaxSTD>2016-12-20T06:05:00</PaxSTD><PRBCCode>TR320AMW</PRBCCode><ScheduleServiceType>J</ScheduleServiceType><Sold>0</Sold><OutMoveDays>0</OutMoveDays><BackMoveDays>0</BackMoveDays><LegNests/><LegSSRs/><OperatingFlightNumber>    </OperatingFlightNumber><OperatedByText/><OperatingCarrier/><OperatingOpSuffix> </OperatingOpSuffix><SubjectToGovtApproval>false</SubjectToGovtApproval><MarketingCode/><ChangeOfDirection>false</ChangeOfDirection><MarketingOverride>false</MarketingOverride></LegInfo><OperationsInfo><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><ActualArrivalGate/><ActualDepartureGate/><ActualOffBlockTime>9999-12-31T00:00:00Z</ActualOffBlockTime><ActualOnBlockTime>9999-12-31T00:00:00Z</ActualOnBlockTime><ActualTouchDownTime>9999-12-31T00:00:00Z</ActualTouchDownTime><AirborneTime>9999-12-31T00:00:00Z</AirborneTime><ArrivalGate/><ArrivalNote/><ArrivalStatus>Default</ArrivalStatus><BaggageClaim/><DepartureGate/><DepartureNote/><DepartureStatus>Default</DepartureStatus><ETA>9999-12-31T00:00:00Z</ETA><ETD>9999-12-31T00:00:00Z</ETD><STA>2016-12-20T09:55:00</STA><STD>2016-12-20T06:05:00</STD><TailNumber/></OperationsInfo><InventoryLegID>2659075</InventoryLegID></Leg></Legs><PaxBags/><PaxSeats/><PaxSSRs/><PaxSegments/><PaxTickets/><PaxSeatPreferences i:nil=\"true\"/><SalesDate>9999-12-31T00:00:00Z</SalesDate><SegmentSellKey>TR~2986~ ~~SIN~12/20/2016 06:05~CAN~12/20/2016 09:55~</SegmentSellKey><PaxScores/><ChannelType>Default</ChannelType></Segment></Segments><JourneySellKey>TR~2986~ ~~SIN~12/20/2016 06:05~CAN~12/20/2016 09:55~</JourneySellKey></Journey><Journey><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><NotForGeneralUse>false</NotForGeneralUse><Segments><Segment><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><ActionStatusCode>KK</ActionStatusCode><ArrivalStation>CAN</ArrivalStation><CabinOfService> </CabinOfService><ChangeReasonCode/><DepartureStation>SIN</DepartureStation><PriorityCode/><SegmentType/><STA>2016-12-21T02:05:00</STA><STD>2016-12-20T22:10:00</STD><International>true</International><FlightDesignator xmlns:a=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"><a:CarrierCode>TZ</a:CarrierCode><a:FlightNumber> 128</a:FlightNumber><a:OpSuffix> </a:OpSuffix></FlightDesignator><XrefFlightDesignator i:nil=\"true\" xmlns:a=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"/><Fares><Fare i:type=\"AvailableFare\"><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><ClassOfService>B1</ClassOfService><ClassType/><RuleTariff/><CarrierCode>TZ</CarrierCode><RuleNumber>2000</RuleNumber><FareBasisCode>B1TZA</FareBasisCode><FareSequence>31</FareSequence><FareClassOfService>B1</FareClassOfService><FareStatus>Default</FareStatus><FareApplicationType>Route</FareApplicationType><OriginalClassOfService>B1</OriginalClassOfService><XrefClassOfService/><PaxFares><PaxFare><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><PaxType>ADT</PaxType><PaxDiscountCode/><FareDiscountCode/><ServiceCharges><BookingServiceCharge><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">New</State><ChargeType>FarePrice</ChargeType><CollectType>SellerChargeable</CollectType><ChargeCode/><TicketCode/><CurrencyCode>SGD</CurrencyCode><Amount>190.0000</Amount><ChargeDetail/><ForeignCurrencyCode>SGD</ForeignCurrencyCode><ForeignAmount>190.0000</ForeignAmount></BookingServiceCharge><BookingServiceCharge><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><ChargeType>Tax</ChargeType><CollectType>SellerChargeable</CollectType><ChargeCode/><TicketCode/><CurrencyCode>SGD</CurrencyCode><Amount>34.00000000</Amount><ChargeDetail>TaxFeeSum</ChargeDetail><ForeignCurrencyCode>SGD</ForeignCurrencyCode><ForeignAmount>34.00000000</ForeignAmount></BookingServiceCharge></ServiceCharges></PaxFare></PaxFares><ProductClass>E1</ProductClass><IsAllotmentMarketFare>false</IsAllotmentMarketFare><TravelClassCode> </TravelClassCode><FareSellKey>0~B1~~B1TZA~2000~~31~X</FareSellKey><InboundOutbound>None</InboundOutbound><AvailableCount>3</AvailableCount><Status>Active</Status><SSRIndexes xmlns:a=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\"/></Fare></Fares><Legs><Leg><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><ArrivalStation>CAN</ArrivalStation><DepartureStation>SIN</DepartureStation><STA>2016-12-21T02:05:00</STA><STD>2016-12-20T22:10:00</STD><FlightDesignator xmlns:a=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\"><a:CarrierCode>TZ</a:CarrierCode><a:FlightNumber> 128</a:FlightNumber><a:OpSuffix> </a:OpSuffix></FlightDesignator><LegInfo><State xmlns=\"http://schemas.navitaire.com/WebServices/DataContracts/Common\">Clean</State><AdjustedCapacity>377</AdjustedCapacity><EquipmentType>789</EquipmentType><EquipmentTypeSuffix>TZA</EquipmentTypeSuffix><ArrivalTerminal/><ArrvLTV>480</ArrvLTV><Capacity>375</Capacity><CodeShareIndicator> </CodeShareIndicator><DepartureTerminal>2</DepartureTerminal><DeptLTV>480</DeptLTV><ETicket>true</ETicket><FlifoUpdated>false</FlifoUpdated><IROP>false</IROP><Status>Normal</Status><Lid>0</Lid><OnTime> </OnTime><PaxSTA>2016-12-21T02:05:00</PaxSTA><PaxSTD>2016-12-20T22:10:00</PaxSTD><PRBCCode>TZ789A01</PRBCCode><ScheduleServiceType>J</ScheduleServiceType><Sold>0</Sold><OutMoveDays>0</OutMoveDays><BackMoveDays>0</BackMoveDays><LegNests/><LegSSRs/><OperatingFlightNumber>    </OperatingFlightNumber><OperatedByText/><OperatingCarrier/><OperatingOpSuffix> </OperatingOpSuffix><SubjectToGovtApproval>false</SubjectToGovtApproval><MarketingCode/><ChangeOfDirection>false</ChangeOfDirection><MarketingOverride>false</MarketingOverride></LegInfo><OperationsInfo i:nil=\"true\"/><InventoryLegID>737393</InventoryLegID></Leg></Legs><PaxBags/><PaxSeats/><PaxSSRs/><PaxSegments/><PaxTickets/><PaxSeatPreferences i:nil=\"true\"/><SalesDate>9999-12-31T00:00:00Z</SalesDate><SegmentSellKey>TZ~ 128~ ~~SIN~12/20/2016 22:10~CAN~12/21/2016 02:05~</SegmentSellKey><PaxScores/><ChannelType>Default</ChannelType></Segment></Segments><JourneySellKey>TZ~ 128~ ~~SIN~12/20/2016 22:10~CAN~12/21/2016 02:05~</JourneySellKey></Journey></Journeys><IncludesTaxesAndFees>true</IncludesTaxesAndFees></JourneyDateMarket></ArrayOfJourneyDateMarket></Schedules></GetTripAvailabilityResponse></GetAvailabilityByTripResponse></s:Body></s:Envelope>";
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(baos)));
		Element root = doc.getDocumentElement();
		NodeList journeyDateMarkets = root.getElementsByTagName("JourneyDateMarket");
		if(journeyDateMarkets==null || journeyDateMarkets.getLength()==0){
			System.out.println("没有航班信息");
			return;
		}
		Element journeyDateMarket = (Element)journeyDateMarkets.item(0);
		Element Journeys = (Element)journeyDateMarket.getElementsByTagName("Journeys").item(0);
		NodeList  journeys = Journeys.getElementsByTagName("Journey");
		for(int i=0;i<journeys.getLength();i++){
			Element journey = (Element)journeys.item(i);
			NodeList segments =  journey.getElementsByTagName("Segment");
			//过滤联程和往返
			if(segments.getLength()>1)continue;
			
			Element segment = (Element)segments.item(0);
			
			String std = segment.getElementsByTagName("STD").item(0).getTextContent();
			String sta = segment.getElementsByTagName("STA").item(0).getTextContent();
			String depDate = std.substring(0, 10);
			String depTime = std.substring(11, 16);
			String desDate = sta.substring(0, 10);
			String desTime = sta.substring(11, 16);
			
			Element  flightDesignator = (Element)segment.getElementsByTagName("FlightDesignator").item(0);
			String airlineCode  = flightDesignator.getElementsByTagName("a:CarrierCode").item(0).getTextContent();
			String airlineNumber  = flightDesignator.getElementsByTagName("a:FlightNumber").item(0).getTextContent();
			
			
			
			Element fare = (Element)segment.getElementsByTagName("Fare").item(0);
			
			String RemainSite = fare.getElementsByTagName("AvailableCount").item(0).getTextContent();
			
			NodeList bookingServiceCharges = fare.getElementsByTagName("BookingServiceCharge");
			String ticketPrice = "";
			String tax = "";
			for(int j=0;j<bookingServiceCharges.getLength();j++){
				Element bookingServiceCharge = (Element)bookingServiceCharges.item(j);
				String chargeType = bookingServiceCharge.getElementsByTagName("ChargeType").item(0).getTextContent();
				String amount = bookingServiceCharge.getElementsByTagName("Amount").item(0).getTextContent();
				if("FarePrice".equals(chargeType)){
					ticketPrice = amount;
				}else if("Tax".equals(chargeType)){
					tax = amount;
				}
			}
			System.out.println(depDate+":"+depTime+":"+desDate+":"+desTime+":"+airlineCode+":"+airlineNumber+":"+RemainSite+":"+ticketPrice+":"+tax);
		}
		
	}
}
