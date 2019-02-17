package com.tapkeer.screenonoff;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.txusballesteros.bubbles.BubblesManager;

public class SetupScreen extends AppCompatActivity {

    private BubblesManager bubblesManager;
    DevicePolicyManager dpm;
    ComponentName cmp;
    Button b;
    TextView un,hint;
    AnalyticsApplication application;
    private Tracker mTracker;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    FingerprintManagerCompat fingerprintManagerCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_screen);

        b = (Button) findViewById(R.id.button);
        un = (TextView) findViewById(R.id.tvUn);
        hint = (TextView) findViewById(R.id.tvHint);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ed = prefs.edit();
        //timepass

        // Analytics
        application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("StartScreen");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        un.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder build = new AlertDialog.Builder(SetupScreen.this);

                build
                        .setTitle("Uninstall")
                        .setMessage("Do you really want to uninstall ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                mTracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("Usage")
                                        .setAction("Uninstall")
                                        .build());
                                dpm.removeActiveAdmin(cmp);
                                Intent intent = new Intent(Intent.ACTION_DELETE);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);

                            }
                        })
                        .setCancelable(true)
                        .setNegativeButton("No", null);

                build.show();
            }
        });


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean overlayAllowed = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(SetupScreen.this)) {
                        overlayAllowed = false;
                        checkDrawPermission();
                    }
                }

                if(overlayAllowed) {
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Usage")
                            .setAction("Enabled Button")
                            .build());
                    Intent i = new Intent(SetupScreen.this, FloatingService.class);
                    startService(i);
                    hint.setVisibility(View.VISIBLE);
                }
            }
        });

        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        cmp = new ComponentName(this, DeviceAdminReceiver.class);

        fingerprintManagerCompat = FingerprintManagerCompat.from(this);

        if (fingerprintManagerCompat.isHardwareDetected()) {
            boolean fingerUse = prefs.getBoolean("fingerUse",false);

            if(!fingerUse) {
                Intent i = new Intent (SetupScreen.this, FingerprintPrompt.class);
                startActivity(i);
            }
        }

        if (!dpm.isAdminActive(cmp))
            getAdminRights();


    }

    public void getAdminRights() {

        AlertDialog.Builder build2 = new AlertDialog.Builder(SetupScreen.this);
        build2
                .setTitle("Give Permission to the App")
                .setMessage("This app uses the Device Administrator permission. The permission is required to lock the screen of the phone." +
                        "On the next Screen select -" +
                        "\n\n\"Activate this device Administrator\"")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cmp);
                        startActivityForResult(intent, 69);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Setup")
                                .setAction("Canceled Admin")
                                .build());
                        Toast.makeText(SetupScreen.this, "Can't start app without this permissions", Toast.LENGTH_SHORT).show();
                        SetupScreen.this.finish();
                    }
                })
                .setCancelable(false);

        build2.create().show();


    }

    public void checkDrawPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {

                AlertDialog.Builder build2 = new AlertDialog.Builder(SetupScreen.this);
                LayoutInflater inflater2 = this.getLayoutInflater();
                final View dialogView2 = inflater2.inflate(R.layout.permission_dialog, null);

                build2
                        .setTitle("Just one last permission")
                        .setView(dialogView2)
                        .setMessage("This permission is required to display the small floating icon.")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, 100);
                            }
                        })
                        .setCancelable(false);

                build2.create().show();

            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (!Settings.canDrawOverlays(this)) {
                checkDrawPermission();

            } else {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Setup")
                        .setAction("Overlay Granted")
                        .build());
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 69) {
            if (resultCode == -1) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Setup")
                        .setAction("Admin Granted")
                        .build());
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkDrawPermission();
            } else {
                getAdminRights();
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
