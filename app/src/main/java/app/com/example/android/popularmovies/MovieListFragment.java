package app.com.example.android.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 * This fragment will populate, show and respond to the main movies screen
 */
public class MovieListFragment extends Fragment {
    OnMovieSelectedListener mCallback;

    public final static String EXTRA_MESSAGE = "app.com.example.android.popularmovies.MESSAGE";
    private GridView m_grid;
    private ImageAdapter m_my_array_adapter;

    //Put in an interface for container activity to implement so that fragment can deliver messages
    public interface OnMovieSelectedListener {
        //called by movielistfragment when a movie is selected
        public void onMovieSelected(int position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.fragment_movies, container, false);

        //find the grid control
        m_grid = (GridView) retView.findViewById(R.id.gridview_movies);

        //set the adapter. Note that ImageAdapter is a custom class
        m_my_array_adapter = new ImageAdapter(getActivity(), MainActivity.mData);
        m_grid.setAdapter(m_my_array_adapter);

        //and listen for a click
        m_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //TODO
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //once a grid item is clicked, open up or update the detail view
                /*
                //Intent intent = new Intent(getActivity(), DetailActivity.class);
                String message = ((TextView) view.findViewById(R.id.grid_item_label)).getText().toString();
                intent.putExtra(EXTRA_MESSAGE, message + " " + message); //TODO
                startActivity(intent); */
                mCallback.onMovieSelected(position);
            }
        });

        return retView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //update the movies...
        updateMovies();

        // When in two-pane layout, set the movie view to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.movielist_fragment) != null) {
            //highlight...
            //TODO
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnMovieSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMovieSelectedListener");
        }
    }

    //update the movie data here...
    private void updateMovies() {

        //get ordering preference...
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String ordering = prefs.getString(getString(R.string.pref_ordering_key), getString(R.string.pref_ordering_default));

        //throw some hack data up...
        //MainActivity.mData.clear();
        //MainActivity.mData.hackPopulateList(getContext());

        //get real data...
        //But first, notify that data no longer valid... (and clear data)
        m_my_array_adapter.notifyDataSetInvalidated();
        MainActivity.mData.clear();
        new FetchMoviesTask().execute(ordering);
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        //background work...
        protected String doInBackground(String... ordering) {

            if (ordering == null) {
                return "";
            }

            MainActivity.mData.loadTMDBFromNetwork(ordering[0], getResources().getString(R.string.TMDB_API_KEY));
            return "";
        }

        //And now to repopulate with real data
        protected void onPostExecute(String dummy) {
            //update the global adapter
            m_my_array_adapter.notifyDataSetChanged();
            m_grid.invalidateViews();   //hmm - I would expect line above to do this. But it does not :(
        }
    }
}
