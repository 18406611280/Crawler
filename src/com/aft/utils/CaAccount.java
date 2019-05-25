package com.aft.utils;
/*
 * 国航APP账号
 */
import java.util.ArrayList;
import java.util.Collections;

public class CaAccount {
	
	public static ArrayList<String> accountList  = new ArrayList<String>();
	
	static {
		String txt = StringTxtUtil.TxtToString("resource/config/CaAccount.txt");
		String[] array = txt.split(";");
		for(int i=0; i<array.length; i++) {
			accountList.add(array[i]);
		}
	}
	
	public static ArrayList getAccountList() {
		return accountList;
	}

}
