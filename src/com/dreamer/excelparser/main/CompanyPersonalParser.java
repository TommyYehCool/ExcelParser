package com.dreamer.excelparser.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dreamer.common.constant.ConfigSetting;
import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.constant.StrConstant;
import com.dreamer.common.util.AccessDbUtil;
import com.dreamer.common.util.ExcelUtil;
import com.dreamer.excelparser.config.CppConfigLoader;
import com.dreamer.excelparser.config.DBConfigLoader;
import com.dreamer.excelparser.db.handler.CompanyToDbHandler;
import com.dreamer.excelparser.db.handler.LegalPersonToDbHandler;
import com.dreamer.excelparser.db.handler.OthersToDbHandler;
import com.dreamer.excelparser.db.handler.PersonalToDbHandler;
import com.dreamer.excelparser.db.queue.CompanyProcessStatusQ;
import com.dreamer.excelparser.db.queue.CompanyToDbQ;
import com.dreamer.excelparser.db.queue.LegalPersonProcessStatusQ;
import com.dreamer.excelparser.db.queue.LegalPersonToDbQ;
import com.dreamer.excelparser.db.queue.OthersProcessStatusQ;
import com.dreamer.excelparser.db.queue.OthersToDbQ;
import com.dreamer.excelparser.db.queue.PersonalProcessStatusQ;
import com.dreamer.excelparser.db.queue.PersonalToDbQ;
import com.dreamer.excelparser.enu.OwnerClassify;
import com.dreamer.excelparser.exception.ExcelParserException;
import com.dreamer.excelparser.format.AdditionalResultDef;
import com.dreamer.excelparser.format.CppSheet1AndResultCellDef;
import com.dreamer.excelparser.vo.Company;
import com.dreamer.excelparser.vo.LegalPerson;
import com.dreamer.excelparser.vo.Others;
import com.dreamer.excelparser.vo.Personal;
import com.syscom.safe.util.xml.ElementNotFoundException;
import com.syscom.safe.util.xml.XmlAggregate;

public class CompanyPersonalParser {
    // ------------- Logger -------------
    private Logger mLogger = Logger.getLogger(Log4jSetting.COMPANY_PERSONAL_PARSER);

    // ------------- Running needed -------------
    private DBConfigLoader mDBConfigLoader = DBConfigLoader.getInstance();
    private CppConfigLoader mCppConfigLoader = CppConfigLoader.getInstance();
    private Set<String> SPECIAL_PATTERN;

    // ------------- Input -------------
    private File[] mAllExcelsToAnalysis;

    private final String COMPANY_KEY_WORD = "公司";
    private final String LEGAL_PERSON_KEY_WORD = "法人";

    private Map<String, Set<String>> mCompaniesData = new Hashtable<String, Set<String>>();
    private Map<String, Set<String>> mLegalPersonData = new Hashtable<String, Set<String>>();
    private Map<String, Set<String>> mPersonalData = new Hashtable<String, Set<String>>();

    // ------------- Output -------------
    private final String COMPANY_FILE_NAME = "公司.xlsx";
    private final String COMPANY_SHEET_NAME = "公司";

    private final String LEGAL_PERSON_FILE_NAME = "法人.xlsx";
    private final String LEGAL_PERSON_SHEET_NAME = "法人";

    private final String PERSONAL_FILE_NAME = "個人.xlsx";
    private final String PERSONAL_SHEET_NAME = "個人";

    private OwnerContentsComparator comparator = new OwnerContentsComparator();

    // ------------- DB -------------
    private CompanyToDbHandler companyToDbHandler;
    private LegalPersonToDbHandler legalPersonToDbHandler;
    private PersonalToDbHandler personalToDbHandler;
    private OthersToDbHandler othersToDbHandler;

    private int TOTAL_COMPANY_COUNTS = 0;
    private int TOTAL_LEGAL_PERSON_COUNTS = 0;
    private int TOTAL_PERSONAL_COUNTS = 0;
    private int TOTAL_OTHERS_COUNTS = 0;

    enum OutputWay {
	CREATE_EXCEL, INSERT_DB;
    }

    private void init() {
	initLog4j();

	mDBConfigLoader.init();

	mCppConfigLoader.init();

	SPECIAL_PATTERN = mCppConfigLoader.getExcludePattern();

	initAccessDbUtil();
    }

