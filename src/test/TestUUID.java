package test;

import java.util.UUID;

public class TestUUID {

    public static void main(String[] args) {
	UUID randomUUID = UUID.randomUUID();
	String uidStr = randomUUID.toString();
	System.out.println(uidStr);
	    
	String temp = uidStr.substring(0, 8) + uidStr.substring(9, 13) + uidStr.substring(14, 18) + uidStr.substring(19, 23) + uidStr.substring(24);  
        System.out.println(temp);  
        
        String temp2 = uidStr.replaceAll("-", "");
        System.out.println(temp2);
    }

}
