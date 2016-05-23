package it.andreacioni.kp2a.plugin.keelink;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by andreacioni on 19/05/16.
 */
public class KeeLink {

    private static final String TARGET_SITE = "http://www.andysite.altervista.org/keepass";

    private Context ctx = null;

    public KeeLink(Context ctx) {
        this.ctx = ctx;
    }

    public void sendKey(String sid, String key,AsyncPostResponse response) {
        AsyncPostTask post = new AsyncPostTask(response);
        post.execute(TARGET_SITE, sid, key);
    }

    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