    private void initLog4j() {
	String log4jConfigPath = Log4jSetting.ONLINE_LOG4J_PATH;

	File onlineLog4jSetting = new File(log4jConfigPath);
	if (!onlineLog4jSetting.isFile()) {
	    log4jConfigPath = Log4jSetting.TEST_LOG4J_PATH;
	}

	PropertyConfigurator.configure(log4jConfigPath);

	mLogger.info("Log 機制啟動成功");
	mLogger.info(StrConstant.SEPRATE_LINE);
    }

    private void initAccessDbUtil() {
	XmlAggregate agrDbService = mDBConfigLoader.getDbServiceConfig();
	if (agrDbService == null) {
	    mLogger.warn("<db-service> not found in " + ConfigSetting.DB_CONFIG_NAME + ", db-service will not be started...");
	}
	else {
	    try {
		AccessDbUtil.getInstance().init(mLogger, ConfigSetting.DB_CONFIG_NAME, agrDbService);
	    }
	    catch (ElementNotFoundException | ClassNotFoundException e) {
		mLogger.error(e);
		System.exit(1);
	    }
	}
    }

    private void start(String folderPath, OutputWay outputWay, String resultFolderPath) {
	File folder = new File(folderPath);

	checkInputFolder(folderPath, folder);

	mLogger.info("嘗試取得 " + folderPath + " 底下所有資料...");

	ArrayList<File> listAllExcelFile = getAllExcelFile(folder);
	mAllExcelsToAnalysis = listAllExcelFile.toArray(new File[0]);

	if (mAllExcelsToAnalysis.length != 0) {
	    mLogger.info("共有 " + mAllExcelsToAnalysis.length + " 個 excel 要處理:\n");
	    switch (outputWay) {
	    case CREATE_EXCEL:
		mLogger.info("將把分析好的資料，產生公司、個人、法人三個 Excels...");
		break;

	    case INSERT_DB:
		mLogger.info("將把分析好的資料，寫入DB...");
		mLogger.info(StrConstant.SEPRATE_LINE);
		try {
		    AccessDbUtil.getInstance().createInsertDatasConnections();
		    mLogger.info(StrConstant.SEPRATE_LINE);

		    String threadName = null;

		    // 啟動 Handler
		    legalPersonToDbHandler = new LegalPersonToDbHandler();
		    threadName = legalPersonToDbHandler.getName();
		    legalPersonToDbHandler.start();
		    mLogger.info("啟動將法人資料寫入 DB 的程式: " + threadName + " 成功");

		    companyToDbHandler = new CompanyToDbHandler();
		    threadName = companyToDbHandler.getName();
		    companyToDbHandler.start();
		    mLogger.info("啟動將公司資料寫入 DB 的程式: " + threadName + " 成功");

		    othersToDbHandler = new OthersToDbHandler();
		    threadName = othersToDbHandler.getName();
		    othersToDbHandler.start();
		    mLogger.info("啟動將其他資料寫入 DB 的程式: " + threadName + " 成功");

		    personalToDbHandler = new PersonalToDbHandler();
		    threadName = personalToDbHandler.getName();
		    personalToDbHandler.start();
		    mLogger.info("啟動將個人資料寫入 DB 的程式: " + threadName + " 成功");
		}
		catch (ClassNotFoundException | SQLException e) {
		    mLogger.error("連線至 DB 產生問題", e);
		    System.exit(1);
		}
		break;
	    }

	    long startTime = System.currentTimeMillis();

	    analysisExcelContent(outputWay);

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("全部分析結束, 共花了: <" + (System.currentTimeMillis() - startTime) / 1000 + " 秒>");
	    mLogger.info(StrConstant.SEPRATE_LINE);

	    if (outputWay == OutputWay.CREATE_EXCEL) {
		generateAnalysisResult(resultFolderPath);
	    }
	}
	else {
	    mLogger.error("請確認資料夾底下有 excel 檔案!!!");
	}
    }

    private void checkInputFolder(String folderPath, File folder) {
	if (!folder.isDirectory()) {
	    mLogger.error("Error: 請確認你輸入為資料夾: " + folderPath);
	    System.exit(1);
	}

	File[] listFiles = folder.listFiles();
	if (listFiles == null || listFiles.length == 0) {
	    mLogger.error("Error: 請確認你輸入的資料夾底下有資料: " + folderPath);
	    System.exit(1);
	}
    }

