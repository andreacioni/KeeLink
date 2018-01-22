package it.andreacioni.kp2a.plugin.keelink.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import it.andreacioni.kp2a.plugin.keelink.asynctask.AsyncPostTask.AsyncPostResponse;
import it.andreacioni.kp2a.plugin.keelink.asynctask.AsyncSavePreferencesTask;
import it.andreacioni.kp2a.plugin.keelink.keelink.KeeLink;
import it.andreacioni.kp2a.plugin.keelink.keelink.KeeLinkUtils;
import it.andreacioni.kp2a.plugin.keelink.keelink.KeelinkDefs;
import it.andreacioni.kp2a.plugin.keelink.R;
import it.andreacioni.kp2a.plugin.keelink.asynctask.RecentActivityLoader;
import it.andreacioni.kp2a.plugin.keelink.preferences.KeelinkPreferences;
import keepass2android.pluginsdk.AccessManager;
import keepass2android.pluginsdk.KeepassDefs;
import keepass2android.pluginsdk.Kp2aControl;
import keepass2android.pluginsdk.Strings;

public class MainActivity extends AppCompatActivity implements ActionMode.Callback{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ACTIVITY_ENABLE_DISABLE = 123;
    private static final int START_KEEPASS_CODE = 399;

    private String passwordReceived = null;
    private KeeLink keeLink = new KeeLink(this);

    private Map<String, String> selected = null;

    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prepareListView();

