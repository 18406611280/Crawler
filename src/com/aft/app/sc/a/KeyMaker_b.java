package com.aft.app.sc.a;

import java.math.BigInteger;

public class KeyMaker_b {
	public static String a;
	public static String b;
	public static String c;

	static {
		a = "65537";
		b = "121193049542313836116621151597310864771446714368993893735972445135024464117188200960335632956735489539596076927170320806955207426553209933617885545465639594710119940282857885308578872862476300562991048610756141947334816345400027743821184496328847030895715177473968630317728320054635056207901448348620583958039";
		c = "51329103806185897265499691851725859796161656022402055456145187598041931283104092254086030242154919864360106615613570878109399156794443568174186323534640853514135700494472992613988216577931922154137554530775549138176702287567958609686358563666187400274317318900262063677635946022678600299611584973015615870537";
	}

	public static String a(String arg3, String arg4, String arg5) {
		String string0;
		new StringBuffer();
		byte[] array_b = new BigInteger(arg3).modPow(new BigInteger(arg4),
				new BigInteger(arg5)).toByteArray();

		// return array_b;

		try {
			string0 = new String(array_b, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			string0 = null;
		}

		return string0;
	}
}
