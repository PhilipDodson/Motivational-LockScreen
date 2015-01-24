package com.motive.lockscreen.motivationals;

public class BibleVerse {
	private String book;
	private String verse;
	private int number;
	private int chapter;

	public BibleVerse(String book,String verse,int chapter,int number) {
		this.book = book;
		this.verse = verse;
		this.number = number;
		this.chapter = chapter;
	}

	public String getBook() {
		return book;
	}
	public void setBook(String book) {
		this.book = book;
	}
	public String getVerse() {
		return verse;
	}
	public void setVerse(String verse) {
		this.verse = verse;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getChapter() {
		return chapter;
	}
	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	@Override
	public String toString() {
		return verse+"\n"+book+" "+chapter+":"+number;
	}


}
