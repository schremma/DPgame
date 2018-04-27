package com.melodispel.dpgame;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.melodispel.dpgame.data.DBContract;
import com.melodispel.dpgame.databinding.ActivityTestBinding;
import com.melodispel.dpgame.gameplay.GamePlayDisplay;
import com.melodispel.dpgame.gameplay.GamePlayManager;
import com.melodispel.dpgame.gameplay.ResponseTimer;
import com.melodispel.dpgame.gameplay.ResultSummary;
import com.melodispel.dpgame.gameplay.TestPlayManager;

public class TestActivity extends AppCompatActivity implements GamePlayDisplay {

    private GamePlayManager gamePlayManager;
    ActivityTestBinding binding;

    private Cursor materialsCursor;
    private int correctTargetViewId;

    private static final String KEY_LEVEL = "levelKey";
    private static final String KEY_GAME_STATE = "gameStateKey";
    private static final String KEY_SESSION_STATE = "sessionStateKey";
    private static final String KEY_RESPONSE_ACCURACY = "accuracyKey";
    private static final String KEY_MAINTAINED_RESULTS = "maintainedResultsKey";
    private static final String KEY_LAST_ITEM_ID = "lastItemId";
    private static final String KEY_RESPONSE_TIMER = "responseTimer";

    private static final int LEVEL_DEFAULT = 1;
    private static final GameEnums.SessionState SESSION_STATE_DEFAULT = GameEnums.SessionState.NOT_STARTED_TESTER;

    private GameEnums.SessionState sessionState;
    private GameEnums.GameState gameState;

    private int lastItemId;
    private GameEnums.ResponseAccuracy responseAccuracy;

    private ResponseTimer responseTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        int currentLevel = savedInstanceState != null ?
                savedInstanceState.getInt(KEY_LEVEL, LEVEL_DEFAULT) : -1;
        lastItemId = savedInstanceState != null ?
                savedInstanceState.getInt(KEY_LAST_ITEM_ID, -1) : -1;


        Log.i(getClass().getSimpleName(), "last item id: " + lastItemId);

        if (currentLevel == -1) {
            currentLevel = intent.getIntExtra(LevelListActivity.EXTRA_LEVEL, LEVEL_DEFAULT);
        }

        if (savedInstanceState !=null) {
            responseTimer = (ResponseTimer) savedInstanceState.getParcelable(KEY_RESPONSE_TIMER);
            sessionState = savedInstanceState.containsKey(KEY_SESSION_STATE) ?
                    (GameEnums.SessionState) savedInstanceState.get(KEY_SESSION_STATE) : SESSION_STATE_DEFAULT;
            gameState = savedInstanceState.containsKey(KEY_GAME_STATE) ?
                    (GameEnums.GameState) savedInstanceState.get(KEY_GAME_STATE) : GameEnums.GameState.NOT_STARTED;
            responseAccuracy = savedInstanceState.containsKey(KEY_RESPONSE_ACCURACY) ?
                    (GameEnums.ResponseAccuracy) savedInstanceState.get(KEY_RESPONSE_ACCURACY) : GameEnums.ResponseAccuracy.NO_RESPONSE;

        }
        else {
            gameState = GameEnums.GameState.NOT_STARTED;
            sessionState = SESSION_STATE_DEFAULT;
            responseAccuracy = GameEnums.ResponseAccuracy.NO_RESPONSE;
        }
        if (responseTimer == null)
            responseTimer = new ResponseTimer();
        Log.i(getClass().getName(), responseTimer.toString());

        initGameArea();

        gamePlayManager = new TestPlayManager(this, this, currentLevel, lastItemId);

