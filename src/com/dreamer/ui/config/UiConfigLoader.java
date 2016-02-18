package com.dreamer.ui.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dreamer.common.constant.ConfigSetting;
import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.constant.StrConstant;
import com.dreamer.ui.vo.Counties;
import com.dreamer.ui.vo.Townships;
import com.syscom.safe.util.xml.ElementNotFoundException;
import com.syscom.safe.util.xml.XmlAggregate;
import com.syscom.safe.util.xml.XmlElement;
import com.syscom.safe.util.xml.XmlException;
import com.syscom.safe.util.xml.XmlParser;

public class UiConfigLoader {
    private static UiConfigLoader instance = new UiConfigLoader();

    private Logger mLogger = Logger.getLogger(Log4jSetting.QUERY_UI);
    
    private int pageSize = 300;
    
    private boolean isForBoss = false;
    
    private LinkedHashMap<String, Counties> mCounties = new LinkedHashMap<String, Counties>();
    
    private UiConfigLoader() {
    }

    public static UiConfigLoader getInstance() {
	return instance;
    }

    /**
     * <pre>
     * 1. ��l�� log4j ����
     * 2. Ū�J QueryLandInfoUi-Config.xml
     * </pre>
     */
    public void init() {
	initLog4j();

	loadUiConfig();
    }

    private void initLog4j() {
	String log4jConfigPath = Log4jSetting.ONLINE_LOG4J_PATH;

	File onlineLog4jSetting = new File(log4jConfigPath);
	if (!onlineLog4jSetting.isFile()) {
	    log4jConfigPath = Log4jSetting.TEST_LOG4J_PATH;
	}

	PropertyConfigurator.configure(log4jConfigPath);

	mLogger.info("Log ����Ұʦ��\");
	mLogger.info(StrConstant.SEPRATE_LINE);
    }

    private void loadUiConfig() {
	String uiConfigPath = ConfigSetting.ONLINE_UI_CONFIG_PATH;

	File uiConfigFile = new File(uiConfigPath);
	if (!uiConfigFile.isFile()) {
	    uiConfigPath = ConfigSetting.TEST_UI_CONFIG_PATH;
	}

	try {
	    XmlAggregate agrUiConfig = XmlParser.parseXmlFile(uiConfigPath);
	    
	    parsePageSize(agrUiConfig);
	    
	    parseIsForBoss(agrUiConfig);
	    
	    parseCountiesTownshipsTable(agrUiConfig);
	}
	catch (FileNotFoundException e) {
	    mLogger.error("�нT�{ config �ؿ����U�s�b " + ConfigSetting.UI_CONFIG_NAME);
	    System.exit(1);
	}
	catch (XmlException e) {
	    mLogger.error("�нT�{ config �ؿ����U " + ConfigSetting.UI_CONFIG_NAME + " ���X�k�� xml �榡", e);
	    System.exit(1);
	}
    }

    private void parsePageSize(XmlAggregate agrUiConfig) {
	try {
	    pageSize = agrUiConfig.getElementIntValue("page-size");
	    mLogger.info("�C����ܵ���: " + pageSize);
	} 
	catch (ElementNotFoundException e) {
	    mLogger.error("�нT�{ " + ConfigSetting.UI_CONFIG_NAME + " ���U�]�t <page-size>");
	    System.exit(1);
	}
    }

    private void parseIsForBoss(XmlAggregate agrUiConfig) {
	String strIsForBoss = null;
	try {
	    strIsForBoss = agrUiConfig.getElementStringValue("is-for-boss");
	}
	catch (ElementNotFoundException e) {
	    mLogger.error("�нT�{ " + ConfigSetting.UI_CONFIG_NAME + " ���U�]�t <is-for-boss>");
	    System.exit(1);
	}
	if (strIsForBoss.compareToIgnoreCase("true") != 0 && strIsForBoss.compareToIgnoreCase("false") != 0) {
	    mLogger.error("�нT�{ " + ConfigSetting.UI_CONFIG_NAME + " ���U�]�t <is-for-boss> ���Ȭ� true �� false");
	    System.exit(1);
	}
	isForBoss = Boolean.parseBoolean(strIsForBoss);
	mLogger.info("isForBoss: " + isForBoss);
    }

    /**
     * �ѪR ���� -> �m�� -> Table �������Y
     * 
     * @param agrUiConfig
     */
    private void parseCountiesTownshipsTable(XmlAggregate agrUiConfig) {
	XmlAggregate agrCountiesTownshipsTable = agrUiConfig.getAggregate("counties-townships-table");
	if (agrCountiesTownshipsTable == null) {
	    mLogger.error("�нT�{ " + ConfigSetting.UI_CONFIG_NAME + " ���U�]�t <counties-townships-table>");
	    System.exit(1);
	}

	List<XmlAggregate> agrAllCounties = agrCountiesTownshipsTable.getAggregates();

	for (XmlAggregate agrCounties : agrAllCounties) {
	    String countiesName = agrCounties.getAttribValue("name");
	    if (countiesName == null || "".equals(countiesName.trim())) {
		mLogger.error("Please check your config that counties has attribute <name>");
		System.exit(1);
	    }

	    String personalTable = agrCounties.getAttribValue("person-table-name");
	    if (personalTable == null || "".equals(personalTable.trim())) {
		mLogger.error("Please check your config that counties has attribute <person-table-name>");
		System.exit(1);
	    }
	    
	    @SuppressWarnings("unchecked")
	    List<XmlElement> elmAllTownships = agrCounties.getElements("townships");
	    if (elmAllTownships == null || elmAllTownships.size() == 0) {
		mLogger.error("Please check your config that counties has at least one <townships>");
		System.exit(1);
	    }

	    for (XmlElement elmTownships : elmAllTownships) {
		Townships townships = new Townships();

		townships.setPersonalTableName(personalTable);

		String townshipsName = elmTownships.getStringValue();

		townships.setTownshipName(townshipsName);

		Counties counties = null;
		if (!mCounties.containsKey(countiesName)) {
		    counties = new Counties();
		    counties.setCountiesName(countiesName);

		    mCounties.put(countiesName, counties);
		}
		else {
		    counties = mCounties.get(countiesName);
		}
		counties.addTownships(townships);
	    }
	}
    }
    
    public int getPageSize() {
	return pageSize;
    }
    
    public boolean isForBoss() {
	return isForBoss;
    }
    
    public Counties[] getAllCounties() {
	synchronized (mCounties) {
	    return mCounties.values().toArray(new Counties[0]);
	}
    }
}