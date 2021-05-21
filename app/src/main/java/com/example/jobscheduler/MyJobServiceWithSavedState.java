package com.example.jobscheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyJobServiceWithSavedState extends JobService {

    private Context context;
    private Intent intent;
    private boolean isJobCancelled = false;
    private JobParameters params;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override public void onCreate() {
        super.onCreate();

        context = this;
        intent = new Intent("job");
        sharedPreferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override public boolean onStartJob(JobParameters params) {
        Toast.makeText(context, "Job started.", Toast.LENGTH_SHORT).show();

        this.params = params;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        final int[] data = new int[1];

        executorService.execute(new Runnable() {
            @Override public void run() {
                //background task
                //compute starting point of the for loop
                int j = 0;
                int size = 50;
                if (sharedPreferences.getInt("lastSavedValue", 0) != 0) {
                    j = sharedPreferences.getInt("lastSavedValue", 0);
                }


                for (int i = j; i < size; i++) {

                    //save the latest iteration in case app exits.
                    //when app starts again, start for loop iteration from this point.
                    editor.putInt("lastSavedValue", i);
                    editor.commit();

                    if (isJobCancelled) {
                        break;
                    }

                    Log.d("TAG", String.valueOf(i));
                    data[0] = i;

                    int finalI = i;
                    handler.post(new Runnable() {
                        @Override public void run() {
                            //ui task
                            intent.putExtra("key", String.valueOf(data[0]));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                            //when all iterations are over, finish the job
                            if (finalI == (size-1)) {
                                notifyJobFinished();
                            }
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
        jobFinished(params, false);
        return true;
    }

    private void notifyJobFinished() {
        Toast.makeText(context, "Job finished", Toast.LENGTH_SHORT).show();
        Log.d("TAG", "Job finished");
        jobFinished(params, false);

        //reset the for loop iteration starting point to 0
        editor.putInt("lastSavedValue",0);
        editor.commit();
    }

    @Override public void onDestroy() {
        Toast.makeText(context, "job destroyed", Toast.LENGTH_SHORT).show();
        isJobCancelled = true;
        super.onDestroy();
    }
}
