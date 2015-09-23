package app.com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
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
import java.util.HashSet;
import java.util.Set;

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

    private ArrayList<MovieItem> mMovies;   //The main database. Note that there are 2 other global string arrays used
                                            //which are defined in MainActivity to store the trailer/review lists
                                            //This class provides utility functions to load these from TMDB
    private String mBaseURL;                //Base URL for images. Per TMDB API docs, this generally only needs to be checked once...
                                            //sure it might change if you left app open for months but deal with this on a production app. (would refresh if I hit error loading bitmap)
    private String mImageSizePath;          //to be used with mBaseURL (size of poster to load)
    private Context mCtx;                   //Used for shared preferences. This class already pretty purpose targeted so okay.

    public MovieData(Context ctx) {
        //just init the list here...
        //note this is the main database of movies we load
        mMovies = new ArrayList<MovieItem>();
        mBaseURL = null;
        mCtx = ctx;
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
            String movieID = ""+i;
            MovieItem movie = new MovieItem(movieID,title, null, synopsis,rating,release,favorite, i*30);
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
        saveFavorites();
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

        String sortby = TMDB_SORTBY_POPULARITY;     //default to popularity search

        if (ordering.equals("2")) {                 //sort by rating
            sortby = TMDB_SORTBY_RATING;            //but if by rating (per settings), sort by rating
        }

        /*
            A note about favorites here...
            Two options for loading favorites really
            The most efficient option since we have the list of favorite movieIDs saved
            away is to create a 3rd query type which just directly loads the movie based
            on movieID.
            The second, less efficient option (but less code to write) is to recognize data
            set is limited to the discovery query return of popularity/rating queries (2 queries).
            So we could just build a big list by quering twice (both ways) and then discarding
            the movies that don't match favorites.
            Now the downside of the second approach is if discovery/popularity queries change,
            you will lose your favorites. So will go ahead and take first approach...

         */

        //need to read both configuration (to get image url base)
        //and make the discovery query...

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

            String urlbuild = buildercfg.build().toString();

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

        //Okay - deal with favorites here (or else process a discovery query
        if (ordering.equals("3")) {                 //load favorites

            //  First get the favorites
            String[] favorites = getFavorites();

            //Check if there are any favorites...
            if (favorites != null) {
                //and iterate through the favorites
                for (int i = 0; i < favorites.length; i++) {
                    //build movie query URL
                    // Possible parameters are avaiable at TMDB API page, at
                    // http://http://docs.themoviedb.apiary.io/#reference
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("http")
                            .authority(TMDB_API_BASE)
                            .appendPath(TMDB_VERSION)
                            .appendPath(TMDB_MOVIES)
                            .appendPath(favorites[i])
                            .appendQueryParameter(TMDB_API_KEY, apikey);

                    String urlbuild = builder.build().toString();

                    Log.v(LOG_TAG, "Built favorites query string: " + urlbuild);

                    moviesJsonStr = getTMDBDataFromURL(urlbuild);

                    //set up something to parse into
                    //and parse
                    //if error, just return
                    if (moviesJsonStr == null) {
                        return;
                    }

                    try {
                        //now parse the movie json data captured earlier
                        getMovieDataFromJson(moviesJsonStr, false);
                        loadFavorites();                    //note that every movie a favorite. We could optimize
                        //here and just blanket set every favorite flag but
                        //like the code cleanliness of just calling function again
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "JSON Error parsing movie data ", e);

                        return;
                    }
                }
            }
        } else {                                    //else is one of the discovery queries
            //build discovery URL first
            // Construct the URL for the TMDB query
            // Possible parameters are avaiable at TMDB API page, at
            // http://http://docs.themoviedb.apiary.io/#reference
            Uri.Builder builder = new Uri.Builder();
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

            //set up something to parse into
            //and parse
            //if error, just return
            if (moviesJsonStr == null) {
                return;
            }

            try {
                //now parse the movie json data captured earlier
                getMovieDataFromJson(moviesJsonStr, true);
                loadFavorites();                    //and parse/apply favorites
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Error parsing movie data ", e);

                return;
            }
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
        Note that the routine handles both discovery queries (which adds an array to moviesDB
        Or simple movie queries (which adds one element)
        For quering an array, pass in true for bArray. For a single movie, pass in false
     */
    private void getMovieDataFromJson(String moviesJsonStr, Boolean bArray)
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
        JSONArray moviesArray = null;
        int readLength = 1;

        //if we are parsing an array, get it and reset the length of the read
        if (bArray == true) {
            moviesArray = moviesJson.getJSONArray(TMDB_LIST);
            readLength = moviesArray.length();
        }

        // mMovies array should have already been cleared

        //and now start reading in the data...
        for(int i = 0; i < readLength; i++) {

            // Get the JSON object representing the movie
            JSONObject jMovie;

            if (bArray != true) {
                //urp - single movie query...
                jMovie = moviesJson;
            } else {
                jMovie = moviesArray.getJSONObject(i);
            }

            //extract what we need
            String title = jMovie.getString(TMDB_TITLE);
            String synopsis = jMovie.getString(TMDB_SYNOPSIS);
            String releaseDate = jMovie.getString(TMDB_RELEASEDATE);
            String posterPath = jMovie.getString(TMDB_POSTER);
            String movieID = jMovie.getString(TMDB_ID);
            String rating = jMovie.getString(TMDB_RATING);

            //create the image file path
            String imgpath = mBaseURL + mImageSizePath + posterPath;

            //If there is no valid poster path, set null for this value as then we will force load
            //a backup bitmap
            if (posterPath.equals("null")) imgpath = null;

            //and create the new movie item
            MovieItem movie = new MovieItem(movieID, title, imgpath, synopsis, rating, releaseDate, false, 0);

            //and add to our array
            mMovies.add(movie);

        }

        return;
    }

    //
    // General favorites notes:
    // Save/load favorites from shared preferences. Store a list of MovieIDs as favorites.
    // Now TMDB has a favorites function but you have to log in to use it. Allow favorites
    // without requiring signing up for an account.
    // Save off the favorites list on a .clear. Load favorites list on an update.
    //

    //
    // Save favorites:
    // Scan through main array and save off all the movieIDs which are marked as favorite
    // Saved as a comma delimited preference string of MovieIDs
    //
    private void saveFavorites() {

        // as the list of movies may change depending on search and we don't want to overwrite favorites
        // that are defined for movies not in the list, first read the current favorites list and add those
        // movies which are not contained within
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        Set<String> favorites = pref.getStringSet(mCtx.getResources().getString(R.string.favorites_list), null);

        // loop through the current movies. if movie not contained in oldfavorites, add the old favorite
        // to the new set (preserve it)
        // and yes, as commented below, this could  be terribly inefficient but working with a small
        // dataset.
        for (int i = 0; i < mMovies.size(); i++) {
            // get the movieID
            String movieID = mMovies.get(i).getMovieID();
            //is it not in current favorites list already?
            if (favorites == null) {
                //first time run. No favorites. So add if necessary
                favorites = new HashSet<String>();              //to store favorite MovieIDs and store into prefs
                if (mMovies.get(i).getFavorite() == true) {
                    //need to add it
                    favorites.add(movieID);
                }
            }
            else if (favorites.contains(movieID) == true) {
                // this movie already in favorites list..
                // so either do nothing (keep it). or remove it.
                if (mMovies.get(i).getFavorite() == false) {
                    // no longer a favorite. Remove it
                    favorites.remove(movieID);
                }
            } else {
                // movie not in favorites
                if (mMovies.get(i).getFavorite() == true) {
                    //need to add it
                    favorites.add(movieID);
                }
            }
        }

        //and now save off this string set
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(mCtx.getResources().getString(R.string.favorites_list), favorites);
        editor.commit();
    }

    //
    // Load the list of favorite movieIDs and apply/update favorite status of the main array
    // Note that this routine both "gets" the favorite list from preferences as well as
    // processes favorites against the currently loaded movie database
    //
    private void loadFavorites() {
        //Get the string from shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        Set<String> favorites = pref.getStringSet(mCtx.getResources().getString(R.string.favorites_list),null);

        //Now scan through mMovies and if a match, set favorite.
        //And yes, this could be *terribly* inefficient O(n^2).
        //But will assume that Set is ordered/hashed for searching and .contains is efficient.
        //If not, would order the list and do a binary search at minimum.

        //Is there a favorites list? (there may not be yet). Check...
        if (favorites != null) {
            // loop through all the movies...
            for (int i = 0; i < mMovies.size(); i++) {
                String movieID = mMovies.get(i).getMovieID();

                //is this movieID in our favorites set?
                mMovies.get(i).setFavorite(favorites.contains(movieID));
            }
        }
    }

    //
    // Gets favorites (and returns as a string array)
    // Needed for sorting by favorites option.
    //
    private String[] getFavorites() {
        //Get the string from shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mCtx);
        Set<String> favorites = pref.getStringSet(mCtx.getResources().getString(R.string.favorites_list),null);

        //and return as a string array
        if (favorites != null) {
            return favorites.toArray(new String[favorites.size()]);
        }

        return null;
    }

}
