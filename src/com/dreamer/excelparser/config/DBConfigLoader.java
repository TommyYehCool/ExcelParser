package com.dreamer.excelparser.config;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.ConfigSetting;
import com.syscom.safe.util.xml.XmlAggregate;
import com.syscom.safe.util.xml.XmlException;
import com.syscom.safe.util.xml.XmlParser;

public class DBConfigLoader {
    private static DBConfigLoader instance = new DBConfigLoader();

    private Logger mLogger;

    private XmlAggregate agrDbService;

    private DBConfigLoader() {
    }

    public static DBConfigLoader getInstance() {
	return instance;
    }

    /**
     * 讀入 DB config
     */
    public void init() {
	loadDBConfig();
    }

    private void loadDBConfig() {
	String cppConfigPath = ConfigSetting.ONLINE_DB_CONFIG_PATH;

	File cppConfigFile = new File(cppConfigPath);
	if (!cppConfigFile.isFile()) {
	    cppConfigPath = ConfigSetting.TEST_DB_CONFIG_PATH;
	}

	try {
	    XmlAggregate agrCppConfig = XmlParser.parseXmlFile(cppConfigPath);

	    parseDbService(agrCppConfig);
	}
	catch (FileNotFoundException e) {
	    mLogger.error("請確認 config 目錄底下存在 DBConfig.xml");
	    System.exit(1);
	}
	catch (XmlException e) {
	    mLogger.error("請確認 config 目錄底下 DBConfig.xml 為合法的 xml 格式", e);
	    System.exit(1);
	}
    }

    /**
     * 處理 DB 連線部分
     * 
     * @param agrCppConfig
     */
    private void parseDbService(XmlAggregate agrCppConfig) {
	agrDbService = agrCppConfig.getAggregate("db-service");
    }

    public boolean needToStoreDb() {
	return agrDbService != null;
    }

    public XmlAggregate getDbServiceConfig() {
	return agrDbService;
    }
}