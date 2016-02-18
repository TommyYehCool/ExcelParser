package com.dreamer.excelparser.config;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.constant.StrConstant;

public class TspConfigLoader {
    private static TspConfigLoader instance = new TspConfigLoader();

    private Logger mLogger = Logger.getLogger(Log4jSetting.TOWNSHIPS_PARSER);

    private TspConfigLoader() {
    }

    public static TspConfigLoader getInstance() {
	return instance;
    }

    /**
     * <pre>
     * 1. ��l�� log4j ����
     * </pre>
     */
    public void init() {
	initLog4j();
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
}
