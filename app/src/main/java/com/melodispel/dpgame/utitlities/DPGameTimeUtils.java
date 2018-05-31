package com.melodispel.dpgame.utitlities;

import android.content.Context;
import android.database.Cursor;

import com.melodispel.dpgame.data.DBContract;

public final class DPGameTimeUtils {

    public static long getTimeStampNow() {
        return System.currentTimeMillis();
    }

    public static long getMillisPassedSinceTimeStamp(long timeStamp) {
        return System.currentTimeMillis() - timeStamp;
    }

    /**
     *
     * @param context
     * @return Millisecs passed since last game, or -1 if no saved respons was found, indicating
     * that the player never played before
     */
    public static long getMillisPassedSinceLastGame(Context context) {

        //String[] projection = new String[] {DBContract.ResponsesEntry.COLUMN_TIME_STAMP};
        Cursor cursor = context.getContentResolver().query(DBContract.ResponsesEntry.buildLastPlayedItemsUri(1),
                null,null, null, null);

        if (cursor.moveToFirst()) {
            long lastPlayedInMillis = cursor.getLong(cursor.getColumnIndex(DBContract.ResponsesEntry.COLUMN_TIME_STAMP));
            long intervalMillis = System.currentTimeMillis()-lastPlayedInMillis;

            cursor.close();
            return intervalMillis;

        } else {
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        }
    }
}