    private ArrayList<File> getAllExcelFile(File folder) {
	ArrayList<File> listAllExcelFiles = new ArrayList<File>();
	File[] files = folder.listFiles();
	if (files != null && files.length != 0) {
	    for (File file : files) {
		if (file.isFile()) {
		    try {
			String canonicalPath = file.getCanonicalPath();
			if (canonicalPath.endsWith("xls") || canonicalPath.endsWith("xlsx")) {
			    if (file.length() != 0) {
				if (mLogger.isDebugEnabled()) {
				    mLogger.debug("檔案: " + canonicalPath);
				}
				listAllExcelFiles.add(file);
			    }
			    else {
				mLogger.warn("檔案: " + canonicalPath + " 是空的, 不處理...");
			    }
			}
		    }
		    catch (IOException e) {
			mLogger.error("IOException raised while get file path", e);
		    }
		}
		else {
		    listAllExcelFiles.addAll(getAllExcelFile(file));
		}
	    }
	}
	return listAllExcelFiles;
    }

    private void analysisExcelContent(OutputWay outputWay) {
	for (int i = 0; i < mAllExcelsToAnalysis.length; i++) {
	    File excelFile = mAllExcelsToAnalysis[i];

	    String filePath = null;
	    FileInputStream fis = null;
	    XSSFWorkbook workbook = null;
	    try {
		filePath = excelFile.getCanonicalPath();

		mLogger.info(StrConstant.SEPRATE_LINE);
		mLogger.info("開始分析，第 " + (i + 1) + " 個檔案：" + filePath);

		fis = new FileInputStream(excelFile);

		workbook = new XSSFWorkbook(fis);

		XSSFSheet sheet = workbook.getSheetAt(0);

		switch (outputWay) {
		case CREATE_EXCEL:
		    parseSheet1ToMemory(sheet);
		    break;

		case INSERT_DB:
		    parseSheet1ToDB(sheet);
		    break;
		}
	    }
	    catch (IOException e) {
		mLogger.error("分析: <" + filePath + "> 內容, 產生 IOException, msg: <" + e.getMessage() + ">", e);
	    }
	    catch (Exception e) {
		mLogger.error("分析: <" + filePath + "> 內容, 產生 Exception, msg: <" + e.getMessage() + ">", e);
	    }
	    finally {
		if (workbook != null) {
		    try {
			workbook.close();
			workbook = null;
		    }
		    catch (IOException e) {
			mLogger.error("IOException raised while closing XSSFWorkbook", e);
		    }
		}
		if (fis != null) {
		    try {
			fis.close();
			fis = null;
		    }
		    catch (IOException e) {
			mLogger.error("IOException raised while closing FileInputStream", e);
		    }
		}
	    }
	}

	// 將總筆數塞入 Handler, 用來判斷何時結束
	if (outputWay == OutputWay.INSERT_DB) {
	    mLogger.info(StrConstant.SEPRATE_LINE);

	    legalPersonToDbHandler.setTotalLegalPersons(TOTAL_LEGAL_PERSON_COUNTS);
	    mLogger.info("法人資料，共 " + TOTAL_LEGAL_PERSON_COUNTS + " 筆要寫入 DB");

	    companyToDbHandler.setTotalCompanies(TOTAL_COMPANY_COUNTS);
	    mLogger.info("公司資料，共 " + TOTAL_COMPANY_COUNTS + " 筆要寫入 DB");

	    othersToDbHandler.setTotalOthers(TOTAL_OTHERS_COUNTS);
	    mLogger.info("其他資料，共 " + TOTAL_OTHERS_COUNTS + " 筆要寫入 DB");

	    personalToDbHandler.setTotalPersonals(TOTAL_PERSONAL_COUNTS);
	    mLogger.info("個人資料，共 " + TOTAL_PERSONAL_COUNTS + " 筆要寫入 DB");
	}
    }

