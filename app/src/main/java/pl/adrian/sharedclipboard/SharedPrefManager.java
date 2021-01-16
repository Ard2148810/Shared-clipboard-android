package pl.adrian.sharedclipboard;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefManager {

    public static void save(SharedPreferences pref, List<String> list, String listKey) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(listKey, jsonString);
        editor.apply();
    }

    public static List<String> read(SharedPreferences pref, String listKey) {
        Gson gson = new Gson();
        String jsonString = pref.getString(listKey, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(jsonString, type);
    }

}
