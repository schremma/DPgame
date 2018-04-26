package com.melodispel.dpgame;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.melodispel.dpgame.data.DBContract;
import com.melodispel.dpgame.databinding.ActivityPlayBinding;
import com.melodispel.dpgame.gameplay.GameManager;
import com.melodispel.dpgame.gameplay.GamePlayDisplay;
import com.melodispel.dpgame.gameplay.GamePlayManager;
import com.melodispel.dpgame.gameplay.ResultSummary;


public class PlayActivity extends AppCompatActivity implements GamePlayDisplay {

    ActivityPlayBinding binding;
    private GamePlayManager gamePlayManager;

    private Cursor materialsCursor;
    private int correctTargetViewId;

    private static final String KEY_LEVEL = "levelKey";
    private static final String KEY_GAME_STATE = "gameStateKey";
    private static final String KEY_SESSION_STATE = "sessionStateKey";
    private static final String KEY_RESPONSE_ACCURACY = "accuracyKey";
    private static final String KEY_RESPONSE_TIME = "rtKey";
    private static final String KEY_MAINTAINED_RESULTS = "maintainedResultsKey";

    private static final int LEVEL_DEFAULT = 1;
    private static final SessionState SESSION_STATE_DEFAULT = SessionState.NOT_STARTED_PLAYER;
    public static final int RESPONSE_TIME_DEFAULT = -1000;


    private SessionState sessionState;
    private GameState gameState;

    private ResponseAccuracy responseAccuracy;
    private long startTimeOfResponse;
    private int responseTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        int currentLevel = savedInstanceState != null ?
                savedInstanceState.getInt(KEY_LEVEL, LEVEL_DEFAULT) : -1;

        if (currentLevel == -1) {
            currentLevel = intent.getIntExtra(LevelListActivity.EXTRA_LEVEL, LEVEL_DEFAULT);
        } else {
            Log.i(getClass().getSimpleName(), "got level from intent");
        }

        if (savedInstanceState !=null) {
            responseTime = savedInstanceState.getInt(KEY_RESPONSE_TIME, 0);
            sessionState = savedInstanceState.containsKey(KEY_SESSION_STATE) ?
                    (SessionState) savedInstanceState.get(KEY_SESSION_STATE) : SESSION_STATE_DEFAULT;
            gameState = savedInstanceState.containsKey(KEY_GAME_STATE) ?
                    (GameState) savedInstanceState.get(KEY_GAME_STATE) : GameState.NOT_STARTED;
            responseAccuracy = savedInstanceState.containsKey(KEY_RESPONSE_ACCURACY) ?
                    (ResponseAccuracy) savedInstanceState.get(KEY_RESPONSE_ACCURACY) : ResponseAccuracy.NO_RESPONSE;

        }
        else {
            gameState = GameState.NOT_STARTED;
            sessionState = SESSION_STATE_DEFAULT;
            responseAccuracy = ResponseAccuracy.NO_RESPONSE;
        }

        initGameArea();

        boolean isPlayerSession = false;
        if (sessionState.equals(SessionState.NOT_STARTED_PLAYER) || sessionState.equals(SessionState.IN_PROGRESS_PLAYER)) {
            isPlayerSession = true;
        }
        gamePlayManager = new GameManager(this, this, currentLevel, isPlayerSession);

        if (sessionState.equals(SessionState.NOT_STARTED_PLAYER) || sessionState.equals(SessionState.NOT_STARTED_TESTER)) {

            if (sessionState.equals(SessionState.NOT_STARTED_PLAYER)) {
                gamePlayManager.onNewSessionStarted(null);
                sessionState = SessionState.IN_PROGRESS_PLAYER;
            } else {
                gamePlayManager.onNewSessionStarted(null);
                sessionState = SessionState.IN_PROGRESS_TESTER;
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_MAINTAINED_RESULTS)) {
            gamePlayManager.setAccumulatedResults(savedInstanceState.getStringArray(KEY_MAINTAINED_RESULTS));
        }

