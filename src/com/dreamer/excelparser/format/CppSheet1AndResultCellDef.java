package com.dreamer.excelparser.format;

import java.util.ArrayList;

/**
 * 定義要抓 Sheet 1 跟產生結果的欄位
 * 
 * @author TommyYeh
 */
public enum CppSheet1AndResultCellDef {
    /**
     * 所有權人列表 (Src)(Result)
     */
    OWNER_LIST			(10, 	0,	0, "所有權人列表", -1, false, 35),
    /**
     * 縣市 (Src)(Result)
     */
    COUNTIES			(0, 	1, 	1, "縣市", 	0, false, 12),
    /**
     * 鄉鎮市 (Src)(Result)
     */
    TOWNSHIPS			(1, 	2, 	2, "鄉鎮市", 	1, false, 12),
    /**
     * 段小段 (Src)(Result)
     */
    SEGMENT			(2, 	3, 	3, "段小段", 	2, false, 27),
    /**
     * 地號 (Src)(Result_Hidden)
     */
    LAND_NO			(3, 	4,     -1, "地號", 	3, false, 10),
    /**
     * 當期公告現值 (Src)(Result)
     */
    LAND_PRICE			(4, 	5, 	4, "當期公告現值", 	4, true, 15),
    /**
     * 面積 (Src)(Result_Hidden)
     */
    AREA			(5,	6,     -1, "面積", 	5, true, 8),
    /**
     * 土地使用分區 (Src)(Result)
     */
    LAND_USED_PARTITON		(6, 	7, 	5, "土地使用分區", 	6, false, 30),
    /**
     * 地上建物棟數 (Src)(Result)
     */
    NUMBERS_OF_LAND_BUILDING	(7, 	8, 	6, "地上建物棟數", 	7, true, 15),
    /**
     * 私法人 (Src)(Result)
     */
    PRIVATE_LEGAL_PERSON	(8, 	9, 	7, "私法人", 	8, true, 10),
    /**
     * 自然人 (Src)(Result)
     */
    NATURAL_PERSON		(9,    10, 	8, "自然人", 	9, true, 10);

    private int srcExcelIndex;
    private int resultExcel_WithHidden_CellIndex;
    private int resultExcel_WithoutHidden_CellIndex;
    /**
     * 產生結果的 Title 名稱
     */
    private String titleName;
    /**
     * BranchPersonalParser 的資料存放陣列位置
     */
    private int bppContentsArrayIndex;
    /**
     * 是否為數字
     */
    private boolean isNumber;
    /**
     * 欄位寬度
     */
    private int columnWidth;

    private CppSheet1AndResultCellDef(
	    int srcExcelIndex, 
	    int resultExcel_WithHidden_CellIndex, 
	    int resultExcel_WithoutHidden_CellIndex,

	    String titleName, 
	    int bppContentsArrayIndex, 
	    boolean isNumber, 
	    int columnWidth) {
	this.srcExcelIndex = srcExcelIndex;
	this.resultExcel_WithHidden_CellIndex = resultExcel_WithHidden_CellIndex;
	this.resultExcel_WithoutHidden_CellIndex = resultExcel_WithoutHidden_CellIndex;

	this.titleName = titleName;
	this.bppContentsArrayIndex = bppContentsArrayIndex;
	this.isNumber = isNumber;
	this.columnWidth = columnWidth;
    }

    public int getSrcCellIndex() {
	return this.srcExcelIndex;
    }

    public int getResultWithHiddenCellIndex() {
	return this.resultExcel_WithHidden_CellIndex;
    }

    public int getResultWithoutHiddenCellIndex() {
	return this.resultExcel_WithoutHidden_CellIndex;
    }

    public String getTitleName() {
	return this.titleName;
    }

    public int getBppContentArrayIndex() {
	return this.bppContentsArrayIndex;
    }

    public boolean isNumber() {
	return this.isNumber;
    }
    
    public int getColumnWidth() {
	return this.columnWidth;
    }

    public static CppSheet1AndResultCellDef[] getSrcColsToExtract() {
	ArrayList<CppSheet1AndResultCellDef> results = new ArrayList<CppSheet1AndResultCellDef>();

	results.add(OWNER_LIST);
	results.add(COUNTIES);
	results.add(TOWNSHIPS);
	results.add(SEGMENT);
	results.add(LAND_NO);

	results.add(LAND_PRICE);
	results.add(AREA);
	results.add(LAND_USED_PARTITON);
	results.add(NUMBERS_OF_LAND_BUILDING);
	results.add(PRIVATE_LEGAL_PERSON);

	results.add(NATURAL_PERSON);

	return results.toArray(new CppSheet1AndResultCellDef[0]);
    }

    public static CppSheet1AndResultCellDef[] getResultColsIncludeHidden() {
	ArrayList<CppSheet1AndResultCellDef> results = new ArrayList<CppSheet1AndResultCellDef>();

	results.add(OWNER_LIST);
	results.add(COUNTIES);
	results.add(TOWNSHIPS);
	results.add(SEGMENT);
	results.add(LAND_NO);

	results.add(LAND_PRICE);
	results.add(AREA);
	results.add(LAND_USED_PARTITON);
	results.add(NUMBERS_OF_LAND_BUILDING);
	results.add(PRIVATE_LEGAL_PERSON);

	results.add(NATURAL_PERSON);

	return results.toArray(new CppSheet1AndResultCellDef[0]);
    }

    public static CppSheet1AndResultCellDef[] getResultColsWithoutHidden() {
	ArrayList<CppSheet1AndResultCellDef> results = new ArrayList<CppSheet1AndResultCellDef>();

	results.add(OWNER_LIST);
	results.add(COUNTIES);
	results.add(TOWNSHIPS);
	results.add(SEGMENT);
	results.add(LAND_PRICE);

	results.add(LAND_USED_PARTITON);
	results.add(NUMBERS_OF_LAND_BUILDING);
	results.add(PRIVATE_LEGAL_PERSON);
	results.add(NATURAL_PERSON);

	return results.toArray(new CppSheet1AndResultCellDef[0]);
    }
}