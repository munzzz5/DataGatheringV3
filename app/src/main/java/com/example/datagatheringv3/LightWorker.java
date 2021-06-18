package com.example.datagatheringv3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class LightWorker extends Worker implements SensorEventListener {
    SensorManager sensorManager;
    long timeStarted;
    public static final long SENSOR_EXECUTION_PERIOD=5000;
    public LightWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        timeStarted=System.currentTimeMillis();
        Log.d("Reached Light","Light worker starting");
        sensorManager=(SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_NORMAL);
        return Result.success();
    }
    private String getLight(SensorEvent sensorEvent) {
        float lightLux=sensorEvent.values[0];
        String lightString="#######LIGHT LUX##########\n"+String.valueOf(lightLux)+"\n##########\n";
        return lightString;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long executingTime=System.currentTimeMillis()-timeStarted;
        if(sensorEvent.sensor.getType()==Sensor.TYPE_LIGHT && executingTime<SENSOR_EXECUTION_PERIOD) {
            Log.d("LightWorkerData", getLight(sensorEvent));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        else if(executingTime>=SENSOR_EXECUTION_PERIOD)
        {
            onStopped();
        }
    }
    @Override
    public void onStopped() {
        Log.d("WORKER LIGHT","STOPPING LIGHT");
        //WorkManager.getInstance(getApplicationContext()).cancelWorkById(this.getId());
        sensorManager.unregisterListener(this);
        super.onStopped();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
