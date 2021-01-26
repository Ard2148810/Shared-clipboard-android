package pl.adrian.sharedclipboard;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPrefManager {

    public static void save(String listKey, List<String> list, Context context, String preferencesFileKey) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(listKey, jsonString);
        editor.apply();
    }

    public static List<String> read(String listKey, Context context, String preferencesFileKey) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonString = sharedPref.getString(listKey, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(jsonString, type);
    }

    public static void addItem(String listKey, String value, Context context, String preferencesFileKey) {
        List<String> list = read(listKey, context, preferencesFileKey);
        if(list == null) {
            list = new ArrayList<>();
        }
        list.add(0, value);
        if(list.size() > 10) {
            list.remove(list.size() - 1);
        }
        save(listKey, list, context, preferencesFileKey);
    }

}