    private void parseSheet1ToMemory(XSSFSheet sheet) throws ExcelParserException {
	int numberOfRows = sheet.getPhysicalNumberOfRows();

	int numberOfDatas = 0;

	StringBuilder contentBuffer = new StringBuilder();

	// 這邊注意, 從第一列開始處理
	for (int currentRow = 1; currentRow < numberOfRows; currentRow++) {
	    try {
		// 如果取得的 row 是 null, 代表到底了, 結束處理這份 Excel
		XSSFRow row = sheet.getRow(currentRow);
		if (row == null) {
		    break;
		}

		// 先取出第 0 個位置, 如為空值代表到底了, 結束處理這份 Excel
		String cell0_val = ExcelUtil.getCellValue(mLogger, row, 0);
		if ("".equals(cell0_val)) {
		    break;
		}

		// 確認有資料的筆數
		numberOfDatas++;

		// 所有權人列表欄位
		String ownerList = null;

		// 取出要擷取的欄位
		CppSheet1AndResultCellDef[] srcToExtractCols = CppSheet1AndResultCellDef.getSrcColsToExtract();

		// 擷取需要的欄位並 append 到 contentBuffer
		for (CppSheet1AndResultCellDef sheet1DataDef : srcToExtractCols) {
		    if (sheet1DataDef == CppSheet1AndResultCellDef.OWNER_LIST) {
			ownerList = ExcelUtil.getCellValue(mLogger, row, CppSheet1AndResultCellDef.OWNER_LIST.getSrcCellIndex());
			if (ownerList == null || "".equals(ownerList.trim())) {
			    ownerList = "-";
			}
		    }
		    else {
			String cellValue = ExcelUtil.getCellValue(mLogger, row, sheet1DataDef.getSrcCellIndex());
			if (cellValue == null || "".equals(cellValue.trim())) {
			    cellValue = sheet1DataDef.isNumber() ? "0" : "-";
			}
			contentBuffer.append(cellValue);
			if (sheet1DataDef != CppSheet1AndResultCellDef.NATURAL_PERSON) {
			    contentBuffer.append("\t");
			}
		    }
		}

		String content = contentBuffer.toString();
		contentBuffer.setLength(0);

		// 分解所有權人列表欄位
		String[] owners = null;
		if (ownerList.contains(",")) {
		    owners = ownerList.split(",");
		}
		else if (ownerList.contains(";")) {
		    owners = ownerList.split(";");
		}
		else {
		    owners = new String[] { ownerList };
		}

		// 根據每位所有權人，將資料放到 map 中
		for (String owner : owners) {
		    String[] noAndName = owner.split(" ");
		    owner = noAndName.length == 2 ? noAndName[1] : owner;
		    owner = owner.trim();

		    // 把不處理的濾掉
		    if (isInExcludePattern(owner)) {
			continue;
		    }
		    // 處理公司
		    else if (owner.contains(COMPANY_KEY_WORD)) {
			String company = owner;

			Set<String> contents = null;
			if (!mCompaniesData.containsKey(company)) {
			    contents = new HashSet<String>();
			    contents.add(content);
			    mCompaniesData.put(company, contents);
			}
			else {
			    contents = mCompaniesData.get(company);
			    contents.add(content);
			}
		    }
		    // 處理法人
		    else if (owner.contains(LEGAL_PERSON_KEY_WORD)) {
			String legalPerson = owner;

			Set<String> contents = null;
			if (!mLegalPersonData.containsKey(legalPerson)) {
			    contents = new HashSet<String>();
			    contents.add(content);
			    mLegalPersonData.put(legalPerson, contents);
			}
			else {
			    contents = mLegalPersonData.get(legalPerson);
			    contents.add(content);
			}
		    }
		    // 處理個人
		    else {
			String personal = owner;

			Set<String> contents = null;
			if (!mPersonalData.containsKey(personal)) {
			    contents = new HashSet<String>();
			    contents.add(content);
			    mPersonalData.put(personal, contents);
			}
			else {
			    contents = mPersonalData.get(personal);
			    contents.add(content);
			}
		    }
		}
	    }
	    catch (Exception e) {
		String errorMsg = "問題發生於第" + currentRow + "列";
		throw new ExcelParserException(errorMsg, e);
	    }
	}
	mLogger.info("全部資料共 " + numberOfDatas + " 筆");
    }

