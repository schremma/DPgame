package com.melodispel.dpgame.gameplay;

public interface GamePlayManager {

    void onNewSessionStarted(boolean isPlayerSession, String sessionCustoms);
    boolean onGameInitialized();
    void onNewPlayerResponse(int sentenceID, int responseTime, boolean accuracy);
    int getCurrentLevel();
    ResultSummary getResultSummary();
    String[] getAccumulatedResults();
    void setAccumulatedResults(String[] results);

}
