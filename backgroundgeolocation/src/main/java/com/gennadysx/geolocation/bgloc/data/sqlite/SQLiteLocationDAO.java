package com.gennadysx.geolocation.bgloc.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.gennadysx.geolocation.bgloc.data.BackgroundLocation;
import com.gennadysx.geolocation.bgloc.data.LocationDAO;

import com.gennadysx.geolocation.sqlbuilder.SqlExpression;
import com.gennadysx.geolocation.sqlbuilder.SqlSelectStatement;

import java.util.ArrayList;
import java.util.Collection;

public class SQLiteLocationDAO implements LocationDAO {
  private SQLiteDatabase db;

  public SQLiteLocationDAO(Context context) {
    SQLiteOpenHelper helper = SQLiteOpenHelper.getHelper(context);
    this.db = helper.getWritableDatabase();
  }

  public SQLiteLocationDAO(SQLiteDatabase db) {
    this.db = db;
  }

  /**
   * Get all locations that match whereClause
   *
   * @param whereClause
   * @param whereArgs
   * @return collection of locations
     */
  private Collection<BackgroundLocation> getLocations(String whereClause, String[] whereArgs) {
    Collection<BackgroundLocation> locations = new ArrayList<BackgroundLocation>();

    String[] columns = queryColumns();
    String groupBy = null;
    String having = null;
    String orderBy = SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME + " ASC";
    Cursor cursor = null;

    try {
      cursor = db.query(
          SQLiteLocationContract.LocationEntry.TABLE_NAME,  // The table to query
          columns,                   // The columns to return
          whereClause,               // The columns for the WHERE clause
          whereArgs,                 // The values for the WHERE clause
          groupBy,                   // don't group the rows
          having,                    // don't filter by row groups
          orderBy                    // The sort order
      );
      while (cursor.moveToNext()) {
        locations.add(hydrate(cursor));
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return locations;
  }
  public Collection<BackgroundLocation> getAllLocations() {
    return getLocations(null, null);
  }

  public Collection<BackgroundLocation> getValidLocations() {
    String whereClause = SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS + " <> ?";
    String[] whereArgs = { String.valueOf(BackgroundLocation.DELETED) };

    return getLocations(whereClause, whereArgs);
  }

  public BackgroundLocation getLocationById(long id) {
    String[] columns = queryColumns();
    String whereClause = SQLiteLocationContract.LocationEntry._ID + " = ?";
    String[] whereArgs = { String.valueOf(id) };

    BackgroundLocation location = null;
    Cursor cursor = null;
    try {
      cursor = db.query(
              SQLiteLocationContract.LocationEntry.TABLE_NAME,  // The table to query
              columns,                   // The columns to return
              whereClause,               // The columns for the WHERE clause
              whereArgs,                 // The values for the WHERE clause
              null,              // don't group the rows
              null,               // don't filter by row groups
              null               // The sort order
      );
      while (cursor.moveToNext()) {
        location = hydrate(cursor);
        if (!cursor.isLast()) {
          throw new RuntimeException("Location " + id + " is not unique");
        }
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return location;
  }

  public BackgroundLocation getFirstUnpostedLocation() {
    SqlSelectStatement subsql = new SqlSelectStatement();
    subsql.column(new SqlExpression(String.format("MIN(%s)", SQLiteLocationContract.LocationEntry._ID)), SQLiteLocationContract.LocationEntry._ID);
    subsql.from(SQLiteLocationContract.LocationEntry.TABLE_NAME);
    subsql.where(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, SqlExpression.SqlOperatorEqualTo, BackgroundLocation.POST_PENDING);
    subsql.orderBy(SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME);

    SqlSelectStatement sql = new SqlSelectStatement();
    sql.columns(queryColumns());
    sql.from(SQLiteLocationContract.LocationEntry.TABLE_NAME);
    sql.where(SQLiteLocationContract.LocationEntry._ID, SqlExpression.SqlOperatorEqualTo, subsql);

    BackgroundLocation location = null;
    Cursor cursor = null;
    try {
      cursor = db.rawQuery(sql.statement(), new String[]{});
      while (cursor.moveToNext()) {
        location = hydrate(cursor);
        if (!cursor.isLast()) {
          throw new RuntimeException("Expected single location");
        }
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return location;
  }

  public BackgroundLocation getNextUnpostedLocation(long fromId) {
    SqlSelectStatement subsql = new SqlSelectStatement();
    subsql.column(new SqlExpression(String.format("MIN(%s)", SQLiteLocationContract.LocationEntry._ID)), SQLiteLocationContract.LocationEntry._ID);
    subsql.from(SQLiteLocationContract.LocationEntry.TABLE_NAME);
    subsql.where(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, SqlExpression.SqlOperatorEqualTo, BackgroundLocation.POST_PENDING);
    subsql.where(SQLiteLocationContract.LocationEntry._ID, SqlExpression.SqlOperatorNotEqualTo, fromId);
    subsql.orderBy(SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME);

    SqlSelectStatement sql = new SqlSelectStatement();
    sql.columns(queryColumns());
    sql.from(SQLiteLocationContract.LocationEntry.TABLE_NAME);
    sql.where(SQLiteLocationContract.LocationEntry._ID, SqlExpression.SqlOperatorEqualTo, subsql);

    BackgroundLocation location = null;
    Cursor cursor = null;
    try {
      cursor = db.rawQuery(sql.statement(), new String[]{});
      while (cursor.moveToNext()) {
        location = hydrate(cursor);
        if (!cursor.isLast()) {
          throw new RuntimeException("Expected single location");
        }
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return location;
  }

  public long getUnpostedLocationsCount() {
    String whereClause = SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS + " = ?";
    String[] whereArgs = { String.valueOf(BackgroundLocation.POST_PENDING) };

    return DatabaseUtils.queryNumEntries(db, SQLiteLocationContract.LocationEntry.TABLE_NAME, whereClause, whereArgs);
  }

  public long getLocationsForSyncCount(long millisSinceLastBatch) {
    String whereClause = TextUtils.join("", new String[]{
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS + " = ? AND ( ",
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_BATCH_START_MILLIS + " IS NULL OR ",
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_BATCH_START_MILLIS + " < ? )",
    });
    String[] whereArgs = {
            String.valueOf(BackgroundLocation.SYNC_PENDING),
            String.valueOf(millisSinceLastBatch)
    };

    return DatabaseUtils.queryNumEntries(db, SQLiteLocationContract.LocationEntry.TABLE_NAME, whereClause, whereArgs);
  }

  /**
   * Persist location into database
   *
   * @param location
   * @return rowId or -1 when error occured
   */
  public long persistLocation(BackgroundLocation location) {
    ContentValues values = getContentValues(location);
    long rowId = db.insertOrThrow(SQLiteLocationContract.LocationEntry.TABLE_NAME, SQLiteLocationContract.LocationEntry.COLUMN_NAME_NULLABLE, values);

    return rowId;
  }

  /**
   * Persist location into database with maximum row limit
   *
   * Method will ensure that there will be no more records than maxRows.
   * Instead old records will be replaced with newer ones.
   * If maxRows will change in time, method will delete excess records and vacuum table.
   *
   * @param location
   * @param maxRows
   * @return rowId or -1 when error occured
   */
  public long persistLocation(BackgroundLocation location, int maxRows) {
    if (maxRows == 0) {
      return -1;
    }

    String sql = null;
    Boolean shouldVacuum = false;

    long rowCount = DatabaseUtils.queryNumEntries(db, SQLiteLocationContract.LocationEntry.TABLE_NAME);

    if (rowCount < maxRows) {
      ContentValues values = getContentValues(location);
      return db.insertOrThrow(SQLiteLocationContract.LocationEntry.TABLE_NAME, SQLiteLocationContract.LocationEntry.COLUMN_NAME_NULLABLE, values);
    }

    db.beginTransactionNonExclusive();

    if (rowCount > maxRows) {
      sql = new StringBuilder("DELETE FROM ")
              .append(SQLiteLocationContract.LocationEntry.TABLE_NAME)
              .append(" WHERE ").append(SQLiteLocationContract.LocationEntry._ID)
              .append(" IN (SELECT ").append(SQLiteLocationContract.LocationEntry._ID)
              .append(" FROM ").append(SQLiteLocationContract.LocationEntry.TABLE_NAME)
              .append(" ORDER BY ").append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME)
              .append(" LIMIT ?)")
              .toString();
      db.execSQL(sql, new Object[] {(rowCount - maxRows)});
      shouldVacuum = true;
    }

    // get oldest location id to be overwritten
    Cursor cursor = null;
    long locationId;
    try {
      cursor = db.query(
              SQLiteLocationContract.LocationEntry.TABLE_NAME,
              new String[] { "min(" + SQLiteLocationContract.LocationEntry._ID + ")" },
              TextUtils.join("", new String[]{
                      SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME,
                      "= (SELECT min(",
                      SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME,
                      ") FROM ",
                      SQLiteLocationContract.LocationEntry.TABLE_NAME,
                      ")"
              }),
              null, null, null, null);
      cursor.moveToFirst();
      locationId = cursor.getLong(0);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    sql = new StringBuilder("UPDATE ")
            .append(SQLiteLocationContract.LocationEntry.TABLE_NAME).append(" SET ")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_PROVIDER).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_ACCURACY).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_SPEED).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_BEARING).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_ALTITUDE).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_RADIUS).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LATITUDE).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LONGITUDE).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ACCURACY).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_SPEED).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_BEARING).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ALTITUDE).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_RADIUS).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LOCATION_PROVIDER).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_BATCH_START_MILLIS).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS).append("= ?,")
            .append(SQLiteLocationContract.LocationEntry.COLUMN_NAME_MOCK_FLAGS).append("= ?")
            .append(" WHERE ").append(SQLiteLocationContract.LocationEntry._ID)
            .append("= ?")
            .toString();
    db.execSQL(sql, new Object[] {
            location.getProvider(),
            location.getTime(),
            location.getAccuracy(),
            location.getSpeed(),
            location.getBearing(),
            location.getAltitude(),
            location.getRadius(),
            location.getLatitude(),
            location.getLongitude(),
            location.hasAccuracy() ? 1 : 0,
            location.hasSpeed() ? 1 : 0,
            location.hasBearing() ? 1 : 0,
            location.hasAltitude() ? 1 : 0,
            location.hasRadius() ? 1 : 0,
            location.getLocationProvider(),
            location.getBatchStartMillis(),
            location.getStatus(),
            location.getMockFlags(),
            locationId
    });

    db.setTransactionSuccessful();
    db.endTransaction();

    if (shouldVacuum) { db.execSQL("VACUUM"); }

    return locationId;
  }

  /**
   * Delete location by given locationId
   *
   * Note: location is not actually deleted only flagged as non valid
   * @param locationId
   */
  public void deleteLocationById(long locationId) {
    if (locationId < 0) {
      return;
    }

    ContentValues values = new ContentValues();
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, BackgroundLocation.DELETED);

    String whereClause = SQLiteLocationContract.LocationEntry._ID + " = ?";
    String[] whereArgs = { String.valueOf(locationId) };

    db.update(SQLiteLocationContract.LocationEntry.TABLE_NAME, values, whereClause, whereArgs);
  }

  public BackgroundLocation deleteFirstUnpostedLocation() {
    BackgroundLocation location = getFirstUnpostedLocation();
    deleteLocationById(location.getLocationId());

    return location;
  }

  public long persistLocationForSync(BackgroundLocation location, int maxRows) {
    Long locationId = location.getLocationId();

    if (locationId == null) {
      location.setStatus(BackgroundLocation.SYNC_PENDING);
      return persistLocation(location, maxRows);
    } else {
      ContentValues values = new ContentValues();
      values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, BackgroundLocation.SYNC_PENDING);

      String whereClause = SQLiteLocationContract.LocationEntry._ID + " = ?";
      String[] whereArgs = { String.valueOf(locationId) };

      db.update(SQLiteLocationContract.LocationEntry.TABLE_NAME, values, whereClause, whereArgs);
      return locationId;
    }
  }

  public void updateLocationForSync(long locationId) {
    ContentValues values = new ContentValues();
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, BackgroundLocation.SYNC_PENDING);

    String whereClause = SQLiteLocationContract.LocationEntry._ID + " = ?";
    String[] whereArgs = { String.valueOf(locationId) };

    db.update(SQLiteLocationContract.LocationEntry.TABLE_NAME, values, whereClause, whereArgs);
  }

  /**
   * Delete all locations
   *
   * Note: location are not actually deleted only flagged as non valid
   */
  public int deleteAllLocations() {
    ContentValues values = new ContentValues();
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, BackgroundLocation.DELETED);

    return db.update(SQLiteLocationContract.LocationEntry.TABLE_NAME, values, null, null);
  }

  /**
   * Delete all locations that are in post location queue
   *
   * Note: Instead of deleting, location status is changed so they can be still synced
   */
  public int deleteUnpostedLocations() {
    ContentValues values = new ContentValues();
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, BackgroundLocation.SYNC_PENDING);

    String whereClause = SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS + " = ?";
    String[] whereArgs = { String.valueOf(BackgroundLocation.POST_PENDING) };

    return db.update(SQLiteLocationContract.LocationEntry.TABLE_NAME, values, whereClause, whereArgs);
  }

  private BackgroundLocation hydrate(Cursor c) {
    BackgroundLocation l = new BackgroundLocation(c.getString(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_PROVIDER)));
    l.setTime(c.getLong(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME)));
    if (c.getInt(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ACCURACY)) == 1) {
      l.setAccuracy(c.getFloat(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_ACCURACY)));
    }
    if (c.getInt(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_SPEED)) == 1) {
      l.setSpeed(c.getFloat(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_SPEED)));
    }
    if (c.getInt(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_BEARING)) == 1) {
      l.setBearing(c.getFloat(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_BEARING)));
    }
    if (c.getInt(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ALTITUDE)) == 1) {
      l.setAltitude(c.getDouble(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_ALTITUDE)));
    }
    if (c.getInt(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_RADIUS)) == 1) {
      l.setRadius(c.getFloat(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_RADIUS)));
    }
    l.setLatitude(c.getDouble(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LATITUDE)));
    l.setLongitude(c.getDouble(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LONGITUDE)));
    l.setLocationProvider(c.getInt(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LOCATION_PROVIDER)));
    l.setBatchStartMillis(c.getLong(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_BATCH_START_MILLIS)));
    l.setStatus(c.getInt(c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS)));
    l.setLocationId(c.getLong(c.getColumnIndex(SQLiteLocationContract.LocationEntry._ID)));
    l.setMockFlags(c.getInt((c.getColumnIndex(SQLiteLocationContract.LocationEntry.COLUMN_NAME_MOCK_FLAGS))));

    return l;
  }

  private ContentValues getContentValues(BackgroundLocation l) {
    ContentValues values = new ContentValues();
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_PROVIDER, l.getProvider());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME, l.getTime());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_ACCURACY, l.getAccuracy());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_SPEED, l.getSpeed());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_BEARING, l.getBearing());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_ALTITUDE, l.getAltitude());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_RADIUS, l.getRadius());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LATITUDE, l.getLatitude());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LONGITUDE, l.getLongitude());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ACCURACY, l.hasAccuracy() ? 1 : 0);
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_SPEED, l.hasSpeed() ? 1 : 0);
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_BEARING, l.hasBearing() ? 1 : 0);
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ALTITUDE, l.hasAltitude() ? 1 : 0);
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_RADIUS, l.hasRadius() ? 1 : 0);
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_LOCATION_PROVIDER, l.getLocationProvider());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS, l.getStatus());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_BATCH_START_MILLIS, l.getBatchStartMillis());
    values.put(SQLiteLocationContract.LocationEntry.COLUMN_NAME_MOCK_FLAGS, l.getMockFlags());

    return values;
  }

  private String[] queryColumns() {
    String[] columns = {
            SQLiteLocationContract.LocationEntry._ID,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_PROVIDER,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_TIME,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_ACCURACY,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_SPEED,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_BEARING,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_ALTITUDE,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_RADIUS,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_LATITUDE,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_LONGITUDE,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ACCURACY,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_SPEED,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_BEARING,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_ALTITUDE,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_HAS_RADIUS,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_LOCATION_PROVIDER,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_STATUS,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_BATCH_START_MILLIS,
            SQLiteLocationContract.LocationEntry.COLUMN_NAME_MOCK_FLAGS
    };

    return columns;
  }
}
