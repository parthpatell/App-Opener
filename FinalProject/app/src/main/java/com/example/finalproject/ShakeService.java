package com.example.finalproject;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class ShakeService extends Service implements SensorEventListener, LocationListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity
    eventDBHelper db;
    boolean vertical, horizontal;
    int rotation;
    long t1, t2, t;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rotation=-1;
        db = new eventDBHelper(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI, new Handler());

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000, 0, this);
            }
            catch(Exception e){}


        Intent notif = new Intent(this, MainActivity.class);
        PendingIntent pend = PendingIntent.getActivity(this,0,notif,0);

        Notification notification = new Notification.Builder(this)
                .setContentText("Final")
                .setContentIntent(pend)
                .build();
        startForeground(1,notification);







        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (rotation == 0) {
                t1 = System.currentTimeMillis();
            }
            try {
                if (Math.abs(x) > 5 && Math.abs(y) < 5 && !horizontal) {
                    vertical = false;
                    horizontal = true;
                    rotation++;
                    SQLiteDatabase shakeOpenDB1 = db.getReadableDatabase();
                    String query1 = "Select * from eventlist Where " + EventContract.EventEntry.COLUMN_ACTION + "='rotate'";
                    Cursor res1 = shakeOpenDB1.rawQuery(query1, null);
                    res1.moveToFirst();
                    final String packageNameM1 = res1.getString(4);
                    System.out.println("Package name is: " + packageNameM1);

                    Intent rotation = new Intent();
                    rotation.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    rotation.setPackage(packageNameM1);
                    startActivity(rotation);
                    Log.d("HI", "horizontal");
                }
                if (Math.abs(x) < 5 && Math.abs(y) > 5 && !vertical) {
                    vertical = true;
                    horizontal = false;
                    rotation++;
                    Log.d("HI", "vertical");
                }
            } catch (CursorIndexOutOfBoundsException e){
                System.out.println(e);
            }
            /*t2 = System.currentTimeMillis();
            t = t2 - t1;
            if (t>1000 && rotation<2) {
                rotation = 0;
            } else if (t<=1000 && rotation==2) {
                Log.d("HI", "Rotated twice in t <= 1000ms");
                rotation = 0;
            } else if (rotation>2) {
                rotation = 0;
            }*/

        }





        if (mAccel > 3) {
            try {
                SQLiteDatabase shakeOpenDB = db.getReadableDatabase();
                String query = "Select * from eventlist Where " + EventContract.EventEntry.COLUMN_ACTION + "='shake'";
                Cursor res = shakeOpenDB.rawQuery(query, null);
                res.moveToFirst();
                final String packageNameM = res.getString(4);
                System.out.println("Package name is: " + packageNameM);


                Intent intent = new Intent();
                intent.setPackage(packageNameM.trim());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (CursorIndexOutOfBoundsException e){
                System.out.println(e);

            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Double mylat= location.getLatitude();
        Double mylng=location.getLongitude();

        //Toast.makeText(this,String.format("%f,%f",mylat,mylng) , Toast.LENGTH_SHORT).show();
        SQLiteDatabase openDB = db.getReadableDatabase();
        Cursor c = openDB.rawQuery("SELECT packageName,location FROM eventlist",null);


        while (c.moveToNext()) {
        String[] latlng=c.getString(1).split(";");
        if(latlng[0].equalsIgnoreCase("none")) {}
        else {
            for (int i = 0; i < latlng.length; i++) {
                String[] z = latlng[i].split(",");
                Double latitude = Double.parseDouble(z[0]);
                Double longitude = Double.parseDouble(z[1]);
                //LatLng loc= new LatLng(latitude,longitude);
                Location loc1 = new Location("");
                loc1.setLatitude(latitude);
                loc1.setLongitude(longitude);
                Location loc2 = new Location("");
                loc2.setLatitude(mylat);
                loc2.setLongitude(mylng);
                float distm = loc1.distanceTo(loc2);

                if (distm < 100) {
                    Intent intent = new Intent();
                    intent.setPackage(c.getString(0).trim());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }


            }
        }


        }



    }

    @Override

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override

    public void onProviderEnabled(String provider) {

    }

    @Override

    public void onProviderDisabled(String provider) {

    }



}


