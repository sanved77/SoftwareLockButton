package com.tapkeer.screenonoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Sanved on 22-07-2017.
 */

public class FingerprintPrompt extends AppCompatActivity {

    Button uninstall, useit;
    AnalyticsApplication application;
    private Tracker mTracker;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint);

        uninstall = (Button) findViewById(R.id.uninstall);
        useit = (Button) findViewById(R.id.useit);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ed = prefs.edit();

        // Analytics
        application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("FingerPrint");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("FingerUsage")
                        .setAction("FingerUninstall" + Build.MODEL + " " + Build.DEVICE )
                        .build());
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });

        useit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("FingerUsage")
                        .setAction("FingerUse" + Build.MODEL + " " + Build.DEVICE )
                        .build());
                Toast.makeText(FingerprintPrompt.this, "Thank you !", Toast.LENGTH_SHORT).show();
                ed.putBoolean("fingerUse",true).commit();
                finish();
            }
        });

    }
}
