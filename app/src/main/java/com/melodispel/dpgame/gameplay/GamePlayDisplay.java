package com.melodispel.dpgame.gameplay;

import android.database.Cursor;

import com.melodispel.dpgame.GameEnums;

public interface GamePlayDisplay {

    void setMaterial(Cursor materialsCursor);
    void progressToNextLevel(Cursor cursor, int level);
    void moveToNextItem();
    GameEnums.GameState getGameState();
}
