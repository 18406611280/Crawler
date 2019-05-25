package com.aft.crawl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.aft.crawl.crawler.impl.app.B2C3KAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CCAAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CCA_MCrawler;
import com.aft.crawl.crawler.impl.app.B2CJTAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CSCAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CTRAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CTZAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CVSAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CVYAppCrawler;
import com.aft.crawl.crawler.impl.app.B2CZH_MCrawler;
import com.aft.crawl.crawler.impl.b2c.B2C3UCrawler;
import com.aft.crawl.crawler.impl.b2c.B2C3UInterCrawler;
import com.aft.crawl.crawler.impl.b2c.B2C5JCrawler;
import com.aft.crawl.crawler.impl.b2c.B2C8LCrawler;
import com.aft.crawl.crawler.impl.b2c.B2C9HCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CA6Crawler;
import com.aft.crawl.crawler.impl.b2c.B2CACCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CBKCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CCACrawler;
import com.aft.crawl.crawler.impl.b2c.B2CCZCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CCZInterCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CDRCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CDZCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CEKCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CEUCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CEYCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CFUCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CFYCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CG5Crawler;
import com.aft.crawl.crawler.impl.b2c.B2CGSCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CGXCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CHOCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CHUCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CHUInterCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CHXCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CITCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CJQCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CJRCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CJTCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CJWCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CKNCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CKYCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CMFCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CMHCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CMMCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CMUCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CMUTWCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CNHCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CNSCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CNXCrawler;
import com.aft.crawl.crawler.impl.b2c.B2COZCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CPMCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CPNCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CQWCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CSCCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CSC_ZSFCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CSQCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CTGCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CTKCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CU2Crawler;
import com.aft.crawl.crawler.impl.b2c.B2CUOCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CUQCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CVACrawler;
import com.aft.crawl.crawler.impl.b2c.B2CVJCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CVNCrawler;
import com.aft.crawl.crawler.impl.b2c.B2CWECrawler;
import com.aft.crawl.crawler.impl.b2c.B2CY8Crawler;
import com.aft.crawl.crawler.impl.b2c.B2CZHCrawler;
import com.aft.crawl.crawler.impl.other.CtripFlightChangeCrawler;
import com.aft.crawl.crawler.impl.other.EtermCabinAmountCrawler;
import com.aft.crawl.crawler.impl.other.SinaWeatherCrawler;
import com.aft.crawl.crawler.impl.other.VeryZhunFlightLineCrawler;
import com.aft.crawl.crawler.impl.other.VeryZhunFltNoCrawler;
import com.aft.crawl.crawler.impl.other.XiaoXiaoCrawler;
import com.aft.crawl.crawler.impl.platform.ELongCrawler;
import com.aft.crawl.crawler.impl.platform.GdsCrawler;
import com.aft.crawl.crawler.impl.platform.QunarHUCrawler;
import com.aft.crawl.crawler.impl.platform.TaobaoHUCrawler;
import com.aft.crawl.crawler.impl.platform.TaobaoLowPriceCrawler;

public class CrawlerType {
	
	/**
	 * PUT6XXX : 外来接口
	 * PUT7XXX : 活动抓取
	 * PUT8XXX : 平台抓取
	 * PUT9XXX : 官网抓取
	 */
	
	// ----------------------平台
	// eterm舱位数
	public final static String EtermCabinAmountPageType = "PUT800";
	
	// taobao低价
	public final static String TaobaoLowPricePageType = "PUT801";
	
	// ctrip低价
	public final static String CtripLowPricePageType = "PUT802";
	
	
	// 非常准(机场大屏)
	public final static String veryZhunFlightLinePageType = "PUT803";
	
	// 非常准(航班动态)
	public final static String veryZhunFltNoPageType = "PUT804";
	
	// 新浪机场天气
	public final static String sinaWeatherPageType = "PUT805";
	
	// 携程(航班动态)
	public final static String ctripFlightChangePageType = "PUT806";
	
	// 艺龙-机票
	public final static String elongFlightPageType = "PUT807";
	
	// ---------------------外来接口
	
	// gds接口 伽利略
	public static final String geligeoInterType = "PUT600";
	
