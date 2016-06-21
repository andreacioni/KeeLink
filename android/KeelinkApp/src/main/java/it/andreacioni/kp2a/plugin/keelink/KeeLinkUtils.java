package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by andreacioni on 16/06/16.
 */

public class KeeLinkUtils {
    public static ProgressDialog setupProgressDialog(Context ctx) {
        ProgressDialog progressDialog = new ProgressDialog(ctx);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");
        return progressDialog;
    }

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
