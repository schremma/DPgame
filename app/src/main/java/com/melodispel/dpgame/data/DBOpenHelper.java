package com.melodispel.dpgame.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.melodispel.dpgame.utitlities.DataUtils;

/**
 * Created by Andrea on 2018-04-13.
 */

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "dpgame.db";

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 11;

    private Context context;

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MATERIALS_TABLE = "CREATE TABLE " + DBContract.MaterialsEntry.TABLE_NAME + " (" +
                DBContract.MaterialsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.MaterialsEntry.COLUMN_SENTENCE_ID + " INTEGER NOT NULL, " +
                DBContract.MaterialsEntry.COLUMN_ITEM_START + " TEXT, " +
                DBContract.MaterialsEntry.COLUMN_ITEM_END + " TEXT, " +
                DBContract.MaterialsEntry.COLUMN_CORRECT + " TEXT, " +
                DBContract.MaterialsEntry.COLUMN_WRONG + " TEXT, " +
                DBContract.MaterialsEntry.COLUMN_LEVEL + " INTEGER" +
                "); ";

        final String SQL_CREATE_RESPONSES_TABLE = "CREATE TABLE " + DBContract.ResponsesEntry.TABLE_NAME + " (" +
                DBContract.ResponsesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.ResponsesEntry.COLUMN_SENTENCE_ID + " INTEGER NOT NULL, " +
                DBContract.ResponsesEntry.COLUMN_ACCURACY + " INTEGER NOT NULL, " +
                DBContract.ResponsesEntry.COLUMN_RT + " INTEGER, " +
                DBContract.ResponsesEntry.COLUMN_TIME_STAMP + " INTEGER, " +
                DBContract.ResponsesEntry.COLUMN_LEVEL + " INTEGER" +
                "); ";

        final String SQL_CREATE_SESSIONDATA_TABLE = "CREATE TABLE " + DBContract.SessionDataEntry.TABLE_NAME + " (" +
                DBContract.SessionDataEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.SessionDataEntry.COLUMN_SESSION_START_TIME_STAMP + " INTEGER, " +
                DBContract.SessionDataEntry.COLUMN_LEVEL + " INTEGER, " +
                DBContract.SessionDataEntry.COLUMN_SESSION_CUSTOMS + " TEXT, " +
                DBContract.SessionDataEntry.COLUMN_IS_PLAYER_SESSION + " INTEGER" +
                "); ";

        db.execSQL(SQL_CREATE_MATERIALS_TABLE);
        db.execSQL(SQL_CREATE_RESPONSES_TABLE);
        db.execSQL(SQL_CREATE_SESSIONDATA_TABLE);
        initDbWithData(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Changing the DATABASE_VERSION will drop the tables.
        // In a production app, this method might be modified to ALTER the table

        db.execSQL("DROP TABLE IF EXISTS " + DBContract.MaterialsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ResponsesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.SessionDataEntry.TABLE_NAME);
        onCreate(db);

    }

    private void initDbWithData(SQLiteDatabase db) {

        DataUtils.FillDbWithMaterialsFromTxt(context, db);

    }
}
