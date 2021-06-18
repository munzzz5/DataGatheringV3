package com.example.datagatheringv3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;

import com.example.datagatheringv3.BroadReceieve.transitionReceiver;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class foregroundWorkCaller extends Service {
    private static final String WORK_ACCEL_ID = "ACCELEROMETER_WORKER";
    private static final String WORK_LIGHT_ID = "LIGHT_WORKER";
    private static final String WORK_STEPS_ID = "STEPS_WORKER";
    private static final String WORK_GYRO_ID = "GYRO_WORKER";
    private transitionReceiver broadcastReceiver;
    private List<ActivityTransition> activityTransitionList;
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";
    private PendingIntent pendingIntentForActivityListener;

    @Override
    public void onCreate() {
        WorkManager.getInstance(this).cancelAllWork();
        createNotificationChannel();
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("OnStartCommand","Foreground Starting");
        Intent intent1=new Intent(this,AccelWorker.class);
        //SecondWorker
        Intent intent2=new Intent(this,LightWorker.class);
        Intent intentNotification=new Intent(this,MainActivity.class);
        PendingIntent piNotif=PendingIntent.getActivity(getApplicationContext(),0,intentNotification,0);
        //PendingIntent piWorker=PendingIntent.getService(getApplicationContext(),0,intent1,0);
        //PendingIntent piWorker2=PendingIntent.getService(getApplicationContext(),0,intent2,0);

        Notification notif=new NotificationCompat.Builder(getApplicationContext(),"MyDataGatheringV3")
                .setContentTitle("Data Gathering")
                .setContentText("This is collecting light and accel")
                .setContentIntent(piNotif)
                .build();
        startForeground(1,notif);

        broadcastReceiver=new transitionReceiver();
        startReceiverForActivityListener();
        activityTransitionList = new ArrayList<>();
        addActivitiesList();
        Intent intent4 = new Intent(TRANSITIONS_RECEIVER_ACTION);
        pendingIntentForActivityListener =
                PendingIntent.getBroadcast(foregroundWorkCaller.this, 0, intent4, 0);
        startWorker();
        ActivityTransitionRequest request = new ActivityTransitionRequest(activityTransitionList);
        Task<Void> task =
                ActivityRecognition.getClient(this)
                        .requestActivityTransitionUpdates(request, pendingIntentForActivityListener);



        return START_STICKY;




    }
    public void addActivitiesList()
    {
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    public void startReceiverForActivityListener()
    {
        registerReceiver(broadcastReceiver,new IntentFilter(TRANSITIONS_RECEIVER_ACTION));
    }
    public void createWorkRequest()
    {
        Log.d("Work Request","Creating Work Request");
        PeriodicWorkRequest periodicWorkRequest=new PeriodicWorkRequest.Builder(AccelWorker.class,15,TimeUnit.MINUTES)
                .setInitialDelay(500,TimeUnit.MILLISECONDS)
                .build();
        //SecondWorker
        PeriodicWorkRequest periodicWorkRequest1=new PeriodicWorkRequest.Builder(LightWorker.class,15,TimeUnit.MINUTES)
                .setInitialDelay(3000,TimeUnit.MILLISECONDS)
                .build();
//        PeriodicWorkRequest periodicWorkRequest2=new PeriodicWorkRequest.Builder(stepCountWorker.class,15,TimeUnit.MINUTES)
//                .setInitialDelay(6000,TimeUnit.MILLISECONDS)
//                .build();
        PeriodicWorkRequest periodicWorkRequest3=new PeriodicWorkRequest.Builder(gyroWorker.class,15,TimeUnit.MINUTES)
                .setInitialDelay(9000,TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_ACCEL_ID, ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest);
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_LIGHT_ID, ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest1);
        //WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_STEPS_ID, ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest2);
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_GYRO_ID, ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest3);
    }

    public void startWorker(){
        if(getStateOfWorker() != WorkInfo.State.ENQUEUED && getStateOfWorker() != WorkInfo.State.RUNNING)
        {
            Log.wtf("Server starting","Starting Accel");
            createWorkRequest();
        }
        else
        {
            Log.wtf("Server Running","Not Starting Accel");
        }
    }
    private WorkInfo.State getStateOfWorker()
    {
        try
        {
            if(WorkManager.getInstance(this).getWorkInfosForUniqueWork(WORK_ACCEL_ID).get().size()>0)
            {
                return WorkManager.getInstance(this).getWorkInfosForUniqueWork(WORK_ACCEL_ID).get().get(0).getState();
            }
            else
            {
                return WorkInfo.State.CANCELLED;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return WorkInfo.State.CANCELLED;
        }
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "MyDataGatheringV3",
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public long timeSettingForAlarm(int hour,int minute,int second)
    {
        Calendar calendar=Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,second);
        Date d=calendar.getTime();
        long millis=d.getTime();
        long currentMillis=System.currentTimeMillis();
        while(currentMillis>millis)
        {
            millis=millis+24*60*60*1000;
        }
        Log.d("TIME CURRENT MILLIS",Long.toString(currentMillis));
        Log.d("TIME SET TIME MILLIS",Long.toString(millis));

        return millis;
    }
}
