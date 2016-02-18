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
     * Ū�J DB config
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
	    mLogger.error("�нT�{ config �ؿ����U�s�b DBConfig.xml");
	    System.exit(1);
	}
	catch (XmlException e) {
	    mLogger.error("�нT�{ config �ؿ����U DBConfig.xml ���X�k�� xml �榡", e);
	    System.exit(1);
	}
    }

    /**
     * �B�z DB �s�u����
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