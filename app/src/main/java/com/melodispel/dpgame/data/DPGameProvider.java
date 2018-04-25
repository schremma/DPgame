package com.melodispel.dpgame.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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


    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DBContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DBContract.PATH_MATERIALS, CODE_MATERIALS);
        matcher.addURI(authority, DBContract.PATH_RESPONSES, CODE_RESPONSES);
        matcher.addURI(authority, DBContract.PATH_SESSIONDATA, CODE_SESSIONDATA);

        matcher.addURI(authority, DBContract.PATH_RESPONSES + "/" + DBContract.COUNT + "/#", CODE_COUNT_RESPONSES);
        matcher.addURI(authority, DBContract.PATH_MATERIALS + "/" + DBContract.COUNT + "/#", CODE_COUNT_SENTENCES);

        matcher.addURI(authority, DBContract.PATH_RESPONSES + "/" + DBContract.DISTINCT, CODE_ALL_ACHIEVED_LEVELS);

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
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
