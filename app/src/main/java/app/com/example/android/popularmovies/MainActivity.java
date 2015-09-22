package app.com.example.android.popularmovies;

import android.app.SearchManager;
import android.content.res.Configuration;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

/*  OK - summary of project
    1) Supports phone/tablet (ideally ATV but lets see)
    2) Main activity will show gridview of movies ordered by popularity, rating, date
    or favorites (seems like hiding this on settings screen would be dumb)
    3) Settings screen used to control ordering
    4) Clicking on movie brings up data/details of movie. There will also be a "Search"
        button which will invoke a search intent
        a) Phone - detail screen. Will be full screen normal fragment
        b) Tablet - detail screen will be a popup. Re-use fragment as dialog...
        c) Both UIs will also have a trailers option and a reviews text box. As well as
        ability to mark/store as favorite locally

    Still TODO - trailers list, trailer launching and reviews

    Trailers and reviews. Ideally I would like the views to be collapsed with a header of
    just "Trailers" and "Reviews". And when you click either of these headers, they expand with
    a *full* list of the loaded trailers and reviews within the main scrollview detail window.

    I don't want a scrollable listview inside a scrollview. First, that UI sucks. Second, it is
    not well supported pre-lollipop and we want functionality back to JB. Third, I like the aesthetics
    of not having separators (except whitespace) between the different reviews. So I don't really
    want to use an expandable listview. Although this probably easiest.

    Instead, do tihs programmatically with text views. Make the header text views clickable. On
    click, programmatically insert text views for trailers/reviews below their header. And make the
    trailers clickable as well. This will keep the whitespace separators. This will also keep a single
    master page scroll control.

    License Notes:
    For image, used image from "all-free-download.com". Specific license is:
    License: Public Domain Dedication (You can copy, modify, distribute and perform the work, even for commercial purposes, all without asking permission.)

 */

