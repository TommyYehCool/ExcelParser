package test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Person {
    private String name;
    private int age;
    private String gender;
    private String email;
    private String phone;
    private String address;

    public Person(String name, int age, String gender, String email, String phone, String address) {
	this.name = name;
	this.age = age;
	this.gender = gender;
	this.email = email;
	this.phone = phone;
	this.address = address;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public int getAge() {
	return age;
    }

    public void setAge(int age) {
	this.age = age;
    }

    public String getGender() {
	return gender;
    }

    public void setGender(String gender) {
	this.gender = gender;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getPhone() {
	return phone;
    }

    public void setPhone(String phone) {
	this.phone = phone;
    }

    public String getAddress() {
	return address;
    }

    public void setAddress(String address) {
	this.address = address;
    }

    public static List<Person> createShortList() {
	List<Person> people = new ArrayList<Person>();
	
	people.add(new Person("Ally", 60, "female", "allylin1207@gmail.com", "0935924225", "Sanchung"));
	people.add(new Person("Tommy", 35, "male", "tommy.yeh1112@gmail.com", "0988147589", "Sanchung"));
	people.add(new Person("Alice", 29, "female", "alicecheh1013@gmail.com", "0936902592", "Sanchung"));
	people.add(new Person("Terence", 31, "male", "terenceyeh@gmail.com", "0930126589", "Sanchung"));
	
	return people;
    }
    
    public String printCustom(Function<Person, String> f) {
	return f.apply(this);
    }
}
