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

    private final String COMPANY_KEY_WORD = "���q";
    private final String LEGAL_PERSON_KEY_WORD = "�k�H";

    private Map<String, Set<String>> mCompaniesData = new Hashtable<String, Set<String>>();
    private Map<String, Set<String>> mLegalPersonData = new Hashtable<String, Set<String>>();
    private Map<String, Set<String>> mPersonalData = new Hashtable<String, Set<String>>();

    // ------------- Output -------------
    private final String COMPANY_FILE_NAME = "���q.xlsx";
    private final String COMPANY_SHEET_NAME = "���q";

    private final String LEGAL_PERSON_FILE_NAME = "�k�H.xlsx";
    private final String LEGAL_PERSON_SHEET_NAME = "�k�H";

    private final String PERSONAL_FILE_NAME = "�ӤH.xlsx";
    private final String PERSONAL_SHEET_NAME = "�ӤH";

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

	mLogger.info("Log ����Ұʦ��\");
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

	mLogger.info("���ը��o " + folderPath + " ���U�Ҧ����...");

	ArrayList<File> listAllExcelFile = getAllExcelFile(folder);
	mAllExcelsToAnalysis = listAllExcelFile.toArray(new File[0]);

	if (mAllExcelsToAnalysis.length != 0) {
	    mLogger.info("�@�� " + mAllExcelsToAnalysis.length + " �� excel �n�B�z:\n");
	    switch (outputWay) {
	    case CREATE_EXCEL:
		mLogger.info("�N����R�n����ơA���ͤ��q�B�ӤH�B�k�H�T�� Excels...");
		break;

	    case INSERT_DB:
		mLogger.info("�N����R�n����ơA�g�JDB...");
		mLogger.info(StrConstant.SEPRATE_LINE);
		try {
		    AccessDbUtil.getInstance().createInsertDatasConnections();
		    mLogger.info(StrConstant.SEPRATE_LINE);

		    String threadName = null;

		    // �Ұ� Handler
		    legalPersonToDbHandler = new LegalPersonToDbHandler();
		    threadName = legalPersonToDbHandler.getName();
		    legalPersonToDbHandler.start();
		    mLogger.info("�ҰʱN�k�H��Ƽg�J DB ���{��: " + threadName + " ���\");

		    companyToDbHandler = new CompanyToDbHandler();
		    threadName = companyToDbHandler.getName();
		    companyToDbHandler.start();
		    mLogger.info("�ҰʱN���q��Ƽg�J DB ���{��: " + threadName + " ���\");

		    othersToDbHandler = new OthersToDbHandler();
		    threadName = othersToDbHandler.getName();
		    othersToDbHandler.start();
		    mLogger.info("�ҰʱN��L��Ƽg�J DB ���{��: " + threadName + " ���\");

		    personalToDbHandler = new PersonalToDbHandler();
		    threadName = personalToDbHandler.getName();
		    personalToDbHandler.start();
		    mLogger.info("�ҰʱN�ӤH��Ƽg�J DB ���{��: " + threadName + " ���\");
		}
		catch (ClassNotFoundException | SQLException e) {
		    mLogger.error("�s�u�� DB ���Ͱ��D", e);
		    System.exit(1);
		}
		break;
	    }

	    long startTime = System.currentTimeMillis();

	    analysisExcelContent(outputWay);

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("�������R����, �@��F: <" + (System.currentTimeMillis() - startTime) / 1000 + " ��>");
	    mLogger.info(StrConstant.SEPRATE_LINE);

	    if (outputWay == OutputWay.CREATE_EXCEL) {
		generateAnalysisResult(resultFolderPath);
	    }
	}
	else {
	    mLogger.error("�нT�{��Ƨ����U�� excel �ɮ�!!!");
	}
    }

    private void checkInputFolder(String folderPath, File folder) {
	if (!folder.isDirectory()) {
	    mLogger.error("Error: �нT�{�A��J����Ƨ�: " + folderPath);
	    System.exit(1);
	}

	File[] listFiles = folder.listFiles();
	if (listFiles == null || listFiles.length == 0) {
	    mLogger.error("Error: �нT�{�A��J����Ƨ����U�����: " + folderPath);
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
				    mLogger.debug("�ɮ�: " + canonicalPath);
				}
				listAllExcelFiles.add(file);
			    }
			    else {
				mLogger.warn("�ɮ�: " + canonicalPath + " �O�Ū�, ���B�z...");
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
		mLogger.info("�}�l���R�A�� " + (i + 1) + " ���ɮסG" + filePath);

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
		mLogger.error("���R: <" + filePath + "> ���e, ���� IOException, msg: <" + e.getMessage() + ">", e);
	    }
	    catch (Exception e) {
		mLogger.error("���R: <" + filePath + "> ���e, ���� Exception, msg: <" + e.getMessage() + ">", e);
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

	// �N�`���ƶ�J Handler, �ΨӧP�_��ɵ���
	if (outputWay == OutputWay.INSERT_DB) {
	    mLogger.info(StrConstant.SEPRATE_LINE);

	    legalPersonToDbHandler.setTotalLegalPersons(TOTAL_LEGAL_PERSON_COUNTS);
	    mLogger.info("�k�H��ơA�@ " + TOTAL_LEGAL_PERSON_COUNTS + " ���n�g�J DB");

	    companyToDbHandler.setTotalCompanies(TOTAL_COMPANY_COUNTS);
	    mLogger.info("���q��ơA�@ " + TOTAL_COMPANY_COUNTS + " ���n�g�J DB");

	    othersToDbHandler.setTotalOthers(TOTAL_OTHERS_COUNTS);
	    mLogger.info("��L��ơA�@ " + TOTAL_OTHERS_COUNTS + " ���n�g�J DB");

	    personalToDbHandler.setTotalPersonals(TOTAL_PERSONAL_COUNTS);
	    mLogger.info("�ӤH��ơA�@ " + TOTAL_PERSONAL_COUNTS + " ���n�g�J DB");
	}
    }

    private void parseSheet1ToMemory(XSSFSheet sheet) throws ExcelParserException {
	int numberOfRows = sheet.getPhysicalNumberOfRows();

	int numberOfDatas = 0;

	StringBuilder contentBuffer = new StringBuilder();

	// �o��`�N, �q�Ĥ@�C�}�l�B�z
	for (int currentRow = 1; currentRow < numberOfRows; currentRow++) {
	    try {
		// �p�G���o�� row �O null, �N��쩳�F, �����B�z�o�� Excel
		XSSFRow row = sheet.getRow(currentRow);
		if (row == null) {
		    break;
		}

		// �����X�� 0 �Ӧ�m, �p���ŭȥN��쩳�F, �����B�z�o�� Excel
		String cell0_val = ExcelUtil.getCellValue(mLogger, row, 0);
		if ("".equals(cell0_val)) {
		    break;
		}

		// �T�{����ƪ�����
		numberOfDatas++;

		// �Ҧ��v�H�C�����
		String ownerList = null;

		// ���X�n�^�������
		CppSheet1AndResultCellDef[] srcToExtractCols = CppSheet1AndResultCellDef.getSrcColsToExtract();

		// �^���ݭn������ append �� contentBuffer
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

		// ���ѩҦ��v�H�C�����
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

		// �ھڨC��Ҧ��v�H�A�N��Ʃ�� map ��
		for (String owner : owners) {
		    String[] noAndName = owner.split(" ");
		    owner = noAndName.length == 2 ? noAndName[1] : owner;
		    owner = owner.trim();

		    // �⤣�B�z���o��
		    if (isInExcludePattern(owner)) {
			continue;
		    }
		    // �B�z���q
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
		    // �B�z�k�H
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
		    // �B�z�ӤH
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
		String errorMsg = "���D�o�ͩ��" + currentRow + "�C";
		throw new ExcelParserException(errorMsg, e);
	    }
	}
	mLogger.info("������Ʀ@ " + numberOfDatas + " ��");
    }

    private void parseSheet1ToDB(XSSFSheet sheet) throws ExcelParserException {
	int numberOfRows = sheet.getPhysicalNumberOfRows();

	int numberOfDatas = 0;

	StringBuilder contentBuffer = new StringBuilder();

	// �o��`�N, �q�Ĥ@�C�}�l�B�z
	for (int currentRow = 1; currentRow < numberOfRows; currentRow++) {
	    try {
		// �p�G���o�� row �O null, �N��쩳�F, �����B�z�o�� Excel
		XSSFRow row = sheet.getRow(currentRow);
		if (row == null) {
		    break;
		}

		// �����X�� 0 �Ӧ�m, �p���ŭȥN��쩳�F, �����B�z�o�� Excel
		String cell0_val = ExcelUtil.getCellValue(mLogger, row, 0);
		if ("".equals(cell0_val)) {
		    break;
		}

		// �T�{����ƪ�����
		numberOfDatas++;

		// �Ҧ��v�H�C�����
		String ownerList = null;

		// ���X�n�^�������
		CppSheet1AndResultCellDef[] srcToExtractCols = CppSheet1AndResultCellDef.getSrcColsToExtract();

		// �^���ݭn������ append �� contentBuffer
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

		// ���ѩҦ��v�H�C�����
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

		// �ھڨC��Ҧ��v�H�A�N��Ƽg�J DB
		for (String owner : owners) {
		    String[] noAndName = owner.split(" ");
		    owner = noAndName.length == 2 ? noAndName[1] : owner;
		    owner = owner.trim();

		    // �B�z�b�ư��W�椺��
		    if (isInExcludePattern(owner)) {
			String strOthers = owner;

			Others others = new Others(strOthers, content);

			TOTAL_OTHERS_COUNTS++;

			OthersToDbQ.getInstance().offer(others);
		    }
		    // �B�z���q
		    else if (owner.contains(COMPANY_KEY_WORD)) {
			String strCompany = owner;

			Company company = new Company(strCompany, content);

			TOTAL_COMPANY_COUNTS++;

			CompanyToDbQ.getInstance().offer(company);
		    }
		    // �B�z�k�H
		    else if (owner.contains(LEGAL_PERSON_KEY_WORD)) {
			String strLegalPerson = owner;

			LegalPerson legalPerson = new LegalPerson(strLegalPerson, content);

			TOTAL_LEGAL_PERSON_COUNTS++;

			LegalPersonToDbQ.getInstance().offer(legalPerson);
		    }
		    // �B�z�ӤH
		    else {
			String strPersonal = owner;

			Personal personal = new Personal(strPersonal, content);

			TOTAL_PERSONAL_COUNTS++;

			PersonalToDbQ.getInstance().offer(personal);
		    }
		}
	    }
	    catch (Exception e) {
		String errorMsg = "���D�o�ͩ��" + currentRow + "�C";
		throw new ExcelParserException(errorMsg, e);
	    }
	}
	mLogger.info("Excel �@�� " + numberOfDatas + " ��");
    }

    private boolean isInExcludePattern(String owner) {
	Iterator<String> itExcludePattern = SPECIAL_PATTERN.iterator();
	while (itExcludePattern.hasNext()) {
	    String excludePattern = itExcludePattern.next();
	    // KeyPoint �o��� equals, �]���Y�� contains, ���ǥu�O�]�t�N�Q�o��
	    if (owner.equals(excludePattern)) {
		// logger.info(owner + " �Q�o��");
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
	    mLogger.info("���G���|���s�b...");
	    resultFolder.mkdirs();
	    mLogger.info("���ͥؿ�����");
	    mLogger.info(StrConstant.SEPRATE_LINE);
	}

	mLogger.info("�}�l���͵��G�ɮ�...");
	long totalStartTime = System.currentTimeMillis();

	// ���ͤ��q���
	if (mCompaniesData.size() > 0) {
	    long startTime = System.currentTimeMillis();

	    String outputFilePath = resultFolderPath + COMPANY_FILE_NAME;

	    mLogger.info("���ͤ��q��� " + outputFilePath + "...");

	    createResultFile(mCompaniesData, outputFilePath, COMPANY_SHEET_NAME, OwnerClassify.COMPANY, true);

	    mLogger.info("���ͤ��q��� " + outputFilePath + " �����A�@��F: <" + (System.currentTimeMillis() - startTime) / 1000 + " ��>");
	}
	else {
	    mLogger.warn("���R���G�A���q��Ƭ� 0�A�N�����ͤ��q���");
	}

	// ���ͪk�H���
	if (mLegalPersonData.size() > 0) {
	    long startTime = System.currentTimeMillis();

	    String outputFilePath = resultFolderPath + LEGAL_PERSON_FILE_NAME;

	    mLogger.info("���ͪk�H��� " + outputFilePath + "...");

	    createResultFile(mLegalPersonData, outputFilePath, LEGAL_PERSON_SHEET_NAME, OwnerClassify.LEGAL_PERSON, true);

	    mLogger.info("���ͪk�H��� " + outputFilePath + " �����A�@��F: <" + (System.currentTimeMillis() - startTime) / 1000 + " ��>");
	}
	else {
	    mLogger.warn("���R���G�A�k�H��Ƭ� 0�A�N�����ͤ��q���");
	}

	// ���ͭӤH���
	if (mPersonalData.size() > 0) {
	    long startTime = System.currentTimeMillis();

	    String outputFilePath = resultFolderPath + PERSONAL_FILE_NAME;

	    mLogger.info("���ͭӤH��� " + outputFilePath + "...");

	    createResultFile(mPersonalData, outputFilePath, PERSONAL_SHEET_NAME, OwnerClassify.PERSONAL, true);

	    mLogger.info("���ͭӤH��� " + outputFilePath + " �����A�@��F: <" + (System.currentTimeMillis() - startTime) / 1000 + " ��>");
	}
	else {
	    mLogger.warn("���R���G�A�ӤH��Ƭ� 0�A�N�����ͤ��q���");
	}

	mLogger.info("���͵��G�ɮ׵���, �@��F: <" + (System.currentTimeMillis() - totalStartTime) / 1000 + " ��>");
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

	    // ���� Title Cell Style
	    XSSFCellStyle titleCellStyle = ExcelUtil.getTitleColCellStyle(workbook);

	    // ���� sheet
	    Sheet sheet = workbook.createSheet(sheetName);

	    // �ھڬO�_�]�t�������A�h���o���P���}�C
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

	    // �N cache ������Ʋ��ͦ� Excel�A���[��t�@�� vo ���F����ƶq�̦h���ƧǦb�W��
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

		    // ���N�e�����R�X�Ӫ���ƶ�i row
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

	    // ���͵��G Excel
	    workbook.write(fos);
	    fos.flush();
	}
	catch (IOException e) {
	    mLogger.error("����" + outputFilePath + "����, IOException raised, msg: <" + e.getMessage() + ">", e);
	}
	catch (Exception e) {
	    mLogger.error("����" + outputFilePath + "����, content: <" + contentForExceptionLog + ">, Exception raised", e);
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

	mLogger.info("���\�M���O������");
    }

    public static void main(String[] args) {
	CompanyPersonalParser companyPersonalParser = new CompanyPersonalParser();

	companyPersonalParser.init();

	Scanner scanIn = new Scanner(System.in);

	while (true) {
	    // -------------- ��J�n parse ����Ƨ� --------------
	    String inputFolderPath = "";
	    while ("".equals(inputFolderPath)) {
		System.out.println("�п�J��Ƨ����|: ");
		inputFolderPath = scanIn.nextLine();

		File folder = new File(inputFolderPath);
		if (!folder.isDirectory()) {
		    System.err.println("�п�J���T��Ƨ����|...");
		    inputFolderPath = "";
		}
	    }
	    System.out.println("\n��Ƨ����|��:\n" + inputFolderPath);

	    // -------------- �߰ݬO�n���� Excel �ο�J�� DB
	    String inputCreateExcelOrInsertDb = "";
	    while (!inputCreateExcelOrInsertDb.equals("0") && !inputCreateExcelOrInsertDb.equals("1")) {
		System.out.println("\n�п�ܭn�N��Ʋ��� Excel(0) �� ��J�� DB (1): ");
		inputCreateExcelOrInsertDb = scanIn.nextLine();
		if (!inputCreateExcelOrInsertDb.equals("0") && !inputCreateExcelOrInsertDb.equals("1")) {
		    System.err.println("�п�J 0 �� 1");
		}
	    }

	    OutputWay outputWay = inputCreateExcelOrInsertDb.equals("0") ? OutputWay.CREATE_EXCEL : OutputWay.INSERT_DB;

	    String inputResultFolderPath = "";
	    if (inputCreateExcelOrInsertDb.equals("0")) {
		// -------------- ��J���G���͸�Ƨ� --------------
		while ("".equals(inputResultFolderPath)) {
		    System.out.println("\n�п�J���G���͸��|: ");
		    inputResultFolderPath = scanIn.nextLine();

		    if (!inputResultFolderPath.contains(":\\") && !inputResultFolderPath.contains(":/")) {
			System.err.println("�п�J���T��Ƨ����|...");
			inputResultFolderPath = "";
		    }
		}
		System.out.println("\n���G�N���ͩ�: " + inputResultFolderPath);
	    }

	    // -------------- �}�l parse �ò��͵��G --------------
	    companyPersonalParser.start(inputFolderPath, outputWay, inputResultFolderPath);

	    // �p�G����J DB�A����C�ӳB�z���G
	    if (outputWay == OutputWay.INSERT_DB) {
		try {
		    String legalPersonProcessStatus = LegalPersonProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("�k�H��ƶ�J DB �B�z���G: <" + legalPersonProcessStatus + ">");

		    String companyProcessStatus = CompanyProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("���q��ƶ�J DB �B�z���G: <" + companyProcessStatus + ">");

		    String othersProcessStatus = OthersProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("��L��ƶ�J DB �B�z���G: <" + othersProcessStatus + ">");

		    String personalProcessStatus = PersonalProcessStatusQ.getInstance().getProcessStatus();
		    System.out.println("�ӤH��ƶ�J DB �B�z���G: <" + personalProcessStatus + ">");
		}
		catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	    // -------------- �߰ݬO�_�~�� --------------
	    String inputContinueOrNot = "";
	    while ("".equals(inputContinueOrNot)) {
		System.out.println("�O�_�~��(y/n)?");
		inputContinueOrNot = scanIn.nextLine();

		if (inputContinueOrNot.compareToIgnoreCase("y") != 0 && inputContinueOrNot.compareToIgnoreCase("n") != 0) {
		    System.err.println("�п�J y/n");
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
     * �αƧǥ�
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