	// gds接口 sabre
	public static final String sabreInterType = "PUT601";
	
	// gds接口 amadeus
	public static final String amaduesInterType = "PUT602";
	
	// 百翼假期
	public static final String baiYiJiaQiType = "PUT603";
	
	// 萧萧接口
	public static final String xiaoxiaoType = "PUT604";
	
	
	// ----------------------官网 单程
	
	// 香港航空官网单程
	public final static String B2CHXPageType = "PUT900";
	
	// 香港快运官网
    public final static String B2CUOPageType = "PUT975";
    
    // 狮子网页APP
    public final static String B2CJTPageType = "PUT978";
	
	// 深圳航空官网单程
	public final static String B2CZHPageType = "PUT970";
	
	// 北部湾航空官网单程
	public final static String B2CGXPageType = "PUT971";
	
	// 长安航空官网单程
	public final static String B2C9HPageType = "PUT972";
	
	// 南方航空官网(30-45)
	public final static String B2CCZ2PageType = "PUT903";
	
	// 南方航空官网国际
	public final static String B2CCZInterPageType = "PUT974";
	
	// 南方航空官网
	public final static String B2CCZPageType = "PUT904";
	
	// 吉祥航空官网
	public final static String B2CHOPageType = "PUT905";
	
	// 东航销售平台官网
//	public final static String B2TMUPageType = "PUT906";
	
	// 天津航空官网
	public final static String B2CGSPageType = "PUT907";
	
	// 东方航空官网
	public final static String B2CMUPageType = "PUT908";
	
	// 中国联航官网
	public final static String B2CKNPageType = "PUT909";
	
	// 上海航空官网
//	public final static String B2CFMPageType = "PUT910";
	
	// 国际航空官网 app
	public final static String B2CCAAppPageType = "PUT911";
	
	// 福州航空官网
	public final static String B2CFUPageType = "PUT912";
	
	// 河北航空官网
	public final static String B2CNSPageType = "PUT913";
	
	// 四川航空官网(会员)
	public final static String B2C3UMemberPageType = "PUT914";
	
	// 东航国际
	public final static String B2CMUInterPageType = "PUT915";
	
	// 东航往返国际
	public final static String B2CMURTInterPageType = "PUT916";
	
	// 成都航空
	public final static String B2CEUPageType = "PUT917";
	
	// 瑞丽航空
	public final static String B2CDRPageType = "PUT918";
	
	// 四川航空官网(金卡)
	public final static String B2C3UCardPageType = "PUT919";
	
	// 厦门航空官网
	public final static String B2CMFPageType = "PUT920";
	
	// 四川航空官网(国际)
	public final static String B2C3UInterPageType = "PUT921";
	
	// 澳门航空官网(国际)
	public final static String B2CNXInterPageType = "PUT922";
	
	// 澳门航空官网往返(国际)
	public final static String B2CNXRTInterPageType = "PUT923";
	
	// 国际航空官网 app(国际)
	public final static String B2CCAAppInterPageType = "PUT924";
	
	// 国际航空官网 app往返(国际)
	public final static String B2CCAAppRTInterPageType = "PUT925";
	
	// 海南航空官网
	public final static String B2CHUPageType = "PUT926";
	
	
	// 乌鲁木齐航空官网
	public final static String B2CUQPageType = "PUT927";
	
	// 去哪儿 海南航空官网
	public final static String QunarHUPageType = "PUT929";
	
	// 青岛航空官网
	public final static String B2CQWPageType = "PUT930";
	
	// 淘宝 海航旗舰店
	public final static String TaobaoHUPageType = "PUT931";
	
	// 四川航空官网(普通)
	public final static String B2C3UPageType = "PUT932";
	
	// 中国联航官网(查询时成人乘机人大于9)
	public final static String B2CKNAdtGt9PageType = "PUT933";
	
	// 昆明航空
	public final static String B2CKYPageType = "PUT934";
	
	// 东方航空官网(KN航班)
	public final static String B2CMU_KNPageType = "PUT935";
	
	// 泰国航空
	public final static String B2CTGPageType = "PUT936";
	
	// 东方航空 会员价
	public final static String B2CMUMemberPageType = "PUT937";
	
