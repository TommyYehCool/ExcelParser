package test;

import java.sql.Statement;

public class TestStatementCode {

    public static void main(String[] args) {
	int code = Statement.EXECUTE_FAILED;
	System.out.println("Statement.EXECUTE_FAILED: " + code);
	
	code = Statement.SUCCESS_NO_INFO;
	System.out.println("Statement.SUCCESS_NO_INFO: " + code);
    }

}
