package com.example.jobscheduler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnScheduleJob, btnCancelJob;
    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScheduleJob = findViewById(R.id.btnScheduleJob);
        btnCancelJob = findViewById(R.id.btnCancelJob);
        txtResult = findViewById(R.id.txtResult);

        btnScheduleJob.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ComponentName componentName = new ComponentName(MainActivity.this,
                        MyJobService.class);
                JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1, componentName);
//                jobInfoBuilder.setRequiresCharging(true);
//                jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
//                jobInfoBuilder.setPeriodic(15 * 60 * 1000);
//                jobInfoBuilder.setPersisted(true);

                JobInfo jobInfo = jobInfoBuilder.build();

                JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                int resultCode = jobScheduler.schedule(jobInfo);
                if (resultCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d("TAG", "Job scheduled");
                } else {
                    Log.d("TAG", "Job failed.");
                }
            }
        });

        btnCancelJob.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                jobScheduler.cancel(1);
                Log.d("TAG", "Job Cancelled");
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("job");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("key");
            txtResult.setText(result);
        }
    };
}