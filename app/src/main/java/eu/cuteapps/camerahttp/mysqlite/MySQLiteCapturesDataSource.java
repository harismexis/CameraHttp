package eu.cuteapps.camerahttp.mysqlite;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MySQLiteCapturesDataSource {

  private SQLiteDatabase database;
  private MySQLiteCapturesHelper dbHelper;
  private String[] allColumns = {
      MySQLiteCapturesHelper.COLUMN_ID,
      MySQLiteCapturesHelper.COLUMN_LATITUDE,
      MySQLiteCapturesHelper.COLUMN_LONGITUDE,
      MySQLiteCapturesHelper.COLUMN_DATE,
      MySQLiteCapturesHelper.COLUMN_MEDIA_TYPE,
      MySQLiteCapturesHelper.COLUMN_MEDIA_FILE_PATH
  };

  public MySQLiteCapturesDataSource(Context context) {
    dbHelper = new MySQLiteCapturesHelper(context);
  }

  public void open() throws SQLException {
    database = this.dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public void addCaptureToDatabase(String latitude, String longitude, String date,
                                   String mediaType, String filePath) {
    final ContentValues values = new ContentValues();
    values.put(MySQLiteCapturesHelper.COLUMN_LATITUDE, latitude);
    values.put(MySQLiteCapturesHelper.COLUMN_LONGITUDE, longitude);
    values.put(MySQLiteCapturesHelper.COLUMN_DATE, date);
    values.put(MySQLiteCapturesHelper.COLUMN_MEDIA_TYPE, mediaType);
    values.put(MySQLiteCapturesHelper.COLUMN_MEDIA_FILE_PATH, filePath);
    database.insert(MySQLiteCapturesHelper.TABLE_CAPTURES, null, values);
  }

  public void deleteCaptureById(String id) {
    database.delete(MySQLiteCapturesHelper.TABLE_CAPTURES,
        MySQLiteCapturesHelper.COLUMN_ID + "=" + id, null);
  }

  public void deleteAllCaptures() {
    database.delete(MySQLiteCapturesHelper.TABLE_CAPTURES, null, null);
  }

  public ArrayList<Capture> getAllModels() {
    final ArrayList<Capture> models = new ArrayList<>();
    final Cursor cursor = database.query(MySQLiteCapturesHelper.TABLE_CAPTURES,
        allColumns, null, null, null, null, null);
    cursor.moveToFirst();
    Capture model;
    while(!cursor.isAfterLast()) {
      model = cursorToModel(cursor);
      models.add(model);
      cursor.moveToNext();
    }
    cursor.close();
    return models;
  }

  private Capture cursorToModel(Cursor cursor) {
    return new Capture(cursor.getString(0), cursor.getString(1), cursor.getString(2),
        cursor.getString(3), cursor.getString(4), cursor.getString(5));
  }
}