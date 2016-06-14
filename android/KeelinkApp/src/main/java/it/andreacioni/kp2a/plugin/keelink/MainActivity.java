package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONException;
import org.json.JSONObject;

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

    private Map<String, String> selected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setupProgressDialog();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new RecentActivityLoader(progressDialog, (ListView) findViewById(R.id.recent_list)).execute();
        prepareListView();

        if (snackStatusShow()) {
            if (savedInstanceState == null) {//condition ensure that the capture activity starts correctly TODO check this solution...
                Intent i = getIntent();
                if ((i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA) != null) &&
                        i.getStringExtra(Strings.EXTRA_ENTRY_ID) != null) {

                    Log.d(TAG, i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA));
                    Log.d(TAG, "Entry GUID: " + i.getStringExtra(Strings.EXTRA_ENTRY_ID));

                    try {
                        passwordReceived = new JSONObject(i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).getString(KeepassDefs.PasswordField);
                    } catch (JSONException e) {
                        Log.e(TAG, "Password parsing error" + e.getMessage());
                    }

                    new AsyncPostTask.AsyncSavePreferencesTask(progressDialog, i.getStringExtra(Strings.EXTRA_ENTRY_ID), i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).execute();
                    startScanActivity();
                }
            } else {
                passwordReceived = savedInstanceState.getString(Strings.EXTRA_ENTRY_OUTPUT_DATA);
                KeelinkDefs.setFastFlag(this,false);
            }
        }
    }

    private void prepareListView() {
        ListView lv = (ListView) findViewById(R.id.recent_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, adapterView.getItemAtPosition(i).toString());
                if (selected != null && selected.equals((Map<String, String>) adapterView.getItemAtPosition(i))) {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Just another click")
                            .setContentText("Click on green button below to open the Keepass entry")
                            .setConfirmText("Ok!")
                            .show();
                } else {
                    selected = (Map<String, String>) adapterView.getItemAtPosition(i);
                    KeelinkDefs.setFastFlag(getApplicationContext(),true);
                }
            }
        });
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

        if (passwordReceived != null) {
            outState.putString(Strings.EXTRA_ENTRY_OUTPUT_DATA, passwordReceived);
            //outState.putString(Strings.EXTRA_FIELD_ID, launcherIntent.getStringExtra(Strings.EXTRA_FIELD_ID));
            //outState.putString(Strings.EXTRA_SENDER, launcherIntent.getStringExtra(Strings.EXTRA_SENDER));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        snackStatusShow();

        ((ListView) findViewById(R.id.recent_list)).setSelection(ListView.INVALID_POSITION);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            case R.id.howto:
                openBrowserWithUrl(KeelinkDefs.TARGET_SITE + "/#howto?onlyinfo=true");
                break;
            case R.id.about:
                openBrowserWithUrl(KeelinkDefs.TARGET_SITE + "/#credits?onlyinfo=true");
                break;
            case R.id.enable_plugin:
                enableDisablePlugin();
                break;
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
                //startScanActivity();
                //LoadingOverlayFragment loading = new LoadingOverlayFragment();
                //loading.show(getSupportFragmentManager(),"Title");
                //Intent i = Kp2aControl.getOpenEntryIntent("",false,true);
                //startActivityForResult(i,399)

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
                                    KeelinkDefs.setFastFlag(getApplicationContext(),true);
                                    openKeepass();
                                }
                            })
                            .show();
                } else if ("0".equals(selected.get(KeelinkDefs.GUID_FIELD).toString())) {
                    Log.d(TAG, "Opening K2PA...");
                    openKeepass();
                } else {
                    Log.d(TAG, "Sending entry...");
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

        if (title != null && !title.trim().isEmpty())
            ret += title.trim() + " ";

        if (user != null && !user.substring(KeepassDefs.UserNameField.length() + 1).trim().isEmpty())
            ret += user.substring(KeepassDefs.UserNameField.length() + 1).trim() + " ";

        /*if(note != null && note.substring(KeepassDefs.NotesField.length()+1).trim().isEmpty())
            ret += note.substring(KeepassDefs.NotesField.length()+1).trim() + " ";*/

        return ret.trim();
    }

    private void openKeepass() {
        openKeepassForSearch("");
    }

    private void openKeepassForSearch(String searchText) {
        Intent i = Kp2aControl.getOpenEntryIntent(searchText, false, true);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult reqCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && passwordReceived != null) {
            String content = result.getContents();

            if (content != null) {

                Log.d(TAG, "Scanned");
                Log.d(TAG, "Pass:" + passwordReceived);

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
                                .setContentText("Password was sent, wait the arriving on your page!").show();
                    }
                    else {
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("There was an error comunicating with the server, try again.").show();
                    }

                }
            });
        }

    }

}
