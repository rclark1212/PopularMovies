package app.com.example.android.popularmovies;

import android.content.Context;
import android.net.Uri;
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
    private final String LOG_TAG = MovieData.class.getSimpleName();

    private ArrayList<MovieItem> mMovies;

    public MovieData() {
        //just init the list here...
        mMovies = new ArrayList<MovieItem>();
    }

    public void hackPopulateList(Context ctx) {
        //hack function
        for (int i = 0; i < 40; i++) {
            //hack in some fake data...
            String title = "TestTitle #" + i;
            String synopsis = "This movie (generic title #" + i + " is a hack test of using objects for the data structures. It is a boring and useless story of test data which continues to be boring and useless.";
            double rating = (double)i/5.;
            Date release = new Date(1000000);
            int poster = R.drawable.android_logo; //TODO - temp just use resource ID
            Boolean favorite = false;
            MovieItem movie = new MovieItem(i,title,poster,synopsis,rating,release,favorite, i*30);
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
            Uri.Builder builder = new Uri.Builder();

            builder.scheme("http")
                    .authority(TMDB_API_BASE)
                    .appendPath(TMDB_VERSION)
                    .appendPath(TMDB_DISCOVERY)
                    .appendPath(TMDB_MOVIES)
                    .appendQueryParameter(TMDB_API_KEY, apikey)
                   //deal with ordering...
                    .appendQueryParameter(TMDB_SORT_ORDER, "popularity.desc");

//                    .appendQueryParameter("mode", "json")
//                    .appendQueryParameter("units", "metric")
//                    .appendQueryParameter("cnt", "7");

            String urlbuild = builder.build().toString();

            Log.v(LOG_TAG, "Built query string: " + urlbuild);

            URL url = new URL(urlbuild);

            // Create the request to TMDB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                // Nothing to do.
                return;
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
                return;
            }

            moviesJsonStr = buffer.toString();

            Log.v(LOG_TAG, "Forecast JSON String:" + moviesJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attemping
            // to parse it.
            return;
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

        //set up something to parse into
        //and parse

        try {
            //now return it
            getMovieDataFromJson(moviesJsonStr);
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, "JSON Error ", e);

            return;
        }
    }

    //TODO - fix it...
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
            Date newDate = new Date(0);
            MovieItem movie = new MovieItem(0,title,R.drawable.android_logo, synopsis, 0, newDate, false, 0);
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
