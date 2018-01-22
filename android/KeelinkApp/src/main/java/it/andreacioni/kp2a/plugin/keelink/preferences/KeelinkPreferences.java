package it.andreacioni.kp2a.plugin.keelink.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.andreacioni.kp2a.plugin.keelink.keelink.KeeLinkUtils;
import it.andreacioni.kp2a.plugin.keelink.keelink.KeelinkDefs;

/**
 * Created by andreacioni on 21/01/18.
 */

public class KeelinkPreferences {

    private static final String TAG = KeelinkPreferences.class.getSimpleName();

    public static final String RECENT_PREFERENCES_FILE = "preferences"; //Generic file where we place our preferences

    public static final String RECENT_PREFERENCES_ENTRY = "recentHistory"; //String
    public static final String FLAG_FAST_TIMEOUT = "fastSend"; //Long
    public static final String FLAG_FAST_ENABLE= "fastSendEnable"; //Boolean

    private static final Map<String, String> DEFAULT_STRING_VALUES;
    private static final Map<String, Long> DEFAULT_LONG_VALUES;
    private static final Map<String, Boolean> DEFAULT_BOOLEAN_VALUES;

    static {
        Map<String, Boolean> tempBoolean = new HashMap<>();
        tempBoolean.put(FLAG_FAST_ENABLE, false);
        DEFAULT_BOOLEAN_VALUES = Collections.unmodifiableMap(tempBoolean);

        Map<String, String> tempString = new HashMap<>();
        tempString.put(RECENT_PREFERENCES_ENTRY, "[]");
        DEFAULT_STRING_VALUES = Collections.unmodifiableMap(tempString);

        Map<String, Long> tempLong = new HashMap<>();
        tempLong.put(FLAG_FAST_TIMEOUT, 0L);
        DEFAULT_LONG_VALUES = Collections.unmodifiableMap(tempLong);
    }

    public static void setBoolean(Context ctx, String key, boolean value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }

    public static void setLong(Context ctx, String key, long value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();

    }

    public static void setString(Context ctx, String key, String  value) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();

    }

    public static String getString(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        String defaultValue = DEFAULT_STRING_VALUES.get(key);
        if(defaultValue == null)
            throw new RuntimeException(String.format("Cannot find default value for key: %s", key));
        return sharedPreferences.getString(key, defaultValue);
    }

    public static long getLong(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        Long defaultValue = DEFAULT_LONG_VALUES.get(key);
        if(defaultValue == null)
            throw new RuntimeException(String.format("Cannot find default value for key: %s", key));
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static boolean getBoolean(Context ctx, String key) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        Boolean defaultValue = DEFAULT_BOOLEAN_VALUES.get(key);
        if(defaultValue == null)
            throw new RuntimeException(String.format("Cannot find default value for key: %s", key));
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