	// 全日空航空
	public final static String B2CNHPageType = "PUT938";
	
	// 韩亚航空
	public final static String B2COZPageType = "PUT939";
	
	// 香港航空官网往返
	public final static String B2CHXRTPageType = "PUT940";
	
	// 东方航空往返
	public final static String B2CMURTPageType = "PUT941";
	
	// 阿提哈德航空
	public final static String B2CEYPageType = "PUT942";
	
	// 越南航空
	public final static String B2CVNPageType = "PUT943";
	
	// 华夏航空
	public final static String B2CG5PageType = "PUT944";
	
	// 扬子江航空
	public final static String B2CY8PageType = "PUT945";
	
	// 西部航空
	public final static String B2CPNPageType = "PUT946";
	
	// 山东航空
	public final static String B2CSCCageType = "PUT947";
	
	// 幸福航空
	public final static String B2CJRCageType = "PUT948";
	
	// 红土航空
	public final static String B2CA6CageType = "PUT949";
	
	// 东航航空
	public final static String B2CDZCageType = "PUT950";
	
	// 奥凯航空
	public final static String B2CBKCageType = "PUT951";
	
	// 祥鹏航空
	public final static String B2C8LCageType = "PUT952";
	
	// 山东航空 app
	public final static String B2CSCAPPCageType = "PUT953";
	
	
	// 捷星航空 app
	public final static String B2C3KAPPCageType = "PUT954";
	// 酷航航空 app
	public final static String B2CTZAPPCageType = "PUT955";
	// 虎航航空 app
	public final static String B2CTRAPPCageType = "PUT956";
	// 伏林航空 app
	public final static String B2CVYAPPCageType = "PUT957";
	// 越捷航空 官网
	public final static String B2CVJCageType = "PUT958";
	// 宿务太平洋航空 官网
	public final static String B2C5JCageType = "PUT959";
	// 乐桃航空官网
	public final static String B2CMMCageType = "PUT960";
	// 狮子航空 app
	public final static String B2CJTCageType = "PUT961";
	// 飞萤航空官网
	public final static String B2CFYCageType = "PUT962";
	// 香草航空官网
	public final static String B2CJWCageType = "PUT963";
	// 捷星航空官网
	public final static String B2CJQCageType = "PUT964";
	// 微笑航空官网
	public final static String B2CWECageType = "PUT965";
	// 易捷航空官网
	public final static String B2CU2CageType = "PUT966";
	// 维珍航空官网
	public final static String B2CVSAPPCageType = "PUT967";
	// 台湾虎航官网
	public final static String B2CITCageType = "PUT968";
	// 酷航APP官网(直飞)
	public final static String B2CTZAPPOWCageType = "PUT969";
	// 海南航空国际官网
	public final static String B2CHUInterPageType = "PUT973";
	// -------------------------------活动
	// 四川航空官网(30-60)
	public final static String B2C3UHdPageType = "PUT700";
	
	// 东航国际官网(活动)
	public final static String B2CMUInterHdPageType = "PUT701";
	
	// 东航往返国际官网(活动)
	public final static String B2CMURTInterHdPageType = "PUT702";
	// 国航航空APP
	public final static String B2CCAPageType = "PUT988";
	// 国航航空APP(国际)
	public final static String B2CCAInterPageType = "PUT989";
	// 国航航空APP(国际)
	public final static String B2CPMPageType = "PUT991";
	// 东航台湾版
	public final static String B2CMUTWPageType = "PUT992";
	// 马来西亚航空
	public final static String B2CMHPageType = "PUT993";
	// 阿联酋航空
	public final static String B2CEKPageType = "PUT994";
	// 土耳其航空
	public final static String B2CTKPageType = "PUT996";
	// 新加坡航空
	public final static String B2CSQPageType = "PUT995";
	// 韩亚航空
	public final static String B2COZ2PageType = "PUT998";
	// 吉祥航空国际航空
	public final static String B2CHOInterPageType = "PUT999";
	// 加拿大航空
	public final static String B2CACPageType = "PUT1000";
	//澳大利亚航空
	public final static String B2CVAPageType = "PUT1001";
	//国航M端
	public final static String B2CCA_MPageType = "PUT1002";
	//西部航空国际
	public final static String B2CPNInterPageType = "PUT501";
	//深航M端
	public final static String B2CZH_MPageType = "PUT502";
	//山东航空掌上飞
	public final static String B2CSC_ZSFPageType = "PUT503";

		
	
