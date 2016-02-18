package test;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestLambdaFilter {
    private final String SEPRATE_LINE = "====================================================================================\n";

    private List<String> languages = Arrays.asList("Java", "Scala", "C++", "Haskell");
    
    private List<Integer> costBeforeTax = Arrays.asList(100, 200, 300, 400, 500);
    
    private List<String> G7 = Arrays.asList("USA", "Japan", "France", "Germany", "Italy", "U.K.","Canada");
    
    private List<Integer> primes = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);

    private void start() {
	System.out.println("Languages: " + languages);
	System.out.println();
	
	testSingleFilter();
	
	testCombineFilter();
	
	testCreate_NewList_FromLanguages_By_Filtering();
	
	System.out.println(SEPRATE_LINE);
	
	testCalculateCostAfterTax();
	
	testSumCostAfterTax();
	
	System.out.println(SEPRATE_LINE);
	
	testCreate_NewStr_FromG7_ToUpperCase_JoinWithComa();
	
	System.out.println(SEPRATE_LINE);
	
	testMath();
    }

    private void testSingleFilter() {
	System.out.println("Languages which starts with J :");
	filter((str) -> str.startsWith("J"));

	System.out.println("Languages which ends with a ");
	filter((str) -> str.endsWith("a"));

	System.out.println("Print all languages :");
	filter((str) -> true);

	System.out.println("Print no language : ");
	filter((str) -> false);

	System.out.println("Print language whose length greater than 4:");
	filter((str) -> str.length() > 4);
    }

    private void filter(Predicate<String> condition) {
	languages.stream()
	     	 .filter((language) -> (condition.test(language)))
	     	 .forEach((language) -> System.out.println(language + " "));
	System.out.println();
    }

    private void testCombineFilter() {
        Predicate<String> startsWithJ = (language) -> language.startsWith("J");
        Predicate<String> fourLetterLong = (language) -> language.length() == 4;
    
        System.out.println("Languages which starts with 'J' and four letter long:");
        languages.stream()
        	 .filter(startsWithJ.and(fourLetterLong))
        	 .forEach((language) -> System.out.println(language));
        
        System.out.println();
    }
    
    private void testCalculateCostAfterTax() {
	System.out.println("Calculate cost after tax:");
	costBeforeTax.stream().map((cost) -> cost + 0.12 * cost).forEach(System.out::println);
	System.out.println();
    }
    
    private void testSumCostAfterTax() {
	System.out.println("Sum cost after tax:");
	double total = costBeforeTax.stream().map((cost) -> cost + 0.12 * cost).reduce((sum, cost) -> sum + cost).get();
	System.out.println("Total: " + total);
	System.out.println();
    }

    private void testCreate_NewList_FromLanguages_By_Filtering() {
        List<String> filtered = languages.stream().filter(x -> x.length() > 4).collect(Collectors.toList());
        System.out.printf("Original List: %s, Filter by length > 4 got List: %s %n", languages, filtered);
        System.out.println();
    }

    private void testCreate_NewStr_FromG7_ToUpperCase_JoinWithComa() {
	String result = G7.stream().map(x -> x.toUpperCase()).collect(Collectors.joining(","));
	System.out.println(result);
	System.out.println();
    }

    private void testMath() {
	IntSummaryStatistics stats = primes.stream().mapToInt((x) -> x).summaryStatistics();

	System.out.println("Number List: " + primes);
	System.out.println("Highest: " + stats.getMax());
	System.out.println("Lowest: " + stats.getMin());
	System.out.println("Sum: " + stats.getSum());
	System.out.println("Average: " + stats.getAverage());
	System.out.println();
    }

    public static void main(String[] args) {
	new TestLambdaFilter().start();
    }
}
