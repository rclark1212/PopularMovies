package app.com.example.android.popularmovies;

import android.content.ContentProvider;
import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by rclark on 10/4/2015.
 * The meat of the content provider.
 * Derived from example at http://www.vogella.com/tutorials/AndroidSQLite/article.html
 * TODO - extend comments
 */
public class FavoritesContentProvider extends ContentProvider {

    // database
    private FavoritesDatabaseHelper database;

    private static final String AUTHORITY = "app.com.example.android.popularmovies.contentprovider";

    private static final String BASE_PATH = "favorites";

    @Override
    public boolean onCreate() {
        database = new FavoritesDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(FavoritesTable.TABLE_FAVORITE);

        // adding the ID to the original query
        queryBuilder.appendWhere(FavoritesTable.COLUMN_ID + "="
                + uri.getLastPathSegment());

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        id = sqlDB.insert(FavoritesTable.TABLE_FAVORITE, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        String id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
            rowsDeleted = sqlDB.delete(FavoritesTable.TABLE_FAVORITE,
                    FavoritesTable.COLUMN_ID + "=" + id,
                    null);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        String id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
            rowsUpdated = sqlDB.update(FavoritesTable.TABLE_FAVORITE,
                    values,
                    FavoritesTable.COLUMN_ID + "=" + id,
                    null);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = { FavoritesTable.COLUMN_TITLE,
                FavoritesTable.COLUMN_SUMMARY, FavoritesTable.COLUMN_RATING,
                FavoritesTable.COLUMN_RELEASEDATE,
                FavoritesTable.COLUMN_ID };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
