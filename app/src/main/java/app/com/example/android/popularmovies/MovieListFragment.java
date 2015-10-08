package app.com.example.android.popularmovies;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

/**
 * A placeholder fragment containing a simple view.
 * This fragment will populate, show and respond to the main movies screen in a grid view.
 * User will select an element of this gridview which will then update a second movie detail view fragment
 */
public class MovieListFragment extends Fragment {
    OnMovieSelectedListener mCallback;

    private GridView m_grid;
    private ImageAdapter m_my_array_adapter;
    private int mSelected;

    //Put in an interface for container activity to implement so that fragment can deliver messages
    public interface OnMovieSelectedListener {
        //called by movielistfragment when a movie is selected
        public void onMovieSelected(int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.fragment_movies, container, false);

        //and the aesthetics of 2pane design looks better if you differentiate the backgrounds.
        //set light gray for the list view.
        //In the two pane version, lets set a different color for movielist
        //first, are we in 2pane view? (does the movielist fragment exist?
        if (getFragmentManager().findFragmentById(R.id.movielist_fragment) != null) {
            retView.setBackgroundColor(Color.LTGRAY);
        }

        //find the grid control
        m_grid = (GridView) retView.findViewById(R.id.gridview_movies);

        //set the adapter. Note that ImageAdapter is a custom class
        m_my_array_adapter = new ImageAdapter(getActivity(), MainActivity.mData);
        m_grid.setAdapter(m_my_array_adapter);

        //and listen for a click
        m_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //once a grid item is clicked, open up or update the detail view
                mSelected = position;
                mCallback.onMovieSelected(position);
            }
        });

        return retView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //update the movies (reloads the dataset from TMDB and refreshes grid view)
        updateMovies();

        // When in two-pane layout, set the movie view to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.movielist_fragment) != null) {
            //highlight...
            //TODO - actually don't do this. Don't like the visual impact of selection. Be stateless...
        } else {
            //if in "phone" view, when we go back to the list screen, lets hide
            //the share action (yes, we could leave it there and it would work from last
            //selected but kind of confusing)
            MainActivity.mbShowShare = false;
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        // housekeeping function
        try {
            mCallback = (OnMovieSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMovieSelectedListener");
        }
    }

    //update the movie data here...
    //routine will
    // 1) clear the main movie database
    // 2) spawns a seperate thread which then loads the data
    // 3) updates the adapter on the UI thread when (2) is done.
    private void updateMovies() {

        //get ordering preference...
        //1 = popularity, 2 = rating, 3 is favorites
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String ordering = prefs.getString(getString(R.string.pref_ordering_key), getString(R.string.pref_ordering_default));

        if (MainActivity.mbInternet == false) {
            //if no internet, force the ordering to favorites
            ordering = getString(R.string.pref_value_favorites);
        }

        //throw some hack data up... For test only.
        //MainActivity.mData.clear();
        //MainActivity.mData.hackPopulateList();

        //get real data...
        //But first, notify that data no longer valid... (and clear data)
        m_my_array_adapter.notifyDataSetInvalidated();
        MainActivity.mData.clear();

        //throw up a progress bar
        ProgressBar progress = (ProgressBar) getView().findViewById(R.id.progress_bar);
        if (progress != null) progress.setVisibility(View.VISIBLE);

        //kick off the fetch background thread
        new FetchMoviesTask().execute(ordering);
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, Long> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        //background work...
        protected Long doInBackground(String... ordering) {

            if (ordering == null) {
                return 0L;
            }

            // And fetch the data...
            MainActivity.mData.loadTMDBFromNetwork(ordering[0], getResources().getString(R.string.TMDB_API_KEY));
            return 0L;
        }

        //And now to repopulate list with real data
        protected void onPostExecute(Long result) {
            //update the global adapter
            m_my_array_adapter.notifyDataSetChanged();
            m_grid.invalidateViews();   //hmm - I would expect line above to do this. But it does not :(

            //also need to update the detail view if we are in a 2pane detail tablet view
            //first, are we in 2pane view? (does the movielist fragment exist?
            if (getFragmentManager().findFragmentById(R.id.movielist_fragment) != null) {
                mCallback.onMovieSelected(MainActivity.mLastSelected);
            }

            //hide the progress bar
            ProgressBar progress = (ProgressBar) getView().findViewById(R.id.progress_bar);
            if (progress != null) progress.setVisibility(View.INVISIBLE);
        }
    }
}