    private void parseSheet1ToDB(XSSFSheet sheet) throws ExcelParserException {
	int numberOfRows = sheet.getPhysicalNumberOfRows();

	int numberOfDatas = 0;

	StringBuilder contentBuffer = new StringBuilder();

	// 這邊注意, 從第一列開始處理
	for (int currentRow = 1; currentRow < numberOfRows; currentRow++) {
	    try {
		// 如果取得的 row 是 null, 代表到底了, 結束處理這份 Excel
		XSSFRow row = sheet.getRow(currentRow);
		if (row == null) {
		    break;
		}

		// 先取出第 0 個位置, 如為空值代表到底了, 結束處理這份 Excel
		String cell0_val = ExcelUtil.getCellValue(mLogger, row, 0);
		if ("".equals(cell0_val)) {
		    break;
		}

		// 確認有資料的筆數
		numberOfDatas++;

		// 所有權人列表欄位
		String ownerList = null;

		// 取出要擷取的欄位
		CppSheet1AndResultCellDef[] srcToExtractCols = CppSheet1AndResultCellDef.getSrcColsToExtract();

		// 擷取需要的欄位並 append 到 contentBuffer
		for (CppSheet1AndResultCellDef sheet1DataDef : srcToExtractCols) {
		    if (sheet1DataDef == CppSheet1AndResultCellDef.OWNER_LIST) {
			ownerList = ExcelUtil.getCellValue(mLogger, row, CppSheet1AndResultCellDef.OWNER_LIST.getSrcCellIndex());
			if (ownerList == null || "".equals(ownerList.trim())) {
			    ownerList = "-";
			}
		    }
		    else {
			String cellValue = ExcelUtil.getCellValue(mLogger, row, sheet1DataDef.getSrcCellIndex());
			if (cellValue == null || "".equals(cellValue.trim())) {
			    cellValue = sheet1DataDef.isNumber() ? "0" : "-";
			}
			contentBuffer.append(cellValue);
			if (sheet1DataDef != CppSheet1AndResultCellDef.NATURAL_PERSON) {
			    contentBuffer.append("\t");
			}
		    }
		}

		String content = contentBuffer.toString();
		contentBuffer.setLength(0);

		// 分解所有權人列表欄位
		String[] owners = null;
		if (ownerList.contains(",")) {
		    owners = ownerList.split(",");
		}
		else if (ownerList.contains(";")) {
		    owners = ownerList.split(";");
		}
		else {
		    owners = new String[] { ownerList };
		}

		// 根據每位所有權人，將資料寫入 DB
		for (String owner : owners) {
		    String[] noAndName = owner.split(" ");
		    owner = noAndName.length == 2 ? noAndName[1] : owner;
		    owner = owner.trim();

		    // 處理在排除名單內的
		    if (isInExcludePattern(owner)) {
			String strOthers = owner;

			Others others = new Others(strOthers, content);

			TOTAL_OTHERS_COUNTS++;

			OthersToDbQ.getInstance().offer(others);
		    }
		    // 處理公司
		    else if (owner.contains(COMPANY_KEY_WORD)) {
			String strCompany = owner;

			Company company = new Company(strCompany, content);

			TOTAL_COMPANY_COUNTS++;

			CompanyToDbQ.getInstance().offer(company);
		    }
		    // 處理法人
		    else if (owner.contains(LEGAL_PERSON_KEY_WORD)) {
			String strLegalPerson = owner;

			LegalPerson legalPerson = new LegalPerson(strLegalPerson, content);

			TOTAL_LEGAL_PERSON_COUNTS++;

			LegalPersonToDbQ.getInstance().offer(legalPerson);
		    }
		    // 處理個人
		    else {
			String strPersonal = owner;

			Personal personal = new Personal(strPersonal, content);

			TOTAL_PERSONAL_COUNTS++;

			PersonalToDbQ.getInstance().offer(personal);
		    }
		}
	    }
	    catch (Exception e) {
		String errorMsg = "問題發生於第" + currentRow + "列";
		throw new ExcelParserException(errorMsg, e);
	    }
	}
	mLogger.info("Excel 共有 " + numberOfDatas + " 筆");
    }

    private boolean isInExcludePattern(String owner) {
	Iterator<String> itExcludePattern = SPECIAL_PATTERN.iterator();
	while (itExcludePattern.hasNext()) {
	    String excludePattern = itExcludePattern.next();
	    // KeyPoint 這邊用 equals, 因為若用 contains, 有些只是包含就被濾掉
	    if (owner.equals(excludePattern)) {
		// logger.info(owner + " 被濾掉");
		return true;
	    }
	}
	return false;
    }

