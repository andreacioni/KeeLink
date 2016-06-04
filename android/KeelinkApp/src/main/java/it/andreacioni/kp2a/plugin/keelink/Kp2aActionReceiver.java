package it.andreacioni.kp2a.plugin.keelink;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;

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
            oe.addEntryAction(oe.getContext().getString(R.string.kp2aplugin_menu_entry),
                    R.drawable.send_keelink, null);

            for (String field: oe.getEntryFields().keySet()) {
                oe.addEntryFieldAction("keepass2android.plugin.qr.show", Strings.PREFIX_STRING+field, oe.getContext().getString(R.string.kp2aplugin_menu_entry),
                        R.drawable.send_keelink, null);
            }

        } catch (PluginAccessException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    protected void actionSelected(ActionSelectedAction actionSelected) {
        Intent i = new Intent(actionSelected.getContext(), MainActivity.class);

        i.putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, new JSONObject(actionSelected.getEntryFields()).toString());
        i.putExtra(Strings.EXTRA_FIELD_ID, actionSelected.getFieldId());
        i.putExtra(Strings.EXTRA_SENDER, actionSelected.getHostPackage());
        i.putExtra(Strings.EXTRA_ENTRY_ID,actionSelected.getEntryId());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        actionSelected.getContext().startActivity(i);

        super.actionSelected(actionSelected);
    }

}
