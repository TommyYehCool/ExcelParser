package test;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestRoboCall {

    public static void main(String[] args) {
	List<Person> pl = Person.createShortList();
	RoboContact robo = new RoboContact();
	
	Predicate<Person> ageOver30 = p -> p.getAge() >= 30;
	Predicate<Person> females = p -> p.getGender().equals("female");
	
	System.out.println("==== Calling old people ====");
	robo.phoneContacts(pl, ageOver30);
	
	System.out.println("\n==== Emailing females ====");
	robo.emailContacts(pl, females);
	
	System.out.println("\n==== Western List ====");
	Function<Person, String> westernStyle = getWesternStyle();
	for (Person p : pl) {
	    System.out.println(
		p.printCustom(westernStyle)
	    );
	}
	
	System.out.println("\n==== Eastern List ====");
	Function<Person, String> easternStyle = getEasternStyle();
	for (Person p : pl) {
	    System.out.println(
		p.printCustom(easternStyle)
	    );
	}
    }
    
    public static Function<Person, String> getWesternStyle() {
	Function<Person, String> westernStyle = p -> {
	    return "Name: " + p.getName() + 
		    "\nAge: " + p.getAge() + 
		    "\nGender: " + p.getGender() + 
		    "\n";
	};
	return westernStyle;
    }
    
    public static Function<Person, String> getEasternStyle() {
	Function<Person, String> easternStyle 
		=  p -> "Name: " + p.getName() + 
		    	"\nAge: " + p.getAge() + 
		    	"\nPhone: " + p.getPhone() + 
		    	"\n";
	return easternStyle;
    }
}
