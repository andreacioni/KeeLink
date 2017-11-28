package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

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
        boolean ret = false;
        if(params == null || params.length != 3 ) {
            Log.e(this.getClass().getSimpleName(),"Invalid params passed");
        } else {
            try {
                String encryptedKey = encryptKey(params[0], params[1], params[2]);
                sendKey(params[0], params[1], encryptedKey);
                ret = true;
            } catch (IOException e) {
                Log.e(TAG, "IO exception on connection to remote server", e);
            } catch (NoSuchPaddingException e) {
                Log.e(TAG, "No padding",e);
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "No encryption algorithm found",e);
            } catch (JSONException e) {
                Log.e(TAG, "JSON parse exception",e);
            } catch (InvalidKeyException e) {
                Log.e(TAG, "Supplied key not valid",e);
            } catch (IllegalBlockSizeException e) {
                Log.e(TAG, "Invalid block size",e);
            } catch (BadPaddingException e) {
                Log.e(TAG, "Bad padding",e);
            } catch (InvalidKeySpecException e) {
                Log.e(TAG, "Invalid key",e);
            }
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        responseCallback.response(aBoolean);

        dialog.cancel();
        dialog.dismiss();
    }

    private String encryptKey(String targetSite,String sid,String key) throws IOException, NullPointerException, JSONException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        Log.d(TAG, "Getting public key from remote");

        String publicKeyString = sendHTTPRequest("GET", targetSite + "/getpublickey.php?sid=" + sid, null);
        PublicKey publicKey = KeeLinkUtils.buildPublicKeyFromBase64String(publicKeyString);
        Log.d(TAG, publicKey.toString());
        key = KeeLinkUtils.encrypt(publicKey, key); //key is now in base64 mode

        return key;

    }

    private void sendKey(String targetSite,String sid,String key) throws IOException, NullPointerException, JSONException {
        Log.d(TAG, "Sending key to remote");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("sid", sid);
        parameters.put("key", key);

        sendHTTPRequest("POST", targetSite + "/updatepsw.php", parameters);
    }

    private String sendHTTPRequest(String method, String url, Map<String, String> postParameters) throws IOException, NullPointerException, JSONException {
        Log.d(TAG, "Sending HTTP " + method + " request to: " + url + ", parameters: " + postParameters);
        String ret = null;

        StringBuffer response = null;
        int responseCode = 0;
        HttpURLConnection conn = null;
        BufferedReader in = null;
        DataOutputStream wr = null;

        try {
            URL obj = new URL(url);
            conn = (HttpURLConnection) obj.openConnection();

            conn.setRequestMethod(method);
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);

            if(method.equals("POST") && !postParameters.isEmpty()) {
                String urlParameters = "";

                for(Map.Entry<String,String> e : postParameters.entrySet()) {
                    urlParameters += e.getKey() + "=" + e.getValue() + "&";
                }
                urlParameters = urlParameters.substring(0, urlParameters.length()-1); //last "&" is not needed

                Log.d(TAG, "Sending POST request parameters");
                conn.setDoOutput(true);
                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();
                wr = null;
            }

            Log.d(TAG, "Looking for HTTP response");

            responseCode = conn.getResponseCode();

            Log.d(TAG, "HTTP response code: " + responseCode);

            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            in = null;
            conn = null;
        } catch (IOException e) {
            throw e;
        } finally {
            if(wr != null)
                wr.close();
            if(in != null)
                in.close();
            if(conn != null)
                conn.disconnect();

        }

        if (response == null) {
            throw new NullPointerException("Null response received");
        } else {
            if (responseCode == 200) {
                Log.d(TAG, "Response: " + response.toString());

                JSONObject jObj = new JSONObject(response.toString());
                Log.d(TAG, jObj.toString());
                Boolean b = jObj.getBoolean("status");
                if(!b)
                    throw new IllegalStateException("Response status is false");
                else
                    ret = jObj.getString("message");

            } else {
                Log.w(TAG, "Something wrong in HTTP request. (" + responseCode + ")");
            }
        }

        return ret;
    }
}
