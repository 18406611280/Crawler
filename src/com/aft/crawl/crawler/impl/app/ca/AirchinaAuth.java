package com.aft.crawl.crawler.impl.app.ca;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2016/4/8.
 */
public class AirchinaAuth {

	private static Logger logger = Logger.getLogger(AirchinaAuth.class);

	// 验证会用publicKey和packageName两个字符串和chanllengeData一起生成chanllengeAnswer

	// publicKey是安卓客户端的签名里拿出来的公钥，并且做了base64编码
	// String publicKey =
	// "MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEA1pMZBN7GCySx7cdi4NnYJT4+zWzrHeL/Boyo6LyozWvTeG6nCqds5g67D5k1Wf/ZPnepQ+foPUtkuOT+otPmVvHiZ6gbv7IwtXjCBEO+THIYuEb1IRWG8DihTonCvjh/jr7Pj8rD2h7jMMnqk9Cnw9xK81AiDVAIBzLggJcX7moFM1nmppTsLLPyhKCkZsh6lNg7MQk6ZzcuL2QSwG5tQvFYGN/+A4HMDNRE2mzdw7gkWBlIAbMlZBNPv96YySh3SNv1Z2pUDYFUyLvKB7niR1UzEcRrmvdv3uzMjmnnyKLQjngmIJQ/mXJ9PAT+cpkdmd+brjigshd/ox1bav7pHwIBAW==";
	static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCiVFO7bgcdW3KH3pKJFFyxyjP7X30j3gSVYTLiTziHRPVYqKSYHWm5ZtMRluBZsj1l41M2mwGclPpTyje4MgT9Z99aiVHsy2tBGdSKSZCzlQ/rQGNTtN3Ra5QhXUWvoreI4UQAi7BIoaV3D1qVM+TH1Ocq+TURvPV4TgC1uw3TewIDAQAB";
	// packageName是安app的包名
	static String packageName = "com.rytong.airchina";

	public static String getChallengeAnswer(String challengeData) {
		String challengeAnswer = new String();

		int i = 0;
		int parsedLen; // 已经解析过的chanllengeData的长度
		int sz = challengeData.length();

		logger.info(challengeData);
		
		while(i < sz) {// 例子：163647C
			// 解析前0~2字节，前3字节应该是数字，使用atoi转化
			String subStr1 = challengeData.substring(i, i + 2 + 1);
			int left = atoi(subStr1);

//			logger.info("AIRCHINA：" + String.format("%d", left));

			// 解析3~5字节，同上
			String subStr2 = challengeData.substring(3 + i, i + 5 + 1);
			int right = atoi(subStr2);

			// 取偏移为6的字节，此字节用于判断下一步骤是什么
			char cond = challengeData.charAt(i + 6);
			parsedLen = i + 6; // 当前解析了7个字节，偏移为6

			if(cond == 0x58) { // case 'X'
				// 例子：XCADCCBF5S
				// X=0x58, 取X到S中间的子字符串，与当前的chanAnswer做逐字节异或
				String sbStr = findSubStr(challengeData, i + 7);// 在当前循环的前7字节之后寻找subString
//				logger.info("subStr：" + sbStr);
				byte[] subStr = sbStr.getBytes();
				int sbLen = subStr.length;
				
//				logger.info("sbLen：" + String.format("%d", sbLen));
				
				byte[] chanAnswer = challengeAnswer.getBytes();
				
//				logger.info("chanAnswerLen：" + String.format("%d", chanAnswer.length));

				// 使用subString 逐字节异或challengeAnswer
				for(int j = 0; j < chanAnswer.length; ++j) {
					int tmp = chanAnswer[j] ^ subStr[j % sbLen];
					chanAnswer[j] = (byte) tmp;
				}
				challengeAnswer = new String(chanAnswer);
//				logger.info("challengeAnswerLen：" + String.format("%d", challengeAnswer.length()));
				parsedLen = i + 6 + sbLen + 1; // sbLen不包括S，所以要+1
			} else if(cond == 0x4e) {// 生成challengeAnswer case 'N'
				challengeAnswer = generateAnswer(challengeAnswer, packageName, left, right);
			} else if(cond == 0x43) { // 生成challengeAnswer case 'C'
				challengeAnswer = generateAnswer(challengeAnswer, publicKey, left, right);
			}
			i = parsedLen + 1; // 下一循环的起始偏移
		}

		// Base64编码,然后开头添加字符'a'
		String encodedAnswer = "a";
		encodedAnswer += Base64.encodeBase64String(challengeAnswer.getBytes());
		logger.info("base64：" + encodedAnswer);
		return encodedAnswer;
	}

	// 复制key的子字符串key[left]~key[right] 到 challengeAnswer尾部,返回新chanllengeAnswer长度
	private static String generateAnswer(String challengeAnswer, String key,
			int left, int right) {
		int keySz = key.length();
		left = left % keySz;
		right = right % keySz;

		if (left > right) {
			int tmp = left;
			left = right;
			right = tmp;
		}
		challengeAnswer += key.substring(left, right + 1);
		return challengeAnswer;
	}

	// 从challengeData的偏移begin开始，找到第一个字节等于0x53出现的偏移end，取这段子字符串
	private static String findSubStr(String challengeData, int begin) {
		String subStr = challengeData.substring(begin);
		int end = subStr.indexOf(0x53); // case 'X'后，寻找字符'S'，样例780739X4A568036S
		return subStr.substring(0, end);
	}

	// 字符转化数字，假定str长度为3，并且每个字符都是'0' ~ '9'。只适用于解析challegeData
	private static int atoi(String str) {
		int sz = str.length();
		int res = 0;
		for(int i = 0; i < sz; ++i) {
			res = res * 10 + (str.charAt(i) - '0');
		}
		return res;
	}
}