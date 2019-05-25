package com.aft.crawl.crawler.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;

import com.aft.crawl.result.vo.common.FlightData;
import com.aft.crawl.result.vo.common.FlightPrice;
import com.aft.crawl.result.vo.inter.CrawlResultInter;
import com.aft.utils.CurrencyUtil;
import com.aft.utils.regex.MyRegexUtil;

public class CrawlerUtil {
	

	/**
	 * 不是html
	 * @param document
	 * @return
	 */
	public static boolean notHtml(Document document) {
		return document.title().contains("无法显示");
	}
	
	/**
	 * 是否 < 开头
	 * @param contnet
	 * @return
	 */
	public static boolean notXml(String contnet) {
		return !contnet.startsWith("<");
	}
	
	/**
	 * 返回舱位数量, A=10, 其他字母=0, 数字返回数字
	 * @param cabin
	 * @return
	 */
	public static int getCabinAmount(String cabin) {
		if(StringUtils.isEmpty(cabin)) return 0;
		cabin = cabin.trim();
		if(!cabin.matches(MyRegexUtil.allNumberRegex)) {	// 不是数字
			if("A".equalsIgnoreCase(cabin)) return 10;
			else return 0;
		}
		return Integer.parseInt(cabin);
	}
	public static CrawlResultInter calPrice(CrawlResultInter b2c,BigDecimal fareAmount,BigDecimal taxAmount,String currencyCode,Map<String,BigDecimal> rateMap){
		BigDecimal ticketPrice = fareAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal salePrice = taxAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
		if("CNY".equals(currencyCode)){
			b2c.setTicketPrice(b2c.getTicketPrice().add(ticketPrice));
			b2c.setTaxFee(b2c.getTaxFee().add(salePrice));
		}else{
			BigDecimal rate = rateMap.get(currencyCode);
			if(rate==null || rate.intValue()==0){
				rate = CurrencyUtil.getRequest3(currencyCode, "CNY");
			}
			if(rate.compareTo(BigDecimal.ZERO)==0){
				rate = CurrencyUtil.getRequest3(currencyCode, "CNY");
				if(rate.compareTo(BigDecimal.ZERO)==0){
					b2c.setTicketPrice(b2c.getTicketPrice().add(ticketPrice));
					b2c.setTaxFee(b2c.getTaxFee().add(salePrice));
					b2c.setCurrency(currencyCode);
				}else{
					rateMap.put(currencyCode, rate);
					BigDecimal cnyPrice = ticketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
					BigDecimal taxPrice = salePrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
					b2c.setTicketPrice(b2c.getTicketPrice().add(cnyPrice));
					b2c.setTaxFee(b2c.getTaxFee().add(taxPrice));
				}
			}else{
				rateMap.put(currencyCode, rate);
				BigDecimal cnyPrice = ticketPrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
				BigDecimal taxPrice = salePrice.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
				b2c.setTicketPrice(b2c.getTicketPrice().add(cnyPrice));
				b2c.setTaxFee(b2c.getTaxFee().add(taxPrice));
			}
		}
		return b2c;
	}

	public static FlightData calPrice(FlightData flightData,BigDecimal fareAmount,BigDecimal taxAmount,String currencyCode,Map<String,BigDecimal> rateMap) {
		List<FlightPrice> flightPriceList = new ArrayList<FlightPrice>();
		FlightPrice Price = new FlightPrice();
		BigDecimal rate = new BigDecimal(0);
		if(!"CNY".equals(currencyCode)){
			rate = rateMap.get(currencyCode);
			if(rate==null || rate.intValue()==0){
				rate = CurrencyUtil.getRequest3(currencyCode, "CNY");
				if(rate.compareTo(BigDecimal.ZERO)==0){
					rate = CurrencyUtil.getRequest3(currencyCode, "CNY");
					if(rate.compareTo(BigDecimal.ZERO)==0){
						return null;
					}
				}
			}
		}else {
			rate = new BigDecimal(1);
		}
		rateMap.put(currencyCode, rate);
		BigDecimal cnyPrice = fareAmount.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
		BigDecimal taxPrice = taxAmount.multiply(rate).setScale(0,BigDecimal.ROUND_UP);
		Price.setFare((cnyPrice.toString()));
		Price.setEquivFare(Price.getFare());
		Price.setTax((taxPrice.toString()));
		Price.setEquivTax(Price.getTax());
		Price.setCurrency("CNY");
		Price.setEquivCurrency("CNY");
		Price.setPassengerType("ADT");
		flightPriceList.add(Price);
		flightData.setPrices(flightPriceList);
		return flightData;
	}
	
}