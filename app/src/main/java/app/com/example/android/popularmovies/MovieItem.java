package app.com.example.android.popularmovies;

import android.widget.ImageView;

import java.util.Date;

/**
 * Created by rclark on 9/11/2015.
 * Trivial class to store the movie data for an individual movie.
 * Essentially use this as a C style structure.
 */
public class MovieItem {
    private String mTitle;
    private int mPoster;        //TODO - to fix. Eventually will be URL. In meantime, use resource
    private String mSynopsis;
    private double mRating;
    private Date mReleaseDate;
    private Boolean mFavorite;
    private int mMovieID;
    private int mRuntime;

    //In case we want to instantiate directly with data
    public MovieItem(int movieID, String title, int poster, String synopsis, double rating, Date releaseDate, Boolean favorite, int runtime) {
        mTitle = title;
        mPoster = poster;
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

    public int getPoster() {
        return mPoster;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public double getRating() {
        return mRating;
    }

    public Date getReleaseDate() {
        return mReleaseDate;
    }

    public Boolean getFavorite() {
        return mFavorite;
    }

    public int getRuntime() { return mRuntime; }
}
