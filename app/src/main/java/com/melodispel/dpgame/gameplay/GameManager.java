package com.melodispel.dpgame.gameplay;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.melodispel.dpgame.GameEnums;
import com.melodispel.dpgame.R;
import com.melodispel.dpgame.data.CustomsGamePlay;
import com.melodispel.dpgame.data.DBContract;
import com.melodispel.dpgame.data.DPGamePreferences;
import com.melodispel.dpgame.data.ResponseData;
import com.melodispel.dpgame.data.SessionData;

public class GameManager implements GamePlayManager {

    private Context context;
    private ResponseManager responseManager;

    private int currentLevel;
    private GamePlayDisplay gamePlayDisplay;
    private boolean isPlayerSession;

    private static final int STRING_VALUES_RT_INDEX = 0;
    private static final int STRING_VALUES_ACCURACY_INDEX = 1;

    public int getCurrentLevel() {
        return currentLevel;
    }

    public GameManager(Context context, GamePlayDisplay gamePlayDisplay, int currentLevel, boolean isPlayerSession) {
        this.context = context;
        this.currentLevel = currentLevel;
        this.gamePlayDisplay = gamePlayDisplay;
        this.isPlayerSession = isPlayerSession;

/*        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int responseCount = sp.getInt(context.getResources().getString(R.string.pref_key_number_of_responses_for_results),
                context.getResources().getInteger(R.integer.pref_default_nbr_of_responses_for_result_calculation));
        int progressionLimit = sp.getInt(context.getResources().getString(R.string.pref_key_progression_limit),
                context.getResources().getInteger(R.integer.pref_default_progression_limit));*/

        int responseCount = DPGamePreferences.getPreferredNumberOfResponsesForResultCalculation(context);
        int progressionLimit = DPGamePreferences.getPreferredNumberOfResponsesForResultCalculation(context);

        Log.i(getClass().getSimpleName(), "Initiating resposne manager with setting: " + String.valueOf(responseCount) +
        String.valueOf(progressionLimit));
        responseManager = new ResponseManager(responseCount, progressionLimit);
    }

    @Override
    public void onNewSessionStarted(String sessionCustoms) {
        SessionData sessionData = new SessionData();
        sessionData.setStartTimeStamp(System.currentTimeMillis());
        sessionData.setIsPlayerSession(isPlayerSession);
        sessionData.setLevel(currentLevel);

        if (sessionCustoms != null) {
            sessionData.setSessionCustoms(sessionCustoms);
        } else {
            sessionData.setSessionCustoms("");
        }

        new SaveSessionAsyncTask().execute(sessionData);
    }

    public boolean onGameInitialized() {

        Cursor materialsCursor = initializeLevelMaterial(currentLevel);
        gamePlayDisplay.setMaterial(materialsCursor);
        return true;
    }

    private Cursor initializeLevelMaterial(int level) {

        // 1. check the current level of the player
        // 2. player might have already played some of the sentences in that level during the last session:
        // check the last time this level was played and check the id of the sentence for the last saved response.
        // Continue with sentences that are higher than the last saved id

        Cursor materialsCursor = getAllSentencesForLevel(currentLevel);
        if (!materialsCursor.moveToFirst()) {
            throw new IllegalArgumentException("No material was found for level");
        }

        int responsesCount = getResponseCountForLevel(currentLevel);

        Log.i(this.getClass().getSimpleName(), "Saved responses at current level: " + String.valueOf(responsesCount));

        if (responsesCount > 0) {
            Cursor lastEntryOnLevel = getLastPlayedItemIdOnLevel(currentLevel);

            if (lastEntryOnLevel.moveToFirst()) {
                int lastPlayedSentenceId = lastEntryOnLevel.getInt(lastEntryOnLevel.getColumnIndex(DBContract.ResponsesEntry.COLUMN_SENTENCE_ID));

                Log.i(this.getClass().getSimpleName(), "Last played id: " + String.valueOf(lastPlayedSentenceId));

                boolean found = false;
                do {

                    found = materialsCursor.getInt(materialsCursor.getColumnIndex(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID)) == lastPlayedSentenceId;

                } while (!found && materialsCursor.moveToNext());
                if (!found)
                    materialsCursor.moveToFirst();
                else {
                    GameEnums.GameState gameState = gamePlayDisplay.getGameState();
                    if (!gameState.equals(gameState.RESPONDED)) {
                        materialsCursor.moveToNext();
                    }
                }

            }
        }
        return materialsCursor;
    }

    public void onNewPlayerResponse(int sentenceID, int responseTime, boolean accuracy) {
        responseManager.addResponse(responseTime, accuracy);

        Log.i(getClass().getSimpleName(), "Rts for stats: " + responseManager.rtValuesToString());
        Log.i(getClass().getSimpleName(), "Accuracy values for stats: " + responseManager.accuracyValuesToString());

        saveLatestResponse(sentenceID, responseTime, accuracy);

        if(responseManager.hasReachedProgressLimit()) {
            progressToNextLevel();
        }
    }

    private void progressToNextLevel() {

        if (getSentenceCountForLevel(currentLevel+1) > 0) {
            currentLevel++;
            Cursor newLevelMaterial = initializeLevelMaterial(currentLevel);

            if (newLevelMaterial != null) {
                responseManager.reset();
                gamePlayDisplay.progressToNextLevel(newLevelMaterial, currentLevel);

            } else {
                throw  new IllegalArgumentException("No material could be retrieved for level " + currentLevel);
            }
        }

        SessionData sessionData = new SessionData();
        sessionData.setStartTimeStamp(System.currentTimeMillis());
        sessionData.setIsPlayerSession(isPlayerSession);
        sessionData.setLevel(currentLevel);

        sessionData.setSessionCustoms(CustomsGamePlay.LEVEL_PROGRESS);

        new SaveSessionAsyncTask().execute(sessionData);
    }


