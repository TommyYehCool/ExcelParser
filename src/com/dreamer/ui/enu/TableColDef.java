package com.dreamer.ui.enu;

public enum TableColDef {
    OWNER(0, DataType.String, "owner", false),
    COUNTIES(1, DataType.String, "counties", false),
    TOWNSHIPS(2, DataType.String, "townships", false),
    SEGMENT(3, DataType.String, "segment", false),
    LAND_NO(4, DataType.String, "landNo", false),
    
    LAND_PRICE(5, DataType.Long, "landPrice", false),
    AREA(6, DataType.Double, "area", false),
    LAND_USE_PARTITION(7, DataType.String, "landUsePartition", false),
    NUMBERS_OF_BUILDING(8, DataType.Integer, "numbersOfBuilding", false),
    PRIVATE_LEGAL_PERSON(9, DataType.Integer, "privateLegalPerson", false),

    NATURAL_PERSON(10, DataType.Integer, "naturalPerson", false),
    PHONE_NO(11, DataType.String, "phoneNo", true),
    ADDRESS(12, DataType.String, "address", true);
    
    private int tableColIndex;
    private DataType dataType;
    private String mappingVoField;
    private boolean isEditable;
    
    private TableColDef(int tableColIndex, DataType dataType, String mappingVoField, boolean isEditable) {
	this.tableColIndex = tableColIndex;
	this.dataType = dataType;
	this.mappingVoField = mappingVoField;
	this.isEditable = isEditable;
    }
    
    public int getTableColIndex() {
	return this.tableColIndex;
    }
    
    public DataType getDateType() {
	return this.dataType;
    }
    
    public String getMappingVoField() {
	return this.mappingVoField;
    }
    
    public boolean isEditable() {
	return this.isEditable;
    }
    
    public static TableColDef convertByTableIndex(int tableColIndex) {
	for (TableColDef col : TableColDef.values()) {
	    if (col.getTableColIndex() == tableColIndex) {
		return col;
	    }
	}
	return null;
    }
    
    public enum DataType {
	String, Integer, Long, Double;
    }
}
