package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class TestWriteExcel {
    private void start() {
	FileOutputStream fos = null;
	HSSFWorkbook workbook = null;

	String outputFilePath = "C:/Temp/TestExcel.xls";
	File file = new File(outputFilePath);

	try {
	    fos = new FileOutputStream(file);
	    workbook = new HSSFWorkbook();

	    HSSFSheet sheet = workbook.createSheet();

	    for (int rownum = 0; rownum < 10; rownum++) {
		HSSFRow row = sheet.createRow(rownum);
		HSSFCell cell = row.createCell(0);

		String testStr = "TestStr_" + rownum;

		cell.setCellValue(testStr);
	    }

	    workbook.write(fos);
	    fos.flush();
	}
	catch (IOException e) {
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
	}
    }

    public static void main(String[] args) {
	new TestWriteExcel().start();
    }
}