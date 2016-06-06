package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.andreacioni.kp2a.plugin.keelink.KeelinkDefs;
import keepass2android.pluginsdk.KeepassDefs;

public class AsyncSavePreferencesTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = AsyncSavePreferencesTask.class.getSimpleName();

    private ProgressDialog dialog = null;
    private String json;
    private String id;
    private Context ctx = null;

    public AsyncSavePreferencesTask(ProgressDialog d, String id, String json) {
        this.dialog = d;
        this.json = json;
        this.id = id;
        this.ctx = dialog.getContext();
    }

    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        saveChoiceOnPref();

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        dialog.cancel();
        dialog.dismiss();
    }

    private void saveChoiceOnPref() {
        if(json != null && json.startsWith("{") && json.endsWith("}") && id != null && !id.isEmpty()) {
            SharedPreferences pref = ctx.getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE, Context.MODE_PRIVATE);
            String currentHistory = pref.getString(KeelinkDefs.RECENT_PREFERENCES_ENTRY,"[]");
            try {
                JSONArray array = new JSONArray(currentHistory);
                JSONObject o = new JSONObject(json);
                o.remove(KeepassDefs.PasswordField);
                o.put(KeelinkDefs.GUID_FIELD,id);

                int ret = guidExist(array, (String) o.get(KeelinkDefs.GUID_FIELD));

                if(ret == -1) {
                    Log.d(TAG,"Entry not exist, creating it");

                    array = insertEntry(array,o);
                } else {
                    Log.d(TAG,"Put entry on top");

                    array = putEntryOnTop(array,o,ret);
                }

                SharedPreferences.Editor editor = pref.edit();
                editor.putString(KeelinkDefs.RECENT_PREFERENCES_ENTRY, array.toString());
                editor.commit();

            } catch (JSONException e) {
                Log.e(TAG,"Parsing exception on saving recents: " + e.getMessage());
            }
        } else
            Log.e(TAG,"Not a valid JSON object to save");
    }

    private JSONArray putEntryOnTop(JSONArray array,JSONObject o,int oIndex) throws JSONException {
        JSONArray ret = new JSONArray();

        ret.put(o);
        for(int i=0;i<oIndex;i++) {
            ret.put(array.get(i));
        }
        for(int i=oIndex+1;i<array.length();i++) {
            ret.put(array.get(i));
        }


        return ret;
    }

    private JSONArray insertEntry(JSONArray array,JSONObject o) throws JSONException {
        JSONArray ret = new JSONArray();

        ret.put(o);
        for(int i=0;i<array.length()-((array.length() > KeelinkDefs.MAX_RECENT_HISTORY_LENGHT)?1:0);i++) {
            ret.put(array.get(i));
        }

        return ret;
    }

    private int guidExist(JSONArray array, String aLong) throws JSONException {
        for(int i=0;i<array.length();i++) {
            JSONObject obj = array.getJSONObject(i);

            if(obj.get(KeelinkDefs.GUID_FIELD).equals(aLong)) {
                return i;
            }
        }

        return -1;
    }
}