    private void generateAnalysisResult(String resultFolderPath) {
	if (!resultFolderPath.endsWith("\\")) {
	    resultFolderPath += "\\";
	}

	File resultFolder = new File(resultFolderPath);
	if (!resultFolder.isDirectory()) {
	    mLogger.info("結果路徑不存在...");
	    resultFolder.mkdirs();
	    mLogger.info("產生目錄完成");
	    mLogger.info(StrConstant.SEPRATE_LINE);
	}

	mLogger.info("開始產生結果檔案...");
	long totalStartTime = System.currentTimeMillis();

	// 產生公司資料
	if (mCompaniesData.size() > 0) {
	    long startTime = System.currentTimeMillis();

	    String outputFilePath = resultFolderPath + COMPANY_FILE_NAME;

	    mLogger.info("產生公司資料 " + outputFilePath + "...");

	    createResultFile(mCompaniesData, outputFilePath, COMPANY_SHEET_NAME, OwnerClassify.COMPANY, true);

	    mLogger.info("產生公司資料 " + outputFilePath + " 完成，共花了: <" + (System.currentTimeMillis() - startTime) / 1000 + " 秒>");
	}
	else {
	    mLogger.warn("分析結果，公司資料為 0，將不產生公司資料");
	}

	// 產生法人資料
	if (mLegalPersonData.size() > 0) {
	    long startTime = System.currentTimeMillis();

	    String outputFilePath = resultFolderPath + LEGAL_PERSON_FILE_NAME;

	    mLogger.info("產生法人資料 " + outputFilePath + "...");

	    createResultFile(mLegalPersonData, outputFilePath, LEGAL_PERSON_SHEET_NAME, OwnerClassify.LEGAL_PERSON, true);

	    mLogger.info("產生法人資料 " + outputFilePath + " 完成，共花了: <" + (System.currentTimeMillis() - startTime) / 1000 + " 秒>");
	}
	else {
	    mLogger.warn("分析結果，法人資料為 0，將不產生公司資料");
	}

	// 產生個人資料
	if (mPersonalData.size() > 0) {
	    long startTime = System.currentTimeMillis();

	    String outputFilePath = resultFolderPath + PERSONAL_FILE_NAME;

	    mLogger.info("產生個人資料 " + outputFilePath + "...");

	    createResultFile(mPersonalData, outputFilePath, PERSONAL_SHEET_NAME, OwnerClassify.PERSONAL, true);

	    mLogger.info("產生個人資料 " + outputFilePath + " 完成，共花了: <" + (System.currentTimeMillis() - startTime) / 1000 + " 秒>");
	}
	else {
	    mLogger.warn("分析結果，個人資料為 0，將不產生公司資料");
	}

	mLogger.info("產生結果檔案結束, 共花了: <" + (System.currentTimeMillis() - totalStartTime) / 1000 + " 秒>");
	mLogger.info(StrConstant.SEPRATE_LINE);
    }

