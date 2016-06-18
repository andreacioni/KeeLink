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

    private ProgressDialog dialog = null;

    private AsyncPostResponse responseCallback = null;

    public AsyncPostTask(Context ctx,AsyncPostResponse r) {
        this.dialog = KeeLinkUtils.setupProgressDialog(ctx);
        responseCallback = r;
    }

    @Override
    protected void onPreExecute() {
        dialog.show();
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

        dialog.cancel();
        dialog.dismiss();
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

}
