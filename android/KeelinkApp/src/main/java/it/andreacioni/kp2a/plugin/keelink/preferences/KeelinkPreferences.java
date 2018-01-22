package it.andreacioni.kp2a.plugin.keelink.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import it.andreacioni.kp2a.plugin.keelink.keelink.KeeLinkUtils;
import it.andreacioni.kp2a.plugin.keelink.keelink.KeelinkDefs;

/**
 * Created by andreacioni on 21/01/18.
 */

public class KeelinkPreferences {

    private static final String TAG = KeelinkPreferences.class.getSimpleName();

    public static final String RECENT_PREFERENCES_FILE = "preferences";

    public static final String RECENT_PREFERENCES_ENTRY = "recentHistory";
    public static final String FLAG_FAST_SEND= "fastSend";

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

    public static long getLong(Context ctx, String key, long defaultValue) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static boolean getBoolean(Context ctx, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
