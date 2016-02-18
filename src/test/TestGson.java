package test;

import com.google.gson.Gson;

public class TestGson {
    private void start() {
	Book book = new Book("956-987236-1", "Java¾úÀI°O", 550);

	Gson gson = new Gson();

	String json = gson.toJson(book);

	System.out.println(json);

	Book jbook = gson.fromJson(json, Book.class);
	System.out.println(jbook);
    }

    public static void main(String[] args) {
	new TestGson().start();
    }

    class Book {
	private String isbn;
	private String name;
	private int price;

	public Book(String _isbn, String _name, int _price) {
	    isbn = _isbn;
	    name = _name;
	    price = _price;
	}

	public String getIsbn() {
	    return isbn;
	}

	public void setIsbn(String isbn) {
	    this.isbn = isbn;
	}

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public int getPrice() {
	    return price;
	}

	public void setPrice(int price) {
	    this.price = price;
	}

	@Override
	public String toString() {
	    return "Book [isbn=" + isbn + ", name=" + name + ", price=" + price + "]";
	}
    }
}