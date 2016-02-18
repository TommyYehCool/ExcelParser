package com.dreamer.excelparser.format;

import java.util.ArrayList;

/**
 * �w�q�n�� Sheet 1 �򲣥͵��G�����
 * 
 * @author TommyYeh
 */
public enum CppSheet1AndResultCellDef {
    /**
     * �Ҧ��v�H�C�� (Src)(Result)
     */
    OWNER_LIST			(10, 	0,	0, "�Ҧ��v�H�C��", -1, false, 35),
    /**
     * ���� (Src)(Result)
     */
    COUNTIES			(0, 	1, 	1, "����", 	0, false, 12),
    /**
     * �m�� (Src)(Result)
     */
    TOWNSHIPS			(1, 	2, 	2, "�m��", 	1, false, 12),
    /**
     * �q�p�q (Src)(Result)
     */
    SEGMENT			(2, 	3, 	3, "�q�p�q", 	2, false, 27),
    /**
     * �a�� (Src)(Result_Hidden)
     */
    LAND_NO			(3, 	4,     -1, "�a��", 	3, false, 10),
    /**
     * ������i�{�� (Src)(Result)
     */
    LAND_PRICE			(4, 	5, 	4, "������i�{��", 	4, true, 15),
    /**
     * ���n (Src)(Result_Hidden)
     */
    AREA			(5,	6,     -1, "���n", 	5, true, 8),
    /**
     * �g�a�ϥΤ��� (Src)(Result)
     */
    LAND_USED_PARTITON		(6, 	7, 	5, "�g�a�ϥΤ���", 	6, false, 30),
    /**
     * �a�W�ت��ɼ� (Src)(Result)
     */
    NUMBERS_OF_LAND_BUILDING	(7, 	8, 	6, "�a�W�ت��ɼ�", 	7, true, 15),
    /**
     * �p�k�H (Src)(Result)
     */
    PRIVATE_LEGAL_PERSON	(8, 	9, 	7, "�p�k�H", 	8, true, 10),
    /**
     * �۵M�H (Src)(Result)
     */
    NATURAL_PERSON		(9,    10, 	8, "�۵M�H", 	9, true, 10);

    private int srcExcelIndex;
    private int resultExcel_WithHidden_CellIndex;
    private int resultExcel_WithoutHidden_CellIndex;
    /**
     * ���͵��G�� Title �W��
     */
    private String titleName;
    /**
     * BranchPersonalParser ����Ʀs��}�C��m
     */
    private int bppContentsArrayIndex;
    /**
     * �O�_���Ʀr
     */
    private boolean isNumber;
    /**
     * ���e��
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