package com.melodispel.dpgame.gameplay;

import android.database.Cursor;

public interface GamePlayDisplay {

    void setMaterial(Cursor materialsCursor);
    void progressToNextLevel(Cursor cursor, int level);
    void moveToFirstItem();
    void moveToNextItem();
}
