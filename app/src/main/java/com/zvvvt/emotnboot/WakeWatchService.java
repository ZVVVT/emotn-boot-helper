package com.zvvvt.emotnboot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

public final class WakeWatchService extends Service {
    private static final String CHANNEL_ID = "emotn_boot_helper";
    private static final int NOTIFICATION_ID = 1101;
    private static final long WAKE_DELAY_MS = 2_000L;
    private static final long DEBOUNCE_MS = 5_000L;

    private long lastWakeElapsed = -DEBOUNCE_MS;
    private boolean receiverRegistered = false;

    private final BroadcastReceiver wakeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent == null ? null : intent.getAction();

            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // 待机前立即补停，不在此时打开任何界面。
                VendorCleanupBlocker.stopWithRetries(getApplicationContext());
                return;
            }

            if (!Intent.ACTION_SCREEN_ON.equals(action)
                    && !Intent.ACTION_USER_PRESENT.equals(action)
                    && !Intent.ACTION_DREAMING_STOPPED.equals(action)) {
                return;
            }

            long now = SystemClock.elapsedRealtime();
            if (now - lastWakeElapsed < DEBOUNCE_MS) {
                return;
            }

            lastWakeElapsed = now;

            // 唤醒时先补停，再延迟返回 Emotn，避免与酷喵桌面抢启动时序。
            VendorCleanupBlocker.stopWithRetries(getApplicationContext());
            LaunchScheduler.schedule(getApplicationContext(), WAKE_DELAY_MS);
        }
    };

    public static void start(Context context) {
        Intent serviceIntent = new Intent(context, WakeWatchService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startAsForeground();
        registerWakeReceiver();
        VendorCleanupBlocker.startWatchdog(getApplicationContext());
        VendorCleanupBlocker.stopWithRetries(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startAsForeground();
        registerWakeReceiver();
        VendorCleanupBlocker.startWatchdog(getApplicationContext());
        VendorCleanupBlocker.stopWithRetries(getApplicationContext());
        return START_STICKY;
    }

    private void registerWakeReceiver() {
        if (receiverRegistered) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_DREAMING_STOPPED);

        registerReceiver(wakeReceiver, filter);
        receiverRegistered = true;
    }

    private void startAsForeground() {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emotn 唤醒监听",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("用于遥控器唤醒后自动返回 Emotn");
            channel.setShowBadge(false);
            manager.createNotificationChannel(channel);
        }

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        Notification notification = builder
                .setContentTitle("Emotn 开机助手")
                .setContentText("正在监听开机与唤醒")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setOngoing(true)
                .setShowWhen(false)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        VendorCleanupBlocker.stopWatchdog();

        if (receiverRegistered) {
            try {
                unregisterReceiver(wakeReceiver);
            } catch (RuntimeException ignored) {
            }
            receiverRegistered = false;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
