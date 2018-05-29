package com.melodispel.dpgame.reminders;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.melodispel.dpgame.MainActivity;
import com.melodispel.dpgame.R;
import com.melodispel.dpgame.data.DPGamePreferences;
import com.melodispel.dpgame.utitlities.DPGameTimeUtils;

import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.Action;
import static android.support.v4.app.NotificationCompat.Builder;
import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;


public class ReminderTasks {

    private static final int PLAY_REMINDER_PENDING_INTENT_ID = 12980;
    private static final int PLAY_NOTIFICATION_ID = 28390;
    private static final int ACTION_PLAY_NOW = 24524;
    private static final int ACTION_CANCEL_NOTIFICATION = 40898;

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
        long millisSinceLastPlay = DPGameTimeUtils.getMillisPassedSinceLastGame(context);

        Log.i("ReminderRasks", "Minutes since last play: " + TimeUnit.MILLISECONDS.toMinutes(millisSinceLastPlay));

        if (millisSinceLastPlay < 0) { // never played before
            millisSinceLastPlay = DPGameTimeUtils.getMillisPassedSinceTimeStamp(jobScheduledTimeMillis);
        }
        int interval = DPGamePreferences.getNotificationInterval(context);
        String unit = DPGamePreferences.getNotificationIntervalUnit(context);

        long timePassedSinceNotificationDue = getTimePassedSinceNotificationDue(millisSinceLastPlay, interval, unit);

        Log.i("ReminderTasks", "time passed since notification due: " + timePassedSinceNotificationDue +
                " " + unit.toString() +
        " interval: " + interval);

        if (timePassedSinceNotificationDue >= 0) {

            String contentText = context.getString(R.string.reminder_text_start)+ " " +
                    ReminderUtilities.getLocalizedStringForIntervalUnit(context, unit, (int)timePassedSinceNotificationDue);

            Builder notificationBuilder = new Builder(context)
            .setSmallIcon(R.drawable.ic_notification_play)
                    .setContentTitle(context.getString(R.string.reminder_title))
                    .setContentText(contentText)
                    .setContentIntent(playReminderContentIntent(context))
                    .setAutoCancel(true)
                    .setPriority(PRIORITY_DEFAULT)
                    .addAction(playNowAction(context))
                    .addAction(cancelPlayRemindersAction(context));

            NotificationManager notificationManager = (NotificationManager)context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(PLAY_NOTIFICATION_ID, notificationBuilder.build());
        }

    }

    private static PendingIntent playReminderContentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context,
                PLAY_REMINDER_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Action playNowAction(Context context) {
        Intent intent = new Intent(context, PlayReminderIntentService.class);
        intent.setAction(PlayReminderIntentService.PLAY_NOW);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                ACTION_PLAY_NOW, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Action action = new Action(R.drawable.ic_notification_play,
                context.getString(R.string.reminder_action_play), pendingIntent);
        return action;
    }

    private static Action  cancelPlayRemindersAction(Context context) {
        Intent intent = new Intent(context, PlayReminderIntentService.class);
        intent.setAction(PlayReminderIntentService.CANCEL_PLAY_REMINDER);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                ACTION_CANCEL_NOTIFICATION, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Action action = new Action(R.drawable.ic_notification_cancel,
                context.getString(R.string.reminder_action_cancel), pendingIntent);
        return action;
    }



    public static void cancelPlayNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PLAY_NOTIFICATION_ID);
    }

    public static void cancelPlayingReminder(Context context) {
        ReminderUtilities.cancelFirebaseJobDispatcherForReminder(context);
        DPGamePreferences.setNotificationIntervalTime(context,
                context.getResources().getInteger(R.integer.pref_notification_interval_not_set));
        cancelPlayNotification(context);
    }

    private static long getTimePassedSinceNotificationDue(long millisSinceLastPlay, int notificationInterval, String unit) {

        long timePassedSinceNotificationDue;

        switch (unit) {
            case (ReminderUtilities.MINUTE):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toMinutes(millisSinceLastPlay) - notificationInterval;
                break;
            case (ReminderUtilities.HOUR):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toHours(millisSinceLastPlay) - notificationInterval;
                break;
            case (ReminderUtilities.DAY):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toDays(millisSinceLastPlay) - notificationInterval;
                break;
            case (ReminderUtilities.WEEK):
                timePassedSinceNotificationDue = TimeUnit.MILLISECONDS.toDays(millisSinceLastPlay) - (notificationInterval * 7);
                break;

            default:
                timePassedSinceNotificationDue = -1;
                break;
        }

            return timePassedSinceNotificationDue;
    }
}
