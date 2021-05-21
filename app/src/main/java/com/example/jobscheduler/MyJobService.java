package com.example.jobscheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyJobService extends JobService {

    private Context context;
    private boolean isJobCancelled = false;
    private Intent intent;

    @Override public void onCreate() {
        super.onCreate();
        this.context = this;
        intent = new Intent("job");
    }

    @Override public boolean onStartJob(JobParameters params) {
        Toast.makeText(context, "Job started.", Toast.LENGTH_SHORT).show();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override public void run() {
                for (int i = 0; i < 50; i++) {
                    if (isJobCancelled) {
                        break;
                    }

                    int data = i;
                    handler.post(new Runnable() {
                        @Override public void run() {
                            intent.putExtra("key", String.valueOf(data));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    });

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return true;
    }

    @Override public boolean onStopJob(JobParameters params) {
        Toast.makeText(context, "Job cancelled.", Toast.LENGTH_SHORT).show();
        isJobCancelled = true;
        return true;
    }

    @Override public void onDestroy() {
        isJobCancelled = true;
        super.onDestroy();
    }
}
