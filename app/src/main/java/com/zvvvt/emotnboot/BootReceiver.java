package com.zvvvt.emotnboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();

        // 每次冷启动或升级后，停止云 OS 的第三方应用清理服务。
        VendorCleanupBlocker.stopWithRetries(appContext);

        // 保持唤醒监听服务运行。
        WakeWatchService.start(appContext);

        String action = intent == null ? null : intent.getAction();
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        // 冷启动完成后进入 Emotn。
        LaunchScheduler.schedule(appContext, 5_000L);
    }
}
