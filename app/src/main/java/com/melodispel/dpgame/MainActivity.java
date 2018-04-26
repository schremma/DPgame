package com.melodispel.dpgame;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.melodispel.dpgame.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    public static final String EXTRA_IS_PLAYER = "com.melodispel.dpgame.ISPLAYER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playIntent = new Intent(getApplicationContext(), LevelListActivity.class);
                playIntent.putExtra(EXTRA_IS_PLAYER, true);
                startActivity(playIntent);
            }
        });

        binding.btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent testIntent = new Intent(getApplicationContext(), LevelListActivity.class);
                testIntent.putExtra(EXTRA_IS_PLAYER, false);
                startActivity(testIntent);
            }
        });

    }

}
