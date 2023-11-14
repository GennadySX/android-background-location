package com.gennadysx.geolocation.bgloc.data.sqlite;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class SQLiteOpenHelper extends android.database.sqlite.SQLiteOpenHelper {
    private static final String TAG = SQLiteOpenHelper.class.getName();
    public static final String SQLITE_DATABASE_NAME = "cordova_bg_geolocation.db";
    public static final int DATABASE_VERSION = 15;

    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String REAL_TYPE = " REAL";
    public static final String COMMA_SEP = ",";

    private static SQLiteOpenHelper instance;

    /**
     * Get SqliteOpenHelper instance (singleton)
     *
     * Use the application context, which will ensure that you
     * don't accidentally leak an Activity's context.
     * See this article for more information: http://bit.ly/6LRzfx
     *
     * @param context
     * @return
     */
    public static synchronized SQLiteOpenHelper getHelper(Context context) {
        if (instance == null)
            instance = new SQLiteOpenHelper(context.getApplicationContext());

        return instance;
    }

    /**
     * Constructor
     *
     * NOTE: Intended to use only for testing purposes.
     * Use factory method getHelper instead.
     *
     * @param context
     */
    public SQLiteOpenHelper(Context context) {
        super(context, SQLITE_DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating db: " + this.getDatabaseName());
        execAndLogSql(db, SQLiteLocationContract.LocationEntry.SQL_CREATE_LOCATION_TABLE);
        execAndLogSql(db, SQLiteConfigurationContract.ConfigurationEntry.SQL_CREATE_CONFIG_TABLE);
        execAndLogSql(db, SQLiteLocationContract.LocationEntry.SQL_CREATE_LOCATION_TABLE_TIME_IDX);
        execAndLogSql(db, SQLiteLocationContract.LocationEntry.SQL_CREATE_LOCATION_TABLE_BATCH_ID_IDX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(this.getClass().getName(), "Upgrading database oldVersion: " + oldVersion + " newVersion: " + newVersion);

        ArrayList<String> alterSql = new ArrayList<String>();
        switch (oldVersion) {
            case 10:
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS + INTEGER_TYPE);
                alterSql.add(SQLiteLocationContract.LocationEntry.SQL_CREATE_LOCATION_TABLE_TIME_IDX);
                alterSql.add(SQLiteConfigurationContract.ConfigurationEntry.SQL_DROP_CONFIG_TABLE);
                alterSql.add(SQLiteConfigurationContract.ConfigurationEntry.SQL_CREATE_CONFIG_TABLE);
            case 11:
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_RADIUS + REAL_TYPE);
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ACCURACY + INTEGER_TYPE);
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_SPEED + INTEGER_TYPE);
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_BEARING + INTEGER_TYPE);
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ALTITUDE + INTEGER_TYPE);
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_RADIUS + INTEGER_TYPE);
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_BATCH_START_MILLIS + INTEGER_TYPE);
                alterSql.add(SQLiteLocationContract.LocationEntry.SQL_CREATE_LOCATION_TABLE_BATCH_ID_IDX);
                alterSql.add("UPDATE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " SET " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ACCURACY + "= 1," +
                        SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_SPEED + "= 1," +
                        SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_BEARING + "= 1," +
                        SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ALTITUDE + "= 1," +
                        SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_RADIUS + "= 1"
                );
            case 12:
                alterSql.add("ALTER TABLE " + SQLiteConfigurationContract.ConfigurationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_TEMPLATE + TEXT_TYPE);
            case 13:
                alterSql.add("ALTER TABLE " + SQLiteLocationContract.LocationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteLocationContract.LocationEntry.COLUMN_NAME_MOCK_FLAGS + INTEGER_TYPE);
            case 14:
                alterSql.add("ALTER TABLE " + SQLiteConfigurationContract.ConfigurationEntry.TABLE_NAME +
                        " ADD COLUMN " + SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIFICATIONS_ENABLED + INTEGER_TYPE);

                break; // DO NOT FORGET TO MOVE DOWN BREAK ON DB UPGRADE!!!
            default:
                onDowngrade(db, 0, 0);
                return;
        }

        for (String sql : alterSql) {
            execAndLogSql(db, sql);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // we don't support db downgrade yet, instead we drop table and start over
        execAndLogSql(db, SQLiteLocationContract.LocationEntry.SQL_DROP_LOCATION_TABLE);
        execAndLogSql(db, SQLiteConfigurationContract.ConfigurationEntry.SQL_DROP_CONFIG_TABLE);
        onCreate(db);
    }

    public void execAndLogSql(SQLiteDatabase db, String sql) {
        Log.d(TAG, sql);
        try {
            db.execSQL(sql);
        } catch (SQLException e) {
            Log.e(TAG, "Error executing sql: " + e.getMessage());
        }
    }
}
