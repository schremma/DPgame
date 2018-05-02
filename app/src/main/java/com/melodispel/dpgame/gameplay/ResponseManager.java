package com.melodispel.dpgame.gameplay;

import android.util.Log;

import java.util.LinkedList;


// TODO create an interface implemented by ResponseManager
public class ResponseManager {

    private LinkedList<Integer> rtQueue = new LinkedList<>();
    private LinkedList<Integer> accuracyQueue = new LinkedList<>();
    private int cumulatedRT;
    private int cumulatedAccuracy;

    public final int numberOfResponsesForResultCalculation;
    public final int queue_max_limit;
    public final int accuracyLimitPercentage;
    public static final String QUEUE_STRING_SEPARATOR = ",";

    public ResponseManager() {
        numberOfResponsesForResultCalculation = 4;
        accuracyLimitPercentage = 80;
        queue_max_limit = numberOfResponsesForResultCalculation;
    }

    public ResponseManager(int numberOfResponsesForResultCalculation, int accuracyLimitPercentage) {
        this.numberOfResponsesForResultCalculation = numberOfResponsesForResultCalculation;
        this.accuracyLimitPercentage = accuracyLimitPercentage;
        this.queue_max_limit = numberOfResponsesForResultCalculation;

    }

    public double getAverageRT() {
        if (rtQueue.size() > 0) {
            //Log.i(getClass().getSimpleName(), "Queue size: " + rtQueue.size() + " cumulated RT: " + cumulatedRT);
            return (double)cumulatedRT / rtQueue.size();
        }
        return 0;
    }

    /**
     * @return Average accuracy performance in percentage, i.e. between 0.0 and 100.0
     */
    public double getAverageAccuracy() {
        if (accuracyQueue.size() > 0) {
            //Log.i(getClass().getSimpleName(), "Queue size: " + accuracyQueue.size() + " cumulated accuracy: " + cumulatedAccuracy);

            return ((double)cumulatedAccuracy / accuracyQueue.size()) * 100;
        }
        return 0;

    }

    public int getAccumulatedNbrOfResponses() {
        return rtQueue.size();
    }

    public boolean hasReachedProgressLimit() {
        return (accuracyQueue.size() >= numberOfResponsesForResultCalculation &&
                Math.round(getAverageAccuracy()) >= accuracyLimitPercentage);
    }

    public void addResponse(int rt, boolean accuracy) {
        //Log.i(getClass().getSimpleName(), "Adding RT: " + rt);


        if (rtQueue.size() >= numberOfResponsesForResultCalculation) {
            cumulatedRT = cumulatedRT - rtQueue.getFirst();
            rtQueue.removeFirst();

            cumulatedAccuracy = cumulatedAccuracy - accuracyQueue.getFirst();
            accuracyQueue.removeFirst();
        }

        rtQueue.addLast(rt);
        cumulatedRT = cumulatedRT + rt;

        if (accuracy) {
            accuracyQueue.addLast(1);
            cumulatedAccuracy = cumulatedAccuracy + 1;
        } else {
            accuracyQueue.addLast(0);
        }
    }

    public void reset() {
        accuracyQueue.clear();
        rtQueue.clear();
        cumulatedAccuracy = 0;
        cumulatedRT = 0;
    }

    public String accuracyValuesToString() {
        return queueToString(accuracyQueue);
    }

    public String rtValuesToString() {
        return queueToString(rtQueue);
    }

    public void setAccuracyValuesFromString(String accuracyValues) {
        if (accuracyValues == null) {
            throw new IllegalArgumentException("Provided accuracy value string cannot be null!");
        }
        if (accuracyQueue == null) {
            accuracyQueue = new LinkedList<>();
        } else if (accuracyQueue.size() > 0){
                accuracyQueue.clear();
        }
        stringToQueue(accuracyValues, accuracyQueue);
        updateCumulatedAccuracyFromQueue();

        Log.i(getClass().getSimpleName(), "Updated cumulated accuracy: " + String.valueOf(cumulatedAccuracy));
    }

    public void setResponseTimeValuesFromString(String responseTimesValues) {
        if (responseTimesValues == null) {
            throw new IllegalArgumentException("Provided response times value string cannot be null!");
        }
        if (rtQueue == null) {
            rtQueue = new LinkedList<>();
        } else if (rtQueue.size() > 0){
            rtQueue.clear();
        }

        stringToQueue(responseTimesValues, rtQueue);
        updateCumulatedRtFromQueue();

        Log.i(getClass().getSimpleName(), "Updated cumulated rt: " + String.valueOf(cumulatedRT));

    }

    private void stringToQueue(String values, LinkedList<Integer> queue) {
        if (values != null && queue != null) {
            String[] items = values.split(QUEUE_STRING_SEPARATOR);

            for(int i = 0; i < items.length; i++) {
                if (queue.size() >= queue_max_limit) {
                    throw new IllegalArgumentException("Provided String values exceed queue limit");
                }
                if (items[i]!= null && items[i].length() > 0 ) {
                    queue.addLast(Integer.parseInt(items[i]));
                }
            }
        }
    }

    private void updateCumulatedRtFromQueue() {
        cumulatedRT = 0;
        if (rtQueue != null && rtQueue.size() > 0) {
            for(int value : rtQueue) {
                cumulatedRT = cumulatedRT+value;
            }
        }
    }

    private void updateCumulatedAccuracyFromQueue() {
        cumulatedAccuracy = 0;
        if (accuracyQueue != null && accuracyQueue.size() > 0) {
            for(int value : accuracyQueue) {
                cumulatedAccuracy = cumulatedAccuracy+value;
            }
        }
    }

    private String queueToString(LinkedList<Integer> queue) {
        if (queue != null) {
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < queue.size(); i++) {
                stringBuilder.append(queue.get(i));
                if (i < queue.size() - 1)
                    stringBuilder.append(QUEUE_STRING_SEPARATOR);
            }

            return stringBuilder.toString();
        }
        return null;
    }

}
