package com.aft.crawl.result.vo.lcc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.JxdMtPostVo;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.common.FlightSegment;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.crawl.result.vo.inter.CrawlResultInterTrip;
import com.aft.crawl.result.vo.lcc.vo.ts.LCCTSPostFromSegments;
import com.aft.crawl.result.vo.lcc.vo.ts.LCCTSPostNewVo;
import com.aft.crawl.result.vo.lcc.vo.ts.LCCTSPostRetSegments;
import com.aft.crawl.result.vo.lcc.vo.ts.LCCTSPostRule;
import com.aft.crawl.result.vo.lcc.vo.ts.LCCTSPostVo;
import com.aft.utils.date.MyDateFormatUtils;

public class LCCPostDataConversion {
	public static List<LCCTSPostNewVo> toThisTS(List<CrawlResultBase> resultBases) throws Exception {
		SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<LCCTSPostNewVo> resultVos =new ArrayList<>();
		LCCTSPostNewVo lcctsPostNewVo = new LCCTSPostNewVo();
		List<LCCPostVoBase> vos = new ArrayList<LCCPostVoBase>();
		for(CrawlResultBase resultBase : resultBases) {
			//新版VO，支持单程联程往返
			if(resultBase instanceof CrawlResultInter) {
				CrawlResultInter b2c = (CrawlResultInter)resultBase;
				//不是人民币的币种不上传到MT上
				if(!"CNY".equals(b2c.getCurrency()))continue;
				LCCTSPostVo vo = new LCCTSPostVo();
				vo.setUpdateTime(SDF_YMD.format(new Date()));
				vo.setDepAirport(b2c.getDepCode());
				vo.setArrAirport(b2c.getDesCode());
				vo.setGoDate(b2c.getDepDate());
				if("PUT954,PUT964".contains(b2c.getPageType())){
					vo.setPriceFrom("JQ");
				}
				else if("PUT955,PUT969".equals(b2c.getPageType())){
					vo.setPriceFrom("TZ");
				}
				else if("PUT956".equals(b2c.getPageType())){
					vo.setPriceFrom("TR");
				}
				else if("PUT957".equals(b2c.getPageType())){
					vo.setPriceFrom("VY");
				}
				else if("PUT958".equals(b2c.getPageType())){
					vo.setPriceFrom("VJ");
				}
				else if("PUT959".equals(b2c.getPageType())){
					vo.setPriceFrom("air5J");
				}
				else if("PUT960".equals(b2c.getPageType())){
					vo.setPriceFrom("MM");
				}
				else if("PUT961".equals(b2c.getPageType())){
					vo.setPriceFrom("JT");
				}
				else if("PUT962".equals(b2c.getPageType())){
					vo.setPriceFrom("FY");
				}
				else if("PUT963".equals(b2c.getPageType())){
					vo.setPriceFrom("JW");
				}
				else if("PUT965".equals(b2c.getPageType())){
					vo.setPriceFrom("WE");
				}
				else if("PUT966".equals(b2c.getPageType())){
					vo.setPriceFrom("U2");
				}
				else if("PUT968".equals(b2c.getPageType())){
					vo.setPriceFrom("IT");
				}
				else if("PUT908".equals(b2c.getPageType()) || "PUT935".equals(b2c.getPageType()) || "PUT937".equals(b2c.getPageType())){
					vo.setPriceFrom("MU");
				}
				else if("PUT903".equals(b2c.getPageType()) || "PUT904".equals(b2c.getPageType())){
					vo.setIsGn("Y");
				}else if("PUT903".equals(b2c.getPageType()) || "PUT904".equals(b2c.getPageType())){
					vo.setPriceFrom("CZ");
					vo.setIsGn("Y");
				}else if("PUT926".equals(b2c.getPageType())){
					vo.setPriceFrom("HU");
					vo.setIsGn("Y");
				}else{
					vo.setPriceFrom(b2c.getFlightTrips().get(0).getAirlineCode());
				}
				vo.setAdultPrice(b2c.getTicketPrice().toString());
				vo.setAdultTax(b2c.getTaxFee().toString());
				LCCTSPostRule rule = new LCCTSPostRule();
				rule.setRefund(b2c.getRefundInfo());
				rule.setEndorse(b2c.getChangeInfo()+";"+b2c.getQianZhuanInfo());
				vo.setRule(rule);
				List<LCCTSPostFromSegments> fromSegments = new ArrayList<LCCTSPostFromSegments>();
				//单程 ，联程
				if("OW".equals(b2c.getRouteType())){
					List<CrawlResultInterTrip> trips = b2c.getFlightTrips();
					for(CrawlResultInterTrip trip : trips){
						LCCTSPostFromSegments formSegment = new LCCTSPostFromSegments();
						formSegment.setArrAirport(trip.getDesCode());
						formSegment.setArrTime(trip.getDesDate()+" "+trip.getDesTime()+":00");
						formSegment.setCabin(trip.getCabin());
						formSegment.setCabinCount(String.valueOf(trip.getRemainSite()));
						formSegment.setCarrier(trip.getAirlineCode());
						formSegment.setDepAirport(trip.getDepCode());
						formSegment.setDepTime(trip.getDepDate()+" "+trip.getDepTime()+":00");
						formSegment.setFlightNumber(trip.getFltNr());
						fromSegments.add(formSegment);
					}
					vo.setFromSegments(fromSegments);
					vos.add(vo);
				}
					
//				LCCMtPostNewVo vo = new LCCMtPostNewVo();
//				vo.setFromCode(b2c.getDepCode());
//				vo.setToCode(b2c.getDesCode());
//				if("OW".equals(b2c.getRouteType()))vo.setTripType("1");
//				else vo.setTripType("2");
//				
//				if("PUT954".equals(b2c.getPageType())){
//					vo.setPriceFrom("JQ");
//				}
//				if("PUT955".equals(b2c.getPageType())){
//					vo.setPriceFrom("TZ");
//				}
//				if("PUT956".equals(b2c.getPageType())){
//					vo.setPriceFrom("TR");
//				}
//				if("PUT957".equals(b2c.getPageType())){
//					vo.setPriceFrom("VY");
//				}
//				if("PUT958".equals(b2c.getPageType())){
//					vo.setPriceFrom("VJ");
//				}
//				if("PUT959".equals(b2c.getPageType())){
//					vo.setPriceFrom("air5J");
//				}
//				Date ymdsDate = SDF_YMD.parse(b2c.getDepDate()+" "+"00:00");
//				vo.setGoDate(SDF_DMY.format(ymdsDate));
//				if(b2c.getBackDate()!=null && !"".equals(b2c.getBackDate())){
//					Date ymdbDate = SDF_YMD.parse(b2c.getBackDate()+" "+"00:00");
//					vo.setBackDate(SDF_DMY.format(ymdbDate));
//				}
//				List<LCCMtPostPrice> priceList = new ArrayList<LCCMtPostPrice>();
//				LCCMtPostPrice price = new LCCMtPostPrice();
//				price.setTicketFee(b2c.getTicketPrice().toString());
//				price.setTax(b2c.getTaxFee().toString());
//				priceList.add(price);
//				vo.setPrice(priceList);
//				
//				List<LCCMtPostFltWays> fltWayList = new ArrayList<LCCMtPostFltWays>();
//				//单程 ，联程
//				if("OW".equals(b2c.getRouteType())){
//					LCCMtPostFltWays fltWay = new LCCMtPostFltWays();
//					fltWay.setFromCode(b2c.getDepCode());
//					fltWay.setToCode(b2c.getDesCode());
//					fltWay.setWay("1");
//					int lastIndex = b2c.getFlightTrips().size()-1;
//					List<CrawlResultInterTrip> trips = b2c.getFlightTrips();
//					Date ymdfDate = SDF_YMD.parse(trips.get(0).getDepDate()+" "+trips.get(0).getDepTime()+":00");
//					Date ymdaDate = SDF_YMD.parse(trips.get(lastIndex).getDesDate()+" "+trips.get(lastIndex).getDesTime()+":00");
//					fltWay.setFltTime(SDF_DMY.format(ymdfDate));
//					fltWay.setArrTime(SDF_DMY.format(ymdaDate));
//					List<LCCMtPostFlts> flts = new ArrayList<LCCMtPostFlts>();
//					for(CrawlResultInterTrip trip : trips){
//						LCCMtPostFlts flt = new LCCMtPostFlts();
//						flt.setFromCode(trip.getDepCode());
//						flt.setToCode(trip.getDesCode());
//						Date dd = SDF_YMD.parse(trip.getDepDate()+" "+trip.getDepTime()+":00");
//						Date ad = SDF_YMD.parse(trip.getDesDate()+" "+trip.getDesTime()+":00");
//						flt.setFltTime(SDF_DMY.format(dd));
//						flt.setArrTime(SDF_DMY.format(ad));
//						flt.setFltNo(trip.getFltNr());
//						flt.setSeat(trip.getCabin());
//						flt.setQty(String.valueOf(trip.getRemainSite()));
//						flt.setSeq(String.valueOf(trip.getTripNo()));
//						flts.add(flt);
//					}
//					fltWay.setFlts(flts);
//					fltWayList.add(fltWay);
//					vo.setFltWays(fltWayList);
//					vos.add(vo);
//				}
			//旧版VO，只支持单程
			}else if(resultBase instanceof CrawlResultB2C) {
				CrawlResultB2C b2c = (CrawlResultB2C)resultBase;
				//不是人民币的币种不上传到MT上
				if(b2c.getType()!=null && !"".equals(b2c.getType()) && b2c.getType().contains("此价格的币种类型"))continue;
				LCCTSPostVo vo = new LCCTSPostVo();
				vo.setUpdateTime(SDF_YMD.format(new Date()));
				vo.setDepAirport(b2c.getDepCode());
				vo.setArrAirport(b2c.getDesCode());
				vo.setGoDate(b2c.getDepDate());
				vo.setIsGn("N");
				if("PUT954,PUT964".contains(b2c.getPageType())){
					vo.setPriceFrom("JQ");
				}
				else if("PUT955".equals(b2c.getPageType())){
					vo.setPriceFrom("TZ");
				}
				else if("PUT956".equals(b2c.getPageType())){
					vo.setPriceFrom("TR");
				}
				else if("PUT957".equals(b2c.getPageType())){
					vo.setPriceFrom("VY");
				}
				else if("PUT958".equals(b2c.getPageType())){
					vo.setPriceFrom("VJ");
				}
				else if("PUT959".equals(b2c.getPageType())){
					vo.setPriceFrom("air5J");
				}
				else if("PUT960".equals(b2c.getPageType())){
					vo.setPriceFrom("MM");
				}
				else if("PUT961".equals(b2c.getPageType())){
					vo.setPriceFrom("JT");
				}
				else if("PUT962".equals(b2c.getPageType())){
					vo.setPriceFrom("FY");
				}
				else if("PUT963".equals(b2c.getPageType())){
					vo.setPriceFrom("JW");
				}
				else if("PUT965".equals(b2c.getPageType())){
					vo.setPriceFrom("WE");
				}
				else if("PUT908".equals(b2c.getPageType()) || "PUT935".equals(b2c.getPageType()) || "PUT937".equals(b2c.getPageType())){
					vo.setPriceFrom("MUGN");
					vo.setIsGn("Y");
				}
				else if("PUT903".equals(b2c.getPageType()) || "PUT904".equals(b2c.getPageType())){
					vo.setPriceFrom("CZGN");
					vo.setIsGn("Y");
				}
				else if("PUT926".equals(b2c.getPageType())){
					vo.setPriceFrom("HUGN");
					vo.setIsGn("Y");
				}else if("PUT947".equals(b2c.getPageType())){
					vo.setPriceFrom("SCGN");
					vo.setIsGn("Y");
				}else if("PUT503".equals(b2c.getPageType())){
					vo.setPriceFrom("SCZSFGN");
					vo.setIsGn("Y");
				}else{
					vo.setPriceFrom(b2c.getAirlineCode());
				}
				
				vo.setAdultPrice(b2c.getTicketPrice().toString());
				vo.setAdultTax(b2c.getTicketPrice().toString().equals(b2c.getSalePrice().toString()) && "Y".equals(vo.getIsGn()) ? "0" :b2c.getSalePrice().toString());
				List<LCCTSPostFromSegments> fromSegments = new ArrayList<LCCTSPostFromSegments>();
				LCCTSPostFromSegments formSegment = new LCCTSPostFromSegments();
				if("PUT947".equals(b2c.getPageType()) && b2c.getType()!=null){
					formSegment.setCabinName(b2c.getType());
				}
				formSegment.setArrAirport(b2c.getDesCode());
				String arrTime = MyDateFormatUtils.getArrivalDate(new SimpleDateFormat("yyyy-MM-dd").parse(b2c.getDepDate()), b2c.getDepTime(), b2c.getDesTime());
				formSegment.setArrTime(arrTime+":00");
				formSegment.setCabin(b2c.getCabin());
				formSegment.setCabinCount(String.valueOf(b2c.getRemainSite()));
				formSegment.setCarrier(b2c.getAirlineCode());
				formSegment.setDepAirport(b2c.getDepCode());
				formSegment.setDepTime(b2c.getDepDate()+" "+b2c.getDepTime()+":00");
				formSegment.setFlightNumber(b2c.getFltNo());
				formSegment.setCabinName(b2c.getType());
				if("N".equals(b2c.getShareFlight())){
					formSegment.setCodeShare("N");
				}else{
					formSegment.setCodeShare("Y");
					formSegment.setCodeShareFltNum(b2c.getShareFlight());
				}
				fromSegments.add(formSegment);
				vo.setFromSegments(fromSegments);
				vos.add(vo);
			}else if(resultBase instanceof FlightData){
				FlightData fData = (FlightData)resultBase;
				//单程，要往返在写
				LCCTSPostVo vo = new LCCTSPostVo();
				vo.setUpdateTime(SDF_YMD.format(new Date()));
				vo.setDepAirport(fData.getDepAirport());
				vo.setArrAirport(fData.getArrAirport());
				vo.setGoDate(fData.getGoDate());
				vo.setBackDate(fData.getBackDate());
				if("PUT954,PUT964".contains(fData.getPageType())){
					vo.setPriceFrom("JQ");
				}
				else if("PUT955".equals(fData.getPageType())){
					vo.setPriceFrom("TZ");
				}
				else if("PUT956".equals(fData.getPageType())){
					vo.setPriceFrom("TR");
				}
				else if("PUT957".equals(fData.getPageType())){
					vo.setPriceFrom("VY");
				}
				else if("PUT958".equals(fData.getPageType())){
					vo.setPriceFrom("VJ");
				}
				else if("PUT959".equals(fData.getPageType())){
					vo.setPriceFrom("air5J");
				}
				else if("PUT960".equals(fData.getPageType())){
					vo.setPriceFrom("MM");
				}
				else if("PUT961".equals(fData.getPageType())){
					vo.setPriceFrom("JT");
				}
				else if("PUT962".equals(fData.getPageType())){
					vo.setPriceFrom("FY");
				}
				else if("PUT963".equals(fData.getPageType())){
					vo.setPriceFrom("JW");
				}
				else if("PUT965".equals(fData.getPageType())){
					vo.setPriceFrom("WE");
				}
				else if("PUT975".equals(fData.getPageType())){
					vo.setPriceFrom("UO");
				}
				else if("PUT932".equals(fData.getPageType())){
					vo.setPriceFrom("3UGN");
					vo.setIsGn("Y");
				}
				else if("PUT908".equals(fData.getPageType()) || "PUT935".equals(fData.getPageType()) || "PUT937".equals(fData.getPageType())){
					vo.setPriceFrom("MUGN");
					vo.setIsGn("Y");
				}
				else if("PUT903".equals(fData.getPageType()) || "PUT904".equals(fData.getPageType())){
					vo.setPriceFrom("CZGN");
					vo.setIsGn("Y");
				}
				else if("PUT926".equals(fData.getPageType())){
					vo.setPriceFrom("HUGN");
					vo.setIsGn("Y");
				}
				else if("PUT905".equals(fData.getPageType())){
					vo.setPriceFrom("HOGN");
					vo.setIsGn("Y");
				}
				else if("PUT988".equals(fData.getPageType())){
					vo.setPriceFrom("CAGN");
					vo.setIsGn("Y");
				}
				else if("PUT989".equals(fData.getPageType())){
					vo.setPriceFrom("CA");
					vo.setIsGn("N");
				}
				else if("PUT973".equals(fData.getPageType())){
					vo.setPriceFrom("HU");
					vo.setIsGn("N");
				}
				else if("PUT992".equals(fData.getPageType())){
					vo.setPriceFrom("MUTW");
					vo.setIsGn("N");
					vo.setMoneyType("TWD");
				}
				else if("PUT993".equals(fData.getPageType())){
					vo.setPriceFrom("MH");
					vo.setIsGn("N");
				}
				else if("PUT994".equals(fData.getPageType())){
					vo.setPriceFrom("EK");
					vo.setMoneyType(fData.getMemo());
					vo.setIsGn("N");
				}
				else if("PUT916".equals(fData.getPageType())){
					vo.setPriceFrom("MU");
					vo.setIsGn("N");
				}
				else if("PUT978".equals(fData.getPageType())){
					vo.setPriceFrom("JT");
					vo.setMoneyType("IDR");
					vo.setIsGn("N");
				}
				else{
					vo.setPriceFrom(fData.getAirlineCode());
				}
				if(fData.getRule()!=null){
					LCCTSPostRule rule = new LCCTSPostRule();
					rule.setRefund(fData.getRule().get(0).getRefund());
					vo.setRule(rule);
				}
				List<FlightPrice> prices = fData.getPrices();
				for (FlightPrice flightPrice : prices) {
					if ("ADT".equals(flightPrice.getPassengerType())) {
						vo.setAdultPrice(flightPrice.getFare());
						vo.setAdultTax(flightPrice.getTax()==null ||"".equals(flightPrice.getTax()) ? "0" :flightPrice.getTax());
					}else if("CHD".equals(flightPrice.getPassengerType())){
						vo.setChildPrice(flightPrice.getFare());
						vo.setChildTax(flightPrice.getTax()==null ||"".equals(flightPrice.getTax()) ? "0" :flightPrice.getTax());
					}
				}
				List<LCCTSPostFromSegments> fromSegments = new ArrayList<LCCTSPostFromSegments>();
				List<FlightSegment> fromSegments2 = fData.getFromSegments();
				for (FlightSegment flightSegment : fromSegments2) {
					LCCTSPostFromSegments formSegment1 = new LCCTSPostFromSegments();
					if("PUT988".equals(fData.getPageType()) && fData.getMemo()!=null){
						formSegment1.setCabinName(fData.getMemo());
					}
					formSegment1.setArrAirport(flightSegment.getArrAirport());
					if (flightSegment.getArrTime()!=null && !"".equals(flightSegment.getArrTime())) {
						if(flightSegment.getDepTime().length()<6){
					String arrivalDate = MyDateFormatUtils.getArrivalDate(new SimpleDateFormat("yyyy-MM-dd").parse(vo.getGoDate()), flightSegment.getDepTime(), flightSegment.getArrTime());
							formSegment1.setArrTime(arrivalDate+":00");
						}else{
							formSegment1.setArrTime(flightSegment.getArrTime());
						}
					}
					formSegment1.setCabin(flightSegment.getCabin()==null ||"".equals(flightSegment.getCabin())? "Y" :flightSegment.getCabin());
					formSegment1.setCabinCount(flightSegment.getCabinCount()==null ||"".equals(flightSegment.getCabinCount())?"9" :flightSegment.getCabinCount());
					formSegment1.setCarrier(flightSegment.getAirlineCode());
					formSegment1.setDepAirport(flightSegment.getDepAirport());
					if(flightSegment.getDepTime().length()<6){
						formSegment1.setDepTime(vo.getGoDate()+" "+flightSegment.getDepTime()+":00");
					}else{
						formSegment1.setDepTime(flightSegment.getDepTime());
					}
					formSegment1.setFlightNumber(flightSegment.getFlightNumber());
					if("".equals(flightSegment.getCodeShare()) ||"N".equals(flightSegment.getCodeShare())){
						formSegment1.setCodeShare("N");
					}else{
						formSegment1.setCodeShare("Y");
						formSegment1.setCodeShareFltNum(flightSegment.getCodeShare());
					}
					fromSegments.add(formSegment1);
				}
				vo.setFromSegments(fromSegments);
				List<LCCTSPostRetSegments> retSegments = new ArrayList<LCCTSPostRetSegments>();
				List<FlightSegment> fromSegments3 = fData.getRetSegments();
				if(fromSegments3!=null) {
					for (FlightSegment flightSegment : fromSegments3) {
						LCCTSPostRetSegments formSegment1 = new LCCTSPostRetSegments();
						if("PUT988".equals(fData.getPageType()) && fData.getMemo()!=null){
							formSegment1.setCabinName(fData.getMemo());
						}
						formSegment1.setArrAirport(flightSegment.getArrAirport());
						if (flightSegment.getArrTime()!=null && !"".equals(flightSegment.getArrTime())) {
							if(flightSegment.getDepTime().length()<6){
								String arrivalDate = MyDateFormatUtils.getArrivalDate(new SimpleDateFormat("yyyy-MM-dd").parse(vo.getGoDate()), flightSegment.getDepTime(), flightSegment.getArrTime());
								formSegment1.setArrTime(arrivalDate+":00");
							}else{
								formSegment1.setArrTime(flightSegment.getArrTime());
							}
						}
						formSegment1.setCabin(flightSegment.getCabin()==null ||"".equals(flightSegment.getCabin())? "Y" :flightSegment.getCabin());
						formSegment1.setCabinCount(flightSegment.getCabinCount()==null ||"".equals(flightSegment.getCabinCount())?"9" :flightSegment.getCabinCount());
						formSegment1.setCarrier(flightSegment.getAirlineCode());
						formSegment1.setDepAirport(flightSegment.getDepAirport());
						if(flightSegment.getDepTime().length()<6){
							formSegment1.setDepTime(vo.getGoDate()+" "+flightSegment.getDepTime()+":00");
						}else{
							formSegment1.setDepTime(flightSegment.getDepTime());
						}
						formSegment1.setFlightNumber(flightSegment.getFlightNumber());
						if("".equals(flightSegment.getCodeShare()) ||"N".equals(flightSegment.getCodeShare())){
							formSegment1.setCodeShare("N");
						}else{
							formSegment1.setCodeShare("Y");
							formSegment1.setCodeShareFltNum(flightSegment.getCodeShare());
						}
						retSegments.add(formSegment1);
					}
					vo.setRetSegments(retSegments);
				}
				vos.add(vo);
			}
		}
		LCCTSPostVo vo = (LCCTSPostVo) vos.get(0);
		lcctsPostNewVo.setDepAirport(vo.getDepAirport());
		lcctsPostNewVo.setArrAirport(vo.getArrAirport());
		lcctsPostNewVo.setIsGn(vo.getIsGn());
		lcctsPostNewVo.setPriceFrom(vo.getPriceFrom());
		lcctsPostNewVo.setGoDate(vo.getGoDate());
		lcctsPostNewVo.setFltList(vos);
		resultVos.add(lcctsPostNewVo);
		return resultVos;
	}
	public static List<LCCPostVoBase> toThisMT(List<CrawlResultBase> resultBases) throws Exception {
		List<LCCPostVoBase> vos = new ArrayList<LCCPostVoBase>();	
		for(CrawlResultBase resultBase : resultBases) {
			//新版VO，支持单程联程往返
			if(resultBase instanceof CrawlResultInter) {
				CrawlResultInter b2c = (CrawlResultInter)resultBase;
				//不支持非人民币，多航段
				if(!"CNY".equals(b2c.getCurrency()) || b2c.getFlightTrips().size()>1)continue;
				
				CrawlResultInterTrip trip = b2c.getFlightTrips().get(0);
				
				JxdMtPostVo vo = new JxdMtPostVo();
				vo.setFromCode(b2c.getDepCode());
				vo.setToCode(b2c.getDesCode());
				vo.setFltNo(trip.getFltNr());
				vo.setFltDate(b2c.getDepDate());
				vo.setSeat(trip.getCabin());
				vo.setSeatCount(String.valueOf(trip.getRemainSite()));
				if("PUT954,PUT964".contains(b2c.getPageType())){
					vo.setPriceFrom("JQ");
				}
				if("PUT955".equals(b2c.getPageType())){
					vo.setPriceFrom("TZ");
				}
				if("PUT956".equals(b2c.getPageType())){
					vo.setPriceFrom("TR");
				}
				if("PUT957".equals(b2c.getPageType())){
					vo.setPriceFrom("VY");
				}
				if("PUT958".equals(b2c.getPageType())){
					vo.setPriceFrom("VJ");
				}
				if("PUT959".equals(b2c.getPageType())){
					vo.setPriceFrom("air5J");
				}
				if("PUT960".equals(b2c.getPageType())){
					vo.setPriceFrom("MM");
				}
				if("PUT961".equals(b2c.getPageType())){
					vo.setPriceFrom("JT");
				}
				if("PUT962".equals(b2c.getPageType())){
					vo.setPriceFrom("FY");
				}
				if("PUT963".equals(b2c.getPageType())){
					vo.setPriceFrom("JW");
				}
				if("PUT965".equals(b2c.getPageType())){
					vo.setPriceFrom("WE");
				}
				vo.setFltTime(trip.getDepDate()+" "+trip.getDepTime()+":00");
				vo.setArrTime(trip.getDesDate()+" "+trip.getDesTime()+":00");
				vo.setTax(b2c.getTaxFee().toString());
				vo.setYPrice(b2c.getTicketPrice().toString());
				vo.setAdtPrice(b2c.getTicketPrice().toString());
				vo.setYuanShiPrice(b2c.getTicketPrice().toString());
				vo.setCNYPrice(b2c.getTicketPrice().toString());
				vos.add(vo);
			}else if(resultBase instanceof CrawlResultB2C) {
				CrawlResultB2C b2c = (CrawlResultB2C)resultBase;
				//不是人民币的币种不上传到MT上
				if(b2c.getType()!=null && !"".equals(b2c.getType()) && b2c.getType().contains("此价格的币种类型"))continue;
				
				JxdMtPostVo vo = new JxdMtPostVo();
				vo.setFromCode(b2c.getDepCode());
				vo.setToCode(b2c.getDesCode());
				vo.setFltNo(b2c.getFltNo());
				vo.setFltDate(b2c.getDepDate());
				vo.setSeat(b2c.getCabin());
				vo.setSeatCount(b2c.getRemainSite().toString());
				if("PUT954,PUT964".contains(b2c.getPageType())){
					vo.setPriceFrom("JQ");
				}
				if("PUT955".equals(b2c.getPageType())){
					vo.setPriceFrom("TZ");
				}
				if("PUT956".equals(b2c.getPageType())){
					vo.setPriceFrom("TR");
				}
				if("PUT957".equals(b2c.getPageType())){
					vo.setPriceFrom("VY");
				}
				if("PUT958".equals(b2c.getPageType())){
					vo.setPriceFrom("VJ");
				}
				if("PUT959".equals(b2c.getPageType())){
					vo.setPriceFrom("air5J");
				}
				if("PUT960".equals(b2c.getPageType())){
					vo.setPriceFrom("MM");
				}
				if("PUT961".equals(b2c.getPageType())){
					vo.setPriceFrom("JT");
				}
				if("PUT962".equals(b2c.getPageType())){
					vo.setPriceFrom("FY");
				}
				if("PUT963".equals(b2c.getPageType())){
					vo.setPriceFrom("JW");
				}
				if("PUT965".equals(b2c.getPageType())){
					vo.setPriceFrom("WE");
				}
				vo.setFltTime(b2c.getDepDate()+" "+b2c.getDepTime()+":00");
				vo.setArrTime(b2c.getEndDate()+" "+b2c.getDesTime()+":00");
				vo.setTax(b2c.getSalePrice().toString());
				vo.setYPrice(b2c.getTicketPrice().toString());
				vo.setAdtPrice(b2c.getTicketPrice().toString());
				vo.setYuanShiPrice(b2c.getTicketPrice().toString());
				vo.setCNYPrice(b2c.getTicketPrice().toString());
				vos.add(vo);
			}
		}
		return vos;
	}
}