        if (gamePlayManager.onGameInitialized()) {
            displayResults();

            if (!gameState.equals(GameState.NOT_STARTED)) {

                if (gameState.equals(gameState.RESPONDED)) {
                    toogleResponseControls(false);
                    showResultsOnTestPanel();
                } else {
                    moveToNextItem();
                    toogleResponseControls(true);
                    resetPreviousResultsOnTestPanel();
                }
                showCurrentSentence();

            }
        } else  {
            displayErrorMessage("Could not initialize game!");
        }

    }

    private void initGameArea() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play);
        binding.sentenceDisplay.targetArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameState.equals(GameState.NOT_STARTED)) {
                    startGame();
                } else if (gameState.equals(GameState.RESPONDED) || gameState.equals(GameState.PROGRESSED_LEVEL)) {
                    continueWithNextItem();
                }
            }
        });

        binding.sentenceDisplay.btnLeft.setOnTouchListener(new TargetItemTouchListener());
        binding.sentenceDisplay.btnRight.setOnTouchListener(new TargetItemTouchListener());
        binding.sentenceDisplay.targetArea.setOnDragListener(new TargetAreaDragListener());


        toogleResponseControls(false);

    }


    private void startGame() {
        gameState = GameState.WAITING_RESPONSE;
        moveToNextItem();
        toogleResponseControls(true);
        showCurrentSentence();
    }

    private void continueWithNextItem() {
        moveToNextItem();
        gameState = GameState.WAITING_RESPONSE;
        toogleResponseControls(true);
        showCurrentSentence();
    }

    private void showCurrentSentence() {

        // TODO: move this to test activity
        showResultsOnTestPanel();

        resetPreviousResultsOnTestPanel();

        if (materialsCursor != null || materialsCursor.getPosition() >= 0) {
            binding.sentenceDisplay.tvItemStart.setText(materialsCursor.getString(materialsCursor.
                    getColumnIndex(DBContract.MaterialsEntry.COLUMN_ITEM_START)));
            binding.sentenceDisplay.tvItemEnd.setText(materialsCursor.getString(materialsCursor.
                    getColumnIndex(DBContract.MaterialsEntry.COLUMN_ITEM_END)));

            String correctChoice = materialsCursor.getString(materialsCursor.
                    getColumnIndex(DBContract.MaterialsEntry.COLUMN_CORRECT));
            String wrongChoice = materialsCursor.getString(materialsCursor.
                    getColumnIndex(DBContract.MaterialsEntry.COLUMN_WRONG));

            boolean leftIsCorrect = Math.random() < 0.5;
            if (leftIsCorrect) {
                binding.sentenceDisplay.btnLeft.setText(correctChoice);
                correctTargetViewId = binding.sentenceDisplay.btnLeft.getId();

                binding.sentenceDisplay.btnRight.setText(wrongChoice);
            } else {
                binding.sentenceDisplay.btnRight.setText(correctChoice);
                correctTargetViewId = binding.sentenceDisplay.btnRight.getId();

                binding.sentenceDisplay.btnLeft.setText(wrongChoice);
            }

            startResponseTImeMeasurement();

            binding.testerPanel.tvSentence.setText(String.valueOf(materialsCursor.getInt(materialsCursor.getColumnIndex(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID))));
        } else {
            resetSentenceDisplay();
        }
    }

    // elapsedRealTime for interval timing: monotonicity guaranteed, incremented even in CPU sleep mode,
    // returns ms
    private void startResponseTImeMeasurement() {
        startTimeOfResponse = SystemClock.elapsedRealtime();
        Log.i(this.getClass().getSimpleName(), "Timer started");
    }

    private void stopResponseTimeMeasurement() {

        // RT should fit into an integer unless something went wrong
        long elapsedTime = SystemClock.elapsedRealtime() - startTimeOfResponse;
        if (elapsedTime < Integer.MIN_VALUE || elapsedTime > Integer.MAX_VALUE) {
            responseTime = RESPONSE_TIME_DEFAULT;
        }
        else {
            responseTime = (int)elapsedTime;
        }
    }

    private void resetPreviousResultsOnTestPanel() {
        responseAccuracy = ResponseAccuracy.NO_RESPONSE;
        responseTime = RESPONSE_TIME_DEFAULT;
    }

    public void moveToNextItem() {
        if (materialsCursor == null || materialsCursor.getCount() == 0) {
            throw new IllegalArgumentException("No material found on moving to next sentence");
        }
        if(!materialsCursor.moveToNext()) {
            materialsCursor.moveToFirst();
        }

    }

    public void moveToFirstItem() {
        if (materialsCursor == null || materialsCursor.getCount() == 0 || !materialsCursor.moveToFirst()) {
            throw new IllegalArgumentException("No material found on moving to next sentence");
        }
    }

    private void showResultsOnTestPanel() {

        String info = "";

        switch (responseAccuracy) {
            case WRONG: info ="incorrect";
                break;
            case CORRECT: info = "correct";
            break;
            case NO_RESPONSE: info = "no response yet";
            break;
            default: info = "?";
        }

        if (!responseAccuracy.equals(ResponseAccuracy.NO_RESPONSE)) {
            info += ", " + responseTime + " ms";
        }

        binding.testerPanel.tvInfo.setText(info);
    }

    public void setMaterial(Cursor materialsCursor) {
        this.materialsCursor = materialsCursor;
    }

    @Override
    public void progressToNextLevel(Cursor cursor, int level) {
        materialsCursor = cursor;
        gameState = GameState.PROGRESSED_LEVEL;

        Toast.makeText(getApplicationContext(), "Progressed to level "
                + String.valueOf(gamePlayManager.getCurrentLevel()) + "!", Toast.LENGTH_SHORT).show();
    }

    private void resetSentenceDisplay() {
        binding.sentenceDisplay.btnLeft.setText("");
        binding.sentenceDisplay.btnRight.setText("");
        binding.sentenceDisplay.tvItemStart.setText("");
        binding.sentenceDisplay.tvItemEnd.setText("");
    }


    class TargetItemTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        v);
                v.startDrag(data, shadowBuilder, v, 0);
                return true;
            } else {
                return false;
            }
        }
    }

    class TargetAreaDragListener implements View.OnDragListener {


        @Override
        public boolean onDrag(View v, DragEvent event) {
            //int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:


                    // stop timer here
                    stopResponseTimeMeasurement();

                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    v.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorTargetArea, null));

                    toogleResponseControls(false);
                    // get the dragged items id to check if it is the correct answer
                    View dragView = (View) event.getLocalState();
                    if (dragView.getId() == correctTargetViewId) {
                        responseAccuracy = ResponseAccuracy.CORRECT;
                    } else {
                        responseAccuracy = ResponseAccuracy.WRONG;
                    }

                    onPlayerResponse();

                    break;
                case DragEvent.ACTION_DRAG_ENDED:

                default:
                    break;
            }
            return true;
        }
    }


    private void onPlayerResponse() {
        gameState = GameState.RESPONDED;

        int sentenceID = materialsCursor.getInt(materialsCursor.getColumnIndex(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID));
        boolean accuracy = false;
        if (responseAccuracy.equals(ResponseAccuracy.CORRECT))
            accuracy = true;
        gamePlayManager.onNewPlayerResponse(sentenceID, responseTime, accuracy);

        displayResults();
    }



    private void toogleResponseControls(boolean enabled) {
        binding.sentenceDisplay.btnLeft.setEnabled(enabled);
        binding.sentenceDisplay.btnRight.setEnabled(enabled);
    }

    private void displayResults() {
        ResultSummary resultSummary = gamePlayManager.getResultSummary();
        double accuracyPercent =  Math.round(resultSummary.getAccuracyPercent());
        int rt = resultSummary.getResponseTimeMillis();

        binding.testerPanel.tvAccuracy.setText(String.valueOf(accuracyPercent) + "%");
        binding.testerPanel.tvRt.setText(String.valueOf(rt) + " ms");
    }

    private void displayErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_LEVEL, gamePlayManager.getCurrentLevel());
        outState.putSerializable(KEY_SESSION_STATE, sessionState);
        outState.putSerializable(KEY_GAME_STATE, gameState);

        if (!gameState.equals(GameState.NOT_STARTED)) {
            outState.putStringArray(KEY_MAINTAINED_RESULTS, gamePlayManager.getAccumulatedResults());

            if (gameState.equals(GameState.RESPONDED)) {
                outState.putSerializable(KEY_RESPONSE_ACCURACY, responseAccuracy);
                outState.putInt(KEY_RESPONSE_TIME, responseTime);
            }
        }
    }

    public enum ResponseAccuracy {
        NO_RESPONSE,
        CORRECT,
        WRONG
    }

    public enum GameState {
        NOT_STARTED,
        WAITING_RESPONSE,
        RESPONDED,
        PROGRESSED_LEVEL
    }

    public enum SessionState {
        NOT_STARTED_PLAYER,
        NOT_STARTED_TESTER,
        IN_PROGRESS_PLAYER,
        IN_PROGRESS_TESTER
    }



}
