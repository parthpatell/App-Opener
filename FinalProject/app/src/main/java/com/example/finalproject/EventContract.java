package com.example.finalproject;

import android.provider.BaseColumns;

public class EventContract {
    private EventContract() {
    }

    public static final class EventEntry implements BaseColumns {
        public static final String TABLE_NAME = "eventlist";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ACTION = "action";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_PACKAGE = "packageName";
    }
}
