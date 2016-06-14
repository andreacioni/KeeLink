package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import keepass2android.pluginsdk.KeepassDefs;

/**
 * Created by andreacioni on 21/05/16.
 */
class AsyncPostTask extends AsyncTask<String,Integer,Boolean> {

    private static final String TAG = AsyncPostTask.class.getSimpleName();

    private static final String USER_AGENT = "Mozilla/5.0";


    private AsyncPostResponse responseCallback = null;

    public AsyncPostTask(AsyncPostResponse r) {
        responseCallback = r;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if(params == null || params.length != 3 ) {
            Log.e(this.getClass().getSimpleName(),"Invalid params passed");
            return false;
        } else {
            return sendThroughStd(params[0],params[1],params[2]);
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        responseCallback.response(aBoolean);
    }

    private boolean sendThroughStd(String targetSite,String sid,String key) {

        boolean ret = false;

        String url = targetSite + "/updatepsw.php";
        StringBuffer response = null;
        int responseCode = 0;

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setReadTimeout(10000);
            con.setConnectTimeout(15000);
            con.setDoInput(true);

            String urlParameters = "sid=" + sid + "&key=" + key;

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            Log.e(TAG, "General IO exception:" + e.getMessage());
        }


        if(response == null) {
            Log.e(TAG, "Null response received");
        } else {
            Log.d(TAG, "Response code: " + responseCode + " Response: " + response.toString());

            if(responseCode == 200) {
                try {
                    JSONObject jObj  = new JSONObject(response.toString());
                    Log.d(TAG,jObj.toString());
                    Boolean b = jObj.getBoolean("status");
                    ret = (b!=null) && b;
                } catch (JSONException e) {
                    Log.e(TAG,"JSON parsing exception: " + e.getMessage());
                }

            }
        }

        return ret;
    }

    public static class AsyncSavePreferencesTask extends AsyncTask<Void,Void,Void> {

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
}
