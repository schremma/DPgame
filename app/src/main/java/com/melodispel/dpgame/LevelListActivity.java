package com.melodispel.dpgame;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.melodispel.dpgame.data.DBContract;

public class LevelListActivity extends AppCompatActivity implements LevelAdapter.LevelAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private SQLiteDatabase db;
    private RecyclerView rwLevels;
    private LevelAdapter levelAdapter;
    private boolean isPlayer;

    public static final String COLUMN_NAME_LEVEL = DBContract.SessionDataEntry.COLUMN_LEVEL;
    public static final String EXTRA_LEVEL = "com.melodispel.dpgame.LEVEL";

    private static final int ID_LEVEL_LOADER = 254;
    private static final boolean IS_PLAYER_DEFAULT = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_list);

        Intent intent = getIntent();
        isPlayer = intent.getBooleanExtra(MainActivity.EXTRA_IS_PLAYER, IS_PLAYER_DEFAULT);

        levelAdapter = new LevelAdapter(this, this);
        rwLevels = (RecyclerView)findViewById(R.id.rw_level_list);
        rwLevels.setAdapter(levelAdapter);
        rwLevels.setLayoutManager(new LinearLayoutManager(this));

        //levelAdapter.setData(levelCursor);

        getLoaderManager().initLoader(ID_LEVEL_LOADER, null, this);

    }


    @Override
    public void onItemCLick(int level) {

        Intent playIntent = new Intent(this, PlayActivity.class);
        playIntent.putExtra(EXTRA_LEVEL, level);
        startActivity(playIntent);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        switch (loaderId) {
            case ID_LEVEL_LOADER:

            // retrieve either player levels or tester level (i.e. all available levels)
            Uri uri = isPlayer ? DBContract.SessionDataEntry.buildAllAchievedLevelUri() :
                    DBContract.MaterialsEntry.buildAllAvailableLevelUri();

            return new CursorLoader(this,
                    uri,
                    null,
                    null,
                    null,
                    null);

            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        levelAdapter.setData(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        levelAdapter.setData(null);
    }
}
