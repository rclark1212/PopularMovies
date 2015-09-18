package app.com.example.android.popularmovies;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.Time;
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
import java.util.Date;

/**
 * Created by rclark on 9/11/2015.
 * Manages the list of movies.
 * Functions supported including loading data, loading images, sorting, marking favorites
 * finding trailers?
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
    private Context mContext;               //Save off a context to use on bitmap loading
    private String mBaseURL;                //Base URL for images. Per TMDB API docs, this generally only needs to be checked once...
                                            //sure it might change if you left app open for months but deal with this on a production app. (would refresh if I hit error loading bitmap)
    private String mImageSizePath;          //to be used with mBaseURL (size of poster to load)

    public MovieData(Context ctx) {
        //just init the list here...
        mMovies = new ArrayList<MovieItem>();
        mContext = ctx;
        mBaseURL = null;
    }

    public void hackPopulateList(Context ctx) {
        //hack function
        for (int i = 0; i < 40; i++) {
            //hack in some fake data...
            String title = "TestTitle #" + i;
            String synopsis = "This movie (generic title #" + i + " is a hack test of using objects for the data structures. It is a boring and useless story of test data which continues to be boring and useless.";
            double rating = (double)i/5.;
            String release = "2012-3-3";
            String posterpath = "";
            //Bitmap posterbm = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.android_logo);
            Boolean favorite = false;
            MovieItem movie = new MovieItem(""+i,title, null, synopsis,rating,release,favorite, i*30);
            mMovies.add(movie);
        }
    }

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
        String sortby = TMDB_SORTBY_POPULARITY;     //default

        if (ordering.equals("2"))
        {
            sortby = TMDB_SORTBY_RATING;            //but if by rating...
        }

        //need to read both configuration (to get image url base)
        //and make the discovery query...
        //build discovery ...
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
            //okay, get the configuration
            Uri.Builder buildercfg = new Uri.Builder();
            buildercfg.scheme("http")
                    .authority(TMDB_API_BASE)
                    .appendPath(TMDB_VERSION)
                    .appendPath(TMDB_CONFIGURATION)
                    .appendQueryParameter(TMDB_API_KEY, apikey);

            urlbuild = buildercfg.build().toString();

            Log.v(LOG_TAG, "getting config URL string: " + urlbuild);

            //Okay - get the config info
            String TMDBCfgStr = getTMDBDataFromURL(urlbuild);

            //if we had a valid read...
            if (TMDBCfgStr != null)
            {
                try {
                    //parse the base info...
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
            //now return it
            getMovieDataFromJson(moviesJsonStr);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, "JSON Error parsing movie data ", e);

            return;
        }
    }

    private String getTMDBDataFromURL(String urlbuild) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;
        StringBuffer buffer = new StringBuffer();

        try {
            // Construct the URL for the TMDB query
            // Possible parameters are avaiable at TMDB API page, at
            // http://http://docs.themoviedb.apiary.io/#reference

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

    //  Parse config information from TMDB call here
    //  Pass in JSON string blob
    //  Use to set the globals for base_url and the default poster load size
    private void getTMDBConfigDataFromJson(String configJsonStr)
            throws JSONException {
        final String TMDB_CFG_BASEURL = "base_url";
        final String TMDB_CFG_POSTERSIZE  = "poster_sizes";
        final String TMDB_CFG_IMAGES = "images";

        //JSON quick ref here...
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

        //all done...
        Log.v(LOG_TAG, "Parse out TMDB config strings (base_url, imagepath): " + mBaseURL + " " + mImageSizePath);

    }

    //TODO - cleanup below...
    /*
        Note that this code can be refactored to be more generic in future.
        Ideally we parse the BaseURL JSON and save that data off. And never parse it again.
        For now, just use a global which contains the base URL (for images) - mJSONBaseURL
        Do it in one monolithic block of code. And note we do repetetive processing for BaseURL when
        we don't really have to do so.
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

        //master array should have already been cleared

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

            //create the image file path
            Bitmap moviebitmap = null;
            String imgpath = mBaseURL + mImageSizePath + posterPath;

            /*
            //make the URL - and read the info...
            try
            {
                Log.v(LOG_TAG, "Trying to load bitmap: " + imgpath);
                moviebitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver() , Uri.parse(imgpath));
            }
            catch (Exception e)
            {
                Log.v(LOG_TAG, "Failure getting bitmap: " + imgpath + " error " + e);
            }
            */

            MovieItem movie = new MovieItem(movieID, title, imgpath, synopsis, 0, releaseDate, false, 0);
            mMovies.add(movie);

            //TODO BELOW
            /*

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            resultStrs[i] = day + " - " + description; */
        }

//        for (String s : resultStrs) {
  //          //Log.v(LOG_TAG, "Movie entry: " + s);
    //    }
        return;// resultStrs;

    }

    //TODO
}
