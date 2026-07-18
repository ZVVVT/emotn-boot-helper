package com.zvvvt.emotnboot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public final class LaunchActivity extends Activity {
    private static final String EMOTN_PACKAGE = "com.oversea.aslauncher";
    private static final String EMOTN_ACTIVITY =
            "com.oversea.aslauncher.ui.main.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            WakeWatchService.start(getApplicationContext());
        } catch (RuntimeException ignored) {
        }

        openEmotn();
        finish();
        overridePendingTransition(0, 0);
    }

    private void openEmotn() {
        Intent emotnIntent = new Intent(Intent.ACTION_MAIN);
        emotnIntent.setComponent(new ComponentName(EMOTN_PACKAGE, EMOTN_ACTIVITY));
        emotnIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION
        );

        try {
            startActivity(emotnIntent);
            return;
        } catch (RuntimeException ignored) {
        }

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        );

        try {
            startActivity(homeIntent);
        } catch (RuntimeException ignored) {
        }
    }
}
