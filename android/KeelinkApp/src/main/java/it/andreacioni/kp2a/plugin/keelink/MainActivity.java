package it.andreacioni.kp2a.plugin.keelink;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private KeeLink keeLink = new KeeLink(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Activity activity = this;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                startScanActivity();
            }
        });


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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String content = result.getContents();
            if (content != null) {
                Log.d(TAG, "Scanned");
                if (content.startsWith(KeeLink.QR_CODE_PREFIX)) {
                    content = content.substring(KeeLink.QR_CODE_PREFIX.length());
                    Log.d(TAG, "Valid code scanned:" + content);
                    validSidReceived(content);
                } else {
                    Log.e(TAG, "Invalid code:" + content);
                    Toast.makeText(this, "Invalid QR code scanned!", Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

        private void validSidReceived(String sid) {

            if(keeLink.checkNetworkConnection()) {
                keeLink.sendKey(sid, "pippo", new AsyncPostResponse() {
                    @Override
                    public void response(boolean result) {
                        if(result)
                            Toast.makeText(getApplicationContext(),"Password correctly sent", Toast.LENGTH_SHORT);
                        else
                            Toast.makeText(getApplicationContext(),"Password NOT correctly sent. Try again...", Toast.LENGTH_SHORT);

                    }
                });
            } else
                Toast.makeText(this,"No network connection available!",Toast.LENGTH_SHORT);

        }

    private void startScanActivity() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CustomCaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }
}
