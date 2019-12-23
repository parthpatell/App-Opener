package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class eventDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "eventlist.db";
    public static final int DATABASE_VERSION = 3;

    public eventDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATIONLIST_TABLE = "CREATE TABLE " +
                EventContract.EventEntry.TABLE_NAME + " (" +
                EventContract.EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                EventContract.EventEntry.COLUMN_NAME + " TEXT," +
                EventContract.EventEntry.COLUMN_ACTION + " TEXT," +
                EventContract.EventEntry.COLUMN_LOCATION + " TEXT," +
                EventContract.EventEntry.COLUMN_PACKAGE + " TEXT)";

        db.execSQL(SQL_CREATE_LOCATIONLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EventContract.EventEntry.TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name, String action, String location, String packageName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(EventContract.EventEntry.COLUMN_NAME, name);
        cv.put(EventContract.EventEntry.COLUMN_ACTION, action);
        cv.put(EventContract.EventEntry.COLUMN_LOCATION, location);
        cv.put(EventContract.EventEntry.COLUMN_PACKAGE, packageName);

        System.out.println("Going to insert into the DB: "+ name + ", " + action + ", " +location + ", " + packageName);
        String query = "INSERT INTO " + EventContract.EventEntry.TABLE_NAME + "(" +
                EventContract.EventEntry.COLUMN_NAME + "," + EventContract.EventEntry.COLUMN_ACTION + ","
                + EventContract.EventEntry.COLUMN_LOCATION + "," + EventContract.EventEntry.COLUMN_PACKAGE
                + ")" + " VALUES ('" + name + "','" + action + "','" + location + "','" + packageName +"');";
        db.execSQL(query);

        return true;
    }

    public void deleteTitle(String name, String action, String location) {

        SQLiteDatabase deleteDB = this.getWritableDatabase();

        String query = "DELETE FROM " + EventContract.EventEntry.TABLE_NAME + " where " +
                EventContract.EventEntry.COLUMN_NAME + "='" + name + "' AND " +
                EventContract.EventEntry.COLUMN_ACTION + "='" + action + "' AND " +
                EventContract.EventEntry.COLUMN_LOCATION + "='" + location + "'";
        deleteDB.execSQL(query);
        System.out.println("DELETING: " + name + ", " + action + ", " + location + " FROM TABLE: " + EventContract.EventEntry.TABLE_NAME);

    }

    public Cursor getAllData(){
        SQLiteDatabase getDB = this.getWritableDatabase();
        getDB.beginTransaction();
        Cursor res = getDB.rawQuery("select * from " + EventContract.EventEntry.TABLE_NAME, null);
        getDB.setTransactionSuccessful();
        getDB.endTransaction();
        return res;
    }

    public void editAction(String name, String action, String location, String newAction){
        SQLiteDatabase addDB = this.getWritableDatabase();
        String query = "UPDATE " + EventContract.EventEntry.TABLE_NAME + " SET " +
                EventContract.EventEntry.COLUMN_ACTION + "='" + newAction + "' WHERE " +
                EventContract.EventEntry.COLUMN_NAME + "='" + name + "' AND " +
                EventContract.EventEntry.COLUMN_ACTION + "='" + action + "' AND " +
                EventContract.EventEntry.COLUMN_LOCATION + "='" + location + "'";
        addDB.execSQL(query);
        System.out.println("Editing Action from " + action + " to " + newAction + " with \n" + query);
    }
}
