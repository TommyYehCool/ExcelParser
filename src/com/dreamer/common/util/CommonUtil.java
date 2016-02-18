package com.dreamer.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.ConfigSetting;
import com.dreamer.common.constant.StrConstant;
import com.dreamer.excelparser.vo.CompanyInfo;

public class CommonUtil {
    public static Set<String> loadSpecialPattern(Logger logger) {
	Set<String> EXCLUDE_PATTERN = new HashSet<String>();

	File fileExcludePattern = new File(ConfigSetting.ONLINE_SPECIAL_FILE_PATH);
	if (!fileExcludePattern.isFile()) {
	    fileExcludePattern = new File(ConfigSetting.TEST_SPECIAL_FILE_PATH);
	    if (!fileExcludePattern.isFile()) {
		logger.error("請確認\"" + ConfigSetting.SPECIAL_FILE_NAME + "\"存在於config目錄底下...");
		System.exit(1);
	    }
	}

	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileExcludePattern), ConfigSetting.SPECIAL_FILE_ENCODING));

	    int count = 0;

	    logger.info("特殊名單: ");

	    StringBuilder logBuffer = new StringBuilder();

	    String line = null;
	    while ((line = reader.readLine()) != null) {
		String excludeStr = line.trim();
		if ("".equals(excludeStr)) {
		    continue;
		}
		EXCLUDE_PATTERN.add(excludeStr);

		count++;

		logBuffer.append(excludeStr).append("\t");
		if (count % 5 == 0) {
		    logger.info(logBuffer.toString());
		    logBuffer.setLength(0);
		}
	    }
	}
	catch (FileNotFoundException e) {
	    logger.error("請確認\"" + ConfigSetting.SPECIAL_FILE_NAME + "\"存在於config目錄底下...");
	    System.exit(1);
	}
	catch (IOException e) {
	    logger.error("讀取\"" + ConfigSetting.SPECIAL_FILE_NAME + "\"時, 產生異常", e);
	    System.exit(1);
	}
	finally {
	    if (reader != null) {
		try {
		    reader.close();
		}
		catch (IOException e) {
		    logger.error("關閉讀取檔案的 reader, 產生異常", e);
		    System.exit(1);
		}
		finally {
		    reader = null;
		}
	    }
	}
	logger.info(StrConstant.SEPRATE_LINE);

	return EXCLUDE_PATTERN;
    }

    public static HashSet<CompanyInfo> loadAlreadyFoundList(Logger logger) {
	HashSet<CompanyInfo> setCompanyNos = new HashSet<CompanyInfo>();

	File fileExcludePattern = new File(ConfigSetting.ONLINE_ALREADY_FOUND_LIST_FILE_PATH);
	if (!fileExcludePattern.isFile()) {
	    fileExcludePattern = new File(ConfigSetting.TEST_ALREADY_FOUND_LIST_FILE_PATH);
	    if (!fileExcludePattern.isFile()) {
		logger.error("不存在\"" + ConfigSetting.ALREADY_FOUND_LIST_FILE_NAME + "\"");
		return setCompanyNos;
	    }
	}

	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileExcludePattern), ConfigSetting.ALREADY_FOUND_LIST_ENCODING));

	    String line = null;
	    while ((line = reader.readLine()) != null) {
		String companyNameNo = line.trim();
		if ("".equals(companyNameNo)) {
		    continue;
		}

		String[] splitByComma = companyNameNo.split(", ");
		String splitCompanyName = splitByComma[0];
		String splitAddress = splitByComma[1];
		String splitPhoneNo = splitByComma[2];

		String companyName = splitCompanyName.substring(splitCompanyName.indexOf(":") + 1).trim();
		String address = splitAddress.substring(splitAddress.indexOf(":") + 1).trim();
		String phoneNo = splitPhoneNo.substring(splitPhoneNo.indexOf(":") + 1).trim();

		setCompanyNos.add(new CompanyInfo(companyName, address, phoneNo));
	    }
	}
	catch (IOException e) {
	    logger.error("讀取\"" + ConfigSetting.ALREADY_FOUND_LIST_FILE_NAME + "\"時, 產生異常", e);
	    System.exit(1);
	}
	finally {
	    if (reader != null) {
		try {
		    reader.close();
		}
		catch (IOException e) {
		    logger.error("關閉讀取檔案的 reader, 產生異常", e);
		    System.exit(1);
		}
		finally {
		    reader = null;
		}
	    }
	}
	return setCompanyNos;
    }
}