package it.andreacioni.kp2a.plugin.keelink;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by andreacioni on 05/06/16.
 */

public class KeelinkDefs {

    public static final String TARGET_SITE = "https://keelink.cloud";

    public static final String RECENT_PREFERENCES_ENTRY = "recentHistory";
    public static final String FLAG_FAST_SEND= "fastSend";

    public static final String RECENT_PREFERENCES_FILE = "preferences";

    public static final String GUID_FIELD = "GUID";

    public static final int MAX_RECENT_HISTORY_LENGHT = 30;

    public static final String STR_NOT_SUPPLIED = "<no supplied>";

    public static void setFastFlag(Context ctx, boolean b) {
        SharedPreferences pref = ctx.getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KeelinkDefs.FLAG_FAST_SEND,b);
        editor.commit();
    }

    public static boolean getFastFlag(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        return pref.getBoolean(KeelinkDefs.FLAG_FAST_SEND,false);
    }

}
