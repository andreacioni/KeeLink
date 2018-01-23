package it.andreacioni.kp2a.plugin.keelink.k2pa;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import it.andreacioni.kp2a.plugin.keelink.R;
import it.andreacioni.kp2a.plugin.keelink.activity.MainActivity;
import it.andreacioni.kp2a.plugin.keelink.keelink.KeeLinkUtils;
import it.andreacioni.kp2a.plugin.keelink.preferences.KeelinkPreferences;
import keepass2android.pluginsdk.PluginAccessException;
import keepass2android.pluginsdk.PluginActionBroadcastReceiver;
import keepass2android.pluginsdk.Strings;

/**
 * Created by andreacioni on 30/05/16.
 */

public class Kp2aActionReceiver extends PluginActionBroadcastReceiver {

    private static final String TAG = Kp2aActionReceiver.class.getSimpleName();

    private boolean fastFlagSending = false;

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

        if(fastFlagSending) {
            Log.d(TAG,"Flash sending enabled");
            Intent i = new Intent(oe.getContext(), MainActivity.class);

            i.putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, new JSONObject(oe.getEntryFields()).toString());
            i.putExtra(Strings.EXTRA_ENTRY_ID, oe.getEntryId());
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            oe.getContext().startActivity(i);
        } else
            Log.d(TAG,"Flash sending disabled");

    }

    @Override
    protected void actionSelected(ActionSelectedAction actionSelected) {
        Log.d(TAG,"Action selected: " + actionSelected);
        Intent i = new Intent(actionSelected.getContext(), MainActivity.class);

        i.putExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA, new JSONObject(actionSelected.getEntryFields()).toString());
        i.putExtra(Strings.EXTRA_ENTRY_ID,actionSelected.getEntryId());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        actionSelected.getContext().startActivity(i);

        super.actionSelected(actionSelected);
    }



    @Override
    public void onReceive(Context ctx, Intent intent) {
        fastFlagSending = KeeLinkUtils.checkFastFlagTimeout(ctx) && KeelinkPreferences.getBoolean(ctx, KeelinkPreferences.FLAG_FAST_ENABLE);
        super.onReceive(ctx,intent);
    }
}
