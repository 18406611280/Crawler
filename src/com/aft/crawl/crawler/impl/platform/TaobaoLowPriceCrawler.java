package com.aft.crawl.crawler.impl.platform;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.aft.crawl.crawler.Crawler;
import com.aft.crawl.crawler.impl.CrawlerUtil;
import com.aft.crawl.result.vo.CrawlResultBase;
import com.aft.crawl.result.vo.b2c.CrawlResultB2C;
import com.aft.utils.copy.MyCopyUtils;
import com.aft.utils.jackson.MyJsonTransformUtil;

/**
 * 淘宝最低价
 */
public class TaobaoLowPriceCrawler extends Crawler {
	
	private final static String queryUrl = "http://120.26.194.214/compare/api/taobao/priceCompare";
	
	private final static String param = "<?xml version=\"1.0\" encoding=\"utf-8\"?><request><arr>%desCode%</arr><dep>%depCode%</dep><flightDate>%depDate%</flightDate><airlineCode>%airlineCode%</airlineCode><flightNo>%fltNo%</flightNo></request>";
	
	public TaobaoLowPriceCrawler(String threadMark) {
		super(threadMark, false);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<CrawlResultBase> crawl() throws Exception {
		String httpResult = this.httpResult();
		if(this.isTimeout()) return null;

		List<CrawlResultBase> crawlResults = new ArrayList<CrawlResultBase>();
		try {
			if(httpResult.contains("\"code\": 00,")) {
				logger.info(this.getJobDetail().toStr() + ", 查询返回:" + httpResult);
				return null;
			}
			Map<String, Object> mapResult = MyJsonTransformUtil.readValue(httpResult, Map.class);
			Object errorResponse = mapResult.get("error_response");
			if(null != errorResponse) {
				logger.info(this.getJobDetail().toStr() + ", 查询返回:" + httpResult);
				return null;
			}
			
			//	api_error_code 			String 			-101 	错误码
			//	api_error_msg 			String 					入参错误 	错误具体信息
			//	api_success 			Boolean 		true 	接口调用是否成功
			//	hs_lowest_product 		LowestProduct 			旗舰店产品价格
			//	jp_lowest_product 		LowestProduct 			金牌产品最低价
			//	jp_self_product 		LowestProduct 			代理商自有金牌产品最低价
			//	jx_self_product 		LowestProduct 			代理商自有精选产品最低价
			//	pt_lowest_product 		LowestProduct 			普通产品最低价
			//	pt_lowest_product_list 	LowestProduct 	[] 		普通产品TOP N最低价
			//	pt_self_product 		LowestProduct 			代理商自有普通产品最低价
			//	jx_lowest_product 		LowestProduct 			精选最低价
			
			Map<String, Object> alitripPricecompareResponse = (Map<String, Object>)mapResult.get("alitrip_pricecompare_response");
			Object apiSuccess = alitripPricecompareResponse.get("api_success");
			if(null == apiSuccess || !(Boolean)apiSuccess) {
				logger.info(this.getJobDetail().toStr() + ", 查询返回:" + httpResult);
				return null;
			}
			CrawlResultB2C crawlResult = null;
			String productType = this.getJobDetail().getTimerJob().getParamMapValueByKey("produtType");
			String[] productTypes = StringUtils.isEmpty(productType) ? null : productType.split(";");
			
			List<Map<String, Object>> products = new ArrayList<Map<String, Object>>();
			// 旗舰店产品价格
			Map<String, Object> map = (Map<String, Object>)alitripPricecompareResponse.get("hs_lowest_product");
			if(null != map) products.add(map);
			
			// 金牌产品最低价
			map = (Map<String, Object>)alitripPricecompareResponse.get("jp_lowest_product");
			if(null != map) products.add(map);
			map = (Map<String, Object>)alitripPricecompareResponse.get("jp_self_product");
			if(null != map) products.add(map);
			
			// 代理商自有金牌产品最低价
			map = (Map<String, Object>)alitripPricecompareResponse.get("jx_lowest_product");
			if(null != map) products.add(map);
			map = (Map<String, Object>)alitripPricecompareResponse.get("jx_self_product");
			if(null != map) products.add(map);
			
			// 普通产品最低价
			map = (Map<String, Object>)alitripPricecompareResponse.get("pt_lowest_product_list");
			if(null != map && !map.isEmpty()) {
				List<Map<String, Object>> ptLowestList = (List<Map<String, Object>>)map.get("lowest_product");
				if(null != ptLowestList && !ptLowestList.isEmpty()) products.addAll(ptLowestList);
			}
			map = (Map<String, Object>)alitripPricecompareResponse.get("pt_lowest_product");
			if(null != map) products.add(map);
			map = (Map<String, Object>)alitripPricecompareResponse.get("pt_self_product");
			if(null != map) products.add(map);
			
			
			CrawlResultB2C hs = null;
			CrawlResultB2C ptSelf = null;
			CrawlResultB2C ptTop1 = null;
			CrawlResultB2C ptTop2 = null;
			for(Map<String, Object> product : products) {
				String cabin = product.get("cabin").toString();
				String type = product.get("product_type").toString();
				if(null != productTypes && !ArrayUtils.contains(productTypes, type)) continue ;
				
				crawlResult = new CrawlResultB2C(this.getJobDetail(), this.getJobDetail().getAirlineCode(),
						this.getJobDetail().getFltNo(), "N", this.getJobDetail().getDepCode(),
						this.getJobDetail().getDesCode(), this.getJobDetail().getDepDate(), cabin);
				
				crawlResult.setType(type);
				crawlResult.setRemainSite(CrawlerUtil.getCabinAmount(product.get("amount").toString()));
				crawlResult.setTicketPrice(new BigDecimal(product.get("ticket_price").toString()));
				crawlResult.setSalePrice(new BigDecimal(product.get("cabin_price").toString()));
				if(!"PT_TOP".equalsIgnoreCase(type)) crawlResults.add(crawlResult);	// top 的不要,太多了...
				
				if(null == hs && "HS".equalsIgnoreCase(type)) hs = crawlResult;
				else if(null == ptSelf && "PT_SELF".equalsIgnoreCase(type)) ptSelf = crawlResult;
				else if(null == ptTop1 && "PT_TOP".equalsIgnoreCase(type)) ptTop1 = crawlResult;
				else if(null == ptTop2 && "PT_TOP".equalsIgnoreCase(type)) ptTop2 = crawlResult;
			}
			
			if(null != hs && null != ptSelf && null != ptTop1) {
				boolean add = true;
				if(ptSelf.getSalePrice().compareTo(ptTop1.getSalePrice()) > 0) {	// 最低价不是自己...取最低的 -1
					crawlResult = MyCopyUtils.deepCopy(ptTop1);
					crawlResult.setSalePrice(crawlResult.getSalePrice().subtract(new BigDecimal(1)));
				} else if(ptSelf.getSalePrice().compareTo(ptTop1.getSalePrice()) == 0 && null != ptTop2
						&& ptTop1.getSalePrice().compareTo(ptTop2.getSalePrice().add(new BigDecimal(0))) != 0
						&& ptTop1.getSalePrice().compareTo(ptTop2.getSalePrice()) != 0) {	// 最低的自己, 倒数第二的价格跟自身有差距...取倒数第二-1
					crawlResult = MyCopyUtils.deepCopy(ptTop2);
					crawlResult.setSalePrice(crawlResult.getSalePrice().subtract(new BigDecimal(1)));
				} else if(ptSelf.getSalePrice().compareTo(ptTop1.getSalePrice()) == 0 && null != ptTop2
						&& ptTop1.getSalePrice().compareTo(ptTop2.getSalePrice().add(new BigDecimal(0))) == 0) {	// 自己是最低价
					crawlResult = MyCopyUtils.deepCopy(ptSelf);
				} else add = false;
				if(add) {
					crawlResult.setType("TOP_1");
					crawlResult.setCrawlMark(ptSelf.getCrawlMark());
					crawlResult.setPageType(ptSelf.getPageType());
					crawlResult.setPageTypeMemo(ptSelf.getPageTypeMemo());
					crawlResults.add(crawlResult);
				}
			}
		} catch(Exception e) {
			logger.error(this.getJobDetail().toStr() + ", 获取航程信息异常:" + httpResult + "\r", e);
			throw e;
		}
		return crawlResults;
	}
	
	@Override
	public String httpResult() {
		String paramContent = param.replaceAll("%depCode%", this.getJobDetail().getDepCode())
							.replaceAll("%desCode%", this.getJobDetail().getDesCode())
							.replaceAll("%depDate%", this.getJobDetail().getDepDate())
							.replaceAll("%airlineCode%", this.getJobDetail().getAirlineCode())
							.replaceAll("%fltNo%", this.getJobDetail().getFltNo());
		return this.httpProxyPost(queryUrl, paramContent, "json");
	}
}