package app.com.example.android.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by rclark on 8/29/2015.
 * Pulled from mkyong example for an image adapter class for use with gridview
 * http://www.mkyong.com/android/android-gridview-example/
 * Adapted for use with popularmovies
 * And note that the code was very buggy. Had to modify/fix to deal with convertview.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private MovieData mValues;

    public ImageAdapter(Context context, MovieData mobileValues) {
        this.mContext = context;
        this.mValues = mobileValues;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View gridView;

        if (convertView == null) {

            gridView = new View(mContext);

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // get layout from mobile.xml
            gridView = inflater.inflate(R.layout.grid_layout, null);

        } else {
            gridView = (View) convertView;
        }

        // set value into textview
        TextView textView = (TextView) gridView
                .findViewById(R.id.grid_item_label);

        textView.setText(mValues.getItem(position).getTitle());

        // set image based on selected text
        ImageView imageView = (ImageView) gridView
                .findViewById(R.id.grid_item_image);

        //TODO - check below
        String posterpath = mValues.getItem(position).getPosterPath();
        if (posterpath != null) {
            Picasso.with(mContext).load(posterpath).into(imageView);
        } else {
            //throw up some generic image...
            imageView.setImageResource(R.drawable.android_logo);
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
