package com.melodispel.dpgame.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.melodispel.dpgame.R;

public class DPGamePreferences {



    public static void SetNumberOfResponsesForResultCalculation(Context context, int numberOfResponses) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt(context.getResources().getString(R.string.pref_key_number_of_responses_for_results), numberOfResponses);
        editor.apply();
    }

    public static void setProgressionLimit(Context context, int limit) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putInt(context.getResources().getString(R.string.pref_key_progression_limit), limit);
        editor.apply();
    }

    private static int getPreferredNumberOfResponsesForResultCalculation(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getInt(context.getResources().getString(R.string.pref_key_number_of_responses_for_results),
                R.integer.pref_default_nbr_of_responses_for_result_calculation);
    }

    private static int getPreferredProgressionLimit(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getInt(context.getResources().getString(R.string.pref_key_progression_limit),
                R.integer.pref_default_progression_limit);
    }
}
