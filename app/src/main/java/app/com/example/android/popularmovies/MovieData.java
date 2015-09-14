package app.com.example.android.popularmovies;

import android.content.Context;
import android.widget.ImageView;

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
    private ArrayList<MovieItem> mMovies;

    public MovieData() {
        //just init the list here...
        mMovies = new ArrayList<MovieItem>();
    }

    public void hackPopulateList(Context ctx) {
        //hack function
        for (int i = 0; i < 50; i++) {
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
    //TODO
}
