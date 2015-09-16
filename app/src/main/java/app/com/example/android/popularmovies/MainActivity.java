package app.com.example.android.popularmovies;

import android.app.Fragment;
import android.content.res.Configuration;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

/*  OK - summary of project
    1) Supports phone/tablet (ideally ATV but lets see)
    2) Main activity will show gridview of movies ordered by popularity, rating, date
    or favorites (seems like hiding this on settings screen would be dumb)
    3) Settings screen used to control ordering
    4) Settings screen also controls how much data (could also do a more button - TBD)
    5) Clicking on movie brings up data/details of movie. There will also be a "More..."
        button which will invoke a search intent
        a) Phone - detail screen. Will be full screen normal fragment
        b) Tablet - detail screen will be a popup. Re-use fragment as dialog...
        c) Both UIs will also have a trailers option and a reviews text box. As well as
        ability to mark/store as favorite locally

    Steps:
    1) xflesh out all primary nav/screens
    2) xadd primary data elements like grid view with dummy data on phone
    3) xImplement tablet for above - 2 fragments, 2 activities, dynamically load fragment
    4) xflesh out additional control elements
    5) implement data population/helper libs
    6) add attribution (need an about menu item)
    "This product uses the TMDb API but is not endorsed or certified by TMDb."
    7) refactor as necessary
    8) tweak UI
 */

public class MainActivity extends AppCompatActivity
    implements MovieListFragment.OnMovieSelectedListener {

    public final static double TWO_PANE_SIZE_THRESHOLD = 5.5;    //change this constant to determine axis (in inches) to make as threadshold for 1 pane or 2 pane operation
    public static MovieData mData;     //this object will be used by other clases...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load the data if it does not exist...
        //first load the data if it does not already exist...
        if (mData == null)
        {
            mData = new MovieData(getApplicationContext());
        }

        //Lets swap the orientation if appropriate here...
        //Don't bother with large/small xml layouts (legacy anyways)
        //especially as for 7" tablets we kind of want 2 pane operation. And 7" tablets have
        //same large description as 5" phones (nexus7/nexus5 report as same)
        //Make it more general. 3 styles.
        //2 screen (for smaller screens)
        //RL for larger landscape
        //TB for larger portrait

        //get width/height...
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        double width = (float)metrics.widthPixels/(float)metrics.densityDpi;
        double height = (float)metrics.heightPixels/(float)metrics.densityDpi;

        //rather than do square roots, lets just set a threshold for going to 2 pane. Lets set 5.5" in one dimension as a threshold
        if ((width > TWO_PANE_SIZE_THRESHOLD) || (height > TWO_PANE_SIZE_THRESHOLD)) {
            //2 pane design!
            //now figure out rotation...
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setContentView(R.layout.activity_main_portrait);
            } else {
                setContentView(R.layout.activity_main_landscape);
            }
        } else {
            setContentView(R.layout.activity_main);
        }

        //Is this the 2 screen version? If so, need to add the fragment...
        if (findViewById(R.id.fragment_container) != null) {
            //Okay - need to add the fragment...

            //But only do this on a new launch...
            if (savedInstanceState != null) {
                return;
            }

            //create a movie list instance...
            MovieListFragment movieList = new MovieListFragment();

            //add the fragment to the layout...
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, movieList).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //launch settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            //show the about box...
            //use a popup window
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.about_box, null, false),ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT, true);

            View popupParent = this.findViewById(R.id.gridview_movies);
            //View popupParent = this.findViewById(R.id.fragment);
            //pw.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // PopupWindow to dismiss when when touched outside
            pw.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            pw.showAtLocation(popupParent, Gravity.CENTER, 0, 0);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //routine called when a movie is selected...
    public void onMovieSelected(int position) {
        //User selected a movie!

        //Get the detail fragment view...
        MovieDetailFragment movieDetail = (MovieDetailFragment) getSupportFragmentManager().findFragmentById(R.id.moviedetail_fragment);

        if (movieDetail != null) {
            //okay - in the tablet 2 fragment layout.
            //update the view...
            movieDetail.updateMovieView(position);
        } else {
            //okay - need to swap fragments... (phone)
            //create a new fragment and send it an argument for the selected article...
            MovieDetailFragment newFragment = new MovieDetailFragment();
            Bundle args = new Bundle();
            args.putInt(MovieDetailFragment.ARG_POSITION, position);
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            //replace the movie list fragment with the detail fragment
            //and put old on the back stack for use with back button nav...
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            //and commit
            transaction.commit();
        }

    }

    //handle the favorites checkbox here
    public void onCheckboxClicked(View view) {
        //TODO
        CheckBox favorites_check = (CheckBox) view.findViewById(R.id.checkbox_detail_favorite);
        if (favorites_check != null)
        {
            boolean checked = favorites_check.isChecked();
        }

        //and now do something with it (save/unsave)
    }

}
