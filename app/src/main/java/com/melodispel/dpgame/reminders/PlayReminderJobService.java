package com.melodispel.dpgame.reminders;

import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class PlayReminderJobService extends JobService {

    private AsyncTask backgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {


        backgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                long schedulingTime = job.getExtras().getLong(ReminderUtilities.EXTRA_SCHEDULING_TIME);
                ReminderTasks.remindOfPlaying(PlayReminderJobService.this, schedulingTime);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                /*
                 * Informs JobManager that the job is done,
                 * with a boolean representing whether the job needs to be rescheduled -
                 * usually needed if something didn't work and the job needs to be run again
                 */
                jobFinished(job, false);
            }
        };

        backgroundTask.execute();
        return false; // Answers the question: "Is there still work going on?"
    }

    /**
     * Usually called if the scheduling engine interrupted execution of the job
     * @param job
     * @return
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        if (backgroundTask != null) {
            backgroundTask.cancel(true);
        }

        return false; // Answers the question: "Should this job be retried?"
    }
}
