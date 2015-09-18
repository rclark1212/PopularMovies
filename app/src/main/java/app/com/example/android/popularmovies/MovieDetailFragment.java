package app.com.example.android.popularmovies;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by rclark on 9/9/2015.
 * Pulled from fragment example in android dev documentation
 */
public class MovieDetailFragment extends Fragment {
    final static String ARG_POSITION = "position";
    int mCurrentPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
        }

        // Inflate the layout for this fragment
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
        } else if (mCurrentPosition != -1) {
            // Set article based on saved instance state defined during onCreateView
            updateMovieView(mCurrentPosition);
        }
    }


    public void updateMovieView(int position) {

        //update detail view based on position selection

        //Do title first
        String message = MainActivity.mData.getItem(position).getTitle();
        TextView text = (TextView) getActivity().findViewById(R.id.detail_movietitle);
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
        //DateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        //message = df.format(MainActivity.mData.getItem(position).getReleaseDate());
        message = MainActivity.mData.getItem(position).getReleaseDate();
        text = (TextView) getActivity().findViewById(R.id.text_detail_releasedate);
        if (text != null) {
            text.setText(message.toCharArray(), 0, message.length());
        }

        //Then release year...
        //Calendar calendar = Calendar.getInstance();
        //calendar.setTime(MainActivity.mData.getItem(position).getReleaseDate());
        if (message.length() > 4) {
            message = message.substring(0, 4);
        }
        text = (TextView) getActivity().findViewById(R.id.text_detail_year);
        if (text != null) {
            text.setText(message.toCharArray(), 0, message.length());
        }

        //Then image...
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.detail_image);
        if (imageView != null) {
            //TODO - check below
            String posterpath = MainActivity.mData.getItem(position).getPosterPath();
            if (posterpath != null) {
                Picasso.with(getContext()).load(posterpath).into(imageView);
            } else {
                //throw up some generic image...
                imageView.setImageResource(R.drawable.android_logo);
            }
        }


        //finaly, deal with the favorites checkbox state
        CheckBox checkbox_favs = (CheckBox) getActivity().findViewById(R.id.checkbox_detail_favorite);
        if (checkbox_favs != null) {
            checkbox_favs.setChecked(MainActivity.mData.getItem(position).getFavorite());
        }

        mCurrentPosition = position;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }
}
