package com.aft.crawl.crawler.impl.app.ca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.aft.crawl.bean.JobDetail;
import com.aft.logger.MyCrawlerLogger;
import com.aft.utils.jackson.MyJsonTransformUtil;


public class CaService {
	
	private final static List<JianQuanVo> jianQuanVos = new ArrayList<JianQuanVo>();
	
	/**
	 * 平台接口格式 {"statuCode":"success","statuMessage":""}
	 * @param jobDetail
	 * @param threadMark
	 * @param flight
	 * @return
	 */
	public Map<String, Object> flightQuery(JobDetail jobDetail, String threadMark, FlightQuery flight) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		long startTime = System.currentTimeMillis();
		try {
			
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 开始 CAAPP 政策查询...");
			JianQuanVo jianQuanVo = JianQuanVo.getJianQuanVo(jianQuanVos, threadMark);
			
			// https请求需要的参数
			CaInterfaceEntity interfaceEntity = null;
			Map<String, Object> optMap = null;
			JSONObject httpsJson = null;
			if(null == jianQuanVo) {
				interfaceEntity = new CaInterfaceEntity();	// https请求需要的参数
				/** 接口需要的参数 */
				interfaceEntity.setDeviceId(UUID.randomUUID().toString().toUpperCase()); // 设置设备ID
				
				/** 1.第一次鉴权 */
				optMap = this.firstJianQuan(flight, interfaceEntity, jobDetail);
				if(!"0".equals(optMap.get("type"))) {
					resultMap.put("statuCode", optMap.get("type"));
					resultMap.put("statuMessage", optMap.get("msg"));
					return resultMap;
				}
				
				/** https请求需要的参数 */
				httpsJson = JSONObject.fromObject(optMap.get("msg"));
				httpsJson.put("deviceId", interfaceEntity.getDeviceId());
				
				/** 2.鉴权参数 */
				optMap = this.authentication(httpsJson, jobDetail);
				if (!"0".equals(optMap.get("type"))) {
					resultMap.put("statuCode", "error");
					resultMap.put("statuMessage", optMap.get("msg"));
					return resultMap;
				}
				
				/** 3.第二次鉴权 */
				optMap = this.iosSecond(flight, httpsJson, jobDetail);
				if (!"0".equals(optMap.get("type"))) {
					resultMap.put("statuCode", "error");
					resultMap.put("statuMessage", optMap.get("msg"));
					return resultMap;
				}
			} else {
				interfaceEntity = jianQuanVo.getCaInterfaceEntity();
				optMap = jianQuanVo.getOptMap();
				httpsJson = jianQuanVo.getHttpsJson();
			}

			/** 4.航班查询,航班查询成功后才将订单状态改为出票中 */
			optMap = this.iosFlightInfo(flight, interfaceEntity, httpsJson, jobDetail);
			if (!"0".equals(optMap.get("type"))) {
				resultMap.put("statuCode", "error");
				resultMap.put("statuMessage", optMap.get("msg"));
				return resultMap;
			}

//			/** 5.航班详情查 */
//			map = iosFilghtDatail(flight, interfaceEntity, httpsJson);
//			if (!"0".equals(map.get("type"))) {
//				resultMap.put("statuCode", "error");
//				resultMap.put("statuMessage", map.get("msg"));
//				return resultMap;
//			}
			resultMap.put("statuCode", "success");
			resultMap.put("statuMessage", optMap.get("flightInfo"));
			if(null == jianQuanVo) jianQuanVos.add(new JianQuanVo(threadMark, interfaceEntity, optMap, httpsJson));
			return resultMap;
		} catch(Exception e) {
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", CAAPP航班查询异常", e);
			resultMap.put("statuCode", "error");
			resultMap.put("statuMessage", "查询异常:" + e);
			return resultMap;
		} finally {
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 结束CAAPP航班查询, 所用时间:" + (System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * 5.航班详情
	 * 
	 * @param outOrderNo
	 * @param orderEntity
	 * @param interfaceEntity
	 * @param httpsJson
	 * @return
	 */
	private Map<String, Object> iosFilghtDatail(FlightQuery flight,
			CaInterfaceEntity interfaceEntity, JSONObject httpsJson, JobDetail jobDetail) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String flightDetailsResult = "";
			JSONObject jsonFlightDetailReq = new JSONObject();
			/**
			 * 判断是否有共享航班号过滤
			 */
			String flightNo = interfaceEntity.getFlightNo();
			jsonFlightDetailReq.put("infantNum", "0");
			jsonFlightDetailReq.put("flightID", interfaceEntity.getFlightID());
			jsonFlightDetailReq.put("flightno", flightNo);
			jsonFlightDetailReq.put("arrivedate", interfaceEntity.getArrDate() + "T" + interfaceEntity.getArrTime()); // 达到日期
			jsonFlightDetailReq.put("searchId", interfaceEntity.getSearchId());
			jsonFlightDetailReq.put("airline", interfaceEntity.getAirline());
			jsonFlightDetailReq.put("adultNum", CaTool.ADULTNUM); // 成人个数，这里填1吧
			// 都是一个
			jsonFlightDetailReq.put("dst", flight.getArr());
			jsonFlightDetailReq.put("org", flight.getDep());
			jsonFlightDetailReq.put("takeoffdate", flight.getDepDate() + "T"
					+ interfaceEntity.getDepTime()); // 出发日期
			jsonFlightDetailReq.put("childNum", "0");
			jsonFlightDetailReq.put("flag", "0");

			StringBuffer flightDetailSb = new StringBuffer();
			flightDetailSb.append("[{\"req\":\"" + jsonFlightDetailReq.toString().replace("\"", "\\\"") + "\",");
			flightDetailSb.append("\"token\":\"" + CaTool.TOKEN + "\",\"lang\":\"zh_CN\"}]");
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班详情请求参数：" + flightDetailSb.toString());
			String parmFlightDetail = "__wl_deviceCtx=Asr_244q0jiwqBAA&adapter=ACFlight&compressResponse=true&isAjaxRequest=true&"
					+ "parameters=" + flightDetailSb.toString() + "&procedure=qryFlightDetail";
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班详情参数：" + parmFlightDetail);
			flightDetailsResult = CaTool.httpsResult(flight.getIp(), flight.getPort(), CaTool.CAAPPURL, parmFlightDetail, httpsJson.toString(), 0);
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班详情:" + flightDetailsResult);
			if(StringUtils.isEmpty(flightDetailsResult)) {
				map.put("type", "1");
				map.put("msg", "航班详情查询失败,航班查询返回结果为空");
				return map;
			}
			flightDetailsResult = flightDetailsResult.replace("/*-secure-", "");
			flightDetailsResult = flightDetailsResult.replace("*/", "");
			JSONObject datailsJson = JSONObject.fromObject(flightDetailsResult);
			JSONObject datailRespJson = datailsJson.getJSONObject("resp");
			String code = datailRespJson.getString("code");
			if (!CaTool.CODE.equals(code)) {
				MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班详情查询失败,返回结果:" + flightDetailsResult);
				map.put("type", "1");
				map.put("msg", "航班详情查询失败");
				return map;
			}
			JSONArray ffCabins = datailRespJson.getJSONArray("FFCabins");
			if (ffCabins.size() == 0) {
				MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班详情查询失败,没有舱位信息。");
				map.put("type", "1");
				map.put("msg", "航班详情查询失败,没有舱位信息");
				return map;
			} else {
				map.put("type", "0");
				map.put("msg", "查询航班成功");
			}
			JSONObject flightJson = new JSONObject();
			flightJson.put("airlineCode", interfaceEntity.getAirline());
			flightJson.put("flightNo", flightNo);
			flightJson.put("dep", flight.getDep());
			flightJson.put("arr", flight.getArr());
			flightJson.put("depTime", interfaceEntity.getDepTime());
			flightJson.put("arrTime", interfaceEntity.getArrTime());
			flightJson.put("depDate", flight.getDepDate());
			JSONArray flightDetail = new JSONArray();
			for(int i = 0; i < ffCabins.size(); i++) {
				JSONObject object = new JSONObject();
				JSONObject temp = (JSONObject)ffCabins.get(i);
				String seatSum = "";	// 座位数
				if(StringUtils.isEmpty(temp.getString("surplusTicket"))) seatSum = "10";
				else seatSum = temp.getString("surplusTicket");
				object.put("cabin", temp.getString("ffcabinId"));
				object.put("price", temp.getString("price"));
				object.put("seatSum", seatSum);
				flightDetail.add(object);
			}
			flightJson.put("flightDetails", flightDetail);
			map.put("flightInfo", flightJson);
		} catch (Exception e) {
			map.put("type", "1");
			map.put("msg", "查询航班详情异常," + e.getMessage());
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", 航班详情查询异常，", e);
		}
		return map;
	}

	/**
	 * 4.航班查询
	 */
	private Map<String, Object> iosFlightInfo(FlightQuery flight,
			CaInterfaceEntity interfaceEntity, JSONObject httpsJson, JobDetail jobDetail) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String flightResut = "";
			String timestamp = Long.toString(System.currentTimeMillis());
			JSONObject jsonFlightReq = new JSONObject();
			jsonFlightReq.put("version", CaTool.VERSION);
			jsonFlightReq.put("token", "11111111");
			jsonFlightReq.put("org", flight.getDep());// 出发地
			jsonFlightReq.put("backDate", "");// 返回日期，留空就好
			jsonFlightReq.put("inf", "0");
			jsonFlightReq.put("dst", flight.getArr());// 目的地
			jsonFlightReq.put("cnn", "0");
			jsonFlightReq.put("date", flight.getDepDate());// 航班日期
			jsonFlightReq.put("timestamp", timestamp);
			jsonFlightReq.put("cabin", "Economy"); // 舱位 经济舱
			jsonFlightReq.put("flag", "0");
			jsonFlightReq.put("adt", "1"); // 1个成人

			StringBuffer flightSb = new StringBuffer();
			flightSb.append("[{\"req\":\"" + jsonFlightReq.toString().replace("\"", "\\\"") + "\",");
			flightSb.append("\"token\":\"" + CaTool.TOKEN + "\",\"lang\":\"zh_CN\"}]");
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班查询请求参数" + flightSb.toString());
			String parmFlight = "__wl_deviceCtx=A4UOI3y_7iiwqBAA&adapter=ACFlight&compressResponse=true&isAjaxRequest=true&"
					+ "parameters=" + flightSb.toString() + "&procedure=qryFlights";
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班查询参数:" + parmFlight);

			String flightNo = flight.getFlightNo();	// 判断是否有共享航班号过滤
			// 是否更新订单状态
			flightResut = CaTool.httpsResult(flight.getIp(), flight.getPort(), CaTool.CAAPPURL, parmFlight, httpsJson.toString(), 0);
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班查询结果:" + flightResut);
			if(StringUtils.isEmpty(flightResut)) {
				map.put("type", "1");
				map.put("msg", "查询航班失败,返回结果为空");
				return map;
			}
			flightResut = flightResut.replace("/*-secure-", "");
			flightResut = flightResut.replace("*/", "");
			JSONObject flightJosn = JSONObject.fromObject(flightResut);
			JSONObject flightRespJson = flightJosn.getJSONObject("resp");
			boolean isSuccessful = flightJosn.getBoolean("isSuccessful");
			String statusReason = flightJosn.getString("statusReason");
			if(!isSuccessful || !"OK".equals(statusReason)) {
				MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班查询失败,返回结果:" + flightResut);
				map.put("type", "1");
				map.put("msg", "航班查询失败");
				return map;
			}
			if(null == flightRespJson) {
				MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班查询失败,没有指定航班数据,返回结果:" + flightResut);
				map.put("type", "1");
				map.put("msg", "没有指定航班数据");
				return map;
			}
			String code = flightRespJson.getString("code");
			if(!CaTool.CODE.equals(code)) {
				MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班查询失败,返回结果:" + flightResut);
				map.put("type", "1");
				map.put("msg", "航班查询失败");
				return map;
			}
			JSONObject flightGotoObject = flightRespJson.getJSONObject("goto");
			/** 航班信息 */
			JSONArray flightInfomationList = flightGotoObject.getJSONArray("flightInfomationList");
			boolean isFlight = false;
			JSONArray flightInfoArray = new JSONArray();
			// 航班数据
			for(int i = 0; i < flightInfomationList.size(); i++) {
				JSONObject temp = (JSONObject) flightInfomationList.get(i);
				JSONArray flightSegmentList = temp.getJSONArray("flightSegmentList");
				// 转乘的航班过滤
				if(flightSegmentList.size() > 1) continue;
				
				for(int j = 0; j < flightSegmentList.size(); j++) {
					JSONObject tempObject = (JSONObject) flightSegmentList.get(j);
					// 出发机场航站楼
					interfaceEntity.setDepartureTerminal(tempObject.getString("flightTerminal"));
					// 到达机场航站楼
					interfaceEntity.setArrivalTerminal(tempObject.getString("flightHTerminal"));
					// 航班搜索searchId
					interfaceEntity.setSearchId(tempObject.getString("searchId"));
					// 航司
					interfaceEntity.setAirline(tempObject.getString("operatingAirline"));
					// 是否共享
					interfaceEntity.setShareFlag(tempObject.getString("isShared"));
					String flightDeptimePlan = tempObject.getString("flightDeptimePlan");
					String flightArrtimePlan = tempObject.getString("flightArrtimePlan");
					if(StringUtils.isNotEmpty(flightDeptimePlan) && flightDeptimePlan.length() > 7) {
						interfaceEntity.setDepTime(flightDeptimePlan.substring(0, 5));
					}
					if(StringUtils.isNotEmpty(flightArrtimePlan) && flightArrtimePlan.length() > 7) {
						interfaceEntity.setArrTime(flightArrtimePlan.substring(0, 5));
					}
					// 到达日期 yyyy-MM-dd
					interfaceEntity.setArrDate(tempObject.getString("flightArrdatePlan"));
					// 搜索id
					interfaceEntity.setFlightID(temp.getString("flightID"));
					//航班号
					interfaceEntity.setFlightNo(tempObject.getString("flightNo"));
					
					
//					// 指定获取的航班号
					if(null == flightNo || "".equals(flightNo)){
						isFlight = true;
						map = iosFilghtDatail(flight,interfaceEntity, httpsJson, jobDetail);
						if("0".equals(map.get("type"))) flightInfoArray.add(map.get("flightInfo"));
					}else{
						if(!flightNo.equals(tempObject.getString("flightNo"))) {
							continue;
						} else {
							isFlight = true;
							map = iosFilghtDatail(flight,interfaceEntity, httpsJson, jobDetail);
							if("0".equals(map.get("type"))) flightInfoArray.add(map.get("flightInfo"));
							break;
						}
					}
				}
			}
			if(!isFlight) {
				MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 航班查询失败,没有查询到指定的航班信息");
				map.put("type", "1");
				map.put("msg", "航班查询失败,没有查询到指定的航班信息");
			} else {
				map.put("type", "0");
				map.put("msg", "查询航班成功");
				map.put("flightInfo", flightInfoArray);
			}
		} catch (Exception e) {
			map.put("type", "1");
			map.put("msg", "查询航班异常," + e.getMessage());
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", 航班查询异常，", e);
		}
		return map;
	}

	/**
	 * 3.第二次鉴权
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private Map<String, Object> iosSecond(FlightQuery flight, JSONObject httpsJson, JobDetail jobDetail) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String resultSecond = "";
			String parmSecond = "__wl_deviceCtx=Ad5xR_zx1_lsqBAA&adapter=ACMCommon&compressResponse=true&isAjaxRequest=true&procedure=queryBannerList&"
								+ "parameters=[{\"req\":\"{}\",\"token\":\"" + CaTool.TOKEN + "\",\"lang\":\"zh_CN\"}]";
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 第二次鉴权参数：" + parmSecond);
			resultSecond = CaTool.httpsResult(flight.getIp(), flight.getPort(), CaTool.CAAPPURL, parmSecond, httpsJson.toString(), 0);
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 第二次鉴权返回结果：" + resultSecond);
			if (null == resultSecond || "".equals(resultSecond)) {
				map.put("type", "1");
				map.put("msg", "第一次鉴权失败,返回结果为空");
				return map;
			}
			JSONObject secondJson = JSONObject.fromObject(resultSecond);
			int statusCode = secondJson.getInt("statusCode");
			boolean isSuccessful = secondJson.getBoolean("isSuccessful"); // true
			// 为成功
			if (statusCode != 200 || !isSuccessful) {
				map.put("type", "1");
				map.put("msg", "第二次鉴权失败,鉴权没通过");
			} else {
				map.put("type", "0");
				map.put("msg", "第二次鉴权成功");
			}
		} catch (Exception e) {
			map.put("type", "1");
			map.put("msg", "第二次鉴权异常");
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", 第二次鉴权异常，", e);
		}
		return map;
	}

	/**
	 * 2.鉴权算法
	 * 
	 * @param wlChakkengData
	 * @return
	 */
	private Map<String, Object> authentication(JSONObject httpsJson, JobDetail jobDetail) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String WL_Challenge_Data = httpsJson.getString("WL_Challenge_Data");
			WL_Challenge_Data = WL_Challenge_Data.substring(0, WL_Challenge_Data.indexOf("S+")) + "S";
			String msg = AirchinaAuth.getChallengeAnswer(WL_Challenge_Data);
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", 鉴权算法返回:" + msg);
			httpsJson.put("wl_authenticityRealm", msg);
			resultMap.put("type", "0");
			resultMap.put("msg", "鉴权算法参数成功");
		} catch (Exception e) {
			resultMap.put("type", "1");
			resultMap.put("msg", "第一次鉴权异常");
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", 鉴权算法异常，", e);
		}
		return resultMap;
	}

	/**
	 * 1.第一次鉴权
	 * @param flight
	 * @param interfaceEntity
	 * @param timerJob
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> firstJianQuan(FlightQuery flight, CaInterfaceEntity interfaceEntity, JobDetail jobDetail) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String parmStart = "__wl_deviceCtx=Ad5xR_zx1_lsqBAA&adapter=ACMCommon&compressResponse=true&isAjaxRequest=true&procedure=queryBannerList&"
					+ "parameters=" + CaTool.urlChange("[{\"req\":\"{}\",\"secureToken\":\"AppSecureToken\",\"token\":\""
									+ CaTool.TOKEN + "\",\"lang\":\"zh_CN\"}]");

			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 初始化鉴权参数：" + parmStart);
			String jianQuanResult = CaTool.firstJianQuanPost(flight.getIp(), flight.getPort(), CaTool.CAAPPURL, parmStart, interfaceEntity.getDeviceId(), 0);
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).info(jobDetail.toStr() + ", 初始化鉴权返回结果：" + jianQuanResult);
			if("forbidden".equals(jianQuanResult)) {
				resultMap.put("type", "-1");
				resultMap.put("msg", "第一次鉴权失败,403拒绝访问");
				return resultMap;
			}
			Map<String, Object> jsonMap = MyJsonTransformUtil.readValue(jianQuanResult, Map.class);
			String WL_Challenge_Data = (String)jsonMap.get("WL_Challenge_Data"); // 鉴权算法需要这个参数
			if(StringUtils.isNotEmpty(WL_Challenge_Data)) {
				resultMap.put("type", "0");
				resultMap.put("msg", jianQuanResult);
				return resultMap;
			}
			resultMap.put("type", "1");
			resultMap.put("msg", "第一次鉴权失败,WL_Challenge_Data返回空");
		} catch(Exception e) {
			resultMap.put("type", "1");
			resultMap.put("msg", "第一次鉴权异常," + e);
			MyCrawlerLogger.getCrawlerLogger(jobDetail.getTimerJob()).error(jobDetail.toStr() + ", 第一次鉴权异常，", e);
		}
		return resultMap;
	}
}