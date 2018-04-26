package com.melodispel.dpgame.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DPGameProvider extends ContentProvider {

    private DBOpenHelper dbOpenHelper;

    public static final int CODE_MATERIALS = 101;
    public static final int CODE_RESPONSES = 102;
    public static final int CODE_SESSIONDATA = 103;
    public static final int CODE_COUNT_SENTENCES = 104;
    public static final int CODE_COUNT_RESPONSES = 105;
    public static final int CODE_ALL_ACHIEVED_LEVELS = 106;
    public static final int CODE_ALL_AVAILABLE_LEVELS = 107;
    public static final int CODE_MATERIAL_WITH_ID = 108;
    public static final int CODE_FIRST_AVAILABLE_LEVEL = 109;
    public static final int CODE_LAST_PLAYED_SENTENCE_ID = 110;
    public static final int CODE_RESPONSE_WITH_ID = 111;
    public static final int CODE_SESSIONDATA_WITH_ID = 112;


    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DBContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DBContract.PATH_MATERIALS, CODE_MATERIALS);
        matcher.addURI(authority, DBContract.PATH_RESPONSES, CODE_RESPONSES);
        matcher.addURI(authority, DBContract.PATH_SESSIONDATA, CODE_SESSIONDATA);

        matcher.addURI(authority, DBContract.PATH_MATERIALS +  "/#", CODE_MATERIAL_WITH_ID);
        matcher.addURI(authority, DBContract.PATH_RESPONSES +  "/#", CODE_RESPONSE_WITH_ID);
        matcher.addURI(authority, DBContract.PATH_SESSIONDATA +  "/#", CODE_SESSIONDATA_WITH_ID);

        matcher.addURI(authority, DBContract.PATH_RESPONSES + "/" + DBContract.COUNT + "/#", CODE_COUNT_RESPONSES);
        matcher.addURI(authority, DBContract.PATH_MATERIALS + "/" + DBContract.COUNT + "/#", CODE_COUNT_SENTENCES);

        matcher.addURI(authority, DBContract.PATH_SESSIONDATA + "/" + DBContract.DISTINCT, CODE_ALL_ACHIEVED_LEVELS);
        matcher.addURI(authority, DBContract.PATH_MATERIALS + "/" + DBContract.DISTINCT, CODE_ALL_AVAILABLE_LEVELS);
        matcher.addURI(authority, DBContract.PATH_MATERIALS + "/" + DBContract.DISTINCT + "/1", CODE_FIRST_AVAILABLE_LEVEL);
        matcher.addURI(authority, DBContract.PATH_RESPONSES + "/" + DBContract.TOP + "/#", CODE_LAST_PLAYED_SENTENCE_ID);

        return matcher;

    }

    @Override
    public boolean onCreate() {

        // no lengthy operations should be performed here: method is called for each registered
        // content provider on application launch

        dbOpenHelper = new DBOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {

            case CODE_MATERIALS:
                cursor = dbOpenHelper.getReadableDatabase().query(
                        DBContract.MaterialsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_MATERIAL_WITH_ID: // materials might be retrieved by database _id or by sentence id
                String id = uri.getLastPathSegment();
                if (selection == null) {
                    selection = DBContract.MaterialsEntry._ID + "=?";
                }
                selectionArgs = new String[]{id};

                cursor = dbOpenHelper.getReadableDatabase().query(
                        DBContract.MaterialsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_RESPONSE_WITH_ID:
                String responseId = uri.getLastPathSegment();

                selection = DBContract.ResponsesEntry._ID + "=?";
                selectionArgs = new String[]{responseId};

                cursor = dbOpenHelper.getReadableDatabase().query(
                        DBContract.ResponsesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_SESSIONDATA_WITH_ID: // materials might be retrieved by database _id or by sentence id
                String sessionId = uri.getLastPathSegment();

                selection = DBContract.SessionDataEntry._ID + "=?";
                selectionArgs = new String[]{sessionId};

                cursor = dbOpenHelper.getReadableDatabase().query(
                        DBContract.SessionDataEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_RESPONSES:
                cursor = dbOpenHelper.getReadableDatabase().query(
                        DBContract.ResponsesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_SESSIONDATA:
                cursor = dbOpenHelper.getReadableDatabase().query(
                        DBContract.SessionDataEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_COUNT_RESPONSES:

                String responseLevel = uri.getLastPathSegment();

                cursor = dbOpenHelper.getReadableDatabase().rawQuery("select count(*) from " +
                        DBContract.ResponsesEntry.TABLE_NAME + " where level='" + responseLevel +"'", null);
                break;

            case CODE_COUNT_SENTENCES:

                String sentenceLevel = uri.getLastPathSegment();

                cursor = dbOpenHelper.getReadableDatabase().rawQuery("select count(*) from " +
                            DBContract.MaterialsEntry.TABLE_NAME + " where level='" + sentenceLevel +"'", null);
                break;

            case CODE_ALL_ACHIEVED_LEVELS:

                cursor = dbOpenHelper.getReadableDatabase().query(true, DBContract.SessionDataEntry.TABLE_NAME,
                        new String[] {DBContract.SessionDataEntry.COLUMN_LEVEL},
                        selection,
                        selectionArgs,
                        null,
                        null,
                        DBContract.SessionDataEntry.COLUMN_LEVEL +" DESC",
                        null);


                break;

            case CODE_ALL_AVAILABLE_LEVELS:

                cursor = dbOpenHelper.getReadableDatabase().query(true, DBContract.MaterialsEntry.TABLE_NAME,
                        new String[] {DBContract.MaterialsEntry.COLUMN_LEVEL},
                        selection,
                        selectionArgs,
                        null,
                        null,
                        DBContract.MaterialsEntry.COLUMN_LEVEL+" ASC",
                        null);
                break;

            case CODE_FIRST_AVAILABLE_LEVEL:

                cursor = dbOpenHelper.getReadableDatabase().query(true, DBContract.MaterialsEntry.TABLE_NAME,
                        new String[] {DBContract.MaterialsEntry.COLUMN_LEVEL},
                        null,
                        null,
                        null,
                        null,
                        DBContract.MaterialsEntry.COLUMN_LEVEL+" ASC",
                        "1");
                break;

            case CODE_LAST_PLAYED_SENTENCE_ID:
                String limit = uri.getLastPathSegment();

                cursor = dbOpenHelper.getReadableDatabase().query(DBContract.ResponsesEntry.TABLE_NAME,
                        new String[] {DBContract.ResponsesEntry.COLUMN_SENTENCE_ID} ,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        DBContract.ResponsesEntry.COLUMN_TIME_STAMP +" DESC",
                        limit);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        return cursor;
    }



    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Getting type is not implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        switch (sUriMatcher.match(uri)) {

            case CODE_MATERIALS:

                long materialId = dbOpenHelper.getWritableDatabase().insert(DBContract.MaterialsEntry.TABLE_NAME, null, contentValues);

                if (materialId != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return Uri.parse(DBContract.PATH_MATERIALS + "/" + materialId);

            case CODE_RESPONSES:
                long responseId = dbOpenHelper.getWritableDatabase().insert(DBContract.ResponsesEntry.TABLE_NAME, null, contentValues);

                if (responseId != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return Uri.parse(DBContract.PATH_RESPONSES + "/" + responseId);

            case CODE_SESSIONDATA:

                long sessionId = dbOpenHelper.getWritableDatabase().insert(DBContract.SessionDataEntry.TABLE_NAME, null, contentValues);

                if (sessionId != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return Uri.parse(DBContract.PATH_SESSIONDATA + "/" + sessionId);


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_MATERIALS:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(DBContract.MaterialsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;

        //passing "1" for the selection will delete all rows and return the number of rows deleted
        if (selection == null)
            selection = "1";

        switch (sUriMatcher.match(uri)) {

            case CODE_RESPONSES:
                numRowsDeleted = dbOpenHelper.getWritableDatabase().delete(
                        DBContract.ResponsesEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new RuntimeException("Update is not implemented");
    }
}
