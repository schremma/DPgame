package com.melodispel.dpgame;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.melodispel.dpgame.GameEnums.GameState;
import com.melodispel.dpgame.GameEnums.ResponseAccuracy;
import com.melodispel.dpgame.GameEnums.SessionState;
import com.melodispel.dpgame.data.DBContract;
import com.melodispel.dpgame.data.DPGamePreferences;
import com.melodispel.dpgame.databinding.ActivityPlayBinding;
import com.melodispel.dpgame.gameplay.GameManager;
import com.melodispel.dpgame.gameplay.GamePlayDisplay;
import com.melodispel.dpgame.gameplay.GamePlayManager;
import com.melodispel.dpgame.gameplay.ResponseTimer;
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
    private static final String KEY_MAINTAINED_RESULTS = "maintainedResultsKey";
    private static final String KEY_RESPONSE_TIMER = "responseTimer";

    private static final int LEVEL_DEFAULT = 1;
    private static final SessionState SESSION_STATE_DEFAULT = SessionState.NOT_STARTED_PLAYER;
    
    private SessionState sessionState;
    private GameState gameState;

    private ResponseAccuracy responseAccuracy;
    private ResponseTimer responseTimer;

    private boolean showInitialInstructions = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DPGamePreferences.applyPreferredAppLanguage(this);

        Intent intent = getIntent();

        int currentLevel = savedInstanceState != null ?
                savedInstanceState.getInt(KEY_LEVEL, LEVEL_DEFAULT) : -1;

        if (currentLevel == -1) {
            currentLevel = intent.getIntExtra(LevelListActivity.EXTRA_LEVEL, LEVEL_DEFAULT);
        }

        if (savedInstanceState !=null) {

            responseTimer = (ResponseTimer) savedInstanceState.getParcelable(KEY_RESPONSE_TIMER);
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

        if (responseTimer == null)
            responseTimer = new ResponseTimer();

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

                if (gameState.equals(GameState.RESPONDED)) {
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

        binding.sentenceDisplay.playSentenceArea.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeLeft() {
                if (gameState.equals(GameState.RESPONDED) || gameState.equals(GameState.PROGRESSED_LEVEL)) {
                    continueWithNextItem();
                }
            }
        });

        binding.sentenceDisplay.btnLeft.setOnTouchListener(new PlayActivity.TargetItemTouchListener());
        binding.sentenceDisplay.btnRight.setOnTouchListener(new PlayActivity.TargetItemTouchListener());
        binding.sentenceDisplay.targetArea.setOnDragListener(new PlayActivity.TargetAreaDragListener());

        if (sessionState.equals(SessionState.NOT_STARTED_PLAYER)) {
            showInitialInstructions = true;
            showGameInstructions(getString(R.string.instruction_play_target_area));
        }

        toogleResponseControls(false);

    }


    private void startGame() {
        toogleResponseControls(true);
/*        if (!hasNextSentenceItem()) {
            moveToFirstItem();
        }*/
        gameState = GameState.WAITING_RESPONSE;
        showCurrentSentence();
        responseTimer.startResponseTImeMeasurement();
        showGameInstructions(getString(R.string.instruction_play_default));
    }

    private void continueWithNextItem() {
        moveToNextItem();
        gameState = GameState.WAITING_RESPONSE;
        toogleResponseControls(true);
        showCurrentSentence();
        responseTimer.startResponseTImeMeasurement();
    }

    private boolean hasNextSentenceItem() {
        return !materialsCursor.isAfterLast();
    }

    private void showCurrentSentence() {


        if (materialsCursor != null || materialsCursor.getPosition() >= 0) {
            binding.sentenceDisplay.targetArea.setText("");
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

            if (gameState.equals(GameState.RESPONDED)) {
                showCorrectChoice(materialsCursor.getString(materialsCursor.
                        getColumnIndex(DBContract.MaterialsEntry.COLUMN_CORRECT)));
                if (responseAccuracy.equals(ResponseAccuracy.CORRECT)) {
                    binding.sentenceDisplay.targetArea.setBackgroundColor(getResources().getColor(R.color.colorTargetAreaCorrect));
                }
            } else {
                binding.sentenceDisplay.targetArea.setBackgroundColor(getResources().getColor(R.color.colorTargetArea));
            }

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

    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    public void moveToFirstItem() {
        if (materialsCursor == null || materialsCursor.getCount() == 0 || !materialsCursor.moveToFirst()) {
            throw new IllegalArgumentException("No material found on moving to next sentence");
        }
    }

    private void showResultsOnTestPanel() {

        String accuracyInfo = "";

        switch (responseAccuracy) {
            case WRONG: accuracyInfo = getString(R.string.result_play_incorrect_response);
                break;
            case CORRECT: accuracyInfo = getString(R.string.result_play_correct_response);
                break;
            case NO_RESPONSE: accuracyInfo = getString(R.string.result_play_no_response);
                break;
            default: accuracyInfo = "?";
        }

        binding.testerPanel.tvInfo.setText(accuracyInfo);

        if (!responseAccuracy.equals(ResponseAccuracy.NO_RESPONSE)) {
            String rtInfo = responseTimer.getResponseTime() + " " + getString(R.string.response_time_unit);
            binding.testerPanel.tvSentence.setText(rtInfo);
        }

    }

    public void setMaterial(Cursor materialsCursor) {
        Cursor old = this.materialsCursor;
        this.materialsCursor = materialsCursor;

        if (old !=null) {
            old.close();
        }
    }

    @Override
    public void progressToNextLevel(Cursor cursor, int level) {
        Cursor old = this.materialsCursor;
        materialsCursor = cursor;

        if (old !=null) {
            old.close();
        }
        gameState = GameState.PROGRESSED_LEVEL;

        Toast.makeText(this, "Progressed to level "
                + String.valueOf(gamePlayManager.getCurrentLevel()) + "!", Toast.LENGTH_SHORT).show();
    }

    private void resetSentenceDisplay() {
        binding.sentenceDisplay.btnLeft.setText("");
        binding.sentenceDisplay.btnRight.setText("");
        binding.sentenceDisplay.tvItemStart.setText("");
        binding.sentenceDisplay.tvItemEnd.setText("");
        binding.sentenceDisplay.targetArea.setText("");
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

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:


                    // stop timer here
                    responseTimer.stopResponseTimeMeasurement();

                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //v.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));
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

        showCorrectChoice(materialsCursor.getString(materialsCursor.
                getColumnIndex(DBContract.MaterialsEntry.COLUMN_CORRECT)));

        int sentenceID = materialsCursor.getInt(materialsCursor.getColumnIndex(DBContract.MaterialsEntry.COLUMN_SENTENCE_ID));
        boolean accuracy = false;
        if (responseAccuracy.equals(ResponseAccuracy.CORRECT))
            accuracy = true;
        gamePlayManager.onNewPlayerResponse(sentenceID, responseTimer.getResponseTime(), accuracy);

        if (showInitialInstructions) {
            binding.sentenceDisplay.targetArea.setText(getString(R.string.instruction_play_continue));
            showInitialInstructions = false;
        }

        displayResults();
        showResultsOnTestPanel();
    }


    private void showCorrectChoice(String correctChoice) {
        binding.sentenceDisplay.targetArea.setText(correctChoice);
        if (responseAccuracy.equals(ResponseAccuracy.CORRECT)) {
            binding.sentenceDisplay.targetArea.setBackgroundColor(getResources().getColor(R.color.colorTargetAreaCorrect));
        }
    }

    private void toogleResponseControls(boolean enabled) {
        binding.sentenceDisplay.btnLeft.setEnabled(enabled);
        binding.sentenceDisplay.btnRight.setEnabled(enabled);
    }

    private void showGameInstructions(String instruction) {
        if (instruction != null) {
            binding.sentenceDisplay.targetArea.setText(instruction);
        }
    }

    private void displayResults() {
        ResultSummary resultSummary = gamePlayManager.getResultSummary();
        double accuracyPercent =  Math.round(resultSummary.getAccuracyPercent());
        int rt = resultSummary.getResponseTimeMillis();

        binding.testerPanel.tvAccuracy.setText(String.valueOf(accuracyPercent) + getString(R.string.accuracy_unit));
        binding.testerPanel.tvRt.setText(String.valueOf(rt) + " "  + getString(R.string.response_time_unit));
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

        if (!gameState.equals(GameEnums.GameState.NOT_STARTED)) {
            outState.putStringArray(KEY_MAINTAINED_RESULTS, gamePlayManager.getAccumulatedResults());
            outState.putSerializable(KEY_RESPONSE_ACCURACY, responseAccuracy);
        }

        outState.putParcelable(KEY_RESPONSE_TIMER, responseTimer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (materialsCursor !=null) {
            materialsCursor.close();
        }
    }
}