	// 集合
	private final static List<CrawlerType> crawlerTypes = new ArrayList<CrawlerType>();
	
	static {
		crawlerTypes.add(new CrawlerType(VeryZhunFlightLineCrawler.class, veryZhunFlightLinePageType, "veryZhun机场大屏"));
		crawlerTypes.add(new CrawlerType(VeryZhunFltNoCrawler.class, veryZhunFltNoPageType, "veryZhun航班动态"));
		crawlerTypes.add(new CrawlerType(CtripFlightChangeCrawler.class, ctripFlightChangePageType, "携程航班动态"));
		crawlerTypes.add(new CrawlerType(SinaWeatherCrawler.class, sinaWeatherPageType, "新浪机场天气"));
		crawlerTypes.add(new CrawlerType(ELongCrawler.class, elongFlightPageType, "艺龙官网-机票"));
		
		crawlerTypes.add(new CrawlerType(GdsCrawler.class, geligeoInterType, "gds-geligeo"));
		crawlerTypes.add(new CrawlerType(GdsCrawler.class, amaduesInterType, "gds-amadues"));
		crawlerTypes.add(new CrawlerType(GdsCrawler.class, sabreInterType, "gds-sabre"));
		crawlerTypes.add(new CrawlerType(XiaoXiaoCrawler.class, xiaoxiaoType, "xiaoxiao"));
		
		
		crawlerTypes.add(new CrawlerType(B2CHXCrawler.class, B2CHXPageType, "香港航空国际Web官网", "HX"));
		crawlerTypes.add(new CrawlerType(B2CUOCrawler.class, B2CUOPageType, "香港快运Web官网", "UO"));
		crawlerTypes.add(new CrawlerType(B2CJTCrawler.class, B2CJTPageType, "狮子网页APP", "JT"));
		crawlerTypes.add(new CrawlerType(B2CHXCrawler.class, B2CHXRTPageType, "香港航空国际Web官网(往返)", "HX"));
		
		crawlerTypes.add(new CrawlerType(B2CNXCrawler.class, B2CNXInterPageType, "澳门航空国际Web官网", "NX"));
		crawlerTypes.add(new CrawlerType(B2CNXCrawler.class, B2CNXRTInterPageType, "澳门航空国际往返Web官网", "NX"));
		
		
		crawlerTypes.add(new CrawlerType(B2CCZCrawler.class, B2CCZ2PageType, "南方航空Web官网(30-45)", "CZ"));
		crawlerTypes.add(new CrawlerType(B2CCZCrawler.class, B2CCZPageType, "南方航空Web官网", "CZ"));
		crawlerTypes.add(new CrawlerType(B2CCZInterCrawler.class, B2CCZInterPageType, "南方航空Web官网(国际)", "CZ"));
		
		crawlerTypes.add(new CrawlerType(B2CCAAppCrawler.class, B2CCAAppPageType, "国际航空App官网", "CA"));
		
		crawlerTypes.add(new CrawlerType(B2CHOCrawler.class, B2CHOPageType, "吉祥航空Web官网", "HO"));
		crawlerTypes.add(new CrawlerType(B2CHOCrawler.class, B2CHOInterPageType, "吉祥航空Web官网国际", "HO"));
		crawlerTypes.add(new CrawlerType(B2CGSCrawler.class, B2CGSPageType, "天津航空Web官网", "GS"));
		
		crawlerTypes.add(new CrawlerType(B2CKNCrawler.class, B2CKNPageType, "联合航空Web官网", "KN"));
		crawlerTypes.add(new CrawlerType(B2CKNCrawler.class, B2CKNAdtGt9PageType, "联合航空Web官网(成人=9)", "KN"));
		
		crawlerTypes.add(new CrawlerType(B2CFUCrawler.class, B2CFUPageType, "福州航空Web官网", "FU"));
		crawlerTypes.add(new CrawlerType(B2CNSCrawler.class, B2CNSPageType, "河北航空Web官网", "NS"));

		crawlerTypes.add(new CrawlerType(B2C3UCrawler.class, B2C3UPageType, "四川航空Web官网(普通)", "3U"));
		crawlerTypes.add(new CrawlerType(B2C3UCrawler.class, B2C3UMemberPageType, "四川航空Web官网(会员)", "3U"));
		crawlerTypes.add(new CrawlerType(B2C3UCrawler.class, B2C3UCardPageType, "四川航空Web官网(金卡)", "3U"));
		crawlerTypes.add(new CrawlerType(B2C3UCrawler.class, B2C3UHdPageType, "四川航空Web官网(30-60)", "3U"));
		crawlerTypes.add(new CrawlerType(B2CACCrawler.class, B2CACPageType, "加拿大航空官网", "AC"));
		crawlerTypes.add(new CrawlerType(B2C3UInterCrawler.class, B2C3UInterPageType, "四川航空官网(国际)", "3U"));
		
		
		
		crawlerTypes.add(new CrawlerType(B2CEUCrawler.class, B2CEUPageType, "成都航空Web官网", "EU"));
		crawlerTypes.add(new CrawlerType(B2CPMCrawler.class, B2CPMPageType, "鹏明B2B平台官网", "PM"));
		crawlerTypes.add(new CrawlerType(B2CZHCrawler.class, B2CZHPageType, "深圳航空Web官网", "ZH"));
		crawlerTypes.add(new CrawlerType(B2CGXCrawler.class, B2CGXPageType, "北部湾航空Web官网", "GX"));
		crawlerTypes.add(new CrawlerType(B2C9HCrawler.class, B2C9HPageType, "长安航空Web官网", "9H"));
		crawlerTypes.add(new CrawlerType(B2CDRCrawler.class, B2CDRPageType, "瑞丽航空Web官网", "DR"));
		crawlerTypes.add(new CrawlerType(B2CMFCrawler.class, B2CMFPageType, "厦门航空Web官网", "MF"));
		crawlerTypes.add(new CrawlerType(B2CCACrawler.class, B2CCAPageType, "国航航空APP官网", "CA"));
		crawlerTypes.add(new CrawlerType(B2CCA_MCrawler.class, B2CCA_MPageType, "国航航空M端", "CA"));
		crawlerTypes.add(new CrawlerType(B2CZH_MCrawler.class, B2CZH_MPageType, "深圳航空M端", "ZH"));
		crawlerTypes.add(new CrawlerType(B2CMHCrawler.class, B2CMHPageType, "马来西亚航空官网", "MH"));
		crawlerTypes.add(new CrawlerType(B2CTKCrawler.class, B2CTKPageType, "土耳其航空官网", "TK"));
		crawlerTypes.add(new CrawlerType(B2CEKCrawler.class, B2CEKPageType, "阿联酋航空官网", "EK"));
		crawlerTypes.add(new CrawlerType(B2CSQCrawler.class, B2CSQPageType, "新加坡航空官网", "SQ"));
		crawlerTypes.add(new CrawlerType(B2CTKCrawler.class, B2CTKPageType, "土耳其航空官网", "TK"));
		crawlerTypes.add(new CrawlerType(B2CCACrawler.class, B2CCAInterPageType, "国航航空APP官网(国际)", "CA"));
		crawlerTypes.add(new CrawlerType(B2CHUCrawler.class, B2CHUPageType, "海南航空Web官网", "HU"));
		crawlerTypes.add(new CrawlerType(B2CHUInterCrawler.class, B2CHUInterPageType, "海南航空Web官网国际", "HU"));
		crawlerTypes.add(new CrawlerType(B2CUQCrawler.class, B2CUQPageType, "乌鲁木齐航空Web官网", "UQ"));
		crawlerTypes.add(new CrawlerType(B2CQWCrawler.class, B2CQWPageType, "青岛航空Web官网", "QW"));
		crawlerTypes.add(new CrawlerType(B2CKYCrawler.class, B2CKYPageType, "昆明航空Web官网", "KY"));
		crawlerTypes.add(new CrawlerType(B2CTGCrawler.class, B2CTGPageType, "泰国航空Web官网", "TG"));
		crawlerTypes.add(new CrawlerType(B2CNHCrawler.class, B2CNHPageType, "全日空航空Web官网", "NH"));
		crawlerTypes.add(new CrawlerType(B2COZCrawler.class, B2COZPageType, "韩亚航空Web官网", "OZ"));
		crawlerTypes.add(new CrawlerType(B2COZCrawler.class, B2COZ2PageType, "韩亚航空Web官网(15-30)", "OZ"));
		crawlerTypes.add(new CrawlerType(B2CEYCrawler.class, B2CEYPageType, "阿提哈德航空Web官网", "EY"));
		crawlerTypes.add(new CrawlerType(B2CVNCrawler.class, B2CVNPageType, "越南航空Web官网", "VN"));
		crawlerTypes.add(new CrawlerType(B2CVACrawler.class, B2CVAPageType, "澳大利亚Web官网", "VA"));
		crawlerTypes.add(new CrawlerType(B2CG5Crawler.class, B2CG5PageType, "华夏航空Web官网", "G5"));
		crawlerTypes.add(new CrawlerType(B2CY8Crawler.class, B2CY8PageType, "扬子江航空Web官网", "Y8"));
		crawlerTypes.add(new CrawlerType(B2CPNCrawler.class, B2CPNPageType, "西部航空Web官网", "PN"));
		crawlerTypes.add(new CrawlerType(B2CPNCrawler.class, B2CPNInterPageType, "西部航空Web国际官网", "PN"));
		crawlerTypes.add(new CrawlerType(B2CSCCrawler.class, B2CSCCageType, "山东航空Web官网", "SC"));
		crawlerTypes.add(new CrawlerType(B2CSC_ZSFCrawler.class, B2CSC_ZSFPageType, "山东航空掌上飞web官网", "SC"));
		crawlerTypes.add(new CrawlerType(B2CJRCrawler.class, B2CJRCageType, "幸福航空Web官网", "JR"));
		crawlerTypes.add(new CrawlerType(B2CA6Crawler.class, B2CA6CageType, "红土航空Web官网", "A6"));
		crawlerTypes.add(new CrawlerType(B2CDZCrawler.class, B2CDZCageType, "东海航空Web官网", "DZ"));
		crawlerTypes.add(new CrawlerType(B2CBKCrawler.class, B2CBKCageType, "奥凯航空Web官网", "BK"));
		crawlerTypes.add(new CrawlerType(B2C8LCrawler.class, B2C8LCageType, "祥鹏航空Web官网", "8L"));
		crawlerTypes.add(new CrawlerType(B2CSCAppCrawler.class, B2CSCAPPCageType, "山东航空App官网", "SC"));
		crawlerTypes.add(new CrawlerType(B2C3KAppCrawler.class, B2C3KAPPCageType, "捷星航空App官网", "3K"));
		crawlerTypes.add(new CrawlerType(B2CTZAppCrawler.class, B2CTZAPPCageType, "酷航航空App官网", "TZ"));
		crawlerTypes.add(new CrawlerType(B2CTZAppCrawler.class, B2CTZAPPOWCageType, "酷航航空App官网(直飞)", "TZ"));
		crawlerTypes.add(new CrawlerType(B2CTRAppCrawler.class, B2CTRAPPCageType, "虎航航空App官网", "TR"));
		crawlerTypes.add(new CrawlerType(B2CVYAppCrawler.class, B2CVYAPPCageType, "伏林航空App官网", "VY"));
		crawlerTypes.add(new CrawlerType(B2CVJCrawler.class, B2CVJCageType, "越捷航空官网", "VJ"));
		crawlerTypes.add(new CrawlerType(B2C5JCrawler.class, B2C5JCageType, "宿务太平洋航空官网", "5J"));
		crawlerTypes.add(new CrawlerType(B2CMMCrawler.class, B2CMMCageType, "乐桃航空官网", "MM"));
		crawlerTypes.add(new CrawlerType(B2CJTAppCrawler.class, B2CJTCageType, "狮子航空App官网", "JT"));
		crawlerTypes.add(new CrawlerType(B2CFYCrawler.class, B2CFYCageType, "飞萤航空官网", "FY"));
		crawlerTypes.add(new CrawlerType(B2CJWCrawler.class, B2CJWCageType, "香草航空官网", "JW"));
		crawlerTypes.add(new CrawlerType(B2CJQCrawler.class, B2CJQCageType, "捷星航空官网", "JQ"));
		crawlerTypes.add(new CrawlerType(B2CWECrawler.class, B2CWECageType, "微笑航空官网", "WE"));
		crawlerTypes.add(new CrawlerType(B2CU2Crawler.class, B2CU2CageType, "易捷航空官网", "U2"));
		crawlerTypes.add(new CrawlerType(B2CVSAppCrawler.class, B2CVSAPPCageType, "维珍航空APP官网", "VS"));
		crawlerTypes.add(new CrawlerType(B2CITCrawler.class, B2CITCageType, "台湾虎航官网", "IT"));
		
		
		
		crawlerTypes.add(new CrawlerType(QunarHUCrawler.class, QunarHUPageType, "去哪儿-海南航空旗舰店", "HU"));
		crawlerTypes.add(new CrawlerType(TaobaoHUCrawler.class, TaobaoHUPageType, "淘宝-海南航空旗舰店", "HU"));
		
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMUPageType, "东方航空Web官网", "MU"));
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMURTPageType, "东方航空Web官网往返", "MU"));
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMU_KNPageType, "东方航空Web官网(KN航班)", "KN"));
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMUMemberPageType, "东方航空Web官网(会员价)", "MU"));
		
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMUInterPageType, "东方航空国际Web官网", "MU"));
		crawlerTypes.add(new CrawlerType(B2CMUTWCrawler.class, B2CMUTWPageType, "东航台湾版官网", "MU"));
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMURTInterPageType, "东方航空国际往返Web官网", "MU"));
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMUInterHdPageType, "东方航空国际Web官网(活动)", "MU"));
		crawlerTypes.add(new CrawlerType(B2CMUCrawler.class, B2CMURTInterHdPageType, "东方航空国际往返Web官网(活动)", "MU"));
		
		
		crawlerTypes.add(new CrawlerType(EtermCabinAmountCrawler.class, EtermCabinAmountPageType, "eterm舱位数"));
		crawlerTypes.add(new CrawlerType(TaobaoLowPriceCrawler.class, TaobaoLowPricePageType, " taobao低价"));
	}
	
	/**
	 * 获取对应的 CrawlerType
	 * @param pageType
	 * @return
	 */
	public static CrawlerType getCrawlerType(String pageType) {
		for(CrawlerType crawlerType : crawlerTypes) {
			if(!crawlerType.getPageType().equals(pageType)) continue ;
			return crawlerType;
		}
		return null;
	}
	
	/**
	 * 获取对应的 Crawler class
	 * @param pageType
	 * @return
	 */
	public static Class<?> getCrawlerClass(String pageType) {
		CrawlerType crawlerType = CrawlerType.getCrawlerType(pageType);
		if(null == crawlerType) return null;
		return crawlerType.getCrawlerClass();
	}
	
	private Class<?> crawlerClass;
	
	private String pageType;
	
	private String pageTypeMemo;
	
	private String crawlerFlag;

	private CrawlerType(Class<?> crawlerClass, String pageType, String pageTypeMemo) {
		this(crawlerClass, pageType, pageTypeMemo, null);
	}
	
	private CrawlerType(Class<?> crawlerClass, String pageType, String pageTypeMemo, String crawlerFlag) {
		this.crawlerClass = crawlerClass;
		this.pageType = pageType;
		this.pageTypeMemo = pageTypeMemo;
		this.crawlerFlag = crawlerFlag;
	}
	
	public Class<?> getCrawlerClass() {
		return crawlerClass;
	}

	public String getPageType() {
		return pageType;
	}

	public String getPageTypeMemo() {
		return pageTypeMemo;
	}

	public String getCrawlerFlag() {
		return crawlerFlag;
	}
	
	public String toStr() {
		return this.pageType + "-" + (StringUtils.isNotEmpty(this.crawlerFlag) ? this.crawlerFlag + "-" : "") + this.pageTypeMemo;
	}
}