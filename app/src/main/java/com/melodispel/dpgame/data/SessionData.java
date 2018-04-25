package com.melodispel.dpgame.data;

public class SessionData {

    private long startTimeStamp;
    private int level;
    private String sessionCustoms;
    private int isPlayerSession;

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSessionCustoms() {
        return sessionCustoms;
    }

    public void setSessionCustoms(String sessionCustoms) {
        this.sessionCustoms = sessionCustoms;
    }

    public boolean getIsPlayerSession() {
        return isPlayerSession == 1;
    }

    public void setIsPlayerSession(boolean isPlayerSession) {
        if (isPlayerSession) {
            this.isPlayerSession = 1;
        } else {
            this.isPlayerSession = 0;
        }
    }
}
