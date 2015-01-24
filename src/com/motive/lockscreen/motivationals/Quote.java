package com.motive.lockscreen.motivationals;

public class Quote {
	private String author;
	private String quote;
	private int length;

	public Quote (String author,String quote,int len) {
		this.author = author;
		this.quote = quote;
		length = len;
	}

	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getQuote() {
		return quote;
	}
	public void setQuote(String quote) {
		this.quote = quote;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public String toString() {
		return quote+"\n"+author;
	}
}
