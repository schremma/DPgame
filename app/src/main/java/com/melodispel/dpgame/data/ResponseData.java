package com.melodispel.dpgame.data;


import com.melodispel.dpgame.PlayActivity;

public class ResponseData {

    private int sentenceId;
    private int accuracy;
    private int RT;
    private long timestamp;
    private int level;

    public int getSentenceId() {
        return sentenceId;
    }

    public void setSentenceId(int sentenceId) {
        this.sentenceId = sentenceId;
    }

    public boolean getAccuracy() {
        return accuracy == 1;
    }

    public void setAccuracy(boolean isCorrect) {
        if (isCorrect)
            accuracy = 1;
        else
            accuracy = 0;
    }

    public int getRT() {
        return RT;
    }

    public void setRT(int RT) {
        this.RT = RT;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }


}
