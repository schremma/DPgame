package com.melodispel.dpgame;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.melodispel.dpgame.data.DBContract;

public class LevelListActivity extends AppCompatActivity implements LevelAdapter.LevelAdapterOnClickHandler {

    private SQLiteDatabase db;
    private RecyclerView rwLevels;
    private boolean isPlayer;

    public static final String COLUMN_NAME_LEVEL = DBContract.SessionDataEntry.COLUMN_LEVEL;
    public static final String EXTRA_LEVEL = "com.melodispel.dpgame.LEVEL";

    public static final boolean IS_PLAYER_DEFAULT = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_list);

        Intent intent = getIntent();
        isPlayer = intent.getBooleanExtra(MainActivity.EXTRA_IS_PLAYER, IS_PLAYER_DEFAULT);

        // retrieve either player sessions or tester sessions
        String selection = DBContract.SessionDataEntry.COLUMN_IS_PLAYER_SESSION + "=?";
        String selectionValue = isPlayer ? "1" : "0";
        String[] selectionArgs = new String[]{selectionValue};

        ContentResolver contentResolver = this.getContentResolver();
        Cursor levelCursor = contentResolver.query(DBContract.SessionDataEntry.buildAllAchievedLevelUri(),
                null,
                selection,
                selectionArgs,
                null);


        if (levelCursor == null || levelCursor.getCount() < 1) {

            levelCursor = contentResolver.query(DBContract.MaterialsEntry.buildFirstAvailableLevelUri(),
                    null,
                    null,
                    null,
                    null);
        }


        if (levelCursor == null) {
            throw new IllegalArgumentException("No levels were found!");
        }

        LevelAdapter levelAdapter = new LevelAdapter(this, this);
        levelAdapter.setData(levelCursor);

        rwLevels = (RecyclerView)findViewById(R.id.rw_level_list);
        rwLevels.setAdapter(levelAdapter);
        rwLevels.setLayoutManager(new LinearLayoutManager(this));

    }


    @Override
    public void onItemCLick(int level) {

        Intent playIntent = new Intent(this, PlayActivity.class);
        playIntent.putExtra(EXTRA_LEVEL, level);
        startActivity(playIntent);

    }
}
