package it.andreacioni.kp2a.plugin.keelink.keelink;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.andreacioni.kp2a.plugin.keelink.asynctask.AsyncPostTask;
import it.andreacioni.kp2a.plugin.keelink.preferences.KeelinkPreferences;

/**
 * Created by andreacioni on 19/05/16.
 */
public class KeeLink {

    public static final String QR_CODE_PREFIX = "ksid://";

    private Context ctx = null;

    public KeeLink(Context ctx) {
        this.ctx = ctx;
    }

    public void sendPassword(String sid, String password, AsyncPostTask.AsyncPostResponse responseCallback) {
        new AsyncPostTask(ctx, responseCallback).execute(sid, password);
    }

    public void sendUsernameAndPassword(String sid, String username, String password, AsyncPostTask.AsyncPostResponse responseCallback) {
        new AsyncPostTask(ctx, responseCallback).execute(sid, password, username);
    }

    public boolean checkNetworkConnection() {
        return checkNetworkConnection(ctx);
    }

    public static boolean checkNetworkConnection(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
