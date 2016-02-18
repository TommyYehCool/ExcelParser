package com.dreamer.common.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.dreamer.common.constant.StrConstant;
import com.dreamer.common.vo.LandInfo;
import com.dreamer.excelparser.vo.Company;
import com.dreamer.excelparser.vo.CompanyInfo;
import com.dreamer.excelparser.vo.LegalPerson;
import com.dreamer.excelparser.vo.Others;
import com.dreamer.excelparser.vo.Personal;
import com.dreamer.ui.enu.RadioButtonStatus;
import com.dreamer.ui.vo.Counties;
import com.dreamer.ui.vo.Townships;
import com.syscom.safe.util.xml.ElementNotFoundException;
import com.syscom.safe.util.xml.XmlAggregate;

public class AccessDbUtil {
    private static AccessDbUtil instance = new AccessDbUtil();

    private Logger logger;

    private String configName;

    private String connUrl = null;
    private String driverClass = null;
    private String userName = null;
    private String password = null;
    private int batchProcessSize;

    private Connection connInsertCompany;
    private Connection connInsertLegalPerson;
    private Connection connInsertPersonal;
    private Connection connInsertOthers;
    private Connection connForQueryAndUpdate;

    private final String INSERT_COMPANY_SQL = 
	    "insert into company values (" 
	  + "?, ?, ?, ?, ?," 
	  + "?, ?, ?, ?, ?," 
	  + "?, ?, ?, ?, ?," 
	  + "?" 
	  + ")";

    private final String INSERT_LEGAL_PERSON_SQL = 
	    "insert into legal_person values (" 
	  + "?, ?, ?, ?, ?," 
	  + "?, ?, ?, ?, ?," 
	  + "?, ?, ?, ?, ?," 
	  + "?"
	  + ")";

    private final String INSERT_PERSONAL_SQL = 
	    "insert into personal values (" 
          + "?, ?, ?, ?, ?," 
          + "?, ?, ?, ?, ?," 
          + "?, ?, ?, ?, ?," 
          + "?" 
          + ")";

    private final String INSERT_OTHERS_SQL = 
	    "insert into others values (" 
	  + "?, ?, ?, ?, ?," 
	  + "?, ?, ?, ?, ?," 
	  + "?, ?, ?, ?, ?," 
	  + "?" 
	  + ")";
    
    private final String COMPANY_TABLE_NAME = "company";
    private final String QUERY_COUNT_STAR_FROM_COMPANY = "select count(*) as totalCnts from " + COMPANY_TABLE_NAME;
    private final String QUERY_LAND_INFO_FROM_COMPANY = "select * from " + COMPANY_TABLE_NAME;
    private final String QUERY_DISTINCT_COMPANIES = "select VC_OWNER, VC_COUNTIES, VC_TOWNSHIPS from " + COMPANY_TABLE_NAME + " group by VC_OWNER, VC_COUNTIES, VC_TOWNSHIPS";
    private final String UPDATE_COMPANY_NO_ADDRESS = "update " + COMPANY_TABLE_NAME + " set VC_PHONE_NO = ?, VC_ADDRESS = ? where VC_OWNER = ? and VC_COUNTIES = ? and VC_TOWNSHIPS = ?";
    
    private final String LEGAL_PERSON_TABLE_NAME = "legal_person";
    private final String QUERY_COUNT_STAR_FROM_LEGAL_PERSON = "select count(*) as totalCnts from " + LEGAL_PERSON_TABLE_NAME;
    private final String QUERY_LAND_INFO_FROM_LEGAL_PERSON = "select * from " + LEGAL_PERSON_TABLE_NAME;
    
    private final String OTHERS_TABLE_NAME = "others";
    private final String QUERY_COUNT_STAR_FROM_OTHERS = "select count(*) as totalCnts from " + OTHERS_TABLE_NAME;
    private final String QUERY_LAND_INFO_FROM_OTHERS = "select * from " + OTHERS_TABLE_NAME;
    
    private final String PERSONAL_TABLE_NAME = "personal";
    private final String QUERY_COUNT_STAR_FROM_PERSONL = "select count(*) as totalCnts from " + PERSONAL_TABLE_NAME;
    private final String QUERY_LAND_INFO_FROM_PERSONL = "select * from " + PERSONAL_TABLE_NAME;
    
    private AccessDbUtil() {
    }

    public static AccessDbUtil getInstance() {
	return instance;
    }

    public void init(Logger logger, String configName, XmlAggregate agrDbService) 
	    throws ElementNotFoundException, ClassNotFoundException {
	this.logger = logger;

	this.configName = configName;

	getConfigValue(agrDbService);
    }

