package com.aft.crawl;

public enum SwitchTypeEnum {

	B2C_CZ_01("PUT904", "南方航空官网"),
	
	
	B2C_HO_01("PUT905", "吉祥航空官网"),
	
	
	B2C_GS_01("PUT907", "天津航空官网"),
	
	
	B2C_MU_01("PUT908", "东方航空官网"),
	B2C_MU_02("PUT935", "东方航空官网(KN航班)"),
	B2C_MU_03("PUT937", "东方航空官网(会员价)"),
	B2C_MU_04("PUT941", "东方航空官网往返"),
	
	
	B2C_KN_01("PUT909", "中国联合航空官网"),
	B2C_KN_02("PUT933", "中国联合航空官网(成人=9)"),
	
	
	B2C_FU_01("PUT912", "福州航空官网"),
	
	
	B2C_NS_01("PUT913", "河北航空官网"),
	
	
	B2C_3U_01("PUT932", "四川航空官网(普通)"),
	B2C_3U_02("PUT914", "四川航空官网(会员)"),
	B2C_3U_03("PUT919", "四川航空官网(金卡)"),
	
	
	B2C_EU_01("PUT917", "成都航空官网"),
	
	
	B2C_MF_01("PUT920", "厦门航空官网"),
	
	
	B2C_DR_01("PUT918", "瑞丽航空官网"),
	
	
	B2C_UQ_01("PUT927", "乌鲁木齐航空官网"),
	
	
	B2C_HU_01("PUT926", "海南航空官网"),
	
	
	B2C_QW_01("PUT930", "青岛航空官网"),
	
	
	B2C_KY_01("PUT934", "昆明航空官网"),
	
	
	B2C_CA_APP_01("PUT911", "国际航空航空app"),
	
	
	B2C_Taobao_01("PUT801", "淘宝低价");
	
	private String pageType;
	
	private String remark;

	private SwitchTypeEnum(String pageType, String remark) {
		this.pageType = pageType;
		this.remark = remark;
	}
	
	/**
	 * 获取转换后的类型
	 * @param switchType
	 * @return
	 */
	public static SwitchTypeEnum getSwitchTypeEnum(String switchType) {
		for(SwitchTypeEnum ste : SwitchTypeEnum.values()) {
			if(ste.pageType.equals(switchType)) return ste;
		}
		return null;
	}
	
	public String getPageType() {
		return pageType;
	}

	public String getRemark() {
		return remark;
	}
}