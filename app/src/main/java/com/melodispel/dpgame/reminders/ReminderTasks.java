package com.melodispel.dpgame.reminders;

import android.content.Context;

import com.melodispel.dpgame.data.DPGamePreferences;
import com.melodispel.dpgame.utitlities.DPGameTimeUtils;

import java.util.concurrent.TimeUnit;


public class ReminderTasks {

    /**
     *
     * @param context
     * @param jobScheduledTimeMillis The time when the job for checking for reminders was scheduled
     *                               Used to deal with cases when the player never played before,
     *                               but a reminder for playing is still due
     */
    public static void remindOfPlaying(Context context, long jobScheduledTimeMillis) {

        // Check if it is time to remind the user of playing
        // retrieve notification interval from preferences: if it is e.g. 1 day, and 1 day or more
        // has passed since last response was saved, issue notification

        if (isTimeToRemindOfPlaying(context, jobScheduledTimeMillis)) {

            // Show notification...
        }

    }

    private static boolean isTimeToRemindOfPlaying(Context context, long jobScheduledTimeMillis) {
        long millisSinceLastPlay = DPGameTimeUtils.getMillisPassedSinceLastGame(context);

        if (millisSinceLastPlay < 0) { // never played before
            millisSinceLastPlay = DPGameTimeUtils.getMillisPassedSinceTimeStamp(jobScheduledTimeMillis);
        }
        int interval = DPGamePreferences.getNotificationInterval(context);
        String unit = DPGamePreferences.getNotificationIntervalUnit(context);
        long timePassedSinceNotificationDue;

        switch (unit) {
            case (ReminderUtilities.MINUTE):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toMinutes(millisSinceLastPlay) - interval;
                break;
            case (ReminderUtilities.HOUR):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toHours(millisSinceLastPlay) - interval;
                break;
            case (ReminderUtilities.DAY):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toDays(millisSinceLastPlay) - interval;
                break;
            case (ReminderUtilities.WEEK):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toDays(millisSinceLastPlay) - (interval * 7);
                break;

            default:
                timePassedSinceNotificationDue = -1;
                break;
        }

            return (timePassedSinceNotificationDue >= 0);
    }
}
