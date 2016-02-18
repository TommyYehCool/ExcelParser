package test;

import java.util.List;
import java.util.function.Predicate;

public class RoboContact {
    public void phoneContacts(List<Person> pl, Predicate<Person> pred) {
	for (Person p : pl) {
	    if (pred.test(p)) {
		roboCall(p);
	    }
	}
    }

    public void emailContacts(List<Person> pl, Predicate<Person> pred) {
	for (Person p : pl) {
	    if (pred.test(p)) {
		roboEmail(p);
	    }
	}
    }

    private void roboCall(Person p) {
	System.out.println("Calling " + p.getName() + " age " + p.getAge() + " at " + p.getPhone());
    }
    
    private void roboEmail(Person p) {
	 System.out.println("Emailing " + p.getName() + " age " + p.getAge() + " at " + p.getEmail());
    }
}
