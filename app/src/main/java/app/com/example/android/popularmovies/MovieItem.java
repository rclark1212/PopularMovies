package app.com.example.android.popularmovies;

/**
 * Created by rclark on 9/11/2015.
 * Trivial class to store the movie data for an individual movie.
 * Essentially use this as a C style structure.
 * This class represents an individual movie dataset
 */
public class MovieItem {
    private String mTitle;
    private String mPosterPath;
    private String mSynopsis;
    private String mRating;
    private String mReleaseDate;
    private Boolean mFavorite;
    private String mMovieID;
    private int mRuntime;           //not used at moment - maybe in future.

    //In case we want to instantiate directly with data
    public MovieItem(String movieID, String title, String poster, String synopsis, String rating, String releaseDate, Boolean favorite, int runtime) {
        mTitle = title;
        mPosterPath = poster;
        mSynopsis = synopsis;
        mRating = rating;
        mReleaseDate = releaseDate;
        mFavorite = favorite;
        mRuntime = runtime;
        mMovieID = movieID;
    }

    //Implement the getters...
    public String getTitle() {
        return mTitle;
    }

    public String getPosterPath() { return mPosterPath; }

    public String getSynopsis() {
        return mSynopsis;
    }

    public String getRating() {
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