    private void createResultFile(Map<String, Set<String>> resultMap, String outputFilePath, String sheetName, OwnerClassify ownerClassify,
	    boolean includeHidden) {
	File file = new File(outputFilePath);

	FileOutputStream fos = null;
	Workbook workbook = null;

	String contentForExceptionLog = null;

	try {
	    fos = new FileOutputStream(file);

	    workbook = new SXSSFWorkbook();

	    // 產生 Title Cell Style
	    XSSFCellStyle titleCellStyle = ExcelUtil.getTitleColCellStyle(workbook);

	    // 產生 sheet
	    Sheet sheet = workbook.createSheet(sheetName);

	    // 根據是否包含隱藏欄位，去取得不同的陣列
	    CppSheet1AndResultCellDef[] resultCells = null;
	    AdditionalResultDef[] additionalResultCells = AdditionalResultDef.values();
	    if (includeHidden) {
		resultCells = CppSheet1AndResultCellDef.getResultColsIncludeHidden();
	    }
	    else {
		resultCells = CppSheet1AndResultCellDef.getResultColsWithoutHidden();
	    }

	    int rowIndex = 0;

	    // create title
	    Row titleRow = sheet.createRow(rowIndex++);
	    for (CppSheet1AndResultCellDef title : resultCells) {
		int cellIndex = includeHidden ? title.getResultWithHiddenCellIndex() : title.getResultWithoutHiddenCellIndex();

		Cell cell = titleRow.createCell(cellIndex);
		cell.setCellValue(title.getTitleName());

		// set Title Cell Style
		cell.setCellStyle(titleCellStyle);
	    }

	    // create additional title
	    for (AdditionalResultDef additionalTitle : additionalResultCells) {
		int cellIndex = includeHidden ? additionalTitle.getResultWithHiddenCellIndex() : additionalTitle.getResultWithoutHiddenCellIndex();

		Cell cell = titleRow.createCell(cellIndex);
		cell.setCellValue(additionalTitle.getTitleName());

		// set Title Cell Style
		cell.setCellStyle(titleCellStyle);
	    }

	    // 將 cache 中的資料產生成 Excel，先加到另一個 vo 為了讓資料量最多的排序在上面
	    List<OwnerContents> ownerContents = new ArrayList<OwnerContents>();
	    String[] owners = resultMap.keySet().toArray(new String[0]);
	    for (String owner : owners) {
		Set<String> contentsSet = resultMap.get(owner);
		OwnerContents ownerContent = new OwnerContents(owner, contentsSet);
		ownerContents.add(ownerContent);
	    }
	    Collections.sort(ownerContents, comparator);

	    for (OwnerContents ownerContent : ownerContents) {
		String owner = ownerContent.getOwner();

		Set<String> contentsSet = ownerContent.getContents();
		for (String content : contentsSet) {
		    contentForExceptionLog = content;

		    String[] splitResults = content.split("\t");

		    Row dataRow = sheet.createRow(rowIndex++);

		    // 先將前面分析出來的資料塞進 row
		    for (CppSheet1AndResultCellDef resultCell : resultCells) {
			int cellIndex = includeHidden ? resultCell.getResultWithHiddenCellIndex() : resultCell.getResultWithoutHiddenCellIndex();

			Cell cell = dataRow.createCell(cellIndex);

			if (resultCell == CppSheet1AndResultCellDef.OWNER_LIST) {
			    String value = owner;
			    cell.setCellValue(value);
			}
			else {
			    boolean isNumber = resultCell.isNumber();

			    if (!isNumber) {
				String value = splitResults[resultCell.getBppContentArrayIndex()];
				cell.setCellValue(value);
			    }
			    else {
				double value = Double.parseDouble(splitResults[resultCell.getBppContentArrayIndex()]);
				cell.setCellValue(value);
			    }
			}
		    }
		}
	    }

	    // KeyPoint freeze the first row
	    sheet.createFreezePane(0, 1);

	    // 產生結果 Excel
	    workbook.write(fos);
	    fos.flush();
	}
	catch (IOException e) {
	    mLogger.error("產生" + outputFilePath + "失敗, IOException raised, msg: <" + e.getMessage() + ">", e);
	}
	catch (Exception e) {
	    mLogger.error("產生" + outputFilePath + "失敗, content: <" + contentForExceptionLog + ">, Exception raised", e);
	}
	finally {
	    if (fos != null) {
		try {
		    fos.close();
		    fos = null;
		}
		catch (IOException e) {
		    mLogger.error("IOException raised while closing fos", e);
		}
	    }
	    if (workbook != null) {
		try {
		    workbook.close();
		    workbook = null;
		}
		catch (IOException e) {
		    mLogger.error("IOException raised while closing workbook", e);
		}
	    }
	}
    }

    private void clearMemory() {
	mAllExcelsToAnalysis = null;
	mCompaniesData.clear();
	mLegalPersonData.clear();
	mPersonalData.clear();

	mLogger.info("成功清除記憶體資料");
    }