        if (snackStatusShow()) {
            if (savedInstanceState == null) {// this condition ensure that the capture activity starts correctly TODO check this solution...
                Intent i = getIntent();

                Log.d(TAG, "Intent is: " + i);

                if (i.getExtras() != null && (i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA) != null) &&
                        i.getStringExtra(Strings.EXTRA_ENTRY_ID) != null) {

                    Log.i(TAG,"Password received from intent!");

                    try {
                        passwordReceived = new JSONObject(i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).getString(KeepassDefs.PasswordField);
                    } catch (JSONException e) {
                        Log.e(TAG, "Password parsing error" + e.getMessage());
                    }

                    new AsyncSavePreferencesTask(MainActivity.this,i.getStringExtra(Strings.EXTRA_ENTRY_ID), i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).execute();
                    startScanActivity();
                }
            } else {
                Log.i(TAG,"Password received from savedInstanceState!");
                passwordReceived = savedInstanceState.getString(Strings.EXTRA_ENTRY_OUTPUT_DATA);

            }
        } else {
            KeeLinkUtils.setFastFlag(getApplicationContext(),false);
        }
    }


    private void prepareListView() {

        ListView lv = (ListView) findViewById(R.id.recent_list);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                Log.d(TAG, adapterView.getItemAtPosition(i).toString());

                if (selected != null && selected.equals((Map<String, String>) adapterView.getItemAtPosition(i))) {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Just another click")
                            .setContentText("Click on green button below to open the Keepass entry")
                            .setConfirmText("Ok!")
                            .show();
                } else {
                    selected = (Map<String, String>) adapterView.getItemAtPosition(i);
                    if(selected.get(KeelinkDefs.GUID_FIELD) != "0") {
                        if(mActionMode == null)
                            mActionMode = startSupportActionMode(MainActivity.this);
                        else
                            Log.e(TAG, "mActionMode is not null");
                    } else
                        Log.w(TAG,"Cannot select placeholder entry");
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (passwordReceived != null) {
            outState.putString(Strings.EXTRA_ENTRY_OUTPUT_DATA, passwordReceived);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //This ensure action mode to terminate when coming back to KeeLink
        if(mActionMode != null) {
            mActionMode.finish();
        }

        supportInvalidateOptionsMenu();

        ListView l = ((ListView) findViewById(R.id.recent_list));
        if(l != null) {
            l.setSelection(ListView.INVALID_POSITION);
            reloadList();
        } else
            Log.w(TAG,"Cannot invalidate position in list");
        snackStatusShow();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        prepareMenu(menu);

        return true;
    }

    private void prepareMenu(Menu m) {
        if(m != null) {
            SwitchCompat switchh = (SwitchCompat) ((MenuItemImpl) m.findItem(R.id.menu_item_switch)).getActionView().findViewById(R.id.pluginEnabledSwitch);
            if(isKeepassInstalled()) {
                switchh.setEnabled(true);
                switchh.setChecked(isEnabled());
                switchh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        enableDisablePlugin();
                    }
                });
            } else
                switchh.setChecked(false);

            if(KeelinkPreferences.getBoolean(getApplicationContext(), KeelinkPreferences.FLAG_FAST_ENABLE)) {
                MenuItem item = m.findItem(R.id.fast_flag);
                item.setChecked(true);
            }

            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                String version = pInfo.versionName;
                m.findItem(R.id.menuVersion).setTitle("v:" + version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Log.d(TAG,"onOptionsItemSelected, option ID:" + id);

        switch (id) {
            case R.id.fast_flag:
                if(item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }

                KeelinkPreferences.setBoolean(getApplicationContext(), KeelinkPreferences.FLAG_FAST_ENABLE, item.isChecked());
                break;
            case R.id.go_online:
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("KeeLink")
                    .setContentText("The other part of this application is placed online on https://keelink.cloud")
                    .show();
                break;
            case R.id.howto:
                openBrowserWithUrl(KeelinkDefs.TARGET_SITE + "/?show=howto&onlyinfo=true");
                break;
            case R.id.about:
                openBrowserWithUrl(KeelinkDefs.TARGET_SITE + "/?show=credits&onlyinfo=true");
                break;
            case R.id.privacy:
                openBrowserWithUrl(KeelinkDefs.TARGET_SITE + "/privacy-policy.html");
                break;
            default:
                Log.w(TAG,"Not a valid ID");
        }

        return super.onOptionsItemSelected(item);
    }

    private void openBrowserWithUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private boolean enableDisablePlugin() {

        try {
            Intent i = new Intent(Strings.ACTION_EDIT_PLUGIN_SETTINGS);
            i.putExtra(Strings.EXTRA_PLUGIN_PACKAGE, getPackageName());
            startActivityForResult(i, ACTIVITY_ENABLE_DISABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean snackStatusShow() {
        boolean ret = false;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(isKeepassInstalled()) {
            if (!isEnabled()) {
                Snackbar.make(fab, "Not enabled as plugin", Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
            } else {
                if (!KeeLink.checkNetworkConnection(this))
                    Snackbar.make(fab, "No network connection", Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
                else {
                    Snackbar.make(fab, "Ready!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    ret = true;
                }
            }
        } else {
            Snackbar.make(fab, "Keepass2Android not found", Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
            fab.hide();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(snackStatusShow()) {
                    if (selected == null) {
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Hmmm not understanding...")
                                .setContentText("No selection made, would you open Keepass2Android?")
                                .setConfirmText("Yes")
                                .setCancelText("Cancel")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        KeeLinkUtils.setFastFlag(getApplicationContext(),true);
                                        openKeepass();
                                    }
                                })
                                .show();
                    } else if ("0".equals(selected.get(KeelinkDefs.GUID_FIELD).toString())) {
                        Log.d(TAG, "Opening K2PA...");

                        KeeLinkUtils.setFastFlag(getApplicationContext(), true);
                        openKeepass();
                    } else {
                        Log.d(TAG, "Sending entry...");

                        String searchString = prepareSearchText(selected);

                        KeeLinkUtils.setFastFlag(getApplicationContext(), true);
                        openKeepassForSearch(searchString);
                    }
                }
            }
        });

        return ret;
    }

    private String prepareSearchText(Map<String, String> selected) {
        String ret = "";
        String title = selected.get(KeepassDefs.TitleField);
        String user = selected.get(KeelinkDefs.USERNAME_VALID_FIELD);

        Log.d(TAG,"Searching for -> TITLE:" + title + ", USER:" + user);

        if (title != null && !title.isEmpty() && !title.equals(KeelinkDefs.STR_NOT_SUPPLIED)) {
            title = title.trim();
            ret += title.trim() + " ";
        }

        if (user != null && !user.isEmpty() && !user.equals(KeelinkDefs.STR_NOT_SUPPLIED)) {
            user = user.substring(KeelinkDefs.USERNAME_VALID_FIELD.length() + 1).trim();
            ret += user + " ";
        }

        /*if(note != null && note.substring(KeepassDefs.NotesField.length()+1).trim().isEmpty())
            ret += note.substring(KeepassDefs.NotesField.length()+1).trim() + " ";*/

        return ret.trim();
    }

    private void openKeepass() {
        openKeepassForSearch("");
    }

    private void openKeepassForSearch(String searchText) {
        //If fast flag is enabled so we need to close after open
        boolean flagFastEnable = KeelinkPreferences.getBoolean(getApplicationContext(), KeelinkPreferences.FLAG_FAST_ENABLE);
        Intent i = Kp2aControl.getOpenEntryIntent(searchText, false, flagFastEnable);
        startActivityForResult(i, START_KEEPASS_CODE);
    }

    private boolean isEnabled() {
        return !AccessManager.getAllHostPackages(this).isEmpty();
    }

    private boolean isKeepassInstalled() {
        PackageManager pm = getPackageManager();
        boolean app_installed;

        //Check online
        try {
            pm.getPackageInfo("keepass2android.keepass2android", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e1) {
            //Check offline
            try {
                pm.getPackageInfo("keepass2android.keepass2android_nonet", PackageManager.GET_ACTIVITIES);
                app_installed = true;
            }
            catch (PackageManager.NameNotFoundException e2) {
                app_installed = false;
            }
        }

        return app_installed;
    }


    private void startScanActivity() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CustomCaptureActivity.class);
        integrator.setOrientationLocked(false);

        integrator.initiateScan();
    }

    private void reloadList() {
        Log.d(TAG,"Reloading list...");
        ListView lv = (ListView) findViewById(R.id.recent_list);
        new RecentActivityLoader(MainActivity.this,lv).execute();
    }

    private void removeSelectedEntry() {
        try {
            JSONArray jArray = new JSONArray(KeelinkPreferences.getString(getApplicationContext(), KeelinkPreferences.RECENT_PREFERENCES_ENTRY));
        } catch (JSONException e) { Log.e(TAG,"Not a valid selected item" + selected.toString()); }
        String id = selected.get(KeelinkDefs.GUID_FIELD);
        JSONObject obj = new JSONObject(selected);

        Log.d(TAG,"Removing object: " + obj.toString());

        new AsyncSavePreferencesTask(this,id,obj.toString(),true).execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult reqCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && passwordReceived != null) {
            String content = result.getContents();

            if (content != null) {

                Log.d(TAG, "Scanned");
                if (content.startsWith(KeeLink.QR_CODE_PREFIX)) {
                    content = content.substring(KeeLink.QR_CODE_PREFIX.length());
                    Log.d(TAG, "Valid code scanned:" + content);
                    validSidReceived(content, passwordReceived);
                } else {
                    Log.e(TAG, "Invalid code:" + content);
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("This is not a KeeLink QR code!").show();
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }

        passwordReceived = null;

    }

    private void validSidReceived(String sid, String password) {

        if (snackStatusShow()) {
            keeLink.sendKey(sid, password, new AsyncPostResponse() {
                @Override
                public void response(boolean result) {
                    if (result) {
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("OK")
                                .setContentText("Password was sent, wait the arriving on your page!")
                                .show();
                    }
                    else {
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("There was an error comunicating with the server, try again.")
                                .show();
                    }

                }
            });
        }

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_list_selection, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.delete_menu_entry:
                removeSelectedEntry();
                mActionMode.finish();
                return true;
        }

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        reloadList();
        selected = null;
        mActionMode = null;
    }
}
