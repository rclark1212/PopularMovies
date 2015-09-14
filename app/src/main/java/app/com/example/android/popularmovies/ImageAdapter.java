package app.com.example.android.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by rclark on 8/29/2015.
 * Pulled from mkyong example for an image adapter class for use with gridview
 * http://www.mkyong.com/android/android-gridview-example/
 * Adapted for use with popularmovies
 * And note that the code was very buggy. Had to modify/fix to deal with convertview.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private MovieData mValues;

    public ImageAdapter(Context context, MovieData mobileValues) {
        this.context = context;
        this.mValues = mobileValues;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View gridView;

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context
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

        //TODO - fix below
        imageView.setImageResource(mValues.getItem(position).getPoster());

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
