package com.melodispel.dpgame.gameplay;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.melodispel.dpgame.data.CustomsGamePlay;
import com.melodispel.dpgame.data.DBContract;
import com.melodispel.dpgame.data.ResponseData;
import com.melodispel.dpgame.data.SessionData;

public class TestPlayManager implements GamePlayManager {

    private Context context;
    private ResponseManager responseManager;

    private int currentLevel;
    private GamePlayDisplay gamePlayDisplay;

    private static final int STRING_VALUES_RT_INDEX = 0;
    private static final int STRING_VALUES_ACCURACY_INDEX = 1;
    private static final boolean IS_PLAYER_SESSION = false;

    public int getCurrentLevel() {
        return currentLevel;
    }

    public TestPlayManager(Context context, GamePlayDisplay gamePlayDisplay, int currentLevel, boolean isPlayerSession) {
        this.context = context;
        this.currentLevel = currentLevel;
        this.gamePlayDisplay = gamePlayDisplay;

        responseManager = new ResponseManager();
    }

    @Override
    public void onNewSessionStarted(String sessionCustoms) {
        SessionData sessionData = new SessionData();
        sessionData.setStartTimeStamp(System.currentTimeMillis());
        sessionData.setIsPlayerSession(IS_PLAYER_SESSION);
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

        return getAllSentencesForLevel(currentLevel);
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

        // TODO save last response id, e.g. to shared preferences to respond to lifecycle changes

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

    private Cursor getLastPlayedSentenceIdOnLevel(int level) {
        return context.getContentResolver().query(DBContract.ResponsesEntry.buildLastPlayedSentenceIdUri(1),
                null,DBContract.ResponsesEntry.COLUMN_LEVEL + "=?", new String[]{String.valueOf(level)}, null);
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

                Uri responseUri = context.getContentResolver().insert(DBContract.SessionDataEntry.CONTENT_URI, cv);

                Log.i(getClass().getSimpleName(), "saved session data: " + cv.toString() +
                        " , to: " + responseUri.toString());

            }
            return null;
        }
    }

}
