package app.com.example.android.popularmovies;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by rclark on 10/4/2015.
 * Class for the favorites table inside the SQL database to store favorites.
 * Not really required but is good practice to have a class per table so as schemas
 * change/enhance, can extend in a structured way.
 * Derived from the vogella example on the internet (http://www.vogella.com/tutorials/AndroidSQLite/article.html)
 */
public class FavoritesTable {
    // Database table
    public static final String TABLE_FAVORITE = "favorite";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_SUMMARY = "summary";
    public static final String COLUMN_RELEASEDATE = "reldate";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_FAVORITE
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_SUMMARY + " text not null,"
            + COLUMN_RATING + " text not null "
            + COLUMN_RELEASEDATE + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(FavoritesTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);
        onCreate(database);
    }
}
