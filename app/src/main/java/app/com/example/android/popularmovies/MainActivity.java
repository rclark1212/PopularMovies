package app.com.example.android.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
    1) flesh out all primary nav/screens
    2) add primary data elements like grid view with dummy data on phone
    3) Implement tablet for above
    4) flesh out additional control elements
    5) implement data population/helper libs
    6) refactor as necessary
    7) tweak UI
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            /* TODO
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent); */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
