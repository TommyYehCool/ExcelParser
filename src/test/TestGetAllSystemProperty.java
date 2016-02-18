package test;

import java.util.Properties;

public class TestGetAllSystemProperty {

    public static void main(String[] args) {
	new TestGetAllSystemProperty().start();
    }

    private void start() {
	Properties props = System.getProperties();  
	props.list(System.out);  
    }

}
