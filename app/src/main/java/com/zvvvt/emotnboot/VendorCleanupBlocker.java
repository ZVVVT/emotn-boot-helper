package com.zvvvt.emotnboot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * 持续阻止云 OS 的第三方应用清理服务。
 *
 * 只停止：
 * com.yunos.tvmgr/com.yunos.tvmgr.service.STRCheckService
 *
 * 不禁用 com.yunos.tvmgr 整包。
 */
public final class VendorCleanupBlocker {

    private static final String TAG = "EmotnBootHelper";
    private static final ComponentName STR_SERVICE = new ComponentName(
            "com.yunos.tvmgr",
            "com.yunos.tvmgr.service.STRCheckService"
    );

    private static final long[] RETRY_DELAYS_MS = {
            0L,
            2_000L,
            8_000L,
            20_000L
    };

    // 厂商服务会在开机约一分钟后重新拉起，因此必须持续看守。
    private static final long WATCH_INTERVAL_MS = 1_000L;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static Context watchdogContext;
    private static boolean watchdogRunning;

    private static final Runnable WATCHDOG_RUNNABLE = new Runnable() {
        @Override
        public void run() {
            Context context;

            synchronized (VendorCleanupBlocker.class) {
                if (!watchdogRunning || watchdogContext == null) {
                    return;
                }
                context = watchdogContext;
            }

            stopOnce(context, false);

            synchronized (VendorCleanupBlocker.class) {
                if (watchdogRunning) {
                    MAIN_HANDLER.postDelayed(this, WATCH_INTERVAL_MS);
                }
            }
        }
    };

    private VendorCleanupBlocker() {
    }

    /**
     * 开机、升级或唤醒时的短期补停。
     */
    public static void stopWithRetries(Context context) {
        final Context appContext = context.getApplicationContext();

        for (long delayMs : RETRY_DELAYS_MS) {
            MAIN_HANDLER.postDelayed(
                    () -> stopOnce(appContext, false),
                    delayMs
            );
        }
    }

    /**
     * 由前台服务持有，持续检查厂商服务是否被重新拉起。
     */
    public static synchronized void startWatchdog(Context context) {
        watchdogContext = context.getApplicationContext();

        if (watchdogRunning) {
            return;
        }

        watchdogRunning = true;
        MAIN_HANDLER.removeCallbacks(WATCHDOG_RUNNABLE);
        MAIN_HANDLER.post(WATCHDOG_RUNNABLE);
        Log.i(TAG, "STRCheckService watchdog started, interval="
                + WATCH_INTERVAL_MS + "ms");
    }

    public static synchronized void stopWatchdog() {
        if (!watchdogRunning) {
            return;
        }

        watchdogRunning = false;
        watchdogContext = null;
        MAIN_HANDLER.removeCallbacks(WATCHDOG_RUNNABLE);
        Log.i(TAG, "STRCheckService watchdog stopped");
    }

    public static boolean stopOnce(Context context) {
        return stopOnce(context, true);
    }

    private static boolean stopOnce(Context context, boolean logNoop) {
        Intent intent = new Intent();
        intent.setComponent(STR_SERVICE);

        try {
            boolean stopped = context.stopService(intent);

            // 持续看守时不记录大量 result=false，只记录真正停止成功。
            if (stopped) {
                Log.i(TAG, "stop STRCheckService result=true");
            } else if (logNoop) {
                Log.i(TAG, "stop STRCheckService result=false");
            }

            return stopped;
        } catch (SecurityException error) {
            Log.e(TAG, "No permission to stop STRCheckService", error);
        } catch (RuntimeException error) {
            Log.e(TAG, "Failed to stop STRCheckService", error);
        }

        return false;
    }
}
