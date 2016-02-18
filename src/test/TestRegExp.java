package test;

public class TestRegExp {

    public static void main(String[] args) {
	String MOBILE_NO_PATTERN = "[0][9][0-9]{2}-{0,1}[0-9]{6}";
	
	String test = "0988-147589";
	String test2 = "0988147589";
	
	System.out.println(test.matches(MOBILE_NO_PATTERN));
	System.out.println(test2.matches(MOBILE_NO_PATTERN));
    }
}