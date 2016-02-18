package com.dreamer.excelparser.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dreamer.common.constant.Log4jSetting;
import com.dreamer.common.constant.StrConstant;
import com.dreamer.common.util.ExcelUtil;
import com.dreamer.excelparser.config.TspConfigLoader;
import com.dreamer.excelparser.exception.ExcelParserException;
import com.dreamer.excelparser.format.AdditionalResultDef;
import com.dreamer.excelparser.format.TspSheet1AndResultDef;

public class TownshipsParser {
    // ------------- Logger -------------
    private Logger mLogger = Logger.getLogger(Log4jSetting.TOWNSHIPS_PARSER);

    // ------------- Running needed -------------
    private TspConfigLoader mTspConfigLoader = TspConfigLoader.getInstance();

    // ------------- Input -------------
    private File[] mAllFoldersToProcess;

    // ------------- Output -------------
    private final String XLSX_EXTENSION_NAME = ".xlsx";

    private void init() {
	mTspConfigLoader.init();
    }

    private void start(String folderPath, String resultFolderPath) {
	File folder = new File(folderPath);

	checkInputFolder(folderPath, folder);

	mLogger.info("���ը��o " + folderPath + " ���U�Ҧ���Ƨ�...");

	ArrayList<File> listAllFolders = getAllFolder(folder);
	mAllFoldersToProcess = listAllFolders.toArray(new File[0]);

	if (mAllFoldersToProcess.length != 0) {
	    mLogger.info("�@�� " + mAllFoldersToProcess.length + " �Ӹ�Ƨ��n�B�z");

	    long startTime = System.currentTimeMillis();

	    analysisFolderAndGenerateResult(resultFolderPath);

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("�����B�z����, �@��F: <" + (System.currentTimeMillis() - startTime) / 1000 + " ��>");
	    mLogger.info(StrConstant.SEPRATE_LINE);
	}
	else {
	    mLogger.error(StrConstant.SEPRATE_LINE);
	    mLogger.error("�нT�{��Ƨ����U���c��: �ڥؿ�\\XX��XX��\\xxx.xls�A�B�ܤ֦s�b�@�Ӹ�Ƨ��A�̭��s�b�@�� Excel");
	    mLogger.error(StrConstant.SEPRATE_LINE);
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

    private ArrayList<File> getAllFolder(File rootFolder) {
	ArrayList<File> listAllFolders = new ArrayList<File>();
	File[] subFolders = rootFolder.listFiles();
	if (subFolders != null && subFolders.length != 0) {
	    for (File subFolder : subFolders) {
		try {
		    String subFolderPath = subFolder.getCanonicalPath();
		    if (subFolder.isDirectory()) {
			int excelCounts = getExcelFilesCounts(subFolder);
			if (excelCounts == 0) {
			    mLogger.warn("��Ƨ�: " + subFolderPath + " ���s�b���� Excel �ɮ�, ���B�z...");
			}
			else {
			    if (mLogger.isDebugEnabled()) {
				mLogger.debug("��Ƨ�: " + subFolderPath);
			    }
			    listAllFolders.add(subFolder);
			}
		    }
		    else {
			mLogger.warn("�ڥؿ��U�o�{�������ɮ�: " + subFolderPath + " ���B�z...");
		    }
		}
		catch (IOException e) {
		    mLogger.error("IOException raised while get file path", e);
		}
	    }
	}
	return listAllFolders;
    }

    private int getExcelFilesCounts(File subFolder) {
	int counts = 0;
	File[] subFolderFiles = subFolder.listFiles();
	for (File subFolderFile : subFolderFiles) {
	    try {
		String filePath = subFolderFile.getCanonicalPath();
		if (filePath.endsWith("xls") || filePath.endsWith("xlsx")) {
		    counts++;
		}
	    }
	    catch (IOException e) {
		mLogger.error("IOException raised while getting file canonical path at get excel files counts", e);
	    }
	}
	return counts;
    }

    private void analysisFolderAndGenerateResult(String resultFolderPath) {
	for (File subFolder : mAllFoldersToProcess) {
	    // �^���X�l�ؿ����W�١A�èM�w�n���͵��G�ɮת��W��
	    String subFolderName = subFolder.getName();
	    subFolderName = subFolderName.contains("(") ? subFolderName.substring(0, subFolderName.indexOf("(")).trim() : subFolderName.trim();

	    // ���o�l�ؿ��U�Ҧ��ɮרӳB�z
	    File[] excelFiles = subFolder.listFiles();
	    int excelFilesLength = excelFiles.length;

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info(subFolderName + "�A���U�@�� " + excelFilesLength + " ���ɮ׭n�B�z...");

	    // ��X�ؿ����U�Ҧ� Excel �ݭn�����
	    ArrayList<String> excelFilesContent = getExcelFilesContent(subFolderName, excelFiles, excelFilesLength);

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    int contentsSize = excelFilesContent.size();
	    mLogger.info(subFolderName + "�A��Ƶ��Ʀ@ " + contentsSize + " ��");

	    if (contentsSize != 0) {
		// ���ͥ]�t������쵲�G�ɮ�
		String withHiddenFileName = subFolderName + XLSX_EXTENSION_NAME;
		createResultFile(excelFilesContent, resultFolderPath, withHiddenFileName, subFolderName, true);
	    }
	    else {
		mLogger.warn("�ѪR�� " + subFolderName + " �����S�����, ���L...");
	    }
	}
    }

    private ArrayList<String> getExcelFilesContent(String subFolderName, File[] excelFiles, int excelFilesLength) {
	ArrayList<String> allContentsToWrite = new ArrayList<String>();
	for (int i = 0; i < excelFilesLength; i++) {
	    File excelFile = excelFiles[i];

	    String filePath = null;
	    try {
		filePath = excelFile.getCanonicalPath();
	    }
	    catch (IOException e) {
		mLogger.error("���o�ɮצW�ٵo�� IOException", e);
		continue;
	    }

	    if (!filePath.endsWith("xls") && !filePath.endsWith("xlsx")) {
		mLogger.warn("���ɮ� " + filePath + " ���� Excel, ���L...");
		continue;
	    }

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("�}�l���R�A�� " + (i + 1) + " ���ɮסG" + filePath);

	    FileInputStream fis = null;
	    XSSFWorkbook workbook = null;
	    try {
		fis = new FileInputStream(excelFile);

		workbook = new XSSFWorkbook(fis);

		XSSFSheet sheet = workbook.getSheetAt(0);

		parseSheet1(subFolderName, filePath, sheet, allContentsToWrite);
	    }
	    catch (IOException e) {
		mLogger.error("Ū���ɮ�: " + filePath + " �o�Ϳ��~", e);
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
	return allContentsToWrite;
    }

    private void parseSheet1(String subFolderName, String filePath, XSSFSheet sheet, ArrayList<String> allContentsToWrite)
	    throws ExcelParserException {
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

		String townships = ExcelUtil.getCellValue(mLogger, row, TspSheet1AndResultDef.TOWNSHIPS.getSrcCellIndex());
		townships = townships.contains("(") ? townships.substring(0, townships.indexOf("(")).trim() : townships.trim();
		if (!subFolderName.contains(townships)) {
		    mLogger.warn("��Ƨ�: <" + subFolderName + "> ���ɮ�: <" + filePath + ">, �o�{: <" + townships + ">, ���L...");
		    continue;
		}

		// ���X�Ҧ��n�^�������
		TspSheet1AndResultDef[] srcToExtractCols = TspSheet1AndResultDef.getSrcColsToExtract();

		// �^���ݭn������ append �� contentBuffer
		for (TspSheet1AndResultDef sheet1DataDef : srcToExtractCols) {
		    String cellValue = ExcelUtil.getCellValue(mLogger, row, sheet1DataDef.getSrcCellIndex());
		    if (cellValue == null || "".equals(cellValue)) {
			cellValue = sheet1DataDef.isNumber() ? "0" : "-";
		    }
		    contentBuffer.append(cellValue);
		    if (sheet1DataDef != TspSheet1AndResultDef.OWNER_LIST) {
			contentBuffer.append("\t");
		    }
		}

		String content = contentBuffer.toString();

		allContentsToWrite.add(content);

		contentBuffer.setLength(0);
	    }
	    catch (Exception e) {
		String errorMsg = "���D�o�ͩ��" + currentRow + "�C";
		throw new ExcelParserException(errorMsg, e);
	    }
	}
	mLogger.info("������Ʀ@ " + numberOfDatas + " ��");
    }

    private void createResultFile(ArrayList<String> excelFilesContent, String resultFolderPath, String resultXlsName, String sheetName,
	    boolean includeHidden) {
	// ���͵��G�ؿ�
	if (!resultFolderPath.endsWith("\\")) {
	    resultFolderPath += "\\";
	}
	File resultFolder = new File(resultFolderPath);
	if (!resultFolder.isDirectory()) {
	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("���G���|���s�b...");
	    resultFolder.mkdirs();
	    mLogger.info("���ͥؿ�: " + resultFolderPath + " ����");
	}

	// ���X���G�ɮ׸��|
	String outputFilePath = resultFolderPath + resultXlsName;

	mLogger.info(StrConstant.SEPRATE_LINE);
	mLogger.info("�ǳƲ��� " + outputFilePath + "...");

	// �ŧi�ǳƲ��ͪ��ɮ�
	File resultFile = new File(outputFilePath);
	FileOutputStream fos = null;
	Workbook outputWorkbook = null;

	String contentForExceptionLog = null;

	try {
	    fos = new FileOutputStream(resultFile);

	    outputWorkbook = new SXSSFWorkbook();

	    // ���� Title Cell Style
	    XSSFCellStyle titleCellStyle = ExcelUtil.getTitleColCellStyle(outputWorkbook);

	    // ���� sheet
	    Sheet sheet = outputWorkbook.createSheet(sheetName);

	    // �ھڬO�_�]�t�������A�h���o���P���}�C
	    TspSheet1AndResultDef[] resultCells = null;
	    AdditionalResultDef[] additionalResultCells = AdditionalResultDef.values();
	    if (includeHidden) {
		resultCells = TspSheet1AndResultDef.getResultColsIncludeHidden();
	    }
	    else {
		resultCells = TspSheet1AndResultDef.getResultColsWithoutHidden();
	    }

	    int rowIndex = 0;

	    // create title
	    Row titleRow = sheet.createRow(rowIndex++);
	    for (TspSheet1AndResultDef title : resultCells) {
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

	    // �N cache ������Ʋ��ͦ� Excel
	    for (String content : excelFilesContent) {
		contentForExceptionLog = content;

		String[] splitResults = content.split("\t");

		Row dataRow = sheet.createRow(rowIndex++);

		// ���N�e�����R�X�Ӫ���ƶ�i row
		for (TspSheet1AndResultDef resultCell : resultCells) {
		    int cellIndex = includeHidden ? resultCell.getResultWithHiddenCellIndex() : resultCell.getResultWithoutHiddenCellIndex();

		    Cell cell = dataRow.createCell(cellIndex);

		    boolean isNumber = resultCell.isNumber();

		    if (!isNumber) {
			String value = splitResults[resultCell.getTspContentArrayIndex()];
			cell.setCellValue(value);
		    }
		    else {
			double value = Double.parseDouble(splitResults[resultCell.getTspContentArrayIndex()]);
			cell.setCellValue(value);
		    }
		}
	    }

	    // KeyPoint freeze the first row
	    sheet.createFreezePane(0, 1);

	    // ���͵��G Excel
	    outputWorkbook.write(fos);
	    fos.flush();

	    mLogger.info("���� " + outputFilePath + " ����");
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
	    if (outputWorkbook != null) {
		try {
		    outputWorkbook.close();
		    outputWorkbook = null;
		}
		catch (IOException e) {
		    mLogger.error("IOException raised while closing outputWorkbook", e);
		}
	    }
	}
    }

    public static void main(String[] args) {
	TownshipsParser townshipsParser = new TownshipsParser();

	townshipsParser.init();

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

	    // -------------- ��J���G���͸�Ƨ� --------------
	    String inputResultFolderPath = "";
	    while ("".equals(inputResultFolderPath)) {
		System.out.println("\n�п�J���G���͸��|: ");
		inputResultFolderPath = scanIn.nextLine();

		if (!inputResultFolderPath.contains(":\\") && !inputResultFolderPath.contains(":/")) {
		    System.err.println("�п�J���T��Ƨ����|...");
		    inputResultFolderPath = "";
		}
	    }
	    System.out.println("\n���G�N���ͩ�: " + inputResultFolderPath);

	    // -------------- �}�l parse �ò��͵��G --------------
	    townshipsParser.start(inputFolderPath, inputResultFolderPath);

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
	}
	scanIn.close();
    }
}