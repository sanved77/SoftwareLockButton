package com.sanved.instantlockbutton;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

/**
 * Created by Sanved on 22-07-2017.
 */

public class FloatingService extends Service {

    private BubblesManager bubblesManager;
    DevicePolicyManager dpm;
    AnalyticsApplication application;
    private Tracker mTracker;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Analytics
        application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("FloatingService");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        initializeBubblesManager();
        return super.onStartCommand(intent, flags, startId);
    }

    private void addNewBubble() {
        BubbleLayout bubbleView = (BubbleLayout) LayoutInflater.from(FloatingService.this).inflate(R.layout.bubble, null);
        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Usage")
                        .setAction("Bubble Removed")
                        .build());
                stopForeground(true);
                stopSelf();
            }
        });
        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {

            @Override
            public void onBubbleClick(BubbleLayout bubble) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Usage")
                        .setAction("Device Locked")
                        .build());
                dpm.lockNow();
            }
        });
        bubbleView.setShouldStickToWall(true);
        bubblesManager.addBubble(bubbleView, 60, 90);
        startForeground(6969, createNotification());
    }

    private void initializeBubblesManager() {
        bubblesManager = new BubblesManager.Builder(this)
                .setTrashLayout(R.layout.trash)
                .setInitializationCallback(new OnInitializedCallback() {
                    @Override
                    public void onInitialized() {
                        addNewBubble();
                    }
                })
                .build();
        bubblesManager.initialize();
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("Software Lock Button is ON");
        builder.setContentText("Drag the bubble below to disable it.");
        builder.setOngoing(true);
        builder.setPriority(-2);
        builder.setCategory("service");
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, SetupScreen.class), 0));
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bubblesManager.recycle();
    }
}