public class MainActivity extends AppCompatActivity
    implements MovieListFragment.OnMovieSelectedListener {

    public final static double TWO_PANE_SIZE_THRESHOLD = 5.5;   //change this constant to determine axis (in inches) to make as threadshold for 1 pane or 2 pane operation
    public static MovieData mData;                              //this object will be used by other clases... make it public
                                                                //this is the primary database of movies data
    public static int mLastSelected = -1;                       //last selected movie
    public final static int START_ID_TRAILERS = 110;            //Start ID for trailers textviews
    public final static int START_ID_REVIEWS = 310;             //Start ID for reviews textviews

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load the data if it does not exist...
        //first create the data if it does not already exist...
        if (mData == null)
        {
            mData = new MovieData(this);
        }

        //Lets swap the orientation if appropriate here...
        //Don't bother with large/small xml layouts (legacy anyways)
        //especially as for 7" tablets we kind of want 2 pane operation. And 7" tablets have
        //same "large" description as 5" phones (nexus7/nexus5 report as same)
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
            //maybe a better way to do this but I am doing it with 2 layouts
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
            //if view ordering changed, should really clear out last selected flag...
            //Do it for any view ordering change at this point.
            mLastSelected = -1;
            return true;
        } else if (id == R.id.action_about) {
            //show the about box...
            //use a popup window
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.about_box, null, false),ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT, true);

            View popupParent = this.findViewById(R.id.gridview_movies);

            // PopupWindow to dismiss when when touched outside
            pw.setBackgroundDrawable(new ColorDrawable(Color.GRAY));

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

        // does the detail view exist?
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
        mLastSelected = position;
    }

    //
    //handle the favorites checkbox here
    //
    public void onCheckboxClicked(View view) {
        //TODO
        CheckBox favorites_check = (CheckBox) view.findViewById(R.id.checkbox_detail_favorite);
        if (favorites_check != null)
        {
            Boolean checked = favorites_check.isChecked();

            //and save this off
            mData.getItem(mLastSelected).setFavorite(checked);
        }
    }

    //
    //handle the search button here
    //
    public void onSearchClick(View v) {
        //launch search intent...

        //throw movie at the end
        String searchStr = mData.getItem(mLastSelected).getTitle() + " movie";

        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, searchStr);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    //
    //  Handle the clicking of the Trailers text
    //  To be specific, we will "expand" or "contract" the list of trailers by creating (destroying)
    //  textviews programatically. We will also shift the relative layout reviews field down under
    //  the new textviews. This offers a better aesthetic than using an expandable list view and frankly
    //  a scrollable control inside a scrollable control sucks for UI.
    //
    public void onTrailersClick(View v) {
        //ok - lets hack this up for a moment
        //but use the toggle function

        ArrayList<String> data = new ArrayList<String>();

        //TODO - fix
        for (int i = 0; i < 10; i++) {
            data.add("hack trailer number " + i);
        }

        //and toggle list open/closed
        ToggleList(v,R.id.detail_trailers,R.id.detail_reviews,START_ID_REVIEWS, data);
    }

    //
    //  Handle the clicking of the Reviews text
    //
    public void onReviewsClick(View v) {
        //ok - lets hack this up for a moment
        //but use the toggle function

        ArrayList<String> data = new ArrayList<String>();

        //TODO - fix
        for (int i = 0; i < 10; i++) {
            data.add("hack review number " + i);
        }

        //and toggle list open/closed
        ToggleList(v,R.id.detail_reviews,0,START_ID_TRAILERS, data);

    }

    //
    //  Utility function to expand or contract a reviews or trailers list.
    //  Takes in the source view (one of the two textviews)
    //  Also takes in a belowID (expand below this) and an aboveID (expand above this ID)
    //  Takes in a startID (use this ID to start for created views)
    //  And data - this contains the text to show - note it must be valid even when removing views
    //  This routine is *not* generic and is meant to be used for the very specific purpose
    //  of only the trailers/review layout.
    //
    private void ToggleList(View v, int belowID, int aboveID, int startID, ArrayList<String> data) {

        //Get the detail fragment view (one up from view passed in)...
        ViewParent parent = v.getParent();
        RelativeLayout detailLayout = (RelativeLayout) parent;

        //does the start ID exist already?
        if (detailLayout.findViewById(startID) != null) {
            //list is already expanded
            //remove all the textviews
            for (int i = 0; i < data.size(); i++) {
                detailLayout.removeView(detailLayout.findViewById(startID + i));
            }

            //does a view below exist?
            if (aboveID != 0) {
                //now shift up the view below...
                //find the bottow reference view first
                TextView viewbottom = (TextView) detailLayout.findViewById(aboveID);   //find the view to move (the bottom one)
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewbottom.getLayoutParams();   //get its params
                params.addRule(RelativeLayout.BELOW, belowID);                      //reset params to follow the view above
                viewbottom.setLayoutParams(params);                                 //and set it
            }
            //all done removing a view
        } else {
            //expand all the views
            for (int i = 0; i < data.size(); i++) {
                TextView newtext = new TextView(this);      //create the textview
                newtext.setText(data.get(i));               //set the text from data array
                newtext.setId(startID + i);                 //set the ID (so we can process clicks and destroy it)
                newtext.setClickable(true);                 //make it clickable (reviews we will ignore click)
                //and listen for a click
                newtext.setOnClickListener(trailers_listener);  //and put on a listener

                //create a layout params file for this new textview
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 5, 5, 5);     //set margins. TODO - remove hardcode
                params.setMarginStart(20);      //extra start. TODO - remove hardcode

                //if this is the first of the new views, set it to be below the top view (belowID)
                if (i == 0) {
                    params.addRule(RelativeLayout.BELOW, belowID);
                } else {
                    //set the view to follow the last one
                    params.addRule(RelativeLayout.BELOW, startID + i - 1);
                }

                //and add the view!
                detailLayout.addView(newtext, params);

                //fix up reviews
                //is there a view below which needs to be shifted?
                if (aboveID != 0) {
                    //get the view
                    TextView viewbottom = (TextView) detailLayout.findViewById(aboveID);
                    RelativeLayout.LayoutParams bottomparams = (RelativeLayout.LayoutParams) viewbottom.getLayoutParams();
                    //set it to be below the last of the expanded list
                    bottomparams.addRule(RelativeLayout.BELOW, startID + 10 - 1);
                    //and update the params
                    viewbottom.setLayoutParams(bottomparams);
                }
            }
        }
    }


    //
    //  Handle the clicking of the Reviews text
    //
    View.OnClickListener trailers_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //hmm - do something
            Toast.makeText(getApplication(),"yay! you clicked!",Toast.LENGTH_SHORT).show();
        }
    };

}
