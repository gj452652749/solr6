package org.gj.analyzer.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.StringUtil;
import org.nlpcn.commons.lang.jianfan.JianFan;
import org.nlpcn.commons.lang.util.WordAlert;

public class WordNormalizeUtils {
	static WordNormalizeUtils instance=null;
	static Map<Character,Character> numMap=new HashMap<Character,Character>();
	private WordNormalizeUtils() { 
		System.out.println("wordnormalized 加载Num词库");
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new BufferedInputStream(getClass().getResourceAsStream("/number.dic")), StandardCharsets.UTF_8));
			String line = null;
			while (null != (line = in.readLine())) {
				if (line.length() == 0 ) {
					continue;
				}
				String[] pair = line.split("	");
				int i=pair.length-1;
				while(i>0) {
					numMap.put(pair[i].charAt(0), pair[0].charAt(0));
					i--;
				}				
			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static WordNormalizeUtils getInstance() {
		if(null==instance){
			synchronized(WordNormalizeUtils.class){
				if(null==instance) {
					instance=new WordNormalizeUtils();
				}
			}
		}
		return instance;
	}
	//仅限于半角转换
	public String removeSymbols(String str) {
		if(null==str) return null;
		StringBuffer sb=new StringBuffer();
		char[] chars = str.toCharArray();
		for(char c:chars) {
			if(c<48 || (c>57 && c<65) || (c>90 && c<97) ||(c>172 && c<178))
				continue;
			sb.append(c);
		}
		return sb.toString();
	}
	/*
	 * 字符串中的的数字转阿拉伯数字，如：一 壹->1
	 */
	public char[] number2Arabic(String str) {
		char[] chars = str.toCharArray();
		char c = 0;
		for (int i = 0; i < chars.length; i++) {
			c= chars[i];
			if(!numMap.containsKey(c)) continue;
			chars[i]=numMap.get(c);
		}
		return chars;
	}

	public String normalizeStr(String str) {
		if(StringUtil.isBlank(str)) return null;
		//String termStr=str.replaceAll("\\pP|\\pS", "");
		String termStr=removeSymbols(str);
		char[] result = WordAlert.alertStr(termStr) ;
		termStr=new String(result);
		termStr=JianFan.f2j(termStr);
		termStr=new String(number2Arabic(termStr));
		return termStr;
		
	}
}
