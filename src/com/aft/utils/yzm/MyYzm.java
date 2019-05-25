package com.aft.utils.yzm;

import java.io.File;

import com.aft.utils.MyStringUtil;
import com.aft.utils.cmd.MyCmdUtil;

public class MyYzm {
	
	/**
	 * 3U 四川航空验证码
	 * @param codeFile
	 * @return
	 * @throws Exception
	 */
	public static String antiV3UCode(String codeFile) throws Exception {
		return MyYzm.antiVCode(codeFile, "3u.cds");
	}
	

	/**
	 * 验证码
	 * @param codeFile
	 * @return
	 * @throws Exception
	 */
	public static String antiVNHCode(String codeFile) throws Exception {
		return MyYzm.antiVCode(codeFile, "nh.cds");
	}
	

	/**
	 * 验证码
	 * @param codeFile
	 * @param codeFcdsle
	 * @return
	 * @throws Exception
	 */
	public static String antiVCode(String codeFile, String cds) throws Exception {
		File file = new File("resource/antiVc/IdentifyVCode.exe");
		if(!file.exists()) return "";
		String fileFullName = file.getCanonicalPath();
		String cdsFileFullName = MyStringUtil.getFilePath(file.getParentFile().getCanonicalPath(), cds);
		String result = MyCmdUtil.execCmd(fileFullName + " " + cdsFileFullName + " " + codeFile);
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(MyYzm.antiVNHCode(new File("resource/antiVc/img/5cdt.jpg").getPath()));
	}
}