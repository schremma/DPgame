package com.melodispel.dpgame;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.melodispel.dpgame.data.DPGamePreferences;
import com.melodispel.dpgame.databinding.ActivityMainBinding;
import com.melodispel.dpgame.reminders.ReminderTasks;
import com.melodispel.dpgame.reminders.ReminderUtilities;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    public static final String EXTRA_IS_PLAYER = "com.melodispel.dpgame.ISPLAYER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        DPGamePreferences.applyPreferredAppLanguage(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playIntent = new Intent(MainActivity.this, LevelListActivity.class);
                playIntent.putExtra(EXTRA_IS_PLAYER, true);
                startActivity(playIntent);
            }
        });

/*        binding.btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent testIntent = new Intent(MainActivity.this, LevelListActivity.class);
                testIntent.putExtra(EXTRA_IS_PLAYER, false);
                startActivity(testIntent);
            }
        });*/

        binding.btnResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ResultsActivity.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        String currentLanguage = getResources().getConfiguration().locale.getLanguage();
        String switchLanguage = getString(R.string.language_english);
        if (currentLanguage.equals("en"))
            switchLanguage = getString(R.string.language_swedish);
        MenuItem languagedMenuItem = menu.findItem(R.id.setings_language);
        languagedMenuItem.setTitle(languagedMenuItem.getTitle() + " " + switchLanguage);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.settings_main) {
            showNotificationPickerDialog();
        } else if (item.getItemId() == R.id.setings_language) {
            String currentLanguage = DPGamePreferences.getCurrentAppLanguage(this);
            if (currentLanguage.equals("en")) {
                switchAppLanguage("sv");
            } else if (currentLanguage.equals("sv")) {
                switchAppLanguage("en");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchAppLanguage(String lang) {

        DPGamePreferences.setPreferredLanguage(this, lang);
        DPGamePreferences.applyPreferredAppLanguage(this);
        restartActivity();

    }


    private void showNotificationPickerDialog() {
        final Dialog dialog = new Dialog(this);
        View dialogView = this.getLayoutInflater().inflate(R.layout.notification_picker_dialog, null);
        dialog.setContentView(dialogView);

        dialog.setTitle("Set notification");

        Button btnCancel = dialogView.findViewById(R.id.btn_notification_cancel);
        Button btnSet = dialogView.findViewById(R.id.btn_notification_set);
        TextView tvInfo = dialogView.findViewById(R.id.tv_notification_info);

        String notificationInfo = "";
        int setInterval = DPGamePreferences.getNotificationInterval(this);
        if (setInterval != getResources().getInteger(R.integer.pref_notification_interval_not_set)) {
            String unit = DPGamePreferences.getNotificationIntervalUnit(this);
            notificationInfo = getString(R.string.notifications_instruction_for_set_notification) + " "
            + ReminderUtilities.getLocalizedStringForIntervalUnit(this, unit, setInterval);
        } else {
            notificationInfo = getString(R.string.notifications_instruction_for_new_notification);
            btnCancel.setVisibility(View.INVISIBLE);
        }
        tvInfo.setText(notificationInfo);


        final NumberPicker unitPicker = dialogView.findViewById(R.id.notificationUnitPicker);
        final String[] values= ReminderUtilities.getIntervalUnits(this);
        unitPicker.setDisplayedValues(values);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(values.length-1);
        unitPicker.setWrapSelectorWheel(true);  //Whether the selector wheel wraps when reaching the min/max value.

        final NumberPicker numberPicker = dialogView.findViewById(R.id.notificationNumberPicker);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(99);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNotificationCanceled();
                dialog.dismiss();
            }
        });
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String unit = ReminderUtilities.getStringForIntervalUnitIndex(unitPicker.getValue());
                onNotificationSet(numberPicker.getValue(), unit);
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void onNotificationSet(int time, String interval) {

        DPGamePreferences.setNotificationIntervalTime(this, time);
        DPGamePreferences.setNotificationIntervalUnit(this, interval);

        if (time > 0) {
            ReminderUtilities.scheduleFirebaseJobDispatcherForReminder(this, time, interval);
            ReminderTasks.remindOfPlaying(this, System.currentTimeMillis());
        } else {
            ReminderUtilities.cancelFirebaseJobDispatcherForReminder(this);
        }

    }

    private void onNotificationCanceled() {
        DPGamePreferences.setNotificationIntervalTime(this, R.integer.pref_notification_interval_not_set);
        ReminderUtilities.cancelFirebaseJobDispatcherForReminder(MainActivity.this);
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
