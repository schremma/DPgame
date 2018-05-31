package com.melodispel.dpgame.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.melodispel.dpgame.R;

import java.util.Locale;

public class DPGamePreferences {


    public static void setNotificationIntervalUnit(Context context, String intervalUnit) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(context.getResources().getString(R.string.pref_key_notification_interval_unit), intervalUnit);
        editor.apply();
    }

    public static void setNotificationIntervalTime(Context context, int intervalTime) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        
        editor.putInt(context.getResources().getString(R.string.pref_key_notification_interval), intervalTime);
        editor.apply();

    }

    public static int getNotificationInterval(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        int interval = sp.getInt(context.getResources().getString(R.string.pref_key_notification_interval),context.getResources().getInteger(R.integer.pref_default_notification_interval));

        Log.i("DPGamePreferences", "interval: " + interval);

        return interval;

    }

    public static String getNotificationIntervalUnit(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getString(context.getResources().getString(R.string.pref_key_notification_interval_unit),
                context.getResources().getString(R.string.pref_default_notification_interval_unit));

    }

    public static void setNumberOfResponsesForResultCalculation(Context context, int numberOfResponses) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(context.getResources().getString(R.string.pref_key_number_of_responses_for_results), String.valueOf(numberOfResponses));
        editor.apply();
    }

    public static void setProgressionLimit(Context context, int limit) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(context.getResources().getString(R.string.pref_key_progression_limit), String.valueOf(limit));
        editor.apply();
    }

    public static int getPreferredNumberOfResponsesForResultCalculation(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);


        String value = sp.getString(context.getResources().getString(R.string.pref_key_number_of_responses_for_results),
                String.valueOf(context.getResources().getInteger(R.integer.pref_default_nbr_of_responses_for_result_calculation)));


        try {
            int nbr = Integer.parseInt(value);

            return nbr;
        } catch (NumberFormatException ex) {
            return context.getResources().getInteger(R.integer.pref_default_nbr_of_responses_for_result_calculation);
        }
    }

    public static int getPreferredProgressionLimit(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String value = sp.getString(context.getResources().getString(R.string.pref_key_progression_limit),
                String.valueOf(context.getResources().getInteger(R.integer.pref_default_progression_limit)));


        try {
            int nbr = Integer.parseInt(value);

            return nbr;
        } catch (NumberFormatException ex) {
            return context.getResources().getInteger(R.integer.pref_default_nbr_of_responses_for_result_calculation);
        }
    }

    public static String getCurrentAppLanguage(Context context) {
        return  context.getResources().getConfiguration().locale.getLanguage();
    }

    public static void setPreferredLanguage(Context context, String lang) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(context.getResources().getString(R.string.pref_key_language), lang);
        editor.apply();
    }

    public static String getPreferredLanguage(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getResources().getString(R.string.pref_key_language),
                context.getResources().getString(R.string.pref_default_language));
    }

    public static void applyPreferredAppLanguage(Context context) {
        String lang = DPGamePreferences.getPreferredLanguage(context);
        if (lang != null) {
            if (!lang.equals(DPGamePreferences.getCurrentAppLanguage(context))) {

                Locale locale = new Locale(lang);
                Resources res = context.getResources();
                DisplayMetrics dm = res.getDisplayMetrics();
                Configuration conf = res.getConfiguration();
                conf.locale = locale;
                res.updateConfiguration(conf, dm);

            }
        }
    }
}
