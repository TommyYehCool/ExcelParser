package com.dreamer.excelextractor.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.dreamer.common.util.ExcelUtil;

public class ExcelExtractor {
    private final int INDEX_CONTACT_WAY = 5;
    
    private final String MOBILE_NO_PATTERN = "[0][9][0-9]{2}-{0,1}[0-9]{3}-{0,1}[0-9]{3}";
    
    private final String RESULT_XLS_NAME = "����q�ܵ��G.xlsx";
    
    private final String SHEET_NAME = "��ʸ��X";
    
    private ArrayList<String> phoneNos = new ArrayList<String>();
    
    public void start(String srcFilePath, String resultFolderPath) {
	parseExcel(srcFilePath);
	
	createResultExcel(resultFolderPath);
    }

    private void parseExcel(String srcFilePathToProcess) {
        File excelFile = new File(srcFilePathToProcess);
        
        FileInputStream fis = null;
        XSSFWorkbook workbook = null;
        try {
            fis = new FileInputStream(excelFile);
    
            workbook = new XSSFWorkbook(fis);
            
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            int numberOfRows = sheet.getPhysicalNumberOfRows();
            
            for (int currentRow = 2; currentRow < numberOfRows; currentRow++) {
        	XSSFRow row = sheet.getRow(currentRow);
        	if (row == null) {
        	    break;
        	}
        	
        	String cellValue = ExcelUtil.getCellValue(null, row, INDEX_CONTACT_WAY);
        	if (cellValue.matches(MOBILE_NO_PATTERN)) {
        	    phoneNos.add(cellValue);
        	}
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createResultExcel(String resultFolderPath) {
	// ���͵��G�ؿ�
	if (!resultFolderPath.endsWith("\\")) {
	    resultFolderPath += "\\";
	}
	File resultFolder = new File(resultFolderPath);
	if (!resultFolder.isDirectory()) {
	    System.out.println("���G���|���s�b...");
	    resultFolder.mkdirs();
	    System.out.println("���ͥؿ�: " + resultFolderPath + " ����");
	}
	
	// ���X���G�ɮ׸��|
	String outputFilePath = resultFolderPath + RESULT_XLS_NAME;
	
	System.out.println("�ǳƲ��� " + outputFilePath + "...");
	
	// �ŧi�ǳƲ��ͪ��ɮ�
	File resultFile = new File(outputFilePath);
	FileOutputStream fos = null;
	Workbook outputWorkbook = null;
	
	try {
	    fos = new FileOutputStream(resultFile);
	    
	    outputWorkbook = new SXSSFWorkbook();
	    
	    Sheet sheet = outputWorkbook.createSheet(SHEET_NAME);
	    
	    for (int rowIndex = 0; rowIndex < phoneNos.size(); rowIndex++) {
		Row row = sheet.createRow(rowIndex);
		
		Cell cell = row.createCell(0);
		
		cell.setCellValue(phoneNos.get(rowIndex));
	    }
	    
	    outputWorkbook.write(fos);
	    fos.flush();
	    
	    System.out.println("���� " + outputFilePath + " ����");
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	finally {
	    if (fos != null) {
		try {
		    fos.close();
		    fos = null;
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	    if (outputWorkbook != null) {
		try {
		    outputWorkbook.close();
		    outputWorkbook = null;
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    public static void main(String[] args) {
	Scanner scanIn = new Scanner(System.in);

	System.out.println("�п�J�n�B�z�� Excel ���|:");
	String inputExcelPathToProcess = "";
	while ("".equals(inputExcelPathToProcess)) {
	    inputExcelPathToProcess = scanIn.nextLine();

	    File excelToProcess = new File(inputExcelPathToProcess);
	    if (!excelToProcess.isFile() || !excelToProcess.exists()) {
		System.err.println("�п�J���T�ɮ׸��|...");
		inputExcelPathToProcess = "";
		continue;
	    }
	    
	    if (excelToProcess.length() == 0) {
		System.err.println("�z�n�B�̪��ɮ׬��Ū�...");
		inputExcelPathToProcess = "";
		continue;
	    }

	    if (!inputExcelPathToProcess.endsWith("xls")
		    && !inputExcelPathToProcess.endsWith("xlsx")) {
		System.err.println("�нT�{��J���|�� xls �� xlsx..");
		inputExcelPathToProcess = "";
		continue;
	    }
	}
	
	System.out.println("�п�J���G Excel ���͸�Ƨ�:");
	String inputResultFolderPath = "";
	while ("".equals(inputResultFolderPath)) {
	    inputResultFolderPath = scanIn.nextLine();
	    
	    File resultFolderPath = new File(inputResultFolderPath);
	    if (resultFolderPath.isFile()) {
		System.err.println("�п�J���T���͸�Ƨ����|...");
		inputResultFolderPath = "";
		continue;
	    }
	    
	    if (!inputResultFolderPath.contains("/") && !inputResultFolderPath.contains("\\")) {
		System.err.println("�п�J���T���͸�Ƨ����|...");
		inputResultFolderPath = "";
		continue;
	    }
	}
	
	new ExcelExtractor().start(inputExcelPathToProcess, inputResultFolderPath);

	scanIn.close();
    }
}