    private void getConfigValue(XmlAggregate agrDbService) throws ElementNotFoundException {
	try {
	    connUrl = agrDbService.getElementStringValue("connection-url");
	}
	catch (ElementNotFoundException e) {
	    throw new ElementNotFoundException("<connection-url> not found in " + configName + " <db-service>, please check...");
	}
	try {
	    driverClass = agrDbService.getElementStringValue("driver-class");
	}
	catch (ElementNotFoundException e) {
	    throw new ElementNotFoundException("<driver-class> not found in " + configName + " <db-service>, please check...");
	}
	try {
	    userName = agrDbService.getElementStringValue("user-name");
	}
	catch (ElementNotFoundException e) {
	    throw new ElementNotFoundException("<user-name> not found in " + configName + " <db-service>, please check...");
	}
	try {
	    password = agrDbService.getElementStringValue("password");
	}
	catch (ElementNotFoundException e) {
	    throw new ElementNotFoundException("<password> not found in " + configName + " <db-service>, please check...");
	}
	try {
	    batchProcessSize = agrDbService.getElementIntValue("batch-process-size");
	}
	catch (ElementNotFoundException e) {
	    throw new ElementNotFoundException("<batch-process-size> not found in " + configName + " <db-service>, please check...");
	}
    }

    public int getBatchProcessSize() {
	return batchProcessSize;
    }

    public void createInsertDatasConnections() throws ClassNotFoundException, SQLException {
	try {
	    Class.forName(driverClass);
	}
	catch (ClassNotFoundException e) {
	    throw new ClassNotFoundException("Please check driver-class: <" + driverClass + "> is existed");
	}

	// 建立處理法人資料的連線
	createInsertLegalPersonConn();

	// 建立處理公司資料的連線
	createInsertCompanyConn();

	// 建立處理其他資料的連線(在排除名單裡的)
	createInsertOthersConn();

	// 建立處理個人資料的連線
	createInsertPersonalConn();
    }

    private void createInsertCompanyConn() throws SQLException {
	try {
	    connInsertCompany = DriverManager.getConnection(connUrl, userName, password);
	    connInsertCompany.setAutoCommit(false);

	    if (logger.isInfoEnabled()) {
		logger.info("使用帳號：<" + userName + ">，密碼：<" + password + "> 建立處理公司資料的連線，至：<" + connUrl + "> 成功");
	    }
	}
	catch (SQLException e) {
	    throw new SQLException("使用帳號：<" + userName + ">, 密碼：<" + password + "> "
	    			   + "建立處理公司資料的連線，至：<" + connUrl + "> 失敗，錯誤訊息：<" + e.getMessage() + ">");
	}
    }
    
    private void createInsertLegalPersonConn() throws SQLException {
	try {
	    connInsertLegalPerson = DriverManager.getConnection(connUrl, userName, password);
	    connInsertLegalPerson.setAutoCommit(false);

	    if (logger.isInfoEnabled()) {
		logger.info("使用帳號：<" + userName + ">，密碼：<" + password + "> 建立處理法人資料的連線，至：<" + connUrl + "> 成功");
	    }
	}
	catch (SQLException e) {
	    throw new SQLException("使用帳號：<" + userName + ">，密碼：<" + password + "> "
	    			   + "建立處理法人資料的連線，至：<" + connUrl + "> 失敗，錯誤訊息：<" + e.getMessage() + ">");
	}
    }

    private void createInsertPersonalConn() throws SQLException {
	try {
	    connInsertPersonal = DriverManager.getConnection(connUrl, userName, password);
	    connInsertPersonal.setAutoCommit(false);

	    if (logger.isInfoEnabled()) {
		logger.info("使用帳號：<" + userName + ">，密碼：<" + password + "> 建立處理個人資料的連線，至：<" + connUrl + "> 成功");
	    }
	}
	catch (SQLException e) {
	    throw new SQLException("使用帳號：<" + userName + ">，密碼：<" + password + "> "
	    			   + "建立處理個人資料的連線，至：<" + connUrl + "> 失敗，錯誤訊息：<" + e.getMessage() + ">");
	}
    }

    private void createInsertOthersConn() throws SQLException {
	try {
	    connInsertOthers = DriverManager.getConnection(connUrl, userName, password);
	    connInsertOthers.setAutoCommit(false);

	    if (logger.isInfoEnabled()) {
		logger.info("使用帳號：<" + userName + ">，密碼：<" + password + "> 建立處理其他資料的連線，至：<" + connUrl + "> 成功");
	    }
	}
	catch (SQLException e) {
	    throw new SQLException("使用帳號：<" + userName + ">，密碼：<" + password + "> "
	    			   + "建立處理其他資料的連線，至：<" + connUrl + "> 失敗，錯誤訊息：<" + e.getMessage() + ">");
	}
    }

