package app.com.example.android.popularmovies;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by rclark on 9/9/2015.
 * Pulled from fragment example in android dev documentation
 * This fragment displays detailed information for a selected movie.
 * It can be presented to user in a multi-fragment 2 pane UI or in a single screen
 */
public class MovieDetailFragment extends Fragment {
    final static String ARG_POSITION = "position";      //used to pass which item selected when we load fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            MainActivity.mLastSelected = savedInstanceState.getInt(ARG_POSITION);
        }

        // Inflate the layout for this fragment and return
        return inflater.inflate(R.layout.movie_detail, container, false);

    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            updateMovieView(args.getInt(ARG_POSITION));
        } else if (MainActivity.mLastSelected != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateMovieView(MainActivity.mLastSelected);
        } else {
            // This should only be hit by tablet on initial conditions (app launch). Nothing selected.
            // A couple options here - we could blank this fragment. But that would look weird.
            // We could put up a screen saying "please select a movie". But kind of redundant.
            // Instead, how about just setting the detail screen to the first movie in the list?
            // Nope - won't work. Race condition on loading data. Instead, lets pass in an argument
            // we can use to blank screen of data
            updateMovieView(-1);
        }
    }


    private void enableMovieViewObject(Boolean bEnable, View object) {

        if (object == null) return;

        if (bEnable == false) {
            object.setVisibility(View.INVISIBLE);
        } else {
            object.setVisibility(View.VISIBLE);
        }

    }


    public void updateMovieView(int position) {

        //update detail view based on position selection

        //First though, lets see if this is initial launch and we should disable the view? (nothing selected)
        Boolean bEnableView = true;

        //On slow devices, there can be a race condition between this function and population
        //of the database. Do a couple of checks here (or could put in a semaphore).
        //Rather than just return, blank out the view if data not ready
        //Note that routine returns early with a blanked out view
        if (MainActivity.mData == null) bEnableView = false;
        if (position >= MainActivity.mData.length()) bEnableView = false;

        //And, if explicit, blank out view
        if (position == -1) bEnableView = false;

        //show/hide the view...
        //Do this by show/hide the controls

        //title
        TextView text = (TextView) getActivity().findViewById(R.id.detail_movietitle);
        enableMovieViewObject(bEnableView, text);
        //synopsis
        text = (TextView) getActivity().findViewById(R.id.text_detail_description);
        enableMovieViewObject(bEnableView, text);
        //Then release date
        text = (TextView) getActivity().findViewById(R.id.text_detail_releasedate);
        enableMovieViewObject(bEnableView, text);
        //Then release year in bigger font (just first 4 chars)
        text = (TextView) getActivity().findViewById(R.id.text_detail_year);
        enableMovieViewObject(bEnableView, text);
        //Then user rating... (and append "user rating" to it
        text = (TextView) getActivity().findViewById(R.id.text_detail_rating);
        enableMovieViewObject(bEnableView, text);
        //Then reviews
        text = (TextView) getActivity().findViewById(R.id.detail_reviews);
        enableMovieViewObject(bEnableView, text);
        //Then trailers
        text = (TextView) getActivity().findViewById(R.id.detail_trailers);
        enableMovieViewObject(bEnableView, text);
        //Then line
        View vw = (View) getActivity().findViewById(R.id.detail_horizontal_line);
        enableMovieViewObject(bEnableView, vw);

        //Then image...
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.detail_image);
        enableMovieViewObject(bEnableView, imageView);
        //search button
        Button search_button = (Button) getView().findViewById(R.id.button_detail_search);
        enableMovieViewObject(bEnableView, search_button);
        //finally, set the favorites checkbox state
        CheckBox checkbox_favs = (CheckBox) getActivity().findViewById(R.id.checkbox_detail_favorite);
        enableMovieViewObject(bEnableView, checkbox_favs);

        //and just return if we are hiding controls...
        if (bEnableView == false) return;

        //Do title first
        String message = MainActivity.mData.getItem(position).getTitle();
        text = (TextView) getActivity().findViewById(R.id.detail_movietitle);
        if (text != null) {
            text.setText(message.toCharArray(), 0, message.length());
        }

        //Then synopsis
        message = MainActivity.mData.getItem(position).getSynopsis();
        text = (TextView) getActivity().findViewById(R.id.text_detail_description);
        if (text != null) {
            text.setText(message.toCharArray(), 0, message.length());
        }

        //Then release date
        message = MainActivity.mData.getItem(position).getReleaseDate();
        text = (TextView) getActivity().findViewById(R.id.text_detail_releasedate);
        if (text != null) {
            text.setText(message.toCharArray(), 0, message.length());
        }

        //Then release year in bigger font (just first 4 chars)
        if (message.length() > 4) {
            message = message.substring(0, 4);
        }
        text = (TextView) getActivity().findViewById(R.id.text_detail_year);
        if (text != null) {
            text.setText(message.toCharArray(), 0, message.length());
        }

        //Then user rating... (and append "user rating" to it
        message = MainActivity.mData.getItem(position).getRating() + " " + getResources().getString(R.string.user_rating);
        text = (TextView) getActivity().findViewById(R.id.text_detail_rating);
        if (text != null) {
            text.setText(message.toCharArray(), 0, message.length());
        }

        //Then image...
        imageView = (ImageView) getActivity().findViewById(R.id.detail_image);
        if (imageView != null) {
            //Use the clever background jpg loading facility of picasso...
            String posterpath = MainActivity.mData.getItem(position).getPosterPath();
            if (posterpath != null) {
                Picasso.with(getContext()).load(posterpath).into(imageView);
            } else {
                //throw up some generic image...
                imageView.setImageResource(R.drawable.cinema_strip_movie_film);
            }
        }

        //finally, set the favorites checkbox state
        checkbox_favs = (CheckBox) getActivity().findViewById(R.id.checkbox_detail_favorite);
        if (checkbox_favs != null) {
            checkbox_favs.setChecked(MainActivity.mData.getItem(position).getFavorite());
        }

        MainActivity.mLastSelected = position;

        //and kick off a background thread to load trailers and reviews here
        if (MainActivity.mLastSelected >= 0) {
            updateMovieDetails(position);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, MainActivity.mLastSelected);
    }

    //update the detailed movie data here...
    //routine will load trailers/reviews into two global arrays (mReviews, mTrailers)
    // 1) clear the array data
    // 2) spawn a seperate thread which then loads the data
    //
    // Note one issue (and something to possibly fix in future). We are asynchronously loading
    // the movie data. If a user quickly clicks to expand trailer/review views (or loading takes
    // a really long time), list might not be fully populated when expanded. Could fix with a simple
    // global semiphone and some waits... (but this defeats purpose of UI not being blocked).
    //
    private void updateMovieDetails(int selected) {

        //get real data...
        //But first, clear the data
        MainActivity.mData.mReviews.clear();
        MainActivity.mData.mTrailers.clear();

        //Don't bother with a progress bar since this should be such a short load
        //will be hidden by user navigating UI.

        //kick off the fetch background thread
        Long position = new Long(selected);
        new FetchMovieDetailTask().execute(position);
    }

    private class FetchMovieDetailTask extends AsyncTask<Long, Void, Long> {
        private final String LOG_TAG = FetchMovieDetailTask.class.getSimpleName();

        //background work...
        protected Long doInBackground(Long... selected) {

            // And fetch the data...
            MainActivity.mData.loadTMDBReviewsTrailers(selected[0].intValue(), getResources().getString(R.string.TMDB_API_KEY));

            return 0L;
        }

        //And now back on UI thread...
        protected void onPostExecute(Long result) {
            //If there was trailer or review data loaded...
            //Mark the review/trailer text as being blue
            TextView tReviews = (TextView) getView().findViewById(R.id.detail_reviews);
            TextView tTrailers = (TextView) getView().findViewById(R.id.detail_trailers);

            if (tReviews != null) {
                if (MainActivity.mData.mReviews.size() > 0) {
                    //mark reviews text as blue
                    tReviews.setTextColor(Color.BLUE);
                } else {
                    //mark reviews text as black
                    tReviews.setTextColor(Color.BLACK);
                }
            }

            MainActivity.mbShowShare = false;           //by default don't show share
            if (tTrailers != null) {
                if (MainActivity.mData.mTrailers.size() > 0) {
                    //mark trailers text as blue
                    tTrailers.setTextColor(Color.BLUE);
                    MainActivity.mbShowShare = true;    //unless we have trailers
                } else {
                    //mark trailers text as black
                    tTrailers.setTextColor(Color.BLACK);
                }
            }

            // And, update the options menu to get the share option...
            getActivity().invalidateOptionsMenu();

            //If there were a semaphore, set it here.
        }
    }
}
