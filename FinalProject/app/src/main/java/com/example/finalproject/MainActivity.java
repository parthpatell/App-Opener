package com.example.finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements myActionDialog.OnInputListener{

    private static final String TAG = "MainActivity";
    Button addButton;
    Button deleteButton;
    Button refreshButton;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    ListView eventList;
    public ArrayList<String> eventName = new ArrayList<>();
    public ArrayList<String> eventAction = new ArrayList<>();
    public ArrayList<String> eventLocation = new ArrayList<>();
    public ArrayList<Boolean> eventChecked = new ArrayList<>();
    public ArrayList<String> eventPackage = new ArrayList<>();
    public CustomAdapter customAdapter;
    eventDBHelper db = new eventDBHelper(this);

    String mInputAction;
    String mInputName;
    String mInputLocation;

    public boolean rotateBool = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int somethin=0;
        while(somethin==0) {
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            0);
                }
                somethin = 1;
            }
            catch(Exception e){}
        }


        //Find layout elements
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);
        refreshButton = findViewById(R.id.refreshButton);
        eventList = findViewById(R.id.eventList);

        //set up list view
        customAdapter = new CustomAdapter();
        eventList.setAdapter(customAdapter);
        loadAll(); //Load all db data into array list

        //add button on click listener NEED TO OPEN LIST OF APPS
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(0, 0); //first 0 = openAdd Dialog, second 0 no effect
                loadAll();
                customAdapter.notifyDataSetChanged();
            }
        });


        //delete button on click listener
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=eventName.size()-1; i>=0; i--){
                    if(eventChecked.get(i)){
                        System.out.println("Told to DELETE: *"+ eventName.get(i) + ", " + eventAction.get(i) +", "+ eventLocation.get(i));
                        db.deleteTitle(eventName.get(i).trim(), eventAction.get(i).trim(), eventLocation.get(i).trim());

                        eventName.remove(i); eventAction.remove(i); eventLocation.remove(i);
                        eventChecked.remove(i); eventPackage.remove(i);

                        customAdapter.notifyDataSetChanged();

                    }
                }
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loadAll();
            }
        });


        Intent intent = new Intent(this, ShakeService.class);
        startService(intent);
        if(intent != null){
            Toast.makeText(MainActivity.this, "Service Launched", Toast.LENGTH_LONG).show();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */
//                try {
//                    SQLiteDatabase shakeOpenDB = db.getReadableDatabase();
//                    String query = "Select * from eventlist Where " + EventContract.EventEntry.COLUMN_ACTION + "='shake'";
//                    Cursor res = shakeOpenDB.rawQuery(query, null);
//                    res.moveToFirst();
//                    final String packageNameM = res.getString(4);
//                    System.out.println("Package name is: " + packageNameM);
//
//
//                    Intent intent = new Intent();
//                    intent.setPackage(packageNameM.trim());
//                    startActivity(intent);
//                } catch (CursorIndexOutOfBoundsException e){
//                    System.out.println(e);
//                }
//                Toast.makeText(MainActivity.this, "Shaked!!!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAll();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();


    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return eventName.size();
        }

        @Override
        public Object getItem(int position) {
            return eventName.get(position);
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.activity_listview, null);

            final CheckBox item_name = convertView.findViewById(R.id.appBox);
            Button action_Button = convertView.findViewById(R.id.actionButton);
            Button location_Button = convertView.findViewById(R.id.locationButton);

            item_name.setText(eventName.get(position));
            action_Button.setText(eventAction.get(position));
            if (eventLocation.get(position).equals("none")){
                location_Button.setText("none");
            } else {
                location_Button.setText("set");
            }

            item_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    if (item_name.isChecked()){
                        eventChecked.set(position, true);
                    } else {
                        eventChecked.set(position, false);
                    }
                }
            });

            action_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(), "Open list of actions to pick from", Toast.LENGTH_SHORT).show();
                    openDialog(1, position); //first number = open action Dialog
                }
            });

            location_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //PLACE LOCATION FUNCTION HERE
                    Intent intent = new Intent(MainActivity.this, MapsActivity2.class);
                    Bundle extras = new Bundle();
                    extras.putString("ID", String.valueOf(getKey(eventName.get(position).trim(),
                            eventAction.get(position).trim(), eventLocation.get(position).trim())));
                    intent.putExtras(extras);
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(), "Open Location Activity", Toast.LENGTH_SHORT).show();
                }
            });

            this.notifyDataSetChanged();
            return convertView;
        }
    }

    public static class AddEventDialog extends AppCompatDialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Information");

            //get a list of installed apps.
            final PackageManager pm = getActivity().getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            final String[] appNames = new String[packages.size()];
            final String[] packageNames = new String[packages.size()];
            ApplicationInfo ai;
            String convertedName;
            for (int i = 0; i<packages.size(); i++) {
                Log.d("debug", "Installed package :" + packages.get(i).packageName);
                try {
                    ai = pm.getApplicationInfo(packages.get(i).packageName, 0);
                } catch (PackageManager.NameNotFoundException e){
                    ai = null;
                }
                convertedName = (String) (ai != null ? pm.getApplicationLabel(ai) : "unknown");
                System.out.println("The converted Name is:" + convertedName);
                appNames[i] = packages.get(i).loadLabel(pm).toString();
                packageNames[i] = packages.get(i).packageName;

            }
            //Create boolean array for checkboxes
            final boolean[] appChecked = new boolean[packages.size()];
            Arrays.fill(appChecked, false);

            //Create Checkbox list
            builder.setMultiChoiceItems( appNames, appChecked, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    // user checked or unchecked a box
                }
            });

            // add OK and Cancel buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    eventDBHelper db = new eventDBHelper(getContext());

                    for (int i = 0; i<appNames.length; i++){
                        if (appChecked[i]){
                            System.out.println("Inserting: "+ appNames[i]);
                            if (db.insertData(appNames[i], "none", "none",packageNames[i])){
                                System.out.println("Successfully added");
                            } else {
                                System.out.println("Could not be added");
                            }
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);
            return builder.create();
        }
    }


    public void openDialog(int type, int position){
        if (type == 0) {
            AddEventDialog addEventDialog = new AddEventDialog();
            addEventDialog.show(getSupportFragmentManager(), "apps dialog");
        } else {
            myActionDialog actionDialog = new myActionDialog();
            actionDialog.show(getSupportFragmentManager(), "action dialog");
            mInputName = eventName.get(position);
            mInputLocation = eventLocation.get(position);
            mInputAction = eventAction.get(position);
        }

        for (int i=0; i<20;i++){
            loadAll();
        }
    }

    @Override
    public void sendInput(String input) {
        Log.d(TAG, "sendInput: got the input: " + input);

        db.editAction(mInputName.trim(), mInputAction.trim(), mInputLocation.trim(), input);
        loadAll();
    }

    public void loadAll(){
        try {
            Cursor res = db.getAllData();
        } catch (Exception e){
            System.out.print("Error: " + e);
        }
        Cursor res = db.getAllData();
        if (res.getCount() == 0){
            //no data available
            return;
        }
        eventName.clear();
        eventAction.clear();
        eventLocation.clear();
        eventChecked.clear();
        eventPackage.clear();
        while (res.moveToNext()){
            eventName.add(res.getString(1));
            eventAction.add(res.getString(2));
            eventLocation.add(res.getString(3));
            eventPackage.add(res.getString(4));
            eventChecked.add(false);
        }
        customAdapter.notifyDataSetChanged();
    }

    public String convertPackageName(String packageName){
        ApplicationInfo ai;
        String convertedName;
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e){
            ai = null;
        }
        convertedName = (String) (ai != null ? pm.getApplicationLabel(ai) : "unknown");
        return convertedName;
    }

    public int getKey(String name, String action, String location){
        SQLiteDatabase readDB = db.getReadableDatabase();

        Cursor cursor = readDB.rawQuery("SELECT * FROM "+ EventContract.EventEntry.TABLE_NAME + " WHERE "
                + EventContract.EventEntry.COLUMN_NAME + " = '" + name + "' AND " + EventContract.EventEntry.COLUMN_ACTION +
                " = '" + action + "' AND " + EventContract.EventEntry.COLUMN_LOCATION + " = '" + location+"'", null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


        }
    }

    }
