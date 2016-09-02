package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import it.andreacioni.kp2a.plugin.keelink.KeelinkDefs;
import it.andreacioni.kp2a.plugin.keelink.MainActivity;
import it.andreacioni.kp2a.plugin.keelink.R;
import keepass2android.pluginsdk.KeepassDefs;

public class RecentActivityLoader extends AsyncTask<Void,Void,Void> {

    private static final String TAG = RecentActivityLoader.class.getSimpleName();

    private ProgressDialog dialog = null;

    private List<Map<String,String>> data =  null;

    private Context ctx = null;

    private ListView listView = null;

    public RecentActivityLoader(Context ctx,ListView vList) {
        this.dialog = KeeLinkUtils.setupProgressDialog(ctx);
        this.ctx = dialog.getContext();
        this.listView = vList;
    }

    @Override
    protected void onPreExecute() {
        data = new ArrayList<Map<String,String>>();
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        SharedPreferences preferences = ctx.getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE, Context.MODE_PRIVATE);
        String jsonPref = preferences.getString(KeelinkDefs.RECENT_PREFERENCES_ENTRY,"[]");
        JSONArray jsonArray = null;

        try {
            jsonArray = new JSONArray(jsonPref);

            for(int i=0;i<jsonArray.length();i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Iterator<String> iter = obj.keys();
                Map<String,String> map = new HashMap<String,String>();
                while(iter.hasNext()) {
                    String key = iter.next();
                    map.put(key, (key.equals(KeepassDefs.TitleField)?"":(key + ": ")) + (obj.getString(key).equals("")?KeelinkDefs.STR_NOT_SUPPLIED:obj.getString(key)));
                }

                data.add(map);
            }
        } catch (JSONException e) {
            Log.e(TAG,"Error parsing data of preferences: " + e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        prepareList();

        dialog.cancel();
        dialog.dismiss();
    }

    private void prepareList() {

        if(data.isEmpty()) {
            HashMap placeholder = new HashMap<String, String>();
            placeholder.put(KeepassDefs.TitleField,"No recents");
            placeholder.put(KeepassDefs.UserNameField,"Your sent history will be available here,");
            placeholder.put(KeepassDefs.UrlField,"click on link button to open Keepass2Android");
            placeholder.put(KeelinkDefs.GUID_FIELD,"0");

            data.add(placeholder);
        }

        SimpleAdapter adapter = new SimpleAdapter(ctx, data,R.layout.recent_list_row, new String[] { KeepassDefs.TitleField,KeelinkDefs.USERNAME_HIDDEN_FIELD,KeepassDefs.UrlField },
                new int[] { R.id.recent_row_title, R.id.recent_row_user, R.id.recent_row_url });
        listView.setAdapter(adapter);
    }
}