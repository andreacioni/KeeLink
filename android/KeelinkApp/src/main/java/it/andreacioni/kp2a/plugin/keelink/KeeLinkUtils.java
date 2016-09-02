package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

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

    public static int guidExist(JSONArray array, String aLong) throws JSONException {
        for(int i=0;i<array.length();i++) {
            JSONObject obj = array.getJSONObject(i);

            if(obj.get(KeelinkDefs.GUID_FIELD).equals(aLong)) {
                return i;
            }
        }

        return -1;
    }

    public static String hideUsernameString(String username) {
        String clone = new String(username);
        char us[] = username.toCharArray();
        if (username.length() >5) {
            for (int i=2;i<username.length()-2;i++){
                us[i]= '*';
                clone = new String(us);
            }
        } else{
            for (int i=1;i<username.length();i++){
                us[i]= '*';
                clone = new String(us);

            }
        }

        return clone;
    }
}