    @Override
    public String[] getAccumulatedResults() {
        String[] resultValues = new String[2];
        resultValues[STRING_VALUES_RT_INDEX] = responseManager.rtValuesToString();
        resultValues[STRING_VALUES_ACCURACY_INDEX] = responseManager.accuracyValuesToString();
        return resultValues;
    }

    @Override
    public void setAccumulatedResults(String[] results){
        if (results == null) {
            throw new IllegalArgumentException("Provided past results are empty!");
        }
        if (results.length < 2) {
            throw new IllegalArgumentException("Provided past results are in invalid format");
        }

        if (results[0].length() > 0 || results[1].length() > 0) {

            if (responseManager == null) {
                responseManager = new ResponseManager();
            }
            responseManager.setResponseTimeValuesFromString(results[STRING_VALUES_RT_INDEX]);
            responseManager.setAccuracyValuesFromString(results[STRING_VALUES_ACCURACY_INDEX]);
        }
    }

    @Override
    public ResultSummary getResultSummary() {
        double accuracyPercent =  responseManager.getAverageAccuracy();
        int rtRounded = (int)Math.round(responseManager.getAverageRT());

        return new ResultSummary(accuracyPercent, rtRounded);
    }

    private void saveLatestResponse(int sentenceID, int responseTime, boolean accuracy) {
        ResponseData responseData = new ResponseData();
        responseData.setAccuracy(accuracy);

        responseData.setLevel(currentLevel);
        responseData.setRT(responseTime);
        responseData.setSentenceId(sentenceID);
        responseData.setTimestamp(System.currentTimeMillis());

        new SaveResponseAsyncTask().execute(responseData);

    }

    private Cursor getAllSentencesForLevel(int level) {
        return context.getContentResolver().query(DBContract.MaterialsEntry.CONTENT_URI,
                null, DBContract.MaterialsEntry.COLUMN_LEVEL + "=?",
                new String[] {String.valueOf(level)},
                DBContract.MaterialsEntry.COLUMN_SENTENCE_ID);
    }


    private int getResponseCountForLevel(int level) {
        Cursor countCursor = context.getContentResolver().query(DBContract.ResponsesEntry.buildCountResponsesAtLevelUri(level),
                null,null,null,null);
        countCursor.moveToFirst();
        return countCursor.getInt(0);
    }

    private long getSentenceCountForLevel(int level) {
        Cursor countCursor = context.getContentResolver().query(DBContract.MaterialsEntry.buildCountSentencesAtLevelUri(level),
                null,null,null,null);
        countCursor.moveToFirst();
        return countCursor.getInt(0);
    }

    private Cursor getLastPlayedItemIdOnLevel(int level) {
        return context.getContentResolver().query(DBContract.ResponsesEntry.buildLastPlayedItemsIdUri(1),
                null,DBContract.ResponsesEntry.COLUMN_LEVEL + "=?", new String[]{String.valueOf(level)}, null);
    }

    private class SaveResponseAsyncTask extends AsyncTask<ResponseData, Void, Void> {

        @Override
        protected Void doInBackground(ResponseData... responseData) {
            for (int i = 0; i < responseData.length; i++) {

                ContentValues cv = new ContentValues();
                cv.put(DBContract.ResponsesEntry.COLUMN_ACCURACY, responseData[i].getAccuracy());
                cv.put(DBContract.ResponsesEntry.COLUMN_LEVEL, responseData[i].getLevel());
                cv.put(DBContract.ResponsesEntry.COLUMN_RT, responseData[i].getRT());
                cv.put(DBContract.ResponsesEntry.COLUMN_SENTENCE_ID, responseData[i].getSentenceId());
                cv.put(DBContract.ResponsesEntry.COLUMN_TIME_STAMP, responseData[i].getTimestamp());

                //db.insert(DBContract.ResponsesEntry.TABLE_NAME, null, cv);

                Uri responseUri = context.getContentResolver().insert(DBContract.ResponsesEntry.CONTENT_URI, cv);

                Log.i(getClass().getSimpleName(), "saved response data: " + cv.toString() +
                " , to: " + responseUri.toString());

            }
            return null;
        }

    }

    private class SaveSessionAsyncTask extends AsyncTask<SessionData, Void, Void>  {

        @Override
        protected Void doInBackground(SessionData... sessionData) {

            for (int i = 0; i < sessionData.length; i++) {
                ContentValues cv = new ContentValues();
                cv.put(DBContract.SessionDataEntry.COLUMN_IS_PLAYER_SESSION, sessionData[i].getIsPlayerSession());
                cv.put(DBContract.SessionDataEntry.COLUMN_LEVEL, sessionData[i].getLevel());
                cv.put(DBContract.SessionDataEntry.COLUMN_SESSION_START_TIME_STAMP, sessionData[i].getStartTimeStamp());
                cv.put(DBContract.SessionDataEntry.COLUMN_SESSION_CUSTOMS, sessionData[i].getSessionCustoms());

               // db.insert(DBContract.SessionDataEntry.TABLE_NAME, null, cv);

                Uri responseUri = context.getContentResolver().insert(DBContract.SessionDataEntry.CONTENT_URI, cv);

                Log.i(getClass().getSimpleName(), "saved session data: " + cv.toString() +
                        " , to: " + responseUri.toString());

            }
            return null;
        }
    }

}
