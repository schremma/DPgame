package com.melodispel.dpgame.gameplay;

public class ResultSummary {

    private double accuracyPercent;
    private int responseTimeMillis;

    public ResultSummary(double accuracyPercent, int responseTimeMillis) {
        this.accuracyPercent = accuracyPercent;
        this.responseTimeMillis = responseTimeMillis;
    }

    public double getAccuracyPercent() {
        return accuracyPercent;
    }

    public void setAccuracyPercent(double accuracyPercent) {
        this.accuracyPercent = accuracyPercent;
    }

    public int getResponseTimeMillis() {
        return responseTimeMillis;
    }

    public void setResponseTimeMillis(int responseTimeMillis) {
        this.responseTimeMillis = responseTimeMillis;
    }
}
