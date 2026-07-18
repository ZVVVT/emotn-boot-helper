package com.zvvvt.emotnboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        WakeWatchService.start(appContext);

        String action = intent == null ? null : intent.getAction();
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        LaunchScheduler.schedule(appContext, 5_000L);
    }
}
