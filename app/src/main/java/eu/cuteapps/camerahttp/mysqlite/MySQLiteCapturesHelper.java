package eu.cuteapps.camerahttp.mysqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteCapturesHelper extends SQLiteOpenHelper {

  public static final String DATABASE_NAME = "captures.db";
  private static final int DATABASE_VERSION = 1;
  public static final String TABLE_CAPTURES = "Captures";
  public static String COLUMN_ID = "Id";
  public static String COLUMN_LATITUDE = "Latitude";
  public static String COLUMN_LONGITUDE = "Longitude";
  public static String COLUMN_DATE = "Date";
  public static String COLUMN_MEDIA_TYPE = "Media_Type";
  public static String COLUMN_MEDIA_FILE_PATH = "Media_File_Path";
  private Context mContext = null;

  private static final String DATABASE_CREATE =
      "create table " + TABLE_CAPTURES + "(" + COLUMN_ID + " integer primary key autoincrement, " +
          COLUMN_LATITUDE + " text not null, " +
          COLUMN_LONGITUDE + " text not null, " +
          COLUMN_DATE + " text not null, " +
          COLUMN_MEDIA_TYPE + " text not null, " +
          COLUMN_MEDIA_FILE_PATH + " text);";

  public MySQLiteCapturesHelper(Context context) {
    super(context, MySQLiteCapturesHelper.DATABASE_NAME, null,
        MySQLiteCapturesHelper.DATABASE_VERSION);
    this.mContext = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(MySQLiteCapturesHelper.DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteCapturesHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to " + newVersion +
            ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + MySQLiteCapturesHelper.TABLE_CAPTURES);
    this.onCreate(db);
  }
}