    public void createConnection() throws SQLException {
	try {
	    connForQueryAndUpdate = DriverManager.getConnection(connUrl, userName, password);
	    connForQueryAndUpdate.setAutoCommit(false);

	    if (logger.isInfoEnabled()) {
		logger.info("使用帳號：<" + userName + ">，密碼：<" + password + "> 建立查詢及更新資料的連線，至：<" + connUrl + "> 成功");
	    }
	}
	catch (SQLException e) {
	    throw new SQLException("使用帳號：<" + userName + ">，密碼：<" + password + "> "
	    			   + "建立查詢及更新資料的連線，至：<" + connUrl + "> 失敗，錯誤訊息：<" + e.getMessage() + ">");
	}
    }

    public void insertCompanies(Company[] companiesToDb) {
	try {
	    PreparedStatement ps = connInsertCompany.prepareStatement(INSERT_COMPANY_SQL);

	    for (Company company : companiesToDb) {
		company.setPsValue(ps);
		ps.addBatch();
	    }
	    ps.executeBatch();
	    connInsertCompany.commit();
	}
	catch (SQLException e) {
	    logger.error("Exception raised while inserting companies to DB, rollback transaction", e);
	    try {
		connInsertCompany.rollback();
	    }
	    catch (SQLException e1) {
		logger.error("Exception raised while rollbacking transaction cause insert companies to DB failed", e1);
	    }
	}
    }

    public void insertLegalPersons(LegalPerson[] legalPersonsToDb) {
	try {
	    PreparedStatement ps = connInsertLegalPerson.prepareStatement(INSERT_LEGAL_PERSON_SQL);

	    for (LegalPerson legalPerson : legalPersonsToDb) {
		legalPerson.setPsValue(ps);
		ps.addBatch();
	    }
	    ps.executeBatch();
	    connInsertLegalPerson.commit();
	}
	catch (SQLException e) {
	    logger.error("Exception raised while inserting legal persons to DB, rollback transaction", e);
	    try {
		connInsertLegalPerson.rollback();
	    }
	    catch (SQLException e1) {
		logger.error("Exception raised while rollbacking transaction cause insert legal persons to DB failed", e1);
	    }
	}
    }

    public void insertPersonals(Personal[] personalsToDb) {
	try {
	    PreparedStatement ps = connInsertPersonal.prepareStatement(INSERT_PERSONAL_SQL);

	    for (Personal personal : personalsToDb) {
		personal.setPsValue(ps);
		ps.addBatch();
	    }
	    ps.executeBatch();
	    connInsertPersonal.commit();
	}
	catch (SQLException e) {
	    logger.error("Exception raised while inserting personals to DB, rollback transaction", e);
	    try {
		connInsertPersonal.rollback();
	    }
	    catch (SQLException e1) {
		logger.error("Exception raised while rollbacking transaction cause insert personals to DB failed", e1);
	    }
	}
    }

    public void insertOthers(Others[] othersToDb) {
	try {
	    PreparedStatement ps = connInsertOthers.prepareStatement(INSERT_OTHERS_SQL);

	    for (Others other : othersToDb) {
		other.setPsValue(ps);
		ps.addBatch();
	    }
	    ps.executeBatch();
	    connInsertOthers.commit();
	}
	catch (SQLException e) {
	    logger.error("Exception raised while inserting others to DB, rollback transaction", e);
	    try {
		connInsertOthers.rollback();
	    }
	    catch (SQLException e1) {
		logger.error("Exception raised while rollbacking transaction cause insert others to DB failed", e1);
	    }
	}
    }
    
    public ArrayList<CompanyInfo> queryDistinctCompanies() {
	ArrayList<CompanyInfo> companies = new ArrayList<CompanyInfo>();
	try {
	    PreparedStatement ps = connForQueryAndUpdate.prepareStatement(QUERY_DISTINCT_COMPANIES);
	    
	    long startTime = System.currentTimeMillis();

	    ResultSet rs = ps.executeQuery();

	    if (logger.isDebugEnabled()) {
		logger.debug(ps.toString());
		logger.debug("Query SpentTime: " + (System.currentTimeMillis() - startTime) / 1000 + " secs");
	    }

	    while (rs.next()) {
		CompanyInfo companyInfo = new CompanyInfo();
		
		companyInfo.setCompanyName(rs.getString("VC_OWNER"));
		
		String counties = rs.getString("VC_COUNTIES");
		companyInfo.setCounties(counties);
		
		String townships = rs.getString("VC_TOWNSHIPS");
		companyInfo.setTownships(townships);
		
		companies.add(companyInfo);
	    }
	} 
	catch (SQLException e) {
	    logger.error("Exception raised while queryDistinctCompanies, sql: <" + QUERY_DISTINCT_COMPANIES + ">", e);
	    recreateQueryConnection();
	}
	return companies;
    }

