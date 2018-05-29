package com.melodispel.dpgame;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.pref_admin);

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        SharedPreferences sp = preferenceScreen.getSharedPreferences();
        int preferenceCount = preferenceScreen.getPreferenceCount();

        for (int i = 0; i < preferenceCount; i ++) {
            Preference preference = preferenceScreen.getPreference(i);

            if (!(preference instanceof CheckBoxPreference)) {
                String value = sp.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }

        EditTextPreference nbrOfResponsesTextPref = (EditTextPreference) preferenceScreen.findPreference(getString(R.string.pref_key_number_of_responses_for_results));
        EditTextPreference progressionLimitTextPref = (EditTextPreference) preferenceScreen.findPreference(getString(R.string.pref_key_progression_limit));


        nbrOfResponsesTextPref.setOnPreferenceChangeListener(new NumberSettingChangeListener());
        progressionLimitTextPref.setOnPreferenceChangeListener(new NumberSettingChangeListener());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        preference.setSummary(value.toString());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Preference preference = findPreference(key);
        if(!(preference instanceof CheckBoxPreference)) {
            setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private class NumberSettingChangeListener implements Preference.OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Boolean validInput = true;
            try {
                int number = Integer.parseInt(newValue.toString());
            } catch (NumberFormatException ex) {
                validInput = false;
            }
            if (!validInput) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Invalid Input");
                builder.setMessage("Please enter an integer number");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
                validInput = false;
            }
            return validInput;
        }
    }
}
