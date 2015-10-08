package app.com.example.android.popularmovies;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import android.net.Uri;
import android.text.TextUtils;


/**
 * Created by rclark on 10/4/2015.
 * Content provider for popular movies
 * Note - rewrote content provider based on a better/more concise example.
 * Derived from example at http://www.tutorialspoint.com/android/android_content_providers.htm
 * Made some small modifications:
 *  Changed field names/DB names
 *  Extended fields
 *  Cleaned up some routines like GetType
 *  Used to store/retrieve favorites data including movieID and movie metadata.
 *  Used by MovieData class
 */
public class FavoritesContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "app.com.example.popularmovies.FavoritesContentProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/favorites";
    static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TMDBID = "movieid";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_SUMMARY = "summary";
    public static final String COLUMN_RELEASEDATE = "reldate";

    private static HashMap<String, String> FAVORITES_PROJECTION_MAP;

    static final int FAVORITES = 1;
    static final int FAVORITE_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "favorites", FAVORITES);
        uriMatcher.addURI(PROVIDER_NAME, "favorites/#", FAVORITE_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "MovieFavorites.db";
    static final String FAVORITE_TABLE_NAME = "favorites";
    static final int DATABASE_VERSION = 1;
    // Database creation SQL statement
    private static final String CREATE_DB_TABLE = " CREATE TABLE "
            + FAVORITE_TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TMDBID + " text not null, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_SUMMARY + " text not null,"
            + COLUMN_RATING + " text not null, "
            + COLUMN_RELEASEDATE + " text not null"
            + ");";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);
        }

        //erase old on an upgrade
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  FAVORITE_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new favorite record
         */
        long rowID = db.insert(	FAVORITE_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */

        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FAVORITE_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case FAVORITES:
                qb.setProjectionMap(FAVORITES_PROJECTION_MAP);
                break;

            case FAVORITE_ID:
                qb.appendWhere( COLUMN_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == ""){
            /**
             * By default sort on movie title
             */
            sortOrder = COLUMN_TITLE;
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case FAVORITES:
                count = db.delete(FAVORITE_TABLE_NAME, selection, selectionArgs);
                break;

            case FAVORITE_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( FAVORITE_TABLE_NAME, COLUMN_ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case FAVORITES:
                count = db.update(FAVORITE_TABLE_NAME, values, selection, selectionArgs);
                break;

            case FAVORITE_ID:
                count = db.update(FAVORITE_TABLE_NAME, values, COLUMN_ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

}