    public int queryCountStar(
	    Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment,  
	    String inputOwner, int areaBigger, int areaSmaller, 
	    RadioButtonStatus radioBtnStatus, boolean neverContact) {
	
	int countStar = 0;
	switch (radioBtnStatus) {
        	case COMPANY:
        	    countStar 
        	    	= queryCountStarFromDiffTableAndCond(
        	    		QUERY_COUNT_STAR_FROM_COMPANY, 
        	    		selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
        	    		inputOwner, areaBigger, areaSmaller, neverContact);
        	    break;
        	    
        	case LEGAL_PERSON:
        	    countStar 
    	    		= queryCountStarFromDiffTableAndCond(
    	    			QUERY_COUNT_STAR_FROM_LEGAL_PERSON, 
    	    			selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
    	    			inputOwner, areaBigger, areaSmaller, neverContact);
        	    break;
        	    
        	case OTHERS:
        	    countStar 
    	    		= queryCountStarFromDiffTableAndCond(
    	    			QUERY_COUNT_STAR_FROM_OTHERS, 
    	    			selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
    	    			inputOwner, areaBigger, areaSmaller, neverContact);
        	    break;
        	    
        	case PERSONAL:
        	    countStar
        	    	= queryCountStarFromDiffTableAndCond(
        	    		QUERY_COUNT_STAR_FROM_PERSONL, 
	    			selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
	    			inputOwner, areaBigger, areaSmaller, neverContact);
        	    break;

        	case ALL:
        	    countStar
        	    	= queryCountStarFromAllTablesAndCond(
        	    		selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
        	    		inputOwner, areaBigger, areaSmaller, neverContact);
        	    break;
	}
	return countStar;
    }

