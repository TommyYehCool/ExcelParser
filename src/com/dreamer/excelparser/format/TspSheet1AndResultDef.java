package com.dreamer.excelparser.format;

import java.util.ArrayList;

public enum TspSheet1AndResultDef {
    /**
     * ���� (Src)(Result)
     */
    COUNTIES			(0, 0, 0, "����", 0, false),
    /**
     * �m�� (Src)(Result)
     */
    TOWNSHIPS			(1, 	1, 	1, "�m��",	1, false),
    /**
     * �q�p�q (Src)(Result)
     */
    SEGMENT			(2, 	2, 	2, "�q�p�q", 	2, false),
    /**
     * �a�� (Src)(Result_Hidden)
     */
    LAND_NO			(3, 	3,     -1, "�a��", 	3, false),
    /**
     * ������i�{�� (Src)(Result)
     */
    LAND_PRICE			(4, 	4, 	3, "������i�{��", 	4, true),
    /**
     * ���n (Src)(Result_Hidden)
     */
    AREA			(6, 	5,     -1, "���n", 	5, true),
    /**
     * �g�a�ϥΤ��� (Src)(Result)
     */
    LAND_USED_PARTITON		(7, 	6, 	4, "�g�a�ϥΤ���", 	6, false),
    /**
     * �a�W�ت��ɼ� (Src)(Result)
     */
    NUMBERS_OF_LAND_BUILDING	(15, 	7, 	5, "�a�W�ت��ɼ�", 	7, true),
    /**
     * �p�k�H (Src)(Result)
     */
    PRIVATE_LEGAL_PERSON	(9,	8, 	6, "�p�k�H", 	8, true),
    /**
     * �۵M�H (Src)(Result)
     */
    NATURAL_PERSON		(10, 	9, 	7, "�۵M�H", 	9, true),
    /**
     * �Ҧ��v�H�C�� (Src)(Result)
     */
    OWNER_LIST			(18,   10, 	8, "�Ҧ��v�H�C��", 10, false);

    private int srcExcelIndex;
    private int resultExcel_WithHidden_CellIndex;
    private int resultExcel_WithoutHidden_CellIndex;
    /**
     * ���͵��G�� Title �W��
     */
    private String titleName;
    /**
     * TownshipsParser ����Ʀs��}�C��m
     */
    private int tspContentsArrayIndex;
    /**
     * �O�_���Ʀr
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
