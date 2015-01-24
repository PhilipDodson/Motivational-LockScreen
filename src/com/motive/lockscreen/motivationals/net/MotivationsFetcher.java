package com.motive.lockscreen.motivationals.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.motive.lockscreen.motivationals.BibleVerse;
import com.motive.lockscreen.motivationals.Quote;

public class MotivationsFetcher {
	private static final String TAG = MotivationsFetcher.class.getSimpleName();
	private static String QUOTE_OF_THE_DAY_URL = "http://api.theysaidso.com/qod.json";
	private static String RANDOM_QUOTE_URL = "http://www.iheartquotes.com/api/v1/random?format=json&max_lines=10";//http://quotesondesign.com/api/3.0/api-3.0.json";

	private static String VERSE_OF_THE_DAY_URL = "http://api.theysaidso.com/bible/vod.json";
	private static String RANDOM_VERSE_URL = "http://api.theysaidso.com/bible/verse.json";

	private static Quote dailyQuote = new Quote("","Tap to refresh Quote",10);
	private static Quote randomQuote = new Quote("","Tap to refresh Quote",10);

	private static BibleVerse dailyBibleVerse = new BibleVerse("","Tap to refresh Verse",10,10);
	private static BibleVerse randomBibleVerse = new BibleVerse("","Tap to refresh Verse",10,10);


	private static String getResponseText (String url_) throws IOException {
		StringBuilder response  = new StringBuilder();

		URL url = new URL(url_);
		HttpURLConnection httpconn = (HttpURLConnection)url.openConnection();
		if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
			String strLine = null;
			while ((strLine = input.readLine()) != null)
			{
				response.append(strLine);
			}
			input.close();
		}
		return response.toString();
	}

	public static Quote getDailyQuote() throws IOException{
		String responseText = getResponseText(QUOTE_OF_THE_DAY_URL);
		Log.d(TAG, responseText);
		JSONObject mainResponseObject;
		try {
			mainResponseObject = new JSONObject(responseText);
			JSONObject contents = mainResponseObject.getJSONObject("contents");
			dailyQuote = new Quote(contents.getString("author"),contents.getString("quote"),Integer.parseInt(contents.getString("length")));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(),e);
		}
		return dailyQuote;
	}

	public static Quote getRandomQuote() throws IOException{
		String responseText = getResponseText(RANDOM_QUOTE_URL);
		Log.d(TAG, responseText);
		try {
			JSONObject contents = new JSONObject(responseText);
			randomQuote = new Quote("",contents.getString("quote"),contents.getString("quote").length());
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(),e);
		}
		return randomQuote;
	}

	public static BibleVerse getDailyBibleVerse() throws IOException{
		String responseText = getResponseText(VERSE_OF_THE_DAY_URL);
		Log.d(TAG, responseText);
		JSONObject mainResponseObject;
		try {
			mainResponseObject = new JSONObject(responseText);
			JSONObject contents = mainResponseObject.getJSONObject("contents");
			dailyBibleVerse = new BibleVerse(contents.getString("book"),contents.getString("verse"),Integer.parseInt(contents.getString("chapter")),Integer.parseInt(contents.getString("number")));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(),e);
		}
		return dailyBibleVerse;
	}

	public static BibleVerse getRandomBibleVerse(int book) throws IOException{
		String responseText = getResponseText(RANDOM_VERSE_URL+(book <= 0 ? "":"?book="+book));

		Log.d(TAG, responseText);
		Log.d(TAG,RANDOM_VERSE_URL+(book <= 0 ? "":"?book="+book));
		JSONObject mainResponseObject;
		try {
			mainResponseObject = new JSONObject(responseText);
			JSONObject contents = mainResponseObject.getJSONObject("contents");
			randomBibleVerse = new BibleVerse(contents.getString("book"),contents.getString("verse"),Integer.parseInt(contents.getString("chapter")),Integer.parseInt(contents.getString("number")));
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(),e);
		}
		return randomBibleVerse;
	}
}
