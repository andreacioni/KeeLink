package it.andreacioni.kp2a.plugin.keelink;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import keepass2android.pluginsdk.AccessManager;
import keepass2android.pluginsdk.Strings;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ACTIVITY_ENABLE_DISABLE = 123;

    private ProgressDialog progressDialog = null;
    private String passwordReceived = null;
    private KeeLink keeLink = new KeeLink(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean status = snackStatusShow();

        if(status && savedInstanceState == null) { //last condition ensure that the capture activity starts correctly TODO check this solution...
            Intent i = getIntent();
            if((i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA) != null) &&
                    (i.getStringExtra(Strings.EXTRA_FIELD_ID) != null) &&
                    (i.getStringExtra(Strings.EXTRA_SENDER) != null)) {
                Log.d(TAG,i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA));
                try {
                    passwordReceived = new JSONObject(i.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA)).getString("Password");
                } catch (JSONException e) {
                    Log.e(TAG,"Password parsing error");
                }
                progressDialog.show();
                new DelayedLauncher(progressDialog).execute((Object[]) null);
            }
        } else {
            passwordReceived = savedInstanceState.getString(Strings.EXTRA_ENTRY_OUTPUT_DATA);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(passwordReceived != null) {
            outState.putString(Strings.EXTRA_ENTRY_OUTPUT_DATA, "pippa");
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
            }
        });

        return ret;
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
        Log.d(TAG,"onActivityResult");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && passwordReceived != null) {
            String content = result.getContents();

            if (content != null) {

                Log.d(TAG, "Scanned");
                Log.d(TAG,"Pass:" + passwordReceived);

                if (content.startsWith(KeeLink.QR_CODE_PREFIX)) {
                    content = content.substring(KeeLink.QR_CODE_PREFIX.length());
                    Log.d(TAG, "Valid code scanned:" + content);
                    validSidReceived(content,"pippo");
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

        if(keeLink.checkNetworkConnection()) {
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

    private class DelayedLauncher extends AsyncTask {
        private ProgressDialog dialog = null;

        private DelayedLauncher(ProgressDialog d) {
            this.dialog = d;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            startScanActivity();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            dialog.cancel();
            dialog.dismiss();
        }
    }

}
