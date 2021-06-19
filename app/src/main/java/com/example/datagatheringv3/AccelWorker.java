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
import com.example.datagatheringv3.utils.saveDataUtil;

import java.util.Calendar;

public class AccelWorker extends Worker implements SensorEventListener {
    SensorManager sensorManager;
    long timeStarted;

    StringBuilder sb;
    public static final long SENSOR_EXECUTION_PERIOD=5000;
    public AccelWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        timeStarted=System.currentTimeMillis();
        Log.d("REACHED ACCEL","WORKER STARTING");
        sb=new StringBuilder();
        sensorManager=(SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_NORMAL);

        return Result.success();
    }
    @Override
    public void onStopped() {
        Log.d("WORKER ACCEL","STOPPING ACCEL");
        //WorkManager.getInstance(getApplicationContext()).cancelWorkById(this.getId());
        sensorManager.unregisterListener(this);
        sb=null;
        super.onStopped();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        long executingTime=System.currentTimeMillis()-timeStarted;
        Log.d("TIMEEXECUTION",String.valueOf(executingTime));
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER && executingTime<SENSOR_EXECUTION_PERIOD) {
            Calendar cal=Calendar.getInstance();
            sb.append("--------ACCELEROMETER-----"+cal.getTime().toString()+"\n"+getAccelerometer(sensorEvent));
            Log.d("SensorValuesAccel", "accel data saving");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //onStopped();
        }
        else if(executingTime>=SENSOR_EXECUTION_PERIOD)
        {
            saveDataUtil.saveData(sb.toString(),getApplicationContext(),"accelerometer.txt");
            onStopped();
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

    private String getAccelerometer(SensorEvent sensorEvent) {
        float[] values=sensorEvent.values;


        float x=values[0];
        float y=values[1];
        float z=values[2];
        String s="";
        if((x>5||x<-5) && y<2 && z<2){
            s="Screen facing you and horizontal phone";
        }
        else if((y>5||y<-5) && x<2 && z<2)
        {
            s="Screen facing you and verticle phone";
        }
        else if((z>5||z<-5) && y<2 && x<2){
            s="screen bottom//screen top";
        }
        else if(y>5 && z>5 && x<2)
        {
            s="Viewing angle!";
        }
        else if(x>5 && z>5 && y<2)
        {
            s="Movie Angle!";
        }
        s+="\nX="+x+"\nY="+y+"\nZ="+z;




        return s;



    }

}