    public static void main(String[] args) {
	CompanyPersonalParser companyPersonalParser = new CompanyPersonalParser();

	companyPersonalParser.init();

	Scanner scanIn = new Scanner(System.in);

	while (true) {
	    // -------------- 輸入要 parse 的資料夾 --------------
	    String inputFolderPath = "";
	    while ("".equals(inputFolderPath)) {
		System.out.println("請輸入資料夾路徑: ");
		inputFolderPath = scanIn.nextLine();

		File folder = new File(inputFolderPath);
		if (!folder.isDirectory()) {
		    System.err.println("請輸入正確資料夾路徑...");
		    inputFolderPath = "";
		}
	    }
	    System.out.println("\n資料夾路徑為:\n" + inputFolderPath);

	    // -------------- 詢問是要產生 Excel 或輸入至 DB
	    String inputCreateExcelOrInsertDb = "";
	    while (!inputCreateExcelOrInsertDb.equals("0") && !inputCreateExcelOrInsertDb.equals("1")) {
		System.out.println("\n請選擇要將資料產生 Excel(0) 或 輸入至 DB (1): ");
		inputCreateExcelOrInsertDb = scanIn.nextLine();
		if (!inputCreateExcelOrInsertDb.equals("0") && !inputCreateExcelOrInsertDb.equals("1")) {
		    System.err.println("請輸入 0 或 1");
		}
	    }

	    OutputWay outputWay = inputCreateExcelOrInsertDb.equals("0") ? OutputWay.CREATE_EXCEL : OutputWay.INSERT_DB;

	    String inputResultFolderPath = "";
	    if (inputCreateExcelOrInsertDb.equals("0")) {
		// -------------- 輸入結果產生資料夾 --------------
		while ("".equals(inputResultFolderPath)) {
		    System.out.println("\n請輸入結果產生路徑: ");
		    inputResultFolderPath = scanIn.nextLine();

		    if (!inputResultFolderPath.contains(":\\") && !inputResultFolderPath.contains(":/")) {
			System.err.println("請輸入正確資料夾路徑...");
			inputResultFolderPath = "";
		    }
		}
		System.out.println("\n結果將產生於: " + inputResultFolderPath);
	    }

	    // -------------- 開始 parse 並產生結果 --------------
	    companyPersonalParser.start(inputFolderPath, outputWay, inputResultFolderPath);

	    // 如果為塞入 DB，抓取每個處理結果
	    if (outputWay == OutputWay.INSERT_DB) {
		try {
		    String legalPersonProcessStatus = LegalPersonProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("法人資料塞入 DB 處理結果: <" + legalPersonProcessStatus + ">");

		    String companyProcessStatus = CompanyProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("公司資料塞入 DB 處理結果: <" + companyProcessStatus + ">");

		    String othersProcessStatus = OthersProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("其他資料塞入 DB 處理結果: <" + othersProcessStatus + ">");

		    String personalProcessStatus = PersonalProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("個人資料塞入 DB 處理結果: <" + personalProcessStatus + ">");
		}
		catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	    // -------------- 詢問是否繼續 --------------
	    String inputContinueOrNot = "";
	    while ("".equals(inputContinueOrNot)) {
		System.out.println("是否繼續(y/n)?");
		inputContinueOrNot = scanIn.nextLine();

		if (inputContinueOrNot.compareToIgnoreCase("y") != 0 && inputContinueOrNot.compareToIgnoreCase("n") != 0) {
		    System.err.println("請輸入 y/n");
		    inputContinueOrNot = "";
		}
	    }
	    if ("n".compareToIgnoreCase(inputContinueOrNot) == 0) {
		break;
	    }
	    else {
		companyPersonalParser.clearMemory();
	    }
	}
	scanIn.close();
    }

    /**
     * 用排序用
     */
    private class OwnerContents {
	private String owner;
	private Set<String> contents;

	public OwnerContents(String owner, Set<String> contents) {
	    this.owner = owner;
	    this.contents = contents;
	}

	public String getOwner() {
	    return owner;
	}

	public Set<String> getContents() {
	    return contents;
	}

	public int getContentSize() {
	    return contents != null ? contents.size() : 0;
	}
    }

    private class OwnerContentsComparator implements Comparator<OwnerContents> {
	@Override
	public int compare(OwnerContents o1, OwnerContents o2) {
	    int o1ContentSize = o1.getContentSize();
	    int o2ContentSize = o2.getContentSize();

	    if (o1ContentSize > o2ContentSize) {
		return -1;
	    }
	    if (o1ContentSize < o2ContentSize) {
		return 1;
	    }
	    return 0;
	}
    }
}