        if (sessionState.equals(GameEnums.SessionState.NOT_STARTED_TESTER)) {

            gamePlayManager.onNewSessionStarted(null);
            sessionState = GameEnums.SessionState.IN_PROGRESS_TESTER;
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_MAINTAINED_RESULTS)) {
            gamePlayManager.setAccumulatedResults(savedInstanceState.getStringArray(KEY_MAINTAINED_RESULTS));
        }


        if (gamePlayManager.onGameInitialized()) {

            displayResults();

            if (!gameState.equals(GameEnums.GameState.NOT_STARTED)) {

                if (gameState.equals(gameState.RESPONDED)) {
                    toogleResponseControls(false);
                } else {
                    toogleResponseControls(true);
                }
                showResultsOnTestPanel();
                showCurrentSentence();

            }
        } else  {
            displayErrorMessage("Could not initialize game!");
        }

    }

    private void initGameArea() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test);
        binding.sentenceDisplay.targetArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameState.equals(GameEnums.GameState.NOT_STARTED)) {
                    startGame();
                } else if (gameState.equals(GameEnums.GameState.RESPONDED)) {
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
        toogleResponseControls(true);
        showCurrentSentence();
        responseTimer.startResponseTImeMeasurement();
        gameState = GameEnums.GameState.WAITING_RESPONSE;
    }

    private void continueWithNextItem() {
        moveToNextItem();
        toogleResponseControls(true);
        showCurrentSentence();
        responseTimer.startResponseTImeMeasurement();
        gameState = GameEnums.GameState.WAITING_RESPONSE;
    }

    private void showCurrentSentence() {


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

            binding.testerPanel.tvSentence.setText(String.valueOf(materialsCursor.getInt(materialsCursor.getColumnIndex(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID))));
        } else {
            resetSentenceDisplay();
        }
    }


    public void moveToNextItem() {
        if (materialsCursor == null || materialsCursor.getCount() == 0) {
            throw new IllegalArgumentException("No material found on moving to next sentence");
        }
        if(!materialsCursor.moveToNext()) {
            materialsCursor.moveToFirst();
        }

        lastItemId = materialsCursor.getInt(materialsCursor.getColumnIndex(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID));

    }

    @Override
    public GameEnums.GameState getGameState() {
        return gameState;
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

        if (!responseAccuracy.equals(GameEnums.ResponseAccuracy.NO_RESPONSE)) {
            info += ", " + responseTimer.getResponseTime() + " ms";
        }

        binding.testerPanel.tvInfo.setText(info);
    }

    public void setMaterial(Cursor materialsCursor) {
        this.materialsCursor = materialsCursor;
    }

    @Override
    public void progressToNextLevel(Cursor cursor, int level) {

        throw new RuntimeException("Progress to next level is not implemented for Test sessions");
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
                    responseTimer.stopResponseTimeMeasurement();

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
                        responseAccuracy = GameEnums.ResponseAccuracy.CORRECT;
                    } else {
                        responseAccuracy = GameEnums.ResponseAccuracy.WRONG;
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
        gameState = GameEnums.GameState.RESPONDED;

        int sentenceID = materialsCursor.getInt(materialsCursor.getColumnIndex(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID));
        boolean accuracy = false;
        if (responseAccuracy.equals(GameEnums.ResponseAccuracy.CORRECT))
            accuracy = true;
        gamePlayManager.onNewPlayerResponse(sentenceID, responseTimer.getResponseTime(), accuracy);

        displayResults();
        showResultsOnTestPanel();
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
        outState.putInt(KEY_LAST_ITEM_ID, lastItemId);

        Log.i(getClass().getSimpleName(), "Saved last item id: " + lastItemId);

        outState.putSerializable(KEY_SESSION_STATE, sessionState);
        outState.putSerializable(KEY_GAME_STATE, gameState);

        if (!gameState.equals(GameEnums.GameState.NOT_STARTED)) {
            outState.putStringArray(KEY_MAINTAINED_RESULTS, gamePlayManager.getAccumulatedResults());
            outState.putSerializable(KEY_RESPONSE_ACCURACY, responseAccuracy);
        }

        outState.putParcelable(KEY_RESPONSE_TIMER, responseTimer);
    }

}
