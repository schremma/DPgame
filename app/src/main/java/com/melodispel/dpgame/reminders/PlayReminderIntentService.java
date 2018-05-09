package com.melodispel.dpgame.reminders;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.melodispel.dpgame.MainActivity;

public class PlayReminderIntentService extends IntentService {

    public PlayReminderIntentService() {super("PlayReminderIntentService");}

    public static final String CANCEL_PLAY_REMINDER = "cancelPlayReminder";
    public static final String PLAY_NOW = "playNow";

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        Log.i("PlayReminderService", "on handle intent");

        if (action.equals(CANCEL_PLAY_REMINDER)) {
            Log.i("PlayReminderService", "canceling reminders");
            ReminderTasks.cancelPlayingReminder(this);
        } else if (action.equals(PLAY_NOW)) {
            ReminderTasks.cancelPlayNotification(this);
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
