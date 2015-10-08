package app.com.example.android.popularmovies;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
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
    5) Both the share and CP functionality have been implemented.

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

    public static MovieData mData;                              //this object will be used by other clases... make it public
                                                                //this is the primary database of movies
    public static int mLastSelected = -1;                       //last selected movie
    public static Boolean mbShowShare = false;                  //indicates if share menu option should be shown
    public static Boolean mbInternet = true;                    //set to false if no internet (and only show favorites)
    private Boolean mbFavoritesExist = false;                   //internal check to see if favorites db exists on start

    public final static int START_ID_TRAILERS = 110;            //Start ID for trailers textviews
    public final static int START_ID_REVIEWS = 310;             //Start ID for reviews textviews
    public final static int TEXT_MARGIN = 6;                    //Margin to use on trailer/review list
    public final static int START_BUTTON_TEXT_MARGIN = 20;      //Margin to give on start button for trailer list
    public final static double TWO_PANE_SIZE_THRESHOLD = 5.5;   //change this constant to determine axis (in inches) to make as threadshold for 1 pane or 2 pane operation
    public final static String YOUTUBE_URL = "http://www.youtube.com/watch?v=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        // First, do we have internet?
        //
        if (isOnline() == false) {
            mbInternet = false;
            String message;

            //check if there is a favorites database here. If not, exit with message. If favorites, allow operation
            File dbFile = getApplicationContext().getDatabasePath(FavoritesContentProvider.DATABASE_NAME);
            if (dbFile != null) {
                mbFavoritesExist = dbFile.exists();
            }

            //Show a different warning if there is no favorites DB (and exit)
            //vs if there is a favorites DB (allow operation).
            if (mbFavoritesExist) {
                message = getString(R.string.alert_internet);
            } else {
                message = getString(R.string.fatal_internet);
            }

            new AlertDialog.Builder(this)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // exit
                            if (mbFavoritesExist == false) {
                                finish(); //exit if no favorites
                            }
                        }
                    }).create().show();
        }

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

        // And implement a share action
        // Fetch/store provider
        // But only if there is data to share
        MenuItem item = menu.findItem(R.id.menu_item_share);

        if (mbShowShare == true) {

            ShareActionProvider shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            if (shareAction != null) {
                shareAction.setShareIntent(createShareIntent());
            }
        } else {
            //hide the share
            item.setVisible(false);
        }

        return true;
    }

    //
    // And share the first video URL
    //
    private Intent createShareIntent() {
        //set intent
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intentShare.setType("text/plain");
        intentShare.putExtra(Intent.EXTRA_TEXT, YOUTUBE_URL + mData.mTrailers.get(0).key);
        return intentShare;
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
            //But only if we have internet (else forced to sort by favorites only
            if (mbInternet == true) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                //if view ordering changed, should really clear out last selected flag...
                //Do it for any view ordering change at this point.
                mLastSelected = -1;
            } else {
                Toast.makeText(getApplicationContext(),R.string.settings_internet,Toast.LENGTH_SHORT).show();
            }
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
            //first, close any expanded lists
            RelativeLayout layout = (RelativeLayout) movieDetail.getView().findViewById(R.id.detail_fragment);
            CloseList(layout, START_ID_TRAILERS, mData.mTrailers.size());
            CloseList(layout, START_ID_REVIEWS, mData.mReviews.size());

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
    //  Add utility routine to check if we have internet connection. Check on start
    //
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo == null)
            return false;

        return netInfo.isConnected();
    }

    //  Handle the clicking of the Trailers text
    //  To be specific, we will "expand" or "contract" the list of trailers by creating (destroying)
    //  textviews programatically. We will also shift the relative layout reviews field down under
    //  the new textviews. This offers a better aesthetic than using an expandable list view and frankly
    //  a scrollable control inside a scrollable control sucks for UI.
    //
    public void onTrailersClick(View v) {
        //use the toggle function

        //and toggle list open/closed
        //however note that we need to create an arraylist param to send in (trailers are in a structure)
        ArrayList<String> params = new ArrayList<String>();

        for (int i = 0; i < mData.mTrailers.size(); i++) {
            params.add(mData.mTrailers.get(i).name);
        }
        ToggleList(v,R.id.detail_trailers,R.id.detail_reviews,START_ID_TRAILERS, R.drawable.play, true, params);
    }

    //
    //  Handle the clicking of the Reviews text
    //
    public void onReviewsClick(View v) {
        //use the toggle function

        //and toggle list open/closed
        ToggleList(v,R.id.detail_reviews,0,START_ID_REVIEWS, 0, false, mData.mReviews);

    }

    //
    // And one more utility function to close the lists if they are expanded
    // (for tablet case where detail view is not being destroyed/recreated)
    //  pass in the layout, startID and length.
    //
    private void CloseList(RelativeLayout layout, int startID, int listLength) {

        // is there valid data?
        if (listLength == 0) return;

        //does the start ID exist already?
        if (layout.findViewById(startID) != null) {
            //close it on up
            for (int i = 0; i < listLength; i++) {
                layout.removeView(layout.findViewById(startID + i));
            }

            //and now fix up the reviews/trailers layouts (put reviews right after trailers)
            TextView reviewsView = (TextView) layout.findViewById(R.id.detail_reviews);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) reviewsView.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.detail_trailers);
            reviewsView.setLayoutParams(params);
        }
    }


    //
    //  Utility function to expand or contract a reviews or trailers list.
    //  Takes in the source view (one of the two textviews)
    //  Also takes in a insertListBelowThisID (expand below this) and an insertListAboveThisID (expand above this ID)
    //  Takes in a startID (use this ID to start for created views)
    //  Takes in a drawable which is prepended to text (if it is not 0). Also note a non-zero drawable
    //     indicates a play function and thus is clickable. Thus we will set this text blue (to indicate it is clickable)
    //  And a flag to keep text single line (ellipsize) or not.
    //  And data - this contains the text to show - note it must be valid even when removing views
    //  This routine is *not* generic and is meant to be used for the very specific purpose
    //  of only the trailers/review layout.
    //
    private void ToggleList(View v, int insertListBelowThisID, int insertListAboveThisID, int startID, int drawableID, Boolean bSingleLine, ArrayList<String> data) {

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
            if (insertListAboveThisID != 0) {
                //now shift up the view below...
                //find the bottow reference view first
                TextView viewbottom = (TextView) detailLayout.findViewById(insertListAboveThisID);   //find the view to move (the bottom one)
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewbottom.getLayoutParams();   //get its params
                params.addRule(RelativeLayout.BELOW, insertListBelowThisID);                      //reset params to follow the view above
                viewbottom.setLayoutParams(params);                                 //and set it
            }
            //all done removing a view
        } else {
            //expand all the views
            for (int i = 0; i < data.size(); i++) {
                TextView newtext = new TextView(this);      //create the textview
                newtext.setText(Html.fromHtml(data.get(i)));//set the text from data array - process html tags
                newtext.setId(startID + i);                 //set the ID (so we can process clicks and destroy it)
                newtext.setClickable(true);                 //make it clickable (reviews we will ignore click)
                //and listen for a click
                newtext.setOnClickListener(trailers_listener);  //and put on a listener
                if (drawableID != 0) {
                    newtext.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0, 0, 0);
                    //and set color blue
                    newtext.setTextColor(Color.BLUE);
                }

                //create a layout params file for this new textview
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(TEXT_MARGIN, TEXT_MARGIN, TEXT_MARGIN, TEXT_MARGIN);  //set margins.
                params.setMarginStart(START_BUTTON_TEXT_MARGIN);                        //extra start.
                newtext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F);                   //set the size

                //is this single line?
                if (bSingleLine == true) {
                    newtext.setEllipsize(TextUtils.TruncateAt.END);
                }

                //if this is the first of the new views, set it to be below the top view (insertListBelowThisID)
                if (i == 0) {
                    params.addRule(RelativeLayout.BELOW, insertListBelowThisID);
                } else {
                    //set the view to follow the last one
                    params.addRule(RelativeLayout.BELOW, startID + i - 1);
                }

                //and add the view!
                detailLayout.addView(newtext, params);

                //fix up reviews
                //is there a view below which needs to be shifted?
                if (insertListAboveThisID != 0) {
                    //get the view
                    TextView viewbottom = (TextView) detailLayout.findViewById(insertListAboveThisID);
                    RelativeLayout.LayoutParams bottomparams = (RelativeLayout.LayoutParams) viewbottom.getLayoutParams();
                    //set it to be below the last of the expanded list
                    bottomparams.addRule(RelativeLayout.BELOW, startID + data.size() - 1);
                    //and update the params
                    viewbottom.setLayoutParams(bottomparams);
                }
            }
        }
    }

    //
    // Utility routine to start youtube intent. Comes from
    // http://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
    //
    public void watchYoutubeVideo(String id){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse(YOUTUBE_URL+id));
            startActivity(intent);
        }
    }

    //
    //  Handle the clicking of the Reviews text
    //
    View.OnClickListener trailers_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //hmm - do something
            int clickID = v.getId();
            int clickPostion = 0;

            if (clickID >= START_ID_REVIEWS) {
                //Do nothing. Reviews don't do anything on click
                //clickPostion = clickID - START_ID_REVIEWS;
                //Toast.makeText(getApplication(),"yay! you clicked item " + clickPostion + " for reviews",Toast.LENGTH_SHORT).show();
            } else {
                clickPostion = clickID - START_ID_TRAILERS;
                //Toast.makeText(getApplication(),"yay! you clicked item " + clickPostion + " for trailers",Toast.LENGTH_SHORT).show();
                //Start a youtube intent
                watchYoutubeVideo(mData.mTrailers.get(clickPostion).key);
            }
        }
    };

}
