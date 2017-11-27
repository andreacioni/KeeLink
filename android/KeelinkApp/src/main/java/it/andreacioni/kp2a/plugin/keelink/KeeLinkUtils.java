package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by andreacioni on 16/06/16.
 */

public class KeeLinkUtils {

    private static final String TAG = KeeLinkUtils.class.getSimpleName();

    public static ProgressDialog setupProgressDialog(Context ctx) {
        ProgressDialog progressDialog = new ProgressDialog(ctx);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");
        return progressDialog;
    }

    public static void setFastFlag(Context ctx, boolean b) {
        Log.d(TAG,"Setting fast flag to true: " + b);
        SharedPreferences pref = ctx.getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(KeelinkDefs.FLAG_FAST_SEND,b?System.currentTimeMillis():0L);
        editor.commit();
    }

    public static boolean getFastFlag(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
        long l = pref.getLong(KeelinkDefs.FLAG_FAST_SEND,0L);
        long s = l+KeelinkDefs.FAST_MILLIS_VALIDITY;
        Log.d(TAG,"FLAG_FAST_SEND is " + l + ", sum is " + s + ", actual time: " +  System.currentTimeMillis());
        boolean ret = (l+KeelinkDefs.FAST_MILLIS_VALIDITY > System.currentTimeMillis());
        return ret;
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

    public static String PEMtoBase64String(String key) {
        return key.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\n", "");
    }

    public static PublicKey buildPublicKeyFromPEMString(String key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        key = PEMtoBase64String(key);
        return buildPublicKeyFromBase64String(key);
    }

    public static PublicKey buildPublicKeyFromBase64String(String key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] byteKey = Base64.decode(key.getBytes(), Base64.DEFAULT);
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(X509publicKey);
    }

    public static String encrypt(PublicKey publicKey, String plainTextKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] plainTextByte = plainTextKey.getBytes();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);

        String encryptedText = Base64.encodeToString(encryptedByte, Base64.URL_SAFE);

        return encryptedText;
    }
}
