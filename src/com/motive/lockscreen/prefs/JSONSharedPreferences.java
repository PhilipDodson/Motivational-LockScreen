package com.motive.lockscreen.prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;


    public class JSONSharedPreferences {
      private static final String PREFIX = "json";

        public static void saveJSONObject(SharedPreferences prefs, String key, JSONObject object) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(JSONSharedPreferences.PREFIX+key, object.toString());
            editor.commit();
        }

        public static void saveJSONArray(SharedPreferences prefs, String key, JSONArray array) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(JSONSharedPreferences.PREFIX+key, array.toString());
            editor.commit();
        }

        public static JSONObject loadJSONObject(SharedPreferences prefs, String key) throws JSONException {
            return new JSONObject(prefs.getString(JSONSharedPreferences.PREFIX+key, "{}"));
        }

        public static JSONArray loadJSONArray(SharedPreferences prefs, String key) throws JSONException {
            return new JSONArray(prefs.getString(JSONSharedPreferences.PREFIX+key, "[]"));
        }

        public static void remove(SharedPreferences prefs, String key) {
            if (prefs.contains(JSONSharedPreferences.PREFIX+key)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(JSONSharedPreferences.PREFIX+key);
                editor.commit();
            }
        }
    }
