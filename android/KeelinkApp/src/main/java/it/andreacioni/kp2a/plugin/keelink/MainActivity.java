package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import keepass2android.pluginsdk.AccessManager;
import keepass2android.pluginsdk.KeepassDefs;
import keepass2android.pluginsdk.Kp2aControl;
import keepass2android.pluginsdk.Strings;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ACTIVITY_ENABLE_DISABLE = 123;
    private static final int START_KEEPASS_CODE = 399;

    private ProgressDialog progressDialog = null;
    private String passwordReceived = null;
    private KeeLink keeLink = new KeeLink(this);

    private Map<String,String> selected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);

        setupProgressDialog();

        new RecentActivityLoader(progressDialog).execute();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean status = snackStatusShow();

        if(status) { //last condition ensure that the capture activity starts correctly TODO check this solution...
            if(savedInstanceState == null) {
                Intent i = getIntent();
                if ((i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA) != null) &&
                        (i.getStringExtra(Strings.EXTRA_FIELD_ID) != null) &&
                        (i.getStringExtra(Strings.EXTRA_SENDER) != null) &&
                        i.getStringExtra(Strings.EXTRA_ENTRY_ID) != null) {

                    Log.d(TAG, i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA));
                    Log.d(TAG, "Entry GUID: " + i.getStringExtra(Strings.EXTRA_ENTRY_ID));

                    try {
                        passwordReceived = new JSONObject(i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).getString(KeepassDefs.PasswordField);
                    } catch (JSONException e) {
                        Log.e(TAG, "Password parsing error" + e.getMessage());
                    }

                    new DelayedLauncher(progressDialog,i.getStringExtra(Strings.EXTRA_ENTRY_ID),i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).execute();
                }

            } else {
                passwordReceived = savedInstanceState.getString(Strings.EXTRA_ENTRY_OUTPUT_DATA);
            }
        }

    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(passwordReceived != null) {
            outState.putString(Strings.EXTRA_ENTRY_OUTPUT_DATA, passwordReceived);
            //outState.putString(Strings.EXTRA_FIELD_ID, launcherIntent.getStringExtra(Strings.EXTRA_FIELD_ID));
            //outState.putString(Strings.EXTRA_SENDER, launcherIntent.getStringExtra(Strings.EXTRA_SENDER));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        snackStatusShow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true; //TODO
            case R.id.enable_plugin:
                return enableDisablePlugin();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean enableDisablePlugin() {

        try {
            Intent i = new Intent(Strings.ACTION_EDIT_PLUGIN_SETTINGS);
            i.putExtra(Strings.EXTRA_PLUGIN_PACKAGE, getPackageName());
            startActivityForResult(i, ACTIVITY_ENABLE_DISABLE);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean snackStatusShow() {
        boolean ret = false;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(isEnabled()) {
            Snackbar.make(fab, "Not enabled as plugin", Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
        } else {
            if(!KeeLink.checkNetworkConnection(this))
                Snackbar.make(fab, "No network connection", Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
            else {
                Snackbar.make(fab, "Ready!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                ret = true;
            }
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startScanActivity();
                //LoadingOverlayFragment loading = new LoadingOverlayFragment();
                //loading.show(getSupportFragmentManager(),"Title");
                //Intent i = Kp2aControl.getOpenEntryIntent("",false,true);
                //startActivityForResult(i,399)

                if(selected == null) {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Hmmm not understanding...")
                            .setContentText("No selection made, would you open Keepass2Android?")
                            .setConfirmText("Yes")
                            .setCancelText("Cancel")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    openKeepass();
                                }
                            })
                            .show();
                } else if("0".equals(selected.get(KeelinkDefs.GUID_FIELD).toString())){
                    Log.d(TAG,"Opening K2PA...");
                    openKeepass();
                } else {
                    Log.d(TAG,"Sending entry...");
                    String title = selected.get(KeepassDefs.TitleField);
                    String user = selected.get(KeepassDefs.UserNameField);

                    String searchString = prepareSearchText(selected);

                    openKeepassForSearch(searchString);
                }
            }
        });

        return ret;
    }

    private String prepareSearchText(Map<String, String> selected) {
        String ret = "";
        String title = selected.get(KeepassDefs.TitleField);
        String user = selected.get(KeepassDefs.UserNameField);
        /*String note = selected.get(KeepassDefs.NotesField);
        String url = selected.get(KeepassDefs.url)*/

        if(title != null && !title.trim().isEmpty())
            ret += title.trim() + " ";

        if(user != null && !user.substring(KeepassDefs.UserNameField.length()+1).trim().isEmpty())
            ret += user.substring(KeepassDefs.UserNameField.length()+1).trim() + " ";

        /*if(note != null && note.substring(KeepassDefs.NotesField.length()+1).trim().isEmpty())
            ret += note.substring(KeepassDefs.NotesField.length()+1).trim() + " ";*/

        return ret.trim();
    }

    private void openKeepass() {
        openKeepassForSearch("");
    }

    private void openKeepassForSearch(String searchText) {
        Intent i = Kp2aControl.getOpenEntryIntent(searchText,false,false);
        startActivityForResult(i, START_KEEPASS_CODE);
    }

    private boolean isEnabled() {
        return AccessManager.getAllHostPackages(this).isEmpty();
    }


    private void startScanActivity() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CustomCaptureActivity.class);
        integrator.setOrientationLocked(false);

        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult reqCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && passwordReceived != null) {
            String content = result.getContents();

            if (content != null) {

                Log.d(TAG, "Scanned");
                Log.d(TAG,"Pass:" + passwordReceived);

                if (content.startsWith(KeeLink.QR_CODE_PREFIX)) {
                    content = content.substring(KeeLink.QR_CODE_PREFIX.length());
                    Log.d(TAG, "Valid code scanned:" + content);
                    validSidReceived(content,passwordReceived);
                } else {
                    Log.e(TAG, "Invalid code:" + content);
                    Toast.makeText(this, "Invalid QR code scanned!", Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }

        passwordReceived = null;

    }

    private void validSidReceived(String sid,String password) {

        if(snackStatusShow()) {
            keeLink.sendKey(sid, password, new AsyncPostResponse() {
                @Override
                public void response(boolean result) {
                    if(result)
                        Toast.makeText(getApplicationContext(),"Password correctly sent", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(),"Password NOT correctly sent. Try again...", Toast.LENGTH_SHORT).show();

                }
            });
        } else
            Toast.makeText(this,"No network connection available!",Toast.LENGTH_SHORT).show();

    }

    private class DelayedLauncher extends AsyncTask<Void,Void,Void> {

        private ProgressDialog dialog = null;
        private String json;
        private String id;

        private DelayedLauncher(ProgressDialog d,String id,String json) {
            this.dialog = d;
            this.json = json;
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            saveChoiceOnPref();

            startScanActivity();

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            dialog.cancel();
            dialog.dismiss();
        }

        private void saveChoiceOnPref() {
            if(json != null && json.startsWith("{") && json.endsWith("}") && id != null && !id.isEmpty()) {
                SharedPreferences pref = getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
                String currentHistory = pref.getString(KeelinkDefs.RECENT_PREFERENCES_ENTRY,"[]");
                try {
                    JSONArray array = new JSONArray(currentHistory);
                    JSONObject o = new JSONObject(json).put(KeelinkDefs.GUID_FIELD,id);

                    int ret = guidExist(array, (String) o.get(KeelinkDefs.GUID_FIELD));

                    if(ret == -1) {
                        Log.d(TAG,"Entry not exist, creating it");

                        array = insertEntry(array,o);
                    } else {
                        Log.d(TAG,"Put entry on top");


                        array = putEntryOnTop(array,o,ret);
                    }

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(KeelinkDefs.RECENT_PREFERENCES_ENTRY, array.toString());
                    editor.commit();

                } catch (JSONException e) {
                    Log.e(TAG,"Parsing exception on saving recents: " + e.getMessage());
                }
            } else
                Log.e(TAG,"Not a valid JSON object to save");
        }

        private JSONArray putEntryOnTop(JSONArray array,JSONObject o,int oIndex) throws JSONException {
            JSONArray ret = new JSONArray();

            ret.put(o);
            for(int i=0;i<oIndex;i++) {
                ret.put(array.get(i));
            }
            for(int i=oIndex+1;i<array.length();i++) {
                ret.put(array.get(i));
            }


            return ret;
        }

        private JSONArray insertEntry(JSONArray array,JSONObject o) throws JSONException {
            JSONArray ret = new JSONArray();

            ret.put(o);
            for(int i=0;i<array.length()-((array.length() > KeelinkDefs.MAX_RECENT_HISTORY_LENGHT)?1:0);i++) {
                ret.put(array.get(i));
            }

            return ret;
        }

        private int guidExist(JSONArray array, String aLong) throws JSONException {
            for(int i=0;i<array.length();i++) {
                JSONObject obj = array.getJSONObject(i);

                if(obj.get(KeelinkDefs.GUID_FIELD).equals(aLong)) {
                    return i;
                }
            }

            return -1;
        }
    }

    private class RecentActivityLoader extends AsyncTask<Void,Void,Void> {

        private ProgressDialog dialog = null;

        private List<Map<String,String>> data =  null;

        private RecentActivityLoader(ProgressDialog dialog) {this.dialog = dialog;}

        @Override
        protected void onPreExecute() {
            data = new ArrayList<Map<String,String>>();
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            SharedPreferences preferences = getSharedPreferences(KeelinkDefs.RECENT_PREFERENCES_FILE,Context.MODE_PRIVATE);
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
                        map.put(key, (key.equals(KeepassDefs.TitleField)?"":(key + ": ")) + (obj.getString(key).equals("")?"<no supplied>":obj.getString(key)));
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
            final ListView list = (ListView) findViewById(R.id.recent_list);

            if(data.isEmpty()) {
                HashMap placeholder = new HashMap<String, String>();
                placeholder.put(KeepassDefs.TitleField,"No recents");
                placeholder.put(KeepassDefs.UserNameField,"Your sent history will be available here,");
                placeholder.put(KeepassDefs.UrlField,"click on link button to open Keepass2Android");
                placeholder.put(KeelinkDefs.GUID_FIELD,"0");

                data.add(placeholder);
            }

            final SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), data,R.layout.recent_list_row, new String[] { KeepassDefs.TitleField,KeepassDefs.UserNameField,KeepassDefs.UrlField },
                    new int[] { R.id.recent_row_title, R.id.recent_row_user, R.id.recent_row_url });
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG,adapterView.getItemAtPosition(i).toString());
                    if(selected != null && selected.equals((Map<String, String>) adapterView.getItemAtPosition(i))) {
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                                .setTitleText("Just another click")
                                .setContentText("Click on green button below to open the Keepass entry")
                                .setConfirmText("Ok!")
                                .show();
                    } else
                        selected = (Map<String, String>) adapterView.getItemAtPosition(i);
                }
            });
            list.setAdapter(adapter);
        }
    }

}
