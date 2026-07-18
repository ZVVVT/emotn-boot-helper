package com.zvvvt.emotnboot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

/**
 * 延迟启动 Emotn。
 *
 * v1.4 不再通过本应用的 LaunchActivity 中转，避免助手 Activity
 * 短暂成为前台后被云 OS AppDaemon 识别并 force-stop 整个助手包。
 */
public final class LaunchScheduler {
    private static final String TAG = "EmotnBootHelper";

    // v1.1-v1.3 使用的旧 LaunchActivity PendingIntent。
    private static final int LEGACY_REQUEST_CODE = 3005;

    // v1.4 直接指向 Emotn 的 PendingIntent，使用新的 requestCode 避免身份冲突。
    private static final int DIRECT_REQUEST_CODE = 3006;

    private static final String EMOTN_PACKAGE = "com.oversea.aslauncher";
    private static final String EMOTN_ACTIVITY =
            "com.oversea.aslauncher.ui.main.MainActivity";

    private LaunchScheduler() {
    }

    public static void schedule(Context context, long delayMs) {
        Context appContext = context.getApplicationContext();
        AlarmManager alarmManager =
                (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager unavailable; direct Emotn launch not scheduled");
            return;
        }

        // 覆盖升级时清除 v1.3 可能遗留的 LaunchActivity 定时任务。
        cancelLegacyLaunchActivity(appContext, alarmManager);

        Intent emotnIntent = new Intent(Intent.ACTION_MAIN);
        emotnIntent.setComponent(new ComponentName(EMOTN_PACKAGE, EMOTN_ACTIVITY));
        emotnIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION
        );

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                appContext,
                DIRECT_REQUEST_CODE,
                emotnIntent,
                flags
        );

        long safeDelayMs = Math.max(delayMs, 0L);
        long triggerAt = SystemClock.elapsedRealtime() + safeDelayMs;

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

        Log.i(TAG, "scheduled direct Emotn launch in " + safeDelayMs + "ms");
    }

    private static void cancelLegacyLaunchActivity(
            Context context,
            AlarmManager alarmManager
    ) {
        Intent legacyIntent = new Intent(context, LaunchActivity.class);
        legacyIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION
        );

        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent legacyPendingIntent = PendingIntent.getActivity(
                context,
                LEGACY_REQUEST_CODE,
                legacyIntent,
                flags
        );

        if (legacyPendingIntent == null) {
            return;
        }

        alarmManager.cancel(legacyPendingIntent);
        legacyPendingIntent.cancel();
        Log.i(TAG, "cancelled legacy LaunchActivity alarm");
    }
}
