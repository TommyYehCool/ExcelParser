package com.dreamer.excelparser.exception;

public class ExcelParserException extends Exception {
    private static final long serialVersionUID = -5419860047456748351L;

    public ExcelParserException(String errorMsg, Exception ex) {
	super(errorMsg, ex);
    }
}