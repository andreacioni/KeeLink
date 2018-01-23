package it.andreacioni.kp2a.plugin.keelink.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import it.andreacioni.kp2a.plugin.keelink.preferences.KeelinkPreferences;

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
        if (username.length() > 5) {
            for (int i = 2; i < username.length() - 2; i++) {
                us[i] = '*';
                clone = new String(us);
            }
        } else {
            for (int i = 1; i < username.length(); i++) {
                us[i] = '*';
                clone = new String(us);

            }
        }

        return clone;
    }

    public static void setFastFlag(Context ctx, boolean b) {
        Log.d(TAG,"Setting fast flag to true: " + b);
        KeelinkPreferences.setLong(ctx, KeelinkPreferences.FLAG_FAST_TIMEOUT,b?System.currentTimeMillis():0L);
    }

    public static boolean checkFastFlagTimeout(Context ctx) {
        long l = KeelinkPreferences.getLong(ctx, KeelinkPreferences.FLAG_FAST_TIMEOUT);
        long s = l+KeelinkDefs.FAST_MILLIS_VALIDITY;
        Log.d(TAG,"FLAG_FAST_TIMEOUT is " + l + ", sum is " + s + ", actual time: " +  System.currentTimeMillis());
        boolean ret = (l+KeelinkDefs.FAST_MILLIS_VALIDITY > System.currentTimeMillis());
        return ret;
    }

    public static RSAPublicKey buildPublicKeyFromBase64String(String key) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] byteKey = Base64.decode(key.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE);
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) kf.generatePublic(X509publicKey);
    }

    public static String encrypt(PublicKey publicKey, String plainTextKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        byte[] plainTextByte = plainTextKey.getBytes();
        Cipher cipher = Cipher.getInstance(KeelinkDefs.TRANSFORMATION_METHOD);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);

        String encryptedText = Base64.encodeToString(encryptedByte, Base64.NO_WRAP | Base64.URL_SAFE);

        return encryptedText;
    }
}
