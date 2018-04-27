package com.melodispel.dpgame;

public class GameEnums {

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
