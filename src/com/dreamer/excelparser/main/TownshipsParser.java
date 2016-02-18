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

	mLogger.info("嘗試取得 " + folderPath + " 底下所有資料夾...");

	ArrayList<File> listAllFolders = getAllFolder(folder);
	mAllFoldersToProcess = listAllFolders.toArray(new File[0]);

	if (mAllFoldersToProcess.length != 0) {
	    mLogger.info("共有 " + mAllFoldersToProcess.length + " 個資料夾要處理");

	    long startTime = System.currentTimeMillis();

	    analysisFolderAndGenerateResult(resultFolderPath);

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("全部處理結束, 共花了: <" + (System.currentTimeMillis() - startTime) / 1000 + " 秒>");
	    mLogger.info(StrConstant.SEPRATE_LINE);
	}
	else {
	    mLogger.error(StrConstant.SEPRATE_LINE);
	    mLogger.error("請確認資料夾底下結構為: 根目錄\\XX市XX區\\xxx.xls，且至少存在一個資料夾，裡面存在一個 Excel");
	    mLogger.error(StrConstant.SEPRATE_LINE);
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
			    mLogger.warn("資料夾: " + subFolderPath + " 不存在任何 Excel 檔案, 不處理...");
			}
			else {
			    if (mLogger.isDebugEnabled()) {
				mLogger.debug("資料夾: " + subFolderPath);
			    }
			    listAllFolders.add(subFolder);
			}
		    }
		    else {
			mLogger.warn("根目錄下發現未分區檔案: " + subFolderPath + " 不處理...");
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
	    // 擷取出子目錄的名稱，並決定要產生結果檔案的名稱
	    String subFolderName = subFolder.getName();
	    subFolderName = subFolderName.contains("(") ? subFolderName.substring(0, subFolderName.indexOf("(")).trim() : subFolderName.trim();

	    // 取得子目錄下所有檔案來處理
	    File[] excelFiles = subFolder.listFiles();
	    int excelFilesLength = excelFiles.length;

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info(subFolderName + "，底下共有 " + excelFilesLength + " 個檔案要處理...");

	    // 整合目錄底下所有 Excel 需要的資料
	    ArrayList<String> excelFilesContent = getExcelFilesContent(subFolderName, excelFiles, excelFilesLength);

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    int contentsSize = excelFilesContent.size();
	    mLogger.info(subFolderName + "，資料筆數共 " + contentsSize + " 筆");

	    if (contentsSize != 0) {
		// 產生包含隱藏欄位結果檔案
		String withHiddenFileName = subFolderName + XLSX_EXTENSION_NAME;
		createResultFile(excelFilesContent, resultFolderPath, withHiddenFileName, subFolderName, true);
	    }
	    else {
		mLogger.warn("解析完 " + subFolderName + " 完全沒有資料, 跳過...");
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
		mLogger.error("取得檔案名稱發生 IOException", e);
		continue;
	    }

	    if (!filePath.endsWith("xls") && !filePath.endsWith("xlsx")) {
		mLogger.warn("此檔案 " + filePath + " 不為 Excel, 跳過...");
		continue;
	    }

	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("開始分析，第 " + (i + 1) + " 個檔案：" + filePath);

	    FileInputStream fis = null;
	    XSSFWorkbook workbook = null;
	    try {
		fis = new FileInputStream(excelFile);

		workbook = new XSSFWorkbook(fis);

		XSSFSheet sheet = workbook.getSheetAt(0);

		parseSheet1(subFolderName, filePath, sheet, allContentsToWrite);
	    }
	    catch (IOException e) {
		mLogger.error("讀取檔案: " + filePath + " 發生錯誤", e);
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
	return allContentsToWrite;
    }

    private void parseSheet1(String subFolderName, String filePath, XSSFSheet sheet, ArrayList<String> allContentsToWrite)
	    throws ExcelParserException {
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

		String townships = ExcelUtil.getCellValue(mLogger, row, TspSheet1AndResultDef.TOWNSHIPS.getSrcCellIndex());
		townships = townships.contains("(") ? townships.substring(0, townships.indexOf("(")).trim() : townships.trim();
		if (!subFolderName.contains(townships)) {
		    mLogger.warn("資料夾: <" + subFolderName + "> 之檔案: <" + filePath + ">, 發現: <" + townships + ">, 跳過...");
		    continue;
		}

		// 取出所有要擷取的欄位
		TspSheet1AndResultDef[] srcToExtractCols = TspSheet1AndResultDef.getSrcColsToExtract();

		// 擷取需要的欄位並 append 到 contentBuffer
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
		String errorMsg = "問題發生於第" + currentRow + "列";
		throw new ExcelParserException(errorMsg, e);
	    }
	}
	mLogger.info("全部資料共 " + numberOfDatas + " 筆");
    }

    private void createResultFile(ArrayList<String> excelFilesContent, String resultFolderPath, String resultXlsName, String sheetName,
	    boolean includeHidden) {
	// 產生結果目錄
	if (!resultFolderPath.endsWith("\\")) {
	    resultFolderPath += "\\";
	}
	File resultFolder = new File(resultFolderPath);
	if (!resultFolder.isDirectory()) {
	    mLogger.info(StrConstant.SEPRATE_LINE);
	    mLogger.info("結果路徑不存在...");
	    resultFolder.mkdirs();
	    mLogger.info("產生目錄: " + resultFolderPath + " 完成");
	}

	// 拼出結果檔案路徑
	String outputFilePath = resultFolderPath + resultXlsName;

	mLogger.info(StrConstant.SEPRATE_LINE);
	mLogger.info("準備產生 " + outputFilePath + "...");

	// 宣告準備產生的檔案
	File resultFile = new File(outputFilePath);
	FileOutputStream fos = null;
	Workbook outputWorkbook = null;

	String contentForExceptionLog = null;

	try {
	    fos = new FileOutputStream(resultFile);

	    outputWorkbook = new SXSSFWorkbook();

	    // 產生 Title Cell Style
	    XSSFCellStyle titleCellStyle = ExcelUtil.getTitleColCellStyle(outputWorkbook);

	    // 產生 sheet
	    Sheet sheet = outputWorkbook.createSheet(sheetName);

	    // 根據是否包含隱藏欄位，去取得不同的陣列
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

	    // 將 cache 中的資料產生成 Excel
	    for (String content : excelFilesContent) {
		contentForExceptionLog = content;

		String[] splitResults = content.split("\t");

		Row dataRow = sheet.createRow(rowIndex++);

		// 先將前面分析出來的資料塞進 row
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

	    // 產生結果 Excel
	    outputWorkbook.write(fos);
	    fos.flush();

	    mLogger.info("產生 " + outputFilePath + " 完成");
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

	    // -------------- 輸入結果產生資料夾 --------------
	    String inputResultFolderPath = "";
	    while ("".equals(inputResultFolderPath)) {
		System.out.println("\n請輸入結果產生路徑: ");
		inputResultFolderPath = scanIn.nextLine();

		if (!inputResultFolderPath.contains(":\\") && !inputResultFolderPath.contains(":/")) {
		    System.err.println("請輸入正確資料夾路徑...");
		    inputResultFolderPath = "";
		}
	    }
	    System.out.println("\n結果將產生於: " + inputResultFolderPath);

	    // -------------- 開始 parse 並產生結果 --------------
	    townshipsParser.start(inputFolderPath, inputResultFolderPath);

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
	}
	scanIn.close();
    }
}