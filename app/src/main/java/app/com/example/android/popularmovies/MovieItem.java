package app.com.example.android.popularmovies;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.Date;

/**
 * Created by rclark on 9/11/2015.
 * Trivial class to store the movie data for an individual movie.
 * Essentially use this as a C style structure.
 */
public class MovieItem {
    private String mTitle;
    private String mPosterPath;
    private Bitmap mBitmap;
    private String mSynopsis;
    private double mRating;
    private String mReleaseDate;
    private Boolean mFavorite;
    private String mMovieID;
    private int mRuntime;

    //In case we want to instantiate directly with data
    public MovieItem(String movieID, String title, String poster, Bitmap bitmap, String synopsis, double rating, String releaseDate, Boolean favorite, int runtime) {
        mTitle = title;
        mPosterPath = poster;
        mBitmap = bitmap;
        mSynopsis = synopsis;
        mRating = rating;
        mReleaseDate = releaseDate;
        mFavorite = favorite;
        mRuntime = runtime;
        mMovieID = movieID;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public Bitmap getBitmap() { return mBitmap; }

    public String getSynopsis() {
        return mSynopsis;
    }

    public double getRating() {
        return mRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public Boolean getFavorite() {
        return mFavorite;
    }

    public int getRuntime() { return mRuntime; }

    public String getMovieID() { return mMovieID; }
}
