package com.melodispel.dpgame.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Andrea on 2018-04-13.
 */

public final class DBContract {

    public static final String CONTENT_AUTHORITY = "com.melodipsel.dpgame.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MATERIALS = "materials";
    public static final String PATH_RESPONSES = "responses";
    public static final String PATH_SESSIONDATA = "sessiondata";

    public static final String COUNT = "count";
    public static final String DISTINCT = "distinct";
    public static final String TOP = "top";

    public static final class MaterialsEntry implements BaseColumns {

        // The CONTENT_URI used to query the materials table from the conent provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MATERIALS)
                .build();

        public static final String TABLE_NAME = "materials";
        public static final String COLUMN_SENTENCE_ID = "sentenceId";
        public static final String COLUMN_ITEM_START = "itemStart";
        public static final String COLUMN_CORRECT = "correct";
        public static final String COLUMN_WRONG = "wrong";
        public static final String COLUMN_ITEM_END = "itemEnd";
        public static final String COLUMN_LEVEL = "level";

        public static Uri buildCountSentencesAtLevelUri(int level) {
            return CONTENT_URI.buildUpon().
                    appendPath(COUNT).
                    appendPath(String.valueOf(level)).
                    build();
        }

        public static Uri buildAllAvailableLevelUri() {
            return CONTENT_URI.buildUpon().
                    appendPath(DISTINCT).
                    build();
        }

    }

    public static final class ResponsesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_RESPONSES)
                .build();

        public static final String TABLE_NAME = "responses";
        public static final String COLUMN_SENTENCE_ID = "sentenceId";
        public static final String COLUMN_ACCURACY = "accuracy";
        public static final String COLUMN_RT = "responseTime";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_TIME_STAMP = "timeStamp";

        public static Uri buildCountResponsesAtLevelUri(int level) {
            return CONTENT_URI.buildUpon().
                    appendPath(COUNT).
                    appendPath(String.valueOf(level)).
                    build();
        }


        public static Uri buildLastPlayedItemsIdUri(int limit) {
            return CONTENT_URI.buildUpon().
                    appendPath(TOP).
                    appendPath(String.valueOf(limit)).
                    build();
        }

    }

    public  static final class SessionDataEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SESSIONDATA)
                .build();

        public static final String TABLE_NAME = "sessionData";
        public static final String COLUMN_SESSION_START_TIME_STAMP = "sessionStart";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_SESSION_CUSTOMS = "sessionCustoms";
        public static final String COLUMN_IS_PLAYER_SESSION = "isPlayerSession";

        public static Uri buildAllAchievedLevelUri() {
            return CONTENT_URI.buildUpon().
                    appendPath(DISTINCT).
                    build();
        }
    }
}
