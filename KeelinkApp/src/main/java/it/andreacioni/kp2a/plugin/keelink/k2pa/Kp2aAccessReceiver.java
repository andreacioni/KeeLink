package it.andreacioni.kp2a.plugin.keelink.k2pa;

import java.util.ArrayList;

import keepass2android.pluginsdk.PluginAccessBroadcastReceiver;
import keepass2android.pluginsdk.Strings;

/**
 * Created by andreacioni on 30/05/16.
 */

public class Kp2aAccessReceiver extends PluginAccessBroadcastReceiver {
    @Override
    public ArrayList<String> getScopes() {
        ArrayList<String> scopes = new ArrayList<String>();

        scopes.add(Strings.SCOPE_DATABASE_ACTIONS);
        scopes.add(Strings.SCOPE_CURRENT_ENTRY);

        return scopes;
    }
}
