package com.dreamer.common.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.excelparser.vo.CompanyInfo;

public class GetPhoneNoUtil {
    private static final Logger recordLogger = Logger.getLogger(Log4jSetting.ALREADY_FOUND_LIST);
    
    private static Logger mLogger;

    private static final String BASE_URL = "http://m.iyp.com.tw/";
    private static final String MAIN_QRY_URL = BASE_URL + "?func=search_all_listing&k={0}";
    private static final String ADVANCE_QRY_URL = BASE_URL + "?func=search_all_listing&pg={0}&k={1}";

    private static final int RETRY_GET_DOC_TIMES = 3;
    private static final int GET_DOC_TIMEOUT = 10000;
    private static final int PROCESS_NEXT_COMPANY_SLEEP_TIME = 300;
    private static final int PROCESS_NEXT_PAGE_SLEEP_TIME = 100;

    private static final String QRY_ENCODING = "UTF-8";
    
    private static final String GET_ADDRESS_AT = "位於";
    private static final String GET_ADDRESS_OF = "的";

    /**
     * 至網路上查詢電話號碼 (PS: 有查到的才放到 Set 中)
     * 
     * @param companiesName 要查詢的公司名稱
     * @return HashSet<CompanyNo>
     * @throws UnsupportedEncodingException
     */
    public static HashSet<CompanyInfo> getPhoneNo(Logger logger, CompanyInfo[] companies) {
	mLogger = logger;
	
	HashSet<CompanyInfo> successDatas = new HashSet<CompanyInfo>();

	String[] utf8CompaniesName = new String[companies.length];
	try {
	    // 將公司名稱轉成 UTF-8
	    for (int i = 0; i < companies.length; i++) {
		utf8CompaniesName[i] = URLEncoder.encode(companies[i].getCompanyName(), QRY_ENCODING);
	    }
	}
	catch (UnsupportedEncodingException e) {
	    String errorMsg = "將公司的編碼轉換為 UTF-8 發生 UnsupportedEncodingException";
	    if (mLogger != null) {
		mLogger.error(errorMsg, e);
	    }
	    else {
		System.err.println(errorMsg);
		e.printStackTrace();
	    }
	    return successDatas;
	}

	// 已跑過公司筆數
	for (int i = 0; i < utf8CompaniesName.length; i++) {
	    String utf8CompanyName = utf8CompaniesName[i];
	    String companyCounties = companies[i].getCounties();
	    if (companyCounties.contains("(")) {
		companyCounties = companyCounties.substring(0, companyCounties.indexOf("("));
	    }
	    String companyTownships = companies[i].getTownships();
	    if (companyTownships.contains("(")) {
		companyTownships = companyTownships.substring(0, companyTownships.indexOf("("));
	    }
	    String origCompanyName = companies[i].getCompanyName();

	    String getPhoneNoMainQryURL = MAIN_QRY_URL.replace("{0}", utf8CompanyName);
	    
	    if (mLogger != null) {
		mLogger.info("縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + "> 開始至網路尋找...");
	    }

	    URL url = null;
	    try {
		url = new URL(getPhoneNoMainQryURL);
	    }
	    catch (MalformedURLException e) {
		String errorMsg = "縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 嘗試取得 URL 發生 Exception";
		if (mLogger != null) {
		    mLogger.error(errorMsg);
		}
		else {
		    System.err.println(errorMsg);
		    e.printStackTrace();
		}
		
		// 加入失敗紀錄
		recordLogger.info("縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 地址: , 電話: ");

		continue;
	    }

	    // --------- 取網頁資料開始 ---------
	    Document getPhoneNoDoc = null;
	    int tryCounts = 0;
	    while (getPhoneNoDoc == null && tryCounts < RETRY_GET_DOC_TIMES) {
		try {
		    tryCounts++;
		    getPhoneNoDoc = Jsoup.parse(url, GET_DOC_TIMEOUT);
		}
		catch (Exception e) {
		    String errorMsg = "Exception raised while getting Document with url: <" + getPhoneNoMainQryURL + ">, "
		    		       + "tryCounts: <" + tryCounts + ">, " 
		    		       + "msg: <" + e.getMessage() + ">";
		    
		    if (mLogger != null) {
			mLogger.error(errorMsg);
		    }
		    else {
			System.err.println(errorMsg);
			e.printStackTrace();
		    }
		}
	    }
	    if (getPhoneNoDoc == null) {
		String errorMsg = "縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 因取不到 Document，且重試次數到達 " + tryCounts + " 次, 所以略過";
		
		if (mLogger != null) {
		    // 略過這筆跑下一筆
		    mLogger.error(errorMsg);
		}
		else {
		    System.err.println(errorMsg);
		}

		// 加入失敗紀錄
		recordLogger.info("縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 地址: , 電話: ");
		
		continue;
	    }
	    // --------- 取網頁資料結束 ---------

	    // 開始趴資料
	    boolean isFound = false;
	    Elements elmsLi = getPhoneNoDoc.select("#listing li");
	    Iterator<Element> it = elmsLi.iterator();

	    while (it.hasNext()) {
		Element elmLi = it.next();

		Elements elmCompanyName = elmLi.select("a h3");
		String webCompanyName = elmCompanyName.get(0).text();
		
		String webCompanyDetailUrl = elmLi.select("a").attr("href");

		String webCompanyPhoneNo = webCompanyDetailUrl.replace("/", "");

		// 找到同名的公司
		if (webCompanyName.equals(origCompanyName)) {
		    
		    String companyAddress = getCompanyAddress(webCompanyDetailUrl);
		    
//		    System.out.println(companyAddress);
		    
		    if (!companyAddress.contains(companyCounties) || !companyAddress.contains(companyTownships)) {
			continue;
		    }
		    
		    // 加入成功紀錄
		    recordLogger.info("縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 地址: " + companyAddress + ", 電話: " + webCompanyPhoneNo);
		    
		    successDatas.add(new CompanyInfo(origCompanyName, companyAddress, webCompanyPhoneNo));

		    isFound = true;

		    break;
		}
	    }

	    // 判對是否有找到
	    if (!isFound) {
		// 嘗試找看看下一頁
		if (findMore(origCompanyName, companyCounties, companyTownships, utf8CompanyName, successDatas) == 0) {
		    // 最後還是沒找到，加入失敗紀錄
		    recordLogger.info("縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 地址: , 電話: ");
		}
	    }

	    // sleep a litte time
	    try {
		Thread.sleep(PROCESS_NEXT_COMPANY_SLEEP_TIME);
	    }
	    catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
	return successDatas;
    }

    private static int findMore(String origCompanyName, String companyCounties, String companyTownships, String utf8CompanyName, HashSet<CompanyInfo> successDatas) {
	int pageNo = 1;
	while (true) {
	    String advanceQryURL = ADVANCE_QRY_URL.replace("{0}", String.valueOf(pageNo));
	    advanceQryURL = advanceQryURL.replace("{1}", utf8CompanyName);

	    // 取網頁資料開始
	    URL url = null;
	    try {
		url = new URL(advanceQryURL);
	    }
	    catch (MalformedURLException e) {
		e.printStackTrace();
		return 0;
	    }

	    Document getPhoneNoDoc = null;
	    int tryCounts = 0;
	    while (getPhoneNoDoc == null) {
		try {
		    if (tryCounts == RETRY_GET_DOC_TIMES) {
			String errorMsg = "縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 因取不到 Document，且重試次數到達 " + tryCounts + " 次, 所以略過";
			if (mLogger != null) {
			    mLogger.error(errorMsg);
			}
			else {
			    System.err.println(errorMsg);
			}
			return 0;
		    }
		    tryCounts++;
		    getPhoneNoDoc = Jsoup.parse(url, GET_DOC_TIMEOUT);
		}
		catch (Exception e) {
		    String errorMsg = "Exception raised while getting Document " 
		    	       	      + "with url: <" + advanceQryURL + ">, "
		    	       	      + "tryCounts: <" + tryCounts + ">, " 
		    	       	      + "msg: <" + e.getMessage() + ">";
		    
		    if (mLogger != null) {
			mLogger.error(errorMsg, e);
		    }
		    else {
			System.err.println(errorMsg);
		    }
		}
	    }
	    if (!getPhoneNoDoc.hasText()) {
		// System.err.println("成功取到 Document 但 hasText 為 false, 所以 return 0");
		return 0;
	    }
	    // 取網頁資料結束

	    // 開始趴資料
	    Elements elmsLi = getPhoneNoDoc.select("#listing li");
	    if (elmsLi.isEmpty()) {
		String errorMsg = "縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 成功取到 Document 但抓不到 li, 所以 return 0";
		if (mLogger != null) {
		    mLogger.error(errorMsg);
		}
		else {
		    System.err.println(errorMsg);
		}
		return 0;
	    }
	    Iterator<Element> it = elmsLi.iterator();

	    while (it.hasNext()) {
		Element elmLi = it.next();

		Elements elmCompanyName = elmLi.select("a h3");
		String webCompanyName = elmCompanyName.get(0).text();

		String webCompanyDetailUrl = elmLi.select("a").attr("href");

		String webCompanyPhoneNo = webCompanyDetailUrl.replace("/", "");

		// 找到同名的公司
		if (webCompanyName.equals(origCompanyName)) {
		    String companyAddress = getCompanyAddress(webCompanyDetailUrl);
		    
//		    System.out.println(companyAddress);
		    
		    if (!companyAddress.contains(companyCounties) || !companyAddress.contains(companyTownships)) {
			continue;
		    }
		    
		    // 加入成功紀錄
		    recordLogger.info("縣市: <" + companyCounties + ">, 鄉鎮區: <" + companyTownships + ">, 公司: <" + origCompanyName + ">, 地址: " + companyAddress + ", 電話: " + webCompanyPhoneNo);
		    
		    successDatas.add(new CompanyInfo(origCompanyName, companyAddress, webCompanyPhoneNo));

		    return 1;
		}
	    }

	    // 趴完資料
	    try {
		Thread.sleep(PROCESS_NEXT_PAGE_SLEEP_TIME);
	    }
	    catch (InterruptedException e) {
		e.printStackTrace();
	    }

	    // 在跑下一頁
	    pageNo++;
	}
    }
    
    private static String getCompanyAddress(String webCompanyDetailUrl) {
	String companyAddress = null;
	
	webCompanyDetailUrl = webCompanyDetailUrl.substring(1, webCompanyDetailUrl.length());
	
	String getAddressMainQryUrl = BASE_URL + webCompanyDetailUrl;
	
	URL url = null;
	try {
	    url = new URL(getAddressMainQryUrl);
	} 
	catch (MalformedURLException e) {
	    e.printStackTrace();
	    return null;
	}
	
	Document getAddressDoc = null;
	int tryCounts = 0;
	boolean getDocSucceed = false;
	while (getAddressDoc == null && tryCounts < RETRY_GET_DOC_TIMES) {
	    try {
		tryCounts++;
		getAddressDoc = Jsoup.parse(url, GET_DOC_TIMEOUT);
		getDocSucceed = true;
	    } 
	    catch (Exception e) {
		String errorMsg = "Exception raised while getting Document "
				   + "with url: <" + getAddressMainQryUrl + ">, "
				   + "tryCounts: <" + tryCounts + ">, " 
				   + "msg: <" + e.getMessage() + ">";
		
		if (mLogger != null) {
		    mLogger.error(errorMsg, e);
		}
		else {
		    System.err.println(errorMsg);
		    e.printStackTrace();
		}
	    }
	}
	
	if (!getDocSucceed) {
	    return companyAddress;
	}
	
	Elements elmsMeta = getAddressDoc.getElementsByTag("meta");
	
	for (Element elmMeta : elmsMeta) {
	    String attrDesc = elmMeta.attr("name");
	    if (!attrDesc.isEmpty() && attrDesc.equals("description")) {
		String content = elmMeta.attr("content");
		
		int indexAt = content.indexOf(GET_ADDRESS_AT);
		int indexOf = content.indexOf(GET_ADDRESS_OF);
		
		if (indexAt != -1 && indexOf != -1) {
		    while (indexOf < indexAt) {
			indexOf = content.indexOf(GET_ADDRESS_OF, indexOf + 1); 
		    }
		    
		    companyAddress = content.substring(indexAt + GET_ADDRESS_AT.length(), indexOf);
		
		    if (companyAddress.contains(",")) {
			companyAddress = companyAddress.replaceAll(",", "-");
		    }
		}
		break;
	    }
	}
	return companyAddress;
    }
}