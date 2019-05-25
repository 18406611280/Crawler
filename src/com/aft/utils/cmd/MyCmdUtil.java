package com.aft.utils.cmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.aft.utils.MyStringUtil;

public class MyCmdUtil {

	private final static Logger logger = Logger.getLogger(MyCmdUtil.class);
	
	/**
	 * 执行CMD命令,并返回String字符串
	 * @param strCmds
	 * @return
	 * @throws Exception
	 */
	public static String execCmd(String... strCmds) throws Exception {
		String strCmd = MyStringUtil.spliceValueNotRepeat(strCmds, " && ");
		logger.info("执行命令 >>>>>" + strCmd);
		Process p = Runtime.getRuntime().exec(strCmd);
		StringBuilder sbCmd = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		while((line = br.readLine()) != null) {
			sbCmd.append(line + "\n");
		}
		p.waitFor();
		p.destroy();
		logger.debug("执行命令返回 >>>>>" + sbCmd);
		return sbCmd.toString().trim();
	}
}