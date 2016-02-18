package com.dreamer.common.util;

import java.util.HashSet;
import java.util.Iterator;

public class StrUtil {
    public static String cutOffByKeyword(String srcStr) {
//	System.out.println(StrConstant.SEPRATE_LINE);
//	System.out.println("Src String = \"" + srcStr + "\"");
	
	String resultStr = srcStr;
	
	HashSet<String> keywords = new HashSet<String>();
	keywords.add("區");
	keywords.add("地");
	
	final String openParenthesis = "(";
	final String period = "。";
	
	Iterator<String> it = keywords.iterator();
	
	while (it.hasNext()) {
	    String keyword = it.next();
	    
	    if (srcStr.contains(keyword)) {
		int indexOfKeyword = srcStr.indexOf(keyword);
//		System.out.println("Analysis = \"" + srcStr + "\" -> 字串長度: " + srcStr.length() + ", 包含 \"" + keyword + "\"    位置: " + indexOfKeyword);

		if (indexOfKeyword == srcStr.length() - 1) {
		    break;
		}

		int indexOfKeywordAddOne = indexOfKeyword + 1;

		String nextChar = srcStr.substring(indexOfKeywordAddOne, indexOfKeywordAddOne + 1);

		if (nextChar.equals(openParenthesis)) {
		    resultStr = srcStr.substring(0, indexOfKeywordAddOne);
		}
//		System.out.println("ResultStr = \"" + resultStr + "\"");
		break;
	    }
	}
	if (resultStr.endsWith(period)) {
	    resultStr = resultStr.substring(0, resultStr.length() - 1);
	}
	if (resultStr.contains(period)) {
	    resultStr = resultStr.substring(0, resultStr.indexOf(period));
	}
	
	return resultStr;
    }
}