    private int queryCountStarFromDiffTableAndCond(
	    String queryFromSql, 
	    Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment, 
	    String inputOwner, int areaBigger, int areaSmaller, boolean neverContact) {
	
	int countStar = 0;

	StringBuilder sqlBuffer = new StringBuilder();
	sqlBuffer.append(queryFromSql);
	sqlBuffer.append(" where ");
	sqlBuffer.append("N_AREA >= ? and N_AREA <= ?");

	if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_COUNTIES = ?");
	}
	if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_TOWNSHIPS = ?");
	}
	if (!inputLandUsePartition.isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_LAND_USE_PARTITION like ?");
	}
	if (!inputSegment.isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_SEGMENT like ?");
	}
	if (!inputOwner.isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_OWNER like ?");
	}
	if (neverContact) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_ALREADY_CONTACT = ?");
	} else {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_ALREADY_CONTACT = ?");
	}
	
	final String sqlToExecute = sqlBuffer.toString();
	try {
	    PreparedStatement ps = connForQueryAndUpdate.prepareStatement(sqlToExecute);

	    int index = 1;
	    ps.setInt(index++, areaBigger);
	    ps.setInt(index++, areaSmaller);
	    if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
		ps.setNString(index++, selectedCounties.getCountiesName());
	    }
	    if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
		ps.setNString(index++, selectedTownships.getTownshipName());
	    }
	    if (!inputLandUsePartition.isEmpty()) {
		inputLandUsePartition = inputLandUsePartition.trim();
		ps.setNString(index++, "%" + inputLandUsePartition + "%");
	    }
	    if (!inputSegment.isEmpty()) {
		inputSegment = inputSegment.trim();
		ps.setNString(index++, "%" + inputSegment + "%");
	    }
	    if (!inputOwner.isEmpty()) {
		inputOwner = inputOwner.trim();
		ps.setNString(index++, "%" + inputOwner + "%");
	    }
	    if (neverContact) {
		ps.setNString(index++, StrConstant.NO);
	    } 
	    else {
		ps.setNString(index++, StrConstant.YES);
	    }

	    long startTime = System.currentTimeMillis();

	    ResultSet rs = ps.executeQuery();

	    if (logger.isDebugEnabled()) {
		logger.debug(ps.toString());
		logger.debug("Query SpentTime: " + (System.currentTimeMillis() - startTime) / 1000 + " secs");
	    }

	    if (rs.next()) {
		countStar = rs.getInt("totalCnts");
	    }
	} 
	catch (SQLException e) {
	    logger.error("Exception raised while queryCountStarFromDiffTableAndCond, sql: <" + sqlToExecute + ">", e);
	    recreateQueryConnection();
	}
	return countStar;
    }
    
    private int queryCountStarFromAllTablesAndCond(
            Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment, 
            String inputOwner, int areaBigger, int areaSmaller, boolean neverContact) {
    
        int countStar = 0;
        
        StringBuilder sqlBuffer = new StringBuilder();
        
        sqlBuffer.append("select (")
        	 .append(COMPANY_TABLE_NAME).append(".totalCnts + ")
        	 .append(LEGAL_PERSON_TABLE_NAME).append(".totalCnts + ")
        	 .append(OTHERS_TABLE_NAME).append(".totalCnts + ")
        	 .append(PERSONAL_TABLE_NAME).append(".totalCnts) as allTotalCnts from ");

        String companySubQueryCountStar 
        	= createSubQueryByConds(QUERY_COUNT_STAR_FROM_COMPANY, true, COMPANY_TABLE_NAME, true,
        				selectedCounties, selectedTownships, inputLandUsePartition, inputSegment,  
        				inputOwner, areaBigger, areaSmaller, neverContact);
        sqlBuffer.append(companySubQueryCountStar);
        
        String legalPersonSubQueryCountStar
        	= createSubQueryByConds(QUERY_COUNT_STAR_FROM_LEGAL_PERSON, true, LEGAL_PERSON_TABLE_NAME, true, 
        				selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
        				inputOwner, areaBigger, areaSmaller, neverContact);
        sqlBuffer.append(legalPersonSubQueryCountStar);
        
        String othersSubQueryCountStar
        	= createSubQueryByConds(QUERY_COUNT_STAR_FROM_OTHERS, true, OTHERS_TABLE_NAME, true, 
        				selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
        				inputOwner, areaBigger, areaSmaller, neverContact);
        sqlBuffer.append(othersSubQueryCountStar);
        
        String personalSubQueryCountStar
        	= createSubQueryByConds(QUERY_COUNT_STAR_FROM_PERSONL, true, PERSONAL_TABLE_NAME, false, 
        				selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
        				inputOwner, areaBigger, areaSmaller, neverContact);
        sqlBuffer.append(personalSubQueryCountStar);
        
        final String sqlToExecute = sqlBuffer.toString();
        try {
	    PreparedStatement ps = connForQueryAndUpdate.prepareStatement(sqlToExecute);

	    int index = 1;
	    
	    for (int i = 0; i < 4; i++) {
		ps.setInt(index++, areaBigger);
		ps.setInt(index++, areaSmaller);
		if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
		    ps.setNString(index++, selectedCounties.getCountiesName());
		}
		if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
		    ps.setNString(index++, selectedTownships.getTownshipName());
		}
		if (!inputLandUsePartition.isEmpty()) {
		    ps.setNString(index++, "%" + inputLandUsePartition + "%");
		}
		if (!inputSegment.isEmpty()) {
		    ps.setNString(index++, "%" + inputSegment + "%");
		}
		if (!inputOwner.isEmpty()) {
		    inputOwner = inputOwner.trim();
		    ps.setNString(index++, "%" + inputOwner + "%");
		}
		if (neverContact) {
		    ps.setNString(index++, StrConstant.NO);
		} 
		else {
		    ps.setNString(index++, StrConstant.YES);
		}
	    }

	    long startTime = System.currentTimeMillis();

	    ResultSet rs = ps.executeQuery();

	    if (logger.isDebugEnabled()) {
		logger.debug(ps.toString());
		logger.debug("Query SpentTime: " + (System.currentTimeMillis() - startTime) / 1000 + " secs");
	    }

	    if (rs.next()) {
		countStar = rs.getInt("allTotalCnts");
	    }
	} 
	catch (SQLException e) {
	    logger.error("Exception raised while queryCountStarFromAllTablesAndCond, sql: <" + sqlToExecute + ">", e);
	    recreateQueryConnection();
	}
        return countStar;
    }

    private void recreateQueryConnection() {
	if (connForQueryAndUpdate != null) {
	    try {
		connForQueryAndUpdate.close();
	    }
	    catch (SQLException e) {
		logger.error("Exception raised while closing connForQuery", e);
	    }
	    finally {
		connForQueryAndUpdate = null;
	    }
	}
	try {
	    createConnection();
	}
	catch (SQLException e) {
	    logger.error("Exception raised while recreate connForQuery", e);
	}
    }

    public ArrayList<LandInfo> queryLandInfos(
	    Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment, 
	    String inputOwner, int areaBigger, int areaSmaller, 
	    RadioButtonStatus radioBtnStatus, boolean neverContact, 
	    int start, int pageSize) {
	
	ArrayList<LandInfo> landInfos = new ArrayList<LandInfo>();
	switch (radioBtnStatus) {
		case COMPANY:
		    landInfos 
		    	= queryLandInfoFromDiffTableAndCond(
		    		QUERY_LAND_INFO_FROM_COMPANY, 
		    		selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
		    		inputOwner, areaBigger, areaSmaller, neverContact, 
		    		start, pageSize);
		    break;

		case LEGAL_PERSON:
		    landInfos 
		    	= queryLandInfoFromDiffTableAndCond(
		    		QUERY_LAND_INFO_FROM_LEGAL_PERSON, 
	      			selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
	      			inputOwner, areaBigger, areaSmaller, neverContact, 
	      			start, pageSize);
		    break;

		case OTHERS:
		    landInfos 
		    	= queryLandInfoFromDiffTableAndCond(
		    		QUERY_LAND_INFO_FROM_OTHERS, 
		    		selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
		    		inputOwner, areaBigger, areaSmaller, neverContact, 
		    		start, pageSize);
		    break;

		case PERSONAL:
		    landInfos
		    	= queryLandInfoFromDiffTableAndCond(
		    		QUERY_LAND_INFO_FROM_PERSONL,
		    		selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
		    		inputOwner, areaBigger, areaSmaller, neverContact,
		    		start, pageSize);
		    break;

        	case ALL:
        	    landInfos
        	    	= queryLandInfoFromAllTablesAndCond(
        	    		selectedCounties, selectedTownships, inputLandUsePartition, inputSegment, 
        	    		inputOwner, areaBigger, areaSmaller, neverContact,
		    		start, pageSize);
        	    break;
	}
	return landInfos;
    }
	
    private ArrayList<LandInfo> queryLandInfoFromDiffTableAndCond(
	    String queryFromSql,
	    Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment,  
	    String inputOwner, int areaBigger, int areaSmaller, boolean neverContact, 
	    int start, int pageSize) {

    	ArrayList<LandInfo> landInfos = new ArrayList<LandInfo>();
	try {
	    StringBuilder sqlBuffer = new StringBuilder();
	    sqlBuffer.append(queryFromSql);
	    sqlBuffer.append(" where ");
	    sqlBuffer.append("N_AREA >= ? and N_AREA <= ?");
	    
	    if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
		sqlBuffer.append(" and ");
		sqlBuffer.append("VC_COUNTIES = ?");
	    }
	    if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
		sqlBuffer.append(" and ");
		sqlBuffer.append("VC_TOWNSHIPS = ?");
	    }
	    if (!inputLandUsePartition.isEmpty()) {
		sqlBuffer.append(" and ");
		sqlBuffer.append("VC_LAND_USE_PARTITION like ?");
	    }
	    if (!inputSegment.isEmpty()) {
		sqlBuffer.append(" and ");
		sqlBuffer.append("VC_SEGMENT like ?");
	    }
	    if (!inputOwner.isEmpty()) {
		sqlBuffer.append(" and ");
		sqlBuffer.append("VC_OWNER like ?");
	    }
	    if (neverContact) {
		sqlBuffer.append(" and ");
		sqlBuffer.append("VC_ALREADY_CONTACT = ?");
	    }
	    else {
		sqlBuffer.append(" and ");
		sqlBuffer.append("VC_ALREADY_CONTACT = ?");
	    }
	    sqlBuffer.append(" order by VC_SEGMENT, VC_LAND_NO ");
	    sqlBuffer.append(" limit ?, ?");
	    
	    PreparedStatement ps = connForQueryAndUpdate.prepareStatement(sqlBuffer.toString());
	    
	    int index = 1;
	    ps.setInt(index++, areaBigger);
	    ps.setInt(index++, areaSmaller);
	    if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
		ps.setNString(index++, selectedCounties.getCountiesName());
	    }
	    if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
		ps.setNString(index++, selectedTownships.getTownshipName());
	    }
	    if (!inputLandUsePartition.isEmpty()) {
		inputLandUsePartition = inputLandUsePartition.trim();
		ps.setNString(index++, "%" + inputLandUsePartition + "%");
	    }
	    if (!inputSegment.isEmpty()) {
		inputSegment = inputSegment.trim();
		ps.setNString(index++, "%" + inputSegment + "%");
	    }
	    if (!inputOwner.isEmpty()) {
		inputOwner = inputOwner.trim();
		ps.setNString(index++, "%" + inputOwner + "%");
	    }
	    if (neverContact) {
		ps.setNString(index++, StrConstant.NO);
	    }
	    else {
		ps.setNString(index++, StrConstant.YES);
	    }
	    ps.setInt(index++, start);
	    ps.setInt(index++, pageSize);
	    
	    long startTime = System.currentTimeMillis();
	    
	    ResultSet rs = ps.executeQuery();

	    if (logger.isDebugEnabled()) {
		logger.debug(ps.toString());
		logger.debug("Query SpentTime: " + (System.currentTimeMillis() - startTime) / 1000 + " secs");
	    }
	    
	    while (rs.next()) {
		String owner = rs.getString("VC_OWNER");
		String counties = rs.getString("VC_COUNTIES");
		String townships = rs.getString("VC_TOWNSHIPS");
		String segment = rs.getString("VC_SEGMENT");
		String landNo = rs.getString("VC_LAND_NO");
		long landPrice = rs.getLong("N_LAND_PRICE");
		double area = rs.getDouble("N_AREA");

		String landUsePartition = rs.getString("VC_LAND_USE_PARTITION");
		int numbersOfBuilding = rs.getInt("N_NUMBERS_OF_BUILDING");
		int privateLegalPerson = rs.getInt("N_PRIVATE_LEGAL_PERSON");
		int naturalPerson = rs.getInt("N_NATURAL_PERSON");
		String alreadyContact = rs.getString("VC_ALREADY_CONTACT");

		String phoneNo = rs.getString("VC_PHONE_NO");
		String extensionUnitUndertaker = rs.getString("VC_EXTENSION_UNIT_UNDERTAKER");
		String address = rs.getString("VC_ADDRESS");

		String chatContent = rs.getString("VC_CHAT_CONTENT");
		long date = rs.getLong("N_CONTACT_DATE");

		LandInfo landInfo 
			= new LandInfo(owner, counties, townships, segment, landNo, landPrice, area, 
				       landUsePartition, numbersOfBuilding, privateLegalPerson, naturalPerson, alreadyContact, 
				       phoneNo, extensionUnitUndertaker, address, 
				       chatContent, date);

		landInfos.add(landInfo);
	    }
	}
	catch (SQLException e) {
	    logger.error("Exception raised while queryFromDifferencTableAndCond, sql: <" + queryFromSql + ">", e);
	    recreateQueryConnection();
	}
	return landInfos;
    }
    
    private ArrayList<LandInfo> queryLandInfoFromAllTablesAndCond(
	    Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment, 
	    String inputOwner, int areaBigger, int areaSmaller, boolean neverContact, 
	    int start, int pageSize) {

	ArrayList<LandInfo> landInfos = new ArrayList<LandInfo>();
	
	StringBuilder sqlBuffer = new StringBuilder();
	
	String companySubQueryLandInfos 
		= createSubQueryByConds(QUERY_LAND_INFO_FROM_COMPANY, false, null, false,
					selectedCounties, selectedTownships, inputLandUsePartition, inputSegment,
					inputOwner, areaBigger, areaSmaller, neverContact);
	sqlBuffer.append(companySubQueryLandInfos);
	
	sqlBuffer.append(" union ");
	
	String legalPersonSubQueryLandInfos
		= createSubQueryByConds(QUERY_LAND_INFO_FROM_LEGAL_PERSON, false, null, false, 
					selectedCounties, selectedTownships, inputLandUsePartition, inputSegment,
					inputOwner, areaBigger, areaSmaller, neverContact);
	sqlBuffer.append(legalPersonSubQueryLandInfos);
	
	sqlBuffer.append(" union ");
	
	String othersSubQueryLandInfos
		= createSubQueryByConds(QUERY_LAND_INFO_FROM_OTHERS, false, null, false, 
					selectedCounties, selectedTownships, inputLandUsePartition, inputSegment,
					inputOwner, areaBigger, areaSmaller, neverContact);
	sqlBuffer.append(othersSubQueryLandInfos);
	
	sqlBuffer.append(" union ");

	String personalSubQueryLandInfos
		= createSubQueryByConds(QUERY_LAND_INFO_FROM_PERSONL, false, null, false, 
					selectedCounties, selectedTownships, inputLandUsePartition, inputSegment,
					inputOwner, areaBigger, areaSmaller, neverContact);
	sqlBuffer.append(personalSubQueryLandInfos);
	
	sqlBuffer.append(" order by VC_SEGMENT, VC_LAND_NO");
	sqlBuffer.append(" limit ?, ?");
	
	final String sqlToExecute = sqlBuffer.toString();
        try {
	    PreparedStatement ps = connForQueryAndUpdate.prepareStatement(sqlToExecute);

	    int index = 1;
	    
	    for (int i = 0; i < 4; i++) {
		ps.setInt(index++, areaBigger);
		ps.setInt(index++, areaSmaller);
		if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
		    ps.setNString(index++, selectedCounties.getCountiesName());
		}
		if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
		    ps.setNString(index++, selectedTownships.getTownshipName());
		}
		if (!inputLandUsePartition.isEmpty()) {
		    ps.setNString(index++, "%" + inputLandUsePartition + "%");
		}
		if (!inputSegment.isEmpty()) {
		    ps.setNString(index++, "%" + inputSegment + "%");
		}
		if (!inputOwner.isEmpty()) {
		    inputOwner = inputOwner.trim();
		    ps.setNString(index++, "%" + inputOwner + "%");
		}
		if (neverContact) {
		    ps.setNString(index++, StrConstant.NO);
		} 
		else {
		    ps.setNString(index++, StrConstant.YES);
		}
	    }
	    
	    ps.setInt(index++, start);
	    ps.setInt(index++, pageSize);

	    long startTime = System.currentTimeMillis();

	    ResultSet rs = ps.executeQuery();

	    if (logger.isDebugEnabled()) {
		logger.debug(ps.toString());
		logger.debug("Query SpentTime: " + (System.currentTimeMillis() - startTime) / 1000 + " secs");
	    }

	    while (rs.next()) {
		String owner = rs.getString("VC_OWNER");
		String counties = rs.getString("VC_COUNTIES");
		String townships = rs.getString("VC_TOWNSHIPS");
		String segment = rs.getString("VC_SEGMENT");
		String landNo = rs.getString("VC_LAND_NO");
		long landPrice = rs.getLong("N_LAND_PRICE");
		double area = rs.getDouble("N_AREA");

		String landUsePartition = rs.getString("VC_LAND_USE_PARTITION");
		int numbersOfBuilding = rs.getInt("N_NUMBERS_OF_BUILDING");
		int privateLegalPerson = rs.getInt("N_PRIVATE_LEGAL_PERSON");
		int naturalPerson = rs.getInt("N_NATURAL_PERSON");
		String alreadyContact = rs.getString("VC_ALREADY_CONTACT");

		String phoneNo = rs.getString("VC_PHONE_NO");
		String extensionUnitUndertaker = rs.getString("VC_EXTENSION_UNIT_UNDERTAKER");
		String address = rs.getString("VC_ADDRESS");

		String chatContent = rs.getString("VC_CHAT_CONTENT");
		long date = rs.getLong("N_CONTACT_DATE");

		LandInfo landInfo 
			= new LandInfo(owner, counties, townships, segment, landNo, landPrice, area, 
				       landUsePartition, numbersOfBuilding, privateLegalPerson, naturalPerson, alreadyContact, 
				       phoneNo, extensionUnitUndertaker, address, 
				       chatContent, date);

		landInfos.add(landInfo);
	    }
	} 
	catch (SQLException e) {
	    logger.error("Exception raised while queryLandInfoFromAllTablesAndCond, sql: <" + sqlToExecute + ">", e);
	    recreateQueryConnection();
	}
	return landInfos;
    }
    
    private String createSubQueryByConds(
	    String baseSql, boolean needTableName, String tableName, boolean needComma, 
	    Counties selectedCounties, Townships selectedTownships, String inputLandUsePartition, String inputSegment, 
	    String inputOwner, int areaBigger, int areaSmaller, boolean neverContact) {

	StringBuilder sqlBuffer = new StringBuilder();
	
	sqlBuffer.append("(");
	sqlBuffer.append(baseSql);
	sqlBuffer.append(" where ");
	sqlBuffer.append("N_AREA >= ? and N_AREA <= ?");

	if (selectedCounties != null && !selectedCounties.getCountiesName().isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_COUNTIES = ?");
	}
	if (selectedTownships != null && !selectedTownships.getTownshipName().isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_TOWNSHIPS = ?");
	}
	if (!inputLandUsePartition.isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_LAND_USE_PARTITION like ?");
	}
	if (!inputSegment.isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_SEGMENT like ?");
	}
	if (!inputOwner.isEmpty()) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_OWNER like ?");
	}
	if (neverContact) {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_ALREADY_CONTACT = ?");
	} 
	else {
	    sqlBuffer.append(" and ");
	    sqlBuffer.append("VC_ALREADY_CONTACT = ?");
	}
	sqlBuffer.append(") ");

	if (needTableName) {
	    sqlBuffer.append(tableName);
	}
	
	if (needComma) {
	    sqlBuffer.append(", ");
	}
	return sqlBuffer.toString();
    }

    public void updateCompanyNoAndAddress(ArrayList<CompanyInfo> successFoundCompanies) {
	final int BATCH_EXECUTE_SIZE = 1000;
	try {
	    PreparedStatement ps = connForQueryAndUpdate.prepareStatement(UPDATE_COMPANY_NO_ADDRESS); 

	    int totalDatasSize = successFoundCompanies.size();
	    		
	    int index = 1;
	    int handledDataCnts = 0;
	    for (CompanyInfo company : successFoundCompanies) {
		// update value
		ps.setNString(index++, company.getPhoneNo());
		ps.setNString(index++, company.getAddress());
		
		// where
		ps.setNString(index++, company.getCompanyName());
		ps.setNString(index++, company.getCounties());
		ps.setNString(index++, company.getTownships());
		
		ps.addBatch();

		index = 1;
		handledDataCnts++;
		
		if (handledDataCnts % BATCH_EXECUTE_SIZE == 0) {
		    logger.info("開始更新 " + (handledDataCnts - BATCH_EXECUTE_SIZE + 1) + " ~ " + handledDataCnts + " 筆...");
		    ps.executeBatch();
		    ps.clearBatch();
		}
		else if (handledDataCnts == totalDatasSize) {
		    logger.info("開始更新 " + (handledDataCnts - BATCH_EXECUTE_SIZE + 1) + " ~ " + handledDataCnts + " 筆...");
		    ps.executeBatch();
		    ps.clearBatch();
		}
	    }
	    connForQueryAndUpdate.commit();
	}
	catch (SQLException e) {
	    logger.error("Exception raised while updating compnay phoneNo and address, rollback transaction", e);
	    try {
		connForQueryAndUpdate.rollback();
	    }
	    catch (SQLException e1) {
		logger.error("Exception raised while rollbacking transaction cause update company phoneNo and address failed", e1);
	    }
	}
    }
}