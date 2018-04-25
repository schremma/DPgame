package com.melodispel.dpgame;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.melodispel.dpgame.data.DBContract;
import com.melodispel.dpgame.data.DBOpenHelper;

public class LevelListActivity extends AppCompatActivity implements LevelAdapter.LevelAdapterOnClickHandler {

    private SQLiteDatabase db;
    private RecyclerView rwLevels;

    public static final String COLUMN_NAME_LEVEL = DBContract.ResponsesEntry.COLUMN_LEVEL;
    public static final String EXTRA_LEVEL = "com.melodispel.dpgame.LEVEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_list);

        SQLiteOpenHelper dbOpenHelper = new DBOpenHelper(this);
        db = dbOpenHelper.getReadableDatabase();
        Cursor levelCursor = getAllDistinctLevels();
        if (levelCursor == null || levelCursor.getCount() < 1) {
            levelCursor = getFirstLevel();
        }
        if (levelCursor == null) {
            throw new IllegalArgumentException("No levels were found!");
        }

        LevelAdapter levelAdapter = new LevelAdapter(this, this);
        levelAdapter.swapCursor(levelCursor);

        rwLevels = (RecyclerView)findViewById(R.id.rw_level_list);
        rwLevels.setAdapter(levelAdapter);
        rwLevels.setLayoutManager(new LinearLayoutManager(this));

    }

    private Cursor getAllDistinctLevels() {

        Cursor levels = db.query(true, DBContract.ResponsesEntry.TABLE_NAME,
                new String[] {DBContract.ResponsesEntry.COLUMN_LEVEL},
                null,
                null,
                null,
                null,
                DBContract.ResponsesEntry.COLUMN_LEVEL+" DESC",
                null);
        return levels;
    }

    private Cursor getFirstLevel() {
        Cursor firstLevel = db.query(DBContract.MaterialsEntry.TABLE_NAME,
                new String[] {DBContract.MaterialsEntry.COLUMN_LEVEL},
                null,
                null,
                null,
                null,
                DBContract.MaterialsEntry.COLUMN_LEVEL+" ASC", "1");
        return firstLevel;
    }

    @Override
    public void onItemCLick(int level) {

        Intent playIntent = new Intent(this, PlayActivity.class);
        playIntent.putExtra(EXTRA_LEVEL, level);
        startActivity(playIntent);

    }
}
