package app.com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by rclark on 8/29/2015.
 * Pulled from mkyong example on web for an image adapter class for use with gridview
 * http://www.mkyong.com/android/android-gridview-example/
 * Adapted for use with popularmovies
 * And note that the code was very buggy. Had to modify/fix to deal with convertview.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;       //save off the context
    private MovieData mValues;      //and a reference to the backing dataset

    public ImageAdapter(Context context, MovieData mobileValues) {
        this.mContext = context;
        this.mValues = mobileValues;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View gridView;

        // are we recycling a view?
        if (convertView == null) {

            // if not, create and inflate a new gridView
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.grid_layout, null);

        } else {
            // use the recycled view
            gridView = (View) convertView;
        }

        // set value into textview
        TextView textView = (TextView) gridView
                .findViewById(R.id.grid_item_label);

        // note that textview set up to only be 2 lines and ellipsized
        textView.setText(mValues.getItem(position).getTitle());

        // find the image
        ImageView imageView = (ImageView) gridView
                .findViewById(R.id.grid_item_image);

        //Set the image based on position (path to image already saved off)
        //Use the clever picasso method for background image loading here...
        String posterpath = mValues.getItem(position).getPosterPath();
        if (posterpath != null) {
            Picasso.with(mContext).load(posterpath).into(imageView);
        } else {
            //throw up some generic image...
            //note - will use this image in the case of being disconnected from internet (or there
            //not being a valid image in TMDB)
            imageView.setImageResource(R.drawable.cinema_strip_movie_film);
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return mValues.length();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
