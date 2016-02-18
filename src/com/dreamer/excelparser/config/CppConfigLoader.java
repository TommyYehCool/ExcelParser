package com.dreamer.excelparser.config;

import java.util.Set;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.util.CommonUtil;

public class CppConfigLoader {
    private static CppConfigLoader instance = new CppConfigLoader();

    private Logger mLogger = Logger.getLogger(Log4jSetting.COMPANY_PERSONAL_PARSER);

    private Set<String> EXCLUDE_PATTERN;

    private CppConfigLoader() {
    }

    public static CppConfigLoader getInstance() {
	return instance;
    }

    public void init() {
	loadExcludePattern();
    }

    private void loadExcludePattern() {
	EXCLUDE_PATTERN = CommonUtil.loadSpecialPattern(mLogger);
    }

    public Set<String> getExcludePattern() {
	return EXCLUDE_PATTERN;
    }
}
