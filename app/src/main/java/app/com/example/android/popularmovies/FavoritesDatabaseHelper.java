package app.com.example.android.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rclark on 10/4/2015.
 * Extend SQLiteOpenHelper class using FavoritesTable helper class methods
 */
public class FavoritesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favoritestable.db";
    private static final int DATABASE_VERSION = 1;

    public FavoritesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        FavoritesTable.onCreate(database);
    }

    // Method is called during an upgrade of the database,
    // e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        FavoritesTable.onUpgrade(database, oldVersion, newVersion);
    }
}
