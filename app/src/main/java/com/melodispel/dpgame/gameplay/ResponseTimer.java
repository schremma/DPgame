package com.melodispel.dpgame.gameplay;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

public class ResponseTimer implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ResponseTimer createFromParcel(Parcel in) {
            return new ResponseTimer(in);
        }

        public ResponseTimer[] newArray(int size) {
            return new ResponseTimer[size];
        }
    };

    public static final int RESPONSE_TIME_DEFAULT = -1000;

    private long startTimeOfResponse;
    private int responseTime;

   public ResponseTimer() { }

    // elapsedRealTime for interval timing: monotonicity guaranteed, incremented even in CPU sleep mode,
    // returns ms
    public void startResponseTImeMeasurement() {
        startTimeOfResponse = SystemClock.elapsedRealtime();
        Log.i(this.getClass().getSimpleName(), "Timer started");
    }

    public void stopResponseTimeMeasurement() {

        // RT should fit into an integer unless something went wrong
        long elapsedTime = SystemClock.elapsedRealtime() - startTimeOfResponse;
        if (elapsedTime < Integer.MIN_VALUE || elapsedTime > Integer.MAX_VALUE) {
            responseTime = RESPONSE_TIME_DEFAULT;
        }
        else {
            responseTime = (int)elapsedTime;
        }

        Log.i(this.getClass().getSimpleName(), "Timer stopped");
    }

    public long getStartTimeOfResponse() {
        return startTimeOfResponse;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public ResponseTimer(Parcel in) {
        this.responseTime = in.readInt();
        this.startTimeOfResponse = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.responseTime);
        parcel.writeLong(this.startTimeOfResponse);
    }

    public String toString() {
        return("Response time: " +
        String.valueOf(responseTime)) + ", " +
                "start time for resposne measurement: " +
                String.valueOf(startTimeOfResponse);
    }
}
