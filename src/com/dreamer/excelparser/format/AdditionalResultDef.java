package com.dreamer.excelparser.format;

public enum AdditionalResultDef {
    /**
     * �a�}
     */
    ADDRESS		     (11, 9,  "�a�}", 35),
    /**
     * �q��
     */
    PHONE_NO		     (12, 10, "�q��", 15),
    /**
     * ���� ��� �ӿ�H
     */
    EXTENSION_UNIT_UNDERTAKER(13, 11, "���� ��� �ӿ�H", 18),
    /**
     * �X�ͤ��e
     */
    CHAT_CONTENT	     (14, 12, "�X�ͤ��e", 20),
    /**
     * ���
     */
    DATE		     (15, 13, "���", 15);

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
