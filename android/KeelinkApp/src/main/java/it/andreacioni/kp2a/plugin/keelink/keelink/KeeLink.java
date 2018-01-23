package it.andreacioni.kp2a.plugin.keelink.keelink;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import it.andreacioni.kp2a.plugin.keelink.asynctask.AsyncPostTask;

/**
 * Created by andreacioni on 19/05/16.
 */
public class KeeLink {

    public static final String QR_CODE_PREFIX = "ksid://";

    private Context ctx = null;

    public KeeLink(Context ctx) {
        this.ctx = ctx;
    }

    public void sendKey(String sid, String key, AsyncPostTask.AsyncPostResponse response) {
        AsyncPostTask post = new AsyncPostTask(ctx,response);
        post.execute(KeelinkDefs.TARGET_SITE, sid, key);
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
