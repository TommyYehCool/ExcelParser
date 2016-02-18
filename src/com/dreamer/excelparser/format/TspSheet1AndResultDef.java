package com.dreamer.excelparser.format;

import java.util.ArrayList;

public enum TspSheet1AndResultDef {
    /**
     * 縣市 (Src)(Result)
     */
    COUNTIES			(0, 0, 0, "縣市", 0, false),
    /**
     * 鄉鎮市 (Src)(Result)
     */
    TOWNSHIPS			(1, 	1, 	1, "鄉鎮市",	1, false),
    /**
     * 段小段 (Src)(Result)
     */
    SEGMENT			(2, 	2, 	2, "段小段", 	2, false),
    /**
     * 地號 (Src)(Result_Hidden)
     */
    LAND_NO			(3, 	3,     -1, "地號", 	3, false),
    /**
     * 當期公告現值 (Src)(Result)
     */
    LAND_PRICE			(4, 	4, 	3, "當期公告現值", 	4, true),
    /**
     * 面積 (Src)(Result_Hidden)
     */
    AREA			(6, 	5,     -1, "面積", 	5, true),
    /**
     * 土地使用分區 (Src)(Result)
     */
    LAND_USED_PARTITON		(7, 	6, 	4, "土地使用分區", 	6, false),
    /**
     * 地上建物棟數 (Src)(Result)
     */
    NUMBERS_OF_LAND_BUILDING	(15, 	7, 	5, "地上建物棟數", 	7, true),
    /**
     * 私法人 (Src)(Result)
     */
    PRIVATE_LEGAL_PERSON	(9,	8, 	6, "私法人", 	8, true),
    /**
     * 自然人 (Src)(Result)
     */
    NATURAL_PERSON		(10, 	9, 	7, "自然人", 	9, true),
    /**
     * 所有權人列表 (Src)(Result)
     */
    OWNER_LIST			(18,   10, 	8, "所有權人列表", 10, false);

    private int srcExcelIndex;
    private int resultExcel_WithHidden_CellIndex;
    private int resultExcel_WithoutHidden_CellIndex;
    /**
     * 產生結果的 Title 名稱
     */
    private String titleName;
    /**
     * TownshipsParser 的資料存放陣列位置
     */
    private int tspContentsArrayIndex;
    /**
     * 是否為數字
     */
    private boolean isNumber;

    private TspSheet1AndResultDef(int srcExcelIndex, int resultExcel_WithHidden_CellIndex, int resultExcel_WithoutHidden_CellIndex, String titleName,
	    int tspContentsArrayIndex, boolean isNumber) {
	this.srcExcelIndex = srcExcelIndex;
	this.resultExcel_WithHidden_CellIndex = resultExcel_WithHidden_CellIndex;
	this.resultExcel_WithoutHidden_CellIndex = resultExcel_WithoutHidden_CellIndex;

	this.titleName = titleName;
	this.tspContentsArrayIndex = tspContentsArrayIndex;
	this.isNumber = isNumber;
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

    public int getTspContentArrayIndex() {
	return this.tspContentsArrayIndex;
    }

    public boolean isNumber() {
	return this.isNumber;
    }

    public static TspSheet1AndResultDef[] getSrcColsToExtract() {
	ArrayList<TspSheet1AndResultDef> results = new ArrayList<TspSheet1AndResultDef>();

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

	results.add(OWNER_LIST);

	return results.toArray(new TspSheet1AndResultDef[0]);
    }

    public static TspSheet1AndResultDef[] getResultColsIncludeHidden() {
	ArrayList<TspSheet1AndResultDef> results = new ArrayList<TspSheet1AndResultDef>();

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

	results.add(OWNER_LIST);

	return results.toArray(new TspSheet1AndResultDef[0]);
    }

    public static TspSheet1AndResultDef[] getResultColsWithoutHidden() {
	ArrayList<TspSheet1AndResultDef> results = new ArrayList<TspSheet1AndResultDef>();

	results.add(COUNTIES);
	results.add(TOWNSHIPS);
	results.add(SEGMENT);
	results.add(LAND_PRICE);
	results.add(LAND_USED_PARTITON);

	results.add(NUMBERS_OF_LAND_BUILDING);
	results.add(PRIVATE_LEGAL_PERSON);
	results.add(NATURAL_PERSON);
	results.add(OWNER_LIST);

	return results.toArray(new TspSheet1AndResultDef[0]);
    }
}
