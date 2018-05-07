package com.melodispel.dpgame.reminders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.melodispel.dpgame.R;
import com.melodispel.dpgame.utitlities.DPGameTimeUtils;

import java.util.concurrent.TimeUnit;

public class ReminderUtilities {

    public static final String REMINDER_JOB_TAG = "reminder-job-tag";
    public static final String EXTRA_SCHEDULING_TIME = "extra-scheduling-time";

    public static final String MINUTE = "minutes";
    public static final String HOUR = "hours";
    public static final String DAY = "days";
    public static final String WEEK = "weeks";
    public static final String[] intervalUnitList = new String[]{MINUTE, HOUR, DAY, WEEK};

    // Schedule a new job that periodically checks whether it's time to remind the user
    // the interval for checking should be modulated by the notification interval set by the user
    // If there is already a job scheduled for the same taks, it shoudl be canceled

    public static void scheduleFirebaseJobDispatcherForReminder(@NonNull Context context, int interval, String intervalUnit) {

        int startTime = getExecutionWindowStart(interval, intervalUnit);
        int startTimeInterval = getExecutionWindowLength(interval, intervalUnit);
        int endTime = startTime + startTimeInterval;

        Log.i("ReminderUtilities", "Scheduling firebase job with interval: " + startTime +
        "-" + endTime);

        Bundle extras = new Bundle();
        extras.putLong(EXTRA_SCHEDULING_TIME, DPGameTimeUtils.getTimeStampNow());

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);

        Job reminderJob = jobDispatcher.newJobBuilder()
                .setService(PlayReminderJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(startTime, endTime)) // start within the provided interval from now
                .setReplaceCurrent(true) // replaces existing job with the same tag
                .setExtras(extras)
                .build();

        jobDispatcher.schedule(reminderJob);
    }

    public static void cancelFirebaseJobDispatcherForReminder(@NonNull Context context) {

        Driver driver = new GooglePlayDriver(context);
        new FirebaseJobDispatcher(driver).cancel(REMINDER_JOB_TAG);

    }

    public static int getExecutionWindowStart(int interval, String intervalUnit) {

        int intervalSecs;

        switch (intervalUnit) {

            case MINUTE:
                intervalSecs = 15;
                break;
            case HOUR:
                if (interval <=24) {
                    intervalSecs = (int)TimeUnit.MINUTES.toSeconds(10);
                } else {
                    intervalSecs = (int)TimeUnit.MINUTES.toSeconds(60);
                }
                break;
            case DAY:
                if (interval <=2) {
                    intervalSecs = (int)TimeUnit.MINUTES.toSeconds(60);
                } else {
                    intervalSecs = (int)TimeUnit.HOURS.toSeconds(12);
                }
                break;
            case WEEK:
                intervalSecs = (int)TimeUnit.HOURS.toSeconds(12);
                break;
            default:
                throw new IllegalArgumentException("Unknown time wondow start for notification check execution");

        }

        return intervalSecs;
    }

    public static int getExecutionWindowLength(int interval, String intervalUnit) {

        int intervalSecs;

        switch (intervalUnit) {

            case MINUTE:
                intervalSecs = 5;
                break;
            case HOUR:
                if (interval <=24) {
                    intervalSecs = (int)TimeUnit.MINUTES.toSeconds(5);
                } else {
                    intervalSecs = (int)TimeUnit.MINUTES.toSeconds(30);
                }
                break;
            case DAY:
                if (interval <=2) {
                    intervalSecs = (int)TimeUnit.MINUTES.toSeconds(30);
                } else {
                    intervalSecs = (int)TimeUnit.HOURS.toSeconds(2);
                }
                break;
            case WEEK:
                intervalSecs = (int)TimeUnit.HOURS.toSeconds(12);
                break;
            default:
                throw new IllegalArgumentException("Unknown time window end for notification check execution");

        }

        return intervalSecs;
    }


    public static String[] getIntervalUnits(Context context) {
        String[] values = new String[intervalUnitList.length];

        for(int i = 0; i < intervalUnitList.length; i++) {
            switch (intervalUnitList[i]) {
                case MINUTE:
                    values[i] = context.getResources().getString(R.string.minute_unit);
                    break;
                case HOUR:
                    values[i] = context.getResources().getString(R.string.hour_unit);
                    break;
                case DAY:
                    values[i] = context.getResources().getString(R.string.day_unit);
                    break;
                case WEEK:
                    values[i] = context.getResources().getString(R.string.week_unit);
                    break;

                    default:
                        values[i] = "";
            }
        }
        return values;
    }

    public static String getStringForIntervalUnitIndex(int index) {
        if (index >= 0 && index < intervalUnitList.length) {
            return intervalUnitList[index];
        } else
            throw new IllegalArgumentException("Provided interval unit string index is out of bounds");
    }


    public static String getLocalizedStringForIntervalUnit(Context context, String unit, int quantity) {
        String unitString = "";

        switch (unit) {
            case MINUTE:
                unitString = context.getResources().getQuantityString(R.plurals.minute, quantity, quantity);
                break;
            case HOUR:
                unitString = context.getResources().getQuantityString(R.plurals.hour, quantity, quantity);
                break;
            case DAY:
                unitString = context.getResources().getQuantityString(R.plurals.day, quantity, quantity);
                break;
            case WEEK:
                unitString = context.getResources().getQuantityString(R.plurals.week, quantity, quantity);
                break;

            default:
                throw new IllegalArgumentException("No corresponding string resource was found for provided interval unit");
        }
        return unitString;
    }


}
