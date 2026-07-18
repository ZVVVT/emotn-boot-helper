package com.zvvvt.emotnboot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * 停止云 OS 的第三方应用清理服务。
 *
 * com.yunos.tvmgr.service.STRCheckService 在 SCREEN_OFF 时会强制停止
 * Emotn 和本助手。该服务在系统清单中 exported=true，且未声明调用权限，
 * 因此普通应用可以通过显式 Intent 请求停止它。
 *
 * 不禁用 com.yunos.tvmgr 整包，只停止这个独立服务。
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

    private VendorCleanupBlocker() {
    }

    public static void stopWithRetries(Context context) {
        final Context appContext = context.getApplicationContext();
        final Handler handler = new Handler(Looper.getMainLooper());

        for (long delayMs : RETRY_DELAYS_MS) {
            handler.postDelayed(
                    () -> stopOnce(appContext),
                    delayMs
            );
        }
    }

    public static boolean stopOnce(Context context) {
        Intent intent = new Intent();
        intent.setComponent(STR_SERVICE);

        try {
            boolean stopped = context.stopService(intent);
            Log.i(TAG, "stop STRCheckService result=" + stopped);
            return stopped;
        } catch (SecurityException error) {
            Log.e(TAG, "No permission to stop STRCheckService", error);
        } catch (RuntimeException error) {
            Log.e(TAG, "Failed to stop STRCheckService", error);
        }

        return false;
    }
}
