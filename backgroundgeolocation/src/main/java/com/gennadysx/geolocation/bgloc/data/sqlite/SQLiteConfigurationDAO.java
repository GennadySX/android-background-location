package com.gennadysx.geolocation.bgloc.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

import com.gennadysx.geolocation.bgloc.Config;
import com.gennadysx.geolocation.bgloc.data.ConfigurationDAO;
import com.gennadysx.geolocation.bgloc.data.LocationTemplateFactory;

public class SQLiteConfigurationDAO implements ConfigurationDAO {
  private static final String TAG = SQLiteConfigurationDAO.class.getName();

    private SQLiteDatabase db;

  public SQLiteConfigurationDAO(Context context) {
    SQLiteOpenHelper helper = SQLiteOpenHelper.getHelper(context);
    this.db = helper.getWritableDatabase();
  }

  public SQLiteConfigurationDAO(SQLiteDatabase db) {
    this.db = db;
  }

  public Config retrieveConfiguration() throws JSONException {
    Cursor cursor = null;

    String[] columns = {
      SQLiteConfigurationContract.ConfigurationEntry._ID,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_RADIUS,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DISTANCE_FILTER,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DESIRED_ACCURACY,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DEBUG,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_TITLE,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_TEXT,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_ICON_LARGE,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_ICON_SMALL,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_COLOR,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_STOP_TERMINATE,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_STOP_ON_STILL,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_START_BOOT,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_START_FOREGROUND,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIFICATIONS_ENABLED,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_LOCATION_PROVIDER,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_INTERVAL,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_FASTEST_INTERVAL,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_ACTIVITIES_INTERVAL,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_URL,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_SYNC_URL,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_SYNC_THRESHOLD,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_HEADERS,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_MAX_LOCATIONS,
      SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_TEMPLATE
    };

    String whereClause = null;
    String[] whereArgs = null;
    String groupBy = null;
    String having = null;
    String orderBy = null;

    Config config = null;
    try {
      cursor = db.query(
          SQLiteConfigurationContract.ConfigurationEntry.TABLE_NAME,  // The table to query
          columns,                   // The columns to return
          whereClause,               // The columns for the WHERE clause
          whereArgs,                 // The values for the WHERE clause
          groupBy,                   // don't group the rows
          having,                    // don't filter by row groups
          orderBy                    // The sort order
      );
      if (cursor.moveToFirst()) {
        config = hydrate(cursor);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return config;
  }

  public boolean persistConfiguration(Config config) throws NullPointerException {
    long rowId = db.replace(SQLiteConfigurationContract.ConfigurationEntry.TABLE_NAME, SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NULLABLE, getContentValues(config));
    Log.d(TAG, "Configuration persisted with rowId = " + rowId);
    if (rowId > -1) {
      return true;
    } else {
      return false;
    }
  }

  private Config hydrate(Cursor c) throws JSONException {
    Config config = Config.getDefault();
    config.setStationaryRadius(c.getFloat(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_RADIUS)));
    config.setDistanceFilter(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DISTANCE_FILTER)));
    config.setDesiredAccuracy(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DESIRED_ACCURACY)));
    config.setDebugging( (c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DEBUG)) == 1) ? true : false );
    config.setNotificationTitle(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_TITLE)));
    config.setNotificationText(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_TEXT)));
    config.setSmallNotificationIcon(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_ICON_SMALL)));
    config.setLargeNotificationIcon(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_ICON_LARGE)));
    config.setNotificationIconColor(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_COLOR)));
    config.setStopOnTerminate( (c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_STOP_TERMINATE)) == 1) ? true : false );
    config.setStopOnStillActivity( (c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_STOP_ON_STILL)) == 1) ? true : false );
    config.setStartOnBoot( (c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_START_BOOT)) == 1) ? true : false );
    config.setStartForeground( (c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_START_FOREGROUND)) == 1) ? true : false );
    config.setNotificationsEnabled( (c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIFICATIONS_ENABLED)) == 1) ? true : false );
    config.setLocationProvider(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_LOCATION_PROVIDER)));
    config.setInterval(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_INTERVAL)));
    config.setFastestInterval(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_FASTEST_INTERVAL)));
    config.setActivitiesInterval(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_ACTIVITIES_INTERVAL)));
    config.setUrl(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_URL)));
    config.setSyncUrl(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_SYNC_URL)));
    config.setSyncThreshold(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_SYNC_THRESHOLD)));
    config.setHttpHeaders(new JSONObject(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_HEADERS))));
    config.setMaxLocations(c.getInt(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_MAX_LOCATIONS)));
    config.setTemplate(LocationTemplateFactory.fromJSONString(c.getString(c.getColumnIndex(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_TEMPLATE))));

    return config;
  }

  private ContentValues getContentValues(Config config) throws NullPointerException {
    ContentValues values = new ContentValues();
    values.put(SQLiteConfigurationContract.ConfigurationEntry._ID, 1);
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_RADIUS, config.getStationaryRadius());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DISTANCE_FILTER, config.getDistanceFilter());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DESIRED_ACCURACY, config.getDesiredAccuracy());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_DEBUG, (config.isDebugging() == true) ? 1 : 0);
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_TITLE, config.getNotificationTitle());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_TEXT, config.getNotificationText());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_ICON_SMALL, config.getSmallNotificationIcon());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_ICON_LARGE, config.getLargeNotificationIcon());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIF_COLOR, config.getNotificationIconColor());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_STOP_TERMINATE, (config.getStopOnTerminate() == true) ? 1 : 0);
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_STOP_ON_STILL, (config.getStopOnStillActivity() == true) ? 1 : 0);
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_START_BOOT, (config.getStartOnBoot() == true) ? 1 : 0);
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_START_FOREGROUND, (config.getStartForeground() == true) ? 1 : 0);
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_NOTIFICATIONS_ENABLED, (config.getNotificationsEnabled() == true) ? 1 : 0);
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_LOCATION_PROVIDER, config.getLocationProvider());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_INTERVAL, config.getInterval());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_FASTEST_INTERVAL, config.getFastestInterval());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_ACTIVITIES_INTERVAL, config.getActivitiesInterval());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_URL, config.getUrl());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_SYNC_URL, config.getSyncUrl());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_SYNC_THRESHOLD, config.getSyncThreshold());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_HEADERS, new JSONObject(config.getHttpHeaders()).toString());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_MAX_LOCATIONS, config.getMaxLocations());
    values.put(SQLiteConfigurationContract.ConfigurationEntry.COLUMN_NAME_TEMPLATE, config.hasTemplate() ? config.getTemplate().toString() : null);

    return values;
  }
}
