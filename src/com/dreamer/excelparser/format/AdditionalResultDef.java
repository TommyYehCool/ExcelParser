package com.dreamer.excelparser.format;

public enum AdditionalResultDef {
    /**
     * 地址
     */
    ADDRESS		     (11, 9,  "地址", 35),
    /**
     * 電話
     */
    PHONE_NO		     (12, 10, "電話", 15),
    /**
     * 分機 單位 承辦人
     */
    EXTENSION_UNIT_UNDERTAKER(13, 11, "分機 單位 承辦人", 18),
    /**
     * 訪談內容
     */
    CHAT_CONTENT	     (14, 12, "訪談內容", 20),
    /**
     * 日期
     */
    DATE		     (15, 13, "日期", 15);

    private int resultExcel_WithHidden_CellIndex;
    private int resultExcel_WithoutHidden_CellIndex;
    private String titleName;
    private int columnWidth;

    private AdditionalResultDef(
	    int resultExcel_WithHidden_CellIndex, 
	    int resultExcel_WithoutHidden_CellIndex, 
	    String titleName, 
	    int columnWidth) {
	this.resultExcel_WithHidden_CellIndex = resultExcel_WithHidden_CellIndex;
	this.resultExcel_WithoutHidden_CellIndex = resultExcel_WithoutHidden_CellIndex;
	this.titleName = titleName;
	this.columnWidth = columnWidth;
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
    
    public int getColumnWidth() {
	return this.columnWidth;
    }
}
