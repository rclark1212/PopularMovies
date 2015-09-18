package app.com.example.android.popularmovies;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by rclark on 9/11/2015.
 * Manages the list of movies.
 * Functions supported including loading data, parsing JSON, and getter functions
 * ?finding trailers?
 * Will also provide utility functions for the movies
 */
public class MovieData {
    private static final String TMDB_API_BASE = "api.themoviedb.org";
    private static final String TMDB_VERSION = "3";
    private static final String TMDB_DISCOVERY = "discover";
    private static final String TMDB_MOVIES = "movie";
    private static final String TMDB_API_KEY = "api_key";
    private static final String TMDB_SORT_ORDER = "sort_by";
    private static final String TMDB_SORTBY_POPULARITY = "popularity.desc";
    private static final String TMDB_SORTBY_RATING = "vote_average.desc";
    private static final String TMDB_CONFIGURATION = "configuration";

    private final String LOG_TAG = MovieData.class.getSimpleName();

    private ArrayList<MovieItem> mMovies;   //The main database
    private String mBaseURL;                //Base URL for images. Per TMDB API docs, this generally only needs to be checked once...
                                            //sure it might change if you left app open for months but deal with this on a production app. (would refresh if I hit error loading bitmap)
    private String mImageSizePath;          //to be used with mBaseURL (size of poster to load)

    public MovieData() {
        //just init the list here...
        //note this is the main database of movies we load
        mMovies = new ArrayList<MovieItem>();
        mBaseURL = null;
    }

    public void hackPopulateList() {
        //hack function
        for (int i = 0; i < 40; i++) {
            //hack in some fake data...
            String title = "TestTitle #" + i;
            String synopsis = "This movie (generic title #" + i + " is a hack test of using objects for the data structures. It is a boring and useless story of test data which continues to be boring and useless.";
            String rating = "" + (double)i/5.;
            String release = "2012-3-3";
            Boolean favorite = false;
            MovieItem movie = new MovieItem(""+i,title, null, synopsis,rating,release,favorite, i*30);
            mMovies.add(movie);
        }
    }

    //Class getters are below
    public MovieItem getItem(int position) {
        return mMovies.get(position);
    }

    public int length() {
        return mMovies.size();
    }

    //Clears the array...
    public void clear() {
        mMovies.clear();
    }

