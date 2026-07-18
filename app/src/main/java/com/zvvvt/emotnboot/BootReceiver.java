package com.zvvvt.emotnboot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

/**
 * 开机广播接收器。
 *
 * 收到系统开机广播后，等待 5 秒，再由系统 AlarmManager 拉起 LaunchActivity。
 * 使用 PendingIntent 比直接在后台广播中启动 Activity 更稳妥。
 */
public final class BootReceiver extends BroadcastReceiver {

    private static final long DELAY_MS = 5_000L;
    private static final int REQUEST_CODE = 3005;

    @Override
    public void onReceive(Context context, Intent intent) {
        scheduleLaunch(context.getApplicationContext());
    }

    private static void scheduleLaunch(Context context) {
        Intent launchIntent = new Intent(context, LaunchActivity.class);
        launchIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION
        );

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                launchIntent,
                flags
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        long triggerAt = SystemClock.elapsedRealtime() + DELAY_MS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt,
                    pendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt,
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAt,
                    pendingIntent
            );
        }
    }
}
