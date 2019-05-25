package com.aft.crawl.result.vo.gds;

import java.io.Serializable;
import java.util.List;
/**
 * 规则说明
 * @ClassName: Fare
 * @Description: TODO
 * @date 2015年10月13日
 *
 */
public class FareRules {
	/***基础票价编码**/
	private String fareBasis;
	/**
	 * 第a航程第b段
	 * 1-1;1-2
	 */
	private String flightsStr;
	private List<FareRule> rules;
public static class Baggage implements Serializable,Cloneable {
		
		private static final long serialVersionUID = -4494211777457313914L;
		/**
		 * 第a航程第b段
		 * 1-1;1-2
		 */
		private String flightsStr;
		/***免费数量***/
		private Integer number;
		/***重量***/
		private Integer maxWeight;
	 	
	 	/****单位 Kilograms ***/
		private String  unit;

		public Integer getNumber() {
			return number;
		}

		public void setNumber(Integer number) {
			this.number = number;
		}

		public Integer getMaxWeight() {
			return maxWeight;
		}

		public void setMaxWeight(Integer maxWeight) {
			this.maxWeight = maxWeight;
		}

		public String getUnit() {
			return unit;
		}

		public void setUnit(String unit) {
			this.unit = unit;
		}

		public String getFlightsStr() {
			return flightsStr;
		}

		public void setFlightsStr(String flightsStr) {
			this.flightsStr = flightsStr;
		}

		
		@Override
		public Baggage clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return (Baggage)super.clone();
		}
		
	}
	
	public static class FareRule{
		/***Rule id***/
		private String category;
		/**说明 超文本***/
		private String value;
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		
	}



	public String getFareBasis() {
		return fareBasis;
	}

	public void setFareBasis(String fareBasis) {
		this.fareBasis = fareBasis;
	}

	public List<FareRule> getRules() {
		return rules;
	}

	public void setRules(List<FareRule> rules) {
		this.rules = rules;
	}

	public String getFlightsStr() {
		return flightsStr;
	}

	public void setFlightsStr(String flightsStr) {
		this.flightsStr = flightsStr;
	}

	
	
	
}
