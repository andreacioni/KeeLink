package it.andreacioni.kp2a.plugin.keelink;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreacioni on 21/05/16.
 */
class AsyncPostTask extends AsyncTask<String,Integer,Boolean> {

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
            Log.e(this.getClass().getSimpleName(), "General IO exception:" + e.getMessage());
        }


        if(response == null) {
            Log.e(this.getClass().getSimpleName(), "Null response received");

            return false;
        } else {
            Log.d(this.getClass().getSimpleName(), "Response code: " + responseCode + " Response: " + response.toString());

            return (responseCode == 200) && "OK".equals(response.toString());
        }

    }

}
