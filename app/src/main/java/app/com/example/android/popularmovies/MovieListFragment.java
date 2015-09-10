package app.com.example.android.popularmovies;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 * This fragment will populate, show and respond to the main movies screen
 */
public class MovieListFragment extends Fragment {
    OnMovieSelectedListener mCallback;

    private ArrayAdapter<String> m_my_array_adapter;
    public final static String EXTRA_MESSAGE = "app.com.example.android.popularmovies.MESSAGE";
    GridView m_grid;
    //TODO
    private static final String[] TestStr = new String[] {
            "A", "B", "C", "D", "E",
            "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O",
            "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"};

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
        m_grid.setAdapter(new ImageAdapter(getActivity(), TestStr));

        //note - show the options menu on main page...
        //setHasOptionsMenu(true);

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
/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }
*/
}
