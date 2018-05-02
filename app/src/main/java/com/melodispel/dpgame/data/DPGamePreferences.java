package com.melodispel.dpgame.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.melodispel.dpgame.R;

public class DPGamePreferences {



    public static void SetNumberOfResponsesForResultCalculation(Context context, int numberOfResponses) {

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


        Log.i("GamePreferences", String.valueOf(value));


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


        Log.i("GamePreferences", String.valueOf(value));

        try {
            int nbr = Integer.parseInt(value);

            return nbr;
        } catch (NumberFormatException ex) {
            return context.getResources().getInteger(R.integer.pref_default_nbr_of_responses_for_result_calculation);
        }
    }
}
