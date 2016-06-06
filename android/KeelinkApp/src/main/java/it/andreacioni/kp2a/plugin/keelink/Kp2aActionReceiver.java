package it.andreacioni.kp2a.plugin.keelink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONException;
import org.json.JSONObject;

import keepass2android.pluginsdk.PluginAccessException;
import keepass2android.pluginsdk.PluginActionBroadcastReceiver;
import keepass2android.pluginsdk.Strings;

/**
 * Created by andreacioni on 30/05/16.
 */

public class Kp2aActionReceiver extends PluginActionBroadcastReceiver {

    private static final String TAG = Kp2aActionReceiver.class.getSimpleName();

    @Override
    protected void openEntry(OpenEntryAction oe) {
        Log.d(TAG,"Open entry:" + oe.toString());

        try {
            for (String field: oe.getEntryFields().keySet()) {
                oe.addEntryFieldAction("keepass2android.keelink.send", Strings.PREFIX_STRING+field, oe.getContext().getString(R.string.kp2aplugin_menu_entry),
                        R.drawable.link_icon_black, null);
            }

        } catch (PluginAccessException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {

        try {
            Intent i = new Intent(ctx, MainActivity.class);

            i.putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, new JSONObject(intent.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).toString());
            i.putExtra(Strings.EXTRA_ENTRY_ID,intent.getStringExtra(Strings.EXTRA_ENTRY_ID));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            ctx.startActivity(i);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing passed extra: " + e.getMessage());
        }

        super.onReceive(ctx,intent);
    }
}
