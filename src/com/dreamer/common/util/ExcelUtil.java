package com.dreamer.common.util;

import java.awt.Color;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class ExcelUtil {
    public static String getCellValue(Logger logger, XSSFRow row, int index) {
	String value = "";

	XSSFCell cell = row.getCell(index);
	if (cell != null) {
	    int cellType = cell.getCellType();
	    switch (cellType) {
	    case Cell.CELL_TYPE_BLANK:
		value = "";
		break;

	    case Cell.CELL_TYPE_NUMERIC:
		double numericCellValue = cell.getNumericCellValue();
		value = String.valueOf(numericCellValue);
		break;

	    case Cell.CELL_TYPE_STRING:
		value = cell.getStringCellValue().trim();
		break;

	    default:
		if (logger != null) {
		    logger.error("Unexpected cell type, cellType: " + cellType + ">");
		}
		break;
	    }
	}
	return value;
    }

    public static XSSFCellStyle getTitleColCellStyle(Workbook workbook) {
	XSSFCellStyle titleCellStyle = (XSSFCellStyle) workbook.createCellStyle();

	titleCellStyle.setFillForegroundColor(new XSSFColor(new Color(198, 255, 241)));

	titleCellStyle.setAlignment(HorizontalAlignment.CENTER);

	titleCellStyle.setBorderLeft(BorderStyle.MEDIUM);
	titleCellStyle.setBorderTop(BorderStyle.MEDIUM);
	titleCellStyle.setBorderRight(BorderStyle.MEDIUM);
	titleCellStyle.setBorderBottom(BorderStyle.MEDIUM);

	titleCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

	return titleCellStyle;
    }
}