    /*
    *   This is the routine that does all the loading work...
    *   It opens up the connection to TMDB
    *   Uses the discovery function with proper ordering to then get the data
     */
    public void loadTMDBFromNetwork(String ordering, String apikey)
    {
        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        Uri.Builder builder = new Uri.Builder();
        String sortby = TMDB_SORTBY_POPULARITY;     //default to popularity search

        if (ordering.equals("2"))
        {
            sortby = TMDB_SORTBY_RATING;            //but if by rating (per settings), sort by rating
        }

        //need to read both configuration (to get image url base)
        //and make the discovery query...
        //build discovery URL first
        // Construct the URL for the TMDB query
        // Possible parameters are avaiable at TMDB API page, at
        // http://http://docs.themoviedb.apiary.io/#reference
        builder.scheme("http")
                .authority(TMDB_API_BASE)
                .appendPath(TMDB_VERSION)
                .appendPath(TMDB_DISCOVERY)
                .appendPath(TMDB_MOVIES)
                .appendQueryParameter(TMDB_API_KEY, apikey)
                //deal with ordering..
                // 1 = popularity, 2 = rating
                .appendQueryParameter(TMDB_SORT_ORDER, sortby);

        String urlbuild = builder.build().toString();

        Log.v(LOG_TAG, "Built query string: " + urlbuild);

        moviesJsonStr = getTMDBDataFromURL(urlbuild);

        //Do we have a base URL yet? (for loading images)
        if (mBaseURL == null) {
            //okay, get the configuration from TMDB
            // Construct the URL for the TMDB query
            // Possible parameters are avaiable at TMDB API page, at
            // http://http://docs.themoviedb.apiary.io/#reference
            Uri.Builder buildercfg = new Uri.Builder();
            buildercfg.scheme("http")
                    .authority(TMDB_API_BASE)
                    .appendPath(TMDB_VERSION)
                    .appendPath(TMDB_CONFIGURATION)
                    .appendQueryParameter(TMDB_API_KEY, apikey);

            urlbuild = buildercfg.build().toString();

            Log.v(LOG_TAG, "getting config URL string: " + urlbuild);

            //Okay - get the config info (raw json data)
            String TMDBCfgStr = getTMDBDataFromURL(urlbuild);

            //if we had a valid read...
            if (TMDBCfgStr != null)
            {
                try {
                    //parse the config info...
                    getTMDBConfigDataFromJson(TMDBCfgStr);
                }
                catch (JSONException e)
                {
                    Log.e(LOG_TAG, "JSON Error parsing cfg data ", e);
                    //don't return here - try to get movie data anyway (will not get images though)
                }
            }

        }

        //set up something to parse into
        //and parse
        //if error, just return
        if (moviesJsonStr == null) {
            return;
        }

        try {
            //now parse the movie json data captured earlier
            getMovieDataFromJson(moviesJsonStr);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, "JSON Error parsing movie data ", e);

            return;
        }
    }

    /*
        Function takes in a url string and returns data returned from the URL on a get query
        Used to get both movie data and config data from TMDB
     */
    private String getTMDBDataFromURL(String urlbuild) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;
        StringBuffer buffer = new StringBuffer();

        try {

            //make the URL
            URL url = new URL(urlbuild);

            // Create the request to TMDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            moviesJsonStr = buffer.toString();

            Log.v(LOG_TAG, "TMDB JSON String:" + moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "URL Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attemping
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        //return the string...
        return moviesJsonStr;
    }

    /*
    //  Parse config information from TMDB call here
    //  Pass in JSON string blob
    //  Use to set the globals for base_url and the default poster load size
    //  Function will set the mBase_URL and mImageSizePath private class globals
    //  Structure of json is avaiable at TMDB API page, at
    //  http://http://docs.themoviedb.apiary.io/#reference
    */
    private void getTMDBConfigDataFromJson(String configJsonStr)
            throws JSONException {
        final String TMDB_CFG_BASEURL = "base_url";
        final String TMDB_CFG_POSTERSIZE  = "poster_sizes";
        final String TMDB_CFG_IMAGES = "images";

        //JSON quick ref here for config structure...
        //obj->images->base_url
        //obj->images->poster_sizes
        JSONObject cfgJson = new JSONObject(configJsonStr);
        JSONObject cfgJSImages = cfgJson.optJSONObject(TMDB_CFG_IMAGES);

        //get baseURL
        mBaseURL = cfgJSImages.getString(TMDB_CFG_BASEURL);

        //get the poster size array
        JSONArray cfgArray = cfgJSImages.getJSONArray(TMDB_CFG_POSTERSIZE);

        // now get poster prefix. Lets use the 3rd from smallest size (pre instructions). (or smallest if only one element)
        if (cfgArray.length() > 2) {
            mImageSizePath = (String) cfgArray.get(2);
        } else if (cfgArray.length() > 0) {
            //get next biggest...
            mImageSizePath = (String) cfgArray.get(cfgArray.length()-1);
        } else {
            mImageSizePath = null;  //boo - didn't get array for some reason...
        }

        //all done... Log the data
        Log.v(LOG_TAG, "Parse out TMDB config strings (base_url, imagepath): " + mBaseURL + " " + mImageSizePath);

    }

    /*
        Function below will parse a TMDB discovery JSON object.
        Structure of json is avaiable at TMDB API page, at
        http://http://docs.themoviedb.apiary.io/#reference
        Note that this code can be refactored to be more generic in future (use same JSON parser
        for both config and discovery). However the functionality of the code is limited enough
        to make it a low ROI.
        Also uses class globals mBaseURL, mImageSizePath to construct the url for image loading
     */
    private void getMovieDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_LIST = "results";
        final String TMDB_TITLE = "title";
        final String TMDB_SYNOPSIS = "overview";
        final String TMDB_RELEASEDATE = "release_date";
        final String TMDB_POSTER = "poster_path";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_ID = "id";
        final String TMDB_RATING = "vote_average";

        //First parse out the movies...
        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_LIST);

        // mMovies array should have already been cleared

        //and now start reading in the data...
        for(int i = 0; i < moviesArray.length(); i++) {

            // Get the JSON object representing the movie
            JSONObject jMovie = moviesArray.getJSONObject(i);

            //extract what we need
            String title = jMovie.getString(TMDB_TITLE);
            String synopsis = jMovie.getString(TMDB_SYNOPSIS);
            String releaseDate = jMovie.getString(TMDB_RELEASEDATE);
            String posterPath = jMovie.getString(TMDB_POSTER);
            String movieID = jMovie.getString(TMDB_ID);
            String rating = jMovie.getString(TMDB_RATING);

            //create the image file path
            String imgpath = mBaseURL + mImageSizePath + posterPath;

            //and create the new movie item
            MovieItem movie = new MovieItem(movieID, title, imgpath, synopsis, rating, releaseDate, false, 0);

            //and add to our array
            mMovies.add(movie);

        }

        return;
    }

}
