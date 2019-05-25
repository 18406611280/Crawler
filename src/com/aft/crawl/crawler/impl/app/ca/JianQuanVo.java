package com.aft.crawl.crawler.impl.app.ca;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

public class JianQuanVo {

	private String threadMark;
	
	private CaInterfaceEntity caInterfaceEntity;
	
	private Map<String, Object> optMap;
	
	private JSONObject httpsJson;

	public JianQuanVo(String threadMark, CaInterfaceEntity caInterfaceEntity,
			Map<String, Object> optMap, JSONObject httpsJson) {
		this.threadMark = threadMark;
		this.caInterfaceEntity = caInterfaceEntity;
		this.optMap = optMap;
		this.httpsJson = httpsJson;
	}

	public CaInterfaceEntity getCaInterfaceEntity() {
		return caInterfaceEntity;
	}

	public Map<String, Object> getOptMap() {
		return optMap;
	}

	public JSONObject getHttpsJson() {
		return httpsJson;
	}

	public static JianQuanVo getJianQuanVo(List<JianQuanVo> jianQuanVos, String threadMark) {
		for(JianQuanVo vo : jianQuanVos) {
			if(vo.threadMark.equals(threadMark)) return vo;
		}
		return null;
	}
}