package com.example.datagatheringv3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.datagatheringv3.utils.saveDataUtil;

import java.util.Calendar;

public class stepCountWorker extends Worker implements SensorEventListener {
    SensorManager sensorManager;
    long timeStarted;

    StringBuilder sb;
    public static final long SENSOR_EXECUTION_PERIOD=5000;


    public stepCountWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {

        timeStarted=System.currentTimeMillis();
        Log.d("REACHED STEPS","WORKER STARTING");
        sb=new StringBuilder();
        sensorManager=(SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_NORMAL);

        return ListenableWorker.Result.success();
    }
    @Override
    public void onStopped() {
        Log.d("WORKER STEPS","STOPPING STEPS");
        //WorkManager.getInstance(getApplicationContext()).cancelWorkById(this.getId());
        sensorManager.unregisterListener(this);
        sb=null;
        super.onStopped();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long executingTime=System.currentTimeMillis()-timeStarted;
        Log.d("TIMEEXECUTION",String.valueOf(executingTime));
        if(sensorEvent.sensor.getType()==Sensor.TYPE_STEP_COUNTER && executingTime<SENSOR_EXECUTION_PERIOD) {
            Calendar cal=Calendar.getInstance();
            sb.append("--------Steps-----"+cal.getTime().toString()+"\n"+getSteps(sensorEvent));
            Log.d("SensorValuesSteps", "Steps data saving");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            saveDataUtil.saveData(sb.toString(),getApplicationContext(),"steps.txt");
            onStopped();
            //onStopped();
        }
        else if(executingTime>=SENSOR_EXECUTION_PERIOD)
        {

        }
//        else if(sensorEvent.sensor.getType()==Sensor.TYPE_LIGHT)
//        {
//            Log.d("SensorValuesLight",getLight(sensorEvent));
//            //onStopped();
//        }



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public String getSteps(SensorEvent sensorEvent)
    {
        return String.valueOf(sensorEvent.values[0]);
    }


}
