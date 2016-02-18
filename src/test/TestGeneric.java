package test;

import java.util.ArrayList;
import java.util.List;

public class TestGeneric {

    public static void main(String[] args) {
	TestGeneric testGeneric = new TestGeneric();

	List<String> hahaha = new ArrayList<String>();
	
	hahaha.add("Tommy");
	hahaha.add("Jeff");
	
	testGeneric.test(hahaha);
	
	List<Integer> heyheyhey = new ArrayList<Integer>();
	
	heyheyhey.add(1);
	heyheyhey.add(2);
	
	testGeneric.test(heyheyhey);
    }

    private <T> void test(List<T> datas) {
	System.out.println(datas);
    }

}
