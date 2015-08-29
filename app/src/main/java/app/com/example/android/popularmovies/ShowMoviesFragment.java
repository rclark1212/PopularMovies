package app.com.example.android.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
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
public class ShowMoviesFragment extends Fragment {

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

    public ShowMoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retView = inflater.inflate(R.layout.fragment_movies, container, false);
        this.setHasOptionsMenu(true);

        //find the grid control
        m_grid = (GridView) retView.findViewById(R.id.gridview_movies);

        //set the adapter. Note that ImageAdapter is a custom class
        m_grid.setAdapter(new ImageAdapter(getActivity(), TestStr));

        //and listen for a click
        m_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //TODO
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //once a grid item is clicked, open up the detail view
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                String message = ((TextView) view.findViewById(R.id.grid_item_label)).getText().toString();
                intent.putExtra(EXTRA_MESSAGE, message + " " + message); //TODO
                startActivity(intent);
            }
        });

        return retView;
    